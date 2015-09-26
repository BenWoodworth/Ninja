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

public class EntitySpikes extends GridEntity {
	@Override
	public int getRenderOrder() { return RenderOrder.FOREGROUND; }
	@Override
	public BufferedImage getIcon() { return Textures.getEntityTexture(11); }
	@Override
	public String toString() { return "Spikes {x=" + (int)getLocationX() + ",y=" + (int)getLocationY() + "}"; }
	@Override
	public EntityTypes getType() { return EntityTypes.SPIKES; }


	public static final int DAMAGE = 1;
	public static final double HITBOX = 5;

	private Animation animation;

	private int rotation;
	public EntitySpikes(GameMap map, double x, double y, int rotation){
		super(map, null, new Rectangle2D.Double(x, y, Tile.TILE_SIZE, Tile.TILE_SIZE));
		setRotation(rotation);
		animation = new Animation(getRotation(), Textures.getEntityTexture(11));
	}

	public void setRotation(int rotation){
		this.rotation = rotation = (rotation % 4 + 4) % 4;
		if (animation != null){
			animation.setRotation(rotation);
			animation.updateFrames();
		}
		switch(rotation){
		case 0: setHitbox(new Rectangle2D.Double(0, 0, Tile.TILE_SIZE, HITBOX)); break;
		case 1: setHitbox(new Rectangle2D.Double(Tile.TILE_SIZE - HITBOX, 0, HITBOX, Tile.TILE_SIZE)); break;
		case 2: setHitbox(new Rectangle2D.Double(0, Tile.TILE_SIZE - HITBOX, Tile.TILE_SIZE, HITBOX)); break;
		case 3: setHitbox(new Rectangle2D.Double(0, 0, HITBOX, Tile.TILE_SIZE));
		}
	}
	public int getRotation(){ return rotation; }

	public void tick(){
		for (Entity e : getMap().getEntities()){
			if (e instanceof LivingEntity && collidesWith(e))
				((LivingEntity)e).dealDamage(DAMAGE, this);
		}
	}
	
	@Override
	public void draw(Graphics g, Rectangle region, double scale) {
		g.drawImage(animation.getCurFrame(), (int)((getLocation().x - region.x) * scale), (int)((getLocation().y - region.y) * scale),
				(int)(Tile.TILE_SIZE * scale), (int)(Tile.TILE_SIZE * scale), null);
	}
}
