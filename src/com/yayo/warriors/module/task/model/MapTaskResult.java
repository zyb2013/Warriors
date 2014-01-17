package com.yayo.warriors.module.task.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.task.entity.UserMapTask;
import com.yayo.warriors.module.user.model.UserDomain;

public class MapTaskResult {
	
	private UserDomain userDomain;
	
	private UserMapTask singleTask = null;
	
	private Set<Object> attributes = new HashSet<Object>();

	private Collection<Long> playerIds = new HashSet<Long>(0);

	private List<BackpackEntry> entries = new ArrayList<BackpackEntry>(0);
	
	private Map<Long, UserMapTask> tasks = new HashMap<Long, UserMapTask>(0);

	public Map<Long, UserMapTask> getTasks() {
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
	
	public List<BackpackEntry> getEntries() {
		return entries;
	}

	public Set<Object> getAttributes() {
		return this.attributes;
	}
	
	public UserMapTask getSingleTask() {
		return singleTask;
	}

	public void setSingleTask(UserMapTask singleTask) {
		this.singleTask = singleTask;
	}
	public void addPushUserTask(UserMapTask...userTasks) {
		for (UserMapTask userTask : userTasks) {
			this.tasks.put(userTask.getId(), userTask);
		}
	}

	public UserDomain getUserDomain() {
		return userDomain;
	}

	public void addBackpackEntries(BackpackEntry...entries) {
		for (BackpackEntry backpackEntry: entries) {
			this.entries.add(backpackEntry);
		}
	}

	public void addBackpackEntries(Collection<BackpackEntry> entries) {
		this.entries.addAll(entries);
	}
	
	public void addAttributes(Object...attributes) {
		for (Object attribute : attributes) {
			this.attributes.add(attribute);
		}
	}
	public static MapTaskResult valueOf(UserDomain userDomain) {
		MapTaskResult mapTaskResult = new MapTaskResult();
		mapTaskResult.userDomain = userDomain;
		return mapTaskResult;
	}
	
	@Override
	public String toString() {
		return "TaskResult [singleTask=" + singleTask + ", attributes=" + attributes
			 + ", playerIds=" + playerIds + ", tasks=" + tasks + ", entries=" + entries + "]";
	}
	
	
}
