package ninja.tile;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import ninja.textures.Textures;


public class WallTile extends Tile {
	public static final float BORDER_OPACITY = .60F;

	public static HashMap<Integer, BufferedImage> textures = new HashMap<Integer, BufferedImage>();

	public WallTile(TileMap tileMap, int x, int y) {
		super(tileMap, x, y, Tiles.WALL.getTileID(), Tile.TYPE_SOLID);
	}
	
	@Override
	public BufferedImage getImage(){
		int borderTextureID = 0;
		boolean ul = false;
		boolean ur = false;
		boolean dl = false;
		boolean dr = false;

		if (!equals(getTileU())) 
			borderTextureID += 1;
		if (!equals(getTileR())) 
			borderTextureID += 2;
		if (!equals(getTileD())) 
			borderTextureID += 4;
		if (!equals(getTileL())) 
			borderTextureID += 8;
		ul = (!equals(getTileUL()) && equals(getTileU()) && equals(getTileL()));
		ur = (!equals(getTileUR()) && equals(getTileU()) && equals(getTileR()));
		dl = (!equals(getTileDL()) && equals(getTileD()) && equals(getTileL()));
		dr = (!equals(getTileDR()) && equals(getTileD()) && equals(getTileR()));
		int textureID = borderTextureID + (ul?16:0) + (ur?32:0) + (dr?64:0) + (dl?128:0);

		BufferedImage result = textures.get(textureID);
		
		if (result != null)
			return result;

		result = Textures.getBlockTexture(0);
		Graphics2D g = (Graphics2D)result.getGraphics();
		int size = Textures.BLOCK_TEXTURE_SIZE;
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, BORDER_OPACITY));
		
		if (borderTextureID != 0) g.drawImage(Textures.getBlockTexture(borderTextureID), 0, 0, size, size, null);
		if (ul) g.drawImage(Textures.getBlockTexture(16), 0, 0, size, size, null);
		if (ur) g.drawImage(Textures.getBlockTexture(17), 0, 0, size, size, null);
		if (dr) g.drawImage(Textures.getBlockTexture(18), 0, 0, size, size, null);
		if (dl) g.drawImage(Textures.getBlockTexture(19), 0, 0, size, size, null);
		g.dispose();
		
		textures.put(textureID, result);
		return result;
	}
}
