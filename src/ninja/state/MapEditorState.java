package ninja.state;

import ninja.mapeditor.MapEditor;
import ninja.Ninja;

public class MapEditorState extends GameState {

	private GameState returnMenu;
	public MapEditorState(StateManager stateManager, GameState returnMenu) {
		super(stateManager);
		this.returnMenu = returnMenu;
	}
	
	public void tick(){
		stateManager.setState(returnMenu);
		MapEditor.run();
		Ninja.frame.setVisible(false);
		Ninja.gameCanvas.pause();
	}
	public GameState getReturnMenuState(){ return returnMenu; }
	public void setReturnMenuState(GameState state){ returnMenu = state; }
}
