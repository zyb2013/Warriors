package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;

/**
 * 每日副本活动配置
 * 
 * @author huachaoping
 */
@Resource
public class ActiveDungeonConfig {

	/** 主键 */
	@Id
	private int id;
	
	/** 副本基础Id */
	private int dungeonId;
	

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
	
}
