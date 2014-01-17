package com.yayo.warriors.module.cooltime.type;

/**
 * CD的元素类型
 * 
 * @author Hyint
 */
public enum CoolType {
	
	/** 公共CD */
	GLOBAL(false),
	
	/** 局部CD, 指定在物品/技能上 */
	LOCALTION(true);
	
	private boolean persist;
	
	CoolType(boolean persist) {
		this.persist = persist;
	}

	public boolean isPersist() {
		return persist;
	}

	public void setPersist(boolean persist) {
		this.persist = persist;
	}
}
