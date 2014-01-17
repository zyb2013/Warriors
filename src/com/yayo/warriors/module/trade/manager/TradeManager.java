package com.yayo.warriors.module.trade.manager;

import com.yayo.warriors.module.trade.model.UserTrade;

/**
 * 交易管理类
 * 
 * @author huachaoping
 */
public interface TradeManager {
	
	/**
	 * 查询玩家交易对象
	 * 
	 * @param playerId         玩家ID    
	 * @return {@link UserTrade}
	 */
	UserTrade getUserTrade(long playerId);
	
	/**
	 * 查询交易缓存对象
	 * 
	 * @param playerId         玩家ID    
	 * @return {@link UserTrade}
	 */
	UserTrade getTradeCache(long playerId);
	
	/**
	 * 交易取消, 清缓存
	 * @param playerId         玩家ID  
	 * @param targetId         目标ID
	 * return 
	 */
	void removeTradeCache(long playerId, long targetId);
	
	/**
	 * 是否在交易状态
	 * 
	 * @param playerId         玩家Id
	 * @return {@link Boolean} true-在交易状态, false-不在交易状态
	 */
	boolean isTradeState(long playerId);
	
	/**
	 * 是否交易物品
	 * 
	 * @param playerId         玩家Id
	 * @param goodsId          道具或装备Id
	 * @param goodsType        物品类型
	 * @return {@link Boolean} true-交易中的物品, false-不是交易物品
	 */
	boolean isTradeProps(long playerId, long goodsId, int goodsType);
}
