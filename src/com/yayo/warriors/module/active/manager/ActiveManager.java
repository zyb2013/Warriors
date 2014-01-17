package com.yayo.warriors.module.active.manager;

import com.yayo.warriors.module.monster.model.Monster;

/**
 * 活动管理
 * 
 * @author huachaoping
 */
public interface ActiveManager {
	
	
	/**
	 * 缓存BOSS
	 * 
	 * @param monster
	 */
	void put2Monsters(int branching, Monster monster);
	
	
	/**
	 * 获得怪物的自增ID
	 * 
	 * @param MonsterId
	 * @return
	 */
	long getMonsterAutoId(int branching, int monsterId);
	
	
	/**
	 * 获得怪物击杀者
	 * 
	 * @param monsterId
	 * @return
	 */
	String getMonsterKiller(int branching, int monsterId);
	
	
	/**
	 * 移除怪物
	 * 
	 * @param monsterId
	 */
	void removeMonster(int monsterId);
	
	
	/**
	 * 更改怪物击杀者
	 * 
	 * @param monsterId
	 * @param playerName
	 */
	public void modifyMonsterKiller(int branching, int monsterId, String playerName);
}
