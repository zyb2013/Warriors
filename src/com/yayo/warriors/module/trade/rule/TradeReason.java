package com.yayo.warriors.module.trade.rule;

/**
 * 交易取消原因
 * @author huachaopoing
 */
public interface TradeReason {
	
	/** 玩家取消交易 */
	int CANCLE_ACTION = 1;
	
	/** 背包不足, 取消交易 */
	int CANCLE_BACKFULL = 2;
	
	/** 角色登出, 取消交易 */
	int CANCLE_LOGOUT = 3;
	
	/** 完成交易, 清空缓存 */
	int COMPLETE_TRADE = 4;
}
