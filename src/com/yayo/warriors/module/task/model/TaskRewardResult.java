package com.yayo.warriors.module.task.model;

import com.yayo.warriors.module.task.entity.UserTask;

public class TaskRewardResult {

	private int level;
	
	private UserTask userTask;

	public int getLevel() {
		return level;
	}

	public UserTask getUserTask() {
		return userTask;
	}
	
	public static TaskRewardResult valueOf(int level, UserTask userTask) {
		TaskRewardResult taskRewardResult = new TaskRewardResult();
		taskRewardResult.level = level;
		taskRewardResult.userTask = userTask;
		return taskRewardResult;
	}
}
