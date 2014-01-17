package com.yayo.warriors.module.meridian.manager.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.common.utility.TimeConstant;
import com.yayo.warriors.basedb.adapter.MeridianService;
import com.yayo.warriors.basedb.model.MeridianConfig;
import com.yayo.warriors.module.meridian.dao.MeridianDao;
import com.yayo.warriors.module.meridian.entity.Meridian;
import com.yayo.warriors.module.meridian.manager.MeridianManager;

/**
 * 经脉管理接口实现类
 * 
 * @author Hyint
 */
@Service
public class MeridianManagerImpl extends CachedServiceAdpter implements MeridianManager {
	
	@Autowired
	private DbService dbService;
	@Autowired
	private MeridianDao meridianDao;
	@Autowired
	private MeridianService meridianService;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	
	private final static String HASH_KEY = "MERIDIAN";
	private final static String MERIDIAN_PLAYERIDS = "ALL_MERIDIAN_PLAYERIDS";
	
	
	/**
	 * 获得玩家对象的经脉对象
	 * 
	 * @param  playerId	 			角色ID
	 * @return {@link Meridian}		经脉对象
	 */
	
	public Meridian getMeridian(long playerId) {
		if(playerId <= 0) {
			return null;
		}
		
		Meridian meridian = this.get(playerId, Meridian.class);
		ChainLock lock = LockUtils.getLock(meridian);
		try {
			lock.lock();
			this.processMeridianTypes(meridian);
			this.processMeridianAttributes(meridian);
			this.processMeridianUpdateTimes(meridian);
		} finally {
			lock.unlock();
		}
		return meridian;
	}

	/**
	 * 处理经脉分享次数
	 * 
	 * @param  meridian 经脉对象
	 */
	private void processMeridianUpdateTimes(Meridian meridian) {
		if(meridian == null) {
			return;
		} 
		Date currentDate = new Date();
		Date updateTime = meridian.getUpdateTime();
		if(updateTime == null || updateTime.before(currentDate)) {
			meridian.updateTimes();
		}
		
		dbService.submitUpdate2Queue(meridian);
	}

	
	
	
	public List<Integer> getMerdianConfigByType(int job, int type) {
		List<Integer> points = new ArrayList<Integer>();
		Collection<MeridianConfig> configList = meridianService.listMeridianConfig(type, job);
		if (configList == null || configList.isEmpty()) {
			return points;
		}
		
		for (MeridianConfig config : configList) {
			if(!points.contains(config.getId())) {
				points.add(config.getId());
			}
		}
		return points;
	}
	
	
	
	public Object[] getMeridianAttrKeyByType(int job, int type) {
		Set<Integer> points = new HashSet<Integer>();
		Collection<MeridianConfig> configList = meridianService.listMeridianConfig(type, job);
		if (configList == null || configList.isEmpty()) {
			return new Object[0];
		}
		for (MeridianConfig config : configList) {
			points.add(config.getAttrKey());
		}
		return points.toArray();
	}
	
	
	/**
	 * 处理经脉属性集合信息
	 * 
	 * @param  meridian			经脉对象
	 * @return {@link Meridian}	经脉对象
	 */
	private void processMeridianTypes(Meridian meridian) {
		if(meridian == null || meridian.getMeridianTypes() != null) {
			return;
		}
		
		if(meridian.getMeridianTypes() != null) {
			return;
		}
		
		Set<Integer> meridiansSet = meridian.getMeridiansSet();
		meridian.setMeridianTypes(new HashMap<Integer, Collection<Integer>>());
		if(meridiansSet == null || meridiansSet.isEmpty()) {
			return;
		}
		
		Map<Integer, Collection<Integer>> meridianTypes = meridian.getMeridianTypes();
		for (Integer meridianId : meridiansSet) {
			MeridianConfig meridianConfig = this.getMeridianConfig(meridianId);
			if(meridianConfig == null) {
				continue;
			}
			
			int meridianType = meridianConfig.getMeridianType();
			Collection<Integer> collection = meridianTypes.get(meridianType);
			if(collection == null) {
				collection = new ArrayList<Integer>();
				meridianTypes.put(meridianType, collection);
			}
			if (!collection.contains(meridianId)) {
				collection.add(meridianId);
			}
		}
		meridian.resetMeridianSet();
	}
	
	/**
	 * 处理经脉的属性值
	 * 
	 * @param meridian
	 */
	private void processMeridianAttributes(Meridian meridian) {
		if(meridian == null || meridian.getMeridianAttributes() != null) {
			return;
		}
		if(meridian.getMeridianAttributes() != null) {
			return;
		}
		
		Set<Integer> meridiansSet = meridian.getMeridiansSet();
		Map<Integer, Integer> attributes = new HashMap<Integer, Integer>(2);
		if(meridiansSet != null && !meridiansSet.isEmpty()) {
			for (Integer meridianId : meridiansSet) {
				MeridianConfig config = this.getMeridianConfig(meridianId);
				if(config == null) {
					continue;
				}
				
				int attribute = config.getAttrKey();
				int value = config.getValue() + config.getExtraAttr();
				Integer attrValue = attributes.get(attribute);
				attrValue = attrValue == null ? 0 : attrValue;
				attributes.put(attribute, attrValue + value);
			}
		}
		meridian.setMeridianAttributes(attributes);
		meridian.resetMeridianSet();     // 清缓存
	}

	
	@SuppressWarnings("unchecked")
	
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == Meridian.class) {
			Meridian meridian = commonDao.get(id, Meridian.class);
			if(meridian == null) {
				try {
					meridian = Meridian.valueOf((Long)id);
					commonDao.save(meridian);
				} catch (Exception e) {
					meridian = null;
					logger.error("角色: [{}] 创建经脉异常:{}", id, e);
				}
			}
			return (T) meridian;
		}
		return super.getEntityFromDB(id, clazz);
	}

	/**
	 * 获得经脉点数据
	 * 
	 * @param  meridianId           	经脉点ID
	 * @return {@link MeridianConfig}	基础经脉对象
	 */
	
	public MeridianConfig getMeridianConfig(int meridianId) {
		return meridianService.getMeridianConfig(meridianId);
	}

	
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		this.removeEntityFromCache(messageInfo.getPlayerId(), Meridian.class);
	}

	
	
	
	public Map<Integer, Integer> getMeridianCounts() {
		return meridianService.getTypeMeridianCount();
	}

	
	
	@SuppressWarnings("unchecked")
	
	public List<Long> getAllMeridians() {
		List<Long> ids = (List<Long>) cachedService.getFromCommonCache(HASH_KEY, MERIDIAN_PLAYERIDS);
		if (ids == null) {
			ids = meridianDao.getMeridianPlayerIds();
			int cacheTime = TimeConstant.ONE_MINUTE_MILLISECOND * 5; 
			cachedService.put2CommonHashCache(HASH_KEY, MERIDIAN_PLAYERIDS, ids, cacheTime);
		}
		return ids;
	}
	
	
}
