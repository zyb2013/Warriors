package com.yayo.warriors.module.task.manager;

import java.util.Collection;

import com.yayo.warriors.basedb.model.LoopRewardConfig;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.task.entity.UserLoopTask;

public interface LoopTaskManager extends DataRemoveListener {
	int getRandomLoopTaskQuality();
	UserLoopTask getUserLoopTask(long playerId);
	LoopRewardConfig getLoopRewardConfig(int loopRewardId);
	Collection<LoopRewardConfig> listCanLoopRewardConfig();
}
