package com.yayo.warriors.module.task.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.basedb.model.MapTaskConfig;
import com.yayo.warriors.basedb.model.TaskConfig;

@Entity
@Table(name="taskComplete")
public class TaskComplete extends BaseModel<Long> {
	private static final long serialVersionUID = -3699425694198583666L;
	
	@Id
	@Column(name="playerId")
	private Long id;
	@Lob
	private String completes = "";
	@Lob
	private String mapTaskCompletes = "";
	
	@Transient
	private transient volatile Set<Integer> completeIdSet = null;

	@Transient
	private transient volatile Set<Integer> mapTaskCompleteSet = null;
	
	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getCompletes() {
		return completes;
	}

	public void setCompletes(String completes) {
		this.completeIdSet = null;
		this.completes = completes;
	}
	
	public String getMapTaskCompletes() {
		return mapTaskCompletes;
	}

	public void setMapTaskCompletes(String mapTaskCompletes) {
		this.mapTaskCompleteSet = null;
		this.mapTaskCompletes = mapTaskCompletes;
	}

	/**
	 * 获得完成任务的ID列表
	 * 
	 * @return {@link Set}
	 */
	public Set<Integer> getCompleteIdSet() {
		if(this.completeIdSet != null) {
			return this.completeIdSet;
		}
		
		synchronized (this) {
			if(this.completeIdSet != null) {
				return this.completeIdSet;
			}
			
			this.completeIdSet = new HashSet<Integer>();
			if(StringUtils.isBlank(this.completes)) {
				return this.completeIdSet;
			}
			
			String[] arrays = this.completes.split(Splitable.ATTRIBUTE_SPLIT);
			for (String array : arrays) {
				if(!StringUtils.isBlank(array)) {
					this.completeIdSet.add(Integer.valueOf(array));
				}
			}
		}
		return this.completeIdSet;
	}
	
	public void addTaskId(int taskId, boolean update) {
		this.getCompleteIdSet().add(taskId);
		if(update) {
			this.updateCompleteSet();
		}
	}
	
	/**
	 * 更新完成任务信息
	 */
	public void updateCompleteSet() {
		StringBuilder builder = new StringBuilder();
		for (Integer taskId : getCompleteIdSet()) {
			builder.append(taskId).append(Splitable.ATTRIBUTE_SPLIT);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		this.completes = builder.toString();
	}

	public boolean isContainTask(TaskConfig taskConfig) {
		return taskConfig == null || this.getCompleteIdSet().contains(taskConfig.getId());
	}

	public boolean isContainTask(List<TaskConfig> taskList) {
		if(taskList == null || taskList.isEmpty()) {
			return true;
		}
		
		Set<Integer> set = this.getCompleteIdSet();
		for (TaskConfig task : taskList) {
			if(set.contains(task.getId())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 构建任务完成对象
	 * 
	 * @param  playerId					角色ID
	 * @return {@link TaskComplete}		任务信息对象
	 */
	public static TaskComplete valueOf(long playerId) {
		TaskComplete taskComplete = new TaskComplete();
		taskComplete.id = playerId;
		return taskComplete;
	}
	
	/**
	 * 获得完成任务的ID列表
	 * 
	 * @return {@link Set}
	 */
	public Set<Integer> getMapCompleteIdSet() {
		if(this.mapTaskCompleteSet != null) {
			return this.mapTaskCompleteSet;
		}
		
		synchronized (this) {
			if(this.mapTaskCompleteSet != null) {
				return this.mapTaskCompleteSet;
			}
			
			this.mapTaskCompleteSet = new HashSet<Integer>();
			if(StringUtils.isBlank(this.mapTaskCompletes)) {
				return this.mapTaskCompleteSet;
			}
			
			String[] arrays = mapTaskCompletes.split(Splitable.ATTRIBUTE_SPLIT);
			for (String array : arrays) {
				if(!StringUtils.isBlank(array)) {
					this.mapTaskCompleteSet.add(Integer.valueOf(array));
				}
			}
		}
		return this.mapTaskCompleteSet;
	}
	
	public void addMapTaskId(int taskId, boolean update) {
		this.getMapCompleteIdSet().add(taskId);
		if(update) {
			this.updateMapCompleteIdSet();
		}
	}
	
	public Integer[] getMapCompleteIdArray() {
		Set<Integer> set = this.getMapCompleteIdSet();
		return set.toArray(new Integer[set.size()]);
	}
	
	/**
	 * 更新完成任务信息
	 */
	public void updateMapCompleteIdSet() {
		StringBuilder builder = new StringBuilder();
		for (Integer taskId : getMapCompleteIdSet()) {
			builder.append(taskId).append(Splitable.ATTRIBUTE_SPLIT);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		this.mapTaskCompletes = builder.toString();
	}

	public boolean isContainTask(MapTaskConfig mapTask) {
		return mapTask == null || this.getMapCompleteIdSet().contains(mapTask.getId());
	}
	
	@Override
	public String toString() {
		return "TaskComplete [id=" + id + ", completes=" + completes + ", mapTaskCompletes="
				+ mapTaskCompletes + "]";
	}
	
}
