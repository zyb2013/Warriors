package com.yayo.warriors.module.recharge.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.recharge.constant.ChargeGiftConstant.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.basedb.adapter.ChargeGiftService;
import com.yayo.warriors.basedb.model.ChargeConditionConfig;
import com.yayo.warriors.basedb.model.ChargeConfig;
import com.yayo.warriors.basedb.model.ChargeRewardConfig;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.CouponLogger;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.CreateResult;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.recharge.entity.RechargeGift;
import com.yayo.warriors.module.recharge.entity.RechargeRecord;
import com.yayo.warriors.module.recharge.facade.ChargeGiftFacade;
import com.yayo.warriors.module.recharge.manager.RechargeGiftManager;
import com.yayo.warriors.module.recharge.manager.RechargeManager;
import com.yayo.warriors.module.recharge.model.CacheRechargeGift;
import com.yayo.warriors.module.recharge.model.GiftContext;
import com.yayo.warriors.module.recharge.model.GiftReward;
import com.yayo.warriors.module.recharge.model.RechargeGiftVO;
import com.yayo.warriors.module.recharge.model.RewardCount;
import com.yayo.warriors.module.recharge.model.RewardIdVO;
import com.yayo.warriors.module.recharge.type.ChargeGiftType;
import com.yayo.warriors.module.recharge.type.GiftState;
import com.yayo.warriors.module.recharge.type.GiftType;
import com.yayo.warriors.module.task.model.RewardVO;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.type.GoodsType;

/**
 * 充值礼包接口
 * 
 * @author Hyint
 */
@Component
public class ChargeGiftFacadeImpl implements ChargeGiftFacade {

	/** 礼包表 */
	private static final ConcurrentHashMap<Long, GiftContext> CACHE_MAPS = new ConcurrentHashMap<Long, GiftContext>();
	
	@Autowired
	private DbService dbService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private RechargeManager rechargeManager;
	@Autowired
	private ChargeGiftService chargeGiftService;
	@Autowired
	private RechargeGiftManager rechargeGiftManager;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired(required=true)
	@Qualifier("GAME_SERVER_FIRST_OPEN")
	private String openServerTime;
	
	/**
	 * 获得充值活动的开始时间
	 * 
	 * @return {@link Date}		开始时间
	 */
	protected Date getStartTime() {
		return DateUtil.string2Date(openServerTime, DatePattern.PATTERN_YYYY_MM_DD);
	}
	
	/**
	 * 列出充值礼包VO对象
	 * 
	 * @param  playerId			角色ID
	 * @return {@link List}		充值礼包列表
	 */
	
	public GiftContext getGiftContext(long playerId, boolean recalculate) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return null;
		}
		
		RechargeGift rechargeGift = rechargeGiftManager.getRechargeGift(userDomain);
		if(rechargeGift == null) {
			return null;
		}
		
		GiftContext giftContext = CACHE_MAPS.get(playerId);
		if(giftContext == null) { //不存在, 则初始化充值礼包
			CACHE_MAPS.put(playerId, GiftContext.valueOf(playerId));
			giftContext = CACHE_MAPS.get(playerId);
			giftContext.updateRecalculate(true);
		}
		
		if(giftContext.isRecalculate() || recalculate) {
			ChainLock lock = LockUtils.getLock(userDomain.getPlayer());
			try {
				lock.lock();
				giftContext.updateRecalculate(false);
				giftContext.getRechargeGiftMap().clear();
				giftContext.addAll(initCacheGift(rechargeGift));
			} finally {
				lock.unlock();
			}
			return giftContext;
		}
		return checkRechargeGift(userDomain, rechargeGift, giftContext);
	}
	
	/**
	 * 列出充值礼包VO列表
	 * 
	 * @param  playerId					角色ID
	 * @param  recalculate				重新计算礼包
	 * @return {@link List}				充值礼包VO列表
	 */
	
	public List<RechargeGiftVO> listRechargeGiftVO(long playerId, boolean recalculate) {
		GiftContext giftContext = this.getGiftContext(playerId, recalculate);
		if(giftContext == null) {
			return Collections.emptyList();
		}
		
		List<RechargeGiftVO> rechargeVOList = new LinkedList<RechargeGiftVO>();
		for (CacheRechargeGift cacheRechargeGift : new LinkedList<CacheRechargeGift>(giftContext.values())) {
			if(cacheRechargeGift.isTimeOut()) {
				continue;
			}
			
			RechargeGiftVO rechargeGiftVO = null;
			try {
				rechargeGiftVO = RechargeGiftVO.valueOf(cacheRechargeGift);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(rechargeGiftVO != null) {
				rechargeVOList.add(rechargeGiftVO);
			}
		}
		return rechargeVOList;
	}

	/**
	 * 检查充值礼包条件
	 * 
	 * @param  userDomain			用户域模型
	 * @param  userRechargeGift		礼包VO列表
	 * @param  giftContext			礼包上下文
	 * @return {@link List}			充值礼包VO列表
	 */
	private GiftContext checkRechargeGift(UserDomain userDomain, RechargeGift userRechargeGift, GiftContext giftContext) {
		if(giftContext == null || userRechargeGift == null) {
			return giftContext;
		}
		
		Map<Integer, CacheRechargeGift> rechargeGiftMap = giftContext.getRechargeGiftMap();
		if(rechargeGiftMap == null || rechargeGiftMap.isEmpty()) {
			return giftContext;
		}
		
		ChainLock lock = LockUtils.getLock(userDomain.getPlayer());
		try {
			lock.lock(); 
			rechargeGiftMap = giftContext.getRechargeGiftMap();
			if(rechargeGiftMap == null || rechargeGiftMap.isEmpty()) {
				return giftContext;
			}
			
			boolean recalculate = false;
			Set<Entry<Integer, CacheRechargeGift>> entrySet = rechargeGiftMap.entrySet();
			for (Iterator<Entry<Integer, CacheRechargeGift>> it = entrySet.iterator(); it.hasNext();) {
				Entry<Integer, CacheRechargeGift> entry = it.next();
				CacheRechargeGift rechargeGift = entry.getValue();
				if(rechargeGift == null) { 
					continue;
				}

				if(rechargeGift.isTimeOut() || rechargeGift.isNeedRecalculate()) { //未到结束时间
					recalculate = true;
					break;
				}
			}
			
			if(recalculate) {
				giftContext.getRechargeGiftMap().clear();
				giftContext.addAll(initCacheGift(userRechargeGift));
			}
		} finally {
			lock.unlock(); 
		}
		return giftContext;
	}
	
	/**
	 * 需要计算的周期
	 * 
	 * @param  cycle 			计算本次周期的开始时间.
	 * @param  canCycle			是否可以周期循环
	 * @return {@link Date}		{@link Date}
	 */
	private Date calcCycleCurrentStartTime(int cycle, boolean canCycle) {
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(getStartTime());
		startCalendar.set(Calendar.HOUR, 0);
		startCalendar.set(Calendar.MINUTE, 0);
		startCalendar.set(Calendar.SECOND, 0);
		startCalendar.set(Calendar.MILLISECOND, 0);
		if(canCycle) {
			int betweenDay = DateUtil.calc2DateTDOADays(getStartTime(), new Date());
			startCalendar.add(Calendar.DATE, (betweenDay / cycle) * cycle); //相隔的周期数量
		}
		return startCalendar.getTime();	//周期的开始时间
	}

	/**
	 * 计算本次活动的结束时间
	 * 
	 * @param  startTime		活动的开始时间
	 * @param  cycle			活动开放的周期
	 * @return {@link Date}		结束时间
	 */
	private Date calcCycleCurrentEndTime(Date startTime, int cycle) {
		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(startTime);
		endCalendar.set(Calendar.HOUR, 0);
		endCalendar.set(Calendar.MINUTE, 0);
		endCalendar.set(Calendar.SECOND, 0);
		endCalendar.set(Calendar.MILLISECOND, 0);
		endCalendar.add(Calendar.DATE, cycle);
		endCalendar.add(Calendar.SECOND, -1);
		return endCalendar.getTime();
	}
	
	/**
	 * 计算单笔最大充值额度
	 * 
	 * @param  records			充值记录列表
	 * @param  maxGolden		最大的金币数
	 * @return {@link Integer}	返回充值的最大金币数
	 */
	private int calculateMaxRechargeGolden(List<Long> records, int maxGolden) {
		if(records == null || records.isEmpty()) {
			return 0;
		}
		
		int totalGolden = 0;
		for (Long rechargeRecordId : records) {
			try {
				RechargeRecord rechargeRecord = rechargeManager.getRechargeRecord(rechargeRecordId);
				if(rechargeRecord == null) {
					continue;
				}
				
				List<Integer> chargeList = rechargeRecord.getChargeList();
				if(chargeList == null || chargeList.isEmpty()) {
					continue;
				}
				
				List<Integer> charges = new ArrayList<Integer>(chargeList);
				Collections.sort(charges);
				int chargeValue = charges.get(charges.size() - 1);
				totalGolden = Math.max(totalGolden, chargeValue);
				if(totalGolden >= maxGolden) {
					return totalGolden;
				}
			} finally {}
		}
		return totalGolden;
	}
	
	
	/**
	 * 解析首充礼包(约定好永久生效)
	 * 
	 * @param  rechargeGift					用户充值礼包
	 * @param  charges						礼包列表	
	 * @return {@link CacheRechargeGift}	缓存礼包列表
	 */
	private CacheRechargeGift parseFirstCacheGift(RechargeGift rechargeGift, List<ChargeConfig> charges) {
		if(charges == null || charges.isEmpty()) {
			return null;
		}
		
		ChargeConfig chargeConfig = charges.get(0);
		List<ChargeConditionConfig> conditions = chargeConfig.getConditions();
		if(conditions == null || conditions.isEmpty() ) {
			return null;
		}
		
		ChargeConditionConfig chargeCondition = conditions.get(0);
		if(chargeCondition == null) {
			return null;
		}
		
		List<ChargeRewardConfig> rechargeRewards = chargeCondition.getRechargeRewards();
		if(rechargeRewards == null || rechargeRewards.isEmpty()) {
			return null;
		}
		
		ChargeRewardConfig chargeRewardConfig = rechargeRewards.get(0);
		if(chargeRewardConfig == null) {
			return null;
		}
		
		int currentGolden = 0;
		boolean returnNull = true;
		long playerId = rechargeGift.getId();
		int rewardId = chargeRewardConfig.getId();
		RewardCount rewardCount = rechargeGift.getRewardCount(rewardId);
		Map<Integer, RewardIdVO> rewardIdVOMap = new HashMap<Integer, RewardIdVO>(0);					//最终的奖励IDVO集合
		//不存在奖励, 或者存在, 但是不包含该条件.
		if(rewardCount == null || !rewardCount.containsCondition(chargeCondition.getId())) {
			List<Long> records = rechargeManager.listRechargeRecordIds(playerId, null, null);			//所有充值列表
			currentGolden = calculateMaxRechargeGolden(records, chargeCondition.getMaxCondition());		//计算充值金币是否满足
			if(currentGolden < chargeCondition.getCondition()) {
				returnNull = false;
				rewardIdVOMap.put(rewardId, RewardIdVO.valueOf(rewardId, GiftState.LIMITED));
			} else {
				returnNull = false;
				rewardIdVOMap.put(rewardId, RewardIdVO.valueOf(rewardId, GiftState.CANREWARD));
			}
		} else if(rewardCount.containsCondition(chargeCondition.getId())) { //已领取过, 直接设置为已领取
			returnNull = true;
			currentGolden = chargeCondition.getCondition();
			rewardIdVOMap.put(rewardId, RewardIdVO.valueOf(rewardId, GiftState.REWARDED));
		}
		
		CacheRechargeGift cacheRechargeGift = null;
		if(!returnNull) {	//创建首充对象
			cacheRechargeGift = new CacheRechargeGift();
			cacheRechargeGift.setEndTime(-1L);
			cacheRechargeGift.setChargeGift(chargeConfig);
			cacheRechargeGift.setCurrentGolden(currentGolden);
			cacheRechargeGift.setRewardIdVOMap(rewardIdVOMap);
			cacheRechargeGift.setChargeCondition(chargeCondition);
		}
		return cacheRechargeGift;
	}
	
	/**
	 * 解析累积充值礼包
	 * 
	 * @param  rechargeGift					用户充值礼包
	 * @param  charges						礼包列表	
	 * @return {@link CacheRechargeGift}	缓存礼包列表
	 */
	private CacheRechargeGift parseAccumulateCacheGift(RechargeGift rechargeGift, List<ChargeConfig> charges){
		if(charges == null || charges.isEmpty()) {
			return null;
		}
		
		ChargeConfig chargeConfig = charges.get(0);
		List<ChargeConditionConfig> conditions = chargeConfig.getConditions();
		if(conditions == null || conditions.isEmpty() ) {
			return null;
		}
		
		ChargeConditionConfig chargeCondition = conditions.get(0);
		if(chargeCondition == null) {
			return null;
		}
		
		List<ChargeRewardConfig> rechargeRewards = chargeCondition.getRechargeRewards();
		if(rechargeRewards == null || rechargeRewards.isEmpty()) {
			return null;
		}
		
		ChargeRewardConfig chargeRewardConfig = rechargeRewards.get(0);
		if(chargeRewardConfig == null) {
			return null;
		}

		int currentGolden = 0;																//当前的金币
		Date currentDate = new Date();														//当前的时间
		long playerId = rechargeGift.getId();												//角色ID
		int cycle = chargeConfig.getTotalCycle();											//礼包的周期
		int rewardId = chargeRewardConfig.getId();											//奖励ID
		boolean canCycle = chargeConfig.getSequence() > 0;									//是否可以循环
		Date startTime = calcCycleCurrentStartTime(cycle, canCycle);						//礼包的开始时间
		Date endTime = calcCycleCurrentEndTime(startTime, cycle);							//活动的结束时间
		RewardCount rewardCount = rechargeGift.getRewardCount(rewardId);					//领取信息
		Map<Integer, RewardIdVO> rewardIdVOMap = new HashMap<Integer, RewardIdVO>(0);		//最终的奖励IDVO集合
		boolean isActivity = startTime.before(currentDate) && currentDate.before(endTime);	//是否在活动中
		if(rewardCount == null) {
			if(!isActivity) { //不在活动中
				rewardIdVOMap.put(rewardId, RewardIdVO.valueOf(rewardId, GiftState.LIMITED));
			} else {
				currentGolden = checkRechargeCondition(playerId, rewardId, chargeConfig, chargeCondition, startTime, endTime, rewardIdVOMap);
			}
		} else {
			if(rewardCount.containsCondition(chargeCondition.getId())) { //已领取过, 直接设置为已领取
				currentGolden = chargeCondition.getCondition();
				rewardIdVOMap.put(rewardId, RewardIdVO.valueOf(rewardId, GiftState.REWARDED));
			} else {
				currentGolden = checkRechargeCondition(playerId, rewardId, chargeConfig, chargeCondition, startTime, endTime, rewardIdVOMap);
			}
		}
		
		//创建累计充值礼包
		CacheRechargeGift cacheRechargeGift = new CacheRechargeGift();
		cacheRechargeGift.setChargeGift(chargeConfig);
		cacheRechargeGift.setEndTime(endTime.getTime());
		cacheRechargeGift.setCurrentGolden(currentGolden);
		cacheRechargeGift.setRewardIdVOMap(rewardIdVOMap);
		cacheRechargeGift.setChargeCondition(chargeCondition);
		if(endTime.before(currentDate)) {	//超过结束时间, 不需要重新计算.
			cacheRechargeGift.setNeedRecalculate(false);
		}
		return cacheRechargeGift;
	}


	private int checkRechargeCondition(long playerId, int rewardId, ChargeConfig chargeConfig, ChargeConditionConfig chargeCondition, Date startTime, Date endTime, Map<Integer, RewardIdVO> rewardIdVOMap) {
		int currentGolden = 0;
		List<Long> records = rechargeManager.listRechargeRecordIds(playerId, startTime, endTime);	//所有充值列表
		if(chargeConfig.getChargeType() == ChargeGiftType.SINGLE_RECHARGE) {
			currentGolden = calculateMaxRechargeGolden(records, chargeCondition.getMaxCondition());		//计算充值金币是否满足
		} else if(chargeConfig.getChargeType() == ChargeGiftType.ACCUMULATE_RECHARGE) {
			currentGolden = calculateTotalRechargeGolden(records, chargeCondition.getMaxCondition());		//计算充值金币是否满足
		}
		
		if(currentGolden < chargeCondition.getCondition()) {
			rewardIdVOMap.put(rewardId, RewardIdVO.valueOf(rewardId, GiftState.LIMITED));
		} else {
			rewardIdVOMap.put(rewardId, RewardIdVO.valueOf(rewardId, GiftState.CANREWARD));
		}
		return currentGolden;
	}
	
	/**
	 * 解析周期礼包
	 * 
	 * @param  rechargeGift					用户充值礼包
	 * @param  charges						礼包列表	
	 * @return {@link CacheRechargeGift}	缓存礼包列表
	 */
	private CacheRechargeGift parseGiftCycleGift(RechargeGift rechargeGift, List<ChargeConfig> charges) {
		if(charges == null || charges.isEmpty()) {
			return null;
		}
		
		ChargeConfig recharge = null;
		ChargeConfig charge = charges.get(0);
		int totalCycle = charge.getTotalCycle();
		boolean canCycle = charge.getSequence() > 0;
		Date startTime = calcCycleCurrentStartTime(totalCycle, canCycle); 	//本次周期的开始时间
		Calendar startCalendar = Calendar.getInstance();					
		startCalendar.setTime(startTime);									//设置开始时间
		startCalendar.set(Calendar.HOUR, 0);								//设置为0时
		startCalendar.set(Calendar.MINUTE, 0);								//设置为0分
		startCalendar.set(Calendar.SECOND, 0);								//设置为0秒
		startCalendar.set(Calendar.MILLISECOND, 0);							//设置为0毫秒

		int calculateCycle = 0;
		Date calcTime = startCalendar.getTime();
		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(calcTime);
		endCalendar.set(Calendar.HOUR, 23);									//设置为23时
		endCalendar.set(Calendar.MINUTE, 59);								//设置为59分
		endCalendar.set(Calendar.SECOND, 59);								//设置为59秒
		endCalendar.set(Calendar.MILLISECOND, 0);							//设置为0毫秒
		long currentSecond = DateUtil.getCurrentSecond();
		for (ChargeConfig chargeConfig : charges) {
			calculateCycle += chargeConfig.getCycle();
			Calendar currentCalendar = Calendar.getInstance();
			currentCalendar.setTime(calcTime);
			currentCalendar.add(Calendar.DATE, calculateCycle - 1);
			currentCalendar.set(Calendar.HOUR, 23);								//设置为23时
			currentCalendar.set(Calendar.MINUTE, 59);							//设置为59分
			currentCalendar.set(Calendar.SECOND, 59);							//设置为59秒
			currentCalendar.set(Calendar.MILLISECOND, 0);						//设置为0毫秒
			recharge = chargeConfig;											//先设置礼包对象
			long endSecond = DateUtil.toSecond(currentCalendar.getTimeInMillis());
			long startSecond = DateUtil.toSecond(startCalendar.getTimeInMillis());
			if(startSecond <= currentSecond && currentSecond <= endSecond) {
				endCalendar.setTime(currentCalendar.getTime());
				break;
			}
			startCalendar.add(Calendar.DATE, chargeConfig.getCycle());
		}
		
		CacheRechargeGift cacheRechargeGift = null;
		if(recharge != null) { //没有在活动时间内的. 直接不做这个活动了
			cacheRechargeGift = parseGiftCycleGift(rechargeGift, recharge, startCalendar.getTime(), endCalendar.getTime());
		}
		return cacheRechargeGift;
	}

	/**
	 * 计算充值的金币总额是否满足需求
	 * 
	 * @param  records			充值记录列表
	 * @param  maxGolden		最大的金币数
	 * @return {@link Integer}	返回充值的最大金币数
	 */
	private int calculateTotalRechargeGolden(List<Long> records, int maxGolden) {
		int totalGolden = 0;
		if(records != null && !records.isEmpty()) {
			for (Long rechargeRecordId : records) {
				RechargeRecord rechargeRecord = rechargeManager.getRechargeRecord(rechargeRecordId);
				totalGolden += rechargeRecord != null ? rechargeRecord.getTotalRecharge() : 0;
				if(totalGolden >= maxGolden) {
					return totalGolden;
				}
			}
		}
		return totalGolden;
	}
	
	/**
	 * 解析礼包循环周期
	 * 
	 * @param  rechargeGift					用户充值礼包对象
	 * @param  recharge						充值礼包基础对象
	 * @return {@link CacheRechargeGift}	缓存充值礼包对象
	 */
	private CacheRechargeGift parseGiftCycleGift(RechargeGift rechargeGift, ChargeConfig recharge, Date startTime, Date endTime) {
		int rechargeValue = 0; 
		long playerId = rechargeGift.getId();															//角色ID
		ChargeConditionConfig currentCondition = null;													//当前的条件配置
		ChargeConditionConfig conditionObject = recharge.getConditions().get(0);						//取得充值条件
		Map<Integer, RewardCount> rewardInfoMap = rechargeGift.getRewardInfoMap();						//充值集合对象
		Map<Integer, RewardIdVO> rewardIdVOMap = new HashMap<Integer, RewardIdVO>(0);					//最终的奖励IDVO集合
		Map<Integer, RewardIdVO> rewardedIdVOMap = new HashMap<Integer, RewardIdVO>(0);					//已经领取过的奖励VO列表
		List<Long> records = rechargeManager.listRechargeRecordIds(playerId, startTime, endTime);		//所有充值列表
		if(recharge.getChargeType() == ChargeGiftType.SINGLE_RECHARGE) {
			rechargeValue = calculateMaxRechargeGolden(records, conditionObject.getMaxCondition());		//计算充值金币是否满足
		} else if(recharge.getChargeType() == ChargeGiftType.ACCUMULATE_RECHARGE) {
			rechargeValue = calculateTotalRechargeGolden(records, conditionObject.getMaxCondition());		//计算充值金币是否满足
		}
		
		OUT_BREAK_POINT: for (ChargeConditionConfig rechargeCondition : recharge.getConditions()) {
			if(rechargeValue < rechargeCondition.getCondition()) { //当前充值金额没达到条件
				for (ChargeRewardConfig rechargeReward : rechargeCondition.getRechargeRewards()) {
					int rewardId = rechargeReward.getId();
					rewardIdVOMap.put(rewardId, RewardIdVO.valueOf(rewardId, GiftState.LIMITED));
				}
				rewardIdVOMap.putAll(rewardedIdVOMap);	//把已经领取过的信息加入列表中
				currentCondition = rechargeCondition;
				break OUT_BREAK_POINT;
			} else { //达到了本次的充值条件. 检查是否检测下一次记录
				boolean needCalculateNextRewards = false;
				Map<Integer, RewardIdVO> cacheRewardIdVOMap = new HashMap<Integer, RewardIdVO>();
				for (ChargeRewardConfig rechargeReward : rechargeCondition.getRechargeRewards()) {
					int rewardId = rechargeReward.getId();
					RewardCount rewardCount = rewardInfoMap.get(rewardId);
					if(rewardCount == null) { //没有领取过. 则判断是否充值, 充值了就可以领取
						cacheRewardIdVOMap.put(rewardId, RewardIdVO.valueOf(rewardId, GiftState.CANREWARD));
					} else { //已存在, 则判断是否已经领取过奖励
						if(rewardCount.containsCondition(rechargeCondition.getId())) { //已经领取过了, 不能再领取了
							needCalculateNextRewards = true;
							rewardedIdVOMap.put(rewardId, RewardIdVO.valueOf(rewardId, GiftState.REWARDED));
						} else { //没有领取过, 则设置为可以领取
							cacheRewardIdVOMap.put(rewardId, RewardIdVO.valueOf(rewardId, GiftState.CANREWARD));
						} 
					}
				}
				
				if(!needCalculateNextRewards) { 			//不需要计算下一级的奖励
					currentCondition = rechargeCondition;
					rewardIdVOMap.putAll(cacheRewardIdVOMap);
					break OUT_BREAK_POINT;
				}
				currentCondition = rechargeCondition;
			}
		}
		
		rewardIdVOMap.putAll(rewardedIdVOMap);	//把已经领取过的信息加入列表中
		CacheRechargeGift cacheRechargeGift = new CacheRechargeGift();
		cacheRechargeGift.setChargeGift(recharge);
		cacheRechargeGift.setEndTime(endTime.getTime());
		cacheRechargeGift.setCurrentGolden(rechargeValue);
		cacheRechargeGift.setRewardIdVOMap(rewardIdVOMap);
		cacheRechargeGift.setChargeCondition(currentCondition);
		if(endTime.before(new Date())) {	//超过结束时间,不需要重算
			cacheRechargeGift.setNeedRecalculate(false);
		}
		
		return cacheRechargeGift;
	}
	
	
	/**
	 * 构建首次充值礼包
	 * 
	 * @param  rechargeGift		用户充值礼包对象
	 * @return {@link List}		充值礼包列表
	 */
	private List<CacheRechargeGift> constFirstRechargeGift(RechargeGift rechargeGift) {
		List<CacheRechargeGift> cacheRechargeGifts = new LinkedList<CacheRechargeGift>();
		for (Integer giftType : chargeGiftService.listGiftTypeByType(GiftType.FIRST_RECHARGE)) {
			List<ChargeConfig> charges = chargeGiftService.listChargeConfig(giftType, false);
			CacheRechargeGift cacheGift = parseFirstCacheGift(rechargeGift, charges);
			if(cacheGift != null) {
				cacheRechargeGifts.add(cacheGift);
			}
		}
		return cacheRechargeGifts;
	}

	/**
	 * 构建累计充值礼包
	 * 
	 * @param  rechargeGift		用户充值礼包对象
	 * @return {@link List}		充值礼包列表
	 */
	private List<CacheRechargeGift> constAccumulateRechargeGift(RechargeGift rechargeGift) {
		List<CacheRechargeGift> cacheRechargeGifts = new LinkedList<CacheRechargeGift>();
		for (Integer giftType : chargeGiftService.listGiftTypeByType(GiftType.ACCUMULATE_RECHARGE)) {
			List<ChargeConfig> charges = chargeGiftService.listChargeConfig(giftType, false);
			CacheRechargeGift cacheGift = parseAccumulateCacheGift(rechargeGift, charges);
			if(cacheGift != null) {
				cacheRechargeGifts.add(cacheGift);
			}
		}
		return cacheRechargeGifts;
	}
	
	/**
	 * 构建充值循环礼包
	 * 
	 * @param  rechargeGift		用户充值礼包对象
	 * @return {@link List}		充值礼包列表
	 */
	private List<CacheRechargeGift> constGiftCycle2Gift(RechargeGift rechargeGift) {
		List<CacheRechargeGift> cacheRechargeGifts = new LinkedList<CacheRechargeGift>();
		for (Integer giftType : chargeGiftService.listGiftTypeByType(GiftType.CYCLE_RECHARGE)) {
			List<ChargeConfig> charges = chargeGiftService.listChargeConfig(giftType, true);
			CacheRechargeGift cacheGift = parseGiftCycleGift(rechargeGift, charges);
			if(cacheGift != null) {
				cacheRechargeGifts.add(cacheGift);
			}
		}
		return cacheRechargeGifts;
	}
	
	/**
	 * 初始化礼包列表
	 * 
	 * @param  rechargeGift		充值礼包
	 * @return {@link List}		重置礼包VO列表
	 */
	private List<CacheRechargeGift> initCacheGift(RechargeGift rechargeGift) {
		List<CacheRechargeGift> cacheRechargeGifts = new LinkedList<CacheRechargeGift>();
		cacheRechargeGifts.addAll(constFirstRechargeGift(rechargeGift));
		cacheRechargeGifts.addAll(constAccumulateRechargeGift(rechargeGift));
		cacheRechargeGifts.addAll(constGiftCycle2Gift(rechargeGift));
		return cacheRechargeGifts;
	}
	
	
	public void onLogoutEvent(UserDomain userDomain) {
		CACHE_MAPS.remove(userDomain.getPlayerId());
	}

	/**
	 * 领取充值礼包奖励
	 * 
	 * @param  playerId					角色ID
	 * @param  giftId					礼包ID
	 * @param  rewardId					奖励ID
	 * @return {@link Integer}			充值礼包模块返回值
	 */
	@SuppressWarnings("unchecked")
	
	public int rewardRechargeGift(long playerId, int giftId, int rewardId) {
		GiftContext giftContext = getGiftContext(playerId, false);
		if(giftContext == null) {
			return CHARGE_GIFT_NOT_FOUND;
		}
		
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		RechargeGift rechargeGift = rechargeGiftManager.getRechargeGift(userDomain);
		if(rechargeGift == null) {
			return CHARGE_GIFT_NOT_FOUND;
		}
		
		ChargeConfig chargeConfig = chargeGiftService.get(giftId, ChargeConfig.class);
		if(chargeConfig == null) {
			return CHARGE_GIFT_NOT_FOUND;
		}

		//缓存的礼包信息
		CacheRechargeGift cacheRechargeGift = giftContext.get(giftId);
		if(cacheRechargeGift == null) {
			return CHARGE_GIFT_NOT_FOUND;
		}
		
		RewardIdVO rewardIdVO = cacheRechargeGift.getRewardIdVO(rewardId);
		if(rewardIdVO == null) {
			return CHARGE_GIFT_NOT_FOUND;
		} else if(rewardIdVO.getState() != GiftState.CANREWARD) {
			return CHARGE_GIFT_STATE_INVALID;
		}
		
		ChargeRewardConfig chargeReward = chargeGiftService.get(rewardId, ChargeRewardConfig.class);
		if(chargeReward == null) {
			return CHARGE_GIFT_NOT_FOUND;
		}

		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		CreateResult<UserProps, UserEquip> createResult = null;
		int backpackSize = propsManager.getBackpackSize(playerId, backpack);
		GiftReward constGiftReward = this.constGiftReward(playerId, chargeReward);
		ChainLock lock = LockUtils.getLock(player, battle, player.getPackLock(), rechargeGift);
		try {
			lock.lock();
			cacheRechargeGift = giftContext.get(giftId);
			if(cacheRechargeGift == null) {
				return CHARGE_GIFT_NOT_FOUND;
			}
			
			rewardIdVO = cacheRechargeGift.getRewardIdVO(rewardId);
			if(rewardIdVO == null) {
				return CHARGE_GIFT_NOT_FOUND;
			} else if(rewardIdVO.getState() != GiftState.CANREWARD) {
				return CHARGE_GIFT_STATE_INVALID;
			}
			
			ChargeConditionConfig condition = cacheRechargeGift.getChargeCondition();
			if(condition == null) {
				return CHARGE_GIFT_NOT_FOUND;
			}
			
			int currentConditionId = condition.getId();
			//所有的奖励中, 有没有被当前条件领取过(当前的条件, 有没有领取过奖励)
			for (ChargeRewardConfig chargeRewardConfig : condition.getRechargeRewards()) { 
				RewardCount rewardCount = rechargeGift.getRewardCount(chargeRewardConfig.getId());
				if(rewardCount != null && rewardCount.containsCondition(currentConditionId)) {
					return DUPLICATE_REWARDS_GIFT;
				}
			}
			
			// 当前的奖励, 有没有被其他条件领取过
			RewardCount currentRewardCount = rechargeGift.getRewardCount(rewardId);
			if(currentRewardCount != null) {
				for (ChargeConditionConfig chargeConditionConfig : chargeConfig.getConditions()) {
					if(currentRewardCount.containsCondition(chargeConditionConfig.getId())) {
						return DUPLICATE_REWARDS_GIFT;
					}
				}
			}
			
			List<UserProps> userProps = constGiftReward.getUserProps();
			List<UserEquip> userEquips = constGiftReward.getUserEquips();
			if(!userEquips.isEmpty() || !userProps.isEmpty()) {
				int goodsSize = userEquips.size() + userProps.size();
				if(!player.canAddNew2Backpack(backpackSize + goodsSize, backpack)) {
					return BACKPACK_FULLED;
				}
				createResult = propsManager.createUserEquipAndUserProps(userProps, userEquips);
				propsManager.put2UserPropsIdsList(playerId, backpack, createResult.getCollections1());
				propsManager.put2UserEquipIdsList(playerId, backpack, createResult.getCollections2());
			}
			
			if(currentRewardCount == null) {
				currentRewardCount = new RewardCount();
				currentRewardCount.setRewardId(rewardId);
				rechargeGift.addRewardCount(currentRewardCount);
			}

			currentRewardCount.setEndTime(cacheRechargeGift.getEndTime());
			currentRewardCount.getConditions().add(currentConditionId);
			rechargeGift.updateRewardInfoMap();
			rewardIdVO.setState(GiftState.REWARDED);
			battle.increaseExp(constGiftReward.getAddExp());
			player.increaseCoupon(constGiftReward.getAddCoupon());
			player.increaseSilver(constGiftReward.getAddSilver());
			giftContext.updateRecalculate(true);
		} catch (Exception e) {
			logger.error("{}", e);
			return FAILURE;
		} finally {
			lock.unlock();
		}
		
		dbService.updateEntityIntime(rechargeGift);
		dbService.submitUpdate2Queue(player, battle);
		if(createResult != null) {
			List<BackpackEntry> backpackEntries = new LinkedList<BackpackEntry>();
			backpackEntries.addAll(createResult.getCollections1());
			backpackEntries.addAll(createResult.getCollections2());
			MessagePushHelper.pushUserProps2Client(playerId, backpack, false, backpackEntries);
		}
		
		LoggerGoods[] goodsInfos = constGiftReward.getLoggerGoodsArray();
		if(goodsInfos.length > 0) {
			GoodsLogger.goodsLogger(player, Source.RECHARGE_GIFT_REWARDS, goodsInfos);
		}
		if(constGiftReward.getAddCoupon() != 0) {
			CouponLogger.inCome(Source.RECHARGE_GIFT_REWARDS, constGiftReward.getAddCoupon(), player, goodsInfos);
		}
		if(constGiftReward.getAddSilver() != 0) {
			SilverLogger.inCome(Source.RECHARGE_GIFT_REWARDS, constGiftReward.getAddCoupon(), player, goodsInfos);
		}
		if(constGiftReward.getAddExp() != 0) {
			ExpLogger.expReward(userDomain, Source.RECHARGE_GIFT_REWARDS, constGiftReward.getAddExp());
		}
		List<Long> playerIds = Arrays.asList(playerId);
		List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
		MessagePushHelper.pushGoodsCountChange2Client(playerId, constGiftReward.getGoodVOList());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIds, unitIds, AttributeRule.RECHARGE_GIFT_ATTRIBUTES);
		return SUCCESS;
	}
	
	/**
	 * 构建礼包奖励
	 * 
	 * @param  playerId				角色ID
	 * @param  chargeReward			充值奖励
	 * @return {@link GiftReward}	礼包奖励对象	
	 */
	private GiftReward constGiftReward(long playerId, ChargeRewardConfig chargeReward) {
		GiftReward giftReward = new GiftReward();
		giftReward.setAddExp(chargeReward.getExp());
		int backpack = BackpackType.DEFAULT_BACKPACK;
		giftReward.setAddCoupon(chargeReward.getCoupon());
		giftReward.setAddSilver(chargeReward.getSilver());
		for (RewardVO rewardVO : chargeReward.getRewardVOList()) {
			int count = rewardVO.getCount();
			int baseId = rewardVO.getBaseId();
			int starLevel = rewardVO.getStarLevel();
			boolean binding = rewardVO.isBinding();
			if(rewardVO.getType() == GoodsType.EQUIP) {
				giftReward.getLoggerGoods().add(LoggerGoods.incomeEquip(baseId, count));
				giftReward.getGoodVOList().add(GoodsVO.valueOf(baseId, GoodsType.EQUIP, count));
				for (int i = 0; i < count; i++) {
					UserEquip userEquip = EquipHelper.newUserEquip2Star(playerId, backpack, baseId, binding, starLevel);
					if(userEquip != null) {
						giftReward.getUserEquips().add(userEquip);
					}
				}
			} else if(rewardVO.getType() == GoodsType.PROPS) {
				giftReward.getLoggerGoods().add(LoggerGoods.incomeProps(baseId, count));
				giftReward.getGoodVOList().add(GoodsVO.valueOf(baseId, GoodsType.PROPS, count));
				giftReward.getUserProps().addAll(PropsHelper.newUserProps(playerId, backpack, baseId, count, binding));
			}
		}
		return giftReward;
	}
 
	
}
