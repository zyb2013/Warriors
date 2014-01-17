package com.yayo.warriors.module.dungeon.types;

/**
 * 副本类型
 * @author liuyuhua
 */
public interface DungeonType {
	
	/***
	 * 0 闯关  类型
	 */
	int BARGED = 0;
	
	/**
	 * 1 回合(每一波怪死亡,在出一波新的)
	 */
	int ROUND = 1;
	
	/**
	 * 2 回合(固定间隔时间出怪)
	 */
	int ROUND_INTIME = 2;
	
	/**
	 * 3 千层塔  类型
	 */
	int LAYER_TOWER = 3;
	
	/**
	 * 4 塔防
	 */
	int TOWER_DEFENSE = 4;
	
	/**
	 * 5 高富帅
	 */
	int HIGH_RICH = 5;
	
	/**
	 * 6 藏宝图
	 */
	int TREASURE = 6;

}
