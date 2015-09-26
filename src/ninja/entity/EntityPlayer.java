package ninja.entity;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import ninja.Controls;
import ninja.animation.Animation;
import ninja.map.GameMap;
import ninja.map.RenderOrder;
import ninja.textures.Textures;
import ninja.tile.Tile;

public class EntityPlayer extends LivingEntity {
	public EntityTypes getType(){ return EntityTypes.PLAYER; }
	public int getRenderOrder(){ return RenderOrder.PLAYER; }
	public BufferedImage getIcon(){ return Textures.getPlayerTexture(8); }
	public String toString(){ return "Player {x=" + getLocationX() + ",y=" + getLocationY() + "}"; }
	
	private static final double MOVE_ACC = .2;
	private static final double MOVE_FALLING_ACC = .04;
	private static final double MOVE_MAX = 1.5;
	private static final double GROUND_DEACCELERATE = .05;
	private static final double JUMP_VELOCITY = -3.5;
	private static final double CLIMB_SPEED = .75;
	private static final double CLIMB_JUMP_VELOCITY_X = 1;
	private static final double CLIMB_JUMP_VELOCITY_Y = -2.25;

	public static final int FLASH_DURATION = 10;

	private Animation curAnimation;
	private Animation idleLeft;
	private Animation idleRight;
	private Animation walkingLeft;
	private Animation walkingRight;
	private Animation fallingLeft;
	private Animation fallingRight;
	private Animation throwStarLeft;
	private Animation throwStarRight;
	private Animation reachingLeft;
	private Animation reachingRight;
	private Animation climbingLeft;
	private Animation climbingRight;
	private Animation climbingIdleLeft;
	private Animation climbingIdleRight;

	public boolean isOnGround;
	public boolean isFacingRight;
	public boolean isClimbing;

	public boolean isKeyLeftPressed;
	public boolean isKeyRightPressed;
	public boolean isKeyUpPressed;
	public boolean isKeyDownPressed;
	public boolean isKeyClimbPressed;
	public boolean isKeyThrowStarPressed;

	public int damagedTicks;
	public int ninjaStarTicks;

	boolean createStar;
	int createStarTicks;

	public int health;

	public EntityPlayer(GameMap map, double x, double y) {
		super(map, new Rectangle2D.Double(4, 1, 8, 15),
				new Rectangle2D.Double(x, y, Textures.PLAYER_TEXTURE_SIZE, Textures.PLAYER_TEXTURE_SIZE), 0, 0, 10, 10);

		idleLeft = createAnimation(0, 10);
		idleRight = createAnimation(8, 10);
		walkingLeft = createAnimation(16, 5);
		walkingRight = createAnimation(24, 5);
		fallingLeft = createAnimation(32, 5);
		fallingRight = createAnimation(40, 5);
		throwStarLeft = createAnimation(48, 5);
		throwStarRight = createAnimation(56, 5);
		reachingLeft = createAnimation(64, 5);
		reachingRight = createAnimation(72, 5);
		climbingLeft = createAnimation(80, 15);
		climbingRight = createAnimation(88, 15);
		climbingIdleLeft = createAnimation(96, 15);
		climbingIdleRight = createAnimation(104, 15);

		curAnimation = idleRight;

		isClimbing = false;
		isOnGround = true;
		isFacingRight = true;
		damagedTicks = 0;

		createStarTicks = 0;
		createStar = false;

		health = 20;
	}
	private Animation createAnimation(int startID, int ticksPerFrame){
		BufferedImage[] frames = new BufferedImage[8];
		for (int id = startID; id < startID + 8; id++){
			frames[id - startID] = Textures.getPlayerTexture(id);
		}
		return new Animation(frames.length == 1?-1:ticksPerFrame, 0, frames);
	}
	private void setAnimation(Animation animation, boolean paused){
		if (curAnimation != animation || curAnimation.isPaused() != paused){
			animation.resetLoopCount();
			animation.setCurFrameIndex(0);
			animation.setPaused(paused);
			curAnimation = animation;
		}
	}

	@Override
	public void draw(Graphics g, Rectangle region, double scale){
		
		Graphics2D g2 = (Graphics2D)g;
		Composite oldComposite = g2.getComposite();
		if (damagedTicks / FLASH_DURATION % 2 == 1)
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4F));;
		BufferedImage image = curAnimation.getCurFrame();
		g.drawImage(image, (int)((getLocationX() - region.getX()) * scale), (int)((getLocationY() - region.getY()) * scale), (int)(image.getWidth() * scale), (int)(image.getHeight() * scale), null);
		g2.setComposite(oldComposite);
	}

	private void timerSubtract(){
		if (damagedTicks > 0) damagedTicks--;
		if (ninjaStarTicks > 0) ninjaStarTicks--;
		if (createStarTicks > 0) createStarTicks--;
	}

	@Override
	public void tick(){
		super.tick();
		timerSubtract();

		boolean isThrowingStar = (curAnimation == throwStarRight || curAnimation == throwStarLeft) && curAnimation.getLoopCount() < 1;

		Tile tileD = map.getTileMap().getTile((int)Math.floor((getLocationX() + getHitbox().getX()) / Tile.TILE_SIZE), (int)Math.floor(getLocationY() / Tile.TILE_SIZE + 1));
		Tile tileDR = map.getTileMap().getTile((int)Math.floor((getLocationX() + getHitbox().getX() + getHitbox().getWidth()) / Tile.TILE_SIZE), (int)Math.floor(getLocationY() / Tile.TILE_SIZE + 1));
		boolean isAboveD = getLocationX() + getHitbox().getX() > (int)((getLocationX() + getHitbox().getX()) / Tile.TILE_SIZE) * Tile.TILE_SIZE;
		boolean isAboveDR = getLocationX() + getHitbox().getX() + getHitbox().getWidth() > (int)((getLocationX() + getHitbox().getX() + getHitbox().getWidth()) / Tile.TILE_SIZE) * Tile.TILE_SIZE;
		isOnGround = (getLocationY() % Tile.TILE_SIZE == 0 && (isAboveD && tileD != null && tileD.isSolid() || isAboveDR && tileDR != null && tileDR.isSolid()));

		isClimbing = false;
		if (!isOnGround && isKeyClimbPressed && !isThrowingStar){
			int lr = isFacingRight?1:-1;
			Tile tileA = map.getTileMap().getTile((int)Math.floor((getLocationX() + getHitbox().getX()) / Tile.TILE_SIZE) + lr, (int)Math.floor(getLocationY() / Tile.TILE_SIZE));
			Tile tileB = tileA == null?null:tileA.getTileD();
			boolean onWall = isFacingRight && (getLocationX() - getLocation().width + getHitbox().x + getHitbox().width) % Tile.TILE_SIZE == 0;
			onWall = onWall || (!isFacingRight && (getLocationX() + getHitbox().x) % Tile.TILE_SIZE == 0);
			isClimbing = tileA != null && tileB != null && tileA != tileB && onWall;
		}

		if (isClimbing){
			if (isKeyUpPressed ^ isKeyDownPressed){
				setVelocityX(0);
				if (isKeyUpPressed){
					setVelocityY(-CLIMB_SPEED);
				}else{
					setVelocityY(CLIMB_SPEED);
				}
			}else{
				setVelocityY(0);
			}
			if (isKeyLeftPressed && isFacingRight){
				setVelocityX(getVelocityX() - CLIMB_JUMP_VELOCITY_X);
				setVelocityY(getVelocityY() + CLIMB_JUMP_VELOCITY_Y);
			}else if (isKeyRightPressed && !isFacingRight){
				setVelocityX(getVelocityX() + CLIMB_JUMP_VELOCITY_X);
				setVelocityY(getVelocityY() + CLIMB_JUMP_VELOCITY_Y);
			}
		}else if ((isKeyLeftPressed ^ isKeyRightPressed) && !isThrowingStar){
			if (isKeyLeftPressed){
				double acceleration = isOnGround?MOVE_ACC:MOVE_FALLING_ACC;
				double newVelocity = getVelocityX() - acceleration;
				if (getVelocityX() > -MOVE_MAX && newVelocity < -MOVE_MAX)
					setVelocityX(-MOVE_MAX);
				else if (getVelocityX() > -MOVE_MAX)
					setVelocityX(newVelocity);
				isFacingRight = false;
			}else{
				double acceleration = isOnGround?MOVE_ACC:MOVE_FALLING_ACC;
				double newVelocity = getVelocityX() + acceleration;
				if (getVelocityX() < MOVE_MAX && newVelocity > MOVE_MAX)
					setVelocityX(MOVE_MAX);
				else if (getVelocityX() < MOVE_MAX)
					setVelocityX(newVelocity);
				isFacingRight = true;
			}
		}else{
			if (isOnGround){
				if (getVelocityX() > 0){
					setVelocityX(getVelocityX() - MOVE_ACC);
					if (getVelocityX() < 0) setVelocityX(0);
				}else if (getVelocityX() < 0){
					setVelocityX(getVelocityX() + MOVE_ACC);
					if (getVelocityX() > 0) setVelocityX(0);
				}
			}
		}

		if (isOnGround){
			Tile tileU = map.getTileMap().getTile((int)((getLocationX() + getHitbox().getX()) / Tile.TILE_SIZE), (int)(getLocationY() / Tile.TILE_SIZE - 1));
			Tile tileUR = map.getTileMap().getTile((int)((getLocationX() + getHitbox().getX() + getHitbox().getWidth()) / Tile.TILE_SIZE), (int)(getLocationY() / Tile.TILE_SIZE - 1));
			boolean isBelowU = getLocationX() + getHitbox().getX() > (int)((getLocationX() + getHitbox().getX()) / Tile.TILE_SIZE) * Tile.TILE_SIZE;
			boolean isBelowUR = getLocationX() + getHitbox().getX() + getHitbox().getWidth() > (int)((getLocationX() + getHitbox().getX() + getHitbox().getWidth()) / Tile.TILE_SIZE) * Tile.TILE_SIZE;
			boolean canJump = (isBelowU && tileU == null || !isBelowU) && (isBelowUR && tileUR == null || !isBelowUR);
			if (isKeyUpPressed && !isThrowingStar && canJump){
				setVelocityY(JUMP_VELOCITY);
			}else if (getVelocityX() > MOVE_MAX){
				setVelocityX(getVelocityX() - GROUND_DEACCELERATE);
				if (getVelocityX() < MOVE_MAX) setVelocityX(MOVE_MAX);
			}else if (getVelocityX() < -MOVE_MAX){
				setVelocityY(getVelocityY() + GROUND_DEACCELERATE);
				if (getVelocityX() > -MOVE_MAX) setVelocityX(-MOVE_MAX);
			}
		}else if (!isClimbing){
			setVelocityY(Math.max(getVelocityY(), Math.min(GRAVITY_MAX_SPEED, getVelocityY() + GRAVITY_ACCELERATION)));
		}

		if (ninjaStarTicks == 0 && isKeyThrowStarPressed && !isThrowingStar && !isClimbing){
			createStarTicks = 0;
			createStar = true;

			ninjaStarTicks = 60;
			
			isThrowingStar = true;
			if (isFacingRight){
				setAnimation(throwStarRight, false);
			}else{
				setAnimation(throwStarLeft, false);
			}
		}
		if (createStar && createStarTicks == 0){
			createStar = false;
			EntityNinjaStar star = new EntityNinjaStar(map, getLocationX(), getLocationY(), 2.5, 0, this);
			if (!isFacingRight) star.setVelocityX(star.getVelocityX() * -1);
			map.addEntity(star);
		}

		if (getVelocityX() > MAX_VELOCITY) setVelocityX(MAX_VELOCITY);
		else if (getVelocityX() < -MAX_VELOCITY) setVelocityX(-MAX_VELOCITY);
		if (getVelocityY() > MAX_VELOCITY) setVelocityY(MAX_VELOCITY);
		else if (getVelocityY() < -MAX_VELOCITY) setVelocityY(-MAX_VELOCITY);

		if (isThrowingStar){
			System.out.print("");
		}else if (isClimbing){
			if (isFacingRight){
				if (isKeyUpPressed ^ isKeyDownPressed){
					setAnimation(climbingRight, false);
				}else{
					setAnimation(climbingIdleRight, false);
				}
			}else{
				if (isKeyUpPressed ^ isKeyDownPressed){
					setAnimation(climbingLeft, false);
				}else{
					setAnimation(climbingIdleLeft, false);
				}
			}
		}else if (!isOnGround){
			if (isKeyClimbPressed){
				if (isFacingRight){
					setAnimation(reachingRight, false);
				}else{
					setAnimation(reachingLeft, false);
				}
			}else{
				if (getVelocityY() > 0){
					if (isFacingRight){
						setAnimation(fallingRight, false);
					}else{
						setAnimation(fallingLeft, false);
					}
				}else{
					if (isFacingRight){
						setAnimation(idleRight, true);
					}else{
						setAnimation(idleLeft, true);
					}
				}
			}
		}else if (isOnGround){
			if (getVelocityX() == 0){
				if (isFacingRight){
					setAnimation(idleRight, false);
				}else{
					setAnimation(idleLeft, false);
				}
			}else{
				if (isFacingRight){
					setAnimation(walkingRight, false);
				}else{
					setAnimation(walkingLeft, false);
				}
			}

		}
		curAnimation.tick();
	}

	@Override
	public boolean dealDamage(double damage, Entity damager){
		if (damagedTicks > 0) return false;
		health -= damage;
		damagedTicks = 180;
		return true;
	}

	public void keyPressed(int key){
		switch (key){
		case Controls.KEY_UP: isKeyUpPressed = true; break;
		case Controls.KEY_DOWN: isKeyDownPressed = true; break;
		case Controls.KEY_LEFT: isKeyLeftPressed = true; break;
		case Controls.KEY_RIGHT: isKeyRightPressed = true; break;
		case Controls.KEY_THROW_STAR: isKeyThrowStarPressed = true; break;
		case Controls.KEY_CLIMB: isKeyClimbPressed = true; break;
		}
	}
	public void keyReleased(int key){
		switch (key){
		case Controls.KEY_UP: isKeyUpPressed = false; break;
		case Controls.KEY_DOWN: isKeyDownPressed = false; break;
		case Controls.KEY_LEFT: isKeyLeftPressed = false; break;
		case Controls.KEY_RIGHT: isKeyRightPressed = false; break;
		case Controls.KEY_THROW_STAR: isKeyThrowStarPressed = false; break;
		case Controls.KEY_CLIMB: isKeyClimbPressed = false; break;
		}
	}
	public void keyTyped(int key){}
}
