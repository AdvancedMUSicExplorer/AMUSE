/**
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 *
 * Copyright 2006-2010 by code authors
 *
 * Created at TU Dortmund, Chair of Algorithm Engineering
 * (Contact: <http://ls11-www.cs.tu-dortmund.de>)
 *
 * AMUSE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMUSE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with AMUSE. If not, see <http://www.gnu.org/licenses/>.
 *
 * Creation date: 15.01.2009
 */
package amuse.data.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import amuse.data.io.attributes.Attribute;
import amuse.data.io.attributes.NominalAttribute;
import amuse.data.io.attributes.NumericAttribute;
import amuse.data.io.attributes.StringAttribute;

/**
 * DataSet is used to represent data vectors in Amuse and is closely related to
 * the <a
 * href="http://weka.sourceforge.net/wekadoc/index.php/en:ARFF_(3.4.6)">ARFF</a>
 * file format, developed by the <a href="http://www.waikato.ac.nz/">University
 * of Waikato</a>. <br/>
 * Each DataSet has a name and a set of attributes. Each attribute corresponds
 * to a data vector with values according to its type. All attributes have
 * exactly the same amount of values, in other words each data vector of a
 * DataSet has the same length.
 * 
 * @author Clemens Waeltken
 * @version $Id$
 */
public class ArffDataSet extends DataSetAbstract {

	private File file;
	private LineNumberReader reader;
	/** the number of lines read so far */
	protected int lines;
	private int bufferedLineCount = 1024 * 8;
	private static final int defaultBufferdLineCount = 1024 * 8;
	private int dataTagLine;
	private int startLine = -1;
	private int endLine = -1;
	private double[][] bufferedLines;
	/**
	 * This String stores the @Relation-Tag as used in ARFF files.
	 */
	protected static final String relationStr = "@RELATION";
	/**
	 * This String stores the @Data-Tag as used in ARFF files, denoting the
	 * beginning of the Data section.
	 */
	protected static final String dataStr = "@DATA";
	/**
	 * This Map stores all AmuseAttributes in this DataSet. AmuseAttributes
	 * extend the standard ARFF format allowing to save different meta
	 * information for the DataSet.
	 */
	private final Map<String, String> amuseAttributes = new HashMap<String, String>();
	private int valueCount = 0;
	private StreamTokenizer tokenizer;
	private List<Integer> emptyLines = new ArrayList<Integer>();

	/**
	 * The standard constructor to create a new DataSet.
	 * 
	 * @param name
	 *            The name of the new DataSet.
	 */
	public ArffDataSet(String name) {
		this.name = name;
	}

	/**
	 * This constructor loads a DataSet from a given arff file. IOExceptions are
	 * thrown due to IO operations. A standart amount of 1000 lines are held in
	 * memory.
	 * 
	 * @param file
	 *            The arff file to load from.
	 * @throws java.io.IOException
	 *             Thrown whenever given file is not a valid arff or not
	 *             existing.
	 */
	public ArffDataSet(File file) throws IOException {
		this(file, defaultBufferdLineCount);
	}

	/**
	 * This constructor loads a DataSet from a given arff file. IOExceptions are
	 * thrown due to IO operations.
	 * 
	 * @param file
	 *            The arff file to load from.
	 * @param linesToCache
	 *            The count of lines to hold in memory.
	 * @throws java.io.IOException
	 *             Thrown whenever given file is not a valid arff or not
	 *             existing.
	 */
	public ArffDataSet(File file, int linesToCache) throws IOException {
		// Check preconditions:
		bufferedLineCount = linesToCache;
		if (!file.isFile()) {
			throw new FileNotFoundException(file.getCanonicalPath());
		}
		// set file Variable:
		this.file = file;
		// Setup states for reading of header:
		String tmpName = null;
		// Initialise LineNumberReader:
		initTokenizer();
		// Read Header:
		// Get name of relation.
		readArffAttributes();
		getFirstToken();
		if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
			errorMessage("premature end of file");
		}
		if (relationStr.equalsIgnoreCase(tokenizer.sval)) {
			getNextToken();
			tmpName = tokenizer.sval;
			getLastToken(false);
		} else {
			errorMessage("keyword " + relationStr + " expected");
		}

		// Get attribute declarations.
		getFirstToken();
		if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
			errorMessage("premature end of file");
		}

		while (Attribute.attributeStr.equalsIgnoreCase(tokenizer.sval)) {
			parseAttribute();
		}

		// Check if data part follows. We can't easily check for EOL.
		if (!dataStr.equalsIgnoreCase(tokenizer.sval)) {
			errorMessage("keyword " + dataStr + " expected");
		}

		dataTagLine = reader.getLineNumber();
		// System.out.println("DataTagAt: " + dataTagLine);

		// Check if any attributes have been declared.
		if (attributes.size() == 0) {
			errorMessage("no attributes declared");
		}

		// System.out.println("\n\n\n");
		this.name = tmpName;
		bufferedLines = new double[bufferedLineCount][this.getAttributeNames()
				.size()];
		validateHeader();
		validateDataSection();
		reader.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amuse.data.io.DataSetAbstract#getValueCount()
	 */
	public final int getValueCount() {
		if (file == null) {
			if (attributes.isEmpty()) {
				return -1;
			}
			return attributes.get(0).getValueCount();
		} else {
			return this.valueCount;
		}
	}
	
	

	/**
	 * This method saves this DataSet as ARFF in given file.
	 * 
	 * @param file
	 *            The file to save to.
	 * @throws java.io.IOException
	 *             Maybe thrown due to IO.
	 */
	public final void saveToArffFile(File file) throws IOException {
		if (!isValid()) {
			throw new IOException("Illegal value count in DataSet: "
					+ this.name);
		}
		if (file == this.file) {
			throw new IOException("Can not write/read to/from same file!");
		}
		File outputFile = new File(file.getAbsolutePath());
		StringBuilder output = new StringBuilder();
		// Write Amuse attributes:
		for (String key : amuseAttributes.keySet()) {
			output.append("%@" + key + "=" + amuseAttributes.get(key) + "\n");
		}
		// Write @RELATION name:
		output.append(this.getRelationHeaderStr() + "\n\n");
		// Write @ATTRIBUTE Headers:
		for (Attribute atr : attributes) {
			output.append(atr.getHeaderStr() + "\n");
		}
		// Begin writing data to StringBuilder:
		output.append("\n" + dataStr + "\n");
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile));
		fileWriter.append(output);
		output = new StringBuilder();
		for (int i = 0; i < getValueCount(); i++) {
			for (Attribute atr : attributes) {
				output.append(atr.getValueStrAt(i));
				if (attributes.indexOf(atr) == attributes.size() - 1) {
					output.append("\n");
				} else {
					output.append(",");
				}
			}
			
			// Write the attribute values of the current instance to file:
			fileWriter.append(output);
			output = new StringBuilder();
		}
		fileWriter.close();
	}

	/**
	 * Adds Amuse-specific metadata to this DataSet. If key already exists its
	 * value will be overwritten.
	 * 
	 * @param key
	 *            The key to store under.
	 * @param value
	 *            The value to store under given key.
	 */
	public final void addAmuseAttribute(String key, String value) {
		amuseAttributes.put(key, value);
	}

	/**
	 * Returns the value of an AmuseAttribute under given key. If the key does
	 * not store any value <code>null</code> will be returned.
	 * 
	 * @param key
	 *            The key to lookup.
	 * @return The value at <code>key</code>.
	 * @throws DataSetException
	 *             Thrown whenever you try to get an AmuseAttribute wich is not
	 *             stored in this DataSet.
	 */
	public final String getAmuseAttribute(String key) throws DataSetException {
		if (!amuseAttributes.containsKey(key)) {
			throw new DataSetException("No such AmuseAttribute!");
		}
		return amuseAttributes.get(key);
	}

	/**
	 * Returns double representation of value of attribute in index line.
	 * 
	 * @param index
	 * @param attribute
	 * @return
	 */
	public double getValueFor(int index, Attribute attribute) {
		// if (!attributes.contains(attribute)) {
		// throw new IllegalArgumentException("No such Attribute in this set: "
		// + attribute.getName());
		// }
		// if (index < 0 || index >= valueCount) {
		// throw new IndexOutOfBoundsException("The index: \"" + index +
		// "\" is out of bounds of this DataSet.");
		// }
		assertInBuffer(index);
		return bufferedLines[calculateBufferIndex(index)][this
				.getAttributeNames().indexOf(attribute.getName())];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amuse.data.io.DataSetAbstract#setValueAt(int,
	 * amuse.data.io.Attribute, java.lang.Object)
	 */
	public void setValueAt(int index, Attribute attribute, Object value) {
		/*assertInBuffer(index);
		bufferedLines[calculateBufferIndex(index)][this
				.getAttributeNames().indexOf(attribute.getName())] = 0.;
		throw new NotImplementedException();*/
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * amuse.data.io.DataSetAbstract#addValue(amuse.data.io.Attribute,
	 * java.lang.Object)
	 */
	public void addValue(Attribute attribute, Object value) {
		throw new UnsupportedOperationException();
	}

	private void assertInBuffer(int index) {
		if (startLine <= index && index < endLine) {
			return;
		} else {
			try {
				// Move Buffer:
				startLine = index;
				initTokenizer(startLine + dataTagLine);
				int bufferIndex = 0;
				getFirstToken();
				while (bufferIndex < bufferedLines.length
						&& tokenizer.ttype != StreamTokenizer.TT_EOF) {
					bufferedLines[bufferIndex] = parseDataLine();
					bufferIndex++;
					getFirstToken();
				}
				endLine = startLine + bufferIndex;
				reader.close();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private int calculateBufferIndex(int index) {
		return index - startLine;
	}

	private boolean isEmptyString(String line) {
		if (!line.isEmpty() && !line.trim().startsWith("%")) {
			return false;
		} else {
			return true;
		}
	}

	private String getRelationHeaderStr() {
		if (name.indexOf(' ') != -1) {
			return relationStr + " \"" + name + "\"";
		}
		return relationStr + " " + name;
	}

	private void initReader(int line) throws IOException {
		reader = new LineNumberReader(new FileReader(file));
		reader.setLineNumber(0);
		for (int i = 0; i < line; i++) {
			reader.readLine();
		}
	}

	private boolean isValid() {
		if (attributes.size() > 0) {
			int values = attributes.get(0).getValueCount();
			for (Attribute at : attributes) {
				if (at.getValueCount() != values) {
					return false;
				}
			}
		}
		return true;
	}

	private void validateDataSection() throws IOException, DataSetException {
		initTokenizer(dataTagLine);
		getFirstToken();
		while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
			parseDataLine(true);
			getFirstToken();
		}
		// System.out.println(Arrays.toString(emptyLines.toArray()));
	}

	private void validateHeader() throws IOException {
		if (this.name == null) {
			throw new IOException("Missing Relation Name!");
		}
		if (this.getAttributeNames().isEmpty()) {
			throw new IOException("This file does not contain Attributes!");
		}
		// if (this.getValueCount() <= 0) {
		// throw new IOException("No vaules present!");
		// }
	}

	private void initTokenizer() throws IOException {
		initTokenizer(0);
	}

	/**
	 * Initializes the StreamTokenizer used for reading the ARFF file.
	 * 
	 * @throws IOException
	 */
	private void initTokenizer(int marker) throws IOException {
		initReader(marker);
		tokenizer = new StreamTokenizer(reader);
		tokenizer.resetSyntax();
		tokenizer.whitespaceChars(0, ' ');
		tokenizer.wordChars(' ' + 1, '\u00FF');
		tokenizer.whitespaceChars(',', ',');
		tokenizer.commentChar('%');
		tokenizer.quoteChar('"');
		tokenizer.quoteChar('\'');
		tokenizer.ordinaryChar('{');
		tokenizer.ordinaryChar('}');
		tokenizer.eolIsSignificant(true);
	}

	/**
	 * Gets next token, skipping empty lines.
	 * 
	 * @throws IOException
	 *             if reading the next token fails
	 */
	private void getFirstToken() throws IOException {
		while (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
			emptyLines.add(reader.getLineNumber());
		}

		if ((tokenizer.ttype == '\'') || (tokenizer.ttype == '"')) {
			tokenizer.ttype = StreamTokenizer.TT_WORD;
		} else if ((tokenizer.ttype == StreamTokenizer.TT_WORD)
				&& (tokenizer.sval.equals("?"))) {
			tokenizer.ttype = '?';
		}
	}

	public int getLineNo() {
		return lines + tokenizer.lineno();
	}

	private void getLastToken(boolean endOfFileOk) throws IOException {
		if ((tokenizer.nextToken() != StreamTokenizer.TT_EOL)
				&& ((tokenizer.ttype != StreamTokenizer.TT_EOF) || !endOfFileOk)) {
			errorMessage("end of line expected");
		}
	}

	/**
	 * Reads and skips all tokens before next end of line token.
	 * 
	 * @throws IOException
	 *             in case something goes wrong
	 */
	protected void readTillEOL() throws IOException {
		while (tokenizer.nextToken() != StreamTokenizer.TT_EOL) {
		}
		tokenizer.pushBack();
	}

	/**
	 * Gets next token, checking for a premature and of line.
	 * 
	 * @throws IOException
	 *             if it finds a premature end of line
	 */
	private void getNextToken() throws IOException {
		if (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
			errorMessage("premature end of line");
		}
		if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
			errorMessage("premature end of file");
		} else if ((tokenizer.ttype == '\'') || (tokenizer.ttype == '"')) {
			tokenizer.ttype = StreamTokenizer.TT_WORD;
		} else if ((tokenizer.ttype == StreamTokenizer.TT_WORD)
				&& (tokenizer.sval.equals("?"))) {
			tokenizer.ttype = '?';
		}
	}

	/**
	 * Throws error message with line number and last token read.
	 * 
	 * @param msg
	 *            the error message to be thrown
	 * @throws IOException
	 *             containing the error message
	 */
	private void errorMessage(String msg) throws IOException {
		String str = msg + ", read '" + tokenizer.sval + "' in line " + reader.getLineNumber() + ": " + tokenizer.lineno();
		this.initReader(reader.getLineNumber() - 1);
		str += reader.readLine();
		if (lines > 0) {
			int line = Integer.parseInt(str.replaceAll(".* line ", ""));
			str = str.replaceAll(" line .*", " line " + (lines + line - 1));
		}
		throw new IOException(str);
	}

	private void parseAttribute() throws IOException {
		String attributeName;

		// Get attribute name.
		getNextToken();
		attributeName = tokenizer.sval;
		getNextToken();

		// Check if attribute is nominal.
		if (tokenizer.ttype == StreamTokenizer.TT_WORD) {

			// Attribute is real, integer, or string.
			if (tokenizer.sval.equalsIgnoreCase(NumericAttribute.typeStr)) {
				attributes.add(new NumericAttribute(attributeName, this));
				readTillEOL();
			} else if (tokenizer.sval.equalsIgnoreCase(StringAttribute.typeStr)) {
				attributes.add(new StringAttribute(attributeName, this));
				readTillEOL();
			} else if (tokenizer.sval.equalsIgnoreCase("integer")
					|| tokenizer.sval.equalsIgnoreCase("real")
					|| tokenizer.sval.equalsIgnoreCase("date")
					|| tokenizer.sval.equalsIgnoreCase("relational")) {
				// AmuseLogger.write
				System.out
						.println("AmuseArff will not handle this Attribute type.");
			}
		} else {

			// Attribute is nominal.
			ArrayList<String> nominalValues = new ArrayList<String>();
			tokenizer.pushBack();

			// Get values for nominal attribute.
			if (tokenizer.nextToken() != '{') {
				errorMessage("{ expected at beginning of enumeration");
			}
			while (tokenizer.nextToken() != '}') {
				if (tokenizer.ttype == StreamTokenizer.TT_EOL) {
					errorMessage("} expected at end of enumeration");
				} else {
					nominalValues.add(tokenizer.sval);
				}
			}
			attributes.add(new NominalAttribute(attributeName, nominalValues,
					this));
		}
		getLastToken(false);
		getFirstToken();
		if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
			errorMessage("premature end of file");
		}
	}

	private void readArffAttributes() throws IOException {
		LineNumberReader lnr = null;
		try{
			lnr = new LineNumberReader(new FileReader(file));
			String line = lnr.readLine();
			while (line != null && !line.trim().startsWith(dataStr)) {
				if (line.startsWith("%@")) {
					if (!line.contains("=")) {
						throw new IOException("Missing \"=\" in line: "
								+ reader.getLineNumber());
					}
					this.addAmuseAttribute(line.substring(0, line.indexOf("=")),
							line.substring(line.indexOf("=") + 1));
				}
				line = lnr.readLine();
			}
		}
		catch(IOException e){
			throw e;
		}
		finally{
			if(lnr != null){
				lnr.close();
			}
		}
	}

	private double[] parseDataLine() throws IOException {
		return parseDataLine(false);
	}

	private double[] parseDataLine(boolean validating) throws IOException {
		double[] dataLine = new double[getAttributeCount()];
		int index;

		// Get values for all attributes.
		for (int i = 0; i < getAttributeCount(); i++) {
			// Get next token
			if (i > 0) {
				getNextToken();
			}

			// Check if value is missing.
			if (tokenizer.ttype == '?') {
				dataLine[i] = Attribute.missingValue();
			} else {

				// Check if token is valid.
				if (tokenizer.ttype != StreamTokenizer.TT_WORD) {
					errorMessage("not a valid value");
				}
				Attribute attribute = getAttribute(i);
				if (attribute instanceof NominalAttribute) {
					// Check if value appears in header.
					index = ((NominalAttribute) attribute)
							.indexOfValue(tokenizer.sval);
					if (index == -1) {
						errorMessage("nominal value not declared in header");
					}
					dataLine[i] = (double) index;
				} else if (attribute instanceof NumericAttribute) {
					// Check if value is really a number.
					try {
						dataLine[i] = Double.valueOf(tokenizer.sval)
								.doubleValue();
					} catch (NumberFormatException e) {
						errorMessage("number expected");
					}
				} else if (attribute instanceof StringAttribute) {
					dataLine[i] = ((StringAttribute) attribute)
							.addStringValue(tokenizer.sval);
				} else {
					errorMessage("unknown attribute type in column " + i
							+ " line " + reader.getLineNumber());
				}
			}
		}
		if (validating) {
			valueCount++;
		}
		tokenizer.nextToken();
		return dataLine;
	}
}
