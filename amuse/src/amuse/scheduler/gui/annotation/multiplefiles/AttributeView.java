package amuse.scheduler.gui.annotation.multiplefiles;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumnModel;




public class AttributeView extends JScrollPane{
	
	JPanel contentPanel;
	
	public AttributeView(TableColumnModel columnModel){
		super(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.setViewportView(contentPanel);
		columnModel.addColumnModelListener(new TableColumnModelListener() {
			
			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void columnRemoved(TableColumnModelEvent e) {//TODO
				int start = e.getFromIndex();
				for(int i = start; i < e.getToIndex(); i++){
					remove(start);
				}
			}
			
			@Override
			public void columnMoved(TableColumnModelEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void columnMarginChanged(ChangeEvent e) { }
			
			@Override
			public void columnAdded(TableColumnModelEvent e) {
				// TODO Auto-generated method stub
				add(new JLabel("" + e.getToIndex()), e.getToIndex());
				revalidate();
				repaint();
			}
		});
	}
}
