package com.yayo.warriors.module.recharge.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.utility.Splitable;

/**
 * 奖励次数
 * 
 * @author Hyint
 */
public class RewardCount {

	/** 奖励ID */
	private int rewardId;
	
	/** 清空时间 */
	private long endTime;
	
	/** 已领取的条件列表 */
	private Set<Integer> conditions = new HashSet<Integer>();

	public int getRewardId() {
		return rewardId;
	}

	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	public Set<Integer> getConditions() {
		return conditions;
	}
	
	public boolean containsCondition(int condition) {
		return this.conditions.contains(condition);
	}

	public void updateConditions(String condition) {
		if(condition != null && !condition.isEmpty()) {
			String[] array = condition.split(Splitable.ATTRIBUTE_SPLITE_1);
			for (String element : array) {
				if(StringUtils.isNotBlank(element)) {
					this.conditions.add(Integer.valueOf(element));
				}
			}
		}
	}
	
	public void setConditions(Set<Integer> conditions) {
		this.conditions = conditions;
	}

	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(this.conditions.isEmpty()) {
			stringBuffer.append(rewardId).append(Splitable.ATTRIBUTE_SPLIT).append(endTime);
		} else {
			stringBuffer.append(rewardId).append(Splitable.ATTRIBUTE_SPLIT);
			stringBuffer.append(endTime).append(Splitable.ATTRIBUTE_SPLIT);
			for (Integer condtion : this.conditions) {
				stringBuffer.append(condtion).append(Splitable.ATTRIBUTE_SPLITE_1);
			}
		}
		return stringBuffer.toString() ;
	}
	
	
}
