package com.yayo.warriors.module.recharge.type;

/**
 * 礼包类型
 * 
 * @author Hyint
 */
public interface GiftType {

	/**
	 * <li>1-首次充值 </li>
	 */
	int FIRST_RECHARGE = 1;

	/**
	 * <li>2-累积充值 </li>
	 */
	int ACCUMULATE_RECHARGE = 2;
	
	/**
	 * <li>3-循环活动</li>
	 */
	int CYCLE_RECHARGE = 3;
	 
}
