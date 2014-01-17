package com.yayo.warriors.socket.vo;

import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.ISpire;

/**
 * 转场信息
 * @author liuyuhua
 *
 */
public class ChangeScreenVo {
	
	/** 转送到的地图ID*/
	private int mapId;
	
	/** 转送到X点*/
	private int x;
	
	/** 转送到Y点*/
	private int y;
	
	/** 副本增量ID*/
	private long dungeonId;
	
	/** 副本基础ID*/
	private int dungeonBaseId;
	
	/** 目标编号*/
	private UnitId unitId ;
	
	/**
	 * 构造方法
	 * @param mapId  地图ID
	 * @param x      地图X轴坐标
	 * @param y      地图Y轴坐标
	 * @param targetSpire 
	 * @return {@link ChangeScreenVo}
	 */
	public static ChangeScreenVo valueOf(int mapId,int x,int y, ISpire targetSpire){
		ChangeScreenVo vo = new ChangeScreenVo();
		vo.mapId = mapId;
		vo.x = x;
		vo.y = y;
		vo.unitId = targetSpire.getUnitId();
		return vo;
	}

	/**
	 * 副本转场构造方法
	 * @param mapId         地图ID
	 * @param x             地图X轴坐标
	 * @param y             地图Y轴坐标
	 * @param dungeonBaseId 副本基础ID
	 * @param dungeonId     副本自增量ID
	 * @return {@link ChangeScreenVo}
	 */
	public static ChangeScreenVo dungeonOf(int mapId, int x, int y, int dungeonBaseId, long dungeonId){
		ChangeScreenVo vo = ChangeScreenVo.valueOf(mapId, x, y);
		vo.setDungeonBaseId(dungeonBaseId);
		vo.setDungeonId(dungeonId);
		return vo;
	}
	
	//Getter and Setter...
	
	public static ChangeScreenVo valueOf(int mapId, int x, int y) {
		ChangeScreenVo vo = new ChangeScreenVo();
		vo.mapId = mapId;
		vo.x = x;
		vo.y = y;
		return vo;
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

	public int getDungeonBaseId() {
		return dungeonBaseId;
	}

	public void setDungeonBaseId(int dungeonBaseId) {
		this.dungeonBaseId = dungeonBaseId;
	}

	public long getDungeonId() {
		return dungeonId;
	}

	public void setDungeonId(long dungeonId) {
		this.dungeonId = dungeonId;
	}
	
	public UnitId getUnitId() {
		return unitId;
	}

	public void setUnitId(UnitId unitId) {
		this.unitId = unitId;
	}

	@Override
	public String toString() {
		return "ChangeScreenVo [mapId=" + mapId + ", x=" + x + ", y=" + y
				+ ", dungeonId=" + dungeonId + ", dungeonBaseId="
				+ dungeonBaseId + "]";
	}
}
