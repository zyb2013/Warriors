package com.yayo.warriors.module.logger.log;

import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.user.entity.Player;

/**
 * 家将日志
 * @author liuyuhua
 */
public class PetLogger extends TraceLog {

	/**
	 * 开启家将令
	 * @param player        玩家对象
	 * @param propsId       道具基础ID
	 * @param usePropsCount 使用道具数量
	 * @param goodsInfos    消耗道具信息
	 */
	public static void petOpenEgg(Player player,int propsId,int usePropsCount,LoggerGoods... goodsInfos){
		TraceLog.log(LogType.PET, goodsInfos,
				 Term.valueOf(SOURCE, Source.PET_OPE_EGG.getCode()),
			     Term.valueOf(PLAYERID, player.getId()),
			     Term.valueOf(CURRENT_GOLDEN, player.getGolden()));
	}
	
	/**
	 * 家将成长度训练道具消耗
	 * @param player        玩家对象
	 * @param petBattle     家将战斗对象
	 * @param golden        消耗元宝
	 * @param propsId       
	 * @param goodsInfos    消耗道具信息
	 */
	public static void petSavvyTraining(Player player,PetBattle petBattle,int golden,int propsId,int usePropsCount,LoggerGoods... goodsInfos){
		TraceLog.log(LogType.PET, goodsInfos,
				 Term.valueOf(SOURCE, Source.PET_SAVVY_TRAINING.getCode()),
			     Term.valueOf(PLAYERID, player.getId()),
			     Term.valueOf(GOLDEN, golden),
			     Term.valueOf(PROPS_ID, propsId),
			     Term.valueOf(USE_PROPS_COUNT, usePropsCount),
			     Term.valueOf(CURRENT_SAVVY_LEVEL, petBattle.getSavvy()),
			     Term.valueOf(CURRENT_MERGED_LEVEL,petBattle.getMergedLevel()),
			     Term.valueOf(CURRENT_MERGED_BLESS,petBattle.getMergedBless()),
			     Term.valueOf(CURRENT_MERGED_BLESS_PERCENT,petBattle.getMergedBlessPercent()),
			     Term.valueOf(CURRENT_GOLDEN, player.getGolden()));
		
		if(golden != 0){
			GoldLogger.outCome(Source.PET_MERGED_TRAINING, golden, player, goodsInfos);
		}
	}
	
	
	/**
	 * 家将契合度训练道具消耗
	 * @param player        玩家对象
	 * @param petBattle     家将战斗对象
	 * @param golden        消耗元宝
	 * @param propsId       
	 * @param goodsInfos    消耗道具信息
	 */
	public static void petMergedTraining(Player player,PetBattle petBattle,int golden,int propsId,int usePropsCount,LoggerGoods... goodsInfos){
		TraceLog.log(LogType.PET, goodsInfos,
				 Term.valueOf(SOURCE, Source.PET_MERGED_TRAINING.getCode()),
			     Term.valueOf(PLAYERID, player.getId()),
			     Term.valueOf(GOLDEN, golden),
			     Term.valueOf(PROPS_ID, propsId),
			     Term.valueOf(USE_PROPS_COUNT, usePropsCount),
			     Term.valueOf(CURRENT_SAVVY_LEVEL, petBattle.getSavvy()),
			     Term.valueOf(CURRENT_MERGED_LEVEL,petBattle.getMergedLevel()),
			     Term.valueOf(CURRENT_MERGED_BLESS,petBattle.getMergedBless()),
			     Term.valueOf(CURRENT_MERGED_BLESS_PERCENT,petBattle.getMergedBlessPercent()),
			     Term.valueOf(CURRENT_GOLDEN, player.getGolden()));
		
		if(golden != 0){
			GoldLogger.outCome(Source.PET_MERGED_TRAINING, golden, player, goodsInfos);
		}
	}
}
