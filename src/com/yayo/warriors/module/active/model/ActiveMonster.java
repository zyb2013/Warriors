package com.yayo.warriors.module.active.model;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * 活动怪物信息
 * 
 * @author huachaoping
 */
public class ActiveMonster implements Serializable {

	private static final long serialVersionUID = -4719716128628972121L;

	/** 怪物自增Id */
	private long autoId;
	
	/** 怪物终结者 */
	private String monsterKiller;
	
	
	/**
	 * 构造怪物信息
	 * @param autoId
	 * @return {@link ActiveMonster}
	 */
	public static ActiveMonster valueOf(long autoId) {
		ActiveMonster activeMonster = new ActiveMonster();
		activeMonster.autoId = autoId;
		return activeMonster;
	}

	
	public long getAutoId() {
		return autoId;
	}

	public void setAutoId(long autoId) {
		this.autoId = autoId;
	}
	
	public synchronized String getMonsterKiller() {
		if (monsterKiller == null) {
			return StringUtils.EMPTY;
		}
		return monsterKiller;
	}

	public void setMonsterKiller(String monsterKiller) {
		this.monsterKiller = monsterKiller;
	}
	
}
