package com.yayo.warriors.module.task.manager;

import java.util.Collection;
import java.util.List;

import com.yayo.warriors.basedb.model.TaskConfig;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.task.entity.TaskComplete;
import com.yayo.warriors.module.task.entity.UserTask;

public interface TaskManager extends DataRemoveListener {
	TaskConfig getTaskConfig(int taskId);
	List<TaskConfig> getPreviousTask(int taskId);
	UserTask getUserTask(long userTaskId);
	List<Long> listUserTaskId(long playerId);
	void initilaizeCreateTaskIdList(long playerId);
	long getUserTask(long playerId, int chain);
	TaskComplete getTaskComplete(long playerId);
	List<UserTask> listUserTask(long playerId);
	void removeAllTask(long playerId);
	void fastCompleteTask(long playerId, TaskComplete taskComplete);
	UserTask getUserTaskByChain(long playerId, int chain);
	void removeUserTaskCache(long playerId);
	void createUserTask(UserTask userTask, Collection<UserProps> userPropsList);
}
