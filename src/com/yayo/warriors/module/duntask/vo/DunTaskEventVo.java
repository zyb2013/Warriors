package com.yayo.warriors.module.duntask.vo;

import java.io.Serializable;

import com.yayo.warriors.module.duntask.types.TaskTypes;

/**
 * 任务事件
 * @author liuyuhua
 */
public class DunTaskEventVo implements Serializable{
	private static final long serialVersionUID = 4488336087003498779L;
	
	/** 事件ID*/
	private int id;
	
	/** 副本类型*/
	private TaskTypes type;

	/** 完成条件(数量)*/
	private int completeCount;
	
	/** 当前数量(数量)*/
	private int currentCount;
	
	/** 条件*/
	private int condition;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public TaskTypes getType() {
		return type;
	}

	public void setType(TaskTypes type) {
		this.type = type;
	}

	public int getCompleteCount() {
		return completeCount;
	}

	public void setCompleteCount(int completeCount) {
		this.completeCount = completeCount;
	}

	public int getCurrentCount() {
		return currentCount;
	}

	public void setCurrentCount(int currentCount) {
		this.currentCount = currentCount;
	}

	public int getCondition() {
		return condition;
	}

	public void setCondition(int condition) {
		this.condition = condition;
	}

	@Override
	public String toString() {
		return "DunTaskEventVo [id=" + id + ", type=" + type
				+ ", completeCount=" + completeCount + ", currentCount="
				+ currentCount + ", condition=" + condition + "]";
	}
	
}
