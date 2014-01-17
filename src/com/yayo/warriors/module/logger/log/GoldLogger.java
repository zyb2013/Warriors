package com.yayo.warriors.module.logger.log;

import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.user.entity.Player;

/**
 * 游戏金币日志. 游戏币日志, 只记录游戏币的收入与支出. 不关心物品得失
 * 
 * @author Hyint
 */
public class GoldLogger extends TraceLog {

	private static final LogType type = LogType.GOLDEN; 
	/**
	 * 支出货币
	 * 
	 * @param source			日志来源
	 * @param costGold			花费的金币
	 * @param player			角色对象	
	 */
	public static void outCome(Source source, long costGold, Player player, LoggerGoods...goodsInfos) {
		TraceLog.log(type, goodsInfos, Term.valueOf(GOLDEN, costGold),
									   Term.valueOf(PLAYERID, player.getId()),
									   Term.valueOf(SOURCE, source.getCode()),
									   Term.valueOf(CURRENT_GOLDEN, player.getGolden()),
									   Term.valueOf(PLAYER_NAME, player.getName()),
									   Term.valueOf(USER_NAME, player.getUserName()),
									   Term.moneyOutcomeTerm());
	};

	/**
	 * 角色收入货币
	 * 
	 * @param playerId			角色ID
	 * @param source			日志来源
	 * @param costGold			花费的金币
	 * @param player			角色对象
	 */
	public static void inCome(Source source, long costGold, Player player, LoggerGoods...goodsInfos) {
		TraceLog.log(type, goodsInfos, Term.valueOf(GOLDEN, costGold),
							 		   Term.valueOf(PLAYERID, player.getId()),
							 		   Term.valueOf(PLAYER_NAME, player.getName()),
							 		   Term.valueOf(USER_NAME, player.getUserName()),
							 		   Term.valueOf(SOURCE, source.getCode()),
									   Term.valueOf(CURRENT_GOLDEN, player.getGolden()),
									   Term.moneyIncomeTerm());
	};
}
