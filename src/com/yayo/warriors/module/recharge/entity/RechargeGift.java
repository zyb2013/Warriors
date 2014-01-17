package com.yayo.warriors.module.recharge.entity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.recharge.model.RewardCount;

/**
 * 用户充值礼包信息
 * 
 * @author Hyint
 */
@Entity
@Table(name="rechargeGift")
public class RechargeGift extends BaseModel<Long> {
	private static final long serialVersionUID = 9065511956852689153L;

	/** 角色ID */
	@Id
	@Column(name="playerId")
	private Long id;
	
	/** 
	 * 奖励信息.
	 * 
	 *  格式: 奖励ID1_清空时间_领取条件ID1#领取条件ID2#...|奖励ID2_清空时间_领取条件ID1#领取条件ID2#...
	 */
	@Lob
	private String rewardInfo;
	
	/**
	 * 奖励信息集合
	 */
	@Transient
	private transient Map<Integer, RewardCount> rewardInfoMap = null;
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getRewardInfo() {
		return rewardInfo;
	}

	public void setRewardInfo(String rewardInfo) {
		this.rewardInfo = rewardInfo;
	}
	
	/**
	 * 获得奖励信息集合
	 * 
	 * @return {@link Map}
	 */
	public Map<Integer, RewardCount> getRewardInfoMap() {
		if(this.rewardInfoMap != null) {
			return this.rewardInfoMap;
		}
		
		synchronized (this) {
			if(this.rewardInfoMap != null) {
				return this.rewardInfoMap;
			}
			
			this.rewardInfoMap = new ConcurrentHashMap<Integer, RewardCount>();
			List<String[]> arrays = Tools.delimiterString2Array(this.rewardInfo);
			if(arrays == null || arrays.isEmpty()) {
				return this.rewardInfoMap;
			}
			
			for (String[] element : arrays) {
				if(element != null) {
					RewardCount rewardCount = new RewardCount();
					rewardCount.setRewardId(Integer.valueOf(element[0]));
					rewardCount.setEndTime(Long.valueOf(element[1]));
					if(element.length >= 3) {
						rewardCount.updateConditions(element[2]);
					}
					this.rewardInfoMap.put(rewardCount.getRewardId(), rewardCount);
				}
			}
		}
		return this.rewardInfoMap;
	}
	
	/**
	 * 更新奖励信息
	 */
	public void updateRewardInfoMap() {
		StringBuffer buffer = new StringBuffer();
		for (RewardCount rewardCount : getRewardInfoMap().values()) {
			if(rewardCount != null) {
				buffer.append(rewardCount).append(Splitable.ELEMENT_DELIMITER);
			}
		}
		
		if(buffer.length() > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		
		this.rewardInfo = buffer.toString();
	}

	/**
	 * 获得已领取的奖励次数
	 * 
	 * @param  rewardId			奖励ID
	 * @return {@link Integer}	已领取的次数
	 */
	public RewardCount getRewardCount(int rewardId) {
		return this.getRewardInfoMap().get(rewardId);
	}
	
	public void addRewardCount(RewardCount rewardCount) {
		if(rewardCount != null) {
			this.getRewardInfoMap().put(rewardCount.getRewardId(), rewardCount);
		}
	}
	/**
	 * 构建充值礼包对象
	 * 
	 * @param  playerId				角色ID
	 * @return {@link RechargeGift}	充值礼包对象
	 */
	public static RechargeGift valueOf(long playerId) {
		RechargeGift rechargeGift = new RechargeGift();
		rechargeGift.id = playerId;
		return rechargeGift;
	}
	
	@Override
	public String toString() {
		return "UserRechargeGift [id=" + id + ", rewardInfo=" + rewardInfo + "]";
	}
}
