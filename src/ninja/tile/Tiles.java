package ninja.tile;


public enum Tiles {
	AIR(0), WALL(1);

	private int tile;
	Tiles(int tile){
		this.tile = tile;
	}
	public int getTileID(){ return tile; }

	public static Tile newTile(Tiles tileID, TileMap tileMap, int x, int y){
		switch (tileID){
		case AIR:
			return null;
		case WALL:
			return new WallTile(tileMap, x, y);
		default:
			return null;
		}
	}
	public static Tile newTile(int tileID, TileMap tileMap, int x, int y){
		switch (tileID){
		case 0:
			return newTile(AIR, tileMap, x, y);
		case 1:
			return newTile(WALL, tileMap, x, y);
		default:
			return null;
		}
	}
	
	public String toString(){
		switch (this){
		case AIR:	return "Air";
		case WALL:	return "Wall";
		default:	return null;
		}
	}
	
	public int value(){
		return tile;
	}
}
