package com.yayo.warriors.module.user.type;

/**
 * 角色的阵营
 * 
 * @author Hyint
 */
public enum Camp {
	
	/** 0 - 无阵营 */
	NONE(""),
	
	/** 1 - 豪杰营 */
	KNIFE_CAMP("豪杰阵营"),
	
	/** 2 - 侠客营 */
	SWORD_CAMP("侠客阵营");
	
	/** 阵营名字 */
	private String name = null;
	
	Camp(String name){
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	
}
