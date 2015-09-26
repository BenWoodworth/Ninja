package ninja.state;

import java.awt.Graphics;


public class StateManager {
	private GameState curState;
	
	
	
	public StateManager(){}
	
	public void keyPressed(int key){
		curState.keyPressed(key);
	}
	public void keyReleased(int key){
		curState.keyReleased(key);
	}
	public void keyTyped(int key){
		curState.keyTyped(key);
	}
	
	public void tick(){
		curState.tick();
	}
	public void draw(Graphics g, double scale){
		curState.draw(g, scale);
	}
	
	public void setState(GameState state){ curState = state; }
	public GameState getState(){ return curState; }
}
