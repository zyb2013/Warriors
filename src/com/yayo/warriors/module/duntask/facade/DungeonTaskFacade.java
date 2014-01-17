package com.yayo.warriors.module.duntask.facade;

import java.util.Collection;
import com.yayo.warriors.module.duntask.model.DunTask;

/**
 * 副本任务接口 
 * @author liuyuhua
 */
public interface DungeonTaskFacade {
	
	/**
	 * 接受副本任务
	 * @param playerId        玩家的ID
	 * @param taskBaseIds      副本任务基础ID
	 */
	void accept(long playerId,Collection<Integer> taskBaseIds);
	
	/**
	 * 获取玩家副本任务
	 * @param playerId        玩家的ID
	 * @return {@link Collection<DunTask>} 副本任务集合
	 */
	Collection<DunTask> getAllDunTask(long playerId);
	
	/**
	 * 领取任务奖励
	 * 
	 * @param  playerId        	玩家的ID
	 * @param  taskId          	副本任务的ID
	 * @return {@link Integer}	副本任务返回值
	 */
	int submit(long playerId,long taskId);
	
}
