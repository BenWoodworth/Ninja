package ninja.state;

import java.awt.Graphics;

public class GameState {
	protected StateManager stateManager;
	
	public GameState(StateManager stateManager){
		this.stateManager = stateManager;
	}
	
	public void keyPressed(int key){}
	public void keyReleased(int key){}
	public void keyTyped(int key){}
	
	public void tick(){}
	public void draw(Graphics g, double scale){}
}