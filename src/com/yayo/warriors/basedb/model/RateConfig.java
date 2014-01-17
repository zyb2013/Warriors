package com.yayo.warriors.basedb.model;

/**
 * 概率配置
 * @author jonsai
 *
 */
public abstract class RateConfig {
	/** 概率 */
	protected int rate;
	
	/** 满值 */
	protected int fullRate;

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getFullRate() {
		return fullRate;
	}

	public void setFullRate(int fullRate) {
		this.fullRate = fullRate;
	}
	
	
}
