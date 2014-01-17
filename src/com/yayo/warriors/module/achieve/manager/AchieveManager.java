package com.yayo.warriors.module.achieve.manager;

import java.util.List;

import com.yayo.warriors.basedb.model.AchieveConfig;
import com.yayo.warriors.module.achieve.entity.UserAchieve;
import com.yayo.warriors.module.server.listener.DataRemoveListener;

public interface AchieveManager extends DataRemoveListener {
	AchieveConfig getAchieveConfig(int achieveId);
	List<AchieveConfig> listAchieveConfigs(int achieveType);
	List<Integer> listAchieveIds(int achieveType);
	UserAchieve getUserAchieve(long playerId);
	
}
