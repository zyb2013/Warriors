package com.yayo.warriors.module.mortal.manager.impl;

import static com.yayo.warriors.module.mortal.rule.MortalRule.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.common.utility.TimeConstant;
import com.yayo.warriors.basedb.adapter.MortalBodyConfigService;
import com.yayo.warriors.basedb.model.MortalAddedConfig;
import com.yayo.warriors.basedb.model.MortalBodyConfig;
import com.yayo.warriors.module.mortal.dao.MortalDao;
import com.yayo.warriors.module.mortal.entity.UserMortalBody;
import com.yayo.warriors.module.mortal.manager.MortalManager;
import com.yayo.warriors.module.mortal.rule.MortalRule;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.Fightable;

/**
 * 肉身管理接口实现类
 * 
 * @author Hyint
 */
@Service
public class MortalManagerImpl extends CachedServiceAdpter implements MortalManager {

	@Autowired
	private MortalDao mortalDao;
	@Autowired
	private MortalBodyConfigService mortalService;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	
	private final static String HASH_KEY = "MORTAL";
	private final static String MORTAL_PLAYERIDS = "ALL_MORTAL_PLAYERIDS";
	
	
	/**
	 * 获得肉身配置对象
	 * @param  job						角色职业
	 * @param  type						肉身类型
	 * @param  level					肉身等级
	 * @return {@link MortalBodyConfig}	肉身配置信息
	 */
	
	public MortalBodyConfig getMorbodyConfig(int job, int type, int level) {
		return mortalService.getMorbodyConfig(job, type, level);
	}

	/**
	 * 获得肉身加成配置对象
	 * @param job                       角色职业
	 * @param level                     加成等级需求
	 * @return {@link MortalAddedConfig}
	 */
	public MortalAddedConfig getMortalAddedConfig(int job, int level) {
		return mortalService.getMortalAddedConfig(job, level);
	}
	
	/**
	 * 取得肉身对象
	 * @param playerBattle        
	 * @return {@link UserMortalBody}
	 */
	
	public UserMortalBody getUserMortalBody(PlayerBattle playerBattle) {
		if (playerBattle == null || playerBattle.getLevel() < USER_LEVEL) {
			return null;
		}
		return this.get(playerBattle.getId(), UserMortalBody.class);
	}
	
	@SuppressWarnings("unchecked")
	
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == UserMortalBody.class) {
			UserMortalBody mortal = commonDao.get(id, UserMortalBody.class);
			if (mortal != null) {
				return (T) mortal;
			}
			
			try {
				mortal = UserMortalBody.valueOf((Long)id);
				commonDao.save(mortal);
				return (T) mortal;
			} catch (Exception e) {
				logger.error("角色: [{}] 创建肉身异常{}", id, e);
				return null;
			}
			
		}
		return super.getEntityFromDB(id, clazz);
	}

	/** 
	 * 获得已加成属性
	 * 
	 * @param  playerId				角色ID 
	 * @return {@link Fightable}	战斗属性对象
	 */
	
	public Fightable getAttributeValue(PlayerBattle playerBattle) {
		Fightable fightable = new Fightable();
		UserMortalBody mortal = this.getUserMortalBody(playerBattle);
		if(mortal == null) {
			return fightable;
		}
		
		int roleJob = playerBattle.getJob().ordinal();
		Set<Entry<Integer, Integer>> entrySet = mortal.getMortalBodyMap().entrySet();
		for (Iterator<Entry<Integer, Integer>> it = entrySet.iterator(); it.hasNext();) {
			Entry<Integer, Integer> entry = it.next();
			if(entry == null) {
				continue;
			}
			
			Integer type  = entry.getKey();
			Integer level = entry.getValue();
			if(type == null || level == null) {
				continue;
			}
			
			MortalBodyConfig config = this.getMorbodyConfig(roleJob, type, level);
			if(config == null) {
				continue;
			}
			
			int[] attributes = config.getAttributes();
			for (int i = 0; i <= attributes.length - 1; i++) {
				fightable.add(attributes[i], config.getValues()[i]);
			}
		}
		
		int minLevel = getMortalMinLevel(playerBattle);
		int level = MortalRule.getMortalAddedLevel(minLevel);
		if (level == 0) return fightable;
		
		MortalAddedConfig config = getMortalAddedConfig(roleJob, level);
		if (config != null) {
			int [] attributes = config.getAttributes();
			for (int i = 0; i <= attributes.length - 1; i++) {
				fightable.add(attributes[i], config.getValues()[i]);
			}
		}
		return fightable;
	}

	/**
	 * 获得最小等级肉身
	 * 
	 * @param playerId
	 * @return {@link Integer}
	 */
	public int getMortalMinLevel(PlayerBattle playerBattle) {
		int minValue = 0;
		UserMortalBody mortal = this.getUserMortalBody(playerBattle);
		if(mortal == null) {
			return minValue;
		}
		Map<Integer, Integer> mortalMap = mortal.getMortalBodyMap();
		if(mortalMap == null || mortalMap.isEmpty()) {
			return minValue;
		}
		
		List<Integer> levels = new ArrayList<Integer>(mortalMap.values());
		if (levels.size() <= TYPE_LIMIT) {
			levels.add(minValue);
		}
		return Collections.min(levels);
	}

	
	/**
	 * 移除肉身缓存数据
	 * 
	 * @param messageInfo	消息信息对象
	 */
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		this.removeEntityFromCache(messageInfo.getPlayerId(), UserMortalBody.class);
	}

	
	
	@SuppressWarnings("unchecked")
	
	public List<Long> getAllMortalPlayers() {
		List<Long> ids = (List<Long>) cachedService.getFromCommonCache(HASH_KEY, MORTAL_PLAYERIDS);
		if (ids == null) {
			ids = mortalDao.getMortalPlayers();
			int cachedTime = TimeConstant.ONE_MINUTE_MILLISECOND * 5;
			cachedService.put2CommonHashCache(HASH_KEY, MORTAL_PLAYERIDS, ids, cachedTime);
		}
		return ids;
	}
	
	
	
}
