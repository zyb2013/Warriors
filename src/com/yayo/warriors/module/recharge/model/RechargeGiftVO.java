package com.yayo.warriors.module.recharge.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 充值礼包 VO对象
 * 
 * @author Hyint
 */
public class RechargeGiftVO implements Serializable {
	private static final long serialVersionUID = -2326307148471837587L;

	/** 礼包ID */
	private int id;
	
	/** 当前充值的金币数量 */
	private int currGolden;
	
	/** 总共需要的金币数量 */
	private int totalGolden;
	
	/** 当前条件需要的元宝数 */
	private int conditionGolden;
	
	/** 礼包的活动结束时间. -1-为永久生效*/
	private long endTime = -1L;

	/** 已领取的奖励ID数组 */
	private RewardIdVO[] rewardIds = null;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCurrGolden() {
		return currGolden;
	}

	public void setCurrGolden(int currGolden) {
		this.currGolden = currGolden;
	}

	public int getTotalGolden() {
		return totalGolden;
	}

	public void setTotalGolden(int totalGolden) {
		this.totalGolden = totalGolden;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public RewardIdVO[] getRewardIds() {
		return rewardIds;
	}

	public void setRewardIds(RewardIdVO[] rewardIds) {
		this.rewardIds = rewardIds;
	}

	public int getConditionGolden() {
		return conditionGolden;
	}

	public void setConditionGolden(int conditionGolden) {
		this.conditionGolden = conditionGolden;
	}

	public static RechargeGiftVO valueOf(CacheRechargeGift rechargeGift) {
		if(rechargeGift == null) {
			return null;
		}
		
		RechargeGiftVO rechargeGiftVO = new RechargeGiftVO();
		rechargeGiftVO.id = rechargeGift.getId();
		rechargeGiftVO.endTime = rechargeGift.getEndTime();
		rechargeGiftVO.currGolden = rechargeGift.getCurrentGolden();
		rechargeGiftVO.totalGolden = rechargeGift.getChargeCondition().getMaxCondition();
		rechargeGiftVO.conditionGolden = rechargeGift.getChargeCondition().getCondition();
		rechargeGiftVO.rewardIds = rechargeGift.getRewardIdVOMap().values().toArray(new RewardIdVO[0]);
		return rechargeGiftVO;
	}
	
	@Override
	public String toString() {
		return "RechargeGiftVO [id=" + id + ", currGolden=" + currGolden + ", totalGolden="
				+ totalGolden + ", endTime=" + endTime + ", rewardIds="
				+ Arrays.toString(rewardIds) + "]";
	}
}
