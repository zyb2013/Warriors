package com.yayo.warriors.module.task.facade;

import java.util.Map;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.task.entity.UserEscortTask;

public interface EscortTaskFacade {
	
	ResultObject<UserEscortTask> loadUserEscortTask(long playerId,boolean flushable);
	ResultObject<UserEscortTask> accept(long playerId,int taskId,long propsId,boolean autoBuy);
	ResultObject<UserEscortTask> giveup(long playerId);
	ResultObject<UserEscortTask> updateEscortTask(long playerId,int npcId);
	void updatePlayerPlunderEscort(long playerId,long targetId);
	void updateMonsterPlunderEscort(long monsterId,long targetId);
	ResultObject<UserEscortTask> completeAndreward(long playerId,int npcId);
	ResultObject<UserEscortTask> refreshOrange(long playerId,String userItem,int autoBuyCount);
	ResultObject<UserEscortTask> refreshRandQuality(long playerId,String userItem,int autoBuyCount);
	ResultObject<Map<String,Object>> refreshTaskQuality(long playerId, int targetQuality, int refreshCount, boolean autoBuyBook);
}
