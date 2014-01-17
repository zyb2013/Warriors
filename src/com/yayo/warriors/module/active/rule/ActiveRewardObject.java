package com.yayo.warriors.module.active.rule;

/**
 * 运营活动,冲级类型,奖励领取对象 
 * @author liuyuhua
 */
public interface ActiveRewardObject {

	/** 所有人*/
	int EVERYBODY = 0;
	
	/** 帮主*/
	int ALLIANCE_MASTER = 1;
	
	/** 帮员*/
	int ALLIANCE_MEMBER = 2;
}
