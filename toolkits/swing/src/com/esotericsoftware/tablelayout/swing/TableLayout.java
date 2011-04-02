
package com.esotericsoftware.tablelayout.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.esotericsoftware.tablelayout.BaseTableLayout;

public class TableLayout extends BaseTableLayout<Component> {
	static {
		addClassPrefix("javax.swing.");
		addClassPrefix("java.awt.");
	}

	static Timer timer;
	static ArrayList<TableLayout> debugLayouts = new ArrayList(0);
	static BasicStroke debugDash = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {3}, 0);
	static BasicStroke debugSolid = new BasicStroke(1);

	final Container container = new Container();

	Container debugParent;
	ArrayList<DebugRect> debugRects;

	public TableLayout () {
		initialize();
	}

	public TableLayout (Container container, String tableText) {
		super(tableText);
		initialize();
	}

	private TableLayout (TableLayout parent) {
		super(parent);
		initialize();
	}

	private void initialize () {
		container.setLayout(new LayoutManager() {
			private Dimension minSize = new Dimension(), prefSize = new Dimension();

			public Dimension preferredLayoutSize (Container parent) {
				layout(); // BOZO - Cache layout?
				prefSize.width = totalPrefWidth;
				prefSize.height = totalPrefHeight;
				return prefSize;
			}

			public Dimension minimumLayoutSize (Container parent) {
				layout(); // BOZO - Cache layout?
				minSize.width = totalMinWidth;
				minSize.height = totalMinHeight;
				return minSize;
			}

			public void layoutContainer (Container parent) {
				if (debug != null) {
					debugParent = parent;
					if (debugRects == null) {
						debugRects = new ArrayList();
						debugLayouts.add(TableLayout.this);
					} else
						debugRects.clear();
				} else if (debugRects != null) {
					debugLayouts.remove(this);
					debugRects = null;
				}

				if (title != null && parent instanceof JComponent) {
					JComponent component = (JComponent)parent;
					Border border = component.getBorder();
					if (border == null || !(border instanceof TitledBorder) || !((TitledBorder)border).getTitle().equals(title))
						setTitle(component, title);
				}

				Insets insets = parent.getInsets();
				tableLayoutX = insets.left;
				tableLayoutY = insets.top;
				tableLayoutWidth = parent.getWidth() - insets.left - insets.right;
				tableLayoutHeight = parent.getHeight() - insets.top - insets.bottom;
				layout();
				ArrayList<Cell> cells = getCells();
				for (int i = 0, n = cells.size(); i < n; i++) {
					Cell c = cells.get(i);
					if (c.ignore) continue;
					Component component = (Component)c.widget;
					component.setLocation(c.widgetX, c.widgetY);
					component.setSize(c.widgetWidth, c.widgetHeight);
				}

				if (debug != null) {
					if (timer == null) {
						timer = new Timer("TableLayout Debug", true);
						timer.schedule(new TimerTask() {
							public void run () {
								if (!EventQueue.isDispatchThread()) {
									EventQueue.invokeLater(this);
									return;
								}
								for (TableLayout table : debugLayouts)
									table.drawDebug();
							}
						}, 100, 250);
					}
				}
			}

			public void addLayoutComponent (String name, Component comp) {
			}

			public void removeLayoutComponent (Component comp) {
			}
		});
	}

	public Container getContainer () {
		return container;
	}

	public void invalidate () {
		container.invalidate();
	}

	protected void addWidget (Component child) {
		container.add(child);
	}

	protected void removeWidget (Component child) {
		container.remove(child);
	}

	protected TableLayout newTableLayout () {
		return new TableLayout(this);
	}

	protected Component newLabel (String text) {
		return new JLabel(text);
	}

	protected void setTitle (Component parent, String title) {
		if (!(parent instanceof JComponent)) return;
		((JComponent)parent).setBorder(BorderFactory.createTitledBorder(title));
	}

	protected void addChild (Component parent, Component child, String layoutString) {
		if (parent instanceof JSplitPane && layoutString == null) {
			if (((JSplitPane)parent).getLeftComponent() instanceof JButton)
				layoutString = "left";
			else if (((JSplitPane)parent).getRightComponent() instanceof JButton) //
				layoutString = "right";
		}

		if (parent instanceof JScrollPane)
			((JScrollPane)parent).setViewportView(child);
		else
			((Container)parent).add(child, layoutString);
	}

	protected Component wrap (Object object) {
		if (object instanceof Component) return (Component)object;
		if (object instanceof TableLayout) return ((TableLayout)object).getContainer();
		if (object instanceof LayoutManager) return new JPanel((LayoutManager)object);
		if (object == null) return new JPanel();
		throw new IllegalArgumentException("Unknown object: " + object);
	}

	protected int getMinWidth (Component widget) {
		return widget.getMinimumSize().width;
	}

	protected int getMinHeight (Component widget) {
		return widget.getMinimumSize().height;
	}

	protected int getPrefWidth (Component widget) {
		return widget.getPreferredSize().width;
	}

	protected int getPrefHeight (Component widget) {
		return widget.getPreferredSize().height;
	}

	protected int getMaxWidth (Component widget) {
		return widget.getMaximumSize().width;
	}

	protected int getMaxHeight (Component widget) {
		return widget.getMaximumSize().height;
	}

	void drawDebug () {
		Graphics2D g = (Graphics2D)debugParent.getGraphics();
		if (g == null) return;
		g.setColor(Color.red);
		for (DebugRect rect : debugRects) {
			g.setColor(Color.red);
			g.setStroke(rect.dash ? debugDash : debugSolid);
			g.draw(rect);
		}
	}

	public void drawDebugRect (boolean dash, int x, int y, int w, int h) {
		if (debugRects != null) debugRects.add(new DebugRect(dash, x, y, w, h));
	}

	static private class DebugRect extends Rectangle {
		final boolean dash;

		public DebugRect (boolean dash, int x, int y, int width, int height) {
			super(x, y, width, height);
			this.dash = dash;
		}
	}
}
