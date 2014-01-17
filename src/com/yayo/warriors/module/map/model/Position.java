package com.yayo.warriors.module.map.model;

/**
 * 据点坐标对象
 * 
 * @author Hyint
 */
public class Position {

	/**
	 * 地图ID
	 */
	private int mapId;
	
	/** X坐标点 */
	private int x = 0;

	/** Y坐标点 */
	private int y = 0;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	/**
	 * 据点坐标对象
	 * 
	 * @param  x				X坐标点
	 * @param  y				Y坐标点
	 * @return {@link Position}	坐标点
	 */
	public static Position valueOf(int mapId, int x, int y) {
		Position position = new Position();
		position.x = x;
		position.y = y;
		position.mapId = mapId;
		return position;
	}

	/**
	 * 据点坐标对象
	 * 
	 * @param  x				X坐标点
	 * @param  y				Y坐标点
	 * @return {@link Position}	坐标点
	 */
	public static Position valueOf(int x, int y) {
		Position point = new Position();
		point.x = x;
		point.y = y;
		return point;
	}

}
