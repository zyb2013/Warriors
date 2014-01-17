package com.yayo.warriors.module.treasure.model;

import java.io.Serializable;


import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.treasure.entity.UserTreasure;

/**
 * 宝藏
 * 
 * @author jonsai
 */
public class Treasure implements Serializable{
	private static final long serialVersionUID = 8398955926215211626L;
	
	/** 宝藏用户道具id */
	private long userPropsId;
	
	/** 目标地图id >0表示有目标坐标 */
	private int mapId;
	
	/** 坐标点 */
	private int x;
	
	/** 坐标点 */
	private int y;
	
	/** 品质 */
	private int quality;
	
	//---------------------------------------
	/**
	 * 构造一个对象
	 * @param userTreasure
	 * @param userProps
	 * @return
	 */
	public static Treasure valueOf(UserTreasure userTreasure, UserProps userProps){
		Treasure treasure = new Treasure();
		treasure.userPropsId = userTreasure.getUserPropsId(); 
		treasure.mapId = userTreasure.getMapId();
		treasure.x = userTreasure.getX();
		treasure.y = userTreasure.getY();
		treasure.quality = userProps.getQuality().ordinal();
		return treasure;
	}
	
	/**
	 * 构造一个对象
	 * @param userPropsId
	 * @param mapId
	 * @param x
	 * @param y
	 * @return
	 */
	public static Treasure valueOf(long userPropsId, int mapId, int x, int y){
		Treasure treasure = new Treasure();
		treasure.setUserPropsId(userPropsId);
		treasure.setMapId(mapId);
		treasure.setX(x);
		treasure.setY(y);
		return treasure;
	}

	public long getUserPropsId() {
		return userPropsId;
	}

	public void setUserPropsId(long userPropsId) {
		this.userPropsId = userPropsId;
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

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}
	
}
