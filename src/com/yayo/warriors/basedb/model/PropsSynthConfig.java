package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;

/**
 * 宝石合成表
 * 
 * @author Hyint
 */
@Resource
public class PropsSynthConfig {
	
	/** 道具ID */
	@Id
	private int id;
	
	/** 合成的道具ID */
	private int nextId;
	
	/** 合成道具需要的游戏币 */
	private int silver;
	
	/** 成功的概率 */
	private int rate;
	
	/** 最大概率 */
	private int maxRate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNextId() {
		return nextId;
	}

	public void setNextId(int nextId) {
		this.nextId = nextId;
	}

	public int getSilver() {
		return silver;
	}

	public void setSilver(int silver) {
		this.silver = silver;
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

	public boolean isSynthSuccess() {
		int random = Tools.getRandomInteger(this.maxRate);
		return random < this.rate;
	}
	
	public int getTotalSynthSilver(int synthCount) {
		return this.silver * synthCount;
	}
	
	@Override
	public String toString() {
		return "PropsSynthConfig [id=" + id + ", nextId=" + nextId + ", silver=" 
				+ silver + ", rate=" + rate + ", maxRate=" + maxRate + "]";
	}
	
	
}
