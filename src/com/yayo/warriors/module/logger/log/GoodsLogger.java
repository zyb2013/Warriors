package com.yayo.warriors.module.logger.log;

import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.user.entity.Player;

/**
 * 物品收支情况.该日志只负责记录收支情况, 不关心其他的收支
 * 
 * @author Hyint
 */
public class GoodsLogger extends TraceLog {

	private static final LogType TYPE = LogType.GOODS; 
	
	/**
	 * 物品日志
	 * 
	 * @param  player			角色对象
	 * @param  source			物品来源
	 * @param  loggerGoods		物品日志长度
	 */
	public static void goodsLogger(Player player, Source source, LoggerGoods...loggerGoods) {
		if(loggerGoods.length > 0) {
			log(TYPE, loggerGoods, Term.valueOf(PLAYERID, player.getId()),
								   Term.valueOf(SOURCE, source.getCode()),
								   Term.valueOf(PLAYER_NAME, player.getName()),
								   Term.valueOf(USER_NAME, player.getUserName())
			);
		}
	}
}
