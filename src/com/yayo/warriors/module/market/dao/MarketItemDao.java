package com.yayo.warriors.module.market.dao;

import java.util.List;

import com.yayo.warriors.module.market.type.ItemType;

/**
 * 商店道具DAO
 * 
 * @author Hyint
 */
public interface MarketItemDao {


	/**
	 * 根据玩家的ID,获取 商品 PK 集合
	 * 
	 * @param  playerId     		玩家的ID
	 * @return {@link List}			摆摊ID列表
	 */
	List<Long> listMarketItemId(long playerId);
	
	
	/**
	 * 根据出售的物品类型获得店主 PK 集合
	 * 
	 * @param  itemType             摆摊物品类型(装备, 道具) 
	 * @return {@link List}         店主ID集
	 */
	List<Long> listMarketOwner(ItemType itemType);
	
}
