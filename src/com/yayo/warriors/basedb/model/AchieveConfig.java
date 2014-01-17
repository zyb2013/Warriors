package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/**
 * 成就基础类
 * 
 * @author huachaoping
 */
@Resource
public class AchieveConfig {
	
	/**  */
	@Id
	private int id;
	
	/** 成就类型 */
	@Index(name=IndexName.ACHIEVE_TYPE, order=0)
	private int achieveType;
	
	/** 成就达成条件 */
	private String conditions;
	
	/** 绑定元宝奖励 */
	private int couponReward;

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAchieveType() {
		return achieveType;
	}

	public void setAchieveType(int achieveType) {
		this.achieveType = achieveType;
	}

	public String getConditions() {
		return conditions;
	}

	public void setConditions(String conditions) {
		this.conditions = conditions;
	}

	public int getCouponReward() {
		return couponReward;
	}

	public void setCouponReward(int couponReward) {
		this.couponReward = couponReward;
	}

	
	public int getConditionValue() {
		return Integer.valueOf(conditions);
	}
	
	@Override
	public String toString() {
		return "AchieveConfig [id=" + id + ", achieveType=" + achieveType
				+ ", conditions=" + conditions + ", couponReward="
				+ couponReward + "]";
	}
	
}
