package ninja.entity;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import ninja.map.GameMap;

public abstract class Entity {
	public static final double GRAVITY_ACCELERATION = 8 / 60.;
	
	public abstract EntityTypes getType();
	public abstract int getRenderOrder();
	public abstract BufferedImage getIcon();
	public abstract String toString();
	
	protected GameMap map;
	public void tileMapChange(){}
	
	private Rectangle2D.Double hitbox;
	private Rectangle2D.Double location;
	private double velocityX;
	private double velocityY;
	private int ticksAlive;
	
	public Entity(GameMap map, Rectangle2D.Double hitbox, Rectangle2D.Double location, double velocityX, double velocityY){
		this.map = map;
		this.hitbox = hitbox;
		this.location = location;
		this.velocityX = velocityX;
		this.velocityY = velocityY;
		ticksAlive = 0;
	}
	
	public GameMap getMap(){ return map; }
	
	public Rectangle2D.Double getLocation(){ return location; }
	public void setLocation(Rectangle2D.Double location){ this.location = location; }
	public void setLocationXY(double x, double y){ this.location.x = x; this.location.y = y; }
	public void setLocationX(double x){ this.location.x = x; }
	public void setLocationY(double y){ this.location.y = y; }
	public double getLocationX(){ return location.x; }
	public double getLocationY(){ return location.y; }
	
	public void setVelocityX(double velocityX){ this.velocityX = velocityX; }
	public double getVelocityX(){ return velocityX; }
	public void setVelocityY(double velocityY){ this.velocityY = velocityY; }
	public double getVelocityY(){ return velocityY; }
	public void setVelocity(double velocityX, double velocityY){ this.velocityX = velocityX; this.velocityY = velocityY; }
	
	public Rectangle2D.Double getHitbox(){ return hitbox; }
	public void setHitbox(Rectangle2D.Double hitbox){ this.hitbox = hitbox; }
	public boolean isInRegion(Rectangle rect){
		Rectangle2D.Double hb = new Rectangle2D.Double(hitbox.x + location.x, hitbox.y + location.y, hitbox.width, hitbox.height);
		return location.intersects(rect) || hb.intersects(rect);
	}
	public boolean collidesWith(Entity e){
		if (hitbox == null || location == null || e.getHitbox() == null || e.location == null) return false;
		Rectangle2D.Double a = new Rectangle2D.Double(e.getHitbox().x + e.location.x, e.getHitbox().y + e.location.y, e.getHitbox().width, e.getHitbox().height);
		Rectangle2D.Double b = new Rectangle2D.Double(hitbox.x + location.x, hitbox.y + location.y, hitbox.width, hitbox.height);
		return a.intersects(b);
	}
	
	public abstract void draw(Graphics g, Rectangle region, double scale);
	public void tick(){ ticksAlive++; }
	public int getTicksAlive(){ return ticksAlive; }
	
	public double distanceSquared(Entity e){
		double dx = getLocationX() - e.getLocationX();
		double dy = getLocationY() - e.getLocationY();
		return dx * dx + dy * dy;
	}
	public double distance(Entity e){
		return Math.sqrt(distanceSquared(e));
	}
}
