package com.yayo.warriors.module.props.type;

/**
 * 使用作用目标
 * 
 * @author Hyint
 */
public interface UseTarget {

	/** -1-无使用对象 */
	int NO_TARGET = -1;
	
	/** 0-公用物品信息, 都可以使用 */
	int COMMON_TARGET = 0;
	
	/** 1-角色使用 */
	int PLAYER_TARGET = 1;
	
	/** 2-角色使用 */
	int ANIMAL_TARGET = 2;
	
}
