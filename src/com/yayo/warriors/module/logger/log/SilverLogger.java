package com.yayo.warriors.module.logger.log;

import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.user.entity.Player;

/**
 * 银币日志. 游戏币日志, 只记录游戏币的收入与支出. 不关心物品得失
 * 
 * @author Hyint
 */
public class SilverLogger extends TraceLog {

	/**
	 * 支出货币
	 * 
	 * @param source			日志来源
	 * @param silver			花费的游戏币
	 * @param player			角色对象	
	 */
	public static void outCome(Source source, long costSilver, Player player, LoggerGoods...loggerGoods) {
		TraceLog.log(LogType.SILVER, loggerGoods, Term.valueOf(SILVER, costSilver),
									 Term.valueOf(PLAYERID, player.getId()),
									 Term.valueOf(SOURCE, source.getCode()),
									 Term.valueOf(PLAYER_NAME, player.getName()),
									 Term.valueOf(USER_NAME, player.getUserName()),
									 Term.valueOf(CURRENT_SILVER, player.getSilver()),
									 Term.moneyOutcomeTerm());
	}

	/**
	 * 角色收入货币
	 * 
	 * @param source			日志来源
	 * @param silver			花费的游戏币
	 * @param player			角色对象
	 */
	public static void inCome(Source source, long silver, Player player, LoggerGoods...loggerGoods) {
		TraceLog.log(LogType.SILVER, loggerGoods, Term.valueOf(SILVER, silver),
									 Term.valueOf(PLAYERID, player.getId()),
									 Term.valueOf(SOURCE, source.getCode()),
									 Term.valueOf(PLAYER_NAME, player.getName()),
									 Term.valueOf(USER_NAME, player.getUserName()),
									 Term.valueOf(CURRENT_SILVER, player.getSilver()),
									 Term.moneyIncomeTerm());
	}
}
