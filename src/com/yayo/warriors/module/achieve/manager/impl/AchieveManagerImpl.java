package com.yayo.warriors.module.achieve.manager.impl;

import java.io.Serializable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.basedb.adapter.AchieveService;
import com.yayo.warriors.basedb.model.AchieveConfig;
import com.yayo.warriors.module.achieve.entity.UserAchieve;
import com.yayo.warriors.module.achieve.manager.AchieveManager;


@Service
public class AchieveManagerImpl extends CachedServiceAdpter implements AchieveManager{

	@Autowired
	private AchieveService achieveService;
	
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		this.removeEntityFromCache(messageInfo.getPlayerId(), UserAchieve.class);
	}

	
	
	
	public AchieveConfig getAchieveConfig(int achieveId) {
		return achieveService.getAchieveConfig(achieveId);
	}

	
	
	public List<AchieveConfig> listAchieveConfigs(int achieveType) {
		return achieveService.listAchieveConfigs(achieveType);
	}

	
	
	public List<Integer> listAchieveIds(int achieveType) {
		return achieveService.listAchieveConfigIds(achieveType);
	}
	

	
	public UserAchieve getUserAchieve(long playerId) {
		if (playerId > 0) {
			return this.get(playerId, UserAchieve.class);
		}
		return null;
	}
	
	
	
	@SuppressWarnings("unchecked")
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if (id != null && clazz == UserAchieve.class) {
			UserAchieve userAchieve = commonDao.get(id, UserAchieve.class);
			if (userAchieve != null) {
				return (T) userAchieve;
			}
			try {
				userAchieve = UserAchieve.valueOf((Long) id);
				commonDao.save(userAchieve);
			} catch (Exception e) {
				userAchieve = null;
			}
			return (T) userAchieve;
		}
		return super.getEntityFromDB(id, clazz);
	}



}
