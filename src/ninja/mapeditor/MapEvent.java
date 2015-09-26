package ninja.mapeditor;

import java.awt.Rectangle;
import java.util.EventListener;

import ninja.tile.TileMap;

public interface MapEvent extends EventListener {
	public void backgroundChange(int backgroundID);
	public void tileMapChange(TileMap tileMap);
	public void zoomChange(double zoom);
	public void regionChange(Rectangle region);
	public void cursorInteractModeChanged(int cursorInteractMode);
	public void newTileMapSizeChange(int width, int height);
}
