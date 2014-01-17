package com.yayo.warriors.module.vip.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.vip.model.VipFunction.*;
import static com.yayo.warriors.type.FormulaKey.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.VipConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.common.helper.VipPushHelper;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.admin.vo.PlayerVipVO;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.vip.entity.PlayerVip;
import com.yayo.warriors.module.vip.facade.VipFacade;
import com.yayo.warriors.module.vip.manager.VipManager;
import com.yayo.warriors.module.vip.model.VipDomain;
import com.yayo.warriors.module.vip.model.VipFunction;
import com.yayo.warriors.module.vip.rule.VipRule;
import com.yayo.warriors.type.ElementType;

/**
 * VIP
 * 
 * @author huachaoping
 */
@Component
public class VipFacadeImpl implements VipFacade {
	
	@Autowired
	private VipManager vipManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private DbService dbService;
	@Autowired
	private MapFacade mapFacade;
	
	
	
	/**
	 * 开通VIP
	 * 
	 * @param  playerId           角色ID
	 * @param  userItemId         用户道具ID
	 * @param  isUseItem          是否使用VIP
	 * @param  baseId             用户基础ID
	 * @return {@link PlayerVipVO}
	 */
	
	public ResultObject<PlayerVipVO> obtainVip(long playerId, long userItemId, int baseId) {
		UserDomain domain = userManager.getUserDomain(playerId);
		if (domain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		return useGolden(domain, baseId);
	}
	
	
	/**
	 * 用VIP卡
	 * @param userDomain
	 * @param userItemId
	 * @return
	 */
//	private ResultObject<PlayerVipVO> useVipCard(UserDomain userDomain, long userItemId) {
//		UserProps userItem = propsManager.getUserProps(userItemId);
//		if(userItem == null) {
//			return ResultObject.ERROR(ITEM_NOT_FOUND);
//		} 
//		
//		int itemId = userItem.getBaseId();
//		PropsConfig propsConfig = propsManager.getPropsConfig(itemId);
//		if(propsConfig == null) {
//			return ResultObject.ERROR(ITEM_NOT_FOUND);
//		}
//		
//		PlayerVip playerVip = vipManager.getPlayerVip(userDomain.getId());
//		int propsVipLevel = (int) Math.round(propsConfig.getAttrValue());
//		if (propsVipLevel < playerVip.getVipLevel()) {
//			return ResultObject.ERROR(LEVEL_INVALID);
//		}
//		
//		VipConfig vipConfig = vipManager.getVipConfig(propsVipLevel);
//		if (vipConfig == null) {
//			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
//		}
//		
//		UserCoolTime userCoolTime = coolTimeManager.getUserCoolTime(userDomain.getId());
//		if(userCoolTime == null) {
//			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
//		}
//		
//		int coolTimeId = propsConfig.getCdId();
//		CoolTimeConfig coolTime = null;
//		if(coolTimeId > 0) {
//			coolTime = coolTimeManager.getCoolTimeConfig(coolTimeId);
//			if(coolTime == null) {
//				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
//			} else if(userCoolTime.isCoolTiming(coolTimeId)) {
//				return ResultObject.ERROR(COOL_TIMING);
//			}
//		}
//		int type = propsConfig.getPropsType();
//		PropsParser parser = itemParserContext.getParser(type);
//		if(parser == null) {
//			return ResultObject.ERROR(ITEM_CANNOT_USE);
//		}
//		
//		int result = parser.effect(userDomain, userCoolTime, coolTime, userItem, 1);
//		if (result < SUCCESS) {
//			return ResultObject.ERROR(result);
//		}
//		
//		VipDomain vipDomain = VipDomain.valueOf(playerVip, vipConfig);     // 构建VIP域
//		ChainLock lock = LockUtils.getLock(playerVip);
//		try {
//			lock.lock();
//			long vipOutOfTime = vipDomain.longValue(VipOutOfDateTime);     // 获取VIP时长
//			playerVip.alterEndTime(propsVipLevel, vipOutOfTime);                
//			playerVip.initVipParams();
//		} finally {
//			lock.unlock();
//		}
//		
//		dbService.submitUpdate2Queue(playerVip);
//		PlayerVipVO playerVipVO = new PlayerVipVO(vipDomain);
//		
//		List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
//		Collection<Long> playerIdList = mapFacade.getScreenViews(userDomain.getId());
//		UserPushHelper.pushAttribute2AreaMember(userDomain.getId(), playerIdList, unitIds, AttributeKeys.VIP_INFO);
//		return ResultObject.SUCCESS(playerVipVO);
//	}
	
	
	/**
	 * 元宝购买VIP
	 * @param userDomain
	 * @param baseId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ResultObject<PlayerVipVO> useGolden(UserDomain userDomain, int baseId) {
		Player player = userDomain.getPlayer();
		PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
		if (propsConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if (userDomain.getBattle().getLevel() < propsConfig.getLevel()) {
			return ResultObject.ERROR(LEVEL_INVALID);
		}
		
		PlayerVip playerVip = vipManager.getPlayerVip(userDomain.getId());
		int propsVipLevel = (int) Math.round(propsConfig.getAttrValue());
		if (propsVipLevel < playerVip.getVipLevel()) {
			return ResultObject.ERROR(LEVEL_INVALID);
		}
		
		VipConfig vipConfig = vipManager.getVipConfig(propsVipLevel);
		if (vipConfig == null) {
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		int costGolden = propsConfig.getMallPrice();
		VipDomain vipDomain = VipDomain.valueOf(playerVip, vipConfig);     // 构建VIP域
		ChainLock lock = LockUtils.getLock(player, playerVip);
		try {
			lock.lock();
			if (player.getGolden() < costGolden) {
				return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
			}
			player.decreaseGolden(costGolden);
			
			long vipOutOfTime = vipDomain.longValue(VipOutOfDateTime);     // 获取VIP时长
			playerVip.alterEndTime(propsVipLevel, vipOutOfTime);                
			playerVip.initVipParams();
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(player, playerVip);					 // 金钱立即存库
		vipManager.put2VipCache(player.getId(), vipDomain);                  
		PlayerVipVO playerVipVO = new PlayerVipVO(vipDomain);
		
		List<Long> receiver = Arrays.asList(userDomain.getId());
		List<UnitId> playerUnits = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(userDomain.getId(), receiver, playerUnits, AttributeKeys.GOLDEN);
		
		Collection<Long> playerIdList = mapFacade.getScreenViews(userDomain.getId());
		UserPushHelper.pushAttribute2AreaMember(userDomain.getId(), playerIdList, playerUnits, AttributeKeys.VIP_INFO);
		GoldLogger.outCome(Source.VIP_LEVEL_UP, costGolden, player, LoggerGoods.outcomePropsAutoBuyGolden(baseId, 1, costGolden));// 元宝日志
		VipRule.pushVipNotice(player, vipConfig);
		return ResultObject.SUCCESS(playerVipVO);
	}
	

	/**
	 * VIP信息
	 * 
	 * @param  playerId           角色ID
	 * @return {@link PlayerVipVO}
	 */
	
	public PlayerVipVO loadVipInfo(long playerId) {
		VipDomain vipDomain = vipManager.getVip(playerId);
		if (vipDomain != null) {
			PlayerVip playerVip = vipDomain.getPlayerVip();
			if (playerVip.clearVipData()) {
				VipPushHelper.pushVipAttention(playerId);
				dbService.submitUpdate2Queue(playerVip);
			}
			return new PlayerVipVO(vipDomain);
		}
		return null;
	}


	
	/**
	 * 领取VIP福利
	 * 
	 * @param playerId            角色ID
	 * @param rewardType          奖励类型
	 * @return {@link CommonConstant}
	 */
	
	public int vipReward(long playerId, int rewardType) {
		UserDomain domain = userManager.getUserDomain(playerId);
		if (domain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		VipDomain vipDomain = vipManager.getVip(playerId);
		if (!vipDomain.isVip()) {
			return PLAYER_NOT_VIP;
		}
		
		VipFunction function = VipFunction.getElementEnumById(rewardType);
		if (function == null) {
			return TYPE_INVALID;
		}
		
		if (function == ReceiveHorseReward) {
//			return horseReward(domain, vipDomain, function);
		} else if (function == ReceiveTaskReward) {
//			return taskReward(domain, vipDomain, function);
		} else if (function == RecieveBlessExp) {
//			return blessReward(domain, vipDomain, function);
		} else if (function == ReceiveVipGift) {
			return vipGiftReward(domain, vipDomain, function);
		}
		
		return FAILURE;
	}
	
	
	/**
	 * 坐骑经验丹奖励
	 * 
	 * @param playerId
	 * @param vipDomain
	 * @param vipFunction
	 * @return
	 */
	@Deprecated
	private int horseReward(UserDomain userDomain, VipDomain vipDomain, VipFunction vipFunction) {
		int rewardCount = vipDomain.intValue(DailyHorseReward);
		if (rewardCount <= 0) {
			return NO_RIGHT;
		}
		
		boolean isReceive = vipDomain.booleanValue(vipFunction);
		if (isReceive) {
			return VIP_REWARD_RECEIVED;
		}
		
		PropsConfig config = propsManager.getPropsConfig(VipRule.HORSE_PROPS_ID);
		if (config == null) {
			return BASEDATA_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		int backpackType = BackpackType.DEFAULT_BACKPACK;
		UserProps userProps = UserProps.valueOf(userDomain.getId(), backpackType, rewardCount, config, true);
		int playerBackSize  = propsManager.getBackpackSize(userDomain.getId(), DEFAULT_BACKPACK);
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock(), vipDomain.getPlayerVip());
		try {
			lock.lock();
			if (!player.canAddNew2Backpack(playerBackSize + 1, DEFAULT_BACKPACK)) {
				return BACKPACK_FULLED;
			}
			vipDomain.alterNum(1, vipFunction);
			propsManager.createUserProps(userProps);
			propsManager.put2UserPropsIdsList(userDomain.getId(), backpackType, userProps);
		} finally {
			lock.unlock();
		}
		dbService.submitUpdate2Queue(vipDomain.getPlayerVip());
		
		MessagePushHelper.pushUserProps2Client(userDomain.getId(), backpackType, false, userProps);
		return SUCCESS;
	}
	
	
	/**
	 * 日坏刷新书奖励
	 * 
	 * @param playerId
	 * @param vipDomain
	 * @param vipFunction
	 * @return
	 */
	@Deprecated
	private int taskReward(UserDomain userDomain, VipDomain vipDomain, VipFunction vipFunction) {
		int rewardCount = vipDomain.intValue(DailyTaskReward);
		if (rewardCount <= 0) {
			return NO_RIGHT;
		}
		
		boolean isReceive = vipDomain.booleanValue(vipFunction);
		if (isReceive) {
			return VIP_REWARD_RECEIVED;
		}
		
		PropsConfig config = propsManager.getPropsConfig(VipRule.TASK_PROPS_ID);
		if (config == null) {
			return BASEDATA_NOT_FOUND;
		}
		
		long playerId = userDomain.getId();
		Player player = userDomain.getPlayer();
		int backpackType = BackpackType.DEFAULT_BACKPACK;
		int playerBackSize  = propsManager.getBackpackSize(playerId, DEFAULT_BACKPACK);
		UserProps userProps = UserProps.valueOf(playerId, backpackType, rewardCount, config, true);
		ChainLock lock = LockUtils.getLock(player.getPackLock(), vipDomain.getPlayerVip());
		try {
			lock.lock();
			if(!player.canAddNew2Backpack(playerBackSize + 1, DEFAULT_BACKPACK)) {
				return BACKPACK_FULLED;
			}
			
			vipDomain.alterNum(1, vipFunction);
			propsManager.createUserProps(userProps);
			propsManager.put2UserPropsIdsList(playerId, backpackType, userProps);
		} finally {
			lock.unlock();
		}
		dbService.submitUpdate2Queue(vipDomain.getPlayerVip());
		
		MessagePushHelper.pushUserProps2Client(playerId, backpackType, false, userProps);
		return SUCCESS;
	}
	
	
	/**
	 * VIP祝福经验奖励
	 * 
	 * @param userDomain
	 * @param vipDomain
	 * @param vipFunction
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	private int blessReward(UserDomain userDomain, VipDomain vipDomain, VipFunction vipFunction) {
		boolean openType = vipDomain.booleanValue(BlessExperience);
		if (!openType) {
			return NO_RIGHT;
		}
		
		boolean isReceive = vipDomain.booleanValue(vipFunction);
		if (isReceive) {
			return VIP_REWARD_RECEIVED;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		PlayerVip playerVip = vipDomain.getPlayerVip();
		
		int level  = battle.getLevel();
		int addExp = FormulaHelper.invoke(PLAYER_VIP_BLESS_FORMULA, level).intValue();
		
		ChainLock lock = LockUtils.getLock(battle, playerVip);
		try {
			lock.lock();
			battle.increaseExp(addExp);
			vipDomain.alterNum(1, vipFunction);
			if(addExp != 0){ //记录日志
				ExpLogger.expReward(userDomain, Source.EXP_VIP_BLESS_REWARD, addExp);
			}
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(battle, playerVip);
		Collection<Long> playerIds = userDomain.getCurrentScreen().getSpireIdCollection(ElementType.PLAYER);
		UserPushHelper.pushAttribute2AreaMember(userDomain.getId(), playerIds, Arrays.asList(userDomain.getUnitId()), AttributeRule.PLAYER_EXP);
		return SUCCESS;
	}
	
	
	/**
	 * VIP礼包
	 * 
	 * @param userDomain
	 * @param vipDomain
	 * @param vipFunction
	 * @return
	 */
	private int vipGiftReward(UserDomain userDomain, VipDomain vipDomain, VipFunction vipFunction) {
		int vipGiftPropsId = vipDomain.intValue(DailyVipGift);
		if (vipGiftPropsId <= 0) {
			return NO_RIGHT;
		}
		
		boolean isReceive = vipDomain.booleanValue(vipFunction);
		if (isReceive) {
			return VIP_REWARD_RECEIVED;
		}
		
		PropsConfig config = propsManager.getPropsConfig(vipGiftPropsId);
		if (config == null) {
			return BASEDATA_NOT_FOUND;
		}
		
		long playerId = userDomain.getId();
		Player player = userDomain.getPlayer();
		int backpackType = BackpackType.DEFAULT_BACKPACK;
		int playerBackSize  = propsManager.getBackpackSize(playerId, DEFAULT_BACKPACK);
		UserProps userProps = UserProps.valueOf(playerId, backpackType, 1, config, true);
		ChainLock lock = LockUtils.getLock(player.getPackLock(), vipDomain.getPlayerVip());
		try {
			lock.lock();
			if(!player.canAddNew2Backpack(playerBackSize + 1, DEFAULT_BACKPACK)) {
				return BACKPACK_FULLED;
			}
			
			vipDomain.alterNum(1, vipFunction);
			propsManager.createUserProps(userProps);
			propsManager.put2UserPropsIdsList(playerId, backpackType, userProps);
			dbService.submitUpdate2Queue(vipDomain.getPlayerVip());
		} finally {
			lock.unlock();
		}
		
		MessagePushHelper.pushUserProps2Client(userDomain.getId(), backpackType, false, userProps);
		GoodsLogger.goodsLogger(player, Source.RECEIVE_VIP_REWARD, LoggerGoods.incomeProps(vipGiftPropsId, 1));
		return SUCCESS;
	}
	
}
