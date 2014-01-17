package com.yayo.warriors.module.task.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.task.model.TaskEvent;
import com.yayo.warriors.module.task.type.EventStatus;
import com.yayo.warriors.module.task.type.TaskStatus;

@Entity
@Table(name="userMapTask")
public class UserMapTask extends BaseModel<Long> {
	private static final long serialVersionUID = 8141324154240356755L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private int chain;

	private int taskId;

	private long playerId;

	@Lob
	private String taskEvent = "";

	private int status = TaskStatus.UNACCEPT;

	@Transient
	private transient volatile boolean busy = false;
	
	@Transient
	private transient volatile TaskEvent[] taskEvents = null;
	
	public boolean isBusy() {
		return busy;
	}

	public void updateBusy(boolean busy) {
		this.busy = busy;
	}

	public boolean canCancelTask() {
		return taskId > 0 && this.status != TaskStatus.UNACCEPT && status != TaskStatus.REWARDS;
	}
	public static UserMapTask valueOf(long playerId, int chain) {
		UserMapTask userTask = new UserMapTask();
		userTask.chain = chain;
		userTask.playerId = playerId;
		userTask.status = TaskStatus.UNACCEPT;
		return userTask;
	}
	
	public static UserMapTask acceptNewTask(long playerId, int chain, int taskId, String taskEvents) {
		UserMapTask userTask = new UserMapTask();
		userTask.setChain(chain);
		userTask.setTaskId(taskId);
		userTask.setPlayerId(playerId);
		userTask.setTaskEvent(taskEvents);
		userTask.setStatus(TaskStatus.ACCEPTED);
		return userTask;
	}
	
	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public int getChain() {
		return chain;
	}

	public void setChain(int chain) {
		this.chain = chain;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getTaskEvent() {
		return taskEvent;
	}

	public void setTaskEvent(String taskEvent) {
		this.taskEvents = null;
		this.taskEvent = taskEvent;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	/**
	 * 获得任务事件对象
	 * 
	 * @return {@link TaskEvent[]}
	 */
	public TaskEvent[] getTaskEvents() {
		if (taskEvents != null) {
			return this.taskEvents;
		}

		synchronized (this) {
			if (taskEvents != null) {
				return this.taskEvents;
			}

			List<TaskEvent> events = new ArrayList<TaskEvent>();
			List<String[]> arrays = Tools.delimiterString2Array(this.taskEvent);
			if (arrays != null && !arrays.isEmpty()) {
				for (String[] array : arrays) {
					int type = Integer.valueOf(array[0]);
					int condition = Integer.valueOf(array[1]);
					int amount = Integer.valueOf(array[2]);
					int totalAmont = Integer.valueOf(array[3]);
					int status = Integer.valueOf(array[4]);
					EventStatus eventStatus = EnumUtils.getEnum(EventStatus.class, status);
					events.add(TaskEvent.valueOf(type, condition, amount,totalAmont, eventStatus));
				}
			}
			this.taskEvents = events.toArray(new TaskEvent[events.size()]);
		}
		return this.taskEvents;
	}
	
	/**
	 * 更新任务事件信息
	 */
	public void updateUserTaskEvents() {
		StringBuilder builder = new StringBuilder();
		for (TaskEvent taskEvent : this.getTaskEvents()) {
			builder.append(taskEvent).append(Splitable.ELEMENT_DELIMITER);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		this.taskEvent = builder.toString();
	}

	@Override
	public String toString() {
		return "UserMapTask [id=" + id + ", chain=" + chain + ", taskId=" + taskId + ", playerId="
				+ playerId + ", taskEvent=" + taskEvent + ", status=" + status + "]";
	}

	public void checkUserTaskStatus() {
		this.updateUserTaskEvents();
		TaskEvent[] events = this.getTaskEvents();
		if(events != null && events.length > 0) {
			for (TaskEvent taskEvent : events) {
				if(!taskEvent.isComplete()) {
					return;
				}
			}
		}
		
		if(this.status == TaskStatus.ACCEPTED) {
			this.status = TaskStatus.COMPLETED;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!super.equals(obj)) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		
		UserMapTask other = (UserMapTask) obj;
		return this.id != null && other.id != null && this.id.equals(other.id) 
			&& this.playerId == other.playerId && this.chain == other.chain;
	}
	
}
