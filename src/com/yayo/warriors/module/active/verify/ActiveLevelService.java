package com.yayo.warriors.module.active.verify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.basedb.model.ActiveOperatorLevelConfig;
import com.yayo.warriors.module.active.rule.ActiveLevelType;
import com.yayo.warriors.module.active.rule.ActiveRewardObject;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.alliance.types.Title;
import com.yayo.warriors.module.horse.entity.Horse;
import com.yayo.warriors.module.horse.manager.HorseManager;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;

@Component
public class ActiveLevelService {
	
	@Autowired
	private HorseManager horseManager;
	
	@Autowired
	private AllianceManager allianceManager;
	
	/**
	 * 是否满足条件
	 * @param userDomain         玩家域对象
	 * @param levelConfig        冲级配置
	 * @return {@link Boolean}   true 满足条件 false 不满足条件  
	 */
	public boolean isCondition(UserDomain userDomain, ActiveOperatorLevelConfig levelConfig){
		if(userDomain == null || levelConfig == null){
			return false;
		}
		
		int type = levelConfig.getType();//类型
		int rewardObject = levelConfig.getRewardObject();//奖励对象
		int condition = levelConfig.getCondition();//条件
		
		boolean falg = false;//标记
		
		switch(type){
		
		case ActiveLevelType.ALLIANCE_LEVEL : falg = levelAllianceVerfi(userDomain,rewardObject,condition); break;
		case ActiveLevelType.HORSE_LEVEL : falg = levelHorseVerfi(userDomain,condition); break;
		default: falg = false; break;
		}
		
		return falg;
	}
	
	/**
	 * 冲级帮派验证
	 * @param userDomain
	 * @param rewardObject
	 * @param condition
	 * @return
	 */
	private boolean levelAllianceVerfi(UserDomain userDomain,int rewardObject , int condition){
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null || !playerAlliance.isExistAlliance()){
			return false;
		}
		
		if(rewardObject == ActiveRewardObject.ALLIANCE_MASTER){ //判断所有人是否是帮主
			if(playerAlliance.getTitle() != Title.MASTER){
				return false;
			}
		}else if(rewardObject == ActiveRewardObject.ALLIANCE_MEMBER){
			if(playerAlliance.getTitle() == Title.MASTER){
				return false;
			}
		}
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.getLevel() < condition){
			return false;
		}
		
		Long levelTime = alliance.getLevelupRecords().get(condition);
		if(levelTime == null){
			return false;
		}
		if(playerAlliance.getJiontime() > levelTime){
			return false;
		}
		
		return true;
	}
	
	/**
	 * 冲级坐骑验证
	 * @param userDomain
	 * @param condition
	 * @return
	 */
	private boolean levelHorseVerfi(UserDomain userDomain, int condition){
		PlayerBattle battle = userDomain.getBattle();
		Horse horse = horseManager.getHorse(battle);
		if(horse == null){
			return false;
		}
		
		if(horse.getLevel() < condition){
			return false;
		}
		return true;
	}
	

}
