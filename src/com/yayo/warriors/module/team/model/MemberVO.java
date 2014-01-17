package com.yayo.warriors.module.team.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 队伍中的成员信息
 * 
 * @author Hyint
 */
public class MemberVO implements Serializable {
	private static final long serialVersionUID = -1406856873896648060L;
	
	/** 角色ID */
	private long playerId;
	
	/** 地图ID */
	private int mapId;
	
	/** 所在地图的 X 坐标 */
	private int x;
	
	/** 所在地图的 Y 坐标 */
	private int y;
	
	/** 成员在线状态 */
	private boolean online;
	
	/** 成员的属性Key */
	private Object[] attributes;
	
	/** 成员的属性值 */
	private Object[] values;

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public Object[] getAttributes() {
		return attributes;
	}

	public void setAttributes(Object[] attributes) {
		this.attributes = attributes;
	}

	public Object[] getValues() {
		return values;
	}

	public void setValues(Object[] values) {
		this.values = values;
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

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}
	
	@Override
	public String toString() {
		return "MemberVO [playerId=" + playerId + ", mapId=" + mapId + ", x=" + x + ", y=" + y 
				+ ", attributes=" + Arrays.toString(attributes) + ", values=" + Arrays.toString(values) + "]";
	}

	
}
