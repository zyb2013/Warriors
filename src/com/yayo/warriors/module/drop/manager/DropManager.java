package com.yayo.warriors.module.drop.manager;

import java.util.List;

import com.yayo.warriors.basedb.model.DropConfig;
import com.yayo.warriors.module.drop.model.DropRewards;

/**
 * 掉落管理接口
 * 
 * @author Hyint
 */
public interface DropManager {

	/**
	 * 查询奖励配置对象
	 * 
	 * @param  rewardId					奖励信息
	 * @return {@link DropConfig}		奖励配置对象
	 */
	DropConfig getDropConfig(int rewardId);
	
	/**
	 * 查询奖励配置对象
	 * 
	 * @param  rewardNo					奖励信息
	 * @return {@link DropConfig}		奖励配置对象
	 */
	DropConfig getDefaultDropConfig(int rewardNo);
	
	/**
	 * 掉落奖励
	 * 
	 * @param  playerId					角色ID
	 * @param  rewardNo					奖励ID
	 * @param  count					掉落数量
	 * @return {@link List}				掉落奖励列表
	 */
	List<DropRewards> dropRewards(long playerId, int rewardNo, int count);
}
