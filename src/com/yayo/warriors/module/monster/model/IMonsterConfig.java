package com.yayo.warriors.module.monster.model;

/**
 *	怪物对象接口
 */
public interface IMonsterConfig {

	/**
	 * 怪物战斗基础数据id
	 * @return
	 */
	int getMonsterFightId();

	/**
	 * 怪物初始x坐标
	 * @return
	 */
	int getBornX();

	/**
	 * 怪物初始y坐标
	 * @return
	 */
	int getBornY();

	/**
	 * 怪物的基础id
	 * @return
	 */
	int getId();
}
