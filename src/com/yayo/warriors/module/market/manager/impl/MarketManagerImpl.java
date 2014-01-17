package com.yayo.warriors.module.market.manager.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.module.market.dao.MarketItemDao;
import com.yayo.warriors.module.market.entity.MarketItem;
import com.yayo.warriors.module.market.manager.MarketManager;
import com.yayo.warriors.module.market.model.UserBooth;
import com.yayo.warriors.module.market.type.ItemType;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 摆摊Manager层模块 
 * 
 * @author liuyuhua
 */
@Component
public class MarketManagerImpl extends CachedServiceAdpter implements MarketManager{

	@Autowired
	private MarketItemDao marketItemDao;
	@Autowired
	private UserManager userManager;
	
	/** 玩家的摊位*/
	private final ConcurrentHashMap<Long, UserBooth> booths = new ConcurrentHashMap<Long, UserBooth>(0); 
	
	
	public MarketItem getMarketItem(long marketItemId) {
		return this.get(marketItemId, MarketItem.class);
	}

	
	public void createMarket(MarketItem marketItem) {
		commonDao.save(marketItem);
	}

	private static final String PREFX = "MARKET_ITEM_";
	private static final String PLAYERID = "_PLAYER_ID_";
	private static final String ITEMTYPE = "_ITEM_TYPE_";
	
	/**
	 * 获得缓存Key
	 * 
	 * @param  playerId			角色ID
	 * @return {@link String}	缓存Key
	 */
	private String getCachedKey(long playerId) {
		return new StringBuffer().append(PREFX).append(PLAYERID).append(playerId).toString();
	}
	
	
	private String getMarketKey(ItemType type) {
		return new StringBuffer().append(PREFX).append(ITEMTYPE).append(type).toString();
	}
	
	
	
	/**
	 * 获得摆摊道具ID列表
	 * 
	 * @param  playerId  		角色ID
	 * @return {@link List}		摆摊道具ID
	 */
	@SuppressWarnings("unchecked")
	private List<Long> getMarketItemIds(long playerId) {
		String key = getCachedKey(playerId);
		List<Long> itemIdList = (List<Long>)cachedService.getFromCommonCache(key);
		if(itemIdList == null) {
			itemIdList = marketItemDao.listMarketItemId(playerId);
			cachedService.put2CommonCache(key, itemIdList);
		}
		return itemIdList;
	}
	
	
	/**
	 * 获得该物品类型摊位拥有者ID列表
	 * 
	 * @param marketItemType    摆摊物品类型(物品, 装备)
	 * @return {@link List}
	 */
	@SuppressWarnings("unchecked")
	private Set<Long> getMarketOwnerIds(ItemType itemType) {
		String key = getMarketKey(itemType);
		List<Long> ownerIdList = (List<Long>) cachedService.getFromCommonCache(key);
		if (ownerIdList == null) {
			ownerIdList = marketItemDao.listMarketOwner(itemType);
			cachedService.put2CommonCache(key, ownerIdList);
		}
		return new HashSet<Long>(ownerIdList);
	}
	
	
	/**
	 * 列出摆摊道具列表
	 * 
	 * @param  playerId				角色ID
	 * @return {@link List}			列出摆摊道具信息
	 */
	
	public List<MarketItem> listMarketItems(long playerId) {
		List<Long> idList = getMarketItemIds(playerId);
		return this.getEntityFromIdList(idList, MarketItem.class);
	}

	/**
	 * 移除摆摊通用缓存信息
	 * 
	 * @param playerId				角色ID
	 */
	
	public void removeMarketCommonCache(long playerId, ItemType itemType) {
		cachedService.removeFromCommonCache(getCachedKey(playerId));
		cachedService.removeFromCommonCache(getMarketKey(itemType));
	}

	
	
	/**
	 * 获取玩家摊位
	 * 
	 * @param  playerId  			玩家的ID
	 * @param  notExistNew			不存在, 是否需要初始化. true-需要, false-不需要
	 * @return  {@link UserBooth}	玩家的摊位信息
	 */
	
	public UserBooth getUserBooth(long playerId, boolean init) {
		UserBooth userBooth = booths.get(playerId);
		if(userBooth != null) {
			return userBooth;
		}
		
		if(!init) {
			return userBooth;
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return userBooth;
		}
		
		Player player = userDomain.getPlayer();
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			userBooth = booths.get(playerId);
			if(userBooth != null){
				return userBooth;
			}
			
			Map<Long, MarketItem> markets = new HashMap<Long, MarketItem>(1);
			List<MarketItem> marketItems = this.listMarketItems(playerId);
			if(marketItems != null && !marketItems.isEmpty()) {
				for (MarketItem marketItem : marketItems) {
					markets.put(marketItem.getId(), marketItem);
				}
			}
			
			String playerName = player.getName();
			userBooth = UserBooth.valueOf(playerId, playerName, markets);
			booths.putIfAbsent(playerId, userBooth);
			userBooth = booths.get(playerId);
		} finally {
			lock.unlock();
		}
		return userBooth;
	}

	
	/**
	 * 移除摊位信息缓存
	 * 
	 * @param playerId              角色ID
	 */
	
	public void removeMarketCache(long playerId) {
		booths.remove(playerId);
	}

	/**
	 * 查询所有摆摊玩家ID缓存
	 * 
	 * @return {@link Set}          玩家ID列表
	 */
	
	public Set<Long> getAllMarketPlayerIds() {
		return booths.keySet();
	}
	
	/**
	 * 是否有摊位
	 * @param playerId              玩家ID
	 * @return {@link Boolean}
	 */
	public boolean isMarket(long playerId) {
		if (!userManager.isOnline(playerId)) {
			return false;
		}
		List<Long> idList = getMarketItemIds(playerId);
		return !idList.isEmpty();
	}

	
	/**
	 * 获取摊位玩家列表
	 * 
	 * @param marketType            摊位物品类型
	 * @return {@link List}
	 */
	
	public Set<Long> listMarketOwnerIds(ItemType marketType) {
		return getMarketOwnerIds(marketType);
	}
	
}
