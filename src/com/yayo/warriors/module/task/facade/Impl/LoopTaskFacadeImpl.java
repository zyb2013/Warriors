package com.yayo.warriors.module.task.facade.Impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.task.constant.TaskConstant.*;
import static com.yayo.warriors.module.task.type.TaskStatus.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.basedb.model.LoopRewardConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.helper.TaskPushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.facade.DungeonFacade;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.helper.LoggerPropsHelper;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.logger.log.GoldLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.npc.facade.NpcFacade;
import com.yayo.warriors.module.npc.model.Npc;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.task.entity.UserLoopTask;
import com.yayo.warriors.module.task.facade.LoopTaskFacade;
import com.yayo.warriors.module.task.manager.LoopTaskManager;
import com.yayo.warriors.module.task.model.QualityResult;
import com.yayo.warriors.module.task.model.RewardVO;
import com.yayo.warriors.module.task.rule.TaskRule;
import com.yayo.warriors.module.task.type.EventType;
import com.yayo.warriors.module.task.type.TaskStatus;
import com.yayo.warriors.module.task.vo.LoopTaskVo;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.vip.manager.VipManager;
import com.yayo.warriors.module.vip.model.VipDomain;
import com.yayo.warriors.module.vip.model.VipFunction;
@Component
public class LoopTaskFacadeImpl implements LoopTaskFacade {
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private DbService dbService;
	@Autowired
	private NpcFacade npcFacade;
	@Autowired
	private VipManager vipManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private DungeonFacade dungeonFacade;
	@Autowired
	private LoopTaskManager loopManager;
	
	
	public LoopTaskVo refreshUserLoopTask(long playerId) {
		return constTaskVO(loopManager.getUserLoopTask(playerId));
	}

	
	private LoopTaskVo constTaskVO(UserLoopTask userTask) {
		LoopTaskVo loopTaskVO = null;
		if(userTask != null) {
			int completes = userTask.getCompletes();
			completes = completes >= TaskRule.MAX_LOOP_COMPLETE_COUNT ? TaskRule.MAX_LOOP_COMPLETE_COUNT : completes + 1;
			Collection<LoopRewardConfig> canRewardConfigs = loopManager.listCanLoopRewardConfig();
			LoopRewardConfig loopReward = loopManager.getLoopRewardConfig(completes);
			loopTaskVO = new LoopTaskVo(userTask, loopReward, canRewardConfigs);
		}
		return loopTaskVO;
	}
	
	
	
	public ResultObject<LoopTaskVo> accept(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}

		UserLoopTask userTask = loopManager.getUserLoopTask(playerId);
		if(userTask == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		}
		
		int taskStatus = userTask.getStatus();
		int playerLevel = userDomain.getBattle().getLevel();
		if(taskStatus == TaskStatus.ACCEPTED) {
			return ResultObject.ERROR(DUPLICATE_ACCEPT_TASK);
		} else if(taskStatus == TaskStatus.COMPLETED) {
			return ResultObject.ERROR(TASK_COMPLETED);
		} else if(taskStatus == TaskStatus.REWARDS) {
			return ResultObject.ERROR(TASK_WAS_REWARDED);
		} else if(playerLevel < TaskRule.MIN_LOOP_TASK_LEVEL) {
			return ResultObject.ERROR(LEVEL_INVALID);
		}
		
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			taskStatus = userTask.getStatus();
			if(taskStatus == TaskStatus.ACCEPTED) {
				return ResultObject.ERROR(DUPLICATE_ACCEPT_TASK);
			} else if(taskStatus == TaskStatus.COMPLETED) {
				return ResultObject.ERROR(TASK_COMPLETED);
			} else if(taskStatus == TaskStatus.REWARDS) {
				return ResultObject.ERROR(TASK_WAS_REWARDED);
			} else if(playerLevel < TaskRule.MIN_LOOP_TASK_LEVEL) {
				return ResultObject.ERROR(LEVEL_INVALID);
			}
			userTask.acceptTask();
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userTask);
		LoopTaskVo constructTaskVO = constTaskVO(userTask);
		return ResultObject.SUCCESS(constructTaskVO);
	}

	
	@SuppressWarnings("unchecked")
	
	public ResultObject<LoopTaskVo> fastComplete(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserLoopTask userTask = loopManager.getUserLoopTask(playerId);
		if(userTask == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		if(player == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		int backpack = BackpackType.DEFAULT_BACKPACK;
		if(userTask.getStatus() == UNACCEPT) {
			return ResultObject.ERROR(TASK_UNACCEPT);
		} else if(userTask.getStatus() == COMPLETED) {
			return ResultObject.ERROR(TASK_COMPLETED);
		} else if(userTask.getStatus() == REWARDS) {
			return ResultObject.ERROR(TASK_WAS_REWARDED);
		}
		
		int baseId = TaskRule.FAST_COMPLETE_LOOP_ITEMID;
		List<UserProps> userPropsList = propsManager.listUserPropByBaseId(playerId, baseId, backpack);
		if(userPropsList == null || userPropsList.isEmpty()) {
			return ResultObject.ERROR(PROPS_NOT_ENOUGH);
		}

		UserProps userPropSwap = null ;
		for(UserProps userProp : userPropsList) {
			if(userProp.getCount() > 0 && !userProp.isTrading()) {
				userPropSwap = userProp;
				break;
			}
		}
		
		if(userPropSwap == null) {
			return ResultObject.ERROR(PROPS_NOT_ENOUGH);
		}
		
		
		ChainLock lock = LockUtils.getLock(player, userTask, player.getPackLock());
		try {
			lock.lock();
			if(userTask.getStatus() == UNACCEPT) {
				return ResultObject.ERROR(TASK_UNACCEPT);
			} else if(userTask.getStatus() == COMPLETED) {
				return ResultObject.ERROR(TASK_COMPLETED);
			} else if(userTask.getStatus() == REWARDS) {
				return ResultObject.ERROR(TASK_WAS_REWARDED);
			}
			
			
			if(!userPropSwap.validBackpack(backpack)) {
				return ResultObject.ERROR(NOT_IN_BACKPACK);
			} else if(userPropSwap.getCount() <= 0) {
				return ResultObject.ERROR(ITEM_NOT_ENOUGH);
			}
			
			userTask.completeLoopTask();
			userPropSwap.decreaseItemCount(1);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userPropSwap );
		} finally {
			lock.unlock();
		}
		
		long userPropsId = userPropSwap.getId();
		dbService.submitUpdate2Queue(player, userPropSwap, userTask);
		LoggerGoods outcomeProps = LoggerGoods.outcomeProps(userPropsId, baseId, 1);
		GoodsLogger.goodsLogger(player, Source.LOOPTASK_FASTCOMPLETE, outcomeProps);
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userPropSwap);
		this.rewardsUserLoopTask(playerId);
		return ResultObject.SUCCESS(constTaskVO(loopManager.getUserLoopTask(playerId)));
	}

	
	
	public ResultObject<Collection<BackpackEntry>> complete(long playerId) {
		UserLoopTask userTask = loopManager.getUserLoopTask(playerId);
		if(userTask == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} 
		
		int status = userTask.getStatus();
		if(status == UNACCEPT) {
			return ResultObject.ERROR(TASK_UNACCEPT);
		} else if(status == COMPLETED) {
			return ResultObject.ERROR(TASK_COMPLETED);
		} else if(status == REWARDS) {
			return ResultObject.ERROR(TASK_WAS_REWARDED);
		}

		if(userTask.getType() == EventType.BUY_EQUIP_COUNT) {
			return this.completeBuyEquipCount(playerId, userTask);
		} else if(userTask.getType() == EventType.BUY_PROPS_COUNT) {
			return this.completeBuyPropsCount(playerId, userTask);
		}
		return ResultObject.ERROR(TYPE_INVALID);
	}
	
	
	@SuppressWarnings("unchecked")
	
	public ResultObject<LoopTaskVo> rewardsUserLoopTask(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		PlayerMotion motion = userDomain.getMotion();
		if(player == null || battle == null || motion == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} else if(battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		PlayerDungeon dungeon = dungeonFacade.getPlayerDungeon(playerId);
		if(dungeon == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserLoopTask userTask = loopManager.getUserLoopTask(playerId);
		if(userTask == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		}
		
		if(userTask.getStatus() == UNACCEPT) {
			return ResultObject.ERROR(TASK_UNACCEPT);
		} else if(userTask.getStatus() == REWARDS) {
			return ResultObject.ERROR(TASK_WAS_REWARDED);
		} else if(userTask.getStatus() == ACCEPTED) {
			return ResultObject.ERROR(TASK_UNCOMPLETE);
		} else if(userTask.getCompletes() >= TaskRule.MAX_LOOP_COMPLETE_COUNT) {
			return ResultObject.ERROR(MAX_COUNT_INVALID);
		}
		
		int nextCompletes = userTask.getCompletes() + 1;
		LoopRewardConfig loopReward = loopManager.getLoopRewardConfig(nextCompletes);
		if(loopReward == null) {
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		int addGas = 0;
		int addExp = 0;
		int addSilver = 0;
		VipDomain vip = vipManager.getVip(battle.getId());
		ChainLock lock = LockUtils.getLock(player, battle, userTask);
		try {
			lock.lock();
			if(userTask.getStatus() == UNACCEPT) {
				return ResultObject.ERROR(TASK_UNACCEPT);
			} else if(userTask.getStatus() == REWARDS) {
				return ResultObject.ERROR(TASK_WAS_REWARDED);
			} else if(userTask.getStatus() == ACCEPTED) {
				return ResultObject.ERROR(TASK_UNCOMPLETE);
			} else if(userTask.getCompletes() >= TaskRule.MAX_LOOP_COMPLETE_COUNT) {
				return ResultObject.ERROR(MAX_COUNT_INVALID);
			}
			
			nextCompletes = userTask.getCompletes() + 1;
			loopReward = loopManager.getLoopRewardConfig(nextCompletes);
			if(loopReward == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}
			
			int playerLevel = userTask.getTaskLevel();
			addGas = loopReward.getGasValue(playerLevel, userTask.getQuality());
			addExp = loopReward.getExpValue(playerLevel, userTask.getQuality());
			addSilver = loopReward.getSilverValue(playerLevel,userTask.getQuality());
			if(vip != null && vip.isVip()) {
				addExp += (addExp * vip.floatValue(VipFunction.DailyTaskExperience));
			}
			
			addGas = player.calcIndulgeProfit(addGas);
			addExp = player.calcIndulgeProfit(addExp);
			addSilver = player.calcIndulgeProfit(addSilver);
			
			userTask.addCompletes(1);
			battle.increaseExp(addExp);
			battle.increaseGas(addGas);
			userTask.setStatus(REWARDS);
			player.increaseSilver(addSilver);
			userTask.updateRefreshable(true);
			dbService.updateEntityIntime(userTask);
			dbService.submitUpdate2Queue(player, battle);
			if(addExp != 0){ 
				ExpLogger.loopTaskExp(userDomain, addExp);
			}
			if(addSilver != 0){
				SilverLogger.inCome(Source.REWARDS_TASK, addSilver, player);
			}
		} finally {
			lock.unlock();
		}
		
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIds, AttributeRule.LOOP_TASK_EXP_ARR);
		UserLoopTask userLoopTask = loopManager.getUserLoopTask(playerId);
		return ResultObject.SUCCESS(constTaskVO(userLoopTask));
	}
	
	
	
	public ResultObject<LoopTaskVo> completeTalkTask(long playerId, int npcId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerMotion motion = userDomain.getMotion();
		if(motion == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}

		Npc npc = npcFacade.getNpc(npcId);
		if(npc == null) {
			return ResultObject.ERROR(TARGET_NOT_FOUND);
		}
		
		if(npc.getMapId() != motion.getMapId()) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		UserLoopTask userTask = loopManager.getUserLoopTask(playerId);
		if(userTask == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} 

		int status = userTask.getStatus();
		if(status == TaskStatus.UNACCEPT) {
			return ResultObject.ERROR(TASK_UNACCEPT);
		} else if(status == TaskStatus.COMPLETED) {
			return ResultObject.ERROR(TASK_COMPLETED);
		} else if(status == TaskStatus.REWARDS) {
			return ResultObject.ERROR(TASK_WAS_REWARDED);
		}
		
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			status = userTask.getStatus();
			if(status == TaskStatus.UNACCEPT) {
				return ResultObject.ERROR(TASK_UNACCEPT);
			} else if(status == TaskStatus.COMPLETED) {
				return ResultObject.ERROR(TASK_COMPLETED);
			} else if(status == TaskStatus.REWARDS) {
				return ResultObject.ERROR(TASK_WAS_REWARDED);
			}
			
			userTask.alterAmount(-1);
			if(!userTask.isComplete() && userTask.completeAllCondition()){
				userTask.completeLoopTask();
			}
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userTask);
		return ResultObject.SUCCESS(constTaskVO(userTask));
	}

	
	public void updateFightLootTask(long playerId, int monsterId) {
		UserLoopTask userTask = loopManager.getUserLoopTask(playerId);
		if(userTask == null) {
			return;
		}
		
		if(userTask.getStatus() != ACCEPTED) {
			return;
		} else if(userTask.getType() != EventType.KILLS) {
			return;
		} else if(!userTask.isKillRightMonster(monsterId)) {
			return;
		}
		
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			if(userTask.getStatus() != ACCEPTED) {
				return;
			} else if(userTask.getType() != EventType.KILLS) {
				return;
			} else if(!userTask.isKillRightMonster(monsterId)) {
				return;
			}
			
			userTask.alterAmount(-1);
			if(!userTask.isComplete() && userTask.completeAllCondition()){
				userTask.completeLoopTask();
			}
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userTask);
		TaskPushHelper.pushUserLoopTask2Client(playerId, userTask);
	}
	
	
	
	public int giveUpTask(long playerId) {
		UserLoopTask userTask = loopManager.getUserLoopTask(playerId);
		if(userTask == null) {
			return TASK_NOT_FOUND;
		}
		
		int status = userTask.getStatus();
		if(status == TaskStatus.UNACCEPT) {
			return TASK_UNACCEPT;
		} else if(status == TaskStatus.REWARDS) {
			return TASK_WAS_REWARDED;
		}
		
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			status = userTask.getStatus();
			if(status == TaskStatus.UNACCEPT) {
				return TASK_UNACCEPT;
			} else if(status == TaskStatus.REWARDS) {
				return TASK_WAS_REWARDED;
			}
			userTask.giveUp();
			dbService.submitUpdate2Queue(userTask);
		} finally {
			lock.unlock();
		}
		
		return SUCCESS;
	}
	
	
	
	public QualityResult<LoopTaskVo> refreshTaskQuality(long playerId, int refreshCount, boolean autoBuyBook) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return QualityResult.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserLoopTask userTask = loopManager.getUserLoopTask(playerId);
		if(userTask == null) {
			return QualityResult.ERROR(TASK_NOT_FOUND);
		}
		
		if(refreshCount == 1) {
			return refreshOneCount(userDomain, userTask, autoBuyBook);
		} else if(refreshCount == 10) {
			return refreshTweentyCount(userDomain, userTask, autoBuyBook);
		} else {
			return QualityResult.ERROR(INPUT_VALUE_INVALID);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private QualityResult<LoopTaskVo> refreshOneCount(UserDomain userDomain, UserLoopTask userTask, boolean autoBuy) {
		int targetQuality = Quality.ORANGE.ordinal();
		if(userTask.getQuality() >= targetQuality) {
			return QualityResult.SUCCESS(constTaskVO(userTask));
		}
		
		int backpack = BackpackType.DEFAULT_BACKPACK;
		int propsId = TaskRule.REFRESH_QUALITY_ITEMID;
		PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
		if(propsConfig == null) {
			return QualityResult.ERROR(ITEM_NOT_FOUND);
		}
		
		long playerId = userDomain.getPlayerId();
		int mallPrice = propsConfig.getMallPrice();
		List<UserProps> propsList = propsManager.listUserPropByBaseId(playerId, propsId, backpack);
		if(!autoBuy && (propsList == null || propsList.isEmpty())) {
			return QualityResult.ERROR(ITEM_NOT_ENOUGH);
		}
		
		int useBooks = 0;				
		int useGolden = 0;				
		int result = SUCCESS;			
		int autoBuyBooksCount = 0;		
		UserProps userProps = null;		
		for (UserProps props : propsList) {
			if(props.getCount() > 0) {
				userProps = props;
				break;
			}
		}
		
		Player player = userDomain.getPlayer();
		ChainLock lock = LockUtils.getLock(player, player.getPackLock(), userTask);
		try {
			lock.lock();
			if(userTask.getQuality() >= targetQuality) {
				return QualityResult.SUCCESS(constTaskVO(userTask));
			} 

			if(userProps == null) {
				if(!autoBuy) {
					return QualityResult.ERROR(PROPS_NOT_ENOUGH);
				} else { 
					if(player.getGolden() < mallPrice) {
						return QualityResult.ERROR(PROPS_NOT_ENOUGH);
					}
					useGolden = mallPrice;
					player.decreaseGolden(mallPrice);
				}
			} else {
				if(userProps.getCount() <= 0) {
					return QualityResult.ERROR(PROPS_NOT_ENOUGH);
				}
				useBooks += 1;
				userProps.decreaseItemCount(1);
			}
			
			userTask.setQuality(loopManager.getRandomLoopTaskQuality());
		} finally {
			lock.unlock();
		}
		
		if(userTask.getQuality() >= targetQuality) {
			BulletinConfig config = NoticePushHelper.getConfig(NoticeID.DAILY_TASK_REFRESH, BulletinConfig.class);
			if (config != null) {
				TaskRule.pushTaskQualityNotice(config, userTask.getQuality(), player);
			}
		}
		
		if(userProps == null) {
			dbService.submitUpdate2Queue(player, userTask);
		} else {
			dbService.submitUpdate2Queue(player, userProps, userTask);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
		}

		if(userProps != null) {
			MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, userProps);
		}
		
		if(useGolden != 0) {
			List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
			UserPushHelper.pushAttribute2AreaMember(playerId, Arrays.asList(playerId), unitIds, AttributeKeys.GOLDEN);
			LoggerGoods loggerGoods = LoggerGoods.outcomePropsAutoBuyGolden(propsId, autoBuyBooksCount, useGolden);
			GoldLogger.outCome(Source.LOOPTASK_REFRESH_QUALITY, useGolden, player, loggerGoods);	
		}
		return QualityResult.valueOf(result, useGolden, useBooks, constTaskVO(userTask));
	}

	
	private QualityResult<LoopTaskVo> refreshTweentyCount(UserDomain userDomain, UserLoopTask userTask, boolean autoBuy) {
		int targetQuality = Quality.ORANGE.ordinal();
		if(userTask.getQuality() >= targetQuality) {
			return QualityResult.SUCCESS(constTaskVO(userTask));
		}
		
		int backpack = BackpackType.DEFAULT_BACKPACK;
		int propsId = TaskRule.REFRESH_QUALITY_ITEMID;
		PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
		if(propsConfig == null) {
			return QualityResult.ERROR(ITEM_NOT_FOUND);
		}
		
		
		long playerId = userDomain.getPlayerId();
		int mallPrice = propsConfig.getMallPrice();
		List<UserProps> propsList = propsManager.listUserPropByBaseId(playerId, propsId, backpack);
		if(!autoBuy && (propsList == null || propsList.isEmpty())) { 
			return QualityResult.ERROR(ITEM_NOT_ENOUGH);
		}
		
		int useBooks = 0;						
		int useGolden = 0;						
		int needCount = 10;						
		int result = SUCCESS;					
		int autoBuyBooksCount = 0;				
		Player player = userDomain.getPlayer();
		Map<UserProps, Integer> userPropsMap = new HashMap<UserProps, Integer>(0);
		ChainLock lock = LockUtils.getLock(player, player.getPackLock());
		try {
			lock.lock();
			if(userTask.getQuality() >= targetQuality) {
				return QualityResult.SUCCESS(constTaskVO(userTask));
			}
			
			for (UserProps userProps : propsList) {
				if(needCount <= 0) {
					break;
				}
				
				int currentCount = userProps.getCount();
				if (currentCount <= 0) {
					continue;
				}

				Integer cacheCount = userPropsMap.get(userProps);
				cacheCount = cacheCount == null ? 0 : cacheCount;
				if (currentCount - cacheCount <= 0) {
					continue;
				}

				int canUseCount = Math.min(currentCount - cacheCount, needCount);
				if (canUseCount <= 0) {
					continue;
				}
				useBooks += canUseCount;
				needCount -= canUseCount;
				userPropsMap.put(userProps, cacheCount + canUseCount);
			}
			
			if(needCount > 0) {
				if(!autoBuy) {
					return QualityResult.ERROR(PROPS_NOT_ENOUGH);
				}
				useGolden = needCount * mallPrice;
				if(player.getGolden() < useGolden) {
					return QualityResult.ERROR(GOLDEN_NOT_ENOUGH);
				}
			}
			
			for (Entry<UserProps, Integer> entry : userPropsMap.entrySet()) {
				UserProps userProps = entry.getKey();
				Integer costCount = entry.getValue();
				if(userProps != null && costCount != null) {
					userProps.decreaseItemCount(costCount);
				}
			}
			player.decreaseGolden(useGolden);
			userTask.setQuality(targetQuality);
		} finally {
			lock.unlock();
		}
	 
		Set<UserProps> userPropsSet = userPropsMap.keySet();
		dbService.submitUpdate2Queue(player, userPropsSet, userTask);
		if(!userPropsSet.isEmpty()) {
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userPropsSet);
			MessagePushHelper.pushUserProps2Client(playerId, DEFAULT_BACKPACK, false, userPropsSet);
		}
		
		if(useGolden != 0) {
			List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
			UserPushHelper.pushAttribute2AreaMember(playerId, Arrays.asList(playerId), unitIds, AttributeKeys.GOLDEN);
			LoggerGoods loggerGoods = LoggerGoods.outcomePropsAutoBuyGolden(propsId, autoBuyBooksCount, useGolden);
			GoldLogger.outCome(Source.LOOPTASK_REFRESH_QUALITY, useGolden, player, loggerGoods);	
		}
		
		BulletinConfig config = NoticePushHelper.getConfig(NoticeID.DAILY_TASK_REFRESH, BulletinConfig.class);
		if (config != null) {
			TaskRule.pushTaskQualityNotice(config, userTask.getQuality(), player);
		}
		return QualityResult.valueOf(result, useGolden, useBooks, constTaskVO(userTask));
	}
	 
	
	private ResultObject<Collection<BackpackEntry>> completeBuyEquipCount(long playerId, UserLoopTask userTask) {
		int amount = userTask.getAmount();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		int equipId = Long.valueOf(userTask.getConditions()).intValue();
		
		List<UserEquip> userEquips = propsManager.listUserEquipByBaseId(playerId, equipId, backpack);
		if(userEquips == null || userEquips.isEmpty() || userEquips.size() < amount) {
			return ResultObject.ERROR(EQUIP_NOT_ENOUGH);
		}
		
		Map<Long, UserEquip> costUserEquips = new HashMap<Long, UserEquip>(0);
		for (UserEquip userEquip : userEquips) {
			if(costUserEquips.size() >= amount) {
				break;
			}
			Long userEquipId = userEquip.getId();
			costUserEquips.put(userEquipId, userEquip);
		}
		
		if(userEquips.size() < amount) {
			return ResultObject.ERROR(EQUIP_NOT_ENOUGH);
		}
		
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			int status = userTask.getStatus();
			if(status == UNACCEPT) {
				return ResultObject.ERROR(TASK_UNACCEPT);
			} else if(status == COMPLETED) {
				return ResultObject.ERROR(TASK_COMPLETED);
			} else if(status == REWARDS) {
				return ResultObject.ERROR(TASK_WAS_REWARDED);
			}
			
			userTask.setAmount(0);
			userTask.setStatus(COMPLETED);
			userTask.checkAndUpdateStatus();
			dbService.submitUpdate2Queue(userTask);
		} finally {
			lock.unlock();
		}
		
		return ResultObject.SUCCESS(processCostUserEquips(playerId, costUserEquips));
	}
	
	
	@SuppressWarnings("unchecked")
	private Collection<BackpackEntry> processCostUserEquips(long playerId, Map<Long, UserEquip> equips) {
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		if(equips == null || equips.isEmpty()) {
			return backpackEntries;
		}
		
		Collection<UserEquip> usreEquips = equips.values();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
		try {
			lock.lock();
			for (UserEquip userEquip : usreEquips) {
				userEquip.setCount(0);
				backpackEntries.add(voFactory.getUserEquipEntry(userEquip));
				userEquip.setBackpack(DROP_BACKPACK);
			}
			propsManager.put2UserEquipIdsList(playerId, DROP_BACKPACK, usreEquips);
			propsManager.removeFromEquipIdsList(playerId, DEFAULT_BACKPACK, usreEquips);
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(usreEquips);
		return backpackEntries;
	}

	
	private ResultObject<Collection<BackpackEntry>> completeBuyPropsCount(long playerId, UserLoopTask userTask) {
		int amount = userTask.getAmount();
		int backpack = DEFAULT_BACKPACK;
		int propsId = Long.valueOf(userTask.getConditions()).intValue();
		List<UserProps> userPropsList = propsManager.listUserPropByBaseId(playerId, propsId, backpack);
		if (userPropsList == null || userPropsList.isEmpty()) {
			return ResultObject.ERROR(EQUIP_NOT_ENOUGH);
		}

		int totalCount = 0;
		Map<Long, Integer> costUserProps = new HashMap<Long, Integer>();
		for (UserProps userProps : userPropsList) {
			if (totalCount >= amount) {
				break;
			}

			long userPropsId = userProps.getId();
			Integer cache = costUserProps.get(userPropsId);
			cache = cache == null ? 0 : cache;

			int count = userProps.getCount() - cache;
			int canCostCount = Math.min(count, amount);
			if (canCostCount <= 0) {
				continue;
			}
			totalCount += canCostCount;
			costUserProps.put(userPropsId, cache + canCostCount);
		}

		if (totalCount < amount) {
			return ResultObject.ERROR(EQUIP_NOT_ENOUGH);
		}

		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			int status = userTask.getStatus();
			if (status == UNACCEPT) {
				return ResultObject.ERROR(TASK_UNACCEPT);
			} else if (status == COMPLETED) {
				return ResultObject.ERROR(TASK_COMPLETED);
			} else if (status == REWARDS) {
				return ResultObject.ERROR(TASK_WAS_REWARDED);
			}

			userTask.setAmount(0);
			userTask.setStatus(COMPLETED);
			userTask.checkAndUpdateStatus();
		} finally {
			lock.unlock();
		}

		dbService.submitUpdate2Queue(userTask);
		List<UserProps> costUserPropsList = propsManager.costUserPropsList(costUserProps);
		propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, costUserPropsList);
		Collection<BackpackEntry> userPropsEntries = voFactory.getUserPropsEntries(costUserPropsList);
		return ResultObject.SUCCESS(userPropsEntries);
	}

	
	
	public ResultObject<LoopTaskVo> getReward(long playerId, int completeTimes) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserLoopTask userTask = loopManager.getUserLoopTask(playerId);
		if(userTask == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		}
		
		LoopRewardConfig rewardConfig = loopManager.getLoopRewardConfig(completeTimes);
		if(rewardConfig == null) {
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		int rewardId = rewardConfig.getId();
		Player player = userDomain.getPlayer();
		if(userTask.canGainThisReward(rewardId)) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int backpack = BackpackType.DEFAULT_BACKPACK;
		PropsStackResult stackResult = calcReward(player, backpack, rewardConfig);
		List<UserProps> newUserProps = stackResult.getNewUserProps();
		Map<Long, Integer> mergeProps = stackResult.getMergeProps();
		int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
		ChainLock playerLock = LockUtils.getLock(player.getPackLock(), userTask);
		try{
			playerLock.lock();
			if(userTask.canGainThisReward(rewardId)) {
				return ResultObject.ERROR(FAILURE);
			}
			
			if(!newUserProps.isEmpty()) {
				if(!player.canAddNew2Backpack(newUserProps.size() + currentBackSize, backpack)) {
					return ResultObject.ERROR(BACKPACK_FULLED);
				}
				newUserProps = propsManager.createUserProps(newUserProps);
				propsManager.put2UserPropsIdsList(playerId, backpack, newUserProps);
			}
			userTask.addRewardInfo(rewardId, true);
			dbService.submitUpdate2Queue(userTask);
		} catch (Exception e) {
			return ResultObject.ERROR(FAILURE);
		} finally {
			playerLock.unlock();
		}
		
		List<LoggerGoods> loggerGoods = new ArrayList<LoggerGoods>(0);
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		if(!newUserProps.isEmpty()) {
			backpackEntries.addAll(newUserProps);
			loggerGoods.addAll(LoggerGoods.incomeProps(newUserProps));
		}
		
		Collection<UserProps> updateUserPropsList = null;
		if(!mergeProps.isEmpty()) {
			updateUserPropsList = propsManager.updateUserPropsList(mergeProps);
			backpackEntries.addAll(updateUserPropsList);
		}
		
		LoggerGoods[] loggerGoodsArray = LoggerPropsHelper.convertLoggerGoods(Orient.INCOME, newUserProps, null, mergeProps, updateUserPropsList);
		if(loggerGoodsArray.length > 0) {
			GoodsLogger.goodsLogger(player, Source.REWARDS_TASK, loggerGoodsArray);
		}
		if(!backpackEntries.isEmpty()) {
			MessagePushHelper.pushUserProps2Client(playerId, backpack, false, backpackEntries);
		}
		return ResultObject.SUCCESS(constTaskVO(userTask));
	}
	
	private PropsStackResult calcReward(Player player, int backpack, LoopRewardConfig rewardConfig) {
		PropsStackResult propsStackResult = PropsStackResult.valueOf();
		if(!player.isGoodsReward()) {
			return propsStackResult;
		}
		
		for (RewardVO rewardVO : rewardConfig.getRewardList()) {
			int itemId = rewardVO.getBaseId();
			int count = rewardVO.getCount();
			boolean binding = rewardVO.isBinding();
			PropsStackResult stack = PropsHelper.calcPropsStack(player.getId(), backpack, itemId, count, binding);
			propsStackResult.getMergeProps().putAll(stack.getMergeProps());
			propsStackResult.getNewUserProps().addAll(stack.getNewUserProps());
		}
		return propsStackResult;
	}
}
