package com.yayo.warriors.module.monster.type;

/**
 *  怪物的区域
 *  
 * @author Hyint
 */
public interface Classification {

	/** 未知 */
	int NONE = 0;
	
	/** 1 普通怪 */
	int NORMAL = 1;
	
	/** 2 副本怪 */
	int DUNGEON = 2;
	
	/** 3阵营怪 */
	int CAMP = 3;
}
