package com.yayo.warriors.module.dungeon.model;

import com.yayo.common.utility.Splitable;

/**
 * 剧情副本信息 
 * @author liuyuhua
 */
public class StoryInfo {
	
	/** 副本基础ID*/
	private int baseId;
	
	/** 状态*/
	private int state;
	
	/**
	 * 构造方法
	 * @param baseId    副本基础ID
	 * @param state     状态
	 * @return {@link StoryInfo} 剧情副本信息对象
	 */
	public static StoryInfo valueOf(int baseId,int state){
		StoryInfo info = new StoryInfo();
		info.baseId = baseId;
		info.state  = state;
		return info;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return baseId + Splitable.ATTRIBUTE_SPLIT + state;
	}

}
