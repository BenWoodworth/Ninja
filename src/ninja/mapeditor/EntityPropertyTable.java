package ninja.mapeditor;

import java.util.List;
import java.util.ArrayList;

import ninja.entity.Entity;
import ninja.entity.EntityUtil;


public class EntityPropertyTable extends PropertyTable {
	private static final long serialVersionUID = -4435190812599389467L;
	
	private Entity entity;
	public EntityPropertyTable(Entity entity){
		this.entity = entity;
	}
	
	private void updatePropertiesAndValues(){
		if (entity == null){
			this.resetProperties();
		}else{
			List<String> properties = new ArrayList<String>();
			List<Class<?>> types = new ArrayList<Class<?>>();
			List<Object> values = EntityUtil.getValues(entity);
			EntityUtil.getProperties(properties, types, entity.getType());
			this.setProperties(properties, types, values);
		}
	}
	private void updateValues(){
		if (entity == null){
			this.resetProperties();
		}else{
			this.setValues(EntityUtil.getValues(entity));
		}
	}
	
	public void setEntity(Entity entity){
		stopEditing();
		this.entity = entity;
		updatePropertiesAndValues();
	}
	public Entity getEntity(){ return entity; }
	
	@Override
	public void setValueAt(Object o, int row, int col){
		super.setValueAt(o, row, col);
		EntityUtil.setValues(entity, properties, values);
		this.stopEditing();
		updateValues();
	}
}
