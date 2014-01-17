package com.yayo.warriors.basedb.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.warriors.basedb.model.ActiveOnlineConfig;
import com.yayo.warriors.module.active.manager.ActiveMonsterManager;
import com.yayo.warriors.module.active.rule.ActiveOnlineType;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.type.IndexName;

/**
 * 在线活动服务
 * @author liuyuhua
 */
@Component
public class OnlineActiveService extends ResourceAdapter{
	
	@Autowired
	private ActiveMonsterManager activeMonsterManager;
	
	/**
	 * 处理在线玩法
	 */
	public void processActiveRuleScheduling(){
		activeMonsterExpRule();
		activeMonsterWarpRule();
	}
	
	/**
	 * 活动围城怪物玩法
	 */
	private void activeMonsterWarpRule(){
		List<ActiveOnlineConfig> configs = resourceService.listByIndex(IndexName.ACTIVE_ONLINE_TYPE, 
                                                                       ActiveOnlineConfig.class, 
                                                                       ActiveOnlineType.MONSTER_WARP_RULE_TYPE, 0);
		
		if(configs == null || configs.isEmpty()){
			return;
		}
		
		for(ActiveOnlineConfig config : configs){
			if(config.isOpen()){
				activeMonsterManager.activeMonsterWrapRule();
			}else{
				activeMonsterManager.clearMonsterWrapRule();
			}
		}
	}
	
	
	/**
	 * 活动怪物经验玩法
	 */
	private void activeMonsterExpRule(){
		List<ActiveOnlineConfig> configs = resourceService.listByIndex(IndexName.ACTIVE_ONLINE_TYPE, 
                                                                       ActiveOnlineConfig.class, 
                                                                       ActiveOnlineType.MONSTER_EXP_RULE_TYPE, 0);
		
		if(configs == null || configs.isEmpty()){
			return;
		}
		
		for(ActiveOnlineConfig config : configs){
			if(config.isOpen()){
				activeMonsterManager.activeMonsterExpRule();
			}
		}
	}
	
	
	/**
	 * 获取活动,怪物的经验收益加成
	 * @param userDomain     玩家对象
	 * @return {@link Float} 收益
	 */
	public float getMonsterProfit(UserDomain userDomain){
		if(userDomain == null){
			return 0f;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		List<ActiveOnlineConfig> configs = resourceService.listByIndex(IndexName.ACTIVE_ONLINE_TYPE, 
				                                                       ActiveOnlineConfig.class, 
				                                                       ActiveOnlineType.MONSTER_TYPE, 0);
		if(configs == null || configs.isEmpty()){
			return 0f;
		}
		
		boolean falg = false;
		for(ActiveOnlineConfig config : configs){
			if(config != null && config.isOpen()){
				falg = true;
			}
			
			if(falg){
				if(battle.getLevel() < config.getLevel()){
					return 0f;
				}
				return config.getProfit();
			}
		}
		
		return 0f;
	}
	
	/**
	 * 获取活动,打坐的经验收益加成
	 * @param userDomain       玩家对象
	 * @return {@link Float}   收益
	 */
	public float getTrainProfit(UserDomain userDomain){
		if(userDomain == null){
			return 0f;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		
		List<ActiveOnlineConfig> configs = resourceService.listByIndex(IndexName.ACTIVE_ONLINE_TYPE, 
				                                                       ActiveOnlineConfig.class, 
				                                                       ActiveOnlineType.TRAIN_TYPE, 0);
		if(configs == null || configs.isEmpty()){
			return 0f;
		}
		
		boolean falg = false;
		for(ActiveOnlineConfig config : configs){
			if(config != null && config.isOpen()){
				falg = true;
			}
			
			if(falg){
				if(battle.getLevel() < config.getLevel()){
					return 0f;
				}
				return config.getProfit();
			}
		}
		
		return 0f;
	}
	
	/**
	 * 获取押镖经验收益
	 * @param userDomain       玩家对象
	 * @return {@link Float}   收益
	 */
	public float getEscortProfit(UserDomain userDomain){
		if(userDomain == null){
			return 0f;
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		List<ActiveOnlineConfig> configs = resourceService.listByIndex(IndexName.ACTIVE_ONLINE_TYPE, 
				                                                       ActiveOnlineConfig.class, 
				                                                       ActiveOnlineType.ESCORT_TYPE, 
				                                                       player.getCamp().ordinal());
		if(configs == null || configs.isEmpty()){
			return 0f;
		}
		
		boolean falg = false;
		for(ActiveOnlineConfig config : configs){
			if(config != null && config.isOpen()){
				falg = true;
			}
			
			if(falg){
				if(battle.getLevel() < config.getLevel()){
					return 0f;
				}
				return config.getProfit();
			}
		}
		
		return 0f;
	}
	
	

	@Override
	public void initialize() {
	}

}
