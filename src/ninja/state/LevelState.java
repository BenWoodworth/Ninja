package ninja.state;

import java.awt.Graphics;

import ninja.entity.EntityPlayer;
import ninja.map.GameMap;

public class LevelState extends GameState {
	private GameMap map;
	
	public LevelState(StateManager stateManager, GameMap map) {
		super(stateManager);
		this.map = map;
	}

	public void keyPressed(int key){ map.getPlayer().keyPressed(key); }
	public void keyReleased(int key){ map.getPlayer().keyReleased(key); }
	public void keyTyped(int key){ map.getPlayer().keyTyped(key); }

	public void draw(Graphics g, double scale){ map.draw(g, scale); }
	public void tick(){ map.tick(); }
	public GameMap getMap(){ return map; }
	public EntityPlayer getPlayer(){ return map.getPlayer(); }

}
