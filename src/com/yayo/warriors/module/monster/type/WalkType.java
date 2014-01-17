package com.yayo.warriors.module.monster.type;
/**
 *	行走方式
 */
public enum WalkType{
	/** 直线 */
	DIRECT(false),
	/** A星 */
	ASTAR(true),
	/** 先直线后A星 */
	OPTIMIZE(false);
	
	/** 是否要验证目标位置 */
	private boolean stand;
	
	WalkType(boolean stand){
		this.stand = stand;
	}

	public boolean isStand() {
		return stand;
	}
	
}