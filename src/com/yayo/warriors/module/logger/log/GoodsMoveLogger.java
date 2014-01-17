package com.yayo.warriors.module.logger.log;

import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.user.entity.Player;

/**
 * 物品背包移动情况.
 * 
 */
public class GoodsMoveLogger extends TraceLog {

	private static final LogType TYPE = LogType.GOODS_MOVE; 
	
	/**
	 * 物品日志
	 * 
	 * @param  player			角色对象
	 * @param  sourceBackpack	源背包
	 * @param  targetPackpack	目标来源
	 * @param  loggerGoods		物品日志长度
	 */
	public static void log(Player player, int sourceBackpack, int targetPackpack, LoggerGoods...loggerGoods) {
		if(loggerGoods.length > 0) {
			log(TYPE, loggerGoods, Term.valueOf(PLAYERID, player.getId()),
								   Term.valueOf(PLAYER_NAME, player.getName()),
								   Term.valueOf(USER_NAME, player.getUserName()),
								   Term.valueOf("sourceBackpack", sourceBackpack),
								   Term.valueOf("targetPackpack", targetPackpack)
			);
		}
	}
}
