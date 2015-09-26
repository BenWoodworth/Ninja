package ninja;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ninja.entity.Entity;
import ninja.state.LevelState;
import ninja.state.StateManager;
import ninja.tile.Tile;

public class Debug {
	public static void drawDebug(Graphics g, double scale, int fps, StateManager stateManager){
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.WHITE);
		g2.setFont(new Font("Arial", Font.PLAIN, 15));

		g2.drawString("Project by:", 10, (int)(GameCanvas.HEIGHT * scale - 27));
		g2.drawString("Ben Woodworth", 10, (int)(GameCanvas.HEIGHT * scale - 12));

		g2.drawString("Game:", 10, 20);
		g2.drawString("FPS: " + fps, 30, 40);
		g2.drawString("TPS: " + fps, 30, 60);
		g2.drawString("Dimensions:", 30, 80);
		g2.drawString("Width=" + (int)(GameCanvas.WIDTH * scale), 50, 100);
		g2.drawString("Height=" + (int)(GameCanvas.HEIGHT * scale), 50, 120);
		g2.drawString("Scale: " + scale, 30, 140);
		g2.drawString("Buffers: " + GameCanvas.BUFFERS, 30, 160);

		if (stateManager.getState() instanceof LevelState){
			LevelState state = (LevelState)stateManager.getState();
			Rectangle region = state.getMap().getRegion();
			if (state.getPlayer() != null){
				g2.drawString("Player:", 140, 20);
				g2.drawString("Tile:", 160, 40);
				g2.drawString("X=" + (int)((state.getPlayer().getLocationX() + state.getPlayer().getHitbox().x) / Tile.TILE_SIZE), 180, 60);
				g2.drawString("Y=" + (int)((state.getPlayer().getLocationY() + state.getPlayer().getHitbox().y) / Tile.TILE_SIZE), 180, 80);
				g2.drawString("Location:", 160, 100);
				g2.drawString("X=" + Math.round(state.getPlayer().getLocationX() * 1000)/1000., 180, 120);
				g2.drawString("Y=" + Math.round(state.getPlayer().getLocationY() * 1000)/1000., 180, 140);
				g2.drawString("Velocity:", 160, 160);
				g2.drawString("X=" + Math.round(state.getPlayer().getVelocityX()*1000)/1000., 180, 180);
				g2.drawString("Y=" + Math.round(state.getPlayer().getVelocityY()*1000)/1000., 180, 200);
				g2.drawString("Direction: " + (state.getPlayer().isFacingRight?"Right":"Left"), 160, 220);
			}
			g2.drawString("Map:", 270, 20);
			g2.drawString("Size:", 290, 40);
			g2.drawString("Width=" + state.getMap().getTileMap().getWidth(), 310, 60);
			g2.drawString("Height=" + state.getMap().getTileMap().getHeight(), 310, 80);
			g2.drawString("Entities: " + state.getMap().getEntities().size(), 290, 100);
			g2.drawString("BackgroundID: " + state.getMap().getBackgroundID(), 290, 120);
			g2.drawString("Region:", 290, 140);
			g2.drawString("X=" + (int)region.getX(), 310, 160);
			g2.drawString("Y=" + (int)region.getY(), 310, 180);
			g2.drawString("Width=" + (int)region.getWidth(), 310, 200);
			g2.drawString("Height=" + (int)region.getHeight(), 310, 220);
			for (Entity e : state.getMap().getEntities()){
				g2.setColor(Color.BLUE);
				g2.drawRect(
					(int)((e.getLocation().x - region.x) * scale),
					(int)((e.getLocation().y - region.y)* scale),
					(int)(e.getLocation().width * scale),
					(int)(e.getLocation().height * scale)
				);
				if (e.getHitbox() != null && region.intersects(new Rectangle((int)(e.getLocationX() + e.getHitbox().x), (int)(e.getLocationY() + e.getHitbox().getY()), (int)(e.getHitbox().getWidth()), (int)(e.getHitbox().getHeight())))){
					g2.setColor(Color.GREEN);
					g2.drawRect(
						(int)((-region.x + e.getHitbox().x + e.getLocationX()) * scale),
						(int)((-region.y + e.getHitbox().y + e.getLocationY()) * scale),
						(int)(e.getHitbox().width * scale),
						(int)(e.getHitbox().height * scale)
					);
				}
			}
		}

	}
}
