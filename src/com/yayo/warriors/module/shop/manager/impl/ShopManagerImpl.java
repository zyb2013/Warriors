package com.yayo.warriors.module.shop.manager.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.basedb.adapter.ShopService;
import com.yayo.warriors.basedb.model.MallActiveConfig;
import com.yayo.warriors.basedb.model.MallConfig;
import com.yayo.warriors.basedb.model.MallSpecialConfig;
import com.yayo.warriors.basedb.model.ShopConfig;
import com.yayo.warriors.module.shop.entity.LaveMallProp;
import com.yayo.warriors.module.shop.entity.PlayerBuyLimit;
import com.yayo.warriors.module.shop.manager.ShopManager;
import com.yayo.warriors.util.GameConfig;

@Service
public class ShopManagerImpl extends CachedServiceAdpter implements ShopManager {
	
	@Autowired
	private ShopService shopService;
	@Autowired
	private ResourceService resourceService;
	

	private final ConcurrentSkipListSet<MallActiveConfig> SEQUENCE_ACTIVE = new ConcurrentSkipListSet<MallActiveConfig>();
	
	
	
	public MallConfig getMallConfig(int mallId) {
		return shopService.get(mallId, MallConfig.class);
	}
	public MallSpecialConfig getMallSpecialConfig(int mallId) {
		return shopService.get(mallId, MallSpecialConfig.class);
	}
	public Collection<ShopConfig> getAllShopConfig(){
		return resourceService.listAll(ShopConfig.class);
	}
	public List<MallSpecialConfig> getSpecialMallConfigs(int activeId) {
		return shopService.listMallSpecial(activeId);
	}

	
	
	public List<MallActiveConfig> getMallActiveConfigs(int isOpen) {
		return shopService.listMallActives(isOpen);
	}
	public ShopConfig getShopConfig(int shopId) {
		return shopService.get(shopId, ShopConfig.class);
	}

	
	public LaveMallProp getLaveMallProp(int mallId) {
		LaveMallProp laveMallProp = null;
		MallSpecialConfig config = getMallSpecialConfig(mallId);
		if(config != null) {
			laveMallProp = this.get(mallId, LaveMallProp.class);
		}
		return laveMallProp;
	}
	
	public long findNpcByEquipOrProps(int type, int propsId, int screenType) {
		return shopService.findNpcByEquipOrProps(type, propsId, screenType);
	}

	
	
	
	public MallActiveConfig getEffectActiveConfig() {
		String openTime = GameConfig.getFirstServerOpenTime();
		Date openDate = DateUtil.string2Date(openTime, DatePattern.PATTERN_YYYY_MM_DD);
		
		if (!SEQUENCE_ACTIVE.isEmpty()) {
			MallActiveConfig activeConfig = SEQUENCE_ACTIVE.first();
			int lastDay = activeConfig.getLastDay();
			Date endDate = DateUtil.changeDateTime(openDate, lastDay, 0, 0, 0);
			if (endDate.before(new Date())) {
				SEQUENCE_ACTIVE.pollFirst();
			}
			if (!SEQUENCE_ACTIVE.isEmpty()) {
				return SEQUENCE_ACTIVE.first();
			}
		}
		
		List<MallActiveConfig> configs = shopService.listMallActives(1);    
		int addDays = 0;
		for (MallActiveConfig config : configs) {
			addDays += config.getActiveRound();
		    Date endDate = DateUtil.changeDateTime(openDate, addDays, 0, 0, 0);
		    if (endDate.after(new Date())) {
		    	config.setLastDay(addDays);
		    	SEQUENCE_ACTIVE.add(config);
		    }
		}
		if (!SEQUENCE_ACTIVE.isEmpty()) {
			return SEQUENCE_ACTIVE.first();
		}
		
		return null;
	}

	
	
	
	public PlayerBuyLimit getPlayerBuyLimit(long playerId) {
		if (playerId > 0) {
			return this.get(playerId, PlayerBuyLimit.class);
		}
		return null;
	}

	
	
	@SuppressWarnings("unchecked")
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if (id != null && clazz == LaveMallProp.class) {
			LaveMallProp laveMallProp = commonDao.get(id, LaveMallProp.class);
			if(laveMallProp != null) {
				return (T) laveMallProp;
			}
			try {
				laveMallProp = LaveMallProp.valueOf((Integer)id, 0);
				commonDao.save(laveMallProp);
				return (T) laveMallProp;
			} catch (Exception e) {
				return null;
			}
		} else if (id != null && clazz == PlayerBuyLimit.class) {
			PlayerBuyLimit limit = commonDao.get(id, PlayerBuyLimit.class);
			if (limit != null) {
				return (T) limit;
			}
			try {
				limit = PlayerBuyLimit.valueOf((Long)id);
				commonDao.save(limit);
				return (T) limit;
			} catch (Exception e) {
				return null;
			}
		}
		return super.getEntityFromDB(id, clazz);
	}
	
}
