package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;

/**
 * 洗练规则配置信息
 * 
 * @author Hyint
 */
@Resource
public class WashRuleConfig {

	/** 规则ID */
	@Id
	private int id;
	
	/** 成功概率 */
	private int rate;
	
	/** 最大概率 */
	private int maxRate;

	/** 银币消耗概率 */
	private double silver;
	
	/** 装备的品质 */
	private int quality;
	
	/** 道具消耗数量 */
	private int itemCount;
	
	/** 失败后还可以拥有的属性条数 */
	private int failureAddition;
	
	/** 成功后还可以拥有的属性条数 */
	private int successAddition;
	
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

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public int getItemCount() {
		return itemCount;
	}

	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}

	public int getFailureAddition() {
		return failureAddition;
	}

	public void setFailureAddition(int failureAddition) {
		this.failureAddition = failureAddition;
	}

	public int getSuccessAddition() {
		return successAddition;
	}

	public void setSuccessAddition(int successAddition) {
		this.successAddition = successAddition;
	}

	public boolean isWashSuccess() {
		return Tools.getRandomInteger(this.maxRate) < this.rate;
	}
	
	@Override
	public String toString() {
		return "WashRuleConfig [id=" + id + ", rate=" + rate + ", maxRate=" + maxRate 
				+ ", silver=" + silver + ", quality=" + quality + ", itemCount=" + itemCount
				+ ", failureAddition=" + failureAddition + ", successAddition=" + successAddition + "]";
	}
}
