package com.yayo.warriors.module.trade.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.trade.constant.TradeConstant.*;
import static com.yayo.warriors.module.trade.rule.TradeRule.*;
import static com.yayo.warriors.module.achieve.model.AchieveType.FIRST_ACHIEVE;
import static com.yayo.warriors.module.achieve.model.FirstType.FIRST_TRADE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.BeanUtil;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.achieve.facade.AchieveFacade;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.log.TradeLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.trade.constant.TradeConstant;
import com.yayo.warriors.module.trade.facade.TradeFacade;
import com.yayo.warriors.module.trade.helper.TradeHelper;
import com.yayo.warriors.module.trade.manager.TradeManager;
import com.yayo.warriors.module.trade.model.TradeProps;
import com.yayo.warriors.module.trade.model.UserTrade;
import com.yayo.warriors.module.trade.rule.TradeReason;
import com.yayo.warriors.module.trade.vo.TradeVo;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.type.Currency;
import com.yayo.warriors.type.GoodsType;

/**
 * 交易系统接口实现类
 * 
 * @author huachaoping
 */
@Component
public class TradeFacadeImpl implements TradeFacade, LogoutListener {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private DbService dbService;
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private TradeManager tradeManager;
	@Autowired
	private AchieveFacade achieveFacade;
	
	
	/**
	 * 角色登出, 取消交易
	 * @param playerId
	 */
	
	public void onLogoutEvent(UserDomain userDomain) {
		long playerId = userDomain.getPlayerId();
		UserTrade userTrade = tradeManager.getTradeCache(playerId);
		if (userTrade != null) {
			long targetId = userTrade.getTradeTarget();
			cancleTrade(playerId, targetId, TradeReason.CANCLE_LOGOUT);
		}
	}
	
	/**
	 * 发送交易邀请
	 * @param playerId                邀请者ID
	 * @param targetId                被邀请者ID
	 * @return {@link TradeConstant}
	 */
	
	public int inviteTrade(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		if (!userManager.isOnline(targetId)) {
			return TARGET_OFF_LINE;
		}
		
		if (userDomain.getBattle().isDead()) {
			return PLAYER_DEADED;
		}

		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		Player target = targetDomain.getPlayer();
		if (player == target) {
			return NOT_INVITE_SELF;
		}
		
		int playerBranching = player.getBranching();
		int targetBranching = target.getBranching();
		if (playerBranching != targetBranching) {
			return BRANCHING_DEFERENCE;
		}
		
		int playerMapId = userDomain.getMapId();
		int targetMapId = targetDomain.getMapId();  
		if (playerMapId != targetMapId) {
			return MAP_DEFERENCE;
		}
		
		UserTrade playerTrade = tradeManager.getUserTrade(playerId);
		UserTrade targetTrade = tradeManager.getUserTrade(targetId);
		if (playerTrade.isTraded()) {
			return PLAYER_TRADING;
		}
		if (targetTrade.isTraded()) {
			return PLAYER_TRADING;
		}
		
		Set<Long> invited = targetTrade.getBeInvitedPlayerIds();
		if (invited.contains(playerId)) {
			return INVITE_MESSAGE_SENDED;
		} else if (invited.size() >= TRADE_LIMIT) {
			return PLAYER_TRADING;
		}
		
		ChainLock lock = LockUtils.getLock(targetTrade);
		try {
			lock.lock();
			invited = targetTrade.getBeInvitedPlayerIds();
			if (invited.contains(playerId)) {
				return INVITE_MESSAGE_SENDED;
			} else if (invited.size() >= TRADE_LIMIT) {
				return PLAYER_TRADING;
			}
			targetTrade.add2InvitedSet(playerId);
		} finally {
			lock.unlock();
		}
		TradeHelper.pushInvitedMessage(playerId, targetId, player.getName());
		return SUCCESS;
	}
	
	/**
	 * 同意交易
	 * @param playerId                被邀请者ID          
	 * @param targetId                邀请者ID
	 * @return {@link TradeConstant}  
	 */
	
	public int acceptTrade(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}

		if(userDomain.getBattle().isDead()) {
			return PLAYER_DEADED;
		}
		
		Player player = userDomain.getPlayer();
		Player target = targetDomain.getPlayer();
		
		UserTrade playerTrade = tradeManager.getUserTrade(playerId);
		UserTrade targetTrade = tradeManager.getUserTrade(targetId);
		Set<Long> beInvitedPlayerIds = playerTrade.getBeInvitedPlayerIds();
		
		if (!userManager.isOnline(targetId)) {
			beInvitedPlayerIds.remove(targetId);
			return TARGET_OFF_LINE;
		}
		
		int playerBranching = player.getBranching();
		int targetBranching = target.getBranching();
		if (playerBranching != targetBranching) {
			beInvitedPlayerIds.remove(targetId);
			return BRANCHING_DEFERENCE;
		}
		
		int playerMapId = userDomain.getMapId();
		int targetMapId = targetDomain.getMapId();  
		if (playerMapId != targetMapId) {
			beInvitedPlayerIds.remove(targetId);
			return MAP_DEFERENCE;
		}
		
		ChainLock lock = LockUtils.getLock(targetTrade, playerTrade);
		try {
			lock.lock();
			if (playerTrade.isTraded()) {
				return PLAYER_TRADING;
			}
			if(targetTrade.isTraded()) {
				return PLAYER_TRADING; 
			}
			playerTrade.addTrade(targetId);
			targetTrade.addTrade(playerId);
		} finally {
			lock.unlock();
		}
		TradeHelper.pushPlayerBeInvited(playerId, targetId, true, player.getName());
		return SUCCESS;
	}

	/**
	 * 拒绝交易  
	 * @param playerId                被邀请者ID     
	 * @param targetId                邀请者ID    
	 * @return {@link TradeConstant}   
	 */
	
	public int rejectTrade(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		UserTrade userTrade = tradeManager.getUserTrade(playerId);
		
		ChainLock lock = LockUtils.getLock(userTrade);
		try {
			lock.lock();
			userTrade.removeBeInvitedPlayerId(targetId);
		} finally {
			lock.unlock();
		}
		TradeHelper.pushPlayerBeInvited(playerId, targetId, false, player.getName());
		return SUCCESS;
	}

	/**
	 * 加入物品
	 * 
	 * @param  playerId                玩家Id   
	 * @param  targetId                目标Id     
	 * @param  userPropsId             用户道具Id          
	 * @param  count                   道具数量        
	 * @return {@link TradeConstant}   模块常量
	 */
	
	public int addUserProps(long playerId, long targetId, long userPropsId, int count) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		if (count < 0) {
			return INPUT_VALUE_INVALID;
		}
		
		int backpack = BackpackType.DEFAULT_BACKPACK;
		UserProps userProps = propsManager.getUserProps(userPropsId);
		if (userProps == null) {
			return ITEM_NOT_FOUND;
		} else if (!userProps.validBackpack(backpack)) {
			return NOT_IN_BACKPACK;
		} else if (userProps.getPlayerId() != playerId) {
			return BELONGS_INVALID;
		} else if (userProps.getCount() != count) {
			return ITEM_NOT_ENOUGH;
		} else if (userProps.isBinding()) {
			return PROPS_BINDING;
		}
		
		PropsConfig config = userProps.getPropsConfig();
		if (config == null) {
			return BASEDATA_NOT_FOUND;
		} else if (config.getMaxAmount() < count) {
			return OUT_OF_MAXCOUNT;
		}
		
		UserTrade userTrade = tradeManager.getUserTrade(playerId);
		if (userTrade.isLockProps()) {
			return TRADE_LOCKING;
		} 
		if (userTrade.getTradeTarget() != targetId) {
			return TARGET_INVALID;
		}
		
		TradeProps tradeProps = null;
		ChainLock lock = LockUtils.getLock(userTrade, userDomain.getPackLock());
		int baseId     = userProps.getBaseId();
		int goodType   = userProps.getGoodsType();
		try {
			lock.lock();
			if (!userTrade.validPropsCount(userProps, count)) {
				return OUT_OF_MAXCOUNT;
			}
			
			tradeProps = TradeProps.valueOf(userPropsId, baseId, count, goodType);
			if (!userTrade.addTradeProps(tradeProps)) {
				return OUT_OF_MAXCOUNT;
			}
			userProps.setTrading(true);
		} finally {
			lock.unlock();
		}
		
		TradeVo vo = TradeVo.valueOf(playerId, userTrade);
		TradeHelper.pushPlayerTradeProps(playerId, targetId, vo);
		return SUCCESS;
	}

	/**
	 * 加入装备
	 * @param playerId                玩家Id  
	 * @param targetId                目标Id
	 * @param userEquipId             用户装备Id  
	 * @param index 
	 * @return {@link TradeConstant}  模块常量   
	 */
	
	public int addUserEquip(long playerId, long targetId, long userEquipId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		int backpack = BackpackType.DEFAULT_BACKPACK;
		UserEquip userEquip = propsManager.getUserEquip(userEquipId);
		if (userEquip == null) {
			return ITEM_NOT_FOUND;
		} else if (!userEquip.validBackpack(backpack)) {
			return NOT_IN_BACKPACK;
		} else if (userEquip.getPlayerId() != playerId) {
			return BELONGS_INVALID;
		} else if (userEquip.getCount() < 1) {
			return ITEM_NOT_ENOUGH;
		} else if (userEquip.isBinding()) {
			return PROPS_BINDING;
		}
		
		UserTrade userTrade = tradeManager.getUserTrade(playerId);
		if (userTrade.isLockProps()) {
			return TRADE_LOCKING;
		}
		if (userTrade.getTradeTarget() != targetId) {
			return TARGET_INVALID;
		}
		
		TradeProps tradeProps = null;
		ChainLock lock = LockUtils.getLock(userTrade, userDomain.getPackLock());
		int baseId     = userEquip.getBaseId();
		int goodType   = userEquip.getGoodsType();
		try {
			lock.lock();
			tradeProps = TradeProps.valueOf(userEquipId, baseId, 1, goodType);
			if (!userTrade.addTradeProps(tradeProps)) {
				return OUT_OF_MAXCOUNT;
			}
			userEquip.setTrading(true);
		} finally {
			lock.unlock();
		}
		
		TradeVo vo = TradeVo.valueOf(playerId, userTrade);
		TradeHelper.pushPlayerTradeProps(playerId, targetId, vo);
		return SUCCESS;
	}

	/**
	 * 移除物品(交易中, 包括装备)
	 * 
	 * @param playerId
	 * @param targetId
	 * @param goodsId
	 * @param count
	 * @param goodsType
	 * @return {@link TradeConstant}
	 */
	
	public int removeUserProps(long playerId, long targetId, long goodsId, int count, int goodsType) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		UserTrade userTrade = tradeManager.getUserTrade(playerId);
		if (userTrade.isLockProps()) {
			return TRADE_LOCKING;
		}
		
		if (goodsType == GoodsType.PROPS) {
			return removeProps(goodsId, count, goodsType, userTrade);
		} else if (goodsType == GoodsType.EQUIP) {
			return removeEquip(goodsId, count, goodsType, userTrade);
		}
		return FAILURE;
	}

	/**
	 * 移除物品
	 * @param goodsId
	 * @param count
	 * @param goodsType
	 * @param userTrade
	 * @return
	 */
	private int removeProps(long goodsId, int count, int goodsType, UserTrade userTrade) {
		UserProps userProps = propsManager.getUserProps(goodsId);
		if (userProps == null) {
			return ITEM_NOT_FOUND;
		} else if (!userProps.isTrading()) {
			return TRADE_LOCKING;
		} else if (userTrade.getPlayerId() != userProps.getPlayerId()) {
			return BELONGS_INVALID;
		}
		UserDomain userDomain = userManager.getUserDomain(userProps.getPlayerId());
		ChainLock lock = LockUtils.getLock(userTrade, userDomain.getPackLock());
		try {
			lock.lock();
			if (!userTrade.removeProps(goodsId, goodsType, count)) {
				return FAILURE;
			}
			userProps.setTrading(false);
		} finally {
			lock.unlock();
		}
		TradeVo vo = TradeVo.valueOf(userTrade.getPlayerId(), userTrade);
		TradeHelper.pushPlayerTradeProps(userTrade.getPlayerId(), userTrade.getTradeTarget(), vo);
		return SUCCESS;
	}
	
	/**
	 * 移除装备
	 * @param goodsId
	 * @param count
	 * @param goodsType
	 * @param userTrade
	 * @return
	 */
	private int removeEquip(long goodsId, int count, int goodsType, UserTrade userTrade) {
		UserEquip userEquip = propsManager.getUserEquip(goodsId);
		if (userEquip == null) {
			return ITEM_NOT_FOUND;
		} else if (!userEquip.isTrading()) {
			return TRADE_LOCKING;
		} else if (userTrade.getPlayerId() != userEquip.getPlayerId()) {
			return BELONGS_INVALID;
		}
		UserDomain userDomain = userManager.getUserDomain(userEquip.getPlayerId());
		ChainLock lock = LockUtils.getLock(userTrade, userDomain.getPackLock());
		try {
			lock.lock();
			if (!userTrade.removeProps(goodsId, goodsType, count)) {
				return FAILURE;
			}
			userEquip.setTrading(false);
		} finally {
			lock.unlock();
		}
		TradeVo vo = TradeVo.valueOf(userTrade.getPlayerId(), userTrade);
		TradeHelper.pushPlayerTradeProps(userTrade.getPlayerId(), userTrade.getTradeTarget(), vo);
		return SUCCESS;
	}

	
	/**
	 * 加入钱币(交易中)
	 * @param playerId                玩家Id
	 * @param currency                货币类型
	 * @param count                   货币数量
	 * @return {@link TradeConstant}
	 */
	
	public int addCurrency(long playerId, long targetId, int currency, long count) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		
		int golden  = Currency.GOLDEN.ordinal();
		int siliver = Currency.SILVER.ordinal();
		if (currency != golden && currency != siliver) {
			return TYPE_INVALID;
		}
		
		if (currency == golden && player.getGolden() < count) {
			return OUT_OF_MAXCOUNT;
		} else if (currency == siliver && player.getSilver() < count) {
			return OUT_OF_MAXCOUNT;
		} else if (count < 0) {
			return INPUT_VALUE_INVALID;
		}
		
		String currencyCount = String.valueOf(count);
		if (CURRENCY_COUNT <= currencyCount.length()) {
			return OUT_OF_MAXCOUNT;
		}
		
		UserTrade userTrade = tradeManager.getUserTrade(playerId);
		if (userTrade.isLockProps()) {
			return TRADE_LOCKING;
		}
		
		ChainLock lock = LockUtils.getLock(userTrade);
		try {
			lock.lock();
			userTrade.addCurrency(currency, count);
		} finally {
			lock.unlock();
		}
		TradeVo vo = TradeVo.valueOf(playerId, userTrade);
		TradeHelper.pushPlayerTradeProps(playerId, targetId, vo);
		return SUCCESS;
	}
	
	/** 
	 * 锁定交易
	 * @param playerId                玩家Id
	 * @param targetId                目标Id
	 * @return {@link TradeConstant}
	 */
	
	public int lockTrade(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		UserTrade userTrade = tradeManager.getUserTrade(playerId);
		if (userTrade.getTradeTarget() != targetId) {
			return TARGET_INVALID;
		} 
		
		ChainLock lock = LockUtils.getLock(userTrade);
		try {
			lock.lock();
			if(userTrade.isLockProps()) {
				return SUCCESS;
			}
			userTrade.setLockProps(true);
		} finally {
			lock.unlock();
		}
		TradeHelper.pushLockProps(playerId, targetId, true);
		return SUCCESS;
	}

	/**
	 * 取消交易
	 * @param playerId                玩家Id
	 * @param targetId                目标Id
	 * @return {@link TradeConstant}
	 */
	 
	public int cancleTrade(long playerId, long targetId, int reason) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		UserTrade playerTrade = tradeManager.getUserTrade(playerId);
		resetTradeProps(userDomain, playerTrade.getPropsList());
		
		UserTrade targetTrade = null;
		if (targetId > 0) {
			targetTrade = tradeManager.getUserTrade(targetId);
			UserDomain targetDomain = userManager.getUserDomain(targetId);
			resetTradeProps(targetDomain, targetTrade.getPropsList());
		}
		
		ChainLock lock = targetTrade == null ? LockUtils.getLock(playerTrade)
								: LockUtils.getLock(playerTrade, targetTrade);
		try {
			lock.lock();
			playerTrade.cancleTrade();
			if (targetTrade != null) {
				targetTrade.cancleTrade();
			}
		} finally {
			lock.unlock();
		}
		if (targetTrade != null) {
			TradeHelper.pushCancleTrade(targetId, player.getName(), reason);
		}
		tradeManager.removeTradeCache(playerId, targetId);
		return SUCCESS;
	}

	/**
	 * 交易处理
	 * @param playerId
	 * @param targetId
	 */
	
	public int completeTrade(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		Player target = targetDomain.getPlayer();
		UserTrade playerTrade = tradeManager.getUserTrade(playerId);
		UserTrade targetTrade = tradeManager.getUserTrade(targetId);
		boolean playerLockProps = playerTrade.isLockProps();
		boolean targetLockProps = targetTrade.isLockProps();
		if (!playerLockProps || !targetLockProps) {
			return CAN_NOT_TRADE;
		}
		
		ChainLock tradelock = LockUtils.getLock(playerTrade);
		try {
			tradelock.lock();
			if (playerTrade.isClick2Trade()) {
				return SUCCESS;
			}
			playerTrade.setClick2Trade(true);
		} finally {
			tradelock.unlock();
		}
		
		if (!targetTrade.isClick2Trade()) {
			TradeHelper.pushClick2Trade(playerId, targetId, true);
			return SUCCESS;
		}
		
		Collection<TradeProps> playerProps = playerTrade.getPropsList();              //玩家交易物品集合
		Collection<TradeProps> targetProps = targetTrade.getPropsList();              //目标交易物品集合
		int playerMaxBackSize = player.getMaxBackSize();                              //玩家背包最大格子数
		int targetMaxBackSize = target.getMaxBackSize();                              //目标背包最大格子数
		int playerBackSize  = propsManager.getBackpackSize(playerId, DEFAULT_BACKPACK); 
		int targetBackSize  = propsManager.getBackpackSize(targetId, DEFAULT_BACKPACK);
		int playerCacheSize = playerBackSize + targetProps.size();                    
		int targetCacheSize = targetBackSize + playerProps.size();
		if (playerMaxBackSize < playerCacheSize || targetMaxBackSize < targetCacheSize) {
			this.cancleTrade(playerId, targetId, TradeReason.CANCLE_BACKFULL);
			return BACKPACK_FULLED;
		}
		
		Collection<TradeProps> tradePropsList = new ArrayList<TradeProps>();          //交易所有物品集合
		tradePropsList.addAll(playerProps);
		tradePropsList.addAll(targetProps);
		List<Long> userIds = Arrays.asList(playerId, targetId);                       //交易双方
		int result = exchangeTradeProps(userIds, tradePropsList);
		if (result < SUCCESS) {
			this.cancleTrade(playerId, targetId, TradeReason.CANCLE_ACTION);
			return result;
		}
		
		ResultObject<List<BackpackEntry>> playerResult = getBackpackEntries(playerId, targetProps);
		ResultObject<List<BackpackEntry>> targetResult = getBackpackEntries(targetId, playerProps);
		if (playerResult.getResult() < SUCCESS || targetResult.getResult() < SUCCESS) {                                      
			this.cancleTrade(playerId, targetId, TradeReason.CANCLE_BACKFULL);         
			return playerResult.getResult() < SUCCESS ? playerResult.getResult() : targetResult.getResult();
		}
		
		List<BackpackEntry> playerEntry = decreaseTradeItems(userDomain, playerProps);
		List<BackpackEntry> targetEntry = decreaseTradeItems(targetDomain, targetProps);
		playerEntry.addAll(playerResult.getValue());
		targetEntry.addAll(targetResult.getValue());
		
		long playerGolden  = playerTrade.getCurrency(Currency.GOLDEN.ordinal());      //玩家交易元宝
		long playerSiliver = playerTrade.getCurrency(Currency.SILVER.ordinal());      //玩家交易银币
		long targetGolden  = targetTrade.getCurrency(Currency.GOLDEN.ordinal());      //目标交易元宝
		long targetSiliver = targetTrade.getCurrency(Currency.SILVER.ordinal());      //目标交易银币
		
		ChainLock lock = LockUtils.getLock(player, target, player.getPackLock(), target.getPackLock());
		try {
			lock.lock();
			if (player.getGolden() < playerGolden || player.getSilver() < playerSiliver) {
				this.cancleTrade(playerId, targetId, TradeReason.CANCLE_ACTION);  
				return GOLDEN_NOT_ENOUGH;
			}
			if (target.getGolden() < targetGolden || target.getSilver() < targetSiliver) {
				this.cancleTrade(playerId, targetId, TradeReason.CANCLE_ACTION);
				return SILVER_NOT_ENOUGH;
			}
			player.increaseGolden(targetGolden);
			player.increaseSilver(targetSiliver);
			player.decreaseGolden(playerGolden);
			player.decreaseSilver(playerSiliver);
			
			target.increaseGolden(playerGolden);
			target.increaseSilver(playerSiliver);
			target.decreaseGolden(targetGolden);
			target.decreaseSilver(targetSiliver);
		} catch (Exception e) {
			LOGGER.error("{}", e);
		} finally {
			lock.unlock();
		}
		
		
		/** 日志*/
		if(targetGolden > 0){
			GoldLogger.inCome(Source.PLAYER_TRADE, targetGolden, player);
			GoldLogger.outCome(Source.PLAYER_TRADE, targetGolden, target);
		}
		if(targetSiliver > 0){
			SilverLogger.inCome(Source.PLAYER_TRADE, targetSiliver, player);
			SilverLogger.outCome(Source.PLAYER_TRADE, targetSiliver, target);
		}
		if(playerGolden > 0){
			GoldLogger.inCome(Source.PLAYER_TRADE, playerGolden, target);
			GoldLogger.outCome(Source.PLAYER_TRADE, playerGolden, player);
		}
		if(playerSiliver > 0){
			SilverLogger.inCome(Source.PLAYER_TRADE, playerSiliver, target);
			SilverLogger.outCome(Source.PLAYER_TRADE, playerSiliver, player);
		}
		
		List<LoggerGoods> playerGoods = new ArrayList<LoggerGoods>(playerProps.size());
		List<LoggerGoods> targetGoods = new ArrayList<LoggerGoods>(playerProps.size());
		if(!playerProps.isEmpty()){
			for(TradeProps tradeProps : playerProps){
				if(tradeProps.getGoodType() == GoodsType.PROPS) { //角色支出, 目标支出
					targetGoods.add(LoggerGoods.incomeProps(tradeProps.getBaseId(), tradeProps.getCount()));
					playerGoods.add(LoggerGoods.outcomeProps(tradeProps.getUserPropId(), tradeProps.getBaseId(), tradeProps.getCount()));
				}else if(tradeProps.getGoodType() == GoodsType.EQUIP){
					targetGoods.add(LoggerGoods.incomeEquip(tradeProps.getBaseId(), tradeProps.getCount()));
					playerGoods.add(LoggerGoods.outcomeEquip(tradeProps.getUserPropId(), tradeProps.getBaseId(), tradeProps.getCount()));
				}
			}
		}
		
		if(!targetProps.isEmpty()){
			for(TradeProps tradeProps : targetProps){
				if(tradeProps.getGoodType() == GoodsType.PROPS) {//目标支出, 则为角色收入
					playerGoods.add(LoggerGoods.incomeProps(tradeProps.getBaseId(), tradeProps.getCount()));
					targetGoods.add(LoggerGoods.outcomeProps(tradeProps.getUserPropId(), tradeProps.getBaseId(), tradeProps.getCount()));
				}else if(tradeProps.getGoodType() == GoodsType.EQUIP){
					playerGoods.add(LoggerGoods.incomeEquip(tradeProps.getBaseId(), tradeProps.getCount()));
					targetGoods.add(LoggerGoods.outcomeEquip(tradeProps.getUserPropId(), tradeProps.getBaseId(), tradeProps.getCount()));
				}
			}
		}
		
		LoggerGoods[] playerGoodsArray = playerGoods.toArray(new LoggerGoods[playerGoods.size()]);
		LoggerGoods[] targetGoodsArray = targetGoods.toArray(new LoggerGoods[targetGoods.size()]);
		GoodsLogger.goodsLogger(player, Source.PLAYER_TRADE, playerGoodsArray);
		GoodsLogger.goodsLogger(target, Source.PLAYER_TRADE, targetGoodsArray);
		TradeLogger.tradeLog(player, target, Source.PLAYER_TRADE, playerTrade, targetTrade, playerGoodsArray);
		
		/** end 交易日志*/
		cancleTrade(playerId, targetId, TradeReason.COMPLETE_TRADE);
		cancleTrade(targetId, playerId, TradeReason.COMPLETE_TRADE);
		
		dbService.submitUpdate2Queue(player, target);
		List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId(), targetDomain.getUnitId()); 
		UserPushHelper.pushAttribute2AreaMember(playerId, userIds, unitIds, AttributeKeys.GOLDEN, AttributeKeys.SILVER);
		MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, playerEntry);
		MessagePushHelper.pushUserProps2Client(targetId, DEFAULT_BACKPACK, false, targetEntry);
		MessagePushHelper.pushGoodsCountChange2Client(playerId, getChangeVOs(playerProps));
		MessagePushHelper.pushGoodsCountChange2Client(targetId, getChangeVOs(targetProps));  
		achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_TRADE);                          // 第一次交易
		return SUCCESS;
	}
	
	/**
	 * 改变后背包信息对象
	 * @param playerId
	 * @param tradeProps
	 * @return
	 */
	private ResultObject<List<BackpackEntry>> getBackpackEntries(long playerId, Collection<TradeProps> tradeProps) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		Player player = userDomain.getPlayer();
		if (player == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		List<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>(); 
		Collection<UserProps> userPropsList = new ArrayList<UserProps>();
		Collection<UserEquip> userEquipList = new ArrayList<UserEquip>();
		
		ChainLock lock = LockUtils.getLock(player, userDomain.getPackLock());
		try {
			lock.lock();
			int playerMaxBackSize = player.getMaxBackSize();
			int playerBackSize  = propsManager.getBackpackSize(playerId, DEFAULT_BACKPACK);
			if (playerMaxBackSize < playerBackSize + tradeProps.size()) {
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
			for (TradeProps props : tradeProps) {
				long userPropsId = props.getUserPropId();
				
				if (props.getGoodType() == GoodsType.PROPS) {
					UserProps copyProps = new UserProps();
					UserProps userProps = propsManager.getUserProps(userPropsId);
					if (userProps == null) continue;
					copyProps.setPlayerId(playerId);                                                             // 设置物品所有人
					BeanUtil.copyProperties(userProps, copyProps, "id", "playerId", "index", "trading");         // 属性拷贝
					userPropsList.add(copyProps);
				} else if (props.getGoodType() == GoodsType.EQUIP) {
					UserEquip copyEquip = new UserEquip();
					UserEquip userEquip = propsManager.getUserEquip(userPropsId);
					if (userEquip == null) continue;
					copyEquip.setPlayerId(playerId);                                                             // 设置装备所有人
					BeanUtil.copyProperties(userEquip, copyEquip, "id", "playerId", "index", "trading");         // 属性拷贝
					userEquipList.add(copyEquip);
				}
			}
			propsManager.createUserEquipAndUserProps(userPropsList, userEquipList);
			propsManager.put2UserPropsIdsList(playerId, DEFAULT_BACKPACK, userPropsList);
			propsManager.put2UserEquipIdsList(playerId, DEFAULT_BACKPACK, userEquipList);
		} finally {
			lock.unlock();
		}
		
		backpackEntries.addAll(voFactory.getUserPropsEntries(userPropsList));
		backpackEntries.addAll(voFactory.getUserEquipEntries(userEquipList));
		return ResultObject.SUCCESS(backpackEntries);
	}
	
	/**
	 * 交换物品
	 * @param backpackEntries
	 * @param playerId
	 * @param tradeProps
	 */
	private int exchangeTradeProps(List<Long> userIds, Collection<TradeProps> tradeProps) {
		for (TradeProps props : tradeProps) {
			int count  = props.getCount();
			int backpack  = DEFAULT_BACKPACK;
			long userPropsId = props.getUserPropId();
			if (props.getGoodType() == GoodsType.PROPS) {
				UserProps userProps = propsManager.getUserProps(userPropsId);
				if (userProps == null) {
					return ITEM_NOT_FOUND;
				} else if (!userIds.contains(userProps.getPlayerId())) {
					return BELONGS_INVALID;	
				} else if (!userProps.validBackpack(backpack)) {
					return NOT_IN_BACKPACK;
				} else if (userProps.getCount() < count) {
					return INPUT_VALUE_INVALID;
				}
			} 
			if (props.getGoodType() == GoodsType.EQUIP) {
				UserEquip userEquip = propsManager.getUserEquip(userPropsId);
				if (userEquip == null) {
					return EQUIP_NOT_FOUND;
				} else if (!userIds.contains(userEquip.getPlayerId())) {
					return BELONGS_INVALID;
				} else if (!userEquip.validBackpack(backpack)) {
					return NOT_IN_BACKPACK;
				} else if (userEquip.getCount() < count) {
					return INPUT_VALUE_INVALID;
				}
			}
		}
		return SUCCESS;
	}

	/**
	 * 交易物品扣除
	 * @param tradeProps
	 * @return
	 */
	private List<BackpackEntry> decreaseTradeItems(UserDomain userDomain, Collection<TradeProps> tradeProps) {
		List<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
		try {
			lock.lock();
			for (TradeProps props : tradeProps) {
				int count = props.getCount();
				int backpack = DEFAULT_BACKPACK;
				long userPropsId = props.getUserPropId();
				if (props.getGoodType() == GoodsType.PROPS) {
					UserProps userProps = propsManager.getUserProps(userPropsId);
					if (userProps == null) {
						continue;
					}
	
					userProps.setTrading(false);
					userProps.decreaseItemCount(count);
					propsManager.removeUserPropsIfCountNotEnough(userProps.getPlayerId(), backpack, userProps);
					dbService.submitUpdate2Queue(userProps);
					backpackEntries.add(voFactory.getUserPropsEntry(userProps));
				}
				
				if (props.getGoodType() == GoodsType.EQUIP) {
					UserEquip userEquip = propsManager.getUserEquip(userPropsId);
					if (userEquip == null) {
						continue;
					}
	
					userEquip.setCount(0);
					userEquip.setTrading(false);
					userEquip.setDiscardTime(new Date());
					userEquip.setBackpack(SOLD_BACKPACK);
	
					int index = userEquip.getIndex();
					int baseId = userEquip.getBaseId();
					long userEquipId = userEquip.getId();
					long playerId = userEquip.getPlayerId();
					Quality quality = userEquip.getQuality();
					
					dbService.submitUpdate2Queue(userEquip);
					propsManager.changeUserEquipBackpack(playerId, backpack, SOLD_BACKPACK, userEquip);
					backpackEntries.add(BackpackEntry.valueEquipEmpty(userEquipId, baseId, backpack, quality, index, userEquip.isBinding()));
				}
			}
			
		} finally {
			lock.unlock();
		}
		return backpackEntries;
	}
	
	
	/**
	 * 交易物品还原(玩家取消交易时调用)
	 * 
	 * @param propsList       交易物品列表
	 */
	private void resetTradeProps(UserDomain userDomain, List<TradeProps> propsList) {
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
		try {
			lock.lock();
			for (TradeProps tradeProps : propsList) {
				if (tradeProps.getGoodType() == GoodsType.PROPS) {
					long userPropsId = tradeProps.getUserPropId();
					UserProps props = propsManager.getUserProps(userPropsId);
					if (props == null) continue;
					props.setTrading(false);
				} 
				if (tradeProps.getGoodType() == GoodsType.EQUIP) {
					long userPropsId = tradeProps.getUserPropId();
					UserEquip equip = propsManager.getUserEquip(userPropsId);
					if (equip == null) continue;
					equip.setTrading(false);
				}
			} 
		} finally {
			lock.unlock();
		}
	}

	
	/**
	 * 获得变化的道具
	 * 
	 * @param  tradeProps        交易道具
	 * @return {@link GoodsVO[]}
	 */
	private List<GoodsVO> getChangeVOs(Collection<TradeProps> tradeProps) {
		List<GoodsVO> voList = new ArrayList<GoodsVO>();
		for (TradeProps props : tradeProps) {
			voList.add(GoodsVO.valueOf(props.getBaseId(), props.getGoodType(), -props.getCount()));
		}
		return voList;
	}
	
	
//	/**
//	 * 交易日志
//	 * @param player              
//	 * @param playerTrade
//	 * @param targetTrade
//	 */
//	private void tradeLogger(Player player, Player target, UserTrade playerTrade, UserTrade targetTrade) {
//		List<TradeProps> propsList = targetTrade.getPropsList();
//		List<LoggerGoods> logGoods = new ArrayList<LoggerGoods>();
//		for (TradeProps props : propsList) {
//			if (props.getGoodType() == GoodsType.PROPS) {
//				logGoods.add(LoggerGoods.incomeProps(props.getBaseId(), props.getCount()));
//			} 
//			if (props.getGoodType() == GoodsType.EQUIP) {
//				logGoods.add(LoggerGoods.incomeEquip(props.getBaseId(), props.getCount(), targetGolden, targetSiliver));
//			}
//		}
//		
//		propsList = playerTrade.getPropsList();
//		for (TradeProps props : propsList) {
//			int baseId = props.getBaseId();
//			int count = props.getCount();
//			Long userPropId = props.getUserPropId();
//			if (props.getGoodType() == GoodsType.PROPS) {
//				logGoods.add(LoggerGoods.outcomeProps(userPropId, baseId, count, false));
//			} else if (props.getGoodType() == GoodsType.EQUIP) {
//				logGoods.add(LoggerGoods.outcomeEquip(userPropId, baseId, count, false));
//			}
//		}
//	} 
	
	
}
