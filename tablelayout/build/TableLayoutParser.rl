// Do not edit this file! Generated by Ragel.

package com.esotericsoftware.tablelayout;

import java.util.ArrayList;

class TableLayoutParser {
	static public void parse (BaseTableLayout table, String input) {
		char[] data = (input + "  ").toCharArray();
		int cs, p = 0, pe = data.length, eof = pe, top = 0;
		int[] stack = new int[4];

		int s = 0;
		String name = null;
		String widgetLayoutString = null;
		String className = null;

		int columnDefaultCount = 0;
		ArrayList<String> values = new ArrayList(4);
		ArrayList<Object> parents = new ArrayList(8);
		Cell cell = null, rowDefaults = null, columnDefaults = null;
		Object parent = table, widget = null;
		RuntimeException parseRuntimeEx = null;
		boolean hasColon = false;

		boolean debug = true;
		if (debug) System.out.println();

		try {
		%%{
			machine tableLayout;

			prepush {
				if (top == stack.length) {
					int[] newStack = new int[stack.length * 2];
					System.arraycopy(stack, 0, newStack, 0, stack.length);
					stack = newStack;
				}
			}

			action buffer { s = p; }
			action name {
				name = new String(data, s, p - s);
				s = p;
			}
			action value {
				values.add(new String(data, s, p - s));
			}
			action tableProperty {
				if (debug) System.out.println("tableProperty: " + name + " = " + values);
				((BaseTableLayout)parent).setTableProperty(name, values);
				values.clear();
				name = null;
			}
			action cellDefaultProperty {
				if (debug) System.out.println("cellDefaultProperty: " + name + " = " + values);
				table.setCellProperty(((BaseTableLayout)parent).cellDefaults, name, values);
				values.clear();
				name = null;
			}
			action startColumn {
				columnDefaults = ((BaseTableLayout)parent).getColumnDefaults(columnDefaultCount++);
			}
			action columnDefaultProperty {
				if (debug) System.out.println("columnDefaultProperty: " + name + " = " + values);
				table.setCellProperty(columnDefaults, name, values);
				values.clear();
				name = null;
			}
			action startRow {
				if (debug) System.out.println("startRow");
				rowDefaults = ((BaseTableLayout)parent).startRow();
			}
			action rowDefaultValue {
				if (debug) System.out.println("rowDefaultValue: " + name + " = " + values);
				table.setCellProperty(rowDefaults, name, values);
				values.clear();
				name = null;
			}
			action cellProperty {
				if (debug) System.out.println("cellProperty: " + name + " = " + values);
				table.setCellProperty(cell, name, values);
				values.clear();
				name = null;
			}
			action widgetLayoutString {
				if (debug) System.out.println("widgetLayoutString: " + new String(data, s, p - s).trim());
				widgetLayoutString = new String(data, s, p - s).trim();
			}
			action newWidgetClassName {
				className = new String(data, s, p - s);
			}
			action newWidget {
				if (debug) System.out.println("newWidget, name:" + name + " class:" + className + " widget:" + widget);
				if (widget != null) { // 'label' or ['label'] or [name:'label']
					if (name != null && name.length() > 0) table.register(name, widget);
				} else if (className == null) {
					if (name.length() > 0) {
						if (hasColon) { // [name:]
							widget = table.wrap(null);
							table.register(name, widget);
						} else { // [name]
							widget = table.getWidget(name);
							if (widget == null) {
								// Try the widget name as a class name.
								try {
									widget = table.newWidget(name);
								} catch (RuntimeException ex) {
									throw new IllegalArgumentException("Widget not found with name: " + name);
								}
							}
						}
					} else // []
						widget = table.wrap(null);
				} else { // [:class] and [name:class]
					widget = table.newWidget(className);
					if (name.length() > 0) table.register(name, widget);
				}
				name = null;
				className = null;
			}
			action newLabel {
				if (debug) System.out.println("newLabel: " + new String(data, s, p - s));
				widget = table.wrap(new String(data, s, p - s));
			}
			action startTable {
				if (debug) System.out.println("startTable, name:" + name);
				parents.add(parent);
				parent = table.newTableLayout(parent instanceof BaseTableLayout ? (BaseTableLayout)parent : null);
				if (name != null) { // [name:{}]
					table.register(name, ((BaseTableLayout)parent).getTable());
					name = null;
				}
				cell = null;
				widget = null;
				fcall table;
			}
			action endTable {
				widget = parent;
				if (!parents.isEmpty()) {
					if (debug) System.out.println("endTable");
					parent = parents.remove(parents.size() - 1);
					fret;
				}
			}
			action startStack {
				if (debug) System.out.println("startStack, name:" + name);
				parents.add(parent);
				parent = table.newStack();
				if (name != null) { // [name:<>]
					table.register(name, parent);
					name = null;
				}
				cell = null;
				widget = null;
				fcall stack;
			}
			action endStack {
				if (debug) System.out.println("endStack");
				widget = parent;
				parent = parents.remove(parents.size() - 1);
				fret;
			}
			action startWidgetSection {
				if (debug) System.out.println("startWidgetSection");
				parents.add(parent);
				parent = widget;
				widget = null;
				fcall widgetSection;
			}
			action endWidgetSection {
				if (debug) System.out.println("endWidgetSection");
				widget = parent;
				parent = parents.remove(parents.size() - 1);
				fret;
			}
			action addCell {
				if (debug) System.out.println("addCell");
				cell = ((BaseTableLayout)parent).addCell(table.wrap(widget));
			}
			action addWidget {
				if (debug) System.out.println("addWidget");
				table.addChild(parent, table.wrap(widget), widgetLayoutString);
				widgetLayoutString = null;
			}
			action widgetProperty {
				if (debug) System.out.println("widgetProperty: " + name + " = " + values);
				table.setProperty(parent, name, values);
				values.clear();
				name = null;
			}

			propertyValue =
				('-'? (alnum | '.' | '_')+ '%'?) >buffer %value |
				('\'' ^'\''* >buffer %value '\'');
			property = alnum+ >buffer %name (space* ':' space* propertyValue (',' propertyValue)* )?;

			startTable = '{' @startTable;
			startStack = '<' @startStack;
			label = '\'' ^'\''* >buffer %newLabel '\'';
			widget =
				# Widget name.
				'[' @{ widget = null; hasColon = false; } space* ^[\]:]* >buffer %name <:
				space* ':'? @{ hasColon = true; } space*
				(
					label | startTable | startStack |
					# Class name.
					(^[\]':{]+ >buffer %newWidgetClassName)
				)?
				space* ']' @newWidget;

			stack := space* ((widget | label | startTable | startStack) %addWidget space*)* '>' @endStack;

			startWidgetSection = '(' @startWidgetSection;
			widgetSection := space*
				# Widget properties.
				(property %widgetProperty (space+ property %widgetProperty)* space*)?
				(
					(
						# Widget contents.
						(widget | label | startTable | startStack) space*
						# Contents layout string.
						(space* <: (alnum | ' ')+ >buffer %widgetLayoutString )?
					) %addWidget <:
					# Contents properties.
					startWidgetSection? space*
				)* <:
				space* ')' @endWidgetSection;

			table = space*
				# Table properties.
				(property %tableProperty (space+ property %tableProperty)* space*)?
				# Default cell properties.
				('*' space* property %cellDefaultProperty (space+ property %cellDefaultProperty)* space*)?
				# Default column properties.
				('|' %startColumn space* (property %columnDefaultProperty (space+ property %columnDefaultProperty)* space*)? '|'? space*)*
				(
					# Start row and default row properties.
					('---' %startRow space* (property %rowDefaultValue (space+ property %rowDefaultValue)* )? )?
					(
						# Cell contents.
						space* (widget | label | startTable | startStack) %addCell space*
						# Cell properties.
						(property %cellProperty (space+ property %cellProperty)* space*)?
						# Contents properties.
						startWidgetSection? space*
					)+
				)+
				space* '}' @endTable;
			
			main := 
				space* '{'? <: table space*
			;

			write init;
			write exec;
		}%%
		} catch (RuntimeException ex) {
			parseRuntimeEx = ex;
		}

		if (p < pe) {
			int lineNumber = 1;
			for (int i = 0; i < p; i++)
				if (data[i] == '\n') lineNumber++;
			throw new IllegalArgumentException("Error parsing layout on line " + lineNumber + " near: " + new String(data, p, pe - p), parseRuntimeEx);
		} else if (top > 0) 
			throw new IllegalArgumentException("Error parsing layout (possibly an unmatched brace or quote): " + input, parseRuntimeEx);
	}

	%% write data;
}
