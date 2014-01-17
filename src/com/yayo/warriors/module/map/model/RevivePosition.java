package com.yayo.warriors.module.map.model;

/**
 * 复活点模型
 * @author liuyuhua
 */
public class RevivePosition {
	
	/** 阵营*/
	private int camp;
	
	/** 复活到地图*/
	private int mapId;

	/** 复活的坐标点 X*/
	private int x;
	
	/** 复活的坐标点 Y*/
	private int y;
	
	/**
	 * 构造方法
	 * @param camp   阵营
	 * @param mapId  地图ID
	 * @param x      x坐标
	 * @param y      y坐标
	 * @return {@link RevivePosition} 复活点对象
	 */
	public static RevivePosition valueOf(int camp,int mapId,int x,int y){
		RevivePosition revivePosition = new RevivePosition();
		revivePosition.camp = camp;
		revivePosition.mapId = mapId;
		revivePosition.x = x;
		revivePosition.y = y;
		return revivePosition;
	}

	public int getCamp() {
		return camp;
	}

	public void setCamp(int camp) {
		this.camp = camp;
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
		return "RevivePosition [camp=" + camp + ", mapId=" + mapId + ", x=" + x
				+ ", y=" + y + "]";
	}
}
