package com.yayo.warriors.module.dungeon.model;

import com.yayo.common.utility.Splitable;

/**
 * 副本信息
 * @author liuyuhua
 */
public class DungeonInfo {
	
	/** 副本基础ID*/
	private int baseId;
	
	/** 进入次数*/
	private int times;
	
	/** 最后一次进入时间*/
	private long date;
	
	/**
	 * 构造方法
	 * @param dungeonBaseId   副本原型ID
	 * @param times       次数   
	 * @param date        最后一次进入时间   
	 * @return
	 */
	public static DungeonInfo valueOf(int dungeonBaseId,int times,long date){
		DungeonInfo info = new DungeonInfo();
		info.baseId = dungeonBaseId;
		info.times = times;
		info.date = date;
		return info;
	}
	
	/**
	 * 增加进入次数
	 */
	public void addHisTimes(){
		this.times += 1;
		this.date = System.currentTimeMillis();
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int dungeonBaseId) {
		this.baseId = dungeonBaseId;
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}
	
	public String toString(){
		return baseId + Splitable.ATTRIBUTE_SPLIT + times + Splitable.ATTRIBUTE_SPLIT + date;
	}
}
