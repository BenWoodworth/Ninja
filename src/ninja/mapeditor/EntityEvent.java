package ninja.mapeditor;

import java.util.EventListener;
import java.util.List;

import ninja.entity.Entity;

public interface EntityEvent extends EventListener {
	public void entityAdded(List<Entity> e);
	public void entityRemoved(List<Entity> e);
}
