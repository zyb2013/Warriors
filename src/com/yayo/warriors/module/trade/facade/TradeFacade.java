package com.yayo.warriors.module.trade.facade;

import com.yayo.warriors.module.trade.constant.TradeConstant;

/** 
 * 交易系统接口
 * @author huachaoping
 */
public interface TradeFacade {
	
	/**
	 * 邀请玩家进行交易
	 * 
	 * @param playerId         邀请者Id
	 * @param targetId         被邀请者Id
	 * @return {@link TradeConstant}
	 */
	int inviteTrade(long playerId, long targetId);
	
	/**
	 * 同意交易
	 * 
	 * @param playerId         被邀请者Id 
	 * @param targetId         邀请者Id
	 * @return {@link TradeConstant}
	 */
	int acceptTrade(long playerId, long targetId);
	
	/**
	 * 拒绝交易
	 * 
	 * @param playerId         被邀请者Id
	 * @param targetId         邀请者Id
	 * @return {@link TradeConstant}
	 */
	int rejectTrade(long playerId, long targetId);
	
	/**
	 * 加入物品(交易中)
	 * 
 	 * @param playerId         玩家Id
 	 * @param targetId         目标Id
	 * @param userPropsId      用户道具Id
	 * @param count            物品数量
	 * @return {@link TradeConstant}
	 */
	int addUserProps(long playerId, long targetId, long userPropsId, int count);
	
	/**
	 * 加入装备(交易中)
	 * 
	 * @param playerId         玩家Id
	 * @param targetId         目标Id
	 * @param userEquipId      用户装备Id
	 * @return {@link TradeConstant}
	 */
	int addUserEquip(long playerId, long targetId, long userEquipId);
	
	/**
	 * 移除物品(交易中, 包括装备)
	 * 
	 * @param playerId
	 * @param targetId
	 * @param goodsId
	 * @param count
	 * @param goodsType
	 * @return {@link TradeConstant}
	 */
	int removeUserProps(long playerId, long targetId, long goodsId, int count, int goodsType);
	
	/**
	 * 加入钱币(交易中)
	 * 
	 * @param playerId         玩家Id
	 * @param currency         货币类型
	 * @param count            货币数量
	 * @return {@link TradeConstant}
	 */
	int addCurrency(long playerId, long targetId, int currency, long count);
	
	/**
	 * 锁定交易
	 * 
	 * @param playerId         玩家Id
	 * @param targetId         目标Id
	 * @return {@link TradeConstant}
	 */
	int lockTrade(long playerId, long targetId);
	
	/** 
	 * 取消交易
	 * 
	 * @param playerId         玩家Id
	 * @param targetId         目标Id
	 * @return {@link TradeConstant}
	 */
	int cancleTrade(long playerId, long targetId, int reason);
	
	/**
	 * 交换物品(交易中)
	 * 
	 * @param playerId         玩家Id
	 * @param targetId         目标Id
	 * @param userPropsId      用户道具Id
	 * @param count            道具数量
	 * @param index            物品索引
	 * @return
	 */
//	int changeTradeProps(long playerId, long targetId, long userPropsId, int count, int index);
	
	/**
	 * 交换装备(交易中)            
	 *  
	 * @param playerId         玩家Id
	 * @param targetId         目标Id
	 * @param userEquipId      用户装备Id
	 * @param index           
	 * @return
	 */
//	int changeTradeEquip(long playerId, long targetId, long userEquipId, int index);
	
	/**
	 * 完成交易
	 * 
	 * @param playerId         玩家Id
	 * @param targetId         目标Id
	 * @return {@link TradeConstant}
	 */
	int completeTrade(long playerId, long targetId);
	
}
