package com.yayo.warriors.module.recharge.model;

import java.util.Map;

import com.yayo.warriors.basedb.model.ChargeConditionConfig;
import com.yayo.warriors.basedb.model.ChargeConfig;

/**
 * 
 * @author Hyint
 *
 */
public class GiftObject {
	
	/** 结束时间 */
	private long endTime = -1L;
	
	/** 当前已充值金额 */
	private int currentGolden = 0;

	/** 充值礼包配置对象*/
	private ChargeConfig chargeConfig;
	
	private Map<Integer, RewardIdVO> rewards;

	/** 充值条件对象 */
	private ChargeConditionConfig chargeCondition;
	
	public int getCurrentGolden() {
		return currentGolden;
	}

	public void setCurrentGolden(int currentGolden) {
		this.currentGolden = currentGolden;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public Map<Integer, RewardIdVO> getRewards() {
		return rewards;
	}

	public void setRewards(Map<Integer, RewardIdVO> rewards) {
		this.rewards = rewards;
	}

	public ChargeConfig getChargeConfig() {
		return chargeConfig;
	}

	public void setChargeConfig(ChargeConfig chargeConfig) {
		this.chargeConfig = chargeConfig;
	}

	public ChargeConditionConfig getChargeCondition() {
		return chargeCondition;
	}

	public void setChargeCondition(ChargeConditionConfig chargeCondition) {
		this.chargeCondition = chargeCondition;
	}
}
