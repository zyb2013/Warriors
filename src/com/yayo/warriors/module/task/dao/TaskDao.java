package com.yayo.warriors.module.task.dao;

import java.util.Collection;
import java.util.List;

import com.yayo.common.db.dao.CommonDao;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.task.entity.UserTask;

public interface TaskDao extends CommonDao {
	List<Long> listUseraTaskId(long playerId);
	Long getUserTaskId(long playerId, int chain);
	void createTask(UserTask userTask, Collection<UserProps> propsList);
	void removeAll(long playerId);
	List<UserTask> listUserTask(int chain, int taskId);
	void delete(Collection<UserTask> userTasks);
}
