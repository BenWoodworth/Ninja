package ninja.state;

import ninja.GameCanvas;

public class ExitState extends GameState {

	public ExitState(StateManager stateManager) {
		super(stateManager);
	}
	
	public void tick(){
		GameCanvas.running = false;
		System.exit(0);
	}
}
