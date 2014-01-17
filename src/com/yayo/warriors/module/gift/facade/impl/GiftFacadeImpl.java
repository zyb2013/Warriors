package com.yayo.warriors.module.gift.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.gift.constant.GiftConstant.*;
import static com.yayo.warriors.module.gift.rule.GiftRule.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.user.type.AttributeKeys.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.TimeConstant;
import com.yayo.warriors.basedb.adapter.PropsService;
import com.yayo.warriors.basedb.model.OnlineGiftConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.gift.entity.Gift;
import com.yayo.warriors.module.gift.entity.UserGift;
import com.yayo.warriors.module.gift.entity.UserOnlineGift;
import com.yayo.warriors.module.gift.facade.GiftFacade;
import com.yayo.warriors.module.gift.manager.GiftManager;
import com.yayo.warriors.module.gift.model.GiftRewardInfo;
import com.yayo.warriors.module.gift.rule.GiftRule;
import com.yayo.warriors.module.gift.type.GiftType;
import com.yayo.warriors.module.gift.vo.GiftVo;
import com.yayo.warriors.module.gift.vo.OnlineGiftVo;
import com.yayo.warriors.module.logger.log.CouponLogger;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.mail.model.ReceiveCondition;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.type.GoodsType;
import com.yayo.warriors.util.SequenceGiftHelper;

/**
 * 礼包接口实现类
 * 
 * @author huachaoping
 */
@Component
public class GiftFacadeImpl implements GiftFacade, LogoutListener {
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private GiftManager giftManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private PropsService propsService;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private DbService dbService;
	@Autowired
	private AllianceManager allianceManager;
	@Autowired
	private SequenceGiftHelper sequenceGiftHelper;

	
	/**
	 * 领取在线礼包
	 * 
	 * @param playerId                  玩家ID
	 * @param onlineGiftId              在线礼包ID
	 * @return {@link CommonConstant}
	 */
	
	public ResultObject<Collection<BackpackEntry>> rewardOnlineGift(long playerId, int onlineGiftId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} else if (!userManager.isOnline(playerId)) {
			return ResultObject.ERROR(PLAYER_OFF_LINE);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if (battle.getLevel() < OPEN_LEVEL) {
			return ResultObject.ERROR(LEVEL_INVALID);
		}
		
		OnlineGiftConfig config = propsService.get(onlineGiftId, OnlineGiftConfig.class);
		if (config == null) {
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		UserOnlineGift onlineGift = giftManager.getUserOnlineGift(playerId);
		if (onlineGift.getOnlineGiftId() >= onlineGiftId) {
			return ResultObject.ERROR(FAILURE);
		}

		Date endTime = onlineGift.getEndTime();                      // 登录超过8天, 没机会领了      
		if (new Date().after(endTime)) {
			return ResultObject.ERROR(OUT_OF_EXPIRATION);
		}
		
		long requiredTime = config.getObtainTime() * TimeConstant.ONE_MINUTE_MILLISECOND;
		long onlineTime = onlineGift.getOnlineTime();
		long curSec = System.currentTimeMillis();
		if (onlineTime == 0L) {                                           // 判断礼包没领之前是否有下线 0L代表一直在线
			Date openTime = onlineGift.getOpenTime();
			if (curSec - openTime.getTime() < requiredTime) {
				return ResultObject.ERROR(ONLINE_TIME_NOT_ENOUGH);
			}
		} else {
			Date loginTime = player.getLoginTime();
			long curOnline = curSec - loginTime.getTime();
			if (onlineTime + curOnline < requiredTime) {
				return ResultObject.ERROR(ONLINE_TIME_NOT_ENOUGH);
			}
		}
		
		List<LoggerGoods> loggerGoods = new ArrayList<LoggerGoods>(0);
		ResultObject<PropsStackResult> calcGiftRewards = calcGiftRewards(playerId, config, loggerGoods);
		if(calcGiftRewards.getResult() < SUCCESS) {
			return ResultObject.ERROR(calcGiftRewards.getResult());
		}
		
		PropsStackResult propsStackResult = calcGiftRewards.getValue();			// 堆叠后的数据信息
		Map<Long, Integer> mergeProps = propsStackResult.getMergeProps();		// 堆叠的道具
		List<UserProps> newUserProps = propsStackResult.getNewUserProps();		// 新创建的道具
		int playerBackSize  = propsManager.getBackpackSize(playerId, DEFAULT_BACKPACK);
		ChainLock lock = LockUtils.getLock(player.getPackLock(), onlineGift);
		try {
			lock.lock();
			if(!newUserProps.isEmpty()) {
				int needSize = newUserProps.size();
				if(!player.canAddNew2Backpack(playerBackSize + needSize, DEFAULT_BACKPACK)) {
					return ResultObject.ERROR(BACKPACK_FULLED);
				}
				newUserProps = propsManager.createUserProps(newUserProps);
				propsManager.put2UserPropsIdsList(playerId, DEFAULT_BACKPACK, newUserProps);
			}
			
			onlineGift.setOnlineTime(0L);                                       // 需要更新领取后在线时间
			onlineGift.setOpenTime(new Date());                                 
			onlineGift.setOnlineGiftId(onlineGiftId);
			dbService.submitUpdate2Queue(onlineGift);
		} finally {
			lock.unlock();
		}
		
		Collection<BackpackEntry> entries = new ArrayList<BackpackEntry>();
		if(!mergeProps.isEmpty()) {
			List<UserProps> updateUserPropsList = propsManager.updateUserPropsList(mergeProps);
			entries.addAll(voFactory.getUserPropsEntries(updateUserPropsList));
		}
		if(!newUserProps.isEmpty()) {
			entries.addAll(voFactory.getUserPropsEntries(newUserProps));
		}
		
		if(!loggerGoods.isEmpty()) {
			GoodsLogger.goodsLogger(player, Source.ONLINE_GIFT, loggerGoods.toArray(new LoggerGoods[loggerGoods.size()]));
		}
		return ResultObject.SUCCESS(entries);
	}

	
	/**
	 * 计算礼包奖励堆叠信息
	 * 
	 * @param  playerId				计算礼包奖励信息
	 * @param  giftConfig			礼包基础对象
	 * @return {@link ResultObject}	计算返回值
	 */
	private ResultObject<PropsStackResult> calcGiftRewards(long playerId, OnlineGiftConfig giftConfig, List<LoggerGoods> loggerGoods) {
		Map<Integer, Integer> giftPropsData = giftConfig.getGiftMap();
		if(giftPropsData == null || giftPropsData.isEmpty()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		PropsStackResult stackResult = PropsStackResult.valueOf();
		for (Map.Entry<Integer, Integer> entry : giftPropsData.entrySet()) {
			int propsId = entry.getKey();
			int count = entry.getValue();
			PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
			if (propsConfig == null) {
				return ResultObject.ERROR(ITEM_NOT_FOUND);
			}
			
			PropsStackResult stack = PropsHelper.calcPropsStack(playerId, DEFAULT_BACKPACK, propsId, count, true);
			stackResult.getMergeProps().putAll(stack.getMergeProps());
			stackResult.getNewUserProps().addAll(stack.getNewUserProps());
			loggerGoods.add(LoggerGoods.incomeProps(propsId, count));
		}
		
		return ResultObject.SUCCESS(stackResult);
	}
	
//	/**
//	 * 开启在线礼包
//	 * 
//	 * @param playerId                  玩家Id
//	 * @return {@link CommonConstant}
//	 */
//	
//	public int openOnlineGift(long playerId) {
//		UserDomain userDomain = userManager.getUserDomain(playerId);
//		if (userDomain == null) {
//			return PLAYER_NOT_FOUND;
//		} 
//		
//		UserOnlineGift onlineGift = giftManager.getUserOnlineGift(playerId);
//		if (onlineGift.getOpenTime() != null) {
//			return ONLINE_GIFT_OPENED;
//		}
//		
//		ChainLock lock = LockUtils.getLock(onlineGift);
//		try {
//			lock.lock();
//			onlineGift.setOpenTime(new Date());            // 领取时间
//		} finally {
//			lock.unlock();
//		}
//		dbService.submitUpdate2Queue(onlineGift);
//		return SUCCESS;
//	}


	/**
	 * 获取在线礼包状态
	 * 
	 * @param playerId
	 * @return 
	 */
	
	public OnlineGiftVo loadGiftState(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain != null) {
			if (userDomain.getBattle().getLevel() < OPEN_LEVEL) {
				return OnlineGiftVo.valueOf(-1, false, -1);
			}
			
			UserOnlineGift onlineGift = giftManager.getUserOnlineGift(playerId);
			Date endTime = onlineGift.getEndTime();                          // 礼包到期时间
			
			if (onlineGift.cleanData()) {                                    // 验证是否需要清数据
				dbService.submitUpdate2Queue(onlineGift);
				endTime = onlineGift.getEndTime();
			}
			
			if (new Date().after(DateUtil.getDate0AM(endTime))) {
				return OnlineGiftVo.valueOf(-1 , true, -1);
			}
			
			int nextGiftId = onlineGift.getOnlineGiftId() + 1;               // 下一个礼包ID
			OnlineGiftConfig config = propsService.get(nextGiftId, OnlineGiftConfig.class);
			if (config == null) {
				return OnlineGiftVo.valueOf(-1, false, -1);
			}
			long requiredTime = config.getObtainTime() * TimeConstant.ONE_MINUTE_MILLISECOND;
			
			long onlineTime = onlineGift.getOnlineTime();
			long remainTime = requiredTime - onlineTime > 0L ? requiredTime - onlineTime : 0L;
			return OnlineGiftVo.valueOf(nextGiftId, false, remainTime);
		}
		return null;
	}
	
	
	/**
	 * 更新在线时间
	 * 
	 * @param player
	 */
	
	public void saveGiftOnlineTime(Player player) {
		UserOnlineGift onlineGift = giftManager.getUserOnlineGift(player.getId());
		Date endTime = onlineGift.getEndTime(); 
		if (endTime == null) {
			return;
		} else if (new Date().after(endTime)) {
			return;
		}
		
		ChainLock lock = LockUtils.getLock(onlineGift);
		try {
			lock.lock();
			long curSec = System.currentTimeMillis();
			long loginTime = player.getLoginTime().getTime() ;
			long openTime = onlineGift.getOpenTime() == null ? 
				 curSec : onlineGift.getOpenTime().getTime() ;
			
			long onlineTime = loginTime > openTime ? curSec - loginTime : curSec - openTime;
			onlineGift.addOnlineTime(onlineTime);
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(onlineGift);
	}


	/**
	 * 下线保存在线礼包
	 * 
	 * @param playerId 
	 */
	
	public void onLogoutEvent(UserDomain userDomain) {
		if (userDomain.getBattle().getLevel() >= OPEN_LEVEL) {
			saveGiftOnlineTime(userDomain.getPlayer());
		}
	}

	
	/**
	 * 
	 * 
	 * @param playerId
	 * @param giftId
	 * @param cdKey
	 * @return {@link CommonConstant}
	 */
	@SuppressWarnings("unchecked")
	
	public ResultObject<Integer> receiveCDKeyGift(long playerId, int giftId, String cdKey) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Gift checkGift = giftManager.getGift(giftId);                   // 验证客户端传过来的ID
		if (checkGift == null) {
			return ResultObject.ERROR(GIFT_NOT_FOUND);
		} else if (!checkGift.isValidTime()) {
			return ResultObject.ERROR(GIFT_OUT_OF_DATE);
		} 
		
		if (StringUtils.isBlank(cdKey)) {
			return ResultObject.ERROR(GIFT_CDKEY_NOT_FOUND);
		} else if (!ArrayUtils.contains(CDKEY_BIT, cdKey.length())) {
			return ResultObject.ERROR(GIFT_CDKEY_NOT_FOUND);
		}
		
		UserGift userGift = giftManager.getUserGift(playerId);
		if (userGift == null) {
			return ResultObject.ERROR(GIFT_NOT_FOUND);
		} else if (userGift.getReceiveIdSet().contains(giftId)) {
			return ResultObject.ERROR(GIFT_RECEIVED);
		}
		
		Player player = userDomain.getPlayer();
		ResultObject<Integer> result = sequenceGiftHelper.processSequence(player, SequenceGiftHelper.QUERY, cdKey);
		if (result.getResult() != SUCCESS) {
			sequenceGiftHelper.processSequence(player, SequenceGiftHelper.CANCEL, cdKey);
			return ResultObject.ERROR(GiftRule.getCDKeyResult(result.getResult()));
		}
		
		if (result.getValue() != giftId) {
			sequenceGiftHelper.processSequence(player, SequenceGiftHelper.CANCEL, cdKey);
			return ResultObject.ERROR(GIFT_CDKEY_NOT_FOUND);
		}
		
		Gift gift = giftManager.getGift(result.getValue());
		if (gift == null) {
			sequenceGiftHelper.processSequence(player, SequenceGiftHelper.CANCEL, cdKey);
			return ResultObject.ERROR(GIFT_NOT_FOUND);
		} else if (!gift.isValidTime()) {
			sequenceGiftHelper.processSequence(player, SequenceGiftHelper.CANCEL, cdKey);
			return ResultObject.ERROR(GIFT_OUT_OF_DATE);
		}
		
		Map<String, String[]> conditions = gift.getConditions();                         // 条件验证
		for (Map.Entry<String, String[]> entry : conditions.entrySet()) {
			String key = entry.getKey();
			String[] value = entry.getValue();
			ReceiveCondition receive = ReceiveCondition.getElementEnumById(Integer.valueOf(key));
			if (!checkCondition(userDomain, receive, value)) {
				sequenceGiftHelper.processSequence(player, SequenceGiftHelper.CANCEL, cdKey);
				return ResultObject.ERROR(CONDITION_NOT_ENOUGH);
			}
		}
		
		long goldenRewards = 0L;
		long silverRewards = 0L;
		long couponRewards = 0L;
		List<UserEquip> equipList = new ArrayList<UserEquip>();
		PropsStackResult stackResult = PropsStackResult.valueOf();
		List<GiftRewardInfo> rewardInfo = gift.getGiftRewardInfos();
		for (GiftRewardInfo info : rewardInfo) {
			int baseId = info.getBaseId();
			int count = info.getCount();
			if (info.getGoodsType() == GoodsType.PROPS) {
				PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
				if (propsConfig == null) {
					sequenceGiftHelper.processSequence(player, SequenceGiftHelper.CANCEL, cdKey);
					return ResultObject.ERROR(ITEM_NOT_FOUND);
				}
				
				PropsStackResult stack = PropsHelper.calcPropsStack(playerId, DEFAULT_BACKPACK, baseId, count, true);
				stackResult.getMergeProps().putAll(stack.getMergeProps());
				stackResult.getNewUserProps().addAll(stack.getNewUserProps());
			} else if (info.getGoodsType() == GoodsType.EQUIP) {
				equipList.addAll(EquipHelper.newUserEquips(playerId, DEFAULT_BACKPACK, baseId, true, count));
			} else if (info.getGoodsType() == GoodsType.GOLDEN) {
				goldenRewards += info.getCount();
			} else if (info.getGoodsType() == GoodsType.SILVER) {
				silverRewards += info.getCount();
			} else if (info.getGoodsType() == GoodsType.COUPON) {
				couponRewards += info.getCount();
			}
		}
		
		Map<Long, Integer> mergeProps = stackResult.getMergeProps();		// 堆叠的道具
		List<UserProps> newUserProps = stackResult.getNewUserProps();		// 新创建的道具
		int playerBackSize  = propsManager.getBackpackSize(playerId, DEFAULT_BACKPACK);
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock(), player, userGift);
		try {
			lock.lock();
			if (userGift.getReceiveIdSet().contains(result.getValue())) {
				sequenceGiftHelper.processSequence(player, SequenceGiftHelper.CANCEL, cdKey);
				return ResultObject.ERROR(GIFT_RECEIVED);
			}
			int needSize = newUserProps.size() + equipList.size();
			if (!player.canAddNew2Backpack(playerBackSize + needSize, DEFAULT_BACKPACK)) {
				sequenceGiftHelper.processSequence(player, SequenceGiftHelper.CANCEL, cdKey);
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
			if (!newUserProps.isEmpty()){
				newUserProps = propsManager.createUserProps(newUserProps);
				propsManager.put2UserPropsIdsList(playerId, DEFAULT_BACKPACK, newUserProps);
			}
			if (!equipList.isEmpty()) {
				equipList = propsManager.createUserEquip(equipList);
				propsManager.put2UserEquipIdsList(playerId, DEFAULT_BACKPACK, equipList);
			}
			
			userGift.add2Received(result.getValue());
			player.increaseGolden(goldenRewards);
			player.increaseSilver(silverRewards);
			player.increaseCoupon(couponRewards);
			dbService.updateEntityIntime(player, userGift);
		} finally {
			lock.unlock();
		}
		
		List<BackpackEntry> entries = new ArrayList<BackpackEntry>();
		List<UserProps> updateUserPropsList = propsManager.updateUserPropsList(mergeProps);
		if(!mergeProps.isEmpty()) {
			entries.addAll(updateUserPropsList);
		}
		if(!newUserProps.isEmpty()) {
			entries.addAll(newUserProps);
		}
		if(!equipList.isEmpty()) {
			entries.addAll(equipList);
		}
		
		if (goldenRewards > 0) {
			GoldLogger.inCome(Source.RECEIVE_CDKEY_REWARDS, goldenRewards, player);
		}
		if (silverRewards > 0) {
			SilverLogger.inCome(Source.RECEIVE_CDKEY_REWARDS, silverRewards, player);
		}
		if (couponRewards > 0) {
			CouponLogger.inCome(Source.RECEIVE_CDKEY_REWARDS, couponRewards, player);
		}
		if (!mergeProps.isEmpty() || !newUserProps.isEmpty()) {
			List<LoggerGoods> goodsInfo = LoggerGoods.loggerProps(Orient.INCOME, mergeProps, newUserProps);
			GoodsLogger.goodsLogger(player, Source.RECEIVE_CDKEY_REWARDS, goodsInfo.toArray(new LoggerGoods[goodsInfo.size()]));
		}
		
		List<Long> receiver = Arrays.asList(playerId);
		List<UnitId> playerUnitId = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, receiver, playerUnitId, GOLDEN, SILVER, COUPON);
		Collection<GoodsVO> goodsVO = GoodsVO.valuleOf(newUserProps, updateUserPropsList, mergeProps, equipList);
		MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, entries);
		MessagePushHelper.pushGoodsCountChange2Client(playerId, goodsVO);
		sequenceGiftHelper.processSequence(player, SequenceGiftHelper.CONFIRM, cdKey);
		return ResultObject.SUCCESS(gift.getId());
	}


	/**
	 * 获得有效的礼包
	 * 
	 * @return {@link GiftVo}
	 */
	
	public Collection<GiftVo> loadEffectGifts(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return Collections.emptyList();
		}
		
		UserGift userGift = giftManager.getUserGift(playerId);
		Set<Integer> received = userGift.getReceiveIdSet();
		Set<Integer> cdKeyGiftIds = giftManager.getConditionGifts();
		
		Collection<GiftVo> voList = new ArrayList<GiftVo>();
		for (int id : cdKeyGiftIds) {
			Gift gift = giftManager.getGift(id);
			if (!gift.isValidTime()) {
				continue;
			} 
			
			int receive = received.contains(id) ? 1 : 0;
			GiftVo giftVo = GiftVo.valueOf(gift, receive);
			voList.add(giftVo);
		}
		
		return voList;
	}

	

	/**
	 * 领取条件礼包(达成条件可领取, 不包括CDKEY礼包)
	 * 
	 * @param playerId 
	 * @param giftId
	 * @return {@link CommonConstant}
	 */
	@SuppressWarnings("unchecked")
	
	public ResultObject<String> receiveEffectGift(long playerId, int giftId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Gift gift = giftManager.getGift(giftId);
		if (gift == null) {
			return ResultObject.ERROR(GIFT_NOT_FOUND);
		} else if (gift.getGiftType() == GiftType.CDKEY_GIFT) {
			return ResultObject.ERROR(GIFT_NOT_FOUND);
		} else if (!gift.isValidTime()) {
			return ResultObject.ERROR(GIFT_OUT_OF_DATE);
		}
		
		Player player = userDomain.getPlayer();
		UserGift userGift = giftManager.getUserGift(playerId);
		
		Map<String, String[]> conditions = gift.getConditions();
		for (Map.Entry<String, String[]> entry : conditions.entrySet()) {
			String key = entry.getKey();
			String[] value = entry.getValue();
			ReceiveCondition receive = ReceiveCondition.getElementEnumById(Integer.valueOf(key));
			if (!checkCondition(userDomain, receive, value)) {
				return ResultObject.ERROR(CONDITION_NOT_ENOUGH);
			}
		}
		
		long goldenRewards = 0L;
		long silverRewards = 0L;
		long couponRewards = 0L;
		List<UserEquip> equipList = new ArrayList<UserEquip>();
		PropsStackResult stackResult = PropsStackResult.valueOf();
		List<GiftRewardInfo> rewardInfo = gift.getGiftRewardInfos();
		for (GiftRewardInfo info : rewardInfo) {
			int baseId = info.getBaseId();
			int count = info.getCount();
			if (info.getGoodsType() == GoodsType.PROPS) {
				PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
				if (propsConfig == null) {
					return ResultObject.ERROR(ITEM_NOT_FOUND);
				}
				
				PropsStackResult stack = PropsHelper.calcPropsStack(playerId, DEFAULT_BACKPACK, baseId, count, true);
				stackResult.getMergeProps().putAll(stack.getMergeProps());
				stackResult.getNewUserProps().addAll(stack.getNewUserProps());
			} else if (info.getGoodsType() == GoodsType.EQUIP) {
				equipList.addAll(EquipHelper.newUserEquips(playerId, DEFAULT_BACKPACK, baseId, true, count));
			} else if (info.getGoodsType() == GoodsType.GOLDEN) {
				goldenRewards += info.getCount();
			} else if (info.getGoodsType() == GoodsType.SILVER) {
				silverRewards += info.getCount();
			} else if (info.getGoodsType() == GoodsType.COUPON) {
				couponRewards += info.getCount();
			}
		}
		
		Map<Long, Integer> mergeProps = stackResult.getMergeProps();		// 堆叠的道具
		List<UserProps> newUserProps = stackResult.getNewUserProps();		// 新创建的道具
		int playerBackSize  = propsManager.getBackpackSize(playerId, DEFAULT_BACKPACK);
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock(), player, userGift);
		try {
			lock.lock();
			if (!gift.isValidTime()) {
				return ResultObject.ERROR(GIFT_NOT_FOUND);
			}
			
			if (userGift.getReceiveIdSet().contains(giftId)) {
				return ResultObject.ERROR(GIFT_RECEIVED);
			}
			int needSize = newUserProps.size() + equipList.size();
			if (!player.canAddNew2Backpack(playerBackSize + needSize, DEFAULT_BACKPACK)) {
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
			if (!newUserProps.isEmpty()){
				newUserProps = propsManager.createUserProps(newUserProps);
				propsManager.put2UserPropsIdsList(playerId, DEFAULT_BACKPACK, newUserProps);
			}
			if (!equipList.isEmpty()) {
				equipList = propsManager.createUserEquip(equipList);
				propsManager.put2UserEquipIdsList(playerId, DEFAULT_BACKPACK, equipList);
			}
			
			userGift.add2Received(giftId);
			player.increaseGolden(goldenRewards);
			player.increaseSilver(silverRewards);
			player.increaseCoupon(couponRewards);
			dbService.updateEntityIntime(player, userGift);              // 礼包即时入库
		} finally {
			lock.unlock();
		}
		
		List<BackpackEntry> entries = new ArrayList<BackpackEntry>();
		List<UserProps> updateUserPropsList = propsManager.updateUserPropsList(mergeProps);
		if(!mergeProps.isEmpty()) {
			entries.addAll(updateUserPropsList);
		}
		if(!newUserProps.isEmpty()) {
			entries.addAll(newUserProps);
		}
		if(!equipList.isEmpty()) {
			entries.addAll(equipList);
		}
		
		if (goldenRewards > 0) {
			GoldLogger.inCome(Source.RECEIVE_GIFT_REWARDS, goldenRewards, player);
		}
		if (silverRewards > 0) {
			SilverLogger.inCome(Source.RECEIVE_GIFT_REWARDS, silverRewards, player);
		}
		if (couponRewards > 0) {
			CouponLogger.inCome(Source.RECEIVE_GIFT_REWARDS, couponRewards, player);
		}
		if (!mergeProps.isEmpty() || !newUserProps.isEmpty()) {
			List<LoggerGoods> goodsInfo = LoggerGoods.loggerProps(Orient.INCOME, mergeProps, newUserProps);
			GoodsLogger.goodsLogger(player, Source.RECEIVE_GIFT_REWARDS, goodsInfo.toArray(new LoggerGoods[goodsInfo.size()]));
		}
		
		List<Long> receiver = Arrays.asList(playerId);
		List<UnitId> playerUnitId = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, receiver, playerUnitId, GOLDEN, SILVER, COUPON);
		Collection<GoodsVO> goodsVO = GoodsVO.valuleOf(newUserProps, updateUserPropsList, mergeProps, equipList);
		MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, entries);
		MessagePushHelper.pushGoodsCountChange2Client(playerId, goodsVO);
		return ResultObject.SUCCESS(gift.getName());
	}
	
	
	/**
	 * 验证条件
	 * 
	 * @param userDomain
	 * @param condition
	 * @param format
	 * @return {@link Boolean}
	 */ 
	private boolean checkCondition(UserDomain userDomain, ReceiveCondition condition, String[] format) {
		if (condition == null) return false; 
		
		switch (condition) {
			case USER_LEVEL:  		return condition.isReachCondition(userDomain.getBattle(), format);
			case LOGIN_DAY: 		return condition.isReachCondition(userDomain.getPlayer(), format);
			case LAST_LOGIN_TIME: 	return condition.isReachCondition(userDomain.getPlayer(), format);
			case USER_GOLDEN: 		return condition.isReachCondition(userDomain.getPlayer(), format);
			case USER_SILVER:		return condition.isReachCondition(userDomain.getPlayer(), format);
			case REGISTER_TIME:		return condition.isReachCondition(userDomain.getPlayer(), format);
			case ALLIANCE_LEVEL: 	Alliance alliance = allianceManager.getAlliance4PlayerId(userDomain.getId());
									return condition.isReachCondition(alliance, format);
			case ALLIANCE_NAME: 	PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(userDomain.getBattle());
									return playerAlliance != null && condition.isReachCondition(playerAlliance, format);
			case LAST_CHARGE_TIME: 	break;
		}
		return false;
	}
	
	
}
