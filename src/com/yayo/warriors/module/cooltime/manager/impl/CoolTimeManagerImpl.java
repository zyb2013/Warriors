package com.yayo.warriors.module.cooltime.manager.impl;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap.Builder;
import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.basedb.model.CoolTimeConfig;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.cooltime.manager.CoolTimeManager;
import com.yayo.warriors.module.cooltime.model.CoolTime;
import com.yayo.warriors.module.cooltime.model.PetCoolTime;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 冷却时间管理接口实现类
 * 
 * @author Hyint
 */
@Service
public class CoolTimeManagerImpl extends CachedServiceAdpter implements CoolTimeManager {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	@Autowired
	private DbService dbService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ResourceService resourceService;
	
	/**
	 * 角色登出保存数据接口
	 * 
	 * @param playerId	角色ID
	 */
	
	public void onLogoutEvent(UserDomain userDomain) {
		long playerId = userDomain.getPlayerId();
		UserCoolTime userCoolTime = getUserCoolTime(playerId);
		if(userCoolTime != null) {
			dbService.submitUpdate2Queue(userCoolTime.updateCoolTimes());
		}
	}

	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		this.removeEntityFromCache(messageInfo.getPlayerId(), UserCoolTime.class);
	}

	/**
	 * 查询基础冷却时间对象
	 * 
	 * @param  coolTimeId				冷却时间ID
	 * @return {@link CoolTimeConfig}	冷却时间对象
	 */
	
	public CoolTimeConfig getCoolTimeConfig(int coolTimeId) {
		return resourceService.get(coolTimeId, CoolTimeConfig.class);
	}

	/**
	 * 查询用户CD时间
	 * 
	 * @param  playerId					角色ID
	 * @return {@link UserCoolTime}		用户CD时间对象
	 */
	
	public UserCoolTime getUserCoolTime(long playerId) {
		UserCoolTime coolTime = null;
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain != null) {
			coolTime = this.get(playerId, UserCoolTime.class);
		}
		return coolTime;
	}
	
	
	@SuppressWarnings("unchecked")
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == UserCoolTime.class) {
			UserCoolTime coolTime = commonDao.get(id, UserCoolTime.class);
			if(coolTime != null) {
				return (T) coolTime;
			}
			
			try {
				coolTime = UserCoolTime.valueOf((Long) id);
				commonDao.save(coolTime);
				return (T) coolTime;
			} catch (Exception e) {
				LOGGER.error("角色:[{}] 创建冷却时间异常: {}", id, e);
				return null;
			}
		}
		return super.getEntityFromDB(id, clazz);
	}

	/** 
	 * 更新角色CD时间
	 * 
	 * @param  playerId					角色ID
	 * @param  coolTimeId				冷却时间ID
	 * @param  coolTime					冷却时间
	 */
	
	public void updateUserCooleTime(long playerId, int coolTimeId, int len) {
		UserCoolTime userCoolTime = this.getUserCoolTime(playerId);
		if(userCoolTime != null) {
			ChainLock lock = LockUtils.getLock(userCoolTime);
			try {
				lock.lock();
				long currMillis = System.currentTimeMillis();
				CoolTime coolTime = userCoolTime.getCoolTime(coolTimeId);
				if(coolTime == null) {
					coolTime = CoolTime.valueOf(coolTimeId, currMillis + len);
				}
				coolTime.setEndTime(currMillis + len);
				userCoolTime.putCoolTime(coolTime);
			} finally {
				lock.unlock();
			}
		}
	}

	/** builder对象*/
	private static final Builder<Long, PetCoolTime> BUILDER = new ConcurrentLinkedHashMap.Builder<Long, PetCoolTime>();
	private static final ConcurrentLinkedHashMap<Long, PetCoolTime> PET_CACHE_TIMES = BUILDER.maximumWeightedCapacity(10000).build();
	
	/**
	 * 获得召唤兽冷却时间
	 * 
	 * @param  userPetId				召唤兽ID
	 * @return {@link PetCoolTime}		召唤兽冷却时间
	 */
	
	public PetCoolTime getPetCoolTime(long userPetId) {
		PetCoolTime petCoolTime = PET_CACHE_TIMES.get(userPetId);
		if(petCoolTime == null) {
			PET_CACHE_TIMES.putIfAbsent(userPetId, new PetCoolTime());
			petCoolTime = PET_CACHE_TIMES.get(userPetId);
		}
		return petCoolTime;
	}
	
}
