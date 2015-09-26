package ninja.mapeditor;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import ninja.entity.Entity;
import ninja.entity.EntityUtil;
import ninja.tile.Tile;
import ninja.tile.TileMap;
import ninja.tile.Tiles;

public class MapView extends JComponent implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = -7216032172124707285L;

	public static final int BUFFERS = 2;
	
	public Map map;
	private double locX;
	private double locY;

	public Tiles leftClickBlock = Tiles.WALL;
	public Tiles rightClickBlock = Tiles.AIR;

	private double zoom = 1;
	public double getZoom(){ return zoom; }
	public void setZoomNE(double zoom){
		this.zoom = zoom;
		map.setZoom(zoom);
		repaint();
	}
	public void setZoom(double zoom){
		setZoomNE(zoom);
		fireZoomChangeEvent(zoom);
	}
	
	private List<EntityEvent> entityEventListeners = new ArrayList<EntityEvent>();
	public void addEntityEventListener(EntityEvent e){
		entityEventListeners.add(e);
	}
	public void fireEntityAddEvent(List<Entity> entity){
		for (EntityEvent e : entityEventListeners)
			e.entityAdded(entity);
	}
	public void fireEntityRemoveEvent(List<Entity> entity){
		for (EntityEvent e : entityEventListeners)
			e.entityRemoved(entity);
	}
	
	private List<MapEvent> mapEventListeners = new ArrayList<MapEvent>();
	public void addMapEventListener(MapEvent e){
		mapEventListeners.add(e);
	}
	public void fireBackgroundChangeEvent(int backgroundID){
		for (MapEvent e : mapEventListeners)
			e.backgroundChange(backgroundID);
	}
	public void fireTileMapChangeEvent(TileMap tileMap){
		for (MapEvent e : mapEventListeners)
			e.tileMapChange(tileMap);
	}
	public void fireZoomChangeEvent(double zoom){
		for (MapEvent e : mapEventListeners)
			e.zoomChange(zoom);
	}
	public void fireRegionChangeEvent(Rectangle region){
		for (MapEvent e : mapEventListeners)
			e.regionChange(region);
	}
	public void fireCursorInteractModeChangeEvent(int cursorInteractMode){
		for (MapEvent e : mapEventListeners)
			e.cursorInteractModeChanged(cursorInteractMode);
	}
	public void fireNewTileMapSizeChangeEvent(int width, int height){
		for (MapEvent e : mapEventListeners)
			e.newTileMapSizeChange(width, height);
	}

	public MapView(int tileWidth, int tileHeight, int bgID){
		locX = locY = 0;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		map = new Map(this);
	}
	public MapView(){
		locX = locY = 0;
		map = new Map(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (map == null) return;
		map.region = getRegion();
		map.draw(g);
	}

	public Map getMap(){ return map; }
	public void setMap(Map map){
		this.getRegion().setSize((int)(map.getTileMap().getWidth() * zoom), (int)(map.getTileMap().getHeight() * zoom));
		map.setCursorInteractMode(this.map.getCursorInteractMode());
		map.curNewEntity = this.map.curNewEntity;
		map.curTile = this.map.curTile;
		map.showEntities = this.map.showEntities;
		map.showGrid = this.map.showGrid;
		this.map = map;
	}

	public Rectangle getRegion(){
		return new Rectangle((int)(locX * zoom), (int)(locY * zoom), (int)(getWidth() / zoom), (int)(getHeight() / zoom));
	}
	public void setRegionX_NE(double x){
		locX = x;
	}
	public void setRegionX(double x){
		setRegionX_NE(x);
		fireRegionChangeEvent(getRegion());
	}
	public void setRegionY_NE(double y){
		locY = y;
	}
	public void setRegionY(double y){
		setRegionY_NE(y);
		fireRegionChangeEvent(getRegion());
	}
	public void setRegionXY_NE(double x, double y){
		locX = x;
		locY = y;
	}
	public void setRegionXY(double x, double y){
		setRegionXY_NE(x, y);
		fireRegionChangeEvent(getRegion());
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	int curMouseButton = 0;
	public boolean entitySnapToGrid = false;
	@Override
	public void mouseEntered(MouseEvent e) {
		setCursorXY(e);
		setCursor();
		this.repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		map.cursorX = map.cursorY = -1;
		this.repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		curMouseButton = e.getButton();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		curMouseButton = 0;
		if (map.getCursorInteractMode() == Map.INTERACT_BLOCK){
			if (e.getButton() == MouseEvent.BUTTON1){
				map.placeBlock(leftClickBlock);
				this.repaint();
			}else if (e.getButton() == MouseEvent.BUTTON3){
				map.placeBlock(rightClickBlock);
				this.repaint();
			}
		}else if (map.getCursorInteractMode() == Map.INTERACT_ENTITY){
			map.addEntity(map.curNewEntity);
			map.curNewEntity = EntityUtil.fromType(map, map.curNewEntity.getType());
			map.curNewEntity.setLocationXY(map.cursorX, map.cursorY);
			this.repaint();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		setCursorXY(e);
		if (map.getCursorInteractMode() == Map.INTERACT_BLOCK){
			if (curMouseButton == MouseEvent.BUTTON1){
				map.placeBlock(leftClickBlock);
			}else if (curMouseButton == MouseEvent.BUTTON3){
				map.placeBlock(rightClickBlock);
			}
		}else if (map.getCursorInteractMode() == Map.INTERACT_ENTITY){
		}
		this.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		setCursorXY(e);
		if (map.getCursorInteractMode() == Map.INTERACT_BLOCK){
		}else if (map.getCursorInteractMode() == Map.INTERACT_ENTITY){
			if (map.curNewEntity != null){
				map.curNewEntity.setLocationXY(map.cursorX, map.cursorY);
			}
		}
		setCursor();
		this.repaint();
	}

	public void setCursorXY(MouseEvent e){
		double newX = e.getX() / zoom + getRegion().getX();
		double newY = e.getY() / zoom + getRegion().getY();
		if (map.getCursorInteractMode() == Map.INTERACT_ENTITY && entitySnapToGrid){
			newX = (int)(newX / Tile.TILE_SIZE) * Tile.TILE_SIZE;
			newY = (int)(newY / Tile.TILE_SIZE) * Tile.TILE_SIZE;
		}
		map.cursorX = newX;
		map.cursorY = newY;
	}

	Cursor invisCursor = null;
	public void setCursor(){
		if (map.getCursorInteractMode() == Map.INTERACT_ENTITY && map.curNewEntity != null){
			if (invisCursor == null){
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				Point hotSpot = new Point(0,0);
				BufferedImage cursorImage = new BufferedImage(1, 1, BufferedImage.TRANSLUCENT);
				invisCursor = toolkit.createCustomCursor(cursorImage, hotSpot, "InvisibleCursor");
			}       
			this.setCursor(invisCursor);
		}else{
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
}
