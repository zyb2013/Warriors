package com.yayo.warriors.basedb.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.DropConfig;

/**
 * 掉落奖励适配器 
 * 
 * @author Hyint 
 */
@Component
public class DropService extends ResourceAdapter {
	
	@Override
	public void initialize() {
		Collection<DropConfig> dropConfigs = resourceService.listAll(DropConfig.class);
		for (DropConfig dropConfig : dropConfigs) {
			dropConfig.getDropInfoList();
			if(!dropConfig.isAcquiesce()) {
				continue;
			}
			
			int rewardNo = dropConfig.getRewardNo();
			DropConfig existDrop = resourceService.getByUnique(String.valueOf(rewardNo), DropConfig.class);
			if(existDrop != null) {
				throw new RuntimeException(String.format("掉落编号:[%d] 默认掉落重复了, 请检查掉落表", rewardNo));
			}
			resourceService.addToIndex(String.valueOf(rewardNo), dropConfig.getId(), DropConfig.class);
		}
	}

	/**
	 * 根据奖励ID查询奖励对象
	 * 
	 * @param  rewardId				掉落ID
	 * @return {@link DropConfig}	掉落对象
	 */
	public DropConfig getRewardConfig(int rewardId) {
		return resourceService.get(rewardId, DropConfig.class);
	}

	/**
	 * 根据任意一个奖励ID获得默认掉落
	 * 
	 * @param  rewardId				默认奖励ID
	 * @return {@link DropConfig}	掉落基础信息
	 */
	public DropConfig getDefaultReward(int rewardId) {
		DropConfig defaultReward = null;
		DropConfig rewardConfig = getRewardConfig(rewardId);
		if(rewardConfig != null) {
			defaultReward = getDefaultByRewardNo(rewardConfig.getRewardNo());
		}
		return defaultReward;
	}
	
	/**
	 * 查询默认奖励编号
	 * 
	 * @param  rewardNo				奖励编号
	 * @return {@link DropConfig}	掉落奖励对象
	 */
	public DropConfig getDefaultByRewardNo(int rewardNo) {
		return resourceService.getByUnique(String.valueOf(rewardNo), DropConfig.class);
	}
	
	/**
	 * 列出奖励列表
	 * 
	 * @param  rewardNo			奖励编号
	 * @return {@link List}		奖励列表
	 */
	public List<DropConfig> listRewardConfig(int rewardNo) {
		return new ArrayList<DropConfig>(resourceService.listByIndex(DropConfig.REWARD_NO, DropConfig.class, rewardNo));
	}
}
