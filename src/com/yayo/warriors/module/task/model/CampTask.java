package com.yayo.warriors.module.task.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.task.type.TaskStatus;

public class CampTask implements Serializable {
	private static final long serialVersionUID = -8773076788678444160L;
	
	private long playerId;
	private int taskId;
	private String taskEvent = "";
	private int status = TaskStatus.ACCEPTED;
	@Transient
	private transient volatile List<TaskEvent> taskEvents = new ArrayList<TaskEvent>();
	public static CampTask valueOf(long playerId,int taskId){
		CampTask camTask = new CampTask();
		camTask.taskId = taskId;
		camTask.playerId = playerId;
		return camTask;
	}
	public void addTaskEvent(TaskEvent event){
		this.taskEvents.add(event);
		this.taskEvent = this.build2String();
	}
	public List<TaskEvent> getTaskEvents(){
		return taskEvents;
	}
	public void updateEvents(){
		this.taskEvent = this.build2String();
	}
	
	public boolean checkTaskStatus(){
		for(TaskEvent event : taskEvents){
			if(!event.isComplete()){
				return false;
			}
		}
		
		if(this.status == TaskStatus.ACCEPTED){
			this.status = TaskStatus.COMPLETED;
		}
		return true;
	}
	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public String getTaskEvent() {
		return taskEvent;
	}

	public void setTaskEvent(String taskEvent) {
		this.taskEvent = taskEvent;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (playerId ^ (playerId >>> 32));
		result = prime * result + taskId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CampTask other = (CampTask) obj;
		if (playerId != other.playerId)
			return false;
		if (taskId != other.taskId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CampTask [playerId=" + playerId + ", taskId=" + taskId
				+ ", taskEvent=" + taskEvent + ", status=" + status + "]";
	}
	private String build2String(){
		if(taskEvents == null || taskEvents.isEmpty()){
			return "";
		}
		
		StringBuilder builder = new StringBuilder();
		for(TaskEvent event : taskEvents){
			builder.append(event).append(Splitable.ELEMENT_DELIMITER);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}
	
	public String build2Task(){
		if(taskEvents == null || taskEvents.isEmpty()){
			return "";
		}
		
		StringBuilder builder = new StringBuilder();
		for(TaskEvent event : taskEvents){
			builder.append(taskId).append(Splitable.ATTRIBUTE_SPLIT).append(event).append(Splitable.ELEMENT_DELIMITER);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}
}
