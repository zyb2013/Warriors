package com.yayo.warriors.module.recharge.type;

/**
 * 礼包的领取状态
 *  
 * @author Hyint
 */
public enum GiftState {

	/** 0 - 不能领取, 限制中 */
	LIMITED,
	
	/** 1 - 可领取奖励 */
	CANREWARD,
	
	/** 2 - 已领取奖励 */
	REWARDED,
}
