package ninja.entity;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import ninja.animation.Animation;
import ninja.map.GameMap;
import ninja.map.RenderOrder;
import ninja.textures.Textures;
import ninja.tile.Tile;

public class EntityTurret extends GridEntity {
	public EntityTypes getType() { return EntityTypes.TURRET; }
	public int getRenderOrder() { return RenderOrder.ENTITY; }
	public BufferedImage getIcon() {
		BufferedImage result = new BufferedImage(Tile.TILE_SIZE, Tile.TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D)result.getGraphics();
		g.rotate(Math.PI, Tile.TILE_SIZE / 2, Tile.TILE_SIZE / 2);
		g.drawImage(Textures.getEntityTexture(9), 0, 0, null);
		g.drawImage(Textures.getEntityTexture(8), 0, 0, null);
		return result;
	}
	public String toString(){ return "Turret {x=" + (int)getLocationX() + ",y=" + (int)getLocationY() + "}"; }
	
	public static final double ROTATE_AMOUNT = Math.PI / 64;
	public static final double ROTATE_RANGE = Math.PI / 2;
	public static final double BALL_SPEED = 2;
	public static final int BARREL_OFFSET = 2;
	
	private int fireTicks;
	private double fireRange;
	private int rotationPointX;
	private int rotationPointY;
	
	private Animation baseAnimation;
	private Animation barrelAnimation;
	
	private int rotation;
	private double targetAngleA;
	private double targetAngleB;
	private double angle;
	
	public EntityTurret(GameMap map, double x, double y, int rotation, int fireTicks, double fireRange){
		super(map, new Rectangle2D.Double(0, 0, Tile.TILE_SIZE, Tile.TILE_SIZE), new Rectangle2D.Double(x, y, Tile.TILE_SIZE, Tile.TILE_SIZE));
		baseAnimation = new Animation(rotation, Textures.getEntityTexture(8));
		barrelAnimation = new Animation(rotation, Textures.getEntityTexture(9));
		setRotation(rotation);
		this.fireTicks = fireTicks;
		this.fireRange = fireRange;
		ticksUntilFire = fireTicks;
	}
	
	public void setFireRate(int fireRate){ this.fireTicks = Math.max(1, fireRate); }
	public int getFireRate(){ return  fireTicks; }
	
	public void setRotation(int rotation){
		rotation = (rotation % 4 + 4) % 4;
		this.rotation = rotation;
		baseAnimation.setRotation(rotation);
		baseAnimation.updateFrames();
		barrelAnimation.setRotation(rotation);
		barrelAnimation.updateFrames();
		switch (rotation){
		case 0:
			
			rotationPointX = Tile.TILE_SIZE / 2;
			rotationPointY = BARREL_OFFSET;
			targetAngleA = -Math.PI / 2;
			targetAngleB = 3 * Math.PI / 2;
			break;
		case 1:
			
			rotationPointX = Tile.TILE_SIZE - BARREL_OFFSET;
			rotationPointY = Tile.TILE_SIZE / 2;
			targetAngleA = Math.PI;
			targetAngleB = -Math.PI;
			break;
		case 2:
			
			rotationPointX = Tile.TILE_SIZE / 2;
			rotationPointY = Tile.TILE_SIZE - BARREL_OFFSET;
			targetAngleA = -3 * Math.PI / 2;
			targetAngleB = Math.PI / 2;
			break;
		case 3:
			
			rotationPointX = BARREL_OFFSET;
			rotationPointY = Tile.TILE_SIZE / 2;
			targetAngleA = 0;
			targetAngleB = 0;
			break;
		}
		angle = targetAngleA;
	}
	public int getRotation(){ return rotation; }
	public void setFireRange(double fireRange){ this.fireRange = fireRange; }
	public double getFireRange(){ return fireRange; }
	
	int ticksUntilFire;
	public void tick(){
		if (this.isInRegion(this.getMap().getRegion())){
			// http://en.wikipedia.org/wiki/Trajectory_of_a_projectile#Angle_required_to_hit_coordinate_.28x.2Cy.29
			
			
			double x = getMap().getPlayer().getLocationX() + getMap().getPlayer().getHitbox().x + getMap().getPlayer().getHitbox().width / 2 - (getLocationX() + rotationPointX);
			double y = -(getMap().getPlayer().getLocationY() + getMap().getPlayer().getHitbox().y + getMap().getPlayer().getHitbox().height / 2 - (getLocationY() + rotationPointY));
			double g = Entity.GRAVITY_ACCELERATION * EntityTurretBall.GRAVITY_SCALE;
			double v = BALL_SPEED;
			double radicand = v*v*v*v - g*(g*x*x + 2*y*v*v);
			if (radicand < 0) return;
			
			double angleA = (v*v + Math.sqrt(radicand)) / (g*x);
			double angleB = (v*v - Math.sqrt(radicand)) / (g*x);
			
			//Choose based off shortest time to reach player

			boolean aIsInRange = Math.abs(angleA - targetAngleA) < ROTATE_RANGE || Math.abs(angleA - targetAngleA) < ROTATE_RANGE;
			boolean bIsInRange = Math.abs(angleB - targetAngleA) < ROTATE_RANGE || Math.abs(angleB - targetAngleA) < ROTATE_RANGE;
			
			double playerAngle = angleA;
			if (aIsInRange || bIsInRange){
				if (aIsInRange ^ bIsInRange){
					if (aIsInRange)
						playerAngle = angleA;
					else
						playerAngle = angleB;
				}else{
					double sinA = Math.sin(angleA);
					double sinB = Math.sin(angleB);
					double timeA = (angleA % Math.PI == 0) ? (Math.sqrt(2 * g * y + sinA * sinA) + sinA) / g : x / Math.cos(angleA);
					double timeB = (angleB % Math.PI == 0) ? (Math.sqrt(2 * g * y + sinB * sinB) + sinB) / g : x / Math.cos(angleB);
					if (timeA < timeB)
						playerAngle = angleA;
					else
						playerAngle = angleB;
				}
			}
			
			if (Math.abs(angle - angleB) < Math.abs(angle - angleA))
				playerAngle = angleB;
			
			//double playerAngle = Math.atan2(-(y - (rotationPointY + getLocationY())), x - (rotationPointX + getLocationX())); //Directly target player
			
			if (Math.abs(playerAngle - angle) < ROTATE_AMOUNT)
				angle = playerAngle;
			else if (playerAngle < angle)
				angle -= ROTATE_AMOUNT;
			else
				angle += ROTATE_AMOUNT;
			
			
			double targetAngle = targetAngleA;
			if (Math.abs(targetAngleB - angle) < Math.abs(targetAngleA - angle))
				targetAngle = targetAngleB;
			
			if (Math.abs(targetAngle - angle) > ROTATE_RANGE){
				if (angle < targetAngle)
					angle = targetAngle - ROTATE_RANGE;
				else
					angle = targetAngle + ROTATE_RANGE;
			}
			
			if (ticksUntilFire > 0) ticksUntilFire--;
			if (ticksUntilFire <= 0 && distanceSquared(getMap().getPlayer()) < fireRange * fireRange){
				ticksUntilFire = fireTicks;
				EntityTurretBall ball = new EntityTurretBall(getMap(),
						getLocationX() - Textures.ENTITY_TEXTURE_SIZE / 2 + rotationPointX,
						getLocationY() - Textures.ENTITY_TEXTURE_SIZE / 2 + rotationPointY,
						BALL_SPEED * Math.cos(angle), -BALL_SPEED * Math.sin(angle));
				getMap().addEntity(ball);
			}
		}
	}
	
	public void draw(Graphics g, Rectangle region, double scale){
		Graphics2D g2 = (Graphics2D)g;
		AffineTransform original = g2.getTransform();
		

		g2.rotate(-((rotation + 1) * Math.PI / 2 + angle), (rotationPointX + getLocation().x - region.x) * scale, (rotationPointY + getLocation().y - region.y) * scale);
		g2.drawImage(barrelAnimation.getCurFrame(), (int)((getLocation().x - region.x) * scale), (int)((getLocation().y - region.y) * scale),
				(int)(Tile.TILE_SIZE * scale), (int)(Tile.TILE_SIZE * scale), null);
		g2.setTransform(original);
		
		g2.drawImage(baseAnimation.getCurFrame(), (int)((getLocation().x - region.x) * scale), (int)((getLocation().y - region.y) * scale),
				(int)(Tile.TILE_SIZE * scale), (int)(Tile.TILE_SIZE * scale), null);
		
	}
}
