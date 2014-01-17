package com.yayo.warriors.module.task.model;

import com.yayo.warriors.module.task.entity.UserMapTask;

public class MapTaskRewardResult {

	private int level;
	
	private UserMapTask userTask;

	public int getLevel() {
		return level;
	}

	public UserMapTask getUserMapTask() {
		return userTask;
	}
	
	public static MapTaskRewardResult valueOf(int level, UserMapTask userTask) {
		MapTaskRewardResult taskRewardResult = new MapTaskRewardResult();
		taskRewardResult.level = level;
		taskRewardResult.userTask = userTask;
		return taskRewardResult;
	}
}
