package com.yayo.warriors.module.market.manager;

import java.util.List;
import java.util.Set;

import com.yayo.warriors.module.market.entity.MarketItem;
import com.yayo.warriors.module.market.model.UserBooth;
import com.yayo.warriors.module.market.type.ItemType;

/**
 * 摆摊Manager层模块 
 * 
 * @author liuyuhua
 */
public interface MarketManager {
	
	/**
	 * 查询摆摊对象
	 * 
	 * @param  marketId				摆摊自增ID
	 * @return {@link MarketItem}	摆摊信息
	 */
	MarketItem getMarketItem(long marketItemId);
	
	/**
	 * 创建摆摊对象
	 * 
	 * @param marketItem			摆摊道具信息
	 */
	void createMarket(MarketItem marketItem);

	/**
	 * 列出摆摊道具列表
	 * 
	 * @param  playerId				角色ID
	 * @return {@link List}			列出摆摊道具信息
	 */
	List<MarketItem> listMarketItems(long playerId);
	
	/**
	 * 移除摆摊通用缓存信息
	 * 
	 * @param playerId				角色ID
	 */
	void removeMarketCommonCache(long playerId, ItemType itemType);
	
	
	/**
	 * 获取玩家摊位
	 * 
	 * @param  playerId  			玩家的ID
	 * @param  init					缓存查询不存在, 是否需要初始化. true-需要, false-不需要
	 * @return {@link UserBooth}	玩家的摊位信息
	 */
	UserBooth getUserBooth(long playerId, boolean init);
	
	/**
	 * 移除摊位信息缓存
	 * 
	 * @param playerId              角色ID
	 */
	void removeMarketCache(long playerId);
	
	
	/**
	 * 查询所有摆摊玩家ID缓存
	 * 
	 * @return {@link Set}          玩家ID列表
	 */
	Set<Long> getAllMarketPlayerIds();
	
	/**
	 * 是否有摊位
	 * 
	 * @param playerId              玩家ID
	 * @return {@link Boolean}
	 */
	boolean isMarket(long playerId);
	
	/**
	 * 获取摊位玩家列表
	 * 
	 * @param marketType            摊位物品类型
	 * @return {@link List}
	 */
	Set<Long> listMarketOwnerIds(ItemType marketType);
	
}
