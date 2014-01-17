package com.yayo.warriors.module.task.manager;

import com.yayo.warriors.basedb.model.PracticeRewardConfig;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.task.entity.UserPracticeTask;
import com.yayo.warriors.module.user.model.UserDomain;

public interface PracticeTaskManager extends DataRemoveListener {
	UserPracticeTask getPracticeTask(UserDomain userDomain);
	PracticeRewardConfig getPracticeRewardConfig(int completes);
	int getRandomPracticeTaskQuality();
}
