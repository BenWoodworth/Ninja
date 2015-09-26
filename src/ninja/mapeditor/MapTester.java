package ninja.mapeditor;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import ninja.GameCanvas;
import ninja.Ninja;
import ninja.map.GameMap;
import ninja.state.LevelState;
import ninja.state.StateManager;

public class MapTester extends JFrame {
	private static final long serialVersionUID = -3401568011593152786L;
	
	private JPanel contentPane;
	
	public MapTester(GameMap map) {
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		StateManager stateManager = new StateManager();
		stateManager.setState(new LevelState(stateManager, map));
		
		GameCanvas game = new GameCanvas(stateManager);
		this.setResizable(false);
		this.add(game);
		this.setIconImages(Ninja.frame.getIconImages());
		this.setTitle(Ninja.frame.getTitle() + " - Map Tester");
		this.pack();
		this.setLocationRelativeTo(null);
	}

}
