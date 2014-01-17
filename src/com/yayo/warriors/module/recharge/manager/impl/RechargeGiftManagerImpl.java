package com.yayo.warriors.module.recharge.manager.impl;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.module.recharge.entity.RechargeGift;
import com.yayo.warriors.module.recharge.manager.RechargeGiftManager;
import com.yayo.warriors.module.recharge.model.RewardCount;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 充值礼包Manager类
 * 
 * @author Hyint
 */
@Service
public class RechargeGiftManagerImpl extends CachedServiceAdpter implements RechargeGiftManager {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private DbService dbService;
	
	
	public RechargeGift getRechargeGift(UserDomain userDomain) {
		RechargeGift rechargeGift = null;
		if(userDomain != null) {
			rechargeGift = this.get(userDomain.getId(), RechargeGift.class);
			this.validChargeGiftRewardCountTimeout(rechargeGift);
		}
		
		return rechargeGift;
	}

	/**
	 * 校验奖励次数过期
	 * 
	 * @param rechargeGift		充值礼包对象
	 */
	private void validChargeGiftRewardCountTimeout(RechargeGift rechargeGift) {
		if(rechargeGift == null) {
			return;
		} 

		Map<Integer, RewardCount> rewardInfoMap = rechargeGift.getRewardInfoMap();
		if(rewardInfoMap.isEmpty()) {
			return;
		}
		
		boolean update = false;
		ChainLock lock = LockUtils.getLock(rechargeGift);
		try {
			lock.lock();
			rewardInfoMap = rechargeGift.getRewardInfoMap();
			if(rewardInfoMap.isEmpty()) {
				return;
			}
			for (Iterator<Entry<Integer, RewardCount>> it = rewardInfoMap.entrySet().iterator(); it.hasNext();) {
				Entry<Integer, RewardCount> entry = it.next();
				RewardCount rewardCount = entry.getValue();
				if(rewardCount == null) {
					it.remove();
					continue;
				}
				
				long endTime = rewardCount.getEndTime();
				if(endTime > 0 && endTime <= System.currentTimeMillis()) {
					rewardCount.setEndTime(0L);
					rewardCount.getConditions().clear();
					update = true;
				}
			}
			
			if(update) {
				rechargeGift.updateRewardInfoMap();
				dbService.submitUpdate2Queue(rechargeGift);
			}
		} finally {
			lock.unlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == RechargeGift.class) {
			RechargeGift rechargeGift = commonDao.get(id, RechargeGift.class);
			if(rechargeGift != null) {
				return (T) rechargeGift;
			}
			
			try {
				rechargeGift = RechargeGift.valueOf((Long)id);
				commonDao.save(rechargeGift);
				return (T) rechargeGift;
			} catch (Exception e) {
				logger.error("角色:[{}] 充值礼包异常: {}", id, e);
				return null;
			}
		}
		return super.getEntityFromDB(id, clazz);
	}
}
