package ninja.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ninja.map.GameMap;

public class EntityUtil {
	public static void getProperties(List<String> setProperties, List<Class<?>> setTypes, EntityTypes type){
		switch (type){
		case LASER:
			setProperties.add("x"); setTypes.add(Double.class);
			setProperties.add("y"); setTypes.add(Double.class);
			setProperties.add("rotation"); setTypes.add(Integer.class);
			setProperties.add("onTicks"); setTypes.add(Integer.class);
			setProperties.add("offTicks"); setTypes.add(Integer.class);
			setProperties.add("curTicks"); setTypes.add(Integer.class);
			setProperties.add("laserOn"); setTypes.add(Boolean.class);
			break;
		case PLAYER:
			setProperties.add("x"); setTypes.add(Double.class);
			setProperties.add("y"); setTypes.add(Double.class);
			break;
		case TURRET:
			setProperties.add("x"); setTypes.add(Double.class);
			setProperties.add("y"); setTypes.add(Double.class);
			setProperties.add("rotation"); setTypes.add(Integer.class);
			setProperties.add("fireRange"); setTypes.add(Double.class);
			setProperties.add("fireTicks"); setTypes.add(Integer.class);
			break;
		case SPIKES:
			setProperties.add("x"); setTypes.add(Double.class);
			setProperties.add("y"); setTypes.add(Double.class);
			setProperties.add("rotation"); setTypes.add(Integer.class);
			break;
		}
	}
	public static List<Object> getValues(Entity e){
		List<Object> result = new ArrayList<Object>();
		if (e instanceof EntityLaser){
			result.add(((EntityLaser)e).getLocationX());
			result.add(((EntityLaser)e).getLocationY());
			result.add(((EntityLaser)e).getRotation());
			result.add(((EntityLaser)e).getLaserOnTicks());
			result.add(((EntityLaser)e).getLaserOffTicks());
			result.add(((EntityLaser)e).getCurTicks());
			result.add(((EntityLaser)e).isLaserOn());
		}else if (e instanceof EntityPlayer){
			result.add(((EntityPlayer)e).getLocationX());
			result.add(((EntityPlayer)e).getLocationY());
		}else if (e instanceof EntityTurret){
			result.add(((EntityTurret)e).getLocationX());
			result.add(((EntityTurret)e).getLocationY());
			result.add(((EntityTurret)e).getRotation());
			result.add(((EntityTurret)e).getFireRange());
			result.add(((EntityTurret)e).getFireRate());
		}else if (e instanceof EntitySpikes){
			result.add(((EntitySpikes)e).getLocationX());
			result.add(((EntitySpikes)e).getLocationY());
			result.add(((EntitySpikes)e).getRotation());
		}
		return result;
	}
	public static void setValues(Entity e, List<String> properties, List<Object> values){
		for (int i = 0; i < properties.size(); i++){
			String p = properties.get(i);
			Object v = values.get(i);
			if (e instanceof EntityLaser){
				if (p.equals("x")) ((EntityLaser)e).setLocationX((double)v);
				if (p.equals("y")) ((EntityLaser)e).setLocationY((double)v);
				if (p.equals("rotation")) ((EntityLaser)e).setRotation((int)v);
				if (p.equals("onTicks")) ((EntityLaser)e).setLaserOnTicks((int)v);
				if (p.equals("offTicks")) ((EntityLaser)e).setLaserOffTicks((int)v);
				if (p.equals("curTicks")) ((EntityLaser)e).setCurTicks((int)v);
				if (p.equals("laserOn")) ((EntityLaser)e).setLaserOn((boolean)v);
			}else if (e instanceof EntityPlayer){
				if (p.equals("x")) ((EntityPlayer)e).setLocationX((double)v);
				if (p.equals("y")) ((EntityPlayer)e).setLocationY((double)v);
			}else if (e instanceof EntityTurret){
				if (p.equals("x")) ((EntityTurret)e).setLocationX((double)v);
				if (p.equals("y")) ((EntityTurret)e).setLocationY((double)v);
				if (p.equals("rotation")) ((EntityTurret)e).setRotation((int)v);
				if (p.equals("fireRange")) ((EntityTurret)e).setFireRange((double)v);
				if (p.equals("fireTicks")) ((EntityTurret)e).setFireRate((int)v);
			}else if (e instanceof EntitySpikes){
				if (p.equals("x")) ((EntitySpikes)e).setLocationX((double)v);
				if (p.equals("y")) ((EntitySpikes)e).setLocationY((double)v);
				if (p.equals("rotation")) ((EntitySpikes)e).setRotation((int)v);
			}
		}
	}
	
	public static Entity fromString(GameMap map, String str){
		HashMap<String, String> vars = new HashMap<String, String>();
		for (String s : str.replaceAll("\\s+","").split("\\|")){
			String[] split = s.split("=");
			if (split.length == 2) vars.put(split[0], split[1]);
		}
		if(vars.get("type") == null) return null;
		if(vars.get("type").equals("EntityPlayer")){
			double x = Double.parseDouble(vars.get("x"));
			double y = Double.parseDouble(vars.get("y"));
			return new EntityPlayer(map, x, y);
		}else if (vars.get("type").equals("EntityLaser")){
			double x = Double.parseDouble(vars.get("x"));
			double y = Double.parseDouble(vars.get("y"));
			int rotation = Integer.parseInt(vars.get("rotation"));
			int onTicks = Integer.parseInt(vars.get("onTicks"));
			int offTicks = Integer.parseInt(vars.get("offTicks"));
			int curTicks = Integer.parseInt(vars.get("curTicks"));
			boolean laserOn = Boolean.parseBoolean(vars.get("laserOn"));
			return new EntityLaser(map, x, y, rotation, onTicks, offTicks, curTicks, laserOn);
		}else if (vars.get("type").equals("EntityTurret")){
			double x = Double.parseDouble(vars.get("x"));
			double y = Double.parseDouble(vars.get("y"));
			int rotation = Integer.parseInt(vars.get("rotation"));
			double fireRange = Double.parseDouble(vars.get("fireRange"));
			int fireTicks = Integer.parseInt(vars.get("fireTicks"));
			return new EntityTurret(map, x, y, rotation, fireTicks, fireRange);
		}else if (vars.get("type").equals("EntitySpikes")){
			double x = Double.parseDouble(vars.get("x"));
			double y = Double.parseDouble(vars.get("y"));
			int rotation = Integer.parseInt(vars.get("rotation"));
			return new EntitySpikes(map, x, y, rotation);
		}
		return null;
	}
	
	public static String toFileString(Entity e){
		List<String> properties = new ArrayList<String>();
		List<Class<?>> types = new ArrayList<Class<?>>();
		getProperties(properties, types, e.getType());
		List<Object> values = getValues(e);
		String result = "type=" + e.getType().getTypeStr();
		for (int i = 0; i < properties.size(); i++){
			result += "|" + properties.get(i) + "=" + values.get(i);
		}
		return result;
	}
	
	public static Entity fromType(GameMap map, EntityTypes type){
		if (type == EntityTypes.LASER){
			return new EntityLaser(map, 0, 0, 0, 0, 0, 0, false);
		}else if (type == EntityTypes.PLAYER){
			return new EntityPlayer(map, 0, 0);
		}else if (type == EntityTypes.TURRET) {
			return new EntityTurret(map, 0, 0, 0, 1, 0);
		}else if (type == EntityTypes.SPIKES){
			return new EntitySpikes(map, 0, 0, 0);
		}
		return null;
	}
}
