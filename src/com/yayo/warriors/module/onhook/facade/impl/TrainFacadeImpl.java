package com.yayo.warriors.module.onhook.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.onhook.constant.TrainConstant.*;
import static com.yayo.warriors.module.onhook.rule.TrainRule.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.user.type.AttributeKeys.*;
import static com.yayo.warriors.module.vip.model.VipFunction.*;
import static com.yayo.warriors.type.FormulaKey.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.EnumUtils;
import com.yayo.common.utility.TimeConstant;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.adapter.OnlineActiveService;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.common.helper.WorldPusherHelper;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.friends.entity.Friend;
import com.yayo.warriors.module.friends.helper.FriendHelper;
import com.yayo.warriors.module.friends.manager.FriendManager;
import com.yayo.warriors.module.horse.facade.HorseFacade;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.map.types.ScreenType;
import com.yayo.warriors.module.map.util.MapUtils;
import com.yayo.warriors.module.onhook.constant.TrainConstant;
import com.yayo.warriors.module.onhook.entity.UserTrain;
import com.yayo.warriors.module.onhook.facade.TrainFacade;
import com.yayo.warriors.module.onhook.helper.TrainHelper;
import com.yayo.warriors.module.onhook.manager.TrainManager;
import com.yayo.warriors.module.onhook.model.ReceiveType;
import com.yayo.warriors.module.onhook.model.UserSingleTrain;
import com.yayo.warriors.module.onhook.rule.TrainRule;
import com.yayo.warriors.module.onhook.vo.TrainVo;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.search.facade.SearchFacade;
import com.yayo.warriors.module.search.vo.CommonSearchVo;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.trade.manager.TradeManager;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.PlayerStatus;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.vip.manager.VipManager;
import com.yayo.warriors.module.vip.model.VipDomain;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.GoodsType;

/**
 * 挂机接口实现类
 * 
 * @author huachaoping
 */
@Component
public class TrainFacadeImpl implements TrainFacade, LogoutListener {

	@Autowired
	private DbService dbService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private TrainManager trainManager;
	@Autowired
	private TrainHelper trainHelper;
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private TradeManager tradeManager;
	@Autowired
	private VipManager vipManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private DungeonManager dungeonManager;
	@Autowired
	private EscortTaskManager escortTaskManager;
	@Autowired
	private SearchFacade searchFacade;
	@Autowired
	private HorseFacade horseFacade;
	@Autowired
	private FriendManager friendManager;
	@Autowired
	private WorldPusherHelper worldPusherHelper;
	@Autowired
	private OnlineActiveService onlineActiveService;
	
	/**
	 * 加载玩家闭关所得经验和真气
	 * 
	 * @param playerId     玩家ID
	 * @return {@link ResultObject}
	 */
	
	public ResultObject<TrainVo> loadClosedInfo(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return ResultObject.ERROR(TrainConstant.PLAYER_NOT_FOUND);
		}
		
		UserTrain userTrain = trainManager.getUserTrain(playerId);		
		if (!userTrain.isReceived()) {
			return ResultObject.SUCCESS(TrainVo.valueOf(0, 0, 0));
		}
		
		long curMillis = System.currentTimeMillis();
		long lagTime = TrainRule.lagTime(curMillis, userTrain.getStartTime());
		
		long curTrainTime = TrainRule.trainTime(lagTime);
		long curTrainMin = curTrainTime / TimeConstant.ONE_MINUTE_MILLISECOND;
		
		int userLevel = userDomain.getBattle().getLevel();
		
		int curExp = FormulaHelper.invoke(OFFLINE_TRAINING_EXP, userLevel, curTrainMin).intValue();
		int curGas = FormulaHelper.invoke(OFFLINE_TRAINING_GAS, curTrainMin).intValue();

		TrainVo trainVo = TrainVo.valueOf(curExp, curGas, curTrainTime);
		return ResultObject.SUCCESS(trainVo);
	}

	/**
	 * 领取闭关奖励 
	 * 
	 * @param playerId      玩家ID
	 * @param userItems     用户道具信息: 用户道具ID_数量
	 * @param propsId       基础道具ID
	 * @param multiple      领取倍数
	 * @param autoBuyCount  自动购买数量
	 * @return {@link CommonConstant}
	 */
	
	public int receiveReward(long playerId, String userItems, int propsId, int multiple, int autoBuyCount) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if (battle.getLevel() < OPEN_LEVEL) {
			return LEVEL_INVALID;
		}
		
		UserTrain userTrain = trainManager.getUserTrain(playerId);
		if (!userTrain.isReceived()) {
			return TRAIN_NOT_OPEN;
		}
		
		ReceiveType receiveType = EnumUtils.getEnum(ReceiveType.class, multiple);
		if (receiveType == null || receiveType == ReceiveType.NONE) {
			return TYPE_INVALID;
		}
		
		return receiveMultipleReward(userDomain, userTrain, userItems, propsId, autoBuyCount, multiple);
	}

	
	/**
	 * 领取单倍奖励
	 * 
	 * @param  userDomain
	 * @param  userTrain
	 * @return {@link CommonConstant}
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@Deprecated
	private int receiveCurrentReward(UserDomain userDomain, UserTrain userTrain) {
		long curMillis = System.currentTimeMillis();
		long lagTime = TrainRule.lagTime(curMillis, userTrain.getStartTime());
		
		long curTrainTime = TrainRule.trainTime(lagTime);
		if (curTrainTime < FIVE_MIN) {
			return TRAIN_TIME_LIMIT;
		}
		
		curTrainTime /= TimeConstant.ONE_MINUTE_MILLISECOND;
		
		PlayerBattle battle = userDomain.getBattle();
		int userLevel = battle.getLevel();
		
		int curExp = FormulaHelper.invoke(OFFLINE_TRAINING_EXP, userLevel, curTrainTime).intValue();
		int curGas = FormulaHelper.invoke(OFFLINE_TRAINING_GAS, curTrainTime).intValue();

		ChainLock lock = LockUtils.getLock(userTrain, battle);
		try {
			lock.lock();
			battle.increaseExp(curExp);
			battle.increaseGas(curGas);
			userTrain.setReceived(false);
		}  finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(battle, userTrain);
		List<Long> receiver = Arrays.asList(battle.getId());
		List<UnitId> playerUnits = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(battle.getId(), receiver, playerUnits, EXP, EXP_MAX, LEVEL, GAS);
		return SUCCESS;
	}
	
	
	/**
	 * 领取多倍奖励
	 * 
	 * @param domain
	 * @param userTrain
	 * @param userPropsId
	 * @param propsId
	 * @param autoBuyCount
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int receiveMultipleReward(UserDomain domain, UserTrain userTrain, String userItems, int propsId, int autoBuyCount, int multiple) {
		long curMillis = System.currentTimeMillis();
		long lagTime = TrainRule.lagTime(curMillis, userTrain.getStartTime());
		
		long curTrainTime = TrainRule.trainTime(lagTime);
		if (curTrainTime < FIVE_MIN) {
			return TRAIN_TIME_LIMIT;
		}
		
		curTrainTime /= TimeConstant.ONE_MINUTE_MILLISECOND;
		
		int requiredCount = FormulaHelper.invoke(OFFLINE_TRAINING_CONSUME, multiple).intValue();
		
		int trainItemCount = 0;
		List<LoggerGoods> goodsLoggers = new ArrayList<LoggerGoods>(2);
		Map<Long, Integer> userBackItems = this.spliteUserItems(userItems);
		for (Map.Entry<Long, Integer> entry : userBackItems.entrySet()) {
			long userPropsId = entry.getKey();
			int propsCount = entry.getValue();
			
			if (propsCount <= 0) {
				return INPUT_VALUE_INVALID;
			}
			UserProps userProps = propsManager.getUserProps(userPropsId);
			if (userProps == null) {
				return TRAIN_ITEM_NOT_ENOUGH;
			} else if (userProps.getPlayerId() != domain.getId()) {
				return BELONGS_INVALID;
			} else if (userProps.getBackpack() != DEFAULT_BACKPACK) {
				return NOT_IN_BACKPACK;
			} else if (userProps.getCount() < propsCount) {
				return ITEM_NOT_ENOUGH;
			} else if(userProps.isTrading()) {
				return ITEM_CANNOT_USE;
			}
			
			int childType = PropsChildType.INC_OFFLINE_EXP_TYPE;
			PropsConfig propsConfig = userProps.getPropsConfig();
			if (propsConfig.getChildType() != childType) {
				return TYPE_INVALID;
			}
			
			trainItemCount += propsCount;
			goodsLoggers.add(LoggerGoods.outcomeProps(userPropsId, propsConfig.getId(), propsCount));
		}
		
		PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
		if (propsConfig == null) {
			return BASEDATA_NOT_FOUND;
		}
		int autoCost = propsConfig.getMallPriceByCount(autoBuyCount);
		
		int userLevel = domain.getBattle().getLevel();
		
		int curExp = FormulaHelper.invoke(OFFLINE_TRAINING_EXP, userLevel, curTrainTime).intValue();
		int curGas = FormulaHelper.invoke(OFFLINE_TRAINING_GAS, curTrainTime).intValue();
		
		curExp *= multiple;
		curGas *= multiple;
		
		ChainLock lock = LockUtils.getLock(userTrain, domain.getBattle(), domain.getPlayer(), domain.getPackLock());
		try {
			lock.lock();
			if (trainItemCount + autoBuyCount != requiredCount) {
				return TRAIN_ITEM_NOT_ENOUGH;
			}
			if (domain.getPlayer().getGolden() < autoCost) {
				return GOLDEN_NOT_ENOUGH;
			}
			userTrain.setReceived(false);
			domain.getBattle().increaseExp(curExp);
			domain.getBattle().increaseGas(curGas);
			domain.getPlayer().decreaseGolden(autoCost);    // 扣除所需元宝
			dbService.submitUpdate2Queue(domain.getBattle(), domain.getPlayer(), userTrain);
		} finally {
			lock.unlock();
		}
		
		if (trainItemCount > 0) {
			GoodsVO goodsVo = GoodsVO.valueOf(propsId, GoodsType.PROPS, trainItemCount);
			List<UserProps> userPropsList = propsManager.costUserPropsList(userBackItems);
			MessagePushHelper.pushUserProps2Client(domain.getId(), DEFAULT_BACKPACK, false, userPropsList);
			MessagePushHelper.pushGoodsCountChange2Client(domain.getId(), goodsVo);
		}
		
		if (autoBuyCount > 0) {
			goodsLoggers.add(LoggerGoods.outcomePropsAutoBuyGolden(propsId, autoBuyCount, autoCost));
		}

		LoggerGoods[] goodsLoggerArray = goodsLoggers.toArray(new LoggerGoods[goodsLoggers.size()]);
		if (goodsLoggerArray.length > 0) {
			GoodsLogger.goodsLogger(domain.getPlayer(), Source.PLAYER_TRAINING, goodsLoggerArray);
		}
		
		if(autoCost != 0) {
			GoldLogger.outCome(Source.PLAYER_TRAINING, autoCost, domain.getPlayer(), goodsLoggerArray);
		}
		
			
		List<Long> receiver = Arrays.asList(domain.getId());
		List<UnitId> playerUnits = Arrays.asList(domain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(domain.getId(), receiver, playerUnits, EXP, EXP_MAX, LEVEL, GAS, GOLDEN);
		return SUCCESS;
	}
	
	
	/**
	 * 开启闭关接口
	 * 
	 * @param playerId     玩家ID
	 * @return {@link TrainConstant}
	 */
	
	public int startTrain(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain ==  null) {
			return PLAYER_NOT_FOUND;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		int userLevel = battle.getLevel();
		if (userLevel < OPEN_LEVEL) {
			return LEVEL_INVALID;
		}
		
		UserTrain userTrain = trainManager.getUserTrain(playerId);
		if (userTrain.isReceived()) {
			return TRAIN_OPENING;
		}
		
		long curMillis = System.currentTimeMillis();
		ChainLock lock = LockUtils.getLock(userTrain);
		try {
			lock.lock();
			boolean receive = userTrain.isReceived();
			if (userLevel >= OPEN_LEVEL && !receive) {
				userTrain.setReceived(true);
				userTrain.setStartTime(curMillis);
				dbService.submitUpdate2Queue(userTrain);
			}
		} finally {
			lock.unlock();
		}
		return SUCCESS;
	}

	
	/** 
	 * 玩家打坐或取消
	 * 
	 * @param playerId     玩家ID
	 * @return {@link TrainConstant}
	 */
	
	public int processSingleTrain(long playerId) {
		UserDomain domain = userManager.getUserDomain(playerId);
		if (domain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		PlayerMotion motion = domain.getMotion();              
		PlayerBattle battle = domain.getBattle();
		PlayerDungeon dungeon = dungeonManager.getPlayerDungeon(playerId);
		if (dungeon.isDungeonStatus()) {
			return FAILURE;
		}
		
		if (battle.getLevel() < OPEN_LEVEL) {
			return LEVEL_INVALID;
		} else if (escortTaskManager.isRide(battle)) {
			return FAILURE;
		} else if (horseFacade.isRide(playerId)) {
			return FAILURE;
		}
		
		boolean trading = tradeManager.isTradeState(playerId);
		if (trading) {
			return FAILURE;
		}
		
		long curSecond = System.currentTimeMillis();
		UserSingleTrain playerTrain = trainManager.getUserSingleTrain(playerId);
		long targetId = playerTrain.getTargetId();
		UserSingleTrain targetTrain = trainManager.getUserSingleTrain(targetId);
		
		ChainLock lock = targetTrain == null ? LockUtils.getLock(playerTrain)
								: LockUtils.getLock(playerTrain, targetTrain);
		try {
			lock.lock();
			if (playerTrain.isSingleTrain()) {
				playerTrain.removeTarget();
				playerTrain.setSingleTrain(false);
				if (targetTrain != null) {
					motion.setFace((byte)0);                    // 删除方向
					targetTrain.removeTarget();
					coupleTrainAddFriendValue(playerTrain.getAccumulateTime(), playerId, targetId);
				}
				playerTrain.clearAccumulateTime();
			} else {
				if (battle.isDead()) {
					return PLAYER_DEADED;
				}
				playerTrain.setSingleTrain(true);
				playerTrain.setStartTime(curSecond);
			}
		} finally {
			lock.unlock();
		}
		
//		if(post) {  // 这里的状态加错了...
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		trainHelper.pushScreenTrainMessage(playerId, isTrainStatus(playerId), playerIdList); // 客户端的操作依赖这个推送... 进入打坐和取消打坐都要推送...
		
		if (playerTrain.isCoupleTrain()) {                          // 双修推送
			pushViewCoupleTrain(domain);
		}
		
		if (targetId > 0L && !playerTrain.isSingleTrain()) {
			trainHelper.pushCancelCoupleTrain(domain.getPlayer(), targetId);
			trainHelper.pushScreenTrainMessage(targetId, isTrainStatus(targetId), playerIdList);
		}
		return SUCCESS;
	}


	/**
	 * 打坐双修领取奖励                
	 * 
	 * @param playerId     玩家ID
	 * @return {@link TrainConstant}
	 */
	
	public int receiveAward(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		PlayerDungeon dungeon = dungeonManager.getPlayerDungeon(playerId);
		if (dungeon.isDungeonStatus()) {
			return FAILURE;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if (battle.getLevel() < OPEN_LEVEL) {
			return LEVEL_INVALID;
		} else if (battle.isDead()) {
			return PLAYER_DEADED;
		}
		
		UserSingleTrain singleTrain = trainManager.getUserSingleTrain(playerId);

		if (singleTrain == null) {
			return TrainConstant.TRAIN_NOT_OPEN;
		} else if (!singleTrain.isSingleTrain()) {
			return TRAIN_NOT_OPEN;
		}
		
		int level    = battle.getLevel();
		int second   = (int) Math.ceil((double)RECEIVE_TIME / 1000);
		int awardGas = FormulaHelper.invoke(TRAIN_GAS_FORMULA, second).intValue();
		int awardExp = FormulaHelper.invoke(TRAIN_EXP_FORMULA, level, second).intValue(); 
		
		double expPercent = battle.getAttributeRate(AttributeKeys.TRAIN_EXP_RATE);
		double gasPercent = battle.getAttributeRate(AttributeKeys.TRAIN_GAS_RATE);
		
		float trainProfit = onlineActiveService.getTrainProfit(userDomain);//活动真气加成值
		int activeAwardGas = (int)(awardGas * trainProfit);//活动真气加成
		int activeAwardExp = (int)(awardExp * trainProfit);//活动经验加成
		
		// 增加VIP操作处理
		VipDomain vipDomain = vipManager.getVip(playerId);
		if (vipDomain.isVip()) {
			expPercent += vipDomain.floatValue(MeditationExpPercent);
			gasPercent += vipDomain.floatValue(MeditationGasPercent);
		}
		
		if (singleTrain.isContainTarget()) {                                   // 双修加成
			expPercent += 1;
			gasPercent += 1;
		}
		
		awardExp *= expPercent;
		awardGas *= gasPercent;
		
		awardExp += activeAwardExp;
		awardGas += activeAwardGas;
		
		awardExp = userDomain.getPlayer().calcIndulgeProfit(awardExp);         // 防沉迷收益计算
		
		ChainLock lock = LockUtils.getLock(battle, singleTrain);
		try {
			lock.lock();
			long curSecond = System.currentTimeMillis();
			long lagTime = lagTime(curSecond, singleTrain.getStartTime());
			
			if (!singleTrain.isSingleTrain()) {
				return TRAIN_NOT_OPEN;
			}
			
			// 判断打坐时间
			if (lagTime < RECEIVE_TIME) {
				return TRAIN_TIME_LIMIT;
			}
			
			awardGas = battle.getGas() >= MAX_VALUE ? 0 : awardGas;
			battle.increaseExp(awardExp);
			battle.increaseGas(awardGas);
			singleTrain.setStartTime(curSecond);
			singleTrain.addAccumulateTime(lagTime); 
			if (awardExp != 0) {                    //记录经验日志
				ExpLogger.trainExp(userDomain, awardExp);
			}
		} finally {
			lock.unlock();
		}
		
		trainHelper.pushTrainAttrMessage(playerId, awardExp, awardGas);
		List<Long> receiver = Arrays.asList(playerId);
		List<UnitId> playerUnits = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, receiver, playerUnits, EXP, EXP_MAX, LEVEL, GAS);
		return SUCCESS;
	}

	/**
	 * 邀请对方双修
	 * 
	 * @param playerId     玩家ID
	 * @param targetId     目标ID
	 * @return {@link TrainConstant}
	 */
	
	public int inviteCoupleTrain(long playerId, long targetId) {
		UserDomain playerDomain = userManager.getUserDomain(playerId);
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if (playerDomain == null || targetDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		PlayerDungeon dungeon = dungeonManager.getPlayerDungeon(playerId);
		if (dungeon.isDungeonStatus()) {
			return DUNGEON_CAN_NOT_COUPLE_TRAIN;
		} 
		
		if (playerDomain.getId() == targetDomain.getId()) {
			return NOT_INVITE_SELF;
		}
		
		if (playerDomain.getMapId() != targetDomain.getMapId()) {
			return OUT_OF_DISTANCE;
		}
		
		GameMap gameMap = playerDomain.getGameMap();
		if (gameMap != null) {
			if(gameMap.getScreenType() == ScreenType.CAMP.ordinal()){
				return CAMP_MAP_NOT_COUPLE_TRAIN;
			}
		}
		
		if (playerDomain.getBranching() != targetDomain.getBranching()) {
			return NOT_SAME_BRANCHING;
		}
		
		if (playerDomain.getBattle().isDead()) {
			return PLAYER_DEADED;
		} else if (targetDomain.getBattle().isDead()) {
			return PLAYER_DEADED;
		}

		if (playerDomain.getBattle().getLevel() < OPEN_LEVEL) {
			return LEVEL_INVALID;
		} else if (targetDomain.getBattle().getLevel() < OPEN_LEVEL) {
			return LEVEL_INVALID;
		}
		
		if (tradeManager.isTradeState(playerId)) {
			return FAILURE;
		} else if (tradeManager.isTradeState(targetId)) {
			return FAILURE;
		}
		
		if (horseFacade.isRide(playerId)) {
			return RIDING_CAN_NOT_COUPLE_TRAIN;
		} else if (horseFacade.isRide(targetId)) {
			return TARGET_RIDING;
		}
		
		if (escortTaskManager.isRide(playerDomain.getBattle())) {
			return ESCORT_TASK_NOT_COUPLE_TRAIN;
		} else if (escortTaskManager.isEscortStatus(targetDomain.getBattle())) {
			return ESCORT_TASK_NOT_COUPLE_TRAIN;
		}
		
		if (!canCoupleTrain(playerId)) {
			return PLAYER_COUPLE_TRAINING;
		}
		if (!canCoupleTrain(targetId)) {
			return PLAYER_COUPLE_TRAINING;
		}
		
		UserSingleTrain targetTrain = trainManager.getUserSingleTrain(targetId);
		if (targetTrain.isContainInviter(playerId)) {
			return INVITE_SENDED;
		} else if (targetTrain.inviterCount() >= INVITE_LIMIT) {
			return PLAYER_COUPLE_TRAINING;
		} else if (targetTrain.isSingleTrain()) {
			if (!MapUtils.checkPosScopeInfloatNoScale(playerDomain, targetDomain, 3)) {
				return OUT_OF_DISTANCE;
			}
		}
		
		PlayerStatus playerStatus = playerDomain.getBattle().getPlayerStatus();
		PlayerStatus targetStatus = targetDomain.getBattle().getPlayerStatus();
		if (playerStatus.isFighting() || targetStatus.isFighting()) {
			return ROLE_FIGHTING;
		}
		
		ChainLock lock = LockUtils.getLock(targetTrain);
		try {
			lock.lock();
			targetTrain.add2InviteSet(playerId);
		} finally {
			lock.unlock();
		}
		trainHelper.applyCouplesTrain(playerDomain.getPlayer(), playerDomain.getBattle(), targetId);
		return SUCCESS;
	}

	/**
	 * 同意双修
	 * 
	 * @param playerId     玩家ID
	 * @param targetId     目标ID
	 * @return {@link TrainConstant}
	 */
	
	public int acceptCoupleTrain(long playerId, long targetId) {
		UserDomain playerDomain = userManager.getUserDomain(playerId);
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if (playerDomain == null || targetDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		UserSingleTrain playerTrain = trainManager.getUserSingleTrain(playerId);
		UserSingleTrain targetTrain = trainManager.getUserSingleTrain(targetId);
		if (!playerTrain.isContainInviter(targetId)) {
			return HAVE_NOT_INVITED;
		}
		playerTrain.removeInviter(targetId);              
		
		if (playerDomain.getId() == targetDomain.getId()) {
			return NOT_INVITE_SELF;
		}
		
		PlayerDungeon dungeon = dungeonManager.getPlayerDungeon(playerId);
		if (dungeon.isDungeonStatus()) {
			return DUNGEON_CAN_NOT_COUPLE_TRAIN;
		} 
		
		GameMap gameMap = playerDomain.getGameMap();
		if (gameMap != null) {
			if(gameMap.getScreenType() == ScreenType.CAMP.ordinal()){
				return CAMP_MAP_NOT_COUPLE_TRAIN;
			}
		}
		
		if (playerDomain.getBranching() != targetDomain.getBranching()) {
			return NOT_SAME_BRANCHING;
		}
		
		if (!canCoupleTrain(playerId)) {
			return PLAYER_COUPLE_TRAINING;
		}
		if (!canCoupleTrain(targetId)) {
			return PLAYER_COUPLE_TRAINING;
		}
		
		if (!MapUtils.checkPosScopeInfloatNoScale(playerDomain, targetDomain, 3)) {
			return INVITER_DISTANCE_LONG;
		}
		
		if (playerDomain.getBattle().isDead()) {
			return PLAYER_DEADED;
		} else if (targetDomain.getBattle().isDead()) {
			return PLAYER_DEADED;
		}
		
		if (playerDomain.getBattle().getLevel() < OPEN_LEVEL) {
			return LEVEL_INVALID;
		} else if (targetDomain.getBattle().getLevel() < OPEN_LEVEL) {
			return LEVEL_INVALID;
		}
		
		if (tradeManager.isTradeState(playerId)) {
			return FAILURE;
		} else if (tradeManager.isTradeState(targetId)) {
			return FAILURE;
		}
		
		if (horseFacade.isRide(playerId)) {
			return RIDING_CAN_NOT_COUPLE_TRAIN;
		} else if (horseFacade.isRide(targetId)) {
			return TARGET_RIDING;
		}
		
		if (escortTaskManager.isRide(playerDomain.getBattle())) {
			return ESCORT_TASK_NOT_COUPLE_TRAIN;
		} else if (escortTaskManager.isEscortStatus(targetDomain.getBattle())) {
			return ESCORT_TASK_NOT_COUPLE_TRAIN;
		}
		
		ChainLock lock = LockUtils.getLock(playerTrain, targetTrain);
		try {
			lock.lock();
			playerTrain.clearInviteCache();
			targetTrain.clearInviteCache();
			playerTrain.addTarget(targetId);
			targetTrain.addTarget(playerId);
			playerTrain.clearAccumulateTime();     // 双修累计时间清0
		} finally {
			lock.unlock();
		}
		trainHelper.processCouplesTrain(playerDomain.getPlayer(), targetId, true);
		
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		if (playerTrain.isSingleTrain()) {
			trainHelper.pushScreenTrainMessage(playerId, isTrainStatus(playerId), playerIdList);
		}
//		if (targetTrain.isSingleTrain()) {
//			trainHelper.pushScreenTrainMessage(targetId, isTrainStatus(playerId), playerIdList);
//		}
		
		return SUCCESS;
	}
	
	/**
	 * 拒绝双修
	 * 
	 * @param playerId     玩家ID
	 * @param targetId     目标ID
	 * @return {@link TrainConstant}
	 */
	
	public int rejectCoupleTrain(long playerId, long targetId) {
		UserDomain playerDomain = userManager.getUserDomain(playerId);
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if (playerDomain == null || targetDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = playerDomain.getPlayer();
		UserSingleTrain playerTrain = trainManager.getUserSingleTrain(playerId);
		if (!playerTrain.isContainInviter(targetId)) {
			return HAVE_NOT_INVITED;
		}
		
		ChainLock lock = LockUtils.getLock(playerTrain);
		try {
			lock.lock();
			playerTrain.removeInviter(targetId);
		} finally {
			lock.unlock();
		}
		trainHelper.processCouplesTrain(player, targetId, false);
		return SUCCESS;
	}

	/**
	 * 查找双修玩家
	 * 
	 * @param playerId     玩家ID
	 * @param keywords     关键字
	 * @return
	 */
	
	public Collection<CommonSearchVo> getSearchPlayer(long playerId, String keywords) {
		return searchFacade.searchScreenPlayer(playerId, keywords);
	}
	
	/**
	 * 保存双修方向
	 * 
	 * @param playerId
	 * @param direction
	 * @return {@link TrainConstant}
	 */
	
	public void savePlayerDirection(long playerId, int direction) {
		UserDomain domain = userManager.getUserDomain(playerId);
		if (domain != null) {
			domain.getMotion().setFace((byte) direction);       // 保存方向...
			UserSingleTrain singleTrain = trainManager.getUserSingleTrain(playerId);
			if (singleTrain.isCoupleTrain()) {                  // 如果保存方向的时候玩家已是双修状态, 推送同屏
				pushViewCoupleTrain(domain);
			}
			trainHelper.pushPlayerDirection(domain.getPlayer(), direction);    // 把方向返回给客户端
		}
	}
	
	
//	/**
//	 * 取消双修
//	 * 
//	 * @param playerId     玩家ID
//	 * @param targetId     目标ID
//	 * @return {@link TrainConstant}
//	 */
//	
//	public int cancleCoupleTrain(long playerId, long targetId) {
//		UserDomain playerDomain = userManager.getUserDomain(playerId);
//		UserDomain targetDomain = userManager.getUserDomain(targetId);
//		if (playerDomain == null || targetDomain == null) {
//			return PLAYER_NOT_FOUND;
//		}
//		
//		UserSingleTrain playerTrain = trainManager.getUserSingleTrain(playerId);
//		UserSingleTrain targetTrain = trainManager.getUserSingleTrain(targetId);
//		
//		ChainLock lock = LockUtils.getLock(playerTrain, targetTrain);
//		try {
//			lock.lock();
//			playerTrain.setSingleTrain(false);
//			playerTrain.removeTarget();
//			targetTrain.removeTarget();
//		} finally {
//			lock.unlock();
//		}
//		trainManager.removeSingleTrainCache(playerId);
//		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
//		trainHelper.pushScreenTrainMessage(playerId, false, playerIdList);
//		return SUCCESS;
//	}
	

	/**
	 * 是否能双修
	 * @param playerId
	 * @return
	 */
	private boolean canCoupleTrain(long playerId) {
		UserSingleTrain train = trainManager.getUserSingleTrain(playerId);
		return train != null && !train.isCoupleTrain();
	}	
	
	/**
	 * 推送可视玩家自己的状态改变
	 * 
	 * @param domain
	 */
	private void pushViewCoupleTrain(UserDomain domain) {
		Set<ISpire> players = domain.getGameMap().getCanViewsSpireCollection(domain, ElementType.PLAYER);
		for (Iterator<ISpire> iterator = players.iterator(); iterator.hasNext();) {
			ISpire spire = iterator.next();
			if (spire == domain) {
				iterator.remove();
				continue;
			}
			((UserDomain)spire).putHideSpire(domain);
			((UserDomain)spire).putCanViewSpire(domain);
		}
		worldPusherHelper.putMessage2Queue(players);
	}

	
	/**
	 * 是否打坐状态
	 * @param playerId            玩家ID
	 * @return 
	 */
	
	public int isTrainStatus(long playerId) {
		UserSingleTrain singleTrain = trainManager.getUserSingleTrain(playerId);
		if (singleTrain == null) {
			return 0;              // 普通状态 
		} else if (singleTrain.isSingleTrain() && !singleTrain.isContainTarget()) {
			return 1;              // 单人打坐状态
		} else if (singleTrain.isSingleTrain() && singleTrain.isContainTarget()) {
			return 2;              // 双修状态
		}
		return 0;
	}

	
	/**
	 * 下线取消打坐
	 * @param playerId
	 * @return
	 */
	
	public void onLogoutEvent(UserDomain userDomain) {
		long playerId = userDomain.getPlayerId();
		if (isTrainStatus(playerId) > 0) {
			this.processSingleTrain(playerId);
		}
		trainManager.removeSingleTrainCache(playerId);
	}

	
	
	
	public void cancelSingleTrain(long playerId) {
		if (isTrainStatus(playerId) > 0) {
			this.processSingleTrain(playerId);
		}
	}
	
	
	/**
	 * 增加好友度
	 * 
	 * @param accumulateTime         累计时间
	 * @param playerId               玩家ID 
	 * @param targetId               目标ID
	 */
	private void coupleTrainAddFriendValue(int accumulateTime, long playerId, long targetId) {
		UserDomain playerDomain = userManager.getUserDomain(playerId);
		if (playerDomain == null) {
			return;
		}
		
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if (targetDomain == null) {
			return;
		}
		
		if (accumulateTime < TimeConstant.ONE_MINUTE_MILLISECOND) {
			return;
		}
		
		int value = accumulateTime / TimeConstant.ONE_MINUTE_MILLISECOND;
		Friend pfriend = friendManager.raiseFriendly(playerId, targetId, value);
		if (pfriend != null) {
			FriendHelper.plusFriendValue(playerDomain.getPlayer(), playerId, pfriend, value);
		}
		
		Friend tfriend = friendManager.raiseFriendly(targetId, playerId, value);
		if (tfriend != null) {
			FriendHelper.plusFriendValue(targetDomain.getPlayer(), targetId, tfriend, value);
		}
		
	}
	
	
	/**
	 * 截取出字符串.
	 * 
	 * @param userItems		用户道具信息. 格式: 用户道具ID_数量|用户道具ID_数量|...
	 * @return
	 */
	private Map<Long, Integer> spliteUserItems(String userItems) {
		Map<Long, Integer> maps = new HashMap<Long, Integer>(2);
		List<String[]> arrays = Tools.delimiterString2Array(userItems);
		if(arrays != null && !arrays.isEmpty()) {
			for (String[] array : arrays) {
				Long userItemId = Long.valueOf(array[0]);
				Integer count = Integer.valueOf(array[1]);
				if(userItemId == null || count == null || count < 0) {
					continue;
				}
				
				Integer cacheCount = maps.get(userItemId);
				cacheCount = cacheCount == null ? 0 : cacheCount;
				maps.put(userItemId, count + cacheCount);
			}
		}
		return maps;
	}

	
}
