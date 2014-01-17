package com.yayo.warriors.module.active.verify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.yayo.warriors.basedb.model.ActiveOperatorRankConfig;
import com.yayo.warriors.module.active.entity.OperatorActive;
import com.yayo.warriors.module.active.rule.ActiveRankType;
import com.yayo.warriors.module.rank.manager.RankManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 运营活动排行 验证服务
 * @author liuyuhua
 */
@Component
public class ActiveRankService {
	
	@Autowired
	private RankManager rankManager;
	
	/**
	 * 是否满足条件
	 * @param userDomain        玩家域对象
	 * @param rankConfig        排行配置
	 * @oaram active            运营活动
	 * @return {@link Boolean}  true 满足条件 false 不满足条件  
	 */
	public boolean isCondition(UserDomain userDomain, ActiveOperatorRankConfig rankConfig,OperatorActive active){
		if(userDomain == null || rankConfig == null){
			return false;
		}
		
		int type = rankConfig.getType();//类型
		int region = rankConfig.getRegion();//范围(全服,阵营) TODO 当前没有用到 阵营 排行,都是全服排行
		int ranking = rankConfig.getRanking();//名次
		
		long currentTime = System.currentTimeMillis();
		if(active.getEndTime() > currentTime){ //所有排行类型的是否可以领取的判断,都需要在活动结束后且在领取结束前才能计算
			return false;
		}
		
		
		boolean falg = false;//标记
		
		switch(type){
		
		case ActiveRankType.ROLE_TYPE       : falg = this.rankRoleVerfi(userDomain, region, ranking); break;
		case ActiveRankType.HORSE_TYPE      : falg = this.rankHorseVerfi(userDomain,region,ranking);  break;
		case ActiveRankType.ROLE_FIGHT_TYPE : falg = this.rankRoleFightVerfi(userDomain, region, ranking); break;
		case ActiveRankType.PET_FIGHT_TYPE  : falg = this.rankPetFightVerfi(userDomain, region, ranking); break;
		default: falg = false; break;
		}
		
		return falg;
	}
	
	/**
	 * 家将战斗力排行
	 * @param userDomain
	 * @param region
	 * @param ranking
	 * @return
	 */
	private boolean rankPetFightVerfi(UserDomain userDomain,int region,int ranking){
		long playerId = userDomain.getId();
		int[] title = rankManager.getRankTitleByPlayerId(playerId);
		if(title == null){
			return false;
		}
		
		if(title[7] != ranking){ //战斗力排行
			return false;
		}
		return true;
	}
	
	
	/**
	 * 角色战斗力排行
	 * @param userDomain
	 * @param region
	 * @param ranking
	 * @return
	 */
	private boolean rankRoleFightVerfi(UserDomain userDomain,int region,int ranking){
		long playerId = userDomain.getId();
		int[] title = rankManager.getRankTitleByPlayerId(playerId);
		if(title == null){
			return false;
		}
		
		if(title[2] != ranking){ //战斗力排行
			return false;
		}
		return true;
	}
	
	
	/**
	 * 玩家等级排行验证
	 * @param userDomain  玩家的域对象
	 * @param region      范围
	 * @param ranking     名次
	 * @return {@link Boolean} true 达成条件  false 未达成条件
	 */
	private boolean rankRoleVerfi(UserDomain userDomain,int region,int ranking){
		long playerId = userDomain.getId();
		int[] title = rankManager.getRankTitleByPlayerId(playerId);
		if(title == null){
			return false;
		}
		
		if(title[1] != ranking){ //等级排行
			return false;
		}
		return true;
	}
	
	/**
	 * 坐骑排行验证
	 * @param userDomain  玩家的域对象
	 * @param region      范围
	 * @param ranking     名次
	 * @return {@link Boolean} true 达成条件  false 未达成条件
	 */
	private boolean rankHorseVerfi(UserDomain userDomain,int region,int ranking){
		long playerId = userDomain.getId();
		int[] title = rankManager.getRankTitleByPlayerId(playerId);
		if(title == null){
			return false;
		}
		
		if(title[9] != ranking){ //坐骑排行
			return false;
		}
		return true;
	}

}
