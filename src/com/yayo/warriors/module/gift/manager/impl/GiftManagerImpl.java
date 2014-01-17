package com.yayo.warriors.module.gift.manager.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.module.gift.dao.GiftDao;
import com.yayo.warriors.module.gift.entity.Gift;
import com.yayo.warriors.module.gift.entity.UserGift;
import com.yayo.warriors.module.gift.entity.UserOnlineGift;
import com.yayo.warriors.module.gift.manager.GiftManager;
import com.yayo.warriors.module.gift.type.GiftType;

/**
 * 礼包管理
 * 
 * @author huachaoping
 */
@Component
public class GiftManagerImpl extends CachedServiceAdpter implements GiftManager {

	@Autowired
	private GiftDao giftDao;
	
	
	/** 日志 */
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	/** HashKey */
	private static final String HASH_KEY = "GIFT_";
	/** SubKey */
	private static final String GIFT_TYPE = "GIFT_TYPE_";
	
	/**
	 * 查询在线礼包实体
	 * 
	 * @param playerId         玩家ID
	 * @return {@link UserOnlineGift}
	 */
	
	public UserOnlineGift getUserOnlineGift(long playerId) {
		if(playerId <= 0L) {
			return null;
		}
		UserOnlineGift onlineGift = this.get(playerId, UserOnlineGift.class);
		return onlineGift;
	}
	
	

	
	public UserGift getUserGift(long playerId) {
		if (playerId > 0L) {
			return this.get(playerId, UserGift.class);
		}
		return null;
	}

	
	
	
	public Gift getGift(int giftId) {
		return this.get(giftId, Gift.class);
	}
	
	
	
	public void createGift(Gift gift) {
		commonDao.save(gift);
		put2EntityCache(gift);
	}
	
	
	
	public void removeGiftCache() {
		cachedService.removeFromCommonHashCache(HASH_KEY, getGiftTypeSubKey(GiftType.NORMAL_GIFT));
		cachedService.removeFromCommonHashCache(HASH_KEY, getGiftTypeSubKey(GiftType.CDKEY_GIFT));
	}
	
	
	
	public Set<Integer> getConditionGifts() {
		Set<Integer> ids = new HashSet<Integer>();
		ids.addAll(this.getBaseGifts(GiftType.CDKEY_GIFT));         // CDKEY礼包
		ids.addAll(this.getBaseGifts(GiftType.NORMAL_GIFT));        // 条件礼包
		return ids;
	}
	
	
	@SuppressWarnings("unchecked")
	private Set<Integer> getBaseGifts(int giftType) {
		String subkey = this.getGiftTypeSubKey(giftType);
		Set<Integer> result = (Set<Integer>) cachedService.getFromCommonCache(HASH_KEY, subkey);
		if (result == null) {
			List<Integer> ids = giftDao.getGiftByType(giftType);
			result = new HashSet<Integer>(ids);
			cachedService.put2CommonHashCache(HASH_KEY, subkey, result);
		}
		return result;
	}
	
	
	/**
	 * 礼包SUBKEY
	 * 
	 * @param giftType
	 * @return {@link String}
	 */
	private String getGiftTypeSubKey(int giftType) {
		return new StringBuffer().append(HASH_KEY).append(GIFT_TYPE).append(giftType).toString();
	}
	
	
	
	@SuppressWarnings("unchecked")
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if (id != null && clazz == UserOnlineGift.class) {
			UserOnlineGift onlineGift = commonDao.get(id, UserOnlineGift.class);
			if(onlineGift == null) {
				try {
					onlineGift = UserOnlineGift.valueOf((Long) id);
					commonDao.save(onlineGift);
				} catch (Exception e) {
					onlineGift = null;
					LOGGER.error("角色:[{}] 创建在线礼包异常:{}", id, e);
				}
			}
			return (T) onlineGift;
		} else if (id != null && clazz == UserGift.class) {
			UserGift userGift = commonDao.get(id, UserGift.class);
			if (userGift == null) {
				try {
					userGift = UserGift.valueOf((Long)id);
					commonDao.save(userGift);
				} catch (Exception e) {
					userGift = null;
					LOGGER.error("角色:[{}] 创建用户礼包异常:{}", id, e);
				}
			}
			return (T) userGift;
		}
		return super.getEntityFromDB(id, clazz);
	}


	
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		this.removeEntityFromCache(messageInfo.getPlayerId(), UserOnlineGift.class);
		this.removeEntityFromCache(messageInfo.getPlayerId(), UserGift.class);
	}


}
