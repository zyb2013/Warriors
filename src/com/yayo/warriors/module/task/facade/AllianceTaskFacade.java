package com.yayo.warriors.module.task.facade;

import java.util.List;
import java.util.Map;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.task.model.AllianceTask;
import com.yayo.warriors.module.user.type.Camp;


public interface AllianceTaskFacade {

	ResultObject<AllianceTask> acceptAllianceTask(long playerId,int allianceTaskId);
	List<AllianceTask> getAllianceTasks(long playerId);
	ResultObject<Map<String, Object>> rewards(long playerId, int taskId);
	void updateFightPlayer(long playerId,Camp camp);
	void updatePlunderEscort(long playerId);
	void updateTreaders(long playerId);
	void updateKillMonster(long playerId,int monsterBaseId);
	void updateDonate(long playerId);
	ResultObject<AllianceTask> completeTalkTask(long playerId, int npcId, int taskId);
	int collectProps(long playerId, int baseId);
	int cancel(long playerId, int taskId);
	int complete(long playerId, int taskId);
	String getUserAllianceTaskProgress(long playerId);
	
}
