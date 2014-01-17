package com.yayo.warriors.module.active.rule;

/**
 * 活动奖励状态 
 * @author liuyuhua
 */
public interface ActiveRewardStatus {
	
	/** 不可领取*/
	int UNRESERVED = 0;

	/** 可以领取*/
	int RESERVED = 1;
	
	/** 已领取*/
	int REWARDED = 2;
}
