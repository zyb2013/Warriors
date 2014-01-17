package com.yayo.warriors.module.user.type;

/**
 * 角色刷新状态
 * 
 * @author Hyint
 */
public interface Flushable {
	
	/** 不需要刷新角色属性 */
	int FLUSHABLE_NOT = 0;
	
	/** 需要刷新角色属性 */
	int FLUSHABLE_NORMAL = 1;
	
	/** 需要刷新角色属性, 并加满HP/MP */
	int FLUSHABLE_LEVEL_UP = 2;
}
