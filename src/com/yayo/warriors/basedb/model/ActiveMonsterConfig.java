package com.yayo.warriors.basedb.model;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.type.IndexName;

/**
 * 活动 怪物玩法
 * @author liuyuhua
 */
@Resource
public class ActiveMonsterConfig {
	
	@Id
	private int id;
	
	/** 活动类型 经验怪类型:1,围城怪物类型:2*/
	@Index(name=IndexName.ACTIVE_MONSTER_RULE, order = 0)
	private int activeType;
	
	/** 刷新的地图*/
	private String refuMap;
	
	/** 坐标x*/
	private int x;
	
	/** 坐标y*/
	private int y;
	
	/** 刷新间隔时间*/
	private int refuTime;
	
	/** 怪物波数*/
	@Index(name=IndexName.ACTIVE_MONSTER_RULE, order = 1)
	private int round;

	/** 怪物ID*/
	private int monsterId;
	
	/** 刷新数量*/
	private int refuCount;
	
	/** 地图*/
	@JsonIgnore
	private transient volatile int[] maps = null;

	/**
	 * 获取随机出现的地图 ID
	 * @return 地图ID
	 */
	public int randomMapId(){
		if(maps != null){
			return maps[Tools.getRandomInteger(maps.length)];
		}
		
		synchronized (this) {
			if(maps != null){
				return maps[Tools.getRandomInteger(maps.length)];
			}
			
			if(this.refuMap == null || this.refuMap.isEmpty()){
				maps = new int[0];
			}
			
			String[] split = this.refuMap.split(Splitable.ELEMENT_SPLIT);
			maps = new int[split.length];
			for(int i = 0 ; i < split.length ; i++){
				maps[i] = Integer.parseInt(split[i]);
			}
			
			return maps[Tools.getRandomInteger(maps.length)];
		}
	}
	
	//Getter and Setter...
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getActiveType() {
		return activeType;
	}

	public void setActiveType(int activeType) {
		this.activeType = activeType;
	}

	public String getRefuMap() {
		return refuMap;
	}

	public void setRefuMap(String refuMap) {
		this.refuMap = refuMap;
	}

	public int getRefuTime() {
		return refuTime;
	}

	public void setRefuTime(int refuTime) {
		this.refuTime = refuTime;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public int getMonsterId() {
		return monsterId;
	}

	public void setMonsterId(int monsterId) {
		this.monsterId = monsterId;
	}

	public int getRefuCount() {
		return refuCount;
	}

	public void setRefuCount(int refuCount) {
		this.refuCount = refuCount;
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
		return "ActiveMonsterConfig [id=" + id + ", activeType=" + activeType
				+ ", refuMap=" + refuMap + ", x=" + x + ", y=" + y
				+ ", refuTime=" + refuTime + ", round=" + round
				+ ", monsterId=" + monsterId + ", refuCount=" + refuCount + "]";
	}
}
