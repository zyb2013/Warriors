package com.yayo.warriors.module.trade.manager.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.yayo.warriors.module.trade.manager.TradeManager;
import com.yayo.warriors.module.trade.model.TradeProps;
import com.yayo.warriors.module.trade.model.UserTrade;

/**
 * 交易管理接口
 * 
 * @author huachaoping
 */
@Service
public class TradeManagerImpl implements TradeManager {
	
	/** {玩家ID, 玩家交易信息对象} */
	private final ConcurrentHashMap<Long, UserTrade> TRADE_MAP = new ConcurrentHashMap<Long, UserTrade>(5);

	
	/**
	 * 查询玩家交易对象
	 * @param playerId                玩家ID 
	 * @return
	 */
	
	public UserTrade getUserTrade(long playerId) {
		UserTrade userTrade = TRADE_MAP.get(playerId);
		if (userTrade == null) {
			TRADE_MAP.putIfAbsent(playerId, UserTrade.valueOf(playerId));
			userTrade = TRADE_MAP.get(playerId);
		}
		return userTrade;
	}

	
	/**
	 * 交易取消, 清缓存
	 * @param playerId
	 * @param targetId
	 */
	
	public void removeTradeCache(long playerId, long targetId) {
		TRADE_MAP.remove(playerId);
		TRADE_MAP.remove(targetId);
	}

	
	/**
	 * 查询交易缓存对象
	 * @param playerId         玩家ID    
	 * @return {@link UserTrade}
	 */
	
	public UserTrade getTradeCache(long playerId) {
		return TRADE_MAP.get(playerId);
	}
	
	
	/**
	 * 是否在交易状态
	 * @param playerId        玩家Id
	 * @return {@link Boolean}
	 */
	
	public boolean isTradeState(long playerId) {
		UserTrade userTrade = TRADE_MAP.get(playerId);
		if (userTrade == null) {
			return false;
		}
		
		Long targetId = userTrade.getTradeTarget();
		if (targetId > 0L) {
			return true;
		}
		return false;
	}

	
	/**
	 * 是否交易物品
	 * 
	 * @param playerId         玩家Id
	 * @param goodsId          道具或装备Id
	 * @param goodsType        物品类型
	 * @return {@link Boolean} true-交易中的物品, false-不是交易物品
	 */
	
	public boolean isTradeProps(long playerId, long goodsId, int goodsType) {
		UserTrade userTrade = TRADE_MAP.get(playerId);
		if (userTrade == null) {
			return false;
		}
		List<TradeProps> propsList = userTrade.getPropsList();
		for (TradeProps props : propsList) {
			long userPropsId = props.getUserPropId();
			int  propsType   = props.getGoodType();
			if (propsType == goodsType && userPropsId == goodsId) {
				return true;
			}
		}
		return false;
	}
}
