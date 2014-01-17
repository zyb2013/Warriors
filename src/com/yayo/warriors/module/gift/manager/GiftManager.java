package com.yayo.warriors.module.gift.manager;

import java.util.Set;

import com.yayo.warriors.module.gift.entity.Gift;
import com.yayo.warriors.module.gift.entity.UserGift;
import com.yayo.warriors.module.gift.entity.UserOnlineGift;
import com.yayo.warriors.module.server.listener.DataRemoveListener;

/**
 * 礼包管理
 * 
 * @author huachaoping
 */
public interface GiftManager extends DataRemoveListener {
	
	/**
	 * 查询在线礼包实体
	 * 
	 * @param playerId         玩家ID
	 * @return {@link UserOnlineGift}
	 */
	UserOnlineGift getUserOnlineGift(long playerId);
	
	
	/** 
	 * 查询用户礼包实体
	 * 
	 * @param playerId         玩家ID
	 * @return {@link UserGift}
	 */
	UserGift getUserGift(long playerId);
	
	
	/**
	 * 根据礼包ID获得礼包实体
	 * 
	 * @param giftId           玩家ID
	 * @return {@link Gift}
	 */
	Gift getGift(int giftId);
	
	
	/**
	 * 创建礼包
	 * 
	 * @param gift             
	 */
	void createGift(Gift gift);
	
	
	/**
	 * 根据类型查询基础礼包
	 * 
	 * @param giftType   
	 * @return {@link Set}
	 */
	Set<Integer> getConditionGifts();
	
	
	/**
	 * 移除礼包缓存
	 * 
	 * @param giftType
	 */
	void removeGiftCache();
}
