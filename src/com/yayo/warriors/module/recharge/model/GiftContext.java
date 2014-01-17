package com.yayo.warriors.module.recharge.model;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yayo.common.lock.IEntity;

/**
 * 礼包上下文
 * 
 * @author Hyint
 */
public class GiftContext implements IEntity<Long> {

	/** 角色ID */
	private long playerId;
	
	/** 是否重新计算 */
	private boolean recalculate = false;
	
	/** 礼包集合 */
	private Map<Integer, CacheRechargeGift> cacheGiftMap = new ConcurrentHashMap<Integer, CacheRechargeGift>();
	
	public void removeCacheRechargeGift(int giftId) {
		this.cacheGiftMap.remove(giftId);
	}
	
	public void addCacheRechargeGift(CacheRechargeGift cacheRechargeGift) {
		if(cacheRechargeGift != null) {
			this.cacheGiftMap.put(cacheRechargeGift.getId(), cacheRechargeGift);
		}
	}
	
	public void addAll(Collection<CacheRechargeGift> cacheRechargeGifts) {
		if(cacheRechargeGifts != null && !cacheRechargeGifts.isEmpty()) {
			for (CacheRechargeGift rechargeGift : cacheRechargeGifts) {
				addCacheRechargeGift(rechargeGift);
			}
		}
	}
	
	
	public boolean isRecalculate() {
		return recalculate;
	}

	public void updateRecalculate(boolean recalculate) {
		this.recalculate = recalculate;
	}

	public Collection<CacheRechargeGift> values() {
		return cacheGiftMap.values();
	}
	
	public Map<Integer, CacheRechargeGift> getRechargeGiftMap() {
		return cacheGiftMap;
	}
	
	private GiftContext() {
	}
	
	public static GiftContext valueOf(long playerId) {
		GiftContext context = new GiftContext();
		context.playerId = playerId;
		context.recalculate = true;
		return context;
	}

	public CacheRechargeGift get(int giftId) {
		return cacheGiftMap.get(giftId);
	}

	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	
	public Long getIdentity() {
		return this.playerId;
	}

	
	public String toString() {
		return "GiftContext [playerId=" + playerId + ", recalculate=" + recalculate
				+ ", cacheGiftMap=" + cacheGiftMap + "]";
	}
	
	
}
