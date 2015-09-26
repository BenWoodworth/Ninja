package ninja.mapeditor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ninja.entity.Entity;
import ninja.entity.EntityPlayer;
import ninja.map.GameMap;
import ninja.map.RenderOrder;
import ninja.textures.Textures;
import ninja.tile.Tile;
import ninja.tile.TileMap;
import ninja.tile.Tiles;

public class Map extends GameMap {
	public static final boolean isMapEditor = true;

	public MapView mapView;
	
	private double zoom = 1;

	private int cursorInteractMode;
	public static final int INTERACT_BLOCK = 0;
	public static final int INTERACT_ENTITY = 1;

	public double cursorX;
	public double cursorY;

	public boolean showGrid = true;
	public boolean showEntities = true;
	public Rectangle region;
	public Entity selectedEntity = null;
	public Tiles curTile = null;
	public Entity curNewEntity;

	public Map(int backgroundID, TileMap tileMap, List<Entity> entities, Rectangle region, int cursorInteractMode, MapView mapView) {
		super(backgroundID, tileMap, entities);
		this.region = region;
		newTileWidth = tileMap.getWidth();
		newTileHeight = tileMap.getHeight();
		this.cursorInteractMode = cursorInteractMode;
		cursorX = -1;
		cursorY = -1;
		this.mapView = mapView;
	}
	public Map(MapView mapView) {
		super(0, new TileMap(new Tile[21][16], null), new ArrayList<Entity>());
		this.getTileMap().setMap(this);
		this.mapView = mapView;
		reset();
	}
	
	public Map(MapView mapView, GameMap map){
		this(mapView);
		setBackgroundID(map.getBackgroundID());
		setEntities(map.getEntities());
		setNewTileSize(map.getTileMap().getWidth(), map.getTileMap().getHeight());
		setPlayer(map.getPlayer());
		setTileMap(map.getTileMap());
	}
	
	public GameMap toGameMap(){
		return new GameMap(getFileString());
	}
	
	public void reset(){
		setTileMap(new TileMap(new Tile[21][16], this));
		setNewTileWidth(getTileMap().getWidth());
		setNewTileHeight(getTileMap().getHeight());
		removeEntities(new ArrayList<Entity>(getEntities()));
		setZoom(1);
		setBackgroundID(0);
		setCursorInteractMode(INTERACT_BLOCK);
	}

	public double getZoom(){ return zoom; }
	public void setZoomNE(double zoom){
		this.zoom = zoom;
	}
	public void setZoom(double zoom){
		setZoomNE(zoom);
		mapView.fireZoomChangeEvent(zoom);
	}

	public Rectangle getRegion(){ return region; }
	public void setRegionNE(Rectangle region){
		this.region = region;
	}
	public void setRegion(Rectangle region){
		setRegionNE(region);
		mapView.fireRegionChangeEvent(region);
	}
	
	public int getCursorInteractMode(){ return cursorInteractMode; }
	public void setCursorInteractModeNE(int cursorInteractMode){
		this.cursorInteractMode = cursorInteractMode;
	}
	public void setCursorInteractMode(int cursorInteractMode){
		setCursorInteractModeNE(cursorInteractMode);
		mapView.fireCursorInteractModeChangeEvent(cursorInteractMode);
	}

	public int getWidth(){ return Math.max(newTileWidth * Tile.TILE_SIZE, getTileMap().getWidthPx()); }
	public int getHeight(){ return Math.max(newTileHeight * Tile.TILE_SIZE, getTileMap().getHeightPx()); }

	
	public Tile getTile(int x, int y){ return getTileMap().getTile(x, y); }
	public void setTileNE(int x, int y, Tile tile){
		getTileMap().setTile(x, y, tile);
	}
	public void setTile(int x, int y, Tile tile){
		setTileNE(x, y, tile);
		mapView.fireTileMapChangeEvent(getTileMap());
	}
	
	public void setBackgroundID_NE(int id){
		super.setBackgroundID(id);
	}
	@Override
	public void setBackgroundID(int id){
		setBackgroundID_NE(id);
		mapView.fireBackgroundChangeEvent(id);
	}
	
	private int newTileWidth;
	private int newTileHeight;
	public void setNewTileSizeNE(int width, int height){
		newTileWidth = width;
		newTileHeight = height;
	}
	public void setNewTileSize(int width, int height){
		setNewTileSizeNE(width, height);
		mapView.fireNewTileMapSizeChangeEvent(width, height);
	}
	public void setNewTileWidth(int width){ setNewTileSize(width, newTileHeight); }
	public void setNewTileWidthNE(int width){ setNewTileSizeNE(width, newTileHeight); }
	public void setNewTileHeight(int height){ setNewTileSize(newTileWidth, height); }
	public void setNewTileHeightNE(int height){ setNewTileSizeNE(newTileWidth, height); }
	public int getNewTileWidth(){ return newTileWidth; }
	public int getNewTileHeight(){ return newTileHeight; }
	
	public void setNewTileSize(){
		Tile[][] newTiles = new Tile[newTileWidth][newTileHeight];
		for (int x = 0; x < getTileMap().getWidth() && x < newTileWidth; x++){
			for (int y = 0; y < getTileMap().getHeight() && y < newTileHeight; y++){
				newTiles[x][y] = getTileMap().getTile(x, y);
			}
		}
		setTileMap(new TileMap(newTiles, this));
	}

	public void tick(){}
	public void removeEntityNE(Entity e){
		getEntities().remove(e);
		mapView.repaint();
	}
	public void removeEntity(Entity e){
		removeEntityNE(e);
		List<Entity> removed = new ArrayList<Entity>();
		removed.add(e);
		mapView.fireEntityRemoveEvent(removed);
		mapView.repaint();
	}
	public void removeEntitiesNE(List<Entity> e){
		getEntities().removeAll(e);
		mapView.repaint();
	}
	public void removeEntities(List<Entity> e){
		removeEntitiesNE(e);
		mapView.fireEntityRemoveEvent(e);
	}
	public void addEntityNE(Entity e){
		if (e instanceof EntityPlayer){
			for (int i = 0; i < getEntities().size(); i++){
				if (getEntities().get(i) instanceof EntityPlayer){
					removeEntity(getEntities().get(i--));
				}
			}
			setPlayer((EntityPlayer)e);
		}
		getEntities().add(e);
		mapView.repaint();
	}
	public void addEntity(Entity e){
		addEntityNE(e);
		List<Entity> added = new ArrayList<Entity>();
		added.add(e);
		mapView.fireEntityAddEvent(added);
		mapView.repaint();
	}
	public void addEntitiesNE(List<Entity> e){
		getEntities().addAll(e);
		mapView.repaint();
	}
	public void addEntities(List<Entity> e){
		addEntitiesNE(e);
		mapView.fireEntityAddEvent(e);
	}
	public void setEntitiesNE(List<Entity> entities){
		removeEntitiesNE(getEntities());
		addEntitiesNE(entities);
	}
	public void setEntities(List<Entity> entities){
		removeEntities(getEntities());
		addEntities(entities);
	}
	
	
	@Override
	public void draw(Graphics g, double scale){ draw(g); }
	public void draw(Graphics g){
		Graphics2D g2 = (Graphics2D)g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (zoom < 1){
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}else{
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		}

		if (getBackground() != null){
			int bgWidth = Textures.BACKGROUND_TEXTURE_WIDTH;
			int bgHeight = Textures.BACKGROUND_TEXTURE_HEIGHT;
			for (int x = 0; x <= region.getWidth(); x += bgWidth){
				for (int y = 0; y <= region.getHeight(); y += bgHeight){
					g.drawImage(getBackground(), (int)(x * zoom), (int)(y * zoom), (int)Math.ceil(bgWidth * zoom), (int)Math.ceil(bgHeight * zoom), null);
				}
			}
		}

		getTileMap().draw(g, region, zoom);
		Color removeColor = new Color(255, 0, 0, 63);
		Color addColor = new Color(0, 255, 0, 63);
		for (int x = 0; x < Math.max(newTileWidth, getTileMap().getWidth()); x++){
			for (int y = 0; y < Math.max(newTileHeight, getTileMap().getHeight()); y++){
				boolean drawAdd = (x <= newTileWidth && x >= getTileMap().getWidth() || y <= newTileHeight && y >= getTileMap().getHeight());
				boolean drawRemove = (x >= newTileWidth && x <= getTileMap().getWidth() || y >= newTileHeight && y <= getTileMap().getHeight());
				if (drawAdd ^ drawRemove){
					if (drawAdd){
						g.setColor(addColor);
					}else{
						g.setColor(removeColor);
					}
					int xPos = (int)Math.round((x * Tile.TILE_SIZE - region.getX()) * zoom);
					int yPos = (int)Math.round((y * Tile.TILE_SIZE - region.getY()) * zoom);
					int width = (int)Math.round(((x + 1) * Tile.TILE_SIZE - region.getX()) * zoom) - xPos; 
					int height = (int)Math.round(((y + 1) * Tile.TILE_SIZE - region.getY()) * zoom) - yPos;
					g.fillRect(xPos, yPos, width, height);
				}
			}
		}

		if (showEntities){
			Collections.sort(getEntities(), new RenderOrder());
			for (Entity e : getEntities()){
				if (e.isInRegion(region)){
					if (e == selectedEntity){
						g.setColor(new Color(0, 255, 0, 127));
					}else if (e instanceof EntityPlayer){
						g.setColor(new Color(255, 0, 255, 127));
					}else{
						g.setColor(new Color(255, 0, 0, 127));
					}
					int drawX = (int)((e.getLocationX() - region.x) * zoom);
					int drawY = (int)((e.getLocationY() - region.y) * zoom);
					int drawW = (int)Math.ceil(e.getLocation().getWidth() * zoom);
					int drawH = (int)Math.ceil(e.getLocation().getHeight() * zoom);
					g.fillRect(drawX, drawY, drawW, drawH);
					e.draw(g, region, zoom);
				}
			}
		}

		if (cursorX != -1 && cursorY != -1 && curTile != null){
			if (cursorInteractMode == INTERACT_BLOCK){
				int tileX = (int)(cursorX / Tile.TILE_SIZE);
				int tileY = (int)(cursorY / Tile.TILE_SIZE);
				if (tileX < getTileMap().getWidth() && tileY < getTileMap().getHeight()){
					int drawX = (int)((tileX * Tile.TILE_SIZE - region.x) * zoom);
					int drawY = (int)((tileY * Tile.TILE_SIZE - region.y) * zoom);
					int drawW = (int)(((tileX + 1) * Tile.TILE_SIZE - region.x) * zoom) - drawX;
					int drawH = (int)(((tileY + 1) * Tile.TILE_SIZE - region.y) * zoom) - drawY;
					Tile toDraw = Tiles.newTile(curTile, getTileMap(), tileX, tileY);
					if (toDraw != null) {
						BufferedImage image = toDraw.getImage();
						g.drawImage(image, drawX, drawY, drawW, drawH, null);
					}
					g.setColor(new Color(255, 255, 255, 31));
					g.fillRect(drawX, drawY, drawW, drawH);
				}
			}else if (cursorInteractMode == INTERACT_ENTITY){
				if (curNewEntity != null){
					curNewEntity.draw(g, region, zoom);
				}
			}
		}

		if (showGrid){
			g.setColor(new Color(255, 255, 255, 23));
			for (int x = 0; x <= getTileMap().getWidth(); x++){
				int xPos = (int)((x * Tile.TILE_SIZE - region.getX()) * zoom);
				g.drawLine(xPos, (int)(-region.getY() * zoom), xPos, (int)((-region.getY() + getTileMap().getHeightPx()) * zoom));
			}
			for (int y = 0; y <= getTileMap().getHeight(); y++){
				int yPos = (int)((y * Tile.TILE_SIZE - region.getY()) * zoom);
				g.drawLine((int)(-region.getX() * zoom), yPos, (int)((-region.getX() + getTileMap().getWidthPx()) * zoom), yPos);
			}
			for (int x = 0; x <= newTileWidth; x++){
				int xPos = (int)((x * Tile.TILE_SIZE - region.getX()) * zoom);
				g.drawLine(xPos, (int)(-region.getY() * zoom), xPos, (int)((-region.getY() + newTileHeight * Tile.TILE_SIZE) * zoom));
			}
			for (int y = 0; y <= newTileHeight; y++){
				int yPos = (int)((y * Tile.TILE_SIZE - region.getY()) * zoom);
				g.drawLine((int)(-region.getX() * zoom), yPos, (int)((-region.getX() + newTileWidth * Tile.TILE_SIZE) * zoom), yPos);
			}
		}
	}

	public void placeBlock(Tiles tile){
		if (cursorX == -1 || cursorY == -1 || curTile == null) return;
		int x = (int)(cursorX / Tile.TILE_SIZE);
		int y = (int)(cursorY / Tile.TILE_SIZE);
		Tile toPlace = Tiles.newTile(tile, getTileMap(), x, y);
		setTile(x, y, toPlace);
	}
}
