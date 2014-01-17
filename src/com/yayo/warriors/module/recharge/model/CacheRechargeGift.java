package com.yayo.warriors.module.recharge.model;

import java.util.HashMap;
import java.util.Map;

import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.basedb.model.ChargeConditionConfig;
import com.yayo.warriors.basedb.model.ChargeConfig;

/**
 * 缓存的奖励对象
 * 
 * @author Hyint
 */
public class CacheRechargeGift {
	
	/** 当前已充值的金额 */
	private int currentGolden;
	
	/** 结束时间 */
	private long endTime = -1;

	/** 充值礼包对象 */
	private ChargeConfig chargeGift;

	/** 是否需要重新计算 */
	private boolean needRecalculate = true;
	
	/** 充值礼包条件对象 */
	private ChargeConditionConfig chargeCondition;
	
	/** 奖励状态对象 */
	private Map<Integer, RewardIdVO> rewardIdVOMap = new HashMap<Integer, RewardIdVO>(0);

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

	public ChargeConfig getChargeGift() {
		return chargeGift;
	}

	public void setChargeGift(ChargeConfig chargeGift) {
		this.chargeGift = chargeGift;
	}

	public ChargeConditionConfig getChargeCondition() {
		return chargeCondition;
	}

	public void setChargeCondition(ChargeConditionConfig chargeCondition) {
		this.chargeCondition = chargeCondition;
	}

	public Map<Integer, RewardIdVO> getRewardIdVOMap() {
		return rewardIdVOMap;
	}

	public void setRewardIdVOMap(Map<Integer, RewardIdVO> rewardIdVOMap) {
		this.rewardIdVOMap = rewardIdVOMap;
	}

	public RewardIdVO getRewardIdVO(int rewardId) {
		return this.rewardIdVOMap.get(rewardId);
	}
	
	public boolean isNeedRecalculate() {
		return needRecalculate;
	}

	public void setNeedRecalculate(boolean needRecalculate) {
		this.needRecalculate = needRecalculate;
	}

	public boolean isTimeOut() {
		return this.endTime > 0 && DateUtil.toSecond(this.endTime)  <= DateUtil.getCurrentSecond();
	}
	
	@Override
	public String toString() {
		return "CacheRechargeGift [currentGolden=" + currentGolden + ", endTime=" + endTime
				+ ", chargeGift=" + chargeGift + ", chargeCondition=" + chargeCondition
				+ ", rewardIdVOMap=" + rewardIdVOMap + "]";
	}

	public int getId() {
		return chargeGift.getId();
	}
}
