package com.yayo.warriors.module.task.manager;

import java.util.List;

import com.yayo.warriors.basedb.model.MapTaskConfig;
import com.yayo.warriors.module.task.entity.TaskComplete;
import com.yayo.warriors.module.task.entity.UserMapTask;
import com.yayo.warriors.module.task.entity.UserTask;

public interface MapTaskManager {
	MapTaskConfig getMapTaskConfig(int taskId);
	MapTaskConfig getPreviousMapTask(int taskId);
	UserMapTask getUserMapTask(long userTaskId);
	List<Long> listUserMapTaskId(long playerId);
	long getUserMapTask(long playerId, int chain);
	List<UserMapTask> listUserMapTask(long playerId);
	void removeAllTask(long playerId);
	void fastCompleteTask(long playerId, TaskComplete taskComplete);
	UserMapTask getUserMapTaskByChain(long playerId, int chain);
	void removeUserMapTaskCache(long playerId);
	void createUserMapTask(UserMapTask userMapTask);
}
