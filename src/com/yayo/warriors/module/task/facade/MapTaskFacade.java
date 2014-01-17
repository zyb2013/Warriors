package com.yayo.warriors.module.task.facade;

import java.util.Collection;
import java.util.List;

import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.task.entity.UserMapTask;
import com.yayo.warriors.module.task.entity.UserTask;
import com.yayo.warriors.module.task.model.MapTaskResult;
import com.yayo.warriors.module.task.model.MapTaskRewardResult;

public interface MapTaskFacade {
	
	UserMapTask getUserMapTask(long userTaskId);
	ResultObject<UserMapTask> completeTalkTask(long playerId, int npcId, long userTaskId);
	Collection<UserMapTask> listFilterUserMapTask(long playerId, int...status);
	int collectProps(long playerId, int baseId);
	List<Integer> getTaskCompleteIds(long playerId);
	ResultObject<MapTaskResult> accept(long playerId, int taskId);
	ResultObject<MapTaskResult> cancel(long playerId, long userTaskId);
	ResultObject<MapTaskRewardResult> rewards(long playerId, long userTaskId);
	ResultObject<MapTaskResult> complete(long playerId, long userTaskId);
	void updateSelectCampTask(long playerId);
	void updateEquipPolishTask(long playerId);
	void updateInstanceTask(long playerId, int instanceId);
	void updateEquipStarTask(long playerId, int starLevel);
	void updateFightMonsterTask(long playerId, int monsterId);
	void updateFightCollectTask(long playerId, MonsterFightConfig monsterFight);

}
