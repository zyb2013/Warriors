package com.yayo.warriors.module.duntask.model;

import com.yayo.warriors.basedb.model.DungeonTaskEventConfig;
import com.yayo.warriors.module.duntask.types.TaskTypes;

/**
 * 副本任务事件
 * @author liuyuhua
 */
public class DunTaskEvent {
	
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
	
	/**
	 * 构造方法
	 * @param config 副本任务时间配置对象
	 * @return {@link DunTaskEvent}
	 */
	public static DunTaskEvent valueOf(DungeonTaskEventConfig config){
		DunTaskEvent taskEvent = new DunTaskEvent();
		if(config == null){
			throw new RuntimeException("构造副本事件对象,副本事件配置为null,无法构建,请检查副本任务事件配置");
		}
		
		taskEvent.id = config.getId();
		taskEvent.currentCount = 0;
		taskEvent.completeCount = config.getCompleteCount();
		taskEvent.type = config.getType();
		taskEvent.condition = config.getCondition();
		return taskEvent;
	}

	
	/**
	 * 事件是否完成
	 * @return true完成;false没有完成
	 */
	public boolean isCompleted(){
		return currentCount >= completeCount;
	}

	//Getter and Setter...
	
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
		return "DunTaskEvent [id=" + id + ", type=" + type + ", completeCount="
				+ completeCount + ", currentCount=" + currentCount
				+ ", condition=" + condition + "]";
	}
}
