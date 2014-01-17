package com.yayo.warriors.module.achieve.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.achieve.constant.AchieveConstant.*;
import static com.yayo.warriors.module.achieve.model.AchieveType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.TimeConstant;
import com.yayo.warriors.basedb.model.AchieveConfig;
import com.yayo.warriors.common.helper.AchievePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.achieve.entity.UserAchieve;
import com.yayo.warriors.module.achieve.facade.AchieveFacade;
import com.yayo.warriors.module.achieve.manager.AchieveManager;
import com.yayo.warriors.module.achieve.model.AchieveType;
import com.yayo.warriors.module.achieve.vo.AchieveVO;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.CouponLogger;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;

@Component
public class AchieveFacadeImpl implements AchieveFacade, LogoutListener {

	@Autowired
	private DbService dbService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AchieveManager achieveManager;
	
	
	
	public void commonAchieved(long playerId, AchieveType achieveType, int achieveValue) {
		UserAchieve userAchieve = achieveManager.getUserAchieve(playerId);
		if (userAchieve == null) {
			return;
		}
		
		int type = achieveType.ordinal();
		List<AchieveConfig> configs = achieveManager.listAchieveConfigs(type);
		for (AchieveConfig config : configs) {
			if (userAchieve.getAchieveIds().contains(config.getId())) {
				continue;
			}
			
			if (achieveValue >= config.getConditionValue()) {
				saveUserAchieve(userAchieve, config.getId());
				List<Integer> history = this.getNonReceivedIds(playerId);
				AchievePushHelper.pushAchieved2Client(playerId, config.getId(), history);
			}
		}
	}
	
	
	public void killMonsterAchieved(long playerId, int killMonsterCount) {
		UserAchieve userAchieve = achieveManager.getUserAchieve(playerId);
		if (userAchieve == null) {
			return;
		}
		
		int achieveType = MONSTER_ACHIEVE.ordinal();
		ChainLock lock = LockUtils.getLock(userAchieve);
		try {
			lock.lock();
			userAchieve.put2AchieveMap(achieveType, killMonsterCount);
			userAchieve.updateAchieved();
			dbService.submitUpdate2Queue(userAchieve);
		} finally {
			lock.unlock();
		}
		
		int monsterCount = userAchieve.getAchieveParams(achieveType);
		commonAchieved(playerId, MONSTER_ACHIEVE, monsterCount);
	}
	
	
	public void firstAchieved(long playerId, AchieveType achieveType, int firstType) {
		UserAchieve userAchieve = achieveManager.getUserAchieve(playerId);
		if (userAchieve == null) {
			return;
		}
		
		int type = achieveType.ordinal();
		List<AchieveConfig> configs = achieveManager.listAchieveConfigs(type);
		for (AchieveConfig config : configs) {
			if (userAchieve.getAchieveIds().contains(config.getId())) {
				continue;
			}
			
			if (firstType == config.getConditionValue()) {
				saveUserAchieve(userAchieve, config.getId());
				List<Integer> history = this.getNonReceivedIds(playerId);
				AchievePushHelper.pushAchieved2Client(playerId, config.getId(), history);
				break;
			}
		}
	}
	
	private void saveUserAchieve(UserAchieve achieve, int achieveId) {
		ChainLock lock = LockUtils.getLock(achieve);
		try {
			lock.lock();
			achieve.updateAchieveIds(achieveId);
			dbService.submitUpdate2Queue(achieve);
		} finally {
			lock.unlock();
		}
	}

	
	public List<Integer> listAchievesByType(long playerId, int achieveType) {
		UserAchieve userAchieve = achieveManager.getUserAchieve(playerId);
		if (userAchieve == null) {
			return Collections.emptyList();
		}
		
		Set<Integer> ids = userAchieve.getAchieveIds();
		if (ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<Integer> configIds = achieveManager.listAchieveIds(achieveType);
		if (configIds == null) {
			return Collections.emptyList();
		}
		
		List<Integer> effectIds = new ArrayList<Integer>(ids);
		effectIds.retainAll(configIds);
		return effectIds;
	}


	
	public AchieveVO getAllAchieves(long playerId) {
		UserAchieve userAchieve = achieveManager.getUserAchieve(playerId);
		AchieveVO achieveVO = new AchieveVO();
		if (userAchieve == null) {
			return achieveVO;
		}
		
		AchieveType[] achieveTypes = AchieveType.values();
		for (AchieveType type : achieveTypes) {
			int achieveType = type.ordinal();
			List<Integer> typeIds = listAchievesByType(playerId, achieveType);
			int typeCount = achieveManager.listAchieveConfigs(achieveType).size();
			
			switch (type) {
			case LEVEL_ACHIEVE:   achieveVO.setLevelAchieveCount(typeIds.size());      achieveVO.setAchieve0(typeCount);    break;
			case MONSTER_ACHIEVE: achieveVO.setMonsterAchieveCount(typeIds.size());    achieveVO.setAchieve1(typeCount);    break;
			case FIRST_ACHIEVE:   achieveVO.setFirstAchieveCount(typeIds.size());      achieveVO.setAchieve2(typeCount);    break;
			case LOGIN_ACHIEVE:   achieveVO.setLoginAchieveCount(typeIds.size());      achieveVO.setAchieve3(typeCount);    break;
			case TOTAL_LOGIN_ACHIEVE: achieveVO.setTotalAchieveCount(typeIds.size());  achieveVO.setAchieve4(typeCount);    break;
			case ONLINE_TIME_ACHIEVE: achieveVO.setOnlineAchieveCount(typeIds.size()); achieveVO.setAchieve5(typeCount);    break;
			}
		}
		
		achieveVO.setIds(userAchieve.getAchieveIds());
		return achieveVO;
	}


	
	@SuppressWarnings("unchecked")
	
	public int receiveAchieveReward(long playerId, int achieveId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		UserAchieve userAchieve = achieveManager.getUserAchieve(playerId);
		if (userAchieve == null) {
			return PLAYER_NOT_FOUND;
		}
		
		AchieveConfig config = achieveManager.getAchieveConfig(achieveId);
		if (config == null) {
			return BASEDATA_NOT_FOUND;
		}
		
		Set<Integer> ids = userAchieve.getAchieveIds();
		
		int key = config.getId() * 100;                         
		Player player = userDomain.getPlayer();
		ChainLock lock = LockUtils.getLock(player, userAchieve);
		try {
			lock.lock();
			if (!ids.contains(achieveId)) {
				return NOT_ACHIEVED;
			}
			
			if (userAchieve.isReceived(config.getId())) {
				return REWARD_RECEIVED;
			}
			
			player.increaseCoupon(config.getCouponReward());
			userAchieve.put2AchieveMap(key, 1);                  
			userAchieve.updateAchieved();
			dbService.submitUpdate2Queue(player, userAchieve);
		} finally {
			lock.unlock();
		}
		
		if(config.getCouponReward() != 0) {
			CouponLogger.inCome(Source.ACHIEVE_REWARD_RECEIVED, config.getCouponReward(), player);
		}
		List<Long> receiver = Arrays.asList(playerId);
		List<UnitId> playerUnits = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, receiver, playerUnits, AttributeKeys.COUPON);
		CouponLogger.inCome(Source.ACHIEVE_REWARD_RECEIVED, config.getCouponReward(), player);
		return SUCCESS;
	}


	
	
	
	public void onlineAchieved(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain != null) {
			Player player = userDomain.getPlayer();
			long curTimeMillis = System.currentTimeMillis();
			long curLoginTime = player.getLoginTime().getTime();
			long min = (curTimeMillis - curLoginTime) / TimeConstant.ONE_MINUTE_MILLISECOND ;            
			commonAchieved(playerId, ONLINE_TIME_ACHIEVE, (int) min);
		}
	}


	
	
	public int getAchieveValue(long playerId, int achieveType) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return -1;                
		}
		
		UserAchieve achieve = achieveManager.getUserAchieve(playerId);
		AchieveType type = EnumUtils.getEnum(AchieveType.class, achieveType);
		
		switch (type) {
			case LEVEL_ACHIEVE:	      return userDomain.getBattle().getLevel();  
			case MONSTER_ACHIEVE:     return achieve.getAchieveParams(achieveType);
			case LOGIN_ACHIEVE:       return userDomain.getPlayer().getContinueMaxDays();
			case TOTAL_LOGIN_ACHIEVE: return userDomain.getPlayer().getLoginDays();
		}
		
		return -1;
	}


	
	public List<Integer> receiveRewards(long playerId) {
		UserAchieve userAchieve = achieveManager.getUserAchieve(playerId);
		if (userAchieve == null) {
			return null;
		}
		
		Set<Integer> idSet = userAchieve.getAchieveIds();
		List<Integer> idList = new ArrayList<Integer>();
		for (int id : idSet) {
			if (userAchieve.isReceived(id)) {
				continue;
			}
			idList.add(id);
			receiveAchieveReward(playerId, id);
		}
		
		return idList;
	}
	
	
	
	
	public void checkLoginAchieved(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain != null) {
			Player player = userDomain.getPlayer();
			commonAchieved(playerId, LOGIN_ACHIEVE, player.getContinueMaxDays());   
			commonAchieved(playerId, TOTAL_LOGIN_ACHIEVE, player.getLoginDays());   
		}
	}

	
	
	
	public List<Integer> getNonReceivedIds(long playerId) {
		UserAchieve userAchieve = achieveManager.getUserAchieve(playerId);
		if (userAchieve == null) {
			return null;
		}
		
		List<Integer> nonReceived = new ArrayList<Integer>();
		Set<Integer> ids = userAchieve.getAchieveIds();
		for (int id : ids) {
			if (!userAchieve.isReceived(id)) {
				nonReceived.add(id);
			}
		}
		
		return nonReceived;
	}
	
	
	
	
	
	public void onLogoutEvent(UserDomain userDomain) {
		
	}
	

}
