package com.yayo.warriors.module.monster.type;

/**
 * 怪物类型
 * @author jonsai
 *
 */
public enum MonsterType {
	
	/** 1 - 普通怪 */
	NORMAL(1),
	
	/** 2 - BOSS */
	BOSS(2),
	
	/** 3 - 精英怪 */
	ELITE(3);
	
	private int value;
	
	MonsterType(int type){
		this.value = type;
	}

	public int getValue() {
		return value;
	}
	
}
