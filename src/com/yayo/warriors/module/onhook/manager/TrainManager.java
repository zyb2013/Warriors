package com.yayo.warriors.module.onhook.manager;

import com.yayo.warriors.module.onhook.entity.UserTrain;
import com.yayo.warriors.module.onhook.model.UserSingleTrain;
import com.yayo.warriors.module.server.listener.DataRemoveListener;

/**
 * 挂机管理接口
 * 
 * @author Hyint
 */
public interface TrainManager extends DataRemoveListener {

	/**
	 * 查询玩家闭关实体
	 * 
	 * @param  playerId				角色ID
	 * @return {@link UserTrain}	用户闭关实体
	 */
	UserTrain getUserTrain(long playerId);
	
	/**
	 * 获得玩家打坐状态
	 * 
	 * @param playerId
	 * @return
	 */
	UserSingleTrain getUserSingleTrain(long playerId);
	
	
	
	/**
	 * 移除打坐缓存
	 * 
	 * @param playerId
	 */
	void removeSingleTrainCache(long playerId);
}
