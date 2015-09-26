package ninja.entity;

import java.awt.geom.Rectangle2D;
import java.util.List;

import ninja.map.GameMap;
import ninja.tile.Tile;

public abstract class LivingEntity extends Entity {
	public static final double GRAVITY_MAX_SPEED = 8;
	public static final double MAX_VELOCITY = Tile.TILE_SIZE / 2;
	
	protected double health;
	protected double maxHealth;
	
	public LivingEntity(GameMap map, Rectangle2D.Double hitbox, Rectangle2D.Double location, double velocityX, double velocityY, double health, double maxHealth) {
		super(map, hitbox, location, velocityX, velocityY);
		setMaxHealth(maxHealth);
		setHealth(health);
	}
	
	public double getHealth(){ return health; }
	public void setHealth(double health){ this.health = Math.max(0, Math.min(maxHealth, health)); }
	public double getMaxHealth(){ return maxHealth; }
	public void setMaxHealth(double maxHealth){
		this.maxHealth = Math.max(0, maxHealth);
		this.health = Math.min(health, this.maxHealth);
	}
	public void tick(){
		super.tick();
		
		if (getVelocityX() > MAX_VELOCITY) setVelocityX(MAX_VELOCITY);
		if (getVelocityX() < -MAX_VELOCITY) setVelocityX(-MAX_VELOCITY);
		if (getVelocityY() > MAX_VELOCITY) setVelocityY(MAX_VELOCITY);
		if (getVelocityY() < -MAX_VELOCITY) setVelocityY(-MAX_VELOCITY);
		
		setLocationX(getLocationX() + getVelocityX());
		List<Tile> overlappingBlocks = map.getTileMap().getOverlappingBlocks(this);
		for (Tile t : overlappingBlocks){
			if (t.isSolid()){
				if (getLocationX() < (t.x + .5) * Tile.TILE_SIZE){
					setLocationX(t.x * Tile.TILE_SIZE - getHitbox().getWidth() - getHitbox().getX());
				}else{
					setLocationX((t.x + 1) * Tile.TILE_SIZE - getHitbox().getX());
				}
				setVelocityX(0);
				break;
			}
		}

		setLocationY(getLocationY() + getVelocityY());
		overlappingBlocks = map.getTileMap().getOverlappingBlocks(this);
		for (Tile t : overlappingBlocks){
			if (t.isSolid()){
				if (getLocationY() < (t.y + .5) * Tile.TILE_SIZE){
					setLocationY(t.y * Tile.TILE_SIZE - getHitbox().getHeight() - getHitbox().getY());
				}else{
					setLocationY((t.y + 1) * Tile.TILE_SIZE - getHitbox().getY());
				}
				setVelocityY(0);
				break;
			}
		}
	}
	
	public boolean dealDamage(double damage, Entity damager){ return false; }
}
