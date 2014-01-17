package com.yayo.warriors.module.task.facade;

import java.util.Collection;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.task.entity.UserLoopTask;
import com.yayo.warriors.module.task.model.QualityResult;
import com.yayo.warriors.module.task.vo.LoopTaskVo;

public interface LoopTaskFacade {
	LoopTaskVo refreshUserLoopTask(long playerId);
	ResultObject<LoopTaskVo> accept(long playerId);
	ResultObject<LoopTaskVo> fastComplete(long playerId);
	ResultObject<Collection<BackpackEntry>> complete(long playerId);
	ResultObject<LoopTaskVo> rewardsUserLoopTask(long playerId);
	ResultObject<LoopTaskVo> completeTalkTask(long playerId, int npcId);
	void updateFightLootTask(long playerId, int monsterId);
	QualityResult<LoopTaskVo> refreshTaskQuality(long playerId, int refreshCount, boolean autoBuyBook);
	int giveUpTask(long playerId);
	ResultObject<LoopTaskVo> getReward(long playerId, int completeTimes);
}
