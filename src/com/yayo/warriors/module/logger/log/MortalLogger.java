package com.yayo.warriors.module.logger.log;

import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;

/**
 * 肉身日志类
 * 
 * 格式:
 * <pre>
 * playerId      :   角色ID
 * mortalType    :   肉身类型
 * mortalLevel   :   肉身等级
 * isSucceed     :   升级成功?
 * goodsInfo     :   消耗物品信息
 * </pre>
 * 
 * @author huachaoping
 */
public class MortalLogger extends TraceLog {
	
	/**
	 * 肉身升级日志
	 * 
	 * @param playerId      角色ID
	 * @param type          肉身类型
	 * @param level         肉身等级
	 * @param isSucceed     升级成功?
	 * @param goodsInfo     消耗物品信息
	 */
	public static void LevelUpLogger(long playerId, int type, int level, boolean isSucceed, LoggerGoods... goodsInfos) {
		TraceLog.log(LogType.MORTAL, goodsInfos, Term.valueOf(PLAYERID, playerId),
												 Term.valueOf("mortalType", type),
												 Term.valueOf("mortalLevel", level),
												 Term.valueOf("isSucceed", isSucceed)
				);
	}
}
