package com.yayo.warriors.module.recharge.manager.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.event.EventBus;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.recharge.dao.RechargeRecordDao;
import com.yayo.warriors.module.recharge.entity.RechargeRecord;
import com.yayo.warriors.module.recharge.event.ChargeEvent;
import com.yayo.warriors.module.recharge.manager.RechargeManager;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 充值管理接口
 * 
 * @author Hyint
 */
@Service
public class RechargeManagerImpl extends CachedServiceAdpter implements RechargeManager {

	@Autowired
	private EventBus eventBus;
	@Autowired
	private DbService dbService;
	@Autowired
	private RechargeRecordDao rechargeRecordDao;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	
	/**
	 * 获得HashKey
	 * 
	 * @param  playerId			角色ID
	 * @param  startTime		开始时间
	 * @param  endTime			结束时间
	 * @return {@link String}	HashKey
	 */
	public String getSubKey(Date startTime) {
		return new StringBuilder().append(startTime).toString();
	}
	
	/**
	 * 获得充值记录ID
	 * 
	 * @param  playerId			角色ID
	 * @param  recordDate		记录日期
	 * @return {@link Long}		记录ID
	 */
	private Long getRechargeRecordId(long playerId, Date recordDate) {
		String hashKey = getHashKey(playerId);
		String subKey = getSubKey(RechargeRecord.toRecordDate(recordDate));
		Long recordId = (Long)cachedService.getFromCommonCache(hashKey, subKey);
		if(recordId == null) {
			recordId = rechargeRecordDao.getRechargeRecord(playerId, recordDate);
			cachedService.put2CommonHashCache(hashKey, subKey, recordId);
		}
		return recordId;
	}
	
	/**
	 * 增加金币到记录信息
	 * 
	 * @param  userDomain 	用户域模型
	 * @param  addGolden	增加的金币数量
	 */
	
	public void addRecharge2Record(UserDomain userDomain, long addGolden) {
		if(userDomain == null || addGolden <= 0) {
			return;
		}
		
		boolean persist = false;
		Date currentDate = new Date();
		Player player = userDomain.getPlayer();
		long playerId = userDomain.getPlayerId();
		Long recordId = this.getRechargeRecordId(playerId, currentDate);
		RechargeRecord rechargeRecord = this.get(recordId, RechargeRecord.class);
		if(rechargeRecord == null) {
			ChainLock lock = LockUtils.getLock(player);
			try {
				lock.lock();
				recordId = this.getRechargeRecordId(playerId, currentDate);
				rechargeRecord = this.get(recordId, RechargeRecord.class);
				if(rechargeRecord == null) {
					persist = true;
					rechargeRecord = new RechargeRecord();
					rechargeRecord.setPlayerId(playerId);
					rechargeRecord.setRecordTime(RechargeRecord.toRecordDate(currentDate));
				}
				
				rechargeRecord.addRecharge2List((int)addGolden);
				rechargeRecord.updateChargeList();
				if(persist) {
					commonDao.save(rechargeRecord);
					removeUserRechargeRecordIdCache(playerId);
				} else {
					dbService.updateEntityIntime(rechargeRecord);
					removeUserRechargeRecordIdCache(playerId); //为了安全啦
				}
			} catch (Exception e) {
				logger.error("{}", e);
				return;
			} finally {
				lock.unlock();
			}
		} else {
			ChainLock lock = LockUtils.getLock(rechargeRecord);
			try {
				lock.lock();
				rechargeRecord.addRecharge2List((int)addGolden);
				rechargeRecord.updateChargeList();
				dbService.updateEntityIntime(rechargeRecord);
			} finally {
				lock.unlock();
			}
		}
		eventBus.post(ChargeEvent.valueOf(userDomain));
	}

	/** 充值记录Key */
	private static final String PREFIX = "RECHARGE_RECORD_";
	
	/**
	 * 获得HashKey
	 * 
	 * @param  playerId			角色ID
	 * @return {@link String}	HashKey
	 */
	public String getHashKey(long playerId) {
		return new StringBuilder().append(PREFIX).append(Splitable.ATTRIBUTE_SPLIT).append(playerId).toString();
	}
	
	/**
	 * 获得HashKey
	 * 
	 * @param  playerId			角色ID
	 * @param  startTime		开始时间
	 * @param  endTime			结束时间
	 * @return {@link String}	HashKey
	 */
	public String getSubKey(String startTime, String endTime) {
		return new StringBuilder().append(startTime).append(Splitable.ATTRIBUTE_SPLIT)
								  .append(endTime).toString();
	}
	
	/**
	 * 列出充值记录ID列表
	 * 
	 * @param  playerId			角色ID
	 * @param  startDate		开始时间
	 * @param  endDate			结束时间
	 * @return {@link List}		主键ID列表
	 */
	@SuppressWarnings("unchecked")
	
	public List<Long> listRechargeRecordIds(long playerId, Date startDate, Date endDate) {
		String startTime = DateUtil.date2String(startDate, DatePattern.PATTERN_YYYY_MM_DD);
		String endTime = DateUtil.date2String(endDate, DatePattern.PATTERN_YYYY_MM_DD);
		String hashKey = getHashKey(playerId);
		String subKey = getSubKey(startTime, endTime);
		List<Long> list = (List<Long>)cachedService.getFromCommonCache(hashKey, subKey);
		if(list == null) {
			list = rechargeRecordDao.getRechargeRecordIds(playerId, startDate, endDate);
			cachedService.put2CommonHashCache(hashKey, subKey, list);
		}
		return list;
	}

	/**
	 * 查询充值记录对象
	 * 
	 * @param  recordIdList		充值记录ID列表
	 * @return {@link List}		充值记录对象列表
	 */
	
	public List<RechargeRecord> listRechargeRecord(Collection<Long> recordIdList) {
		return getEntityFromIdList(recordIdList, RechargeRecord.class);
	}

	/**
	 * 查询充值记录对象
	 * 
	 * @param  recordIdList				充值记录ID列表
	 * @return {@link List}				充值记录对象列表
	 */
	
	public RechargeRecord getRechargeRecord(long recordId) {
		return this.get(recordId, RechargeRecord.class);
	}

	/**
	 * 移除角色的充值缓存
	 * 
	 * @param playerId			角色ID
	 */
	
	public void removeUserRechargeRecordIdCache(long playerId) {
		cachedService.removeFromCommonCache(this.getHashKey(playerId));
	}
	
}
