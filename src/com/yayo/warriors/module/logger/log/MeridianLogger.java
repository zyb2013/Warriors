package com.yayo.warriors.module.logger.log;

import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;

/**
 * 经脉日志
 * 
 * 格式: 
 * <pre>
 * time  			: 当前服务器时间,
 * source  			: 日志来源类型,
 * playerId         : 角色ID,
 * goodsInfo        : 物品支出信息
 * </pre>
 * 
 * @author huachaoping
 */
public class MeridianLogger extends TraceLog {
	
	
	/**
	 * 角色点脉日志
	 * 
	 * @param playerId          角色ID
	 * @param meridianId        脉点ID
	 * @param isSucceed         点脉是否成功
	 * @param AttrKey           点脉成功加成
	 * @param attrValue         点脉成功加成值
	 * @param addExp            加成经验值
	 * @param goodsInfo         物品支出信息
	 */
	public static void lightMeridianPoint(long playerId, int meridianId, boolean isSucceed,
			 			            int attrKey, int attrValue, LoggerGoods...goodsInfo) {
		TraceLog.log(LogType.MERIDIAN, goodsInfo, Term.valueOf(PLAYERID,   playerId),
												  Term.valueOf("meridianId", meridianId),
												  Term.valueOf("isSucceed", isSucceed),
												  Term.valueOf("attrKey", attrKey),
												  Term.valueOf("attrValue", attrValue),
												  Term.valueOf(SOURCE, Source.LIGHT_MERIDIAN.getCode())
				                       );
	}
	
	/**
	 * 角色经脉升阶日志
	 * 
	 * @param playerId          角色ID 
	 * @param preStage          升阶前经脉阶段
	 * @param curStage          升阶后经脉阶段
	 * @param goodsInfo         物品支出信息
	 */
	public static void meridianStageUp(long playerId, int preStage, int curStage, LoggerGoods...goodsInfo) {
		TraceLog.log(LogType.MERIDIAN, goodsInfo, Term.valueOf(PLAYERID, playerId),
									   			  Term.valueOf("preStage", preStage),
									   			  Term.valueOf("curStage", curStage),
									   			  Term.valueOf(SOURCE, Source.MERIDIAN_STAGEUP.getCode())
						               );
	}
}
