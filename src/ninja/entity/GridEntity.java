package ninja.entity;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import ninja.map.GameMap;
import ninja.tile.Tile;

public abstract class GridEntity extends Entity {

	public GridEntity(GameMap map, Double hitbox, Double location) {
		super(map, hitbox, location, 0, 0);
		setLocationXY(location.x, location.y);
	}
	
	public int getTileX(){ return (int)(getLocationX() / Tile.TILE_SIZE); }
	public int getTileY(){ return (int)(getLocationY() / Tile.TILE_SIZE); }
	@Override
	public void setLocation(Rectangle2D.Double location){
		location.x = (int)(location.x / Tile.TILE_SIZE) * Tile.TILE_SIZE;
		location.y = (int)(location.y / Tile.TILE_SIZE) * Tile.TILE_SIZE;
		super.setLocation(location);
	}
	@Override
	public void setLocationX(double x){ super.setLocationX((int)(x / Tile.TILE_SIZE) * Tile.TILE_SIZE); }
	@Override
	public void setLocationY(double y){ super.setLocationY((int)(y / Tile.TILE_SIZE) * Tile.TILE_SIZE); }
	@Override
	public void setLocationXY(double x, double y){
		setLocationX(x);
		setLocationY(y);
	}
}
