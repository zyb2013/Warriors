package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/**
 * 怪物刷新活动配置
 * 
 * @author huachaoping
 */
@Resource
public class ActiveBossConfig {
	
	/** 主键 */
	@Id
	private int id;
	
	/** 怪物基础Id */
	@Index(name = IndexName.ACTIVE_MONSTERID)
	private int monsterId;
	
	/** 怪物所在地图Id */
	private int mapId;

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMonsterId() {
		return monsterId;
	}

	public void setMonsterId(int monsterId) {
		this.monsterId = monsterId;
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}
	
	
}
