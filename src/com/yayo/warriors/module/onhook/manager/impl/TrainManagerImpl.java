package com.yayo.warriors.module.onhook.manager.impl;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.module.onhook.entity.UserTrain;
import com.yayo.warriors.module.onhook.manager.TrainManager;
import com.yayo.warriors.module.onhook.model.UserSingleTrain;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 挂机管理接口
 * 
 * @author Hyint
 */
@Service
public class TrainManagerImpl extends CachedServiceAdpter implements TrainManager {

	@Autowired
	private UserManager userManager;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/** 打坐配置对象<PLAYERID, 玩家打坐储存信息> */
	private final ConcurrentHashMap<Long, UserSingleTrain> SINGLE_TRAIN = new ConcurrentHashMap<Long, UserSingleTrain>();
	
	
	
	public UserTrain getUserTrain(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return null;
		}
		return this.get(playerId, UserTrain.class);
	}

	
	
	public UserSingleTrain getUserSingleTrain(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return null;
		}
		UserSingleTrain train = SINGLE_TRAIN.get(playerId);
		if (train == null) {
			train = UserSingleTrain.valueOf(playerId);
			SINGLE_TRAIN.putIfAbsent(playerId, train);
			train = SINGLE_TRAIN.get(playerId);
		} 
		return train;
	}
	
	
	
	public void removeSingleTrainCache(long playerId) {
		SINGLE_TRAIN.remove(playerId);
	}
	
	
	
	@SuppressWarnings("unchecked")
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == UserTrain.class) {
			UserTrain userTrain = commonDao.get(id, UserTrain.class);
			if(userTrain != null) {
				return (T) userTrain;
			}
			
			try {
				userTrain = UserTrain.valueOf((Long) id);
				commonDao.save(userTrain);
				return (T) userTrain;
			} catch (Exception e) {
				logger.error("角色: [{}] 创建挂机对象异常:{}", id, e);
				return null;
			}
		}
		return super.getEntityFromDB(id, clazz);
	}

	
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		this.removeEntityFromCache(messageInfo.getPlayerId(), UserTrain.class);
	}
	
}
