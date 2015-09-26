package ninja.entity;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import ninja.animation.Animation;
import ninja.map.GameMap;
import ninja.map.RenderOrder;
import ninja.textures.Textures;
import ninja.tile.Tile;

public class EntityNinjaStar extends Entity {
	public EntityTypes getType(){ return null; }
	public int getRenderOrder(){ return RenderOrder.PLAYER_WEAPON; }
	public BufferedImage getIcon(){
		BufferedImage result = new BufferedImage(Textures.ENTITY_TEXTURE_SIZE, Textures.ENTITY_TEXTURE_SIZE, BufferedImage.TYPE_INT_ARGB);
		result.getGraphics().drawImage(Textures.getPlayerTexture(120), Textures.ENTITY_TEXTURE_SIZE / 4, Textures.ENTITY_TEXTURE_SIZE / 4,
				Textures.ENTITY_TEXTURE_SIZE / 2, Textures.ENTITY_TEXTURE_SIZE / 2, null);
		return result;
	}

	public static final int DAMAGE = 2;

	private Animation movingLeft;
	private Animation movingRight;

	Entity thrower;

	private boolean isRemoving;
	private int lastMoveTicks;
	private int removeTicks;
	private int fadeTicks;
	private float totalFadeTicks;

	public EntityNinjaStar(GameMap map, double x, double y, double velocityX, double velocityY, Entity thrower){
		super(map, new Rectangle2D.Double(4, 4, 8, 8), new Rectangle2D.Double(x, y, Textures.ENTITY_TEXTURE_SIZE, Textures.ENTITY_TEXTURE_SIZE), velocityX, velocityY);
		this.thrower = thrower;

		isRemoving = false;
		lastMoveTicks = 0;
		removeTicks = 0;
		fadeTicks = 0;
		opacity = 1;

		BufferedImage[] leftFrames = new BufferedImage[8];
		for (int id = 112; id < 120; id++) leftFrames[id - 112] = Textures.getPlayerTexture(id);
		movingLeft = new Animation(1, 0, leftFrames);
		BufferedImage[] rightFrames = new BufferedImage[8];
		for (int id = 120; id < 128; id++) rightFrames[id - 120] = Textures.getPlayerTexture(id);
		movingRight = new Animation(1, 0, rightFrames);
	}

	public void tick(){
		super.tick();
		if (lastMoveTicks > 0) lastMoveTicks--;
		if (!isRemoving || lastMoveTicks > 0){
			movingLeft.tick();
			movingRight.tick();
			setLocationX(getLocationX() + getVelocityX());
			setLocationY(getLocationY() + getVelocityY());
			if (!isRemoving){
				for (Tile t : map.getTileMap().getOverlappingBlocks(this)){
					if (t.isSolid()){
						isRemoving = true;
						lastMoveTicks = 2;
						removeTicks = 180;
						totalFadeTicks = fadeTicks = 100;
						break;
					}
				}
			}
			if (!isRemoving){
				for (Entity e : map.getEntities()){
					if (e instanceof LivingEntity && e != thrower && collidesWith(e)){
						isRemoving = true;
						removeTicks = 0;
						totalFadeTicks = fadeTicks = 20;
						lastMoveTicks = 0;
						((LivingEntity)e).dealDamage(DAMAGE, this);
						break;
					}
				}
			}
		}else if (isRemoving){
			if (removeTicks > 0) removeTicks--;
			if (removeTicks == 0){
				if (fadeTicks > 0) fadeTicks--;
				opacity = fadeTicks / totalFadeTicks;
				if (fadeTicks == 0) map.removeEntity(this);
			}
		}
	}

	private float opacity;
	public void draw(Graphics g, Rectangle region, double scale){
		int size = (int)(Textures.PLAYER_TEXTURE_SIZE * scale) / 2;
		BufferedImage image = getVelocityX() < 0 ? movingLeft.getCurFrame() : movingRight.getCurFrame();
		Graphics2D g2 = (Graphics2D)g;
		Composite oldComposite = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
		g2.drawImage(image, (int)((getLocationX() - region.x) * scale + size / 2), (int)((getLocationY() - region.y) * scale + size / 2), size, size, null);
		g2.setComposite(oldComposite);
	}

	public String toString(){ return "Ninja Star {x=" + getLocationX() + ",y=" + getLocationY() + "}"; }
}
