package com.yayo.warriors.module.recharge.model;

import java.io.Serializable;

import com.yayo.common.scheduling.ScheduledType;
import com.yayo.warriors.module.recharge.type.GiftState;

/**
 * 奖励ID信息
 * 
 * @author Hyint
 */
public class RewardIdVO implements Serializable {
	private static final long serialVersionUID = 3957893218137515905L;

	/** 奖励ID */
	private int rewardId;
	
	/** 奖励状态 */
	private GiftState state = GiftState.LIMITED;

	public int getRewardId() {
		return rewardId;
	}

	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}

	public GiftState getState() {
		return state;
	}

	public void setState(GiftState state) {
		this.state = state;
	}

	public static RewardIdVO valueOf(int rewardId, GiftState giftState) {
		RewardIdVO rewardIdVO = new RewardIdVO();
		rewardIdVO.rewardId = rewardId;
		rewardIdVO.state = giftState;
		return rewardIdVO;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + rewardId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		RewardIdVO other = (RewardIdVO) obj;
		return rewardId == other.rewardId;
	}
	
	@Override
	public String toString() {
		return "RewardIdVO [rewardId=" + rewardId + ", state=" + state + "]";
	} 
	
}
