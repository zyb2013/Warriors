package com.yayo.warriors.module.task.facade;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.task.model.QualityResult;
import com.yayo.warriors.module.task.vo.LoopTaskVo;
import com.yayo.warriors.module.task.vo.PracticeTaskVO;

/**
 * 试练任务接口
 * 
 * @author Hyint
 */
public interface PracticeTaskFacade {
	
	/**
	 * 获得试练任务对象
	 * 
	 * @param   playerId				角色ID
	 * @return  {@link PracticeTaskVO}	用户日环任务
	 */
	PracticeTaskVO refreshPracticeTask(long playerId);
	
	/**
	 * 接受试练任务
	 * 
	 * @param  playerId					角色ID
	 * @return {@link LoopTaskVo}		任务模块返回值
	 */
	ResultObject<PracticeTaskVO> accept(long playerId);

	/**
	 * 领取试练任务奖励
	 * 
	 * @param  playerId					角色ID
	 * @return {@link ResultObject}		用户试练任务
	 */
	ResultObject<PracticeTaskVO> rewardUserPracticeTask(long playerId);
	
	/**
	 * 更新战斗试练任务
	 * 
	 * @param playerId					角色ID
	 * @param monsterId					怪物ID
	 */
	void updateFightPracticeTask(long playerId, int monsterId);
	
	/**
	 * 放弃试练任务
	 * 
	 * @param  playerId					角色ID
	 * @return {@link Integer}			返回值信息
	 */
	int giveUpPracticeTask(long playerId);
	
	/**
	 * 获得奖励信息对象
	 * 
	 * @param  playerId					角色ID
	 * @param  completeTimes			完成次数
	 * @return {@link ResultObject}		任务模块返回值
	 */
	ResultObject<PracticeTaskVO> getReward(long playerId, int completeTimes);
	
	/**
	 * 刷新试练任务的品质..
	 * 
	 * @param  playerId					角色ID
	 * @param  quality					任务品质
	 * @param  refreshCount				刷新次数
	 * @param  autoBuyBook				自动购买书籍
	 * @return {@link QualityResult}	返回值对象
	 */
	QualityResult<PracticeTaskVO> refreshQuality(long playerId, int quality, int refreshCount, boolean autoBuyBook);

	/**
	 * 快速完成试练任务
	 * 
	 * @param  playerId					角色ID
	 * @param  auto						是否自动购买. true-自动购买, false-不自动购买
	 * @return {@link ResultObject}		任务返回值对象
	 */
	ResultObject<PracticeTaskVO> fastCompletePracticeTask(long playerId, boolean auto);
}
