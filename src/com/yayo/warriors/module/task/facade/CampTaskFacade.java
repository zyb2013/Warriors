package com.yayo.warriors.module.task.facade;

import java.util.List;
import java.util.Map;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.task.constant.TaskConstant;
import com.yayo.warriors.module.task.model.CampTask;
import com.yayo.warriors.module.user.type.Camp;


public interface CampTaskFacade {
	
	ResultObject<Map<String,Object>> getCampTaskResult(long playerId);
	List<CampTask> getCampTasks(long playerId); 
	ResultObject<CampTask> accept(long playerId, int taskId);
	int cancel(long playerId,int taskId);
	int rewards(long playerId,int taskId);
	ResultObject<CampTask> completeTalkTask(long playerId, int npcId, int campTaskId);
	void updateFightMonsterTask(long playerId, int monsterId);
	void updateFightPlayerTask(long playerId,Camp camp);
	int collectProps(long playerId, int baseId);
	int complete(long playerId,int taskId);
}
