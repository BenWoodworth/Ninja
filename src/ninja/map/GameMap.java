package ninja.map;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ninja.GameCanvas;
import ninja.entity.Entity;
import ninja.entity.EntityPlayer;
import ninja.entity.EntityUtil;
import ninja.textures.Textures;
import ninja.tile.Tile;
import ninja.tile.TileMap;
import ninja.tile.Tiles;

public class GameMap {
	public static final boolean isMapEditor = false;

	public static final double PARALAX_SCALE = .25;
	public static final double BACKGROUND_X = -.2;
	public static final double BACKGROUND_Y = .1;
	private double bgOffsetX;
	private double bgOffsetY;

	private List<Entity> entities;
	private TileMap tileMap;
	private EntityPlayer player;
	private BufferedImage background;
	private int backgroundID;

	public static GameMap fromFile(File f) throws IOException{
		return fromInputStreamReader(new InputStreamReader(new FileInputStream(f)));
	}
	public static GameMap fromInputStreamReader(InputStreamReader s) throws IOException{
		String file = "";
		String curLine;
		BufferedReader br = new BufferedReader(s);
		while((curLine = br.readLine()) != null)
			file += curLine + "\n";
		br.close();
		return new GameMap(file);
	}
	
	public GameMap(String mapFile){
		String[] lines = mapFile.split("\n");
		backgroundID = Integer.parseInt(lines[0]);
		background = Textures.getBackgroundTexture(backgroundID);
		int cols = Integer.parseInt(lines[1]);
		int rows = Integer.parseInt(lines[2]);
		Tile[][] tiles = new Tile[cols][rows];
		tileMap = new TileMap(tiles, this);
		for (int y = 0; y < rows; y++) {
			String[] vals = lines[y + 3].split(",");
			for (int x = 0; x < cols; x++) {
				tiles[x][y] = Tiles.newTile(Integer.parseInt(vals[x]), tileMap, x, y);
			}
		}

		entitiesToRemove = new ArrayList<Entity>();
		entitiesToAdd = new ArrayList<Entity>();
		entities = new ArrayList<Entity>();
		for (int line = 3 + rows; line < lines.length; line++){
			Entity e = EntityUtil.fromString(this, lines[line]);
			if (e == null) continue;
			if (e instanceof EntityPlayer){
				if (player == null){
					player = (EntityPlayer)e;
				}
			}
			entities.add(e);
		}
	}
	public GameMap(int backgroundID, TileMap tileMap, List<Entity> entities){
		bgOffsetX = bgOffsetY = 0;
		entitiesToRemove = new ArrayList<Entity>();
		entitiesToAdd = new ArrayList<Entity>();
		this.backgroundID = backgroundID;
		background = Textures.getBackgroundTexture(backgroundID);
		this.tileMap = tileMap;
		this.entities = entities;
		for (Entity e : entities){
			if (e instanceof EntityPlayer){
				player = (EntityPlayer)e;
				entities.remove(e);
				break;
			}
		}
	}

	public EntityPlayer getPlayer(){ return player; }
	public void setPlayer(EntityPlayer player){ this.player = player; }
	public TileMap getTileMap(){ return tileMap; }
	public void setTileMap(TileMap tileMap){ this.tileMap = tileMap; }
	public void tileMapChange(){ for (Entity e : entities) e.tileMapChange(); }
	public List<Entity> getEntities(){ return entities; }
	public void setEntities(List<Entity> entities){ this.entities = entities; }

	public void setBackgroundID(int id){
		backgroundID = id;
		background = Textures.getBackgroundTexture(id);
	}
	public BufferedImage getBackground(){ return background; }

	public void tick(){
		tileMap.tick();
		for (Entity e : entities) e.tick();
		entities.removeAll(entitiesToRemove);
		entities.addAll(entitiesToAdd);
		entitiesToRemove.clear();
		entitiesToAdd.clear();

		bgOffsetX += BACKGROUND_X;
		bgOffsetY += BACKGROUND_Y;
	}
	List<Entity> entitiesToRemove;
	public void removeEntity(Entity e){
		entitiesToRemove.add(e);
	}
	public void removeEntities(List<Entity> e){
		entitiesToRemove.addAll(e);
	}
	List<Entity> entitiesToAdd;
	public void addEntity(Entity e){
		entitiesToAdd.add(e);
	}

	public void draw(Graphics g, double scale){
		Rectangle region = getRegion();

		if (background != null){
			double bgX = (-region.getX() * PARALAX_SCALE + bgOffsetX) % background.getWidth();
			double bgY = (-region.getY() * PARALAX_SCALE + bgOffsetY) % background.getHeight();
			double bgX2 = (bgX + background.getWidth() * (bgX < 0?1:-1)) % background.getWidth();
			double bgY2 = (bgY + background.getHeight() * (bgY < 0?1:-1)) % background.getHeight();

			g.drawImage(background, (int)(bgX * scale), (int)(bgY * scale), (int)(background.getWidth() * scale), (int)(background.getHeight() * scale), null);
			if (bgX != bgX2) g.drawImage(background, (int)(bgX2 * scale), (int)(bgY * scale), (int)(background.getWidth() * scale), (int)(background.getHeight() * scale), null);
			if (bgY != bgY2) g.drawImage(background, (int)(bgX * scale), (int)(bgY2 * scale), (int)(background.getWidth() * scale), (int)(background.getHeight() * scale), null);
			if (bgX != bgX2 && bgY != bgY2) g.drawImage(background, (int)(bgX2 * scale), (int)(bgY2 * scale), (int)(background.getWidth() * scale), (int)(background.getHeight() * scale), null);
		}

		tileMap.draw(g, region, scale);
		Collections.sort(entities, new RenderOrder());
		for (Entity e : entities) if (e.isInRegion(region)) e.draw(g, region, scale);
	}

	public Rectangle getRegion(){
		int x, y;
		if (tileMap.getWidthPx() <= GameCanvas.WIDTH){
			x = (tileMap.getWidthPx() - GameCanvas.WIDTH) / 2;
		}else{
			x = (int)Math.max(0, Math.min(tileMap.getWidthPx() - GameCanvas.WIDTH, player.getLocationX() + player.getHitbox().getWidth() / 2 - GameCanvas.WIDTH / 2));
		}
		if (tileMap.getHeightPx() <= GameCanvas.HEIGHT){
			y = (tileMap.getHeightPx() - GameCanvas.HEIGHT) / 2;
		}else{
			y = (int)Math.max(0, Math.min(tileMap.getWidthPx() - GameCanvas.HEIGHT, player.getLocation().y + player.getHitbox().getHeight() / 2 - GameCanvas.HEIGHT / 2));
		}
		return new Rectangle(x, y, GameCanvas.WIDTH, GameCanvas.HEIGHT);
	}

	public int getBackgroundID(){ return backgroundID; }

	public String getFileString(){
		String result = "";
		result += backgroundID + "\n";
		result += tileMap.getWidth() + "\n";
		result += tileMap.getHeight() + "\n";
		for (int y = 0; y < tileMap.getHeight(); y++){
			for (int x = 0; x < tileMap.getWidth(); x++){
				Tile t = tileMap.getTile(x, y);
				result += t==null ? "0" : t.getID();
				if (x != tileMap.getWidth() - 1) result += ",";
			}
			result += "\n";
		}
		for (Entity e : entities){
			result += EntityUtil.toFileString(e) + "\n";
		}
		return result;
	}
}
