package com.yayo.warriors.module.logger.log;

import static com.yayo.warriors.module.logger.type.LogType.*;

import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.trade.model.UserTrade;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.type.Currency;

/**
 * 交易日志
 * 
 * @author huachaoping
 */
public class TradeLogger extends TraceLog {
	
	/**
	 * 玩家交易操作日志
	 * 
	 * @param player          交易玩家
	 * @param target          交易目标
	 * @param source          日志来源
	 * @param playerTrade     玩家交易信息
	 * @param targetTrade     目标交易信息
	 * @param goodsInfo       玩家物品收支情况
	 */
	public static void tradeLog(Player player, Player target, Source source, UserTrade playerTrade, UserTrade targetTrade, LoggerGoods ...goodsInfo) {
		log(PLAYER_TRADE, goodsInfo, Term.valueOf(PLAYER_NAME, player.getName()),
									 Term.valueOf(TRADE_TARGET, target.getName()),
									 Term.valueOf(SOURCE, source.getCode()),
									 Term.valueOf("playerGolden", playerTrade.getCurrency(Currency.GOLDEN.ordinal())),
									 Term.valueOf("playerSilver", playerTrade.getCurrency(Currency.SILVER.ordinal())),
									 Term.valueOf("targetGolden", targetTrade.getCurrency(Currency.GOLDEN.ordinal())),
									 Term.valueOf("targetSilver", targetTrade.getCurrency(Currency.SILVER.ordinal()))
									 );
	}
}
