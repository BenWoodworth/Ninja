package ninja.map;

import java.util.Comparator;

import ninja.entity.Entity;

public class RenderOrder implements Comparator<Entity> {
	public static final int UNSPECIFIED = -1000;
	public static final int BACKGROUND = -5;
	public static final int BEHIND_ENTITY = -4;
	public static final int ENTITY = -3;
	public static final int LIVING_ENTITY = -2;
	public static final int LIVING_ENTITY_WEAPON = -1;
	public static final int PLAYER = 0;
	public static final int PLAYER_WEAPON = 1;
	public static final int FOREGROUND = 2;
	
	public int compare(Entity a, Entity b){ return a.getRenderOrder() - b.getRenderOrder(); }
}
