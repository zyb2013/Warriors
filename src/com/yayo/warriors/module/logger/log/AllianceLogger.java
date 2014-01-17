package com.yayo.warriors.module.logger.log;

import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.rule.AllianceRule;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogPropsID;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.user.entity.Player;

/**
 * 帮派日志
 * @author liuyuhua
 */
public class AllianceLogger extends TraceLog {
	
	/**
	 * 创建帮派日志
	 * @param player        玩家对象
	 * @param name          帮派的名字
	 * @param silver        消耗的银币
	 * @param golden        消耗的金币
	 * @param goodsInfos    物品道具消耗
	 */
	public static void createAlliance(Player player,String name,int golden,int silver,LoggerGoods... goodsInfos){
		TraceLog.log(LogType.ALLIANCE, goodsInfos,
				 Term.valueOf(SOURCE, Source.ALLIANCE_CREATE.getCode()),
			     Term.valueOf(GOLDEN, golden),
			     Term.valueOf(ALLIANCE_NAME, name),
			     Term.valueOf(SILVER, silver),
			     Term.valueOf(PLAYERID, player.getId()),
			     Term.valueOf(PROPS_ID, LogPropsID.ALLIANCE_CREATE_PROPS),
			     Term.valueOf(USE_PROPS_COUNT, AllianceRule.CREATE_ALLIANCE_USE_ITEM_COUNT),
			     Term.valueOf(CURRENT_GOLDEN, player.getGolden()));
	}
	

	/**
	 * 捐献帮派道具
	 * @param alliance      帮派对象
	 * @param playerName    玩家的对象
	 * @param propsId       道具的ID
	 * @param usePropsCount 道具的数量
	 * @param goodsInfos    物品道具消耗
	 */
	public static void donateProps(Player player,Alliance alliance,int propsId,int usePropsCount,LoggerGoods... goodsInfos){
		TraceLog.log(LogType.ALLIANCE, goodsInfos,
				 Term.valueOf(SOURCE, Source.ALLIANCE_DONATE_PROPS.getCode()),
			     Term.valueOf(ALLIANCE_NAME, alliance.getName()),
			     Term.valueOf(CURRENT_ALLIANCE_LEVEL, alliance.getLevel()),
			     Term.valueOf(PLAYERID, player.getId()),
			     Term.valueOf(PLAYER_NAME, player.getName()),
			     Term.valueOf(PROPS_ID,propsId),
			     Term.valueOf(USE_PROPS_COUNT, usePropsCount));
	}

}
