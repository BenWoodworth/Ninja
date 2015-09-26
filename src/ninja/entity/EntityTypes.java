package ninja.entity;


public enum EntityTypes {
	PLAYER(0), LASER(2), TURRET(3), SPIKES(4);

	private int id;
	private EntityTypes(int id){ this.id = id; }
	public int getEntityID(){ return id; }

	@Override
	public String toString(){
		switch (this){
		case LASER:			return "Laser";
		case PLAYER:		return "Player";
		case TURRET:		return "Turret";
		case SPIKES:		return "Spikes";
		default:			return null;
		}
	}
	
	public String getTypeStr(){
		switch (this){
		case LASER:			return "EntityLaser";
		case PLAYER:		return "EntityPlayer";
		case TURRET:		return "EntityTurret";
		case SPIKES:		return "EntitySpikes";
		default:			return null;
		}
	}
}
