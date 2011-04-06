
package com.esotericsoftware.tablelayout.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JComponent;

public class Table extends JComponent {
	public final TableLayout layout;

	public Table () {
		this(new TableLayout());
	}

	public Table (final TableLayout layout) {
		this.layout = layout;
		layout.table = this;

		setLayout(new LayoutManager() {
			private Dimension minSize = new Dimension(), prefSize = new Dimension();

			public Dimension preferredLayoutSize (Container parent) {
				layout.layout(); // BOZO - Cache layout?
				prefSize.width = layout.tableMinWidth;
				prefSize.height = layout.tableMinHeight;
				return prefSize;
			}

			public Dimension minimumLayoutSize (Container parent) {
				layout.layout(); // BOZO - Cache layout?
				minSize.width = layout.tableMinWidth;
				minSize.height = layout.tableMinHeight;
				return minSize;
			}

			public void layoutContainer (Container ignored) {
				layout.layout();
			}

			public void addLayoutComponent (String name, Component comp) {
			}

			public void removeLayoutComponent (Component comp) {
			}
		});
	}
}
