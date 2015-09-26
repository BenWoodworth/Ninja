package ninja.tile;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import ninja.entity.Entity;
import ninja.textures.Textures;

public class Tile {
	public static final int TILE_SIZE = Textures.BLOCK_TEXTURE_SIZE;
	
	public static int TYPE_INTANGABLE = 1;
	public static int TYPE_SOLID = 2;
	
	protected int tileID;
	protected int tileType;
	public int x, y;
	public TileMap tileMap; 
	
	public Tile (TileMap tileMap, int x, int y, int tileID, int tileType){
		this.tileMap = tileMap;
		this.tileID = tileID;
		this.tileType = tileType;
		this.x = x;
		this.y = y;
	}
	public void tick(){}
	public BufferedImage getImage(){ return null; }
	public int getTileID(){ return tileID; }
	public int getType(){ return tileType; }
	public int getID(){ return tileID; }

	public Tile getTileU(){ return (tileMap == null)?null:tileMap.getTile(x, y - 1); }
	public Tile getTileD(){ return (tileMap == null)?null:tileMap.getTile(x, y + 1); }
	public Tile getTileL(){ return (tileMap == null)?null:tileMap.getTile(x - 1, y); }
	public Tile getTileR(){ return (tileMap == null)?null:tileMap.getTile(x + 1, y); }
	public Tile getTileUR(){ return (tileMap == null)?null:tileMap.getTile(x + 1, y - 1); }
	public Tile getTileDL(){ return (tileMap == null)?null:tileMap.getTile(x - 1, y + 1); }
	public Tile getTileUL(){ return (tileMap == null)?null:tileMap.getTile(x - 1, y - 1); }
	public Tile getTileDR(){ return (tileMap == null)?null:tileMap.getTile(x + 1, y + 1); }
	
	public boolean equals(Tile tile){ return this == tile || (tile != null) && (tileID == tile.tileID); }
	public boolean isSolid(){ return tileType == TYPE_SOLID; }
	
	public boolean collidesWith(Entity e){
		Rectangle ehb = new Rectangle((int)(e.getLocationX() + e.getHitbox().getX()), (int)(e.getLocationX() + e.getHitbox().getY()), (int)e.getHitbox().getWidth(), (int)e.getHitbox().getHeight());
		return new Rectangle(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE).intersects(ehb);
	}
	public boolean overlaps(Entity e){
		double x1 = e.getLocationX() + e.getHitbox().getX();
		double x2 = x1 + e.getHitbox().getWidth();
		double x3 = (x1 + x2) / 2;
		double y1 = e.getLocationY() + e.getHitbox().getY();
		double y2 = y1 + e.getHitbox().getHeight();
		double y3 = (y1 + y2) / 2;
		
		int a1 = x * TILE_SIZE;
		int a2 = a1 + TILE_SIZE;
		int a3 = (a1 + a2) / 2;
		int b1 = y * TILE_SIZE;
		int b2 = b1 + TILE_SIZE;
		int b3 = (b1 + b2) / 2;

		if (a1 < x1 && x1 < a2 && b1 < y1 && y1 < b2) return true;
		if (a1 < x2 && x2 < a2 && b1 < y1 && y1 < b2) return true;
		if (a1 < x1 && x1 < a2 && b1 < y2 && y2 < b2) return true;
		if (a1 < x2 && x2 < a2 && b1 < y2 && y2 < b2) return true;
		
		if (a1 < x3 && x3 < a2 && b1 < y1 && y1 < b2) return true;
		if (a1 < x3 && x3 < a2 && b1 < y2 && y2 < b2) return true;
		if (a1 < x1 && x1 < a2 && b1 < y3 && y3 < b2) return true;
		if (a1 < x2 && x2 < a2 && b1 < y3 && y3 < b2) return true;
		
		if (x1 < a1 && a1 < x2 && y1 < b1 && b1 < y2) return true;
		if (x1 < a2 && a2 < x2 && y1 < b1 && b1 < y2) return true;
		if (x1 < a1 && a1 < x2 && y1 < b2 && b2 < y2) return true;
		if (x1 < a2 && a2 < x2 && y1 < b2 && b2 < y2) return true;
		
		if (x1 < a3 && a3 < x2 && y1 < b1 && b1 < y2) return true;
		if (x1 < a3 && a3 < x2 && y1 < b2 && b2 < y2) return true;
		if (x1 < a1 && a1 < x2 && y1 < b3 && b3 < y2) return true;
		if (x1 < a2 && a2 < x2 && y1 < b3 && b3 < y2) return true;
		
		return false;
	}
}
