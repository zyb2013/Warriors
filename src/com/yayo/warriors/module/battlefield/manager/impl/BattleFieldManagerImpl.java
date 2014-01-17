package com.yayo.warriors.module.battlefield.manager.impl;

import java.io.Serializable;

import org.springframework.stereotype.Service;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.warriors.module.battlefield.entity.PlayerBattleField;
import com.yayo.warriors.module.battlefield.manager.BattleFieldManager;

@Service
public class BattleFieldManagerImpl extends CachedServiceAdpter implements BattleFieldManager {

	
	public PlayerBattleField getPlayerBattleField(Long playerId) {
		return this.get(playerId, PlayerBattleField.class);
	}

	@SuppressWarnings("unchecked")
	
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == PlayerBattleField.class){
			PlayerBattleField playerBattleField = commonDao.get(id, PlayerBattleField.class);
			if(playerBattleField == null){
				playerBattleField = PlayerBattleField.valueOf( (Long)id );
				commonDao.save(playerBattleField);
			}
			return (T) playerBattleField;
		}
		return super.getEntityFromDB(id, clazz);
	}
	
	
}
