
package com.esotericsoftware.tablelayout.android;

import java.util.List;

import com.esotericsoftware.tablelayout.Cell;

import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;

public class Table extends ViewGroup {
	static private final OnHierarchyChangeListener hierarchyChangeListener = new OnHierarchyChangeListener() {
		public void onChildViewAdded (View parent, View child) {
			((Table)parent).layout.otherChildren.add(child);
		}

		public void onChildViewRemoved (View parent, View child) {
			((Table)parent).layout.otherChildren.remove(child);
		}
	};

	final TableLayout layout;
	private boolean sizeToBackground;

	public Table () {
		this(new TableLayout());
	}

	public Table (TableLayout layout) {
		super(AndroidToolkit.context);
		this.layout = layout;
		layout.setTable(this);
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setOnHierarchyChangeListener(hierarchyChangeListener);
	}

	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		boolean widthUnspecified = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED;
		boolean heightUnspecified = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED;

		layout.setLayoutSize(0, 0, //
			widthUnspecified ? 0 : MeasureSpec.getSize(widthMeasureSpec), //
			heightUnspecified ? 0 : MeasureSpec.getSize(heightMeasureSpec));

		measureChildren(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		// Measure GONE children to 0x0.
		List<Cell> cells = layout.getCells();
		for (int i = 0, n = cells.size(); i < n; i++) {
			Cell c = cells.get(i);
			if (c.getIgnore()) continue;
			if (((View)c.getWidget()).getVisibility() == GONE) {
				((View)c.getWidget()).measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY));
			}
		}

		layout.layout();

		int measuredWidth = widthUnspecified ? layout.getMinWidth() : layout.getPrefWidth();
		int measuredHeight = heightUnspecified ? layout.getMinHeight() : layout.getPrefHeight();

		measuredWidth = Math.max(measuredWidth, getSuggestedMinimumWidth());
		measuredHeight = Math.max(measuredHeight, getSuggestedMinimumHeight());

		setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec), resolveSize(measuredHeight, heightMeasureSpec));
	}

	protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
		layout.setLayoutSize(0, 0, right - left, bottom - top);

		layout.layout();

		if (layout.getDebug() != TableLayout.DEBUG_NONE && layout.debugRects != null) {
			setWillNotDraw(false);
			invalidate();
		}
	}

	protected int getSuggestedMinimumWidth () {
		int width = layout.getMinWidth();
		if (sizeToBackground && getBackground() != null) width = Math.max(width, getBackground().getMinimumWidth());
		return width;
	}

	protected int getSuggestedMinimumHeight () {
		int height = layout.getMinHeight();
		if (sizeToBackground && getBackground() != null) height = Math.max(height, getBackground().getMinimumHeight());
		return height;
	}

	protected void dispatchDraw (Canvas canvas) {
		super.dispatchDraw(canvas);
		layout.drawDebug(canvas);
	}

	public void setSizeToBackground (boolean sizeToBackground) {
		this.sizeToBackground = sizeToBackground;
	}

	public TableLayout getTableLayout () {
		return layout;
	}
}