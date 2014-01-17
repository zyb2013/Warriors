package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;

/**
 * 日环任务品质对象
 * 
 * @author Hyint
 */
@Resource
public class LoopQualityConfig implements Comparable<LoopQualityConfig> {
	
	/** 任务的品质 */
	@Id
	private int id;
	
	/** 获得的概率 */
	private int rate;
	
	/** 最大概率 */
	private int maxRate;

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

	
	public int compareTo(LoopQualityConfig o) {
		return o == null || this.id < o.getId() ? -1 : (this.id == o.getId() ? 0 : 1);
	}
	
	
	public String toString() {
		return "LoopQualityConfig [id=" + id + ", rate=" + rate + ", maxRate=" + maxRate + "]";
	}
}
