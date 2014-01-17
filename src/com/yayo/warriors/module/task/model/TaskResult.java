package com.yayo.warriors.module.task.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.task.entity.UserTask;
import com.yayo.warriors.module.user.model.UserDomain;

public class TaskResult {
	
	private UserDomain userDomain;
	
	private UserTask singleTask = null;
	
	private Set<Object> attributes = new HashSet<Object>(0);

	private Collection<Long> playerIds = new HashSet<Long>(0);
	
	private Map<Long, UserTask> tasks = new HashMap<Long, UserTask>(0);

	private List<BackpackEntry> entries = new ArrayList<BackpackEntry>(0);

	public Map<Long, UserTask> getTasks() {
		return tasks;
	}
	
	public Collection<Long> getPlayerIds() {
		return this.playerIds;
	}

	public void addPlayerIds(Long...playerIds) {
		for (Long playerId : playerIds) {
			this.playerIds.add(playerId);
		}
	}
	
	public UserDomain getUserDomain() {
		return userDomain;
	}

	public List<BackpackEntry> getEntries() {
		return entries;
	}

	public Set<Object> getAttributes() {
		return this.attributes;
	}
	
	public UserTask getSingleTask() {
		return singleTask;
	}

	public void setSingleTask(UserTask singleTask) {
		this.singleTask = singleTask;
	}

	/**
	 * 增加需要推送的任务信息
	 * 
	 * @param  tasks		任务数组
	 */
	public void addPushUserTask(UserTask...userTasks) {
		for (UserTask userTask : userTasks) {
			this.tasks.put(userTask.getId(), userTask);
		}
	}

	
	/**
	 * 增加背包实体信息
	 * 
	 * @param entries		背包实体数组
	 */
	public void addBackpackEntries(BackpackEntry...entries) {
		for (BackpackEntry backpackEntry: entries) {
			this.entries.add(backpackEntry);
		}
	}

	/**
	 * 增加背包实体信息
	 * 
	 * @param entries		背包实体列表
	 */
	public void addBackpackEntries(Collection<BackpackEntry> entries) {
		this.entries.addAll(entries);
	}
	
	public void addAttributes(Object...attributes) {
		for (Object attribute : attributes) {
			this.attributes.add(attribute);
		}
	}
	
	/**
	 * 构建任务返回值
	 * 
	 * @param  userDomain			用户域模型对象		
	 * @return {@link TaskResult}	任务返回值
	 */
	public static TaskResult valueOf(UserDomain userDomain) {
		TaskResult taskResult = new TaskResult();
		taskResult.userDomain = userDomain;
		return taskResult;
	}
	
	@Override
	public String toString() {
		return "TaskResult [singleTask=" + singleTask + ", attributes=" + attributes
			 + ", playerIds=" + playerIds + ", tasks=" + tasks + ", entries=" + entries + "]";
	}
	
	
}
