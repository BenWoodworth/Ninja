package ninja.entity;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import ninja.map.GameMap;
import ninja.map.RenderOrder;
import ninja.textures.Textures;
import ninja.tile.Tile;

public class EntityTurretBall extends Entity {
	public static final double DAMAGE = 2;
	public static final double GRAVITY_SCALE = .25;
	
	public EntityTypes getType(){ return null; }
	public int getRenderOrder(){ return RenderOrder.BEHIND_ENTITY; }
	public BufferedImage getIcon(){ return Textures.getEntityTexture(10); }
	public String toString() { return "Turret Ball {x=" + getLocationX() + ",y=" + getLocationY() + "}"; }
	
	public EntityTurretBall(GameMap map, double locationX, double locationY, double velocityX, double velocityY) {
		super(map, new Rectangle2D.Double(6, 6, 4, 4), new Rectangle2D.Double(locationX, locationY, Tile.TILE_SIZE, Tile.TILE_SIZE), velocityX, velocityY);
	}
	@Override
	public void draw(Graphics g, Rectangle region, double scale) {
		g.drawImage(Textures.getEntityTexture(10), (int)((getLocation().x - region.x) * scale), (int)((getLocation().y - region.y) * scale),
				(int)(Textures.ENTITY_TEXTURE_SIZE * scale), (int)(Textures.ENTITY_TEXTURE_SIZE * scale), null);
	}
	public void tick(){
		if (getMap().getTileMap().getOverlappingBlocks(this).size() > 0)
			getMap().removeEntity(this);
		
		super.tick();
		setVelocityY(getVelocityY() + Entity.GRAVITY_ACCELERATION * GRAVITY_SCALE); //TODO Uncomment to apply gravity
		getLocation().x += getVelocityX();
		getLocation().y += getVelocityY();
		
		for (Entity e : getMap().getEntities())
			if (e instanceof LivingEntity && collidesWith(e))
				((LivingEntity)e).dealDamage(2, this);
		
	}
}
