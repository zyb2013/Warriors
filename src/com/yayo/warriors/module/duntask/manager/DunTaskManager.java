package com.yayo.warriors.module.duntask.manager;

import java.util.Collection;
import java.util.List;

import com.yayo.warriors.basedb.model.DungeonTaskConfig;
import com.yayo.warriors.module.duntask.model.DunTask;
import com.yayo.warriors.module.duntask.types.TaskTypes;

/**
 * 副本Manager接口 
 * @author liuyuhua
 */
public interface DunTaskManager {

	/**
	 * 接受副本任务
	 * <per>因为接受任务,是由于副本系统内部调用,所以不设计对外</per>
	 * @param playerId      玩家的ID
	 * @param taskBaseIds   副本基础任务集合
	 * @return {@link List<DunTask>} 副本任务对象集合
	 */
	List<DunTask> accpet(long playerId,Collection<Integer> taskBaseIds);
	
	/**
	 * 通过ID,获取副本任务对象
	 * @param playerId     玩家的ID
	 * @param taskId       任务的增量ID
	 * @return {@link DunTask} 副本任务对象
	 */
	DunTask getDunTask(long playerId,long taskId);
	
	/**
	 * 获取玩家副本任务
	 * @param playerId        玩家的ID
	 * @return {@link Collection<DunTask>} 副本任务集合
	 */
	 Collection<DunTask> getAllDunTask(long playerId);
	
	/**
	 * 删除副本任务
	 * @param playerId    玩家的ID
	 * @param taskId      任务的增量ID
	 * @return true 成功 false 失败
	 */
	boolean remove(long playerId,long taskId);
	
	/**
	 * 删除所有任务
	 * @param playerId     玩家的ID
	 */
	void removeAll(long playerId);
	
	/**
	 * 更新进度
	 * @param playerId        玩家的ID
	 * @param condition       条件
	 * @param type            类型
	 */
	 void updateProgress(long playerId,int condition,TaskTypes type);
	 
	 /**
	  * 获取副本任务配置
	  * @param taskBaseId   副本任务基础ID
	  * @return {@link DungeonTaskConfig} 副本任务配置对象
	  */
	 DungeonTaskConfig getDungeonTaskConfig(int taskBaseId);
		
    /**
	 * 获取所有副本任务配置
	 * @return {@link Collection<DungeonTaskConfig>} 副本配置对象集合
	 */ 
	 Collection<DungeonTaskConfig> getAllDungeonConfig();
}
