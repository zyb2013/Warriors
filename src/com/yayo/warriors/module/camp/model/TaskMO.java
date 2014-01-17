package com.yayo.warriors.module.camp.model;

import com.yayo.common.utility.Splitable;

/**
 * 阵营任务临时模型
 * @author liuyuhua
 */
public class TaskMO {
	
	/** 任务增量ID*/
	private long taskId;
	
	/** 任务原型ID*/
	private int taskBaseId;
	
	/** 品质*/
	private int quality;
	
	/**
	 * 构造方法
	 * @param taskId       任务增量ID
	 * @param taskBaseId   任务原型ID
	 * @param quality      任务品质
	 * @return {@link TaskMO} 阵营任务临时模型
	 */
	public static TaskMO valueOf(long taskId,int taskBaseId,int quality) {
		TaskMO taskMO = new TaskMO();
		taskMO.taskId = taskId;
		taskMO.taskBaseId = taskBaseId;
		taskMO.quality = quality;
		return taskMO;
	}
	
	/**
	 * 构造方法
	 * @param taskStr  格式:{任务增量ID_任务原型ID_品质}
	 * @return {@link TaskMo} 阵营任务临时模型
	 */
	public static TaskMO valueOf(String taskStr) {
		if(taskStr == null) {
			return new TaskMO();
		}
		
		String[] tmps = taskStr.split(taskStr);
		if(tmps.length < 3) {
			return new TaskMO();
		}
		
		long taskId = Long.parseLong(tmps[0]);
		int taskBaseId = Integer.parseInt(tmps[1]);
		int quality = Integer.parseInt(tmps[2]);
		return TaskMO.valueOf(taskId, taskBaseId, quality);
	}
	
	/**
	 * 格式化信息
	 * @return {@link String} {任务增量ID_任务原型ID_品质}
	 */
	public String format() {
		return this.taskId + Splitable.ATTRIBUTE_SPLIT + this.taskBaseId + Splitable.ATTRIBUTE_SPLIT + this.quality;
	}
	
	//Getter and Setter...

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public int getTaskBaseId() {
		return taskBaseId;
	}

	public void setTaskBaseId(int taskBaseId) {
		this.taskBaseId = taskBaseId;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	@Override
	public String toString() {
		return "TaskMO [taskId=" + taskId + ", taskBaseId=" + taskBaseId
				+ ", quality=" + quality + "]";
	}
}
