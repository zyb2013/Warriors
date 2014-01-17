package com.yayo.warriors.module.dungeon.types;

/**
 * 副本难度类型
 * <per>这个类型直接关系到副本进入的人数</per> 
 * @author liuyuhua
 */
public interface DifficultyType {
	
	/** 普通副本*/
	int SINGLE = 0;
	
	/** 剧情副本*/
	int STORY  = 1;

}
