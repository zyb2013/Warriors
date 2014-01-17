package com.yayo.warriors.module.task.facade;

import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.bo.ResultObject;


public interface TaskMainFacade {
	ResultObject<int[]> collect(long playerId, int npcId);
	void updateSelectCampTask(long playerId);
	void updateEquipPolishTask(long playerId);
	void updateCompleteInstanceTask(long playerId, int instanceId);
	void updateFightMonsterTask(long playerId, MonsterFightConfig monsterFight);
	void updateEquipAscentStarTask(long playerId, int starLevel);
}
