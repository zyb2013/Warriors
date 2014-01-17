package com.yayo.warriors.module.task.model;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.task.type.EventStatus;
import com.yayo.warriors.module.task.type.EventType;

public class TaskEvent implements Serializable {
	private static final long serialVersionUID = -3241360913241683650L;

	private int type;
	
	private int condition;
	
	private int amount;
	
	private int totalAmont ;

	private EventStatus status = EventStatus.PROGRESS;
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCondition() {
		return condition;
	}

	public void setCondition(int condition) {
		this.condition = condition;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public EventStatus getStatus() {
		return status;
	}

	public void setStatus(EventStatus status) {
		this.status = status;
	}

	public boolean isComplete() {
		this.updateTaskState();
		return this.status == EventStatus.COMPLETED;
	}
	
	/**
	 * 检查是否完成任务 
	 */
	public void updateTaskState() {
		if(this.amount <= 0 && this.status == EventStatus.PROGRESS) {
			this.status = EventStatus.COMPLETED;
		}
	}
	
	public static TaskEvent valueOf(String condition) {
		if(StringUtils.isBlank(condition)) {
			return null;
		}
		
		String[] array = condition.split(Splitable.DELIMITER_ARGS);
		int type = Integer.valueOf(array[0]);
		int cond = Integer.valueOf(array[1]);
		int amount = Integer.valueOf(array[2]);
		int status = Integer.valueOf(array[3]);
		return valueOf(type, cond, amount,amount, EnumUtils.getEnum(EventStatus.class, status));
	}
	
	public static TaskEvent valueOf(int type, int condition, int amount,int totalAmonnt, EventStatus status) {
		TaskEvent taskEvent = new TaskEvent();
		taskEvent.type = type;
		taskEvent.amount = amount;
		taskEvent.status = status;
		taskEvent.condition = condition;
		taskEvent.totalAmont = totalAmonnt ;
		return taskEvent;
	}
	
	
	public int getTotalAmont() {
		return totalAmont;
	}

	public void setTotalAmont(int totalAmont) {
		this.totalAmont = totalAmont;
	}

	@Override
	public String toString() {
		return new StringBuffer().append(type).append(Splitable.ATTRIBUTE_SPLIT)
								 .append(condition).append(Splitable.ATTRIBUTE_SPLIT)
								 .append(amount).append(Splitable.ATTRIBUTE_SPLIT)
								 .append(totalAmont).append(Splitable.ATTRIBUTE_SPLIT)
								 .append(status.ordinal()).toString();
	}
}
