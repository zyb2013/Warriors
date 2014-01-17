package com.yayo.warriors.module.active.vo;

import java.io.Serializable;

/**
 * 每日副本活动VO
 * 
 * @author huachaoping
 */
public class ActiveDungeonVO implements Serializable{

	private static final long serialVersionUID = -4099758587289590920L;
	
	/** 活动Id */
	private int id;
	
	/** 副本Id */
	private int dungeonId;
	
	/** 已进入副本次数 */
	private int dungeonCount;
	

	public static ActiveDungeonVO valueOf(int id, int dungeonId, int dungeonCount) {
		ActiveDungeonVO vo = new ActiveDungeonVO();
		vo.id = id;
		vo.dungeonId = dungeonId;
		vo.dungeonCount = dungeonCount;
		return vo;
	}
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDungeonId() {
		return dungeonId;
	}

	public void setDungeonId(int dungeonId) {
		this.dungeonId = dungeonId;
	}

	public int getDungeonCount() {
		return dungeonCount;
	}

	public void setDungeonCount(int dungeonCount) {
		this.dungeonCount = dungeonCount;
	}
	
	
}
