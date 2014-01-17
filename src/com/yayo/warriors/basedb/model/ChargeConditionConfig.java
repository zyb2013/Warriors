package com.yayo.warriors.basedb.model;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;


/**
 * 充值条件对象
 * 
 * @author Hyint
 */
@Resource
public class ChargeConditionConfig implements Comparable<ChargeConditionConfig> {

	/** 充值条件ID */
	@Id
	private int id;
	
	/** 充值条件序列号 */
	@Index(name="RECHARGE_SERIAL", order=0)
	private int serial;
	
	/** 充值要求. 元宝数量 */
	private int condition;
	
	/** 最大充值元宝数量 */
	private int maxCondition;
	
	/** 可以获得的礼包ID */
	private String rewardId;

	/** 下一个条件ID */
	private int nextConditionId;
	
	@JsonIgnore
	private transient List<Integer> rewardIdList;
	
	/** 重置奖励配置 */
	@JsonIgnore
	private transient List<ChargeRewardConfig> rechargeRewards;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSerial() {
		return serial;
	}

	public void setSerial(int serial) {
		this.serial = serial;
	}

	public int getCondition() {
		return condition;
	}

	public void setCondition(int condition) {
		this.condition = condition;
	}

	public String getRewardId() {
		return rewardId;
	}
	
	public int getNextConditionId() {
		return nextConditionId;
	}
	
	public boolean hasNextCondition() {
		return nextConditionId > 0;
	} 
	
	public void setNextConditionId(int nextConditionId) {
		this.nextConditionId = nextConditionId;
	}

	public List<Integer> getRewardIds() {
		if(this.rewardIdList != null) {
			return this.rewardIdList;
		}
		
		synchronized (this) {
			if(this.rewardIdList != null) {
				return this.rewardIdList;
			}
			
			this.rewardIdList = new LinkedList<Integer>();
			if(StringUtils.isBlank(this.rewardId)) {
				return this.rewardIdList;
			}
			
			for (String element : this.rewardId.split(Splitable.ELEMENT_SPLIT)) {
				if(StringUtils.isNotBlank(element)) {
					this.rewardIdList.add(Integer.valueOf(element));
				}
			}
		}
		return this.rewardIdList;
	}

	public void setRewardId(String rewardId) {
		this.rewardId = rewardId;
	}

	public List<ChargeRewardConfig> getRechargeRewards() {
		return rechargeRewards;
	}

	public void setRechargeRewards(List<ChargeRewardConfig> rechargeRewards) {
		this.rechargeRewards = rechargeRewards;
	}

	public int getMaxCondition() {
		return maxCondition;
	}

	public void setMaxCondition(int maxCondition) {
		this.maxCondition = maxCondition;
	}

	
	public int compareTo(ChargeConditionConfig o) {
		if(this.condition == o.getCondition()) {
			return 0;
		}
		if(this.condition < o.getCondition()) {
			return -1;
		}
		return 1;
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChargeConditionConfig other = (ChargeConditionConfig) obj;
		if (id != other.id)
			return false;
		return true;
	}

	
	public String toString() {
		return "RechargeConditionConfig [id=" + id + ", serial=" + serial + ", condition="
				+ condition + ", rewardId=" + rewardId + "]";
	}
}
