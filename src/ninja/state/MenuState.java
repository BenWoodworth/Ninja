package ninja.state;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import ninja.GameCanvas;
import ninja.textures.Textures;

public class MenuState extends GameState {
	public static final int KEY_ARROW_UP = 38;
	public static final int KEY_ARROW_DOWN = 40;
	public static final int KEY_ARROW_LEFT = 37;
	public static final int KEY_ARROW_RIGHT = 39;
	public static final int KEY_ENTER = 10;
	public static final int KEY_ESC = 27;
	
	public static final double BACKGROUND_SPEED = .25;

	public static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 50);
	public static final Font FONT_SUBTITLE = new Font("Arial", Font.BOLD, 20);
	public static final Font FONT_ITEM = new Font("Arial", Font.PLAIN, 20);
	public static final Font FONT_ITEM_SELECTED = new Font("Arial", Font.BOLD, 20);
	public static final Color FONT_TITLE_COLOR = Color.DARK_GRAY;
	public static final Color FONT_SUBTITLE_COLOR = new Color(92, 92, 92);
	public static final Color FONT_ITEM_COLOR = Color.DARK_GRAY;
	public static final Color FONT_ITEM_SELECTED_COLOR = Color.LIGHT_GRAY;
	
	protected String title;
	protected String subtitle;
	protected List<String> menuItems;
	protected List<GameState> states;
	protected GameState escapeMenu;
	protected int curMenuItem;
	protected double backgroundX;
	protected BufferedImage background;
	
	public MenuState(StateManager stateManager, String title, String subtitle,
			List<String> menuItems, List<GameState> states, GameState escapeMenu) {
		super(stateManager);
		this.title = title;
		this.subtitle = subtitle;
		this.menuItems = menuItems;
		this.states = states;
		this.escapeMenu = escapeMenu;
		curMenuItem = 0;
		backgroundX = 0;
		background = Textures.getBackgroundTexture(0);
	}
	
	public void keyPressed(int key){
		switch (key){
		case KEY_ARROW_UP:
			curMenuItem = (curMenuItem - 1 + menuItems.size()) % menuItems.size();
			break;
		case KEY_ARROW_DOWN:
			curMenuItem = (curMenuItem + 1) % menuItems.size();
			break;
		case KEY_ARROW_LEFT:
			
			break;
		case KEY_ARROW_RIGHT:
			
			break;
		case KEY_ENTER:
			stateManager.setState(states.get(curMenuItem));
			break;
		case KEY_ESC:
			stateManager.setState(escapeMenu);
			break;
		}
	}
	
	public void tick(){
		backgroundX = (backgroundX - BACKGROUND_SPEED + background.getWidth()) % background.getWidth();
	}
	public void draw(Graphics g, double scale){
		g.drawImage(background, (int)(backgroundX * scale), 0, (int)(background.getWidth() * scale), (int)(background.getHeight() * scale), null);
		if (backgroundX != 0) g.drawImage(background, (int)((backgroundX - background.getWidth()) * scale), 0, (int)(background.getWidth() * scale), (int)(background.getHeight() * scale), null);
		
		BufferedImage text = new BufferedImage(GameCanvas.WIDTH, GameCanvas.HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics g2 = text.getGraphics();
		
		g2.setFont(FONT_TITLE);
		g2.setColor(FONT_TITLE_COLOR);
		drawString(g2, 100, title);
		
		g2.setFont(FONT_SUBTITLE);
		g2.setColor(FONT_SUBTITLE_COLOR);
		drawString(g2, 130, subtitle);
		
		for (int i = 0; i < menuItems.size(); i++){
			if (i == curMenuItem){
				g2.setFont(FONT_ITEM_SELECTED);
				g2.setColor(FONT_ITEM_SELECTED_COLOR);
			}else{
				g2.setFont(FONT_ITEM);
				g2.setColor(FONT_ITEM_COLOR);
			}
			drawString(g2, 175 + i * 25, menuItems.get(i));
		}
		g2.dispose();
		g.drawImage(text, 0, 0, (int)(text.getWidth() * scale), (int)(text.getHeight() * scale), null);
	}
	private void drawString(Graphics g, int y, String str){
		g.drawString(str, (int)((GameCanvas.WIDTH - g.getFontMetrics().getStringBounds(str, g).getWidth()) / 2), y);
	}
	
	public void setTitle(String title){}
	public String getTitle(){ return null; }
	
	public void setMenuItems(List<String> menuItems){}
	public List<String> getMenuItems(){ return null; }
}
