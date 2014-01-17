package com.yayo.warriors.module.active.manager;

/**
 * 在线活动,怪物活动玩法 
 * @author liuyuhua
 */
public interface ActiveMonsterManager {
	
	/**
	 * 创建经验怪物玩法
	 */
	void activeMonsterExpRule();
	
	/**
	 * 创建围城玩法
	 */
	void activeMonsterWrapRule();
	
	/**
	 * 清理围城玩法怪物
	 */
	void clearMonsterWrapRule();
}
