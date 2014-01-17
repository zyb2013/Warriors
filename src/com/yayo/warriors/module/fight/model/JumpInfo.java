package com.yayo.warriors.module.fight.model;

/**
 * 跳跃信息
 * 
 * @author Hyint
 */
public class JumpInfo {
	
	/** 起跳点的地图ID */
	private int mapId;

	/** 起跳时的X据点 */
	private int sourceX;

	/** 起跳时的Y据点 */
	private int sourceY;
	
	/** 跳起来的目的地X坐标 */
	private int targetX;

	/** 跳起来的目的地Y坐标 */
	private int targetY;

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public int getSourceX() {
		return sourceX;
	}

	public void setSourceX(int sourceX) {
		this.sourceX = sourceX;
	}

	public int getSourceY() {
		return sourceY;
	}

	public void setSourceY(int sourceY) {
		this.sourceY = sourceY;
	}

	public int getTargetX() {
		return targetX;
	}

	public void setTargetX(int targetX) {
		this.targetX = targetX;
	}

	public int getTargetY() {
		return targetY;
	}

	public void setTargetY(int targetY) {
		this.targetY = targetY;
	}
	
	public static JumpInfo valueOf(int mapId, int sourceX, int sourceY, int targetX, int targetY) {
		JumpInfo jumpInfo = new JumpInfo();
		jumpInfo.mapId = mapId;
		jumpInfo.targetX = targetX;
		jumpInfo.targetY = targetY;
		jumpInfo.sourceX = sourceX;
		jumpInfo.sourceY = sourceY;
		return jumpInfo;
	}
}
