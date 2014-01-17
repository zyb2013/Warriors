package com.yayo.warriors.module.drop.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.drop.constant.LootConstant.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.scheduling.Scheduled;
import com.yayo.warriors.basedb.model.BigMapConfig;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.common.helper.LootPushHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.active.manager.ActiveOperatorManager;
import com.yayo.warriors.module.drop.facade.LootFacade;
import com.yayo.warriors.module.drop.manager.DropManager;
import com.yayo.warriors.module.drop.model.DropRewards;
import com.yayo.warriors.module.drop.model.LootWrapper;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.DropLogger;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.model.MonsterBattle;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.type.Currency;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.GoodsType;

/**
 * 战斗奖励接口
 * 
 * @author Hyint
 */
@Component
public class LootFacadeImpl implements LootFacade {
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private UserManager userManager;
	@Autowired
	private DropManager dropManager;
	@Autowired
	private DbService cachedService;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private ActiveOperatorManager activeOperatorManager;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/** 奖励的上下文. {自增ID, 战斗的奖励信息 }*/
	private ConcurrentHashMap<Long, LootWrapper> REWARD_CONTEXTS = new ConcurrentHashMap<Long, LootWrapper>();

	/**
	 * 定时处理过期的物品消失
	 */
	@Scheduled(name="定时计算超时的奖励", value="0 2/5 * * * ?")
	protected void scheduler2ClearTimeoutReward() {
		if(!REWARD_CONTEXTS.isEmpty()) {
			Collection<LootWrapper> values = REWARD_CONTEXTS.values();
			List<LootWrapper> lootWrappers = new ArrayList<LootWrapper>(values);
			for (LootWrapper lootWrapper : lootWrappers) {
				this.checkWrappper(lootWrapper);
			}
		}
	}
	
	/**
	 * 获得奖励封装对象
	 * 
	 * @param  lootRewardId			奖励封装对象自增ID
	 * @param  timeOutRemove		是否超时就移除
	 * @return {@link LootWrapper}	奖励封装对象
	 */
	protected LootWrapper getLootWrapper(long lootRewardId) {
		return checkWrappper(REWARD_CONTEXTS.get(lootRewardId));
	}
	
	/**
	 * 校验掉落信息
	 * 
	 * @param  wrapper				掉落奖励封装信息
	 * @return {@link LootWrapper}	掉落信息
	 */
	private LootWrapper checkWrappper(LootWrapper wrapper) {
		if(wrapper != null && wrapper.isTimeOut()) {
			removeLootWraper(wrapper);
			return null;
		}
		return wrapper;
	}
	
	/**
	 * 移除缓存封装对象
	 * @param wrapper
	 */
	private LootWrapper removeLootWraper(LootWrapper wrapper) {
		if(wrapper != null) {
			wrapper.leaveScreen();
			REWARD_CONTEXTS.remove(wrapper.getId());
		}
		return wrapper;
	}
	
	/**
	 * 战斗奖励接口
	 * 
	 * @param  userDomain		用户模型对象
	 * @param  playerIds		可以拾取该奖励的角色ID列表
	 * @param  monsterDomain	怪物的移动对象
	 * @param  dropInfo			掉落ID与次数信息
	 */
	
	public void createFightLoot(UserDomain userDomain, Collection<Long> playerIds, MonsterDomain monsterDomain, Map<Integer, Integer> dropInfo) {
		if(monsterDomain == null || userDomain == null || dropInfo == null || dropInfo.isEmpty()) {
			return;
		}

		GameScreen currentScreen = monsterDomain.getCurrentScreen();
		if(currentScreen == null) {
			return;
		}
		
		long playerId = userDomain.getPlayerId();
		GameMap gameMap = currentScreen.getGameMap();
		if(gameMap == null) {
			return;
		}
		
		MonsterBattle monsterBattle = monsterDomain.getMonsterBattle();
		if(monsterBattle == null) {
			return;
		}
		
		MonsterFightConfig monsterFight = monsterBattle.getMonsterFight();
		if(monsterFight == null) {
			return;
		}
		
		int positionX = monsterDomain.getX();
		int positionY = monsterDomain.getY();
		int branching = gameMap.getBranching();
		Set<LootWrapper> fightRewards = new HashSet<LootWrapper>(); 
		for (Entry<Integer, Integer> entry : dropInfo.entrySet()) {
			Integer rewardNo = entry.getKey();
			Integer count = entry.getValue();
			if(rewardNo == null || count == null || count <= 0) {
				continue;
			}
			
			List<DropRewards> dropRewards = dropManager.dropRewards(playerId, rewardNo, count);
			if(dropRewards == null || dropRewards.isEmpty()) {
				continue;
			}
			
			//迭代并且设置到内存中
			for (DropRewards dropReward : dropRewards) {
				LootWrapper wrapper = LootWrapper.valueOf(playerIds);
				wrapper.setBranching(branching);
				wrapper.setMonsterFight(monsterFight);
				wrapper.setNotice(dropReward.isNotice());
				wrapper.setBaseId(dropReward.getBaseId());
				wrapper.setAmount(dropReward.getAmount());
				wrapper.setGoodsType(dropReward.getType());
				wrapper.setEndTime(dropReward.getEndTime());
				wrapper.setBinding(dropReward.isBinding());
				fightRewards.add(wrapper);
				wrapper.changeMap(gameMap, positionX, positionY);
				removeLootWraper(REWARD_CONTEXTS.put(wrapper.getId(), wrapper));
			}
		}
		
		if(monsterDomain.isDropActiveItem()){
			List<Integer> drops = activeOperatorManager.getDropList(); //活动掉落编号
			if(drops != null && !drops.isEmpty()){
				for(int dropNumber : drops){
					List<DropRewards> dropRewards = dropManager.dropRewards(playerId, dropNumber, 1);//与凡平商量过,这里固定掉落一次
					if(dropRewards == null || dropRewards.isEmpty()) {
						continue;
					}
					
					//迭代并且设置到内存中
					for (DropRewards dropReward : dropRewards) {
						LootWrapper wrapper = LootWrapper.valueOf(playerIds);
						wrapper.setBranching(branching);
						wrapper.setMonsterFight(monsterFight);
						wrapper.setNotice(dropReward.isNotice());
						wrapper.setBaseId(dropReward.getBaseId());
						wrapper.setAmount(dropReward.getAmount());
						wrapper.setGoodsType(dropReward.getType());
						wrapper.setEndTime(dropReward.getEndTime());
						wrapper.setBinding(dropReward.isBinding());
						fightRewards.add(wrapper);
						wrapper.changeMap(gameMap, positionX, positionY);
						removeLootWraper(REWARD_CONTEXTS.put(wrapper.getId(), wrapper));
					}
				}
			}
		}
		
		LootPushHelper.pushNewFightReward2Client(userDomain, playerIds, fightRewards);
	}

	/**
	 * 角色进入场景, 只给当前场景中的掉落物品给客户端看
	 * 
	 * @param  userDomain			登陆场景的角色ID
	 * @param  gameMap				角色所在的地图对象
	 */
	
	public void enterScreen(UserDomain userDomain, GameMap gameMap) {
		long playerId = userDomain.getPlayerId();
//		List<GameScreen> viewScreen = gameMap.calcViewScreen(userDomain);
//		if(viewScreen == null || viewScreen.isEmpty()) {
//			logger.debug("角色:[{}] 登陆进入场景, 当前可视场景:[{}] 不存在", playerId, viewScreen);
//			return;
//		}
//		
//		List<Long> spireIds = GameMap.getSpireIds(viewScreen, ElementType.DROP_REWARD);
//		if(spireIds == null || spireIds.isEmpty()) {
//			return;
//		}
		
		Collection<Long> spireIds = gameMap.getAllSpireIdCollection(ElementType.DROP_REWARD);
		Set<LootWrapper> dropWrappers = new HashSet<LootWrapper>(spireIds == null ? 0 : spireIds.size());
		for (Long lootRewardId : spireIds) {
			LootWrapper lootWrapper = this.getLootWrapper(lootRewardId);
			if(lootWrapper != null && !lootWrapper.isPickup()) {
				dropWrappers.add(lootWrapper);
			}
		}
		LootPushHelper.pushEnterScreenReward(playerId, Arrays.asList(playerId), dropWrappers);
	}

	/**
	 * 拾取掉落奖励
	 * 
	 * @param  playerId			角色ID
	 * @param  rewardId			奖励ID
	 * @return {@link Integer}	掉落模块返回值
	 */
	
	public int pickupLootReward(long playerId, long rewardId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if(player == null || battle == null) {
			return PLAYER_NOT_FOUND;
		} else if(battle.isDead()) {
			return PLAYER_DEADED;
		}
		
		LootWrapper lootWrapper = this.getLootWrapper(rewardId);
		if(lootWrapper == null) {
			return REWARD_NOT_FOUND;
		}
		
		GameScreen currentScreen = userDomain.getCurrentScreen();
		GameScreen lootCurrScreen = lootWrapper.getCurrentScreen();
		if(currentScreen == null || lootCurrScreen == null) {
			return POSITION_INVALID;
		} 

		GameMap currentGameMap = currentScreen.getGameMap();
		GameMap lootCurrGameMap = lootCurrScreen.getGameMap();
		if(currentGameMap == null || lootCurrGameMap == null) {
			return POSITION_INVALID;
		} else if(currentGameMap != lootCurrGameMap) {
			return POSITION_INVALID;
		}
		
		int result = updateRewardBusyStatus(lootWrapper, true);
		if(result != SUCCESS) {
			return result;
		}
		if(lootWrapper.isPickup() || lootWrapper.isTimeOut()){
			return REWARD_NOT_FOUND;
		}
		
		switch (lootWrapper.getGoodsType()) {
			case GoodsType.GOLDEN:	result = pickupMoneyLoot(userDomain, lootWrapper);	break;
			case GoodsType.SILVER:	result = pickupMoneyLoot(userDomain, lootWrapper);	break;
			case GoodsType.PROPS:	result = pickupPropsLoot(userDomain, lootWrapper);	break;
			case GoodsType.EQUIP:	result = pickupEquipLoot(userDomain, lootWrapper);	break;
		}
		
		if(result >= SUCCESS) {
//			this.removeLootWraper(lootWrapper);	//解决掉落同屏，不删除等过期
			lootWrapper.setPickup(true);
			Collection<Long> playerIdList = currentGameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
			if(lootWrapper.getSharePlayers() != null) {
				playerIdList.addAll(Arrays.asList(lootWrapper.getSharePlayers()));
			}
			playerIdList.remove(playerId);
			LootPushHelper.pushRemoveReward(playerId, playerIdList, rewardId);
		}
		
		this.updateRewardBusyStatus(lootWrapper, false);
		return result;
	}
	
	/**
	 * 更新奖励的忙碌状态
	 * 
	 * @param  loot		掉落信息
	 * @param  busy		忙碌状态
	 * @return
	 */
	private int updateRewardBusyStatus(LootWrapper loot, boolean busy) {
		if(loot == null) {
			return REWARD_NOT_FOUND;
		}
		
		//同步锁一下
		ChainLock lock = LockUtils.getLock(loot);
		try {
			lock.lock();
			if(busy && loot.isBusy()) {
				return TARGET_BUSY;
			}
			loot.setBusy(busy);
		} finally {
			lock.unlock();
		}
		return SUCCESS;
	}
	
	/**
	 * 拾取货币
	 * 
	 * @param  player			角色对象
	 * @param  loot				掉落信息
	 * @return {@link Integer}	返回值信息
	 */
	private int pickupMoneyLoot(UserDomain userDomain, LootWrapper loot) {
		int type = loot.getGoodsType();
		if(!ArrayUtils.contains(GoodsType.CURRENCYS, type)) {
			return TYPE_INVALID;
		} else if(loot.getAmount() <= 0) {			//数量
			return TYPE_INVALID;
		}
		
		int attribute = -1;							//需要推送的属性类型
		Currency currency = null;					//货币类型
		int amount = loot.getAmount();				//掉落的货币数量
		Player player = userDomain.getPlayer();
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			if(type == GoodsType.GOLDEN) {
				currency = Currency.GOLDEN;
				attribute = AttributeKeys.GOLDEN;
				player.increaseGolden(amount);
			} else if(type == GoodsType.SILVER) {
				currency = Currency.SILVER;
				attribute = AttributeKeys.SILVER;
				player.increaseSilver(amount);
			} else {
				return FAILURE;
			}
		} finally {
			lock.unlock();
		}
		
		cachedService.submitUpdate2Queue(player);
		long playerId = userDomain.getPlayerId();
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, attribute);
		if(logger.isDebugEnabled()) {
			logger.debug("角色:[{}] 拾取货币:[{}] 数量:[{}] ", new Object[] { playerId, currency, amount });
		}
		
		if(currency == Currency.GOLDEN && amount > 0) {
			GoldLogger.inCome(Source.FIGHT_DROP, amount, player);
		} else if(currency == Currency.SILVER && amount > 0) {
			SilverLogger.inCome(Source.FIGHT_DROP, amount, player);
		}
		
		if(amount > 0) {
			DropLogger.pickupMoneyLogger(player, Source.FIGHT_DROP, currency, loot);   // 掉落日志 ---- 超平加 2012.6.26 
		}
		//TODO  这里后面需要处理公告
		return SUCCESS;
	}
	
	
	/**
	 * 拾取装备掉落
	 * 
	 * @param  player			角色对象
	 * @param  loot				掉落信息
	 * @return {@link Integer}	返回值
	 */
	private int pickupEquipLoot(UserDomain userDomain, LootWrapper loot) {
		int equipId = loot.getBaseId();
		int dropCount = loot.getAmount();
		EquipConfig equip = propsManager.getEquipConfig(equipId);
		if(equip == null) {
			return EQUIP_NOT_FOUND;
		} else if(dropCount <= 0) {
			return TYPE_INVALID;
		}
		
		long playerId = userDomain.getId();
		boolean binding = loot.isBinding();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		List<UserEquip> userEquips = EquipHelper.newUserEquips(playerId, backpack, equipId, binding, dropCount);
		if(userEquips == null || userEquips.isEmpty()) {
			return FAILURE;
		}
		
		Player player = userDomain.getPlayer();
		int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			if(!player.canAddNew2Backpack(currentBackSize + dropCount, backpack)) {
				return BACKPACK_FULLED;
			}
			
			userEquips = propsManager.createUserEquip(userEquips);
			propsManager.put2UserEquipIdsList(playerId, backpack, userEquips);
		} catch (Exception e) {
			logger.error("{}", e);
			return FAILURE;
		} finally {
			lock.unlock();
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("角色:[{}] 拾取装备:[{}] ", playerId, equipId);
		}
		
		MessagePushHelper.pushUserProps2Client(playerId, BackpackType.DEFAULT_BACKPACK, false, userEquips);
		
		LoggerGoods loggerGoods = LoggerGoods.incomeEquip(equipId, dropCount);
		DropLogger.pickupPropsLogger(player, Source.FIGHT_DROP, loot, loggerGoods);    // 增加掉落日志
		pushLootNotice(player, loot);                                                  // 发公告 --- 超平2012.8.24
		return SUCCESS; 
	}
	
	/**
	 * 拾取道具奖励
	 * 
	 * @param  player			角色对象
	 * @param  loot				掉落奖励对象
	 * @return
	 */
	private int pickupPropsLoot(UserDomain userDomain, LootWrapper loot) {
		int propsId = loot.getBaseId();			//基础道具ID
		int dropCount = loot.getAmount();		//掉落个数
		Player player = userDomain.getPlayer();	//角色对象
		PropsConfig props = propsManager.getPropsConfig(propsId);
		if(props == null) {
			return ITEM_NOT_FOUND;
		} else if(dropCount <= 0) {
			return TYPE_INVALID;
		}
		
		long playerId = player.getId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		List<UserProps> totalProps = new ArrayList<UserProps>();
		PropsStackResult propsStack = PropsHelper.calcPropsStack(playerId, backpack, propsId, dropCount, loot.isBinding());
		int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
		ChainLock lock = LockUtils.getLock(player.getPackLock());
		try {
			lock.lock();
			int currSize = propsStack.getNewUserProps().size();
			if(!player.canAddNew2Backpack(currentBackSize + currSize, backpack)){
				return BACKPACK_FULLED;
			}
			
			if(!propsStack.getNewUserProps().isEmpty()) {
				List<UserProps> newUserProps = propsStack.getNewUserProps();
				newUserProps = propsManager.createUserProps(newUserProps);
				propsManager.put2UserPropsIdsList(playerId, backpack, newUserProps);
				totalProps.addAll(newUserProps);
			}
			if(!propsStack.getMergeProps().isEmpty()) {
				totalProps.addAll(propsManager.updateUserPropsList(propsStack.getMergeProps()));
			}
		} finally {
			lock.unlock();
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("角色:[{}] 拾取道具:[{}] 数量:[{}] ", new Object[] { playerId, propsId, dropCount });
		}
		
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, totalProps);
		
		LoggerGoods loggerGoods = LoggerGoods.incomeProps(propsId, dropCount);
		DropLogger.pickupPropsLogger(player, Source.FIGHT_DROP, loot, loggerGoods);   // 增加掉落日志
		
		//TODO 保留推送公告
		return SUCCESS;
	}
	
	
	/**
	 * 推送装备掉落公告
	 * 
	 * @param loot
	 */
	private void pushLootNotice(Player player, LootWrapper loot) {
		if (!loot.isNotice()) {
			return;
		}
		
		EquipConfig eConfig  = null;
		BigMapConfig bConfig = null;
		
		if (loot.getGoodsType() == GoodsType.EQUIP) {
			eConfig = NoticePushHelper.getConfig(loot.getBaseId(), EquipConfig.class);
		}
		
		GameMap gameMap = loot.getGameMap();
		if (gameMap != null) {
			bConfig = NoticePushHelper.getConfig(gameMap.getMapId(), BigMapConfig.class);
		}
		
		if (eConfig == null || bConfig == null) {
			return;
		}
		
		BulletinConfig config = NoticePushHelper.getConfig(NoticeID.PICK_UP_DROP_EQUIP, BulletinConfig.class);
		if (config != null) {
			Map<String, Object> params = new HashMap<String, Object>(4);
			params.put(NoticeRule.playerName, player.getName());
			params.put(NoticeRule.map, bConfig.getName());
			params.put(NoticeRule.BOSS, loot.getMonsterFight().getName());
			params.put(NoticeRule.equip, eConfig.getName());
			NoticePushHelper.pushNotice(config.getId(), NoticeType.HONOR, params, config.getPriority());
		}
	}
	
}
