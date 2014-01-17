package com.yayo.warriors.basedb.model;

/**
 * 副本地图坐标系解析
 * 小工具
 * @author liuyuhua
 */
public class DungeonPoint {
	
	/** 地图的ID*/
	private int mapId;
	
	/** 地图X轴坐标*/
	private int x;
	
	/** 地图Y轴坐标*/
	private int y;
	
	/**
	 * 构造方法
	 * @param mapId   地图ID
	 * @param x       地图X轴坐标
	 * @param y       地图Y轴坐标
	 * @return {@link DungeonPoint}
	 */
	public static DungeonPoint valueOf(int mapId,int x,int y){
		DungeonPoint dungeonPoint = new DungeonPoint();
		dungeonPoint.mapId = mapId;
		dungeonPoint.x = x;
		dungeonPoint.y = y;
		return dungeonPoint;
	} 

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

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

	@Override
	public String toString() {
		return "DungeonPoint [mapId=" + mapId + ", x=" + x + ", y=" + y + "]";
	}

}
