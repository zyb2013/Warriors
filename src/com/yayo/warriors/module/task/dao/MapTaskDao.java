package com.yayo.warriors.module.task.dao;

import java.util.Collection;
import java.util.List;

import com.yayo.common.db.dao.CommonDao;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.task.entity.UserMapTask;

public interface MapTaskDao extends CommonDao {

	List<Long> listUserMapTaskId(long playerId);
	Long getUserMapTask(long playerId, int chain);
	void createMapTask(UserMapTask userMapTask, Collection<UserProps> propsList);
	void removeAll(long playerId);
}
