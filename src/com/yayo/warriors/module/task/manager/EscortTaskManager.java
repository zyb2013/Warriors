package com.yayo.warriors.module.task.manager;

import java.util.List;

import com.yayo.warriors.basedb.model.EscortGharryConfig;
import com.yayo.warriors.basedb.model.EscortTaskConfig;
import com.yayo.warriors.module.server.listener.LoginListener;
import com.yayo.warriors.module.task.entity.UserEscortTask;
import com.yayo.warriors.module.user.entity.PlayerBattle;

public interface EscortTaskManager extends LoginListener {
	int getRandomEscortTaskQuality();
	UserEscortTask getEscortTask(PlayerBattle battle);
	EscortTaskConfig getEscortTaskConfig(int taskId);
	EscortGharryConfig getEscortGharryConfig(int quality);
	List<EscortTaskConfig> getEscortTaskConfig4Camp(int camp);
	boolean isEscortStatus(PlayerBattle battle);
	boolean isRide(PlayerBattle battle);
	int getEscortMount(PlayerBattle battle);
	int getMoveSpeed(PlayerBattle battle);
	int caclPlunderEscortExp(int exp);
	int caclEscortProtectUnplunder(int exp);
	int caclEscortUnprotectPlunder(int exp);
	int caclEscortProtectPlunder(int exp);
}
