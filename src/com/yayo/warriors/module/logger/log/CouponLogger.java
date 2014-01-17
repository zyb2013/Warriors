package com.yayo.warriors.module.logger.log;

import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.model.Term;
import com.yayo.warriors.module.logger.model.TraceLog;
import com.yayo.warriors.module.logger.type.LogType;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.user.entity.Player;

/**
 * 礼金日志文件
 * 
 * @author Hyint
 */
public class CouponLogger extends TraceLog {

	private static final LogType type = LogType.COUPON; 
	
	/**
	 * 支出礼金
	 * 
	 * @param source			日志来源
	 * @param costCoupon		花费的金币
	 * @param player			角色对象	
	 */
	public static void outCome(Source source, long costCoupon, Player player, LoggerGoods...goodsInfos) {
		TraceLog.log(type, goodsInfos, Term.valueOf(COUPON, costCoupon),
									   Term.valueOf(PLAYERID, player.getId()),
									   Term.valueOf(SOURCE, source.getCode()),
									   Term.valueOf(CURRENT_COUPON, player.getCoupon()),
									   Term.valueOf(PLAYER_NAME, player.getName()),
									   Term.valueOf(USER_NAME, player.getUserName()),
									   Term.moneyOutcomeTerm());
	};

	/**
	 * 角色收入货币
	 * 
	 * @param playerId			角色ID
	 * @param source			日志来源
	 * @param costCoupon		花费的金币
	 * @param player			角色对象
	 */
	public static void inCome(Source source, long costCoupon, Player player, LoggerGoods...goodsInfos) {
		TraceLog.log(type, goodsInfos, Term.valueOf(COUPON, costCoupon),
							 		   Term.valueOf(PLAYERID, player.getId()),
							 		   Term.valueOf(PLAYER_NAME, player.getName()),
							 		   Term.valueOf(USER_NAME, player.getUserName()),
							 		   Term.valueOf(SOURCE, source.getCode()),
									   Term.valueOf(CURRENT_COUPON, player.getCoupon()),
									   Term.moneyIncomeTerm());
	};
}
