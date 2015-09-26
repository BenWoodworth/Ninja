package ninja.tile;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import ninja.entity.Entity;
import ninja.map.GameMap;
import ninja.textures.Textures;

public class TileMap {
	private Tile[][] tiles;
	private int rows;
	private int cols;
	private GameMap map;
	
	public TileMap(Tile[][] tiles, GameMap map){
		this.tiles = tiles;
		cols = tiles.length;
		rows = tiles[0].length;
		this.map = map;
	}
	public TileMap(TileMap tileMap){
		this.tiles = tileMap.tiles.clone();
		this.rows = tileMap.rows;
		this.cols = tileMap.cols;
		this.map = tileMap.map;
	}

	public void setMap(GameMap map){ this.map = map; }
	public GameMap getMap(){ return map; }
	
	public void setTile(int x, int y, Tile tile){
		if (x < 0 || y < 0 || x >= cols || y >= rows) return;
		tiles[x][y] = tile;
		map.tileMapChange();
	}
	public Tile getTile(int x, int y){
		if (x < 0 || y < 0 || x >= cols || y >= rows) return null;
		return tiles[x][y];
	}

	public void draw(Graphics g, Rectangle region, double scale){
		int tileSize = Textures.BLOCK_TEXTURE_SIZE;
		for (int tileX = (int)Math.max(0, region.getX() / tileSize); tileX < getWidth() && tileX <= (int)((region.getX() + region.getWidth()) / tileSize); tileX++){
			for (int tileY = (int)Math.max(0, region.getY() / tileSize); tileY < getHeight() && tileY <= (int)((region.getY() + region.getHeight()) / tileSize); tileY++){
				Tile tile = tiles[tileX][tileY];
				int drawX = (int)((tileX * tileSize - region.getX()) * scale);
				int drawY = (int)((tileY * tileSize - region.getY()) * scale);
				int drawW = (int)(((tileX + 1) * tileSize - region.getX()) * scale) - drawX;
				int drawH = (int)(((tileY + 1) * tileSize - region.getY()) * scale) - drawY;
				if (tile != null) g.drawImage(tile.getImage(), drawX, drawY, drawW, drawH, null);
			}
		}
	}

	public void tick(){
		for (int x = 0; x < cols; x++){
			for (int y = 0; y < rows; y++){
				if (tiles[x][y] != null) tiles[x][y].tick();
			}
		}
	}

	public int getWidth(){
		return tiles.length;
	}
	public int getWidthPx(){
		return getWidth() * Textures.BLOCK_TEXTURE_SIZE;
	}
	public int getHeight(){
		return tiles[0].length;
	}
	public int getHeightPx(){
		return getHeight() * Textures.BLOCK_TEXTURE_SIZE;
	}
	
	public List<Tile> getCollidingBlocks(Entity e){
		List<Tile> result = new ArrayList<Tile>();
		int startX = (int)((e.getLocationX() + e.getHitbox().getX()) / Tile.TILE_SIZE);
		int endX = (int)Math.ceil((e.getLocationX() + e.getHitbox().getX() + e.getHitbox().getWidth()) / Tile.TILE_SIZE);
		int startY = (int)((e.getLocation().getY() + e.getHitbox().getY()) / Tile.TILE_SIZE);
		int endY = (int)Math.ceil((e.getLocationY() + e.getHitbox().getY() + e.getHitbox().getHeight()) / Tile.TILE_SIZE);
		for (int x = Math.max(0, startX); x < cols && x <= endX; x++){
			for (int y = Math.max(0, startY); y < cols && y <= endY; y++){
				Tile test = getTile(x, y);
				if (test != null && test.collidesWith(e)) result.add(getTile(x, y));
			}
		}
		return result;
	}
	
	public List<Tile> getOverlappingBlocks(Entity e){
		List<Tile> result = new ArrayList<Tile>();
		int startX = (int)((e.getLocation().getX() + e.getHitbox().getX()) / Tile.TILE_SIZE);
		int endX = (int)Math.ceil((e.getLocationX() + e.getHitbox().getX() + e.getHitbox().getWidth()) / Tile.TILE_SIZE);
		int startY = (int)((e.getLocationY() + e.getHitbox().getY()) / Tile.TILE_SIZE);
		int endY = (int)Math.ceil((e.getLocationY() + e.getHitbox().getY() + e.getHitbox().getHeight()) / Tile.TILE_SIZE);
		for (int x = Math.max(0, startX); x < cols && x <= endX; x++){
			for (int y = Math.max(0, startY); y < cols && y <= endY; y++){
				Tile test = getTile(x, y);
				if (test != null && test.overlaps(e)) result.add(getTile(x, y));
			}
		}
		return result;
	}
}
