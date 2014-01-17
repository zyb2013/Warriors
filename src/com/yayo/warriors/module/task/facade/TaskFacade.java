package com.yayo.warriors.module.task.facade;

import java.util.Collection;
import java.util.List;

import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.task.entity.UserTask;
import com.yayo.warriors.module.task.model.TaskResult;
import com.yayo.warriors.module.task.model.TaskRewardResult;


public interface TaskFacade {
	
	UserTask getUserTask(long userTaskId);
	ResultObject<UserTask> completeTalkTask(long playerId, int npcId, long userTaskId);
	Collection<UserTask> listFilterUserTask(long playerId, int...status);
	int collectProps(long playerId, int baseId);
	List<Integer> getTaskCompleteIds(long playerId);
	ResultObject<TaskResult> accept(long playerId, int taskId);
	ResultObject<TaskResult> cancel(long playerId, long userTaskId);
	ResultObject<TaskRewardResult> rewards(long playerId, long userTaskId);
	ResultObject<TaskResult> complete(long playerId, long userTaskId);
	void updateSelectCampTask(long playerId);
	void updateEquipPolishTask(long playerId);
	void updateInstanceTask(long playerId, int instanceId);
	void updateEquipStarTask(long playerId, int starLevel);
	void updateFightMonsterTask(long playerId, int monsterId);
	void updateFightCollectTask(long playerId, MonsterFightConfig monsterFight);
	boolean updatePetGrowLevelTask(long playerId); 
	boolean updateLettoryTask(long playerId, int count);
	boolean updateAddFriendTask(long playerId);
	boolean updateEnchanceEquipTask(long playerId);
	boolean updateJoinAllianceTask(long playerId);
	boolean updateRushMeridianTask(long playerId);
	boolean updateMortalLevelUpTask(long playerId);
	boolean updatePetSavvyLevelTask(long playerId);
	boolean updateHorseLevelTask(long playerId);
	boolean updateHorseSeniorFancyTask(long playerId, int count);
	boolean updateBuyMallItemTask(long playerId, int propsId, int amount);
}
