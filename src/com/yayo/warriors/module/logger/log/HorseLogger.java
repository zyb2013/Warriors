package com.yayo.warriors.module.logger.log;

import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.user.entity.Player;

/**
 * 坐骑日志类
 * @author liuyuhua
 */
public class HorseLogger extends TraceLog{
	
	/**
	 * 坐骑升级
	 * @param player              玩家对象
	 * @param beforeHorseLevel    坐骑之前的等级
	 * @param afterHorseLevel     坐骑之后的等级
	 * @param golden              消耗的元宝
	 * @param propsId             道具的ID
	 * @param usePropsCount       道具消耗的数量
	 * @pram  goodsInfos          消耗的道具
	 */
	public static void horseLevelup(Player player,int beforeHorseLevel,int afterHorseLevel, int golden,int propsId,int usePropsCount,LoggerGoods... goodsInfos){
		TraceLog.log(LogType.HORSE, goodsInfos,
				     Term.valueOf(GOLDEN, golden),
				     Term.valueOf(PLAYER_NAME, player.getName()),
				     Term.valueOf(USER_NAME, player.getUserName()),
				     Term.valueOf(SOURCE, Source.HORSE_LEVEL_UP.getCode()),
				     Term.valueOf(PLAYERID, player.getId()),
				     Term.valueOf(CURRENT_GOLDEN, player.getGolden()),
				     Term.valueOf(BEFORE_LEVEL, beforeHorseLevel),
				     Term.valueOf(AFTER_LEVEL, afterHorseLevel),
				     Term.valueOf(PROPS_ID, propsId),
				     Term.valueOf(USE_PROPS_COUNT, usePropsCount));
	}

}
