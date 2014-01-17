package com.yayo.warriors.module.task.manager;

import com.yayo.warriors.basedb.model.CampTaskConfig;
import com.yayo.warriors.module.task.entity.UserCampTask;

public interface CampTaskManager {
	UserCampTask getUserCampTask(long playerId);
	CampTaskConfig getCampTaskConfig(int taskId);

}
