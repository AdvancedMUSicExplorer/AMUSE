package amuse.scheduler.gui.annotation.singlefile;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import amuse.scheduler.gui.annotation.singlefile.action.AnnotationAddAttributeEntryAction;
import amuse.scheduler.gui.annotation.singlefile.action.AnnotationEditAttributeEntryAction;
import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationAttribute;
import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationAttributeEntry;
import amuse.scheduler.gui.annotation.singlefile.attribute.AnnotationAttributeType;
import amuse.scheduler.gui.controller.SingleFileAnnotationController;

/**
 * Displays a visualization of the annotation 
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationVisualizationPanel extends AnnotationScrollPane {

	public static final int DRAWHEIGHT_PER_ATTRIBUTE = 21;
	private double secsPerPixel;
	private EntryPanel<?> newEntryPanel;
	private AnnotationAttributeEntry<?> selectedEntry;

	public AnnotationVisualizationPanel(SingleFileAnnotationController pAnnotationController, JList<AnnotationAttributeEntry<?>> entryList) {
		super(pAnnotationController);
		secsPerPixel = 1.;
		contentPanel.setLayout(null);
		newEntryPanel = null;
		
		selectedEntry = null;
		entryList.addListSelectionListener(e -> {
			if(selectedEntry != null){
				selectedEntry.getEntryPanel().setSelected(false);
				selectedEntry.getEntryPanel().repaint();
			}
			selectedEntry = entryList.getSelectedValue();
			if(selectedEntry != null){
				selectedEntry.getEntryPanel().setSelected(true);
				selectedEntry.getEntryPanel().repaint();
			}
		});
		
		contentPanel.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				if(newEntryPanel != null){
					annotationController.addUndoableAction(new AnnotationAddAttributeEntryAction(annotationController, newEntryPanel.getEntry()));
				}
				newEntryPanel = null;
			}

			@Override
			public void mousePressed(MouseEvent e) {
				int selectedIndex = e.getY() / DRAWHEIGHT_PER_ATTRIBUTE;
				if(selectedIndex >= annotationController.getAttributeListModel().size()){
					return;
				}
				AnnotationAttribute<?> att = annotationController.getAttributeListModel()
						.getElementAt(selectedIndex);
				if (e.getClickCount() >= 2 && att.getType() == AnnotationAttributeType.EVENT) {
					AnnotationAttributeEntry<?> entry = annotationController.addNewEntryToAttribute(att);
					annotationController.selectAttributeEntry(entry);
					entry.setStart(e.getX() * secsPerPixel);
					annotationController.selectAttributeEntry(entry);
					entry.getEntryPanel().refreshBounds();
					annotationController.addUndoableAction(new AnnotationAddAttributeEntryAction(annotationController, entry));
				} else if (e.getClickCount() == 1 && att.getType() != AnnotationAttributeType.EVENT) {
					AnnotationAttributeEntry<?> entry = annotationController.addNewEntryToAttribute(att);
					annotationController.selectAttributeEntry(entry);
					double totalSecs = annotationController.getDurationInSecs();
					entry.setStart(e.getX() * secsPerPixel);
					entry.setEnd(Math.min(entry.getStart() + 0.001, totalSecs));
					newEntryPanel = entry.getEntryPanel();
					newEntryPanel.refreshBounds();
					
					annotationController.selectAttributeEntry(entry);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});

		contentPanel.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (newEntryPanel != null) {
					AnnotationAttributeEntry<?> entry = newEntryPanel.getEntry();
					double maxTime = annotationController.getDurationInSecs();
					AnnotationAttributeEntry<?> nextEntry = entry.getNextEntry();
					if(nextEntry != null){
						maxTime = nextEntry.getStart();
					}
					double difference = ((e.getX() - newEntryPanel.getX() - newEntryPanel.getWidth())
							* secsPerPixel);
					// Bound the Start to the end of the previous entry or zero
					double newEnd = Math.min(entry.getEnd() + difference, maxTime); 
					newEnd = Math.max(newEnd, entry.getStart() + 0.001); // No negative duration
					entry.setEnd(newEnd);
					newEntryPanel.refreshBounds();

					// Refresh the entries written in the lower part of the panel
					((AnnotationView) annotationController.getView()).getAnnotationSelectionPanel().repaint();
				}
			}
		});

	}

	public void clearAnnotation() {
		contentPanel.removeAll();
		contentPanel.repaint();
		contentPanel.revalidate();
	}

	public void addEntryPanel(AnnotationAttributeEntry<?> entry) {
		if (entry != null) {
			EntryPanel<?> panel;
			switch (entry.getAnnotationAttribute().getType()) {
			case EVENT:
				panel = new EntryEventPanel((AnnotationAttributeEntry<Integer>) entry);
				break;
			case NOMINAL:
				panel = new EntryNominalPanel((AnnotationAttributeEntry<String>) entry);
				break;
			case STRING:
				panel = new EntryStringPanel((AnnotationAttributeEntry<String>) entry);
				break;
			case NUMERIC:
				panel = new EntryNumericPanel((AnnotationAttributeEntry<Double>) entry);
				break;
			default:
				panel = new EntryTimePanel(entry);
			}
			contentPanel.add(panel);
			panel.repaint();
		}
	}

	public void removeEntryPanel(AnnotationAttributeEntry<?> entry) {
		if (entry != null) {
			contentPanel.remove(entry.getEntryPanel());
			contentPanel.repaint();
		}
	}

	public void drawContent(Graphics g) {
		int totalWidth = ((AnnotationView) annotationController.getView()).getAnnotationAudioSpectrumPanel()
				.getContentSize().width;
		double totalSecs = annotationController.getDurationInSecs();
		secsPerPixel = totalSecs / totalWidth;

		for (int i = 0; i < contentPanel.getComponentCount(); i++) {
			((EntryPanel<?>) contentPanel.getComponent(i)).refreshBounds();
		}
	}

	@Override
	public Dimension getContentSize() {
		int height = ((DefaultListModel<?>) annotationController.getAttributeListModel()).size()
				* DRAWHEIGHT_PER_ATTRIBUTE + 3;
		return new Dimension(((AnnotationView) annotationController.getView()).getAnnotationAudioSpectrumPanel()
				.getContentSize().width, height);
	}

	public abstract class EntryPanel<T> extends JPanel {
		protected AnnotationAttributeEntry<T> entry;
		protected double oldStart, oldEnd;
		protected Object oldValue;
		protected int mouseDragStart;
		protected boolean isSelected;

		public EntryPanel(AnnotationAttributeEntry<T> entry) {
			super();
			isSelected = false;
			entry.setEntryPanel(this);
			this.setOpaque(false);
			this.entry = entry;
			oldStart = entry.getStart();
			oldEnd = entry.getEnd();
			oldValue = entry.getValue();
			mouseDragStart = 0;
			this.refreshBounds();
			this.addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) {
					if(oldStart != entry.getStart()
							|| oldEnd != entry.getEnd()
							|| oldValue != entry.getValue()){
						annotationController.addUndoableAction(new AnnotationEditAttributeEntryAction(annotationController, entry, oldStart, oldEnd, oldValue));
					}
				}

				@Override
				public void mousePressed(MouseEvent e) {
					annotationController.selectAttributeEntry(entry);
					oldStart = entry.getStart();
					oldEnd = entry.getEnd();
					oldValue = entry.getValue();
					mouseDragStart = e.getX();
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseClicked(MouseEvent e) {
				}
			});
			this.addMouseMotionListener(new MouseMotionListener() {

				@Override
				public void mouseMoved(MouseEvent e) {
					((AnnotationView) annotationController.getView())
							.setMouseTime((getX() + e.getX()) * secsPerPixel);
				}

				@Override
				public void mouseDragged(MouseEvent e) {
				}
			});
		}
		
		public void setSelected(boolean isSelected){
			this.isSelected = isSelected;
		}

		public abstract void refreshBounds();

		public AnnotationAttributeEntry<T> getEntry() {
			return entry;
		}

		@Override
		public void paintComponent(Graphics g) {
			int indexOfSelectedValue = this.getBounds().y / DRAWHEIGHT_PER_ATTRIBUTE;
			g.setColor(new Color(Color.HSBtoRGB(indexOfSelectedValue * 0.16667f, 0.8f, 0.7f)));
			//g.setColor(new Color(Color.HSBtoRGB(0.0f, 0.8f, 0.8f)));
			super.paintComponent(g);
			paintEntryPanel(g);
		}

		protected abstract void paintEntryPanel(Graphics g);

	}

	public class EntryTimePanel<T> extends EntryPanel<T> {

		public EntryTimePanel(AnnotationAttributeEntry<T> pEntry) {
			super(pEntry);
			this.addMouseMotionListener(new MouseMotionListener() {

				@Override
				public void mouseMoved(MouseEvent e) {
					final int THRESHOLD = 5;
					if (getWidth() - e.getX() < THRESHOLD) {
						setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
					} else if (e.getX() < THRESHOLD) {
						setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
					} else {
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					int cursorType = getCursor().getType();
					double minTime = 0;
					double maxTime = annotationController.getDurationInSecs();
					AnnotationAttributeEntry<?> nextEntry = entry.getNextEntry();
					if(nextEntry != null){
						maxTime = nextEntry.getStart();
					}
					AnnotationAttributeEntry<?> previousEntry = entry.getPreviousEntry();
					if(previousEntry != null){
						minTime = previousEntry.getEnd();
					}
					if (cursorType == Cursor.W_RESIZE_CURSOR) {
						double difference = e.getX() * secsPerPixel;
						// Bound the Start to the end of the previous entry or zero
						double newStart = Math.max(entry.getStart() + difference, minTime); 
						// No negative duration
						newStart = Math.min(newStart, entry.getEnd() - 0.001); 
						entry.setStart(newStart);
					} else if (cursorType == Cursor.E_RESIZE_CURSOR) {
						double difference = (e.getX() - getWidth()) * secsPerPixel;
						// Bound the Start to the end of the next entry or track duration
						double newEnd = Math.min(entry.getEnd() + difference, maxTime); 
						// No negative duration
						newEnd = Math.max(newEnd, entry.getStart() + 0.001); 
						entry.setEnd(newEnd);
					} else {
						double difference = (e.getX() - mouseDragStart) * secsPerPixel;
						if (entry.getStart() + difference <= minTime) {
							entry.setEnd(minTime + entry.getEnd() - entry.getStart());
							entry.setStart(minTime);
						} else if (entry.getEnd() + difference >= maxTime) {
							entry.setStart(maxTime - entry.getDuration());
							entry.setEnd(maxTime);
						} else {
							entry.setStart(entry.getStart() + difference);
							entry.setEnd(entry.getEnd() + difference);
						}
					}
					refreshBounds();
					annotationController.selectAttributeEntry(entry);

					// Refresh the entries written in the lower part of the panel
					((AnnotationView) annotationController.getView()).getAnnotationSelectionPanel().repaint();
				}
			});

		}

		@Override
		public void refreshBounds() {
			this.setBounds(new Rectangle((int) (entry.getStart() / secsPerPixel),
					annotationController.getAttributeListModel().indexOf(entry.getAnnotationAttribute()) * DRAWHEIGHT_PER_ATTRIBUTE,
					Math.max(2, (int) ((entry.getEnd() - entry.getStart()) / secsPerPixel)),
					DRAWHEIGHT_PER_ATTRIBUTE));
		}

		protected void paintEntryPanel(Graphics g) {
			int width = this.getWidth();
			int height = this.getHeight();
			
			g.fillRect(0, 0, width - 1, height - 1);

			g.setColor(Color.WHITE);
			g.drawString(entry.getValue() + "", Math.max(5, getViewport().getViewPosition().x - getX() + 5), height - 5);
			
			g.setColor(Color.BLACK);
			g.drawRect(0, 0, width - 1, height - 1);
			if(isSelected){
				g.drawRect(1, 1, width - 3, height - 3);
				g.drawRect(2, 2, width - 5, height - 5);
			}
			
		}
	}

	public class EntryNominalPanel extends EntryTimePanel<String> {

		public EntryNominalPanel(AnnotationAttributeEntry<String> entry) {
			super(entry);
		}

		@Override
		protected void paintEntryPanel(Graphics g) {
			//int indexOfSelectedValue = ((AnnotationNominalAttribute) entry.getAnnotationAttribute()).getAllowedValues()
			//		.indexOf(entry.getValue());
			//g.setColor(new Color(Color.HSBtoRGB(indexOfSelectedValue * 0.3f, 0.8f, 0.8f)));
			super.paintEntryPanel(g);
		}

	}

	public class EntryStringPanel extends EntryTimePanel<String> {

		public EntryStringPanel(AnnotationAttributeEntry<String> entry) {
			super(entry);
		}

		@Override
		protected void paintEntryPanel(Graphics g) {
			super.paintEntryPanel(g);
		}

	}

	public class EntryNumericPanel extends EntryTimePanel<Double> {

		public EntryNumericPanel(AnnotationAttributeEntry<Double> entry) {
			super(entry);
		}

		@Override
		protected void paintEntryPanel(Graphics g) {
			super.paintEntryPanel(g);
		}

	}

	public class EntryEventPanel extends EntryPanel<Integer> {

		public EntryEventPanel(AnnotationAttributeEntry<Integer> pEntry) {
			super(pEntry);
			this.addMouseMotionListener(new MouseMotionListener() {

				@Override
				public void mouseMoved(MouseEvent e) {
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					double difference = ((e.getX() - mouseDragStart) * secsPerPixel);
					double newStart = Math.max(entry.getStart() + difference, 0); // No negative values
					newStart = Math.min(newStart, annotationController.getDurationInSecs()); // No values longer than the track
					entry.setStart(newStart);

					annotationController.selectAttributeEntry(entry);
					refreshBounds();

					// Refresh the values written in the lower part of the panel
					((AnnotationView) annotationController.getView()).getAnnotationSelectionPanel().repaint();
				}
			});

		}

		@Override
		public void refreshBounds() {
			int locationX = (int) (entry.getStart() / secsPerPixel);
			this.setBounds(new Rectangle(locationX - (DRAWHEIGHT_PER_ATTRIBUTE / 2),
					annotationController.getAttributeListModel().indexOf(entry.getAnnotationAttribute())
							* DRAWHEIGHT_PER_ATTRIBUTE,
					DRAWHEIGHT_PER_ATTRIBUTE, DRAWHEIGHT_PER_ATTRIBUTE));
		}

		@Override
		protected void paintEntryPanel(Graphics g) {
			g.fillPolygon(new Polygon(
					new int[] { 0, (this.getWidth() - 1) / 2, this.getWidth() - 1, (this.getWidth() - 1) / 2 },
					new int[] { (this.getHeight() - 1) / 2, 0, (this.getHeight() - 1) / 2, this.getHeight() - 1 }, 4));
			g.setColor(Color.BLACK);

			if(isSelected){
				g.drawPolygon(new Polygon(
						new int[] { 1, (this.getWidth() - 1) / 2, this.getWidth() - 2, (this.getWidth() - 1) / 2},
						new int[] { (this.getHeight() - 1) / 2, 1, (this.getHeight() - 1) / 2, this.getHeight() - 2 }, 4));
				g.drawPolygon(new Polygon(
						new int[] { 2, (this.getWidth() - 1) / 2, this.getWidth() - 3, (this.getWidth() - 1) / 2},
						new int[] { (this.getHeight() - 1) / 2, 2, (this.getHeight() - 1) / 2, this.getHeight() - 3 }, 4));
			}
			
			g.drawPolygon(new Polygon(
					new int[] { 0, (this.getWidth() - 1) / 2, this.getWidth() - 1, (this.getWidth() - 1) / 2 },
					new int[] { (this.getHeight() - 1) / 2, 0, (this.getHeight() - 1) / 2, this.getHeight() - 1 }, 4));
		}
	}

}
