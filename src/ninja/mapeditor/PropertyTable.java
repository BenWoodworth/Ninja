package ninja.mapeditor;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;

import ninja.entity.Entity;
import ninja.entity.EntityUtil;

public class PropertyTable extends JTable {
	private static final long serialVersionUID = -7351636114888226110L;
	
	protected List<String> properties;
	protected List<Class<?>> types;
	protected List<Object> values;

	public List<Object> getValues(){ return values; }
	public PropertyTable(){ this(new ArrayList<String>(), new ArrayList<Class<?>>()); }
	public PropertyTable(List<String> properties, List<Class<?>> types){
		super();
		this.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		setProperties(properties, types);
	}

	public List<String> getProperties(){ return properties; }
	public void setProperties(List<String> properties, List<Class<?>> types){
		this.stopEditing();
		this.properties = properties;
		this.types = types;
		values = new ArrayList<Object>();
		for (Class<?> c : types) values.add(getDefaultValue(c));
		this.setModel(new MyTableModel(properties, types));
	}
	public void setProperties(List<String> properties, List<Class<?>> types, List<Object> values){
		this.values = values;
		this.properties = properties;
		this.types = types;
		this.setModel(new MyTableModel(properties, types));
	}
	public void setValues(List<Object> values){
		this.values = values;
	}
	
	public void stopEditing(){
		if (this.isEditing()) this.getCellEditor().cancelCellEditing(); //.stopEditing();
	}
	
	public void resetProperties(){
		setProperties(new ArrayList<String>(), new ArrayList<Class<?>>());
	}

	@Override
	public void setValueAt(Object o, int row, int col){
		if (o instanceof String){
			String val = (String)o;
			Class<?> c = types.get(row);
			Object parsed = null;
			try{
				if (c == Integer.class){
					parsed = new Integer(Integer.parseInt(val));
				}else if (c == Double.class){
					parsed = new Double(Double.parseDouble(val));
				}else if (c == Boolean.class){
					parsed = new Boolean(val.equalsIgnoreCase("true") || val.equalsIgnoreCase("t"));
				}
			}catch(Exception e){
				parsed = getDefaultValue(c);
			}
			values.set(row, parsed);
			super.setValueAt(parsed.toString(), row, 1);
		}
	}
	@Override
	public Object getValueAt(int row, int col){
		if (col == 0)
			return properties.get(row);
		return values.get(row);
	}
	public Object getDefaultValue(Class<?> c){
		if (c == Integer.class){
			return new Integer(0);
		}else if (c == Double.class){
			return new Double(0);
		}else if (c == Boolean.class){
			return new Boolean(false);
		}
		return null;
	}

	@Override
	public int getColumnCount(){ return 2; }
	@Override
	public int getRowCount(){ return properties==null?0:properties.size(); }

	public Entity getEntity(String type, MapView mapView){
		String entity = "type=" + type + "|";
		for (int i = 0; i < getRowCount(); i++) {
			entity += getValueAt(i, 0);
			entity += "=" + getValueAt(i, 1);
			if (i != getRowCount() - 1) {
				entity += "|";
			}
		}
		return EntityUtil.fromString(mapView.map, entity);
	}

	@Override
	public void changeSelection(int row, int col, boolean extend, boolean toggle){
		super.changeSelection(row, 1, false, false);
	}
	
	@Override
	public Component prepareEditor(TableCellEditor editor, int row, int column) {
	    Component c = super.prepareEditor(editor, row, column);
	    if (c instanceof JTextComponent) ((JTextComponent) c).selectAll();
	    return c;
	}
}

class MyTableModel extends DefaultTableModel{
	private static final long serialVersionUID = 8095735615667315502L;

	public MyTableModel(List<String> properties, List<Class<?>> types){
		super();
		this.setColumnIdentifiers(new Object[]{"Properties", "Values"});
		this.setRowCount(properties.size());

		for (int i = 0; i < properties.size(); i++){
			this.setValueAt(properties.get(i), i, 0);
		}
	}
	@Override
	public boolean isCellEditable(int row, int col){ return col == 1; }
}
