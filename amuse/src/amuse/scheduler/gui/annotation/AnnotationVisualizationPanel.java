package amuse.scheduler.gui.annotation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import amuse.nodes.annotation.AnnotationAttribute;
import amuse.nodes.annotation.AnnotationAttributeType;
import amuse.nodes.annotation.AnnotationAttributeValue;
import amuse.nodes.annotation.AnnotationNominalAttribute;
import amuse.scheduler.gui.controller.AnnotationController;

/**
 * Displays a visualization of the annotation 
 * @author Frederik Heerde
 * @version $Id$
 */
public class AnnotationVisualizationPanel extends AnnotationScrollPane {

	public static final int DRAWHEIGHT_PER_ATTRIBUTE = 21;
	private double millisPerPixel;
	private ValuePanel<?> newValuePanel;

	public AnnotationVisualizationPanel(AnnotationController pAnnotationController) {
		super(pAnnotationController);
		millisPerPixel = 1;
		contentPanel.setLayout(null);
		newValuePanel = null;

		contentPanel.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				newValuePanel = null;
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
					AnnotationAttributeValue<?> value = annotationController.addNewValueToAttribute(att);
					annotationController.selectAttributeValue(value);
					value.setStart((int) (e.getX() * millisPerPixel));
					annotationController.selectAttributeValue(value);
					for (int i = 0; i < contentPanel.getComponentCount(); i++) {
						Component c = contentPanel.getComponent(i);
						if (c instanceof ValuePanel && ((ValuePanel<?>) c).getValue().equals(value)) {
							((ValuePanel<?>) c).refreshBounds();
							break;
						}
					}
				} else if (e.getClickCount() == 1 && att.getType() != AnnotationAttributeType.EVENT) {
					AnnotationAttributeValue<?> value = annotationController.addNewValueToAttribute(att);
					annotationController.selectAttributeValue(value);
					double totalMillis = annotationController.getDurationInMs();
					value.setStart((int) (e.getX() * millisPerPixel));
					value.setEnd((int) Math.min(value.getStart() + 1, totalMillis));

					for (int i = 0; i < contentPanel.getComponentCount(); i++) {
						Component c = contentPanel.getComponent(i);
						if (c instanceof ValuePanel && ((ValuePanel<?>) c).getValue().equals(value)) {
							newValuePanel = (ValuePanel<?>) c;
							break;
						}
					}
					annotationController.selectAttributeValue(value);
					newValuePanel.refreshBounds();
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
				if (newValuePanel != null) {
					int difference = (int) ((e.getX() - newValuePanel.getX() - newValuePanel.getWidth())
							* millisPerPixel);
					int newEnd = Math.min(newValuePanel.getValue().getEnd() + difference,
							(int) annotationController.getDurationInMs()); // No
																			// values
																			// longer
																			// than
																			// the
																			// song
					newEnd = Math.max(newEnd, newValuePanel.getValue().getStart() + 1); // No
																						// negative
																						// duration
					newValuePanel.getValue().setEnd(newEnd);
					newValuePanel.refreshBounds();

					// Refresh the values written in the lower part of the panel
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

	public void addValuePanel(AnnotationAttributeValue<?> value) {
		if (value != null) {
			ValuePanel<?> panel;
			switch (value.getAnnotationAttribute().getType()) {
			case EVENT:
				panel = new ValueEventPanel((AnnotationAttributeValue<Integer>) value);
				break;
			case NOMINAL:
				panel = new ValueNominalPanel((AnnotationAttributeValue<String>) value);
				break;
			case STRING:
				panel = new ValueStringPanel((AnnotationAttributeValue<String>) value);
				break;
			case NUMERIC:
				panel = new ValueNumericPanel((AnnotationAttributeValue<Double>) value);
				break;
			default:
				panel = new ValueTimePanel(value);
			}
			contentPanel.add(panel);
			panel.repaint();
		}
	}

	public void removeValuePanel(AnnotationAttributeValue<?> value) {
		if (value != null) {
			for (int i = 0; i < contentPanel.getComponentCount(); i++) {
				Component c = contentPanel.getComponent(i);
				if (c instanceof ValuePanel && ((ValuePanel<?>) c).getValue().equals(value)) {
					contentPanel.remove(c);
					contentPanel.repaint();
					return;
				}
			}
		}
	}

	public void drawContent(Graphics g) {
		int totalWidth = ((AnnotationView) annotationController.getView()).getAnnotationAudioSpectrumPanel()
				.getContentSize().width;
		double totalMillis = annotationController.getDurationInMs();
		millisPerPixel = totalMillis / totalWidth;

		for (int i = 0; i < contentPanel.getComponentCount(); i++) {
			((ValuePanel<?>) contentPanel.getComponent(i)).refreshBounds();
		}
	}

	@Override
	public Dimension getContentSize() {
		int height = ((DefaultListModel<?>) annotationController.getAttributeListModel()).size()
				* DRAWHEIGHT_PER_ATTRIBUTE + 3;
		return new Dimension(((AnnotationView) annotationController.getView()).getAnnotationAudioSpectrumPanel()
				.getContentSize().width, height);
	}

	private abstract class ValuePanel<T> extends JPanel {
		protected AnnotationAttributeValue<T> value;
		protected int mouseDragStart;

		public ValuePanel(AnnotationAttributeValue<T> pValue) {
			super();
			this.value = pValue;
			mouseDragStart = 0;
			this.refreshBounds();
			this.addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
					annotationController.selectAttributeValue(value);
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
					int totalWidth = getContentSize().width;
					double totalMillis = annotationController.getDurationInMs();
					double millisPerPixel = totalMillis / totalWidth;
					((AnnotationView) annotationController.getView())
							.setMouseTime((getX() + e.getX()) * millisPerPixel);
				}

				@Override
				public void mouseDragged(MouseEvent e) {
				}
			});
		}

		public abstract void refreshBounds();

		public AnnotationAttributeValue<T> getValue() {
			return value;
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			paintValuePanel(g);
		}

		protected abstract void paintValuePanel(Graphics g);

	}

	private class ValueTimePanel<T> extends ValuePanel<T> {

		public ValueTimePanel(AnnotationAttributeValue<T> pValue) {
			super(pValue);
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
					if (cursorType == Cursor.W_RESIZE_CURSOR) {
						int difference = (int) (e.getX() * millisPerPixel);
						int newStart = Math.max(value.getStart() + difference, 0); // No
																					// negative
																					// values
						newStart = Math.min(newStart, value.getEnd() - 1); // No
																			// negative
																			// duration
						value.setStart(newStart);
					} else if (cursorType == Cursor.E_RESIZE_CURSOR) {
						int difference = (int) ((e.getX() - getWidth()) * millisPerPixel);
						int newEnd = Math.min(value.getEnd() + difference,
								(int) annotationController.getDurationInMs()); // No
																				// values
																				// longer
																				// than
																				// the
																				// song
						newEnd = Math.max(newEnd, value.getStart() + 1); // No
																			// negative
																			// duration
						value.setEnd(newEnd);
					} else {
						int difference = (int) ((e.getX() - mouseDragStart) * millisPerPixel);
						int durationOfMusic = (int) annotationController.getDurationInMs();
						if (value.getStart() + difference <= 0) {
							value.setEnd(value.getEnd() - value.getStart());
							value.setStart(0);
						} else if (value.getEnd() + difference >= durationOfMusic) {
							value.setStart(durationOfMusic - value.getDuration());
							value.setEnd(durationOfMusic);
						} else {
							value.setStart(value.getStart() + difference);
							value.setEnd(value.getEnd() + difference);
						}
					}
					refreshBounds();
					annotationController.selectAttributeValue(value);

					// Refresh the values written in the lower part of the panel
					((AnnotationView) annotationController.getView()).getAnnotationSelectionPanel().repaint();
				}
			});

		}

		@Override
		public void refreshBounds() {
			this.setBounds(new Rectangle((int) (value.getStart() / millisPerPixel),
					annotationController.getAttributeListModel().indexOf(value.getAnnotationAttribute())
							* DRAWHEIGHT_PER_ATTRIBUTE,
					Math.max(1, (int) ((value.getEnd() - value.getStart()) / millisPerPixel)),
					DRAWHEIGHT_PER_ATTRIBUTE));
		}

		protected void paintValuePanel(Graphics g) {
			g.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);

			g.setColor(Color.WHITE);
			g.drawString(value.getValue() + "", 5, this.getHeight() - 5);

			g.setColor(Color.BLACK);
			g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
		}
	}

	private class ValueNominalPanel extends ValueTimePanel<String> {

		public ValueNominalPanel(AnnotationAttributeValue<String> pValue) {
			super(pValue);
		}

		@Override
		protected void paintValuePanel(Graphics g) {
			int indexOfSelectedValue = ((AnnotationNominalAttribute) value.getAnnotationAttribute()).getAllowedValues()
					.indexOf(value.getValue());
			g.setColor(new Color(Color.HSBtoRGB(indexOfSelectedValue * 0.3f, 0.8f, 0.8f)));
			super.paintValuePanel(g);
		}

	}

	private class ValueStringPanel extends ValueTimePanel<String> {

		public ValueStringPanel(AnnotationAttributeValue<String> pValue) {
			super(pValue);
		}

		@Override
		protected void paintValuePanel(Graphics g) {
			g.setColor(new Color(Color.HSBtoRGB(0.0f, 0.8f, 0.8f)));
			super.paintValuePanel(g);
		}

	}

	private class ValueNumericPanel extends ValueTimePanel<Double> {

		public ValueNumericPanel(AnnotationAttributeValue<Double> pValue) {
			super(pValue);
		}

		@Override
		protected void paintValuePanel(Graphics g) {
			g.setColor(new Color(Color.HSBtoRGB(0.0f, 0.8f, 0.8f)));
			super.paintValuePanel(g);
		}

	}

	private class ValueEventPanel extends ValuePanel<Integer> {

		public ValueEventPanel(AnnotationAttributeValue<Integer> pValue) {
			super(pValue);
			this.addMouseMotionListener(new MouseMotionListener() {

				@Override
				public void mouseMoved(MouseEvent e) {
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					int difference = (int) ((e.getX() - mouseDragStart) * millisPerPixel);
					int newStart = Math.max(value.getStart() + difference, 0); // No
																				// negative
																				// values
					newStart = Math.min(newStart, (int) annotationController.getDurationInMs()); // No
																									// values
																									// longer
																									// than
																									// the
																									// song
					value.setStart(newStart);

					annotationController.selectAttributeValue(value);
					refreshBounds();

					// Refresh the values written in the lower part of the panel
					((AnnotationView) annotationController.getView()).getAnnotationSelectionPanel().repaint();
				}
			});

		}

		@Override
		public void refreshBounds() {
			int locationX = (int) (value.getStart() / millisPerPixel);
			this.setBounds(new Rectangle(locationX - (DRAWHEIGHT_PER_ATTRIBUTE / 2),
					annotationController.getAttributeListModel().indexOf(value.getAnnotationAttribute())
							* DRAWHEIGHT_PER_ATTRIBUTE,
					DRAWHEIGHT_PER_ATTRIBUTE, DRAWHEIGHT_PER_ATTRIBUTE));
		}

		@Override
		protected void paintValuePanel(Graphics g) {
			g.setColor(new Color(200, 0, 0));
			g.fillPolygon(new Polygon(
					new int[] { 0, (this.getWidth() - 1) / 2, this.getWidth() - 1, (this.getWidth() - 1) / 2 },
					new int[] { (this.getHeight() - 1) / 2, 0, (this.getHeight() - 1) / 2, this.getHeight() - 1 }, 4));
			g.setColor(Color.BLACK);
			g.drawPolygon(new Polygon(
					new int[] { 0, (this.getWidth() - 1) / 2, this.getWidth() - 1, (this.getWidth() - 1) / 2 },
					new int[] { (this.getHeight() - 1) / 2, 0, (this.getHeight() - 1) / 2, this.getHeight() - 1 }, 4));
		}
	}

}
