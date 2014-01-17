package com.yayo.warriors.module.task.facade.Impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.task.constant.TaskConstant.*;
import static com.yayo.warriors.module.task.type.TaskStatus.*;
import static com.yayo.warriors.module.vip.model.VipFunction.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.adapter.TaskService;
import com.yayo.warriors.basedb.model.PracticeRewardConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.TaskPushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.facade.DungeonFacade;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.task.entity.UserPracticeTask;
import com.yayo.warriors.module.task.facade.PracticeTaskFacade;
import com.yayo.warriors.module.task.manager.PracticeTaskManager;
import com.yayo.warriors.module.task.model.QualityResult;
import com.yayo.warriors.module.task.model.RewardVO;
import com.yayo.warriors.module.task.rule.TaskRule;
import com.yayo.warriors.module.task.type.EventType;
import com.yayo.warriors.module.task.type.TaskStatus;
import com.yayo.warriors.module.task.vo.LoopTaskVo;
import com.yayo.warriors.module.task.vo.PracticeTaskVO;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.vip.manager.VipManager;
import com.yayo.warriors.module.vip.model.VipDomain;



@Component
public class PracticeTaskFacadeImpl implements PracticeTaskFacade {
	@Autowired
	private DbService dbService;
	@Autowired
	private VipManager vipManager;
	@Autowired
	private TaskService taskService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private DungeonFacade dungeonFacade;
	@Autowired
	private PracticeTaskManager practiceManager;
	
	
	
	public PracticeTaskVO refreshPracticeTask(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return null;
		}
	
		UserPracticeTask practiceTask = practiceManager.getPracticeTask(userDomain);
		if(practiceTask == null) {
			return null;
		}
		
		return constPracticeTaskVO(practiceTask);
	}
	

	private PracticeTaskVO constPracticeTaskVO(UserPracticeTask userTask) {
		if(userTask != null) {
			int completes = userTask.getCompletes();
			completes = completes >= TaskRule.MAX_PRACTICE_COMPLETE_COUNT ? TaskRule.MAX_PRACTICE_COMPLETE_COUNT : completes + 1;
			Collection<PracticeRewardConfig> canRewardConfigs = taskService.listCanPracticeRewardConfig(); 
			PracticeRewardConfig practiceReward = practiceManager.getPracticeRewardConfig(completes);
			return new PracticeTaskVO(userTask, practiceReward, canRewardConfigs);
		}
		return null;
	}

	
	public ResultObject<PracticeTaskVO> accept(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserPracticeTask userTask = practiceManager.getPracticeTask(userDomain);
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
		} else if(playerLevel < TaskRule.MIN_PRACTICE_LEVEL) {
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
			} else if(playerLevel < TaskRule.MIN_PRACTICE_LEVEL) {
				return ResultObject.ERROR(LEVEL_INVALID);
			}
			userTask.acceptTask();
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userTask);
		return ResultObject.SUCCESS(constPracticeTaskVO(userTask));
	}

	@SuppressWarnings("unchecked")
	
	public ResultObject<PracticeTaskVO> rewardUserPracticeTask(long playerId) {
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
		
		UserPracticeTask userTask = practiceManager.getPracticeTask(userDomain);
		if(userTask == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		}
		
		if(userTask.getStatus() == UNACCEPT) {
			return ResultObject.ERROR(TASK_UNACCEPT);
		} else if(userTask.getStatus() == REWARDS) {
			return ResultObject.ERROR(TASK_WAS_REWARDED);
		} else if(userTask.getStatus() == ACCEPTED) {
			return ResultObject.ERROR(TASK_UNCOMPLETE);
		} else if(userTask.getCompletes() >= TaskRule.MAX_PRACTICE_COMPLETE_COUNT) {
			return ResultObject.ERROR(MAX_COUNT_INVALID);
		}
		
		int nextCompletes = userTask.getCompletes() + 1;
		PracticeRewardConfig practiceReward = practiceManager.getPracticeRewardConfig(nextCompletes);
		if(practiceReward == null) {
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		VipDomain vipDomain = vipManager.getVip(playerId);
		
		int addExp = 0;
		int addSilver = 0;
		ChainLock lock = LockUtils.getLock(player, battle, userTask);
		try {
			lock.lock();
			if(userTask.getStatus() == UNACCEPT) {
				return ResultObject.ERROR(TASK_UNACCEPT);
			} else if(userTask.getStatus() == REWARDS) {
				return ResultObject.ERROR(TASK_WAS_REWARDED);
			} else if(userTask.getStatus() == ACCEPTED) {
				return ResultObject.ERROR(TASK_UNCOMPLETE);
			} else if(userTask.getCompletes() >= TaskRule.MAX_PRACTICE_COMPLETE_COUNT) {
				return ResultObject.ERROR(MAX_COUNT_INVALID);
			}
			
			nextCompletes = userTask.getCompletes() + 1;
			practiceReward = practiceManager.getPracticeRewardConfig(nextCompletes);
			if(practiceReward == null) {
				return ResultObject.ERROR(BASEDATA_NOT_FOUND);
			}
			
			int playerLevel = userTask.getTaskLevel();
			addExp = practiceReward.getExpValue(playerLevel, userTask.getQuality());
			addSilver = practiceReward.getSilverValue(playerLevel, userTask.getQuality());
			addExp += vipDomain.calsVipExperience(addExp, PracticeTaskExperience);
			
			userTask.addCompletes(1);
			battle.increaseExp(addExp);
			userTask.setStatus(REWARDS);
			player.increaseSilver(addSilver);
			userTask.updateRefreshable(true);
			dbService.updateEntityIntime(userTask);
			dbService.submitUpdate2Queue(player, battle);
			if(addExp != 0) { 
				ExpLogger.practiceTaskExp(userDomain, addExp);
			}
			if(addSilver != 0){
				SilverLogger.inCome(Source.REWARDS_TASK, addSilver, player);
			}
		} finally {
			lock.unlock();
		}
		
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIds, AttributeRule.TASK_EXP_ARR);
		UserPracticeTask userPracticeTask = practiceManager.getPracticeTask(userDomain);
		return ResultObject.SUCCESS(constPracticeTaskVO(userPracticeTask));
	}
	
	
	public ResultObject<PracticeTaskVO> fastCompletePracticeTask(long playerId, boolean auto) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserPracticeTask userTask = practiceManager.getPracticeTask(userDomain);
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
		
		int baseId = TaskRule.FAST_COMPLETE_PRACTICE_ITEMID;
		PropsConfig props = propsManager.getPropsConfig(baseId);
		if(props == null) {
			return ResultObject.ERROR(PROPS_NOT_ENOUGH);
		}
		
		UserProps userPropSwap = null ;
		List<UserProps> userPropsList = propsManager.listUserPropByBaseId(playerId, baseId, backpack);
		if(userPropsList != null && !userPropsList.isEmpty()) {
			for(UserProps userProp : userPropsList) {
				if(userProp.getCount() > 0 && !userProp.isTrading()) {
					userPropSwap = userProp;
					break;
				}
			}
		}
		
		if(userPropSwap == null && !auto) {
			return ResultObject.ERROR(PROPS_NOT_ENOUGH);
		}
		
		int costGolden = 0;
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
			
			if(userPropSwap != null) {
				if(!userPropSwap.validBackpack(backpack)) {
					return ResultObject.ERROR(NOT_IN_BACKPACK);
				} else if(userPropSwap.getCount() <= 0) {
					return ResultObject.ERROR(ITEM_NOT_ENOUGH);
				}
				userPropSwap.decreaseItemCount(1);
				propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userPropSwap );
			} else {
				if(player.getGolden() < props.getMallPrice()) {
					return ResultObject.ERROR(GOLDEN_NOT_ENOUGH);
				}
				costGolden = props.getMallPrice();
			}
			
			player.decreaseGolden(costGolden);
			userTask.completeLoopTask();
		} finally {
			lock.unlock();
		}
		
		if(costGolden != 0) {
			dbService.updateEntityIntime(player);
			List<Long> playerIdList = Arrays.asList(playerId);
			UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList,  Arrays.asList(userDomain.getUnitId()), AttributeKeys.GOLDEN);
		}
		
		if(userPropSwap != null) {
			long userPropsId = userPropSwap.getId();
			dbService.submitUpdate2Queue(userPropSwap);
			MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userPropSwap);
			LoggerGoods outcomeProps = LoggerGoods.outcomeProps(userPropsId, baseId, 1);
			GoodsLogger.goodsLogger(player, Source.LOOPTASK_FASTCOMPLETE, outcomeProps);
		}
		
		dbService.submitUpdate2Queue(userTask);
		this.rewardUserPracticeTask(playerId);
		return ResultObject.SUCCESS(constPracticeTaskVO(practiceManager.getPracticeTask(userDomain)));
	}

	
	public void updateFightPracticeTask(long playerId, int monsterId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return;
		}
		
		UserPracticeTask userTask = practiceManager.getPracticeTask(userDomain);
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
		TaskPushHelper.pushUserPracticeTask2Client(playerId, userTask);
	}
	
	
	public int giveUpPracticeTask(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		UserPracticeTask practiceTask = practiceManager.getPracticeTask(userDomain);
		if(practiceTask == null) {
			return TASK_NOT_FOUND;
		}
		
		int status = practiceTask.getStatus();
		if(status == TaskStatus.UNACCEPT) {
			return TASK_UNACCEPT;
		} else if(status == TaskStatus.REWARDS) {
			return TASK_WAS_REWARDED;
		}
		
		int quality = practiceManager.getRandomPracticeTaskQuality();
		ChainLock lock = LockUtils.getLock(practiceTask);
		try {
			lock.lock();
			status = practiceTask.getStatus();
			if(status == TaskStatus.UNACCEPT) {
				return TASK_UNACCEPT;
			} else if(status == TaskStatus.REWARDS) {
				return TASK_WAS_REWARDED;
			}
			practiceTask.giveUp(quality);
			dbService.submitUpdate2Queue(practiceTask);
		} finally {
			lock.unlock();
		}
		
		return SUCCESS;
	}
	
	
	public QualityResult<PracticeTaskVO> refreshQuality(long playerId, int targetQuality, int refreshCount, boolean autoBuyBook) {
		return QualityResult.ERROR(FAILURE);
	}	
	 
	/**
	 * 获得奖励信息对象
	 * 
	 * @param  playerId					角色ID
	 * @param  completeTimes			完成次数
	 * @return {@link Integer}			任务模块返回值
	 */
	
	public ResultObject<PracticeTaskVO> getReward(long playerId, int completeTimes) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserPracticeTask userTask = practiceManager.getPracticeTask(userDomain);
		if(userTask == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		}
		
		PracticeRewardConfig rewardConfig = practiceManager.getPracticeRewardConfig(completeTimes);
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
			dbService.updateEntityIntime(userTask);
		} catch (Exception e) {
			return ResultObject.ERROR(FAILURE);
		} finally {
			playerLock.unlock();
		}
		
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		if(!newUserProps.isEmpty()) {
			backpackEntries.addAll(newUserProps);
			GoodsLogger.goodsLogger(player, Source.REWARDS_TASK, LoggerGoods.incomeProps(newUserProps).toArray(new LoggerGoods[newUserProps.size()]) );
		}
		
		if(!mergeProps.isEmpty()) {
			Collection<UserProps> updateUserPropsList = propsManager.updateUserPropsList(mergeProps);
			backpackEntries.addAll(updateUserPropsList);
		}
		
		if(!backpackEntries.isEmpty()) {
			MessagePushHelper.pushUserProps2Client(playerId, backpack, false, backpackEntries);
		}
		return ResultObject.SUCCESS(constPracticeTaskVO(userTask));
	}
	
	
	private PropsStackResult calcReward(Player player, int backpack, PracticeRewardConfig rewardConfig) {
		PropsStackResult propsStackResult = PropsStackResult.valueOf();
		if(!player.isGoodsReward()) {
			return propsStackResult;
		}
		
		for (RewardVO rewardVO : rewardConfig.getRewardList()) {
			int count = rewardVO.getCount();
			int itemId = rewardVO.getBaseId();
			boolean binding = rewardVO.isBinding();
			PropsStackResult stack = PropsHelper.calcPropsStack(player.getId(), backpack, itemId, count, binding);
			propsStackResult.getMergeProps().putAll(stack.getMergeProps());
			propsStackResult.getNewUserProps().addAll(stack.getNewUserProps());
		}
		return propsStackResult;
	}
}
