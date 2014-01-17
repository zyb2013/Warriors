package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.module.task.type.EventType;

/**
 * 日环任务配置对象
 * 
 * @author Hyint
 */
@Resource
public class LoopTaskConfig implements Comparable<LoopTaskConfig> {

	/** 配置ID */
	@Id
	private int id;
	
	/** 日环任务名字 */
	private String name;
	
	/** 事件类型. {@link EventType} */
	private int type;
	
	/** 获得该事件的概率 */
	private int rate;
	
	/** 最大概率 */
	private int maxRate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public String toString() {
		return "LoopTaskConfig [id=" + id + ", type=" + type + ", rate=" + rate + ", maxRate=" + maxRate + "]";
	}
	
	
	public int compareTo(LoopTaskConfig o) {
		return o == null || this.id < o.getId() ? -1 : (this.id == o.getId() ? 0 : 1);
	}

}
