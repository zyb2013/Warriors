package com.yayo.warriors.module.treasure.manager.impl;

import java.io.Serializable;

import org.springframework.stereotype.Service;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.module.treasure.entity.UserTreasure;
import com.yayo.warriors.module.treasure.manager.TreasureManager;

@Service
public class TreasureManageImpl extends CachedServiceAdpter implements TreasureManager{

	
	public UserTreasure getUserTreasure(long playerId) {
		if(playerId > 0L){
			return this.get(playerId, UserTreasure.class);
		}
		return null;
	}
	
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		this.removeEntityFromCache(messageInfo.getPlayerId(), UserTreasure.class);
	}

	@SuppressWarnings("unchecked")
	
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == UserTreasure.class){
			UserTreasure userTreasure = commonDao.get(id, UserTreasure.class);
			if(userTreasure == null){
				userTreasure = UserTreasure.valueOf( (Long)id );
				commonDao.save(userTreasure);
			}
			return (T) userTreasure;
		}
		return super.getEntityFromDB(id, clazz);
	}
	
}
