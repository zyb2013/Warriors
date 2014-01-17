package com.yayo.warriors.module.duntask.vo;

import java.io.Serializable;
import java.util.Arrays;

import com.yayo.warriors.module.duntask.types.TaskState;

/**
 * 任务的VO 
 * @author liuyuhua
 */
public class DunTaskVo implements Serializable{
	private static final long serialVersionUID = -6732731857436282510L;

	/** 任务ID*/
	private long id;
	
	/** 任务基础ID*/
	private int baseId;
	
	/** 状态*/
	private TaskState state;
	
	/** 事件类型*/
	private DunTaskEventVo[] events;
	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public TaskState getState() {
		return state;
	}

	public void setState(TaskState state) {
		this.state = state;
	}

	public DunTaskEventVo[] getEvents() {
		return events;
	}

	public void setEvents(DunTaskEventVo[] events) {
		this.events = events;
	}

	@Override
	public String toString() {
		return "DunTaskVo [id=" + id + ", baseId=" + baseId + ", state="
				+ state + ", events=" + Arrays.toString(events) + "]";
	}
}
