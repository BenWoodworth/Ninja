package ninja.animation;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Animation {
	private BufferedImage[] rawFrames;
	public List<BufferedImage> frames;
	private int curFrame;
	private int loopCount;
	private int ticksPerFrame;
	private int tickCount;
	private boolean paused;
	private int rotation;
	
	public Animation(int ticksPerFrame, int rotation, BufferedImage... frames){
		this.rawFrames = frames;
		this.frames = new ArrayList<BufferedImage>();
		this.ticksPerFrame = ticksPerFrame;
		curFrame = 0;
		loopCount = 0;
		tickCount = 0;
		paused = ticksPerFrame == -1;
		setRotation(rotation);
		updateFrames();
	}
	public Animation(int rotation, BufferedImage frame){
		this(-1, rotation, frame);
	}
	
	public void tick(){
		if (paused) return;
		tickCount++;
		if (tickCount >= ticksPerFrame){
			tickCount = 0;
			curFrame = (curFrame + 1) % frames.size();
			if (curFrame == 0) loopCount++;
		}
	}
	
	public void resetLoopCount(){ loopCount = 0; }
	public int getLoopCount(){ return loopCount; }
	
	public void setRotation(int rotation){
		rotation = (rotation % 4 + 4) % 4;
		this.rotation = rotation;
	}
	public int getRotation(){ return rotation; }
	
	public void updateFrames(){
		frames.clear();
		for (BufferedImage i : rawFrames){
			if (i != null && i.getRGB(0, 0) != 0xFF7F007F){
				if (rotation == 0){
					frames.add(i);
				}else{
					BufferedImage bi = new BufferedImage(i.getWidth(), i.getHeight(), BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = (Graphics2D)bi.getGraphics();
					g.setTransform(AffineTransform.getQuadrantRotateInstance(rotation, bi.getWidth() / 2, bi.getHeight() / 2));
					g.drawImage(i, 0, 0, null);
					frames.add(bi);
				}
			}
		}
		curFrame = 0;
	}
	
	public BufferedImage getCurFrame(){ return getFrame(curFrame); }
	public BufferedImage getFrame(int frame){ return frames.get(frame); }
	public void setCurFrameIndex(int frame){ tickCount = 0; curFrame = frame; }
	public int getCurFrameIndex(){ return curFrame; }
	
	public boolean isPaused(){ return paused; }
	public void setPaused(boolean paused){ this.paused = paused; }
}
