package com.yayo.warriors.basedb.adapter;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.BattleCollectConfig;
import com.yayo.warriors.basedb.model.BattlePointConfig;
import com.yayo.warriors.basedb.model.BattleRewardsConfig;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.type.IndexName;

/**
 * 乱武阵营战基础数据接口
 * @author jonsai
 *
 */
@Component
public class BattleFieldService extends ResourceAdapter {
	/** 杀人得荣誉 */
	@Autowired(required = false)
	@Qualifier("BATTLE_FIELD_KILL_HONOR")
	private Integer killHonor = 3;
	
	/** 被杀得荣誉 */
	@Autowired(required = false)
	@Qualifier("BATTLE_FIELD_DEATH_HONOR")
	private Integer deathHonor = 1;
	
	@Override
	public void initialize() {
		
	}
	
	/**
	 * 杀人得荣誉
	 * @return
	 */
	public Integer getKillHonor() {
		return killHonor;
	}

	/**
	 * 被杀得荣誉
	 * @return
	 */
	public Integer getDeathHonor() {
		return deathHonor;
	}
	
	/**
	 * 取得战场据点配置
	 * @param type	0-进入战场出生点，1-据点复活，2-无据点时复活点
	 * @param camp
	 * @return
	 */
	public BattlePointConfig getBattlePointConfig(int type, Camp camp){
		return resourceService.getByUnique(IndexName.BATTLE_FIELD_TYPE_CAMP, BattlePointConfig.class, type, camp.ordinal() );
	}

	/**
	 * 取得阵营采集配置对象
	 * @param camp
	 * @return
	 */
	public BattleCollectConfig getBattleCollectConfig(Camp camp){
		return resourceService.getByUnique(IndexName.BATTLE_FIELD_CAMP_COLLECT, BattleCollectConfig.class, camp.ordinal() );
	}

	/**
	 * 取得阵营战得分奖励和胜利奖励
	 * @param honor		玩家的荣誉
	 * @return
	 */
	public BattleRewardsConfig getBattleHonorRewards(int honor) {
		List<BattleRewardsConfig> rewards = (List<BattleRewardsConfig>)resourceService.listAll(BattleRewardsConfig.class);
		BattleRewardsConfig sReward = null;
		if(rewards != null && rewards.size() > 0){
			BattleRewardsConfig firstRewards = rewards.get(0);
			if(honor < firstRewards.getMinHonor() ){
				return null;
			}
			BattleRewardsConfig lastRewards = rewards.get(rewards.size() - 1);
			if(honor >= lastRewards.getMinHonor() ){
				sReward = lastRewards;
				
			} else {
				for(BattleRewardsConfig reward : rewards){
					if( honor <= reward.getMaxHonor() ){
						sReward = reward;
						break;
					}
				}
				
			}
			
		}
		return sReward;
	}
	
}
