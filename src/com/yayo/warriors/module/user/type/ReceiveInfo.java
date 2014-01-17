package com.yayo.warriors.module.user.type;

/**
 * 角色新手引导可以获得物品的信息格式类型
 * 
 * @author Hyint
 */
public enum ReceiveInfo {

	/** 0 - 领取VIP*/
	RECEIVE_VIP(16, 160001),
	
	/** 1 - 扩展背包 */
	EXPAND_BACKPACK(18, 120003),

	/** 2 - 家将令 */
	PET_PROPS(19, 110006),
	
	/** 3 - 收藏游戏奖励 */
	GARNER_REWARD(30, 1501001)
	
	;
	
	private int level;

	private int propsId;
	
	ReceiveInfo(int playerLevel, int propsId) {
		this.level = playerLevel;
		this.propsId = propsId;
	}
	
	public int getPropsId() {
		return propsId;
	}
	
	/**
	 * 验证角色等级是否符合
	 * 
	 * @param  playerLevel		角色等级
	 * @return {@link Boolean}	true-等级符合可以领取, false-等级不符合
	 */
	public boolean isLevelLimit(int playerLevel) {
		return playerLevel < this.level;
	}

	
}
