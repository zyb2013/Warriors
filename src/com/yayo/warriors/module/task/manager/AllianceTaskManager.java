package com.yayo.warriors.module.task.manager;

import java.util.List;

import com.yayo.warriors.basedb.model.AllianceTaskConfig;
import com.yayo.warriors.module.task.entity.UserAllianceTask;
import com.yayo.warriors.module.task.model.AllianceTask;
import com.yayo.warriors.module.user.entity.PlayerBattle;


public interface AllianceTaskManager {
	
	UserAllianceTask getUserAllianceTask(PlayerBattle battle);
	List<AllianceTaskConfig> getAllianceTaskConfigs();
	AllianceTaskConfig getAllianceTaskConfig(int taskId);
	AllianceTask buildAllianceTask(PlayerBattle battle, UserAllianceTask userAllianceTask, AllianceTaskConfig config);

}
