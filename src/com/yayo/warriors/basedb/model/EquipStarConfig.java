package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;

/**
 * 装备星级配置
 * 
 * @author Hyint
 */
@Resource
public class EquipStarConfig {
	
	/** 星级 */
	@Id
	private int id;
	
	/** 成功率 */
	private int rate;
	
	/** 最大概率 */
	private int maxRate;
	
	
	/** 升星失败后的星级 */
	private int failureStar;
	
	/** 附加属性的最小值 */
	private int minAttrValue;

	/** 需要扣除的道具ID */
	private int itemId = 0;

	/** 需要扣除的道具数量 */
	private int itemCount= 0;
	
	/** 需要消耗的银币 */
	private double silver = 0D;
	
	/** 属性值提升倍率*/
	private double attrValueRatio;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getMaxRate() {
		return maxRate;
	}

	public void setMaxRate(int maxRate) {
		this.maxRate = maxRate;
	}

	public double getSilver() {
		return silver;
	}

	public void setSilver(double silver) {
		this.silver = silver;
	}

	public int getFailureStar() {
		return failureStar;
	}

	public void setFailureStar(int failureStar) {
		this.failureStar = failureStar;
	}

	public int getMinAttrValue() {
		return minAttrValue;
	}

	public void setMinAttrValue(int minAttrValue) {
		this.minAttrValue = minAttrValue;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getItemCount() {
		return itemCount;
	}

	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}

	public double getAttrValueRatio() {
		return attrValueRatio;
	}

	public void setAttrValueRatio(double attrValueRatio) {
		this.attrValueRatio = attrValueRatio;
	}

	public int getStarAttrValue(int baseAttrValue) {
		int addValue = (int) Math.floor(baseAttrValue * this.attrValueRatio);
		int attrValue = baseAttrValue + Math.max(addValue, this.minAttrValue);
		return attrValue;
	}
	
	public boolean isAscentStarSuccess(int addLuckyCount) {
		return Tools.getRandomInteger(this.maxRate) < rate + (addLuckyCount * 10);
	}
	
	@Override
	public String toString() {
		return "EquipStarConfig [id=" + id + ", rate=" + rate + ", maxRate=" + maxRate + ", silver=" 
				+ silver + ", failureStar=" + failureStar + ", minAttrValue=" + minAttrValue + ", itemId=" 
				+ itemId + ", itemCount=" + itemCount + ", attrValueRatio=" + attrValueRatio + "]";
	}
}
