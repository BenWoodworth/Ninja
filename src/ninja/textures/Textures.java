package ninja.textures;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import ninja.ResourceFetch;

public class Textures {
	private static boolean loaded = false;
	
	public static final int BLOCK_TEXTURE_SIZE = 16;
	public static ArrayList<BufferedImage> blockTextures;

	public static final int PLAYER_TEXTURE_SIZE = 16;
	public static ArrayList<BufferedImage> playerTextures;
	
	public static final int BACKGROUND_TEXTURE_WIDTH = 420;
	public static final int BACKGROUND_TEXTURE_HEIGHT = 320;
	public static ArrayList<BufferedImage> backgroundTextures;
	
	public static final int ENTITY_TEXTURE_SIZE = 16;
	public static ArrayList<BufferedImage> entityTextures;

	
	private static BufferedImage copyImage(BufferedImage image){
		BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		result.getGraphics().drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
		return result;
	}
	public static BufferedImage getBlockTexture(int id){
		if (!loaded) loadTextures();
		return id==-1?null:copyImage(blockTextures.get(id));
	}
	public static BufferedImage getPlayerTexture(int id){
		if (!loaded) loadTextures();
		return id==-1?null:copyImage(playerTextures.get(id));
	}
	public static BufferedImage getBackgroundTexture(int id){
		if (!loaded) loadTextures();
		return id==-1?null:copyImage(backgroundTextures.get(id));
	}
	public static BufferedImage getEntityTexture(int id){
		if (!loaded) loadTextures();
		return id==-1?null:copyImage(entityTextures.get(id));
	}
	public static BufferedImage getScaledImage(BufferedImage image, double scale){
		BufferedImage result = new BufferedImage((int)(image.getWidth() * scale), (int)(image.getHeight() * scale), image.getType());
		result.getGraphics().drawImage(image, 0, 0, result.getWidth(), result.getHeight(), null);
		return result;
	}


	public static void loadTextures(){
		try {
			blockTextures = new ArrayList<BufferedImage>();
			InputStream is = ResourceFetch.fetch("/textures/Blocks.png");
			BufferedImage blockTexture = ImageIO.read(is);
			for (int y = 0; y < blockTexture.getHeight(); y += BLOCK_TEXTURE_SIZE){
				for (int x = 0; x < blockTexture.getWidth(); x += BLOCK_TEXTURE_SIZE){
					blockTextures.add(blockTexture.getSubimage(x, y, BLOCK_TEXTURE_SIZE, BLOCK_TEXTURE_SIZE));
				}
			}
			is.close();

			playerTextures = new ArrayList<BufferedImage>();
			is = ResourceFetch.fetch("/textures/Player.png");
			BufferedImage playerTexture = ImageIO.read(is);
			for (int y = 0; y < playerTexture.getHeight(); y += PLAYER_TEXTURE_SIZE){
				for (int x = 0; x < playerTexture.getWidth(); x += PLAYER_TEXTURE_SIZE){
					playerTextures.add(playerTexture.getSubimage(x, y, PLAYER_TEXTURE_SIZE, PLAYER_TEXTURE_SIZE));
				}
			}
			is.close();

			backgroundTextures = new ArrayList<BufferedImage>();
			is = ResourceFetch.fetch("/textures/Backgrounds.png");
			BufferedImage backgroundTexture = ImageIO.read(is);
			for (int y = 0; y < backgroundTexture.getHeight(); y += BACKGROUND_TEXTURE_HEIGHT){
				for (int x = 0; x < backgroundTexture.getWidth(); x += BACKGROUND_TEXTURE_WIDTH){
					backgroundTextures.add(backgroundTexture.getSubimage(x, y, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT));
				}
			}
			is.close();

			entityTextures = new ArrayList<BufferedImage>();
			is = ResourceFetch.fetch("/textures/Entities.png");
			BufferedImage entityTexture = ImageIO.read(is);
			for (int y = 0; y < entityTexture.getHeight(); y += ENTITY_TEXTURE_SIZE){
				for (int x = 0; x < entityTexture.getWidth(); x += ENTITY_TEXTURE_SIZE){
					entityTextures.add(entityTexture.getSubimage(x, y, ENTITY_TEXTURE_SIZE, ENTITY_TEXTURE_SIZE));
				}
			}
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		loaded = true;
	}
}
