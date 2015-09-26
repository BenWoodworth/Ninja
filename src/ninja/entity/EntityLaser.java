package ninja.entity;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import ninja.animation.Animation;
import ninja.map.GameMap;
import ninja.map.RenderOrder;
import ninja.textures.Textures;
import ninja.tile.Tile;
import ninja.tile.TileMap;

public class EntityLaser extends GridEntity {
	public EntityTypes getType(){ return EntityTypes.LASER; }
	public int getRenderOrder(){ return RenderOrder.ENTITY; }
	public BufferedImage getIcon(){
		BufferedImage result = Textures.getEntityTexture(1);
		result.getGraphics().drawImage(Textures.getEntityTexture(0), 0, 0, null);
		return result;
	}

	private static final int LASER_BEAM_WIDTH = 6;
	private static final double DAMAGE = 1;

	private Animation laserBeamAnimation;
	private Animation laserAnimation;
	private int rotation;

	private int laserOnTicks;
	private int laserOffTicks;
	private int curTicks;
	private boolean laserOn;

	public EntityLaser(GameMap map, double x, double y, int rotation, int laserOnTicks, int laserOffTicks, int curTicks, boolean laserOn) {
		super(map, null, new Rectangle2D.Double(x, y, Textures.ENTITY_TEXTURE_SIZE, Textures.ENTITY_TEXTURE_SIZE));

		BufferedImage[] frames = new BufferedImage[7];
		for (int i = 1; i < 7; i++) frames[i - 1] = Textures.getEntityTexture(i);
		laserBeamAnimation = new Animation(5, 0, frames);
		laserAnimation = new Animation(0, Textures.getEntityTexture(0));

		this.laserOn = laserOn;
		this.curTicks = curTicks;
		laserOn = false;
		this.laserOnTicks = laserOnTicks;
		this.laserOffTicks = laserOffTicks;

		setRotation(rotation);

		updateHitbox();
	}

	@Override
	public void setLocation(Rectangle2D.Double location){
		super.setLocation(location);
		updateHitbox();
	}
	@Override
	public void setLocationX(double x){ super.setLocationX((int)(x / Tile.TILE_SIZE) * Tile.TILE_SIZE); updateHitbox(); }
	@Override
	public void setLocationY(double y){ super.setLocationY((int)(y / Tile.TILE_SIZE) * Tile.TILE_SIZE); updateHitbox(); }
	@Override
	public void setLocationXY(double x, double y){
		setLocationX(x);
		setLocationY(y);
	}

	public void setRotation(int rotation){
		rotation = (rotation % 4 + 4) % 4;
		this.rotation = rotation;
		updateHitbox();
		laserBeamAnimation.setRotation(rotation);
		laserBeamAnimation.updateFrames();
		laserAnimation.setRotation(rotation);
		laserAnimation.updateFrames();
	}
	public int getRotation(){ return rotation; }
	public void setLaserOnTicks(int ticks){ laserOnTicks = ticks; }
	public int getLaserOnTicks(){ return laserOnTicks; }
	public void setLaserOffTicks(int ticks){ laserOffTicks = ticks; }
	public int getLaserOffTicks(){ return laserOffTicks; }
	public void setCurTicks(int ticks){ curTicks = ticks; }
	public int getCurTicks(){ return curTicks; }
	public void setLaserOn(boolean on){ laserOn = on; updateHitbox(); }
	public boolean isLaserOn(){ return laserOn; }

	public void tick(){
		super.tick();
		laserAnimation.tick();
		laserBeamAnimation.tick();
		curTicks -= 1;
		if (curTicks <= 0){
			laserOn = !laserOn;
			if (laserOn && laserOnTicks == 0) laserOn = false;
			if (!laserOn && laserOffTicks == 0) laserOn = true;
			curTicks = laserOn ? laserOnTicks : laserOffTicks;
			updateHitbox();
		}
		if (laserOn){
			for (Entity e : map.getEntities()){
				if (e instanceof LivingEntity && e.collidesWith(this)){
					((LivingEntity)e).dealDamage(DAMAGE, this);
				}
			}
		}
	}
	
	@Override
	public void tileMapChange(){ updateHitbox(); }
	public void updateHitbox(){
		TileMap tileMap = map.getTileMap();
		if (rotation == 0){
			int y; for (y = getTileY(); y < tileMap.getHeight() && (tileMap.getTile(getTileX(), y) == null || !tileMap.getTile(getTileX(), y).isSolid()); y++); y--;
			setHitbox(new Rectangle2D.Double((getLocation().width - LASER_BEAM_WIDTH) / 2, 0, LASER_BEAM_WIDTH, (y - getTileY() + 1) * Tile.TILE_SIZE));
		}else if (rotation == 1){
			int x; for (x = getTileX(); x > 0 && (tileMap.getTile(x, getTileY()) == null || !tileMap.getTile(x, getTileY()).isSolid()); x--); x++;
			setHitbox(new Rectangle2D.Double((getTileX() - x) * -Tile.TILE_SIZE, (getLocation().height - LASER_BEAM_WIDTH) / 2, (getTileX() - x + 1) * Tile.TILE_SIZE, LASER_BEAM_WIDTH));
		}else if (rotation == 2){
			int y; for (y = getTileY(); y > 0 && (tileMap.getTile(getTileX(), y) == null || !tileMap.getTile(getTileX(), y).isSolid()); y--); y++;
			setHitbox(new Rectangle2D.Double((getLocation().width - LASER_BEAM_WIDTH) / 2, (getTileY() - y) * -Tile.TILE_SIZE, LASER_BEAM_WIDTH, (getTileY() - y + 1) * Tile.TILE_SIZE));
		}else{
			int x; for (x = getTileX(); x < tileMap.getWidth() && (tileMap.getTile(x, getTileY()) == null || !tileMap.getTile(x, getTileY()).isSolid()); x++); x--;
			setHitbox(new Rectangle2D.Double(0, (getLocation().height - LASER_BEAM_WIDTH) / 2, (x - getTileX() + 1) * Tile.TILE_SIZE, LASER_BEAM_WIDTH));
		}
	}

	public void draw(Graphics g, Rectangle region, double scale){
		if (laserOn){
			if (rotation == 0 || rotation == 2){
				int start = (int)Math.max(getHitbox().y + getLocationY(), (int)(region.y / Tile.TILE_SIZE) * Tile.TILE_SIZE);
				for (int y = start; y < getHitbox().height + getHitbox().y + getLocationY() && y < region.y + region.height; y += Textures.ENTITY_TEXTURE_SIZE){
					g.drawImage(laserBeamAnimation.getCurFrame(),
							(int)((getLocationX() - region.x) * scale), (int)((y - region.y) * scale),
							(int)(Tile.TILE_SIZE * scale), (int)(Tile.TILE_SIZE * scale), null);
				}
			}else{
				int start = (int)Math.max(getHitbox().x + getLocationX(), (int)(region.x / Tile.TILE_SIZE) * Tile.TILE_SIZE);
				for (int x = start; x < getHitbox().width + getHitbox().x + getLocationX() && x < region.x + region.width; x += Textures.ENTITY_TEXTURE_SIZE){
					g.drawImage(laserBeamAnimation.getCurFrame(),
							(int)((x - region.x) * scale), (int)((getLocationY() - region.y) * scale),
							(int)(Tile.TILE_SIZE * scale), (int)(Tile.TILE_SIZE * scale), null);
				}
			}
		}
		g.drawImage(laserAnimation.getCurFrame(),
				(int)((getTileX() * Tile.TILE_SIZE - region.x) * scale), (int)((getTileY() * Tile.TILE_SIZE - region.y) * scale),
				(int)(Tile.TILE_SIZE * scale), (int)(Tile.TILE_SIZE * scale), null
				);
	}

	public String toString(){ return "Laser {x=" + getLocationX() + ",y=" + getLocationY() + "}"; }
}
