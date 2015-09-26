package ninja;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.UIManager;

import ninja.map.GameMap;
import ninja.state.ExitState;
import ninja.state.GameState;
import ninja.state.LevelState;
import ninja.state.MapEditorState;
import ninja.state.MenuState;
import ninja.state.StateManager;

public class Ninja {
	public static JFrame frame;
	public static GameCanvas gameCanvas;
	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		frame = new JFrame();
		frame.setResizable(false);

		StateManager stateManager = new StateManager();
		List<String> menuItems = new ArrayList<String>();
		List<GameState> states = new ArrayList<GameState>();
		ExitState exitState = new ExitState(stateManager);
		menuItems.add("Play");
		try {
			states.add(new LevelState(stateManager, GameMap.fromInputStreamReader(new InputStreamReader(ResourceFetch.fetch("/maps/Level1.map")))));
		} catch (IOException e) {
			e.printStackTrace();
		}
		menuItems.add("Map Editor");
		MapEditorState editorState = new MapEditorState(stateManager, null);
		states.add(editorState);
		menuItems.add("Exit");
		states.add(exitState);
		MenuState curState = new MenuState(stateManager, "Ninja!", "(Better Title Pending)", menuItems, states, exitState);
		editorState.setReturnMenuState(curState);
		stateManager.setState(curState);

		gameCanvas = new GameCanvas(stateManager);
		frame.add(gameCanvas);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImages(new Ninja().getIcon());
		frame.setTitle("Awesome Ninja Game!");
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public List<Image> getIcon(){
		List<Image> result = new ArrayList<Image>();
		try {
			File folder = new File(getClass().getResource("/icon/").toURI());
			for (File f : folder.listFiles()){
				InputStream is = getClass().getResourceAsStream("/icon/" + f.getName());
				result.add(ImageIO.read(is));
				is.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
}
