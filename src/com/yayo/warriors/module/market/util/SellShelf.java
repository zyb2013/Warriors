package com.yayo.warriors.module.market.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.yayo.warriors.module.market.model.Sell;
import com.yayo.warriors.module.market.type.ItemType;


/**
 * 货架(就是一般商品中那些卖东西用来成列的货架)
 * 
 * @author liuyuhua
 */
@Component
public class SellShelf {
	
	/** 商品对应玩家的信息 */
	private final ConcurrentHashMap<Sell, List<Long>> GOODS = new ConcurrentHashMap<Sell, List<Long>>(1);

	/**
	 * 加入商品售卖
	 * @param playerId    玩家的ID
	 * @param type        物品类型
	 * @param baseId      物品原型ID
	 */
	public void enterShelf(Long playerId, ItemType type, int baseId, String goodsName) {
		Sell sell = Sell.valueOf(type.ordinal(), baseId, goodsName);
		putGoods4Players(sell, playerId);
	}
	
	/**
	 * 获取该类商品的出售者
	 * (返回值允许为空)
	 * @param type       商品类型
	 * @param baseId     商品原型ID
	 * @return  
	 */
	public Collection<Long> getSeller4PROPS(ItemType type, int baseId, String name) {
		Sell sell = Sell.valueOf(type.ordinal(), baseId, name);
		return GOODS.get(sell);
	}
	
	/**
	 * 删除 出售该商品的玩家
	 * @param playerId    玩家的ID
	 * @param type        商品类型
	 * @param baseId      商品原型ID
	 * @return
	 */
	public boolean removeGoods(long playerId, ItemType type, int baseId, String name) {
		Sell sell = Sell.valueOf(type.ordinal(), baseId, name);		
		List<Long> seller = GOODS.get(sell);
		if (!seller.remove(playerId)) {
			return false;
		}
		// 该玩家没该物品卖了, 要将其清除
		if (seller.isEmpty()) {
			removeSell(sell);
		}
		return true; // 删除玩家
		
	}
	
	/**
	 * 删除商品
	 * @param sell       商品
	 */
	public void removeSell(Sell sell) {
		GOODS.remove(sell);
	}

	
	/**
	 * 清空玩家所有销售物品
	 * (一般是下线时候使用,或者货品已为空)
	 * @param playerId    玩家ID
	 */
	public void clearPlayerShelf(Long playerId) {
		Set<Sell> sells = new HashSet<Sell>(GOODS.keySet());
		for (Sell sell : sells) {
			List<Long> playerIds = GOODS.get(sell);
			playerIds.remove(playerId);
		}
	}

	/**
	 * 获取同类销售的玩家
	 * @param sell
	 * @param player
	 * @return
	 */
	private void putGoods4Players(Sell sell, long playerId) {
		List<Long> playerIds = GOODS.get(sell);
		if(playerIds == null) {
			playerIds = new ArrayList<Long>();
			GOODS.putIfAbsent(sell, playerIds);
			playerIds = GOODS.get(sell);
		}
		playerIds.add(playerId);
	}
	
	
	
	/** 获得所有在出售的物品信息 */
	public Set<Sell> getAllSells() {
		return new HashSet<Sell>(GOODS.keySet());
	}
	
	/** 获得同类商品的玩家 */
	public Set<Long> getSellPlayers(Sell sell) {
		List<Long> sellList = GOODS.get(sell);
		return new HashSet<Long>(sellList);
	}
	
}
