
package com.yayo.warriors.module.drop.manager.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.scheduling.Scheduled;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.basedb.adapter.DropService;
import com.yayo.warriors.basedb.model.DropConfig;
import com.yayo.warriors.module.drop.dao.DropDao;
import com.yayo.warriors.module.drop.entity.DropRecord;
import com.yayo.warriors.module.drop.manager.DropManager;
import com.yayo.warriors.module.drop.model.Drop;
import com.yayo.warriors.module.drop.model.DropResult;
import com.yayo.warriors.module.drop.model.DropRewards;
import com.yayo.warriors.module.drop.rule.DropRule;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 掉落接口对象
 * 
 * @author Hyint
 */
@Service
public class DropManagerImpl implements DropManager, LogoutListener, DataRemoveListener {
	
	@Autowired
	private DropDao dropDao;
	@Autowired
	private UserManager userManager;
	@Autowired
	private DropService dropService;
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	/** 掉落限制 */
	private final ConcurrentHashMap<Long, DropRecord> DROP_RECORDS = new ConcurrentHashMap<Long, DropRecord>(5);
	
	
	public DropConfig getDropConfig(int rewardId) {
		return dropService.getRewardConfig(rewardId);
	}

	
	public DropConfig getDefaultDropConfig(int rewardNo) {
		return dropService.getDefaultByRewardNo(rewardNo);
	}
	
	/**
	 * 更新掉落信息
	 * 
	 * @param dropRecords
	 */
	public void updateDropRecords(Collection<DropRecord> dropRecords) {
		dropDao.update(dropRecords);
	}

	
	public void onLogoutEvent(UserDomain userDomain) {
		long playerId = userDomain.getPlayerId();
		if(!DROP_RECORDS.containsKey(playerId)) {
			return;
		}
		
		DropRecord dropRecord = DROP_RECORDS.get(playerId);
		if(dropRecord == null) {
			return;
		}
		
		ChainLock lock = LockUtils.getLock(dropRecord);
		try {
			lock.lock();
			dropRecord.updateInfo();
			dropDao.update(dropRecord);
		} catch (Exception e) {
			LOGGER.error("玩家:[{}] 登出保存掉落记录异常.", playerId, e);
		} finally {
			lock.unlock();
		}
	}
	

	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		DROP_RECORDS.remove(messageInfo.getPlayerId());
	}

	/**
	 * 获得掉落记录对象
	 * 
	 * @param  playerId				角色ID
	 * @return {@link DropRecord}	掉落记录对象
	 */
	private DropRecord getDropRecord(long playerId) {
		DropRecord dropRecord = this.getAndCreateDrop(playerId);
		if(dropRecord == null) {
			return dropRecord;
		}
	
		int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		if(dropRecord.getDay() == currentDay) {
			return dropRecord;
		}
		
		try {
			if(dropRecord.getDay() == currentDay) {
				return dropRecord;
			}
			dropRecord.setDropInfo("");
			dropRecord.setDay(currentDay);
			dropDao.update(dropRecord);
		} catch (Exception e) {
			LOGGER.error("{}", e);
		}
		return dropRecord;
	}
	
	/**
	 * 查询掉落奖励记录对象
	 * 
	 * @param  rewardId					奖励ID
	 * @return {@link DropRecord}		奖励记录对象
	 */
	private DropRecord getAndCreateDrop(long playerId) {
		DropRecord record = null;
		if(playerId < DropRecord.GLOBAL_DROP_ID) {
			return null;
		}
		
		record = DROP_RECORDS.get(playerId);
		if(record != null) {
			return record;
		}
		
		record = dropDao.get(playerId, DropRecord.class);
		if(record != null) {
			DROP_RECORDS.putIfAbsent(playerId, record);
			return DROP_RECORDS.get(playerId);
		}
		
		synchronized (this) {
			record = DROP_RECORDS.get(playerId);
			if(record != null) {
				return record;
			}
			
			try {
				DropRecord dropRecord = DropRecord.valueOf(playerId);
				dropDao.save(dropRecord);
				DROP_RECORDS.putIfAbsent(playerId, dropRecord);
			} catch (Exception e) {
				LOGGER.debug("{}", e);
			}
		}
		
		return DROP_RECORDS.get(playerId);
	}
	
	/**
	 * 创建掉落奖励
	 * 
	 * @param  playerId					角色ID
	 * @param  rewardNo					掉落奖励ID
	 * @param  count					奖励次数
	 * @param  job						角色的职业
	 * @return {@link List<Rewards>}	奖励模块返回值
	 */
	
	public List<DropRewards> dropRewards(long playerId, int rewardNo, int count) {
		List<DropRewards> rewardsList = new ArrayList<DropRewards>();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return rewardsList;
		}

		PlayerBattle battle = userDomain.getBattle();
		DropConfig defaultDrop = this.getDefaultDropConfig(rewardNo);
		if (defaultDrop == null) {
			LOGGER.error("掉落编号:[{}] 的默认掉落物品不存在..", rewardNo);
			return rewardsList;
		}

		int playerLevel = battle.getLevel();
		int playerJob = battle.getJob().ordinal();
		List<DropResult> rewardResultList = createReward(rewardNo, count);
		for (DropResult dropResult : rewardResultList) {
			if(dropResult == null || dropResult.getDrops().isEmpty()) {	//不存在, 或者不掉任何东西
				continue;
			}
			
			int rewardId = dropResult.getRewardId();
			DropConfig currentDrop = getDropConfig(rewardId);
			if (currentDrop == null) {
				LOGGER.error("奖励配置编号: [{}]  奖励ID: [{}]不存在", rewardNo, rewardId);
				continue;
			}

			int dropAmount = currentDrop.getAmount();				//掉落数量
			int maxAmount = currentDrop.getMaxAmount();				//最大数量
			int maxPersonal = currentDrop.getMaxPersonal();			//最大的个人掉落数量
			if(!currentDrop.isCanDrop(playerJob, playerLevel) || !isCanDrop(playerId, currentDrop)) {	//掉落默认掉落
				rewardId = defaultDrop.getId();
				dropAmount = defaultDrop.getAmount();
				maxAmount = defaultDrop.getMaxAmount();
				maxPersonal = defaultDrop.getMaxPersonal();
				dropResult = DropRule.createRewardResult(defaultDrop);
				if (dropResult == null || dropResult.getDrops().isEmpty()) {
					if(LOGGER.isDebugEnabled()){
						LOGGER.debug("掉落:[{}] 返回值信息为空, 或则没有掉落", rewardId);
					}
					continue;
				}
			}

			if(maxPersonal > 0) {
				this.addDropCount(playerId, rewardId, dropAmount);
			}

			if (maxAmount > 0) { // 全局掉落增长
				this.addDropCount(DropRecord.GLOBAL_DROP_ID, rewardId, dropAmount);
			}

			boolean notice = dropResult.isNotice();
			for (Drop drop : dropResult.getDrops()) {
				rewardsList.add(DropRewards.valueOf(drop, notice));
			}
		}
		return rewardsList;
	}
	
	/**
	 * 是否掉默认掉落
	 * 
	 * @param  playerId				角色ID
	 * @param  dropConfig			掉落信息
	 * @return {@link Boolean}		true-掉落默认, false-不能掉落默认
	 */
	private boolean isCanDrop(long playerId, DropConfig dropConfig) {
		int rewardId = dropConfig.getId();
		int maxAmount = dropConfig.getMaxAmount();
		int maxPersonal = dropConfig.getMaxPersonal();
		long globalDropId = DropRecord.GLOBAL_DROP_ID;
		if (!isCanDropReward(globalDropId, rewardId, maxAmount)) { 	//已达到上限
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("掉落编号:[{}] 已达到全局掉落限制", rewardId);
			}
			return false;
		}

		if (!isCanDropReward(playerId, rewardId, maxPersonal)) { 	//已达到个人掉落上限
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("掉落编号:[{}] 已达到个人掉落限制", rewardId);
			}
			return false;
		}
		return true;
	}
		
	/**
	 * 验证全局掉落
	 * 
	 * @param  ownerId			拥有者的ID
	 * @param  rewardId			奖励ID
	 * @param  maxAmount		最大数量
	 * @return {@link Boolean}	验证全局掉落, 是否可以掉落
	 */
	private boolean isCanDropReward(long ownerId, int rewardId, int maxAmount) {
		boolean canDrop = true;
		if(maxAmount > 0) {
			DropRecord globalRecord = this.getDropRecord(ownerId);
			canDrop = globalRecord.getDropCount(rewardId) < maxAmount;
		}
		return canDrop;
	}
 
	
	/**
	 * 增加掉落掉落
	 * 
	 * @param ownerId		拥有者的ID
	 * @param rewardId		掉落奖励ID
	 * @param addCount		掉落次数
	 */
	private void addDropCount(long ownerId, int rewardId, int addCount) {
		if(addCount != 0) {
			DropRecord globalRecord = this.getDropRecord(ownerId);
			if(globalRecord != null) {
				globalRecord.addDropCount(rewardId, addCount);
			}
		}
	}
	
	/**
	 * 创建奖励
	 * 
	 * @param  rewardId 		奖励编号
	 * @param  count 			奖励次数
	 * @return {@link List} 	奖励结果列表
	 */
	private List<DropResult> createReward(int rewardNo, int count) {
		List<DropResult> resultList = new ArrayList<DropResult>(count);
		List<DropConfig> dropList = dropService.listRewardConfig(rewardNo);
		if (dropList == null || dropList.isEmpty()) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("掉落编号:[{}] 的掉落物品不存在..", rewardNo);
			}
			return resultList;
		}
		
		// 掉落表总概率
		int totalRate = dropList.get(0).getFullValue();
		for (int curr = 0; curr < count; curr++) {	// 抽出一条掉落
			DropConfig dropInfo = DropRule.doRandomReward(dropList, totalRate);
			DropResult rewardResult = DropRule.createRewardResult(dropInfo);
			if (rewardResult != null) {	// 添加到结果列表
				resultList.add(rewardResult);
			}
		}
		return resultList;
	}
	
	/** 定时(每半小时)保存全局掉落记录 */
	@Scheduled(name = "保存全局掉落记录", value = "0 0/30 * * * ?")
	protected void ontimeSaveDropRecord() {
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("保存全局掉落记录...");
		}
		if(DROP_RECORDS.isEmpty()) {
			return;
		}
		
		Set<DropRecord> dropRecords = new HashSet<DropRecord>();
		Set<Long> playerIds = new HashSet<Long>(DROP_RECORDS.keySet());
		for (Long playerId : playerIds) {
			DropRecord dropRecord = this.getDropRecord(playerId);
			if(dropRecord == null) {
				continue;
			}
			
			ChainLock lock = LockUtils.getLock(dropRecord);
			try {
				lock.lock();
				dropRecord.updateInfo();
			} finally {
				lock.unlock();
				dropRecords.add(dropRecord);
			}
		}
		
		if(!dropRecords.isEmpty()) {
			try {
				updateDropRecords(dropRecords);
			} catch (Exception e) {
				LOGGER.error("定时更新掉落记录: {}", e);
			}
		}
	}
}
