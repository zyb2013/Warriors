package com.yayo.warriors.module.task.facade.Impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.task.constant.TaskConstant.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.MapTaskConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.TaskPushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.helper.LoggerPropsHelper;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Orient;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.npc.facade.NpcFacade;
import com.yayo.warriors.module.npc.model.Npc;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.task.entity.TaskComplete;
import com.yayo.warriors.module.task.entity.UserMapTask;
import com.yayo.warriors.module.task.facade.MapTaskFacade;
import com.yayo.warriors.module.task.manager.MapTaskManager;
import com.yayo.warriors.module.task.manager.TaskManager;
import com.yayo.warriors.module.task.model.FightCollectInfo;
import com.yayo.warriors.module.task.model.MapTaskResult;
import com.yayo.warriors.module.task.model.MapTaskRewardResult;
import com.yayo.warriors.module.task.model.RewardVO;
import com.yayo.warriors.module.task.model.TaskCondition;
import com.yayo.warriors.module.task.model.TaskEvent;
import com.yayo.warriors.module.task.model.TaskRewardVO;
import com.yayo.warriors.module.task.type.EventStatus;
import com.yayo.warriors.module.task.type.EventType;
import com.yayo.warriors.module.task.type.TaskStatus;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.type.GoodsType;

@Component
public class MapTaskFacadeImpl implements MapTaskFacade {
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private DbService dbService;
	@Autowired
	private NpcFacade npcFacade;
	@Autowired
	private PetManager petManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private TaskManager taskManager;
	@Autowired
	private MapTaskManager mapTaskManager;
	
	@Autowired
	private PropsManager propsManager;
	
	 
	
	
	public UserMapTask getUserMapTask(long userTaskId) {
		return mapTaskManager.getUserMapTask(userTaskId);
	}

	
	public ResultObject<UserMapTask> completeTalkTask(long playerId, int npcId, long userTaskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerMotion motion = userDomain.getMotion();
		if(player == null || motion == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Npc npc = npcFacade.getNpc(npcId);
		if(npc == null) {
			return ResultObject.ERROR(TARGET_NOT_FOUND);
		} else if(npc.getMapId() != motion.getMapId()) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		int baseNpcId = npc.getBaseId();
		UserMapTask userTask = mapTaskManager.getUserMapTask(userTaskId);
		if(userTask == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} else if(userTask.getStatus() == TaskStatus.COMPLETED) {
			return ResultObject.ERROR(TASK_COMPLETED);
		} else if(userTask.getStatus() == TaskStatus.UNACCEPT) {
			return ResultObject.ERROR(TASK_UNACCEPT);
		} else if(userTask.getStatus() == TaskStatus.REWARDS) {
			return ResultObject.ERROR(TASK_WAS_REWARDED);
		}
		
		
		boolean updateUserTasks = false;
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			if(userTask.getStatus() == TaskStatus.COMPLETED) {
				return ResultObject.ERROR(TASK_COMPLETED);
			} else if(userTask.getStatus() == TaskStatus.UNACCEPT) {
				return ResultObject.ERROR(TASK_UNACCEPT);
			} else if(userTask.getStatus() == TaskStatus.REWARDS) {
				return ResultObject.ERROR(TASK_WAS_REWARDED);
			}
			
			TaskEvent[] taskEvents = userTask.getTaskEvents();
			if(taskEvents == null || taskEvents.length <= 0) {
				updateUserTasks = true;
			} else {
				for (TaskEvent taskEvent : taskEvents) {
					if(taskEvent == null || taskEvent.isComplete()) {
						continue;
					} 

					int eventType = taskEvent.getType();
					int condition = taskEvent.getCondition();
					if(eventType != EventType.TALK || condition != baseNpcId) {
						continue;
					}
					
					updateUserTasks = true;
					taskEvent.setAmount(Math.max(taskEvent.getAmount() - 1, 0));
					taskEvent.updateTaskState();
				}
			}
			
			if(!updateUserTasks) {
				return ResultObject.ERROR(FAILURE);
			}
			userTask.checkUserTaskStatus();
		} finally {
			lock.unlock();
		}
		
		dbService.updateEntityIntime(userTask);
		return ResultObject.SUCCESS(userTask);
	}

	/**
	 * 列出用户任务, 同时过滤任务状态
	 * 
	 * @param  playerId					角色ID
	 * @param  status					任务状态数组
	 * @return {@link Collection}		用户任务数组
	 */
	
	public Collection<UserMapTask> listFilterUserMapTask(long playerId, int... status) {
		Collection<UserMapTask> userTasks = new HashSet<UserMapTask>();
		List<UserMapTask> userTaskList = mapTaskManager.listUserMapTask(playerId);
		if(userTaskList != null && !userTaskList.isEmpty()) {
			for (UserMapTask userTask : userTaskList) {
				if(!ArrayUtils.contains(status, userTask.getStatus())) {
					userTasks.add(userTask);
				}
			}
		}
		return userTasks;
	}
	
	public ResultObject<MapTaskResult> accept(long playerId, int taskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		MapTaskConfig taskConfig = mapTaskManager.getMapTaskConfig(taskId);
		if(taskConfig == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} else if(!taskConfig.isMatchingLevel(battle.getLevel())) {
			return ResultObject.ERROR(LEVEL_INVALID);
		}
		
		int startNpcId = taskConfig.getAcceptNpc();
		Npc npc = npcFacade.getNpc(startNpcId);
		if(npc == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		PlayerMotion motion = userDomain.getMotion();
		if(motion.getMapId() != npc.getMapId().intValue()) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		TaskComplete taskComplete = taskManager.getTaskComplete(playerId);
		if(taskComplete == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} 

		MapTaskConfig previousTask = mapTaskManager.getPreviousMapTask(taskId);
		if(!taskComplete.isContainTask(previousTask)) {	
			return ResultObject.ERROR(PRIVIOUS_UNCOMPLETED);
		}
		
		int chain = taskConfig.getChain();
		UserMapTask userTask = mapTaskManager.getUserMapTaskByChain(playerId, chain);
		if(userTask == null) { 	
			return acceptNewTask(userDomain, taskComplete, taskConfig);
		} else { 				
			return acceptUpdateTask(userDomain, userTask, taskComplete, taskConfig);
		}
	}
	private ResultObject<MapTaskResult> acceptNewTask(UserDomain userDomain, TaskComplete taskComplete, MapTaskConfig taskConfig) {
		UserMapTask userTask = null;
		int taskId = taskConfig.getId();
		int chain = taskConfig.getChain();
		long playerId = userDomain.getPlayerId();
		Player player = userDomain.getPlayer();
		String taskEvents = taskConfig.getTaskEvents();
		MapTaskResult taskResult = MapTaskResult.valueOf(userDomain);
		MapTaskConfig previousTask = mapTaskManager.getPreviousMapTask(taskId);
		ChainLock lock = LockUtils.getLock(player, taskComplete);
		try {
			lock.lock();
			userTask = mapTaskManager.getUserMapTaskByChain(playerId, chain);
			if(userTask != null) {
				return ResultObject.ERROR(FAILURE);
			}
			
			if(!taskComplete.isContainTask(previousTask)) {
				return ResultObject.ERROR(PRIVIOUS_UNCOMPLETED);
			}
			
			userTask = UserMapTask.acceptNewTask(playerId, chain, taskId, taskEvents);
			mapTaskManager.createUserMapTask(userTask);
			mapTaskManager.removeUserMapTaskCache(playerId);
		} catch (Exception e) {
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		taskResult.setSingleTask(userTask);
		return ResultObject.SUCCESS(taskResult);
	}

	private ResultObject<MapTaskResult> acceptUpdateTask(UserDomain userDomain, UserMapTask userTask, TaskComplete taskComplete, MapTaskConfig taskConfig) {
		int taskId = taskConfig.getId();
		Player player = userDomain.getPlayer();
		String taskEvents = taskConfig.getTaskEvents();
		MapTaskResult taskResult = MapTaskResult.valueOf(userDomain);
		MapTaskConfig previousTask = mapTaskManager.getPreviousMapTask(taskId);
		ChainLock lock = LockUtils.getLock(player, userTask, taskComplete);
		try {
			lock.lock();
			if(userTask.getTaskId() == taskId) {
				if(userTask.getStatus() == TaskStatus.ACCEPTED || userTask.getStatus() == TaskStatus.COMPLETED) {
					return ResultObject.ERROR(DUPLICATE_ACCEPT_TASK);
				} else if(userTask.getStatus() == TaskStatus.REWARDS && taskComplete.isContainTask(taskConfig)) { 
					return ResultObject.ERROR(DUPLICATE_ACCEPT_TASK);
				}
				
			} else {
				if(!taskComplete.isContainTask(previousTask)) {
					return ResultObject.ERROR(PRIVIOUS_UNCOMPLETED);
				} else if(taskComplete.isContainTask(taskConfig)) {
					return ResultObject.ERROR(TASK_WAS_REWARDED);
				}
			}
			
			userTask.setTaskId(taskId);
			userTask.setTaskEvent(taskEvents);
			userTask.setStatus(TaskStatus.ACCEPTED);
		} catch (Exception e) {
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		
		taskResult.setSingleTask(userTask);
		dbService.updateEntityIntime(userTask);
		return ResultObject.SUCCESS(taskResult);
	}
	
	 
	public ResultObject<MapTaskResult> cancel(long playerId, long userTaskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		UserMapTask userTask = mapTaskManager.getUserMapTask(userTaskId);
		if(userTask == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} else if(!userTask.canCancelTask()) {
			return ResultObject.ERROR(CANNOT_CANCEL_TASK);
		} else if(userTask.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		}
		
		int taskId = userTask.getTaskId();
		MapTaskConfig taskConfig = mapTaskManager.getMapTaskConfig(taskId);
		if(taskConfig == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} else if(!taskConfig.isCancel()) {
			return ResultObject.ERROR(TASK_CANCEL_FAILURE);
		}
		
		String taskEvents = taskConfig.getTaskEvents();
		MapTaskResult taskResult = MapTaskResult.valueOf(userDomain);
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			if(!userTask.canCancelTask()) {
				return ResultObject.ERROR(CANNOT_CANCEL_TASK);
			}
			
			userTask.setTaskEvent(taskEvents);
			userTask.setStatus(TaskStatus.UNACCEPT);
			userTask.updateUserTaskEvents();
			taskResult.setSingleTask(userTask);
			dbService.updateEntityIntime(userTask);
		} finally {
			lock.unlock();
		}
		
		List<LoggerGoods> goodsInfos = new ArrayList<LoggerGoods>();
		taskResult.addBackpackEntries(removeTaskCollectItems(playerId, taskConfig, goodsInfos));
		return ResultObject.SUCCESS(taskResult);
	}
	 
	@SuppressWarnings("unchecked")
	private Collection<BackpackEntry> removeTaskCollectItems(long playerId, MapTaskConfig taskConfig, List<LoggerGoods> infoList) {
		int backpack = BackpackType.DEFAULT_BACKPACK;
		List<TaskCondition> taskConditions = taskConfig.getTaskConditions();
		if(taskConditions == null || taskConditions.isEmpty()) {
			return Collections.emptyList();
		}
		
		Map<Long, UserProps> propsMap = new HashMap<Long, UserProps>();
		for (TaskCondition condition : taskConditions) {
			if(condition == null) {
				continue;
			}
			
			int type = condition.getType();
			if(type != EventType.COLLECT) {
				continue;
			}
			
			int amount = condition.getAmount();
			int itemId = condition.getCondition();
			List<UserProps> propsList = propsManager.listUserPropByBaseId(playerId, itemId, backpack);
			if(propsList == null || propsList.isEmpty()) {
				continue;
			}
			
			UserDomain userDomain = userManager.getUserDomain(playerId);
			ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
			try {
				lock.lock();
				for (UserProps userProps : propsList) {
					if(amount <= 0) {
						break;
					}
					int count = userProps.getCount();
					int canAddCount = Math.min(count, amount);
					if(canAddCount <= 0) {
						continue;
					}
					
					amount -= canAddCount;
					Long userPropsId = userProps.getId();
					userProps.decreaseItemCount(canAddCount);
					propsMap.put(userPropsId, userProps);
					propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
					infoList.add(LoggerGoods.outcomeProps(userPropsId, itemId, canAddCount));
				}
			} finally {
				lock.unlock();
			}
		}
		
		Collection<UserProps> updatePropsList = propsMap.values();
		if(!propsMap.isEmpty()) {
			dbService.submitUpdate2Queue(updatePropsList);
		}
		return voFactory.getUserPropsEntries(updatePropsList);
	}
	
	@SuppressWarnings("unchecked")
	
	public ResultObject<MapTaskRewardResult> rewards(long playerId, long userTaskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if(player == null || battle == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} else if(battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		UserMapTask userTask = mapTaskManager.getUserMapTask(userTaskId);
		if(userTask == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} else if(userTask.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userTask.getStatus() == TaskStatus.UNACCEPT) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} else if(userTask.getStatus() == TaskStatus.ACCEPTED) {
			return ResultObject.ERROR(TASK_UNCOMPLETE);
		} else if(userTask.getStatus() == TaskStatus.REWARDS) {
			return ResultObject.ERROR(TASK_WAS_REWARDED);
		}
		
		int taskId = userTask.getTaskId();
		MapTaskConfig taskConfig = mapTaskManager.getMapTaskConfig(taskId);
		if(taskConfig == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		}
		
		TaskComplete taskComplete = taskManager.getTaskComplete(playerId);
		if(taskComplete == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} else if(taskComplete.isContainTask(taskConfig)) {
			return ResultObject.ERROR(TASK_WAS_REWARDED);
		}
		
		int completeNpcId = taskConfig.getCompleteNpc();
		Npc npc = npcFacade.getNpc(completeNpcId);
		if(npc == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		PlayerMotion motion = userDomain.getMotion();
		if(motion.getMapId() != npc.getMapId().intValue()) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		int backpack = BackpackType.DEFAULT_BACKPACK;
		TaskRewardVO taskRewardVO = constructTaskReward(player, battle, taskConfig);
		int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
		ChainLock lock = LockUtils.getLock(player.getPackLock(), player, battle, userTask, taskComplete);
		Collection<UserProps> newPropsList = taskRewardVO.getNewPropsList();
		Collection<UserEquip> newUserEquipList = taskRewardVO.getNewUserEquipList();
		try {
			lock.lock();
			if(userTask.getStatus() == TaskStatus.UNACCEPT) {
				return ResultObject.ERROR(TASK_NOT_FOUND);
			} else if(userTask.getStatus() == TaskStatus.ACCEPTED) {
				return ResultObject.ERROR(TASK_UNCOMPLETE);
			} else if(userTask.getStatus() == TaskStatus.REWARDS) {
				return ResultObject.ERROR(TASK_WAS_REWARDED);
			}
			
			if(!newPropsList.isEmpty() || !newUserEquipList.isEmpty()) {
				int needSize = currentBackSize + newPropsList.size() + newUserEquipList.size();
				if(!player.canAddNew2Backpack(needSize, DEFAULT_BACKPACK)) {
					return ResultObject.ERROR(BACKPACK_FULLED);
				}
				propsManager.createUserEquipAndUserProps(newPropsList, newUserEquipList);
				propsManager.put2UserPropsIdsList(playerId, DEFAULT_BACKPACK, newPropsList);
				propsManager.put2UserEquipIdsList(playerId, DEFAULT_BACKPACK, newUserEquipList);
			}
			
			taskComplete.addMapTaskId(taskId, true);
			userTask.setStatus(TaskStatus.REWARDS);
			int addExp = taskRewardVO.getAddExp();
			battle.increaseExp(addExp);
			player.increaseSilver(taskRewardVO.getAddSilver());
			if(addExp != 0){	
				ExpLogger.mapTaskExp(userDomain, taskConfig, addExp);
			}
			if(taskRewardVO.getAddSilver() != 0){
				SilverLogger.inCome(Source.REWARDS_TASK, taskRewardVO.getAddSilver(), player);
			}
		} catch (Exception e) {
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(player, battle);
		dbService.updateEntityIntime(userTask, taskComplete);
		List<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		Map<Long, Integer> mergePropMap = taskRewardVO.getMergePropMap();
		List<UserProps> mergeList = null;
		if(mergePropMap != null && !mergePropMap.isEmpty()) {
			mergeList = propsManager.updateUserPropsList(mergePropMap);
			backpackEntries.addAll(mergeList);
		}
		
		if(!newPropsList.isEmpty()) {
			backpackEntries.addAll(newPropsList);
		}

		if(!newUserEquipList.isEmpty()) {
			backpackEntries.addAll(newUserEquipList);
		}

		GoodsLogger.goodsLogger(player, Source.REWARDS_TASK, LoggerPropsHelper.convertLoggerGoods(Orient.INCOME, newPropsList, newUserEquipList, mergePropMap, mergeList) );
		
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeRule.TASK_EXP_ARR);
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, backpackEntries);
		return ResultObject.SUCCESS(MapTaskRewardResult.valueOf(userDomain.getBattle().getLevel(), userTask));
	}
	
	private TaskRewardVO constructTaskReward(Player player, PlayerBattle battle, MapTaskConfig taskConfig) {
		TaskRewardVO taskRewardVO = new TaskRewardVO();
		taskRewardVO.increaseExp(player.calcIndulgeProfit(taskConfig.getExp()));
		taskRewardVO.increaseSilver(player.calcIndulgeProfit(taskConfig.getSilver()));
		
		
		List<RewardVO> rewardList = taskConfig.getRewardList();
		if(rewardList != null && !rewardList.isEmpty() && player.isGoodsReward()) {
			for (RewardVO rewardVO : rewardList) {
				int type = rewardVO.getType();
				int count = rewardVO.getCount();
				int baseId = rewardVO.getBaseId();
				boolean banding = rewardVO.isBinding();
				if(count > 0 && type == GoodsType.EQUIP) {
					this.processEquipReward(battle, baseId, count, banding, taskRewardVO);
				} else if(count > 0 && type == GoodsType.PROPS) {
					this.processPropsReward(battle, baseId, count, banding, taskRewardVO);
				}
			}
		}
		return taskRewardVO;
	}
	
	private void processEquipReward(PlayerBattle battle, int equipId, int count, boolean banding, TaskRewardVO taskRewardVO) {
		long playerId = battle.getId();
		int playerJob = battle.getJob().ordinal();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		EquipConfig equipConfig = propsManager.getEquipConfig(equipId);
		if(equipConfig != null && (equipConfig.getJob() == Job.COMMON.ordinal() || equipConfig.getJob() == playerJob)) {
			taskRewardVO.getNewUserEquipList().addAll(EquipHelper.newUserEquips(playerId, backpack, equipId, banding, count));
		}
	}

	private void processPropsReward(PlayerBattle battle, int propsId, int count, boolean banding, TaskRewardVO taskRewardVO) {
		Job playerJob = battle.getJob();
		long playerId = battle.getId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
		if(propsConfig != null && (propsConfig.getJob() == Job.COMMON.ordinal() || propsConfig.getJob() == playerJob.ordinal())) {
			PropsStackResult stackResult = PropsHelper.calcPropsStack(playerId, backpack, propsId, count, banding);
			taskRewardVO.addMergeProps(stackResult.getMergeProps());
			taskRewardVO.addNewPropsList(stackResult.getNewUserProps());
		}
	}

	
	public ResultObject<MapTaskResult> complete(long playerId, long userTaskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		UserMapTask userTask = mapTaskManager.getUserMapTask(userTaskId);
		if(userTask == null || userTask.getStatus() == TaskStatus.UNACCEPT) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} else if(userTask.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(userTask.getStatus() == TaskStatus.REWARDS) {
			return ResultObject.ERROR(TASK_WAS_REWARDED);
		} else if(userTask.getStatus() == TaskStatus.COMPLETED) {
			return ResultObject.ERROR(TASK_COMPLETED);
		}
		
		int taskId = userTask.getTaskId();
		MapTaskConfig taskConfig = mapTaskManager.getMapTaskConfig(taskId);
		if(taskConfig == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		}
	
		Set<Integer> useCompleteTypes = taskConfig.getUseCompleteTypes();
		if(useCompleteTypes == null || useCompleteTypes.isEmpty()) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int completeNpcId = taskConfig.getCompleteNpc();
		Npc npc = npcFacade.getNpc(completeNpcId);
		if(npc == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		PlayerMotion motion = userDomain.getMotion();
		if(motion.getMapId() != npc.getMapId().intValue()) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			if(userTask.getStatus() == TaskStatus.UNACCEPT) {
				return ResultObject.ERROR(TASK_NOT_FOUND);
			} else if(userTask.getPlayerId() != playerId) {
				return ResultObject.ERROR(BELONGS_INVALID);
			} else if(userTask.getStatus() == TaskStatus.REWARDS) {
				return ResultObject.ERROR(TASK_WAS_REWARDED);
			} else if(userTask.getStatus() == TaskStatus.COMPLETED) {
				return ResultObject.ERROR(TASK_COMPLETED);
			} else if(userTask.isBusy()) {
				return ResultObject.ERROR(TARGET_BUSY);
			}
			updateTaskBusyStatus(userTask, true, false);
		} finally {
			lock.unlock();
		}
		
		boolean hasTaskEventUpdate = false;
		MapTaskResult taskResult = MapTaskResult.valueOf(userDomain);
		try {
			TaskEvent[] taskEvents = userTask.getTaskEvents();
			Set<BackpackEntry> equipEntries = new HashSet<BackpackEntry>();
			Set<BackpackEntry> propsEntries = new HashSet<BackpackEntry>();
			hasTaskEventUpdate = taskEvents == null || taskEvents.length <= 0;
			if(taskEvents != null && taskEvents.length > 0) {
				for (TaskEvent taskEvent : taskEvents) {
					if(taskEvent.getStatus() != EventStatus.PROGRESS) {
						continue;
					}
					
					boolean success = false;
					switch (taskEvent.getType()) {
						case EventType.PET_LEVEL		:success = updatePetLevelEventStatus(userDomain, taskEvent);					break;
						case EventType.MESSENGE			:success = updateMessageStatus(userDomain, taskEvent, propsEntries);	break;
						case EventType.COLLECT			:success = updatePropsEventStatus(userDomain, taskEvent, propsEntries);			break;		
						case EventType.KILL_COLLECT		:success = updatePropsEventStatus(userDomain, taskEvent, propsEntries);			break;		
						case EventType.PLAYER_LEVEL		:success = updatePlayerLevelEventStatus(userDomain, taskEvent);			break;		
						case EventType.BUY_PROPS_COUNT	:success = updatePropsEventStatus(userDomain, taskEvent, propsEntries);			break;		
						case EventType.BUY_EQUIP_COUNT	:success = updateBuyEquipEventStatus(userDomain, taskEvent, equipEntries);		break;		
						case EventType.ENTER_CAMP_TASK	:success = updatePlayerCampTask(userDomain, taskEvent);					break;
					}
					
					hasTaskEventUpdate = success ? true : hasTaskEventUpdate;
				}
			}
			
			taskResult.addBackpackEntries(equipEntries);
			taskResult.addBackpackEntries(propsEntries);
		} catch (Exception e) {
		} finally {
			updateTaskBusyStatus(userTask, false, true);
		}
		
		if(hasTaskEventUpdate) {
			lockAndUpdateTaskStatus(userTask);
			dbService.submitUpdate2Queue(userTask);
		}
		taskResult.setSingleTask(userTask);
		return ResultObject.SUCCESS(taskResult);
	}
	
	private boolean updatePlayerCampTask(UserDomain userDomain, TaskEvent taskEvent) {
		Camp camp = userDomain.getPlayer().getCamp();
		if(camp == null || camp == Camp.NONE) {
			return false;
		}
		
		ChainLock lock = LockUtils.getLock(taskEvent);
		try {
			lock.lock();
			if(taskEvent.isComplete()) {
				return false;
			}
			taskEvent.setAmount(0);
			taskEvent.updateTaskState();
		} finally {
			lock.unlock();
		}
		return true;
	}

	private void lockAndUpdateTaskStatus(UserMapTask userTask) {
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			userTask.checkUserTaskStatus();
		} finally {
			lock.unlock();
		}
	}
	private void updateTaskBusyStatus(UserMapTask userTask, boolean busy, boolean isLock) {
		if(!isLock) {
			userTask.updateBusy(busy);
			return;
		}

		ChainLock locker = LockUtils.getLock(userTask);
		try {
			locker.lock();
			userTask.updateBusy(busy);
		} finally {
			locker.unlock();
		}
	}

	private boolean updateBuyEquipEventStatus(UserDomain userDomain, TaskEvent taskEvent, Set<BackpackEntry> equipEntries) {
		int amount = taskEvent.getAmount();
		int equipId = taskEvent.getCondition();
		long playerId = userDomain.getPlayerId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		List<UserEquip> equipList = propsManager.listUserEquipByBaseId(playerId, equipId, backpack);
		if(equipList == null || equipList.isEmpty()) {
			return false;
		}
		
		Map<Long, UserEquip> costEquipMap = new HashMap<Long, UserEquip>(0);
		for (UserEquip userEquip : equipList) {
			if(amount <= 0) {
				break;
			}
			
			amount -= 1;
			costEquipMap.put(userEquip.getId(), userEquip);
		}
		
		if(amount <= 0) {
			ChainLock lock = LockUtils.getLock(taskEvent);
			try {
				lock.lock();
				taskEvent.setAmount(0);
				taskEvent.updateTaskState();
			} finally {
				lock.unlock();
			}

			removeEquipToComplete(playerId, costEquipMap, equipEntries);
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private void removeEquipToComplete(long playerId, Map<Long, UserEquip> costEquipMap, Set<BackpackEntry> equipEntries) {
		if(costEquipMap == null || costEquipMap.isEmpty()) {
			return;
		}
		
		List<UserEquip> userEquips = new ArrayList<UserEquip>();
		List<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
		try {
			lock.lock();
			for (UserEquip userEquip : costEquipMap.values()) {
				int index = userEquip.getIndex();
				int baseId = userEquip.getBaseId();
				long userEquipId = userEquip.getId();
				int backpack = userEquip.getBackpack();
				Quality quality = userEquip.getQuality();
				
				userEquip.setCount(0);
				userEquip.setBackpack(DROP_BACKPACK);
				userEquips.add(userEquip);
				backpackEntries.add(BackpackEntry.valueEquipEmpty(userEquipId, baseId, backpack, quality, index, userEquip.isBinding()));
			}
			propsManager.put2UserEquipIdsList(playerId, DROP_BACKPACK, userEquips);
			propsManager.removeFromEquipIdsList(playerId, DEFAULT_BACKPACK, userEquips );
		} finally {
			lock.unlock();
		}

		equipEntries.addAll(backpackEntries);
		dbService.submitUpdate2Queue(userEquips);
	}

	private boolean updatePlayerLevelEventStatus(UserDomain userDomain, TaskEvent taskEvent) {
		if(userDomain.getBattle().getLevel() >= taskEvent.getCondition()) {
			ChainLock lock = LockUtils.getLock(taskEvent);
			try {
				lock.lock();
				taskEvent.setAmount(0);
				taskEvent.updateTaskState();
			} finally {
				lock.unlock();
			}
			return true;
		}
		return false;
	}

	private boolean updatePetLevelEventStatus(UserDomain userDomain, TaskEvent taskEvent) {
		int totalAmount = 0;
		int amount = taskEvent.getAmount();
		long playerId = userDomain.getPlayerId();
		int needLevel = taskEvent.getCondition();
		List<PetDomain> petDomains = petManager.getPetDomains(playerId);
		if(petDomains != null && !petDomains.isEmpty()) {
			for (PetDomain petDomain : petDomains) {
				PetBattle battle = petDomain.getBattle();
				if(battle == null || battle.getLevel() < needLevel) {
					continue;
				}
				
				totalAmount++;
				if(totalAmount >= amount) {
					break;
				}
			}
		}
		
		if(totalAmount >= amount) {
			ChainLock lock = LockUtils.getLock(taskEvent);
			try {
				lock.lock();
				taskEvent.setAmount(0);
				taskEvent.updateTaskState();
			} finally {
				lock.unlock();
			}
			return true;
		}
		return false;
	}
	
	private boolean updatePropsEventStatus(UserDomain useDomain, TaskEvent taskEvent, Set<BackpackEntry> propsEntries) {
		int amount = taskEvent.getAmount();
		long playerId = useDomain.getPlayerId();
		int propsId = taskEvent.getCondition();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		List<UserProps> propsList = propsManager.listUserPropByBaseId(playerId, propsId, backpack);
		if(propsList == null || propsList.isEmpty()) {
			return false;
		}
		
		Map<Long, Integer> costPropsMap = new HashMap<Long, Integer>(0);
		for (UserProps userProps : propsList) {
			long userPropsId = userProps.getId();
			if(amount <= 0) {
				break;
			}
			
			int count = userProps.getCount();
			int canCostCount = Math.min(amount, count);
			if(canCostCount <= 0) {
				continue;
			}
			
			amount -= canCostCount;
			Integer cacheCount = costPropsMap.get(userPropsId);
			cacheCount = cacheCount == null ? 0 : cacheCount;
			costPropsMap.put(userPropsId, count + cacheCount);
		}
		
		if(amount > 0) {
			return false;
		}
		
		ChainLock lock = LockUtils.getLock(taskEvent);
		try {
			lock.lock();
			taskEvent.setAmount(0);
			taskEvent.updateTaskState();
		} finally {
			lock.unlock();
		}
		
		Collection<UserProps> userPropsList = propsManager.costUserPropsList(costPropsMap);
		propsEntries.addAll(voFactory.getUserPropsEntries(userPropsList));
		return true;
	}
	private boolean updateMessageStatus(UserDomain userDomain, TaskEvent taskEvent, Set<BackpackEntry> propsEntries) {
		int amount = taskEvent.getAmount();
		int propsId = taskEvent.getCondition();
		long playerId = userDomain.getPlayerId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		List<UserProps> propsList = propsManager.listUserPropByBaseId(playerId, propsId, backpack);
		if(propsList == null || propsList.isEmpty()) {
			return false;
		}
		
		Map<Long, Integer> costPropsMap = new HashMap<Long, Integer>(0);
		for (UserProps userProps : propsList) {
			long userPropsId = userProps.getId();
			if(amount <= 0) {
				break;
			}
			
			int count = userProps.getCount();
			int canCostCount = Math.min(amount, count);
			if(canCostCount <= 0) {
				continue;
			}
			
			amount -= count;
			Integer cacheCount = costPropsMap.get(userPropsId);
			cacheCount = cacheCount == null ? 0 : cacheCount;
			costPropsMap.put(userPropsId, count + cacheCount);
		}
		
		if(amount > 0) {
			return false;
		}
		
		ChainLock lock = LockUtils.getLock(taskEvent);
		try {
			lock.lock();
			taskEvent.setAmount(0);
			taskEvent.updateTaskState();
		} finally {
			lock.unlock();
		}

		Collection<UserProps> userPropsList = propsManager.costUserPropsList(costPropsMap);
		propsEntries.addAll(voFactory.getUserPropsEntries(userPropsList));
		return true;
	}
	
	@SuppressWarnings("unchecked")
	
	public void updateEquipStarTask(long playerId, int starLevel) {
		List<UserMapTask> userTasks = mapTaskManager.listUserMapTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return;
		}
		
		int type = EventType.ASCENT_EQUIP_STAR;
		Set<UserMapTask> modifyTaskSet = new HashSet<UserMapTask>();
		for (UserMapTask userTask : userTasks) {
			if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
				continue;
			}
			
			int taskId = userTask.getTaskId();
			MapTaskConfig taskConfig = mapTaskManager.getMapTaskConfig(taskId);
			if(taskConfig == null || !taskConfig.hasEventType(type)) {
				continue;
			}
			
			
			boolean updateTaskStatus = false;
			ChainLock lock = LockUtils.getLock(userTask);
			try {
				lock.lock();
				if(userTask.getStatus() != TaskStatus.ACCEPTED) {
					continue;
				}
				
				TaskEvent[] taskEvents = userTask.getTaskEvents();
				updateTaskStatus = taskEvents == null || taskEvents.length <= 0;
				if(taskEvents != null && taskEvents.length > 0){
					for (TaskEvent taskEvent : taskEvents) {
						if(taskEvent.isComplete()) {
							continue;
						} 
	
						int amount = taskEvent.getAmount();
						int eventType = taskEvent.getType();
						int condition = taskEvent.getCondition();
						if (eventType != type || amount <= 0 || starLevel < condition) {
							continue;
						} 
						
						updateTaskStatus = true;
						taskEvent.setAmount(Math.max(0, amount - 1));
						taskEvent.updateTaskState();
					}
				}
				
				if(updateTaskStatus) {
					modifyTaskSet.add(userTask);
					userTask.updateUserTaskEvents();
					userTask.checkUserTaskStatus();
				}
			} finally {
				lock.unlock();
			}
		}
		
		if(!modifyTaskSet.isEmpty()) {
			dbService.submitUpdate2Queue(modifyTaskSet);
			TaskPushHelper.pushUserMapTask2Client(playerId, modifyTaskSet);
		}
	}

	@SuppressWarnings("unchecked")
	
	public void updateEquipPolishTask(long playerId) {
		List<UserMapTask> userTasks = mapTaskManager.listUserMapTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return;
		}
		
		int type = EventType.POLISH_EQUIP_COUNT;
		Set<UserMapTask> modifyTaskSet = new HashSet<UserMapTask>();
		for (UserMapTask userTask : userTasks) {
			if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
				continue;
			}
			
			int taskId = userTask.getTaskId();
			MapTaskConfig taskConfig = mapTaskManager.getMapTaskConfig(taskId);
			if(taskConfig == null || !taskConfig.hasEventType(type)) {
				continue;
			}
			
			
			boolean updateTaskStatus = false;
			ChainLock lock = LockUtils.getLock(userTask);
			try {
				lock.lock();
				if(userTask.getStatus() != TaskStatus.ACCEPTED) {
					continue;
				}
				
				TaskEvent[] taskEvents = userTask.getTaskEvents();
				updateTaskStatus = taskEvents == null || taskEvents.length <= 0;
				if( taskEvents != null && taskEvents.length > 0) {
					for (TaskEvent taskEvent : taskEvents) {
						if(taskEvent.isComplete()) {
							continue;
						} 
	
						int amount = taskEvent.getAmount();
						int eventType = taskEvent.getType();
						if (eventType != type || amount <= 0) {
							continue;
						} 
						
						updateTaskStatus = true;
						taskEvent.setAmount(Math.max(0, amount - 1));
						taskEvent.updateTaskState();
					}
				}
				
				if(updateTaskStatus) {
					modifyTaskSet.add(userTask);
					userTask.updateUserTaskEvents();
					userTask.checkUserTaskStatus();
				}
			} finally {
				lock.unlock();
			}
			
		}
		if(!modifyTaskSet.isEmpty()) {
			dbService.submitUpdate2Queue(modifyTaskSet);
			TaskPushHelper.pushUserMapTask2Client(playerId, modifyTaskSet);
		}
	}
	
	
	public List<Integer> getTaskCompleteIds(long playerId) {
		List<Integer> idList = new ArrayList<Integer>();
		TaskComplete taskComplete = taskManager.getTaskComplete(playerId);
		if(taskComplete != null) {
			idList.addAll(taskComplete.getMapCompleteIdSet());
		}
		return idList;
	}

	
	public int collectProps(long playerId, int baseId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		List<UserMapTask> userTasks = mapTaskManager.listUserMapTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return TASK_NOT_FOUND;
		} else if(!calcCanCollect(userTasks, baseId)) {
			return FAILURE;
		}
		
		int backpack = BackpackType.DEFAULT_BACKPACK;
		PropsStackResult stackResult = PropsHelper.calcPropsStack(playerId, backpack, baseId, 1, true);
		List<UserProps> newUserProps = stackResult.getNewUserProps();
		Map<Long, Integer> mergeProps = stackResult.getMergeProps();
		if(mergeProps.isEmpty() && newUserProps.isEmpty()) {
			return FAILURE;
		}
		
		if(!newUserProps.isEmpty()) {
			int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
			ChainLock playerLock = LockUtils.getLock(player.getPackLock());
			try {
				playerLock.lock();
				if(!player.canAddNew2Backpack(newUserProps.size() + currentBackSize, DEFAULT_BACKPACK)) {
					return BACKPACK_FULLED;
				}
				
				newUserProps = propsManager.createUserProps(newUserProps);
				propsManager.put2UserPropsIdsList(playerId, DEFAULT_BACKPACK, newUserProps);
			} catch (Exception e) {
				return FAILURE;
			} finally {
				playerLock.unlock();
			}
		}
		
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>();
		if(!newUserProps.isEmpty()) {
			backpackEntries.addAll(newUserProps);
			GoodsLogger.goodsLogger(player, Source.PROPS_COLLECT, LoggerGoods.incomeProps(baseId, 1));
		}
		
		if(!mergeProps.isEmpty()) {
			Collection<UserProps> updateUserPropsList = propsManager.updateUserPropsList(mergeProps);
			backpackEntries.addAll(updateUserPropsList);
		}
		
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, backpackEntries);
		return SUCCESS;
	}

	private boolean calcCanCollect(List<UserMapTask> userTasks, int baseId) {
		if(userTasks == null || userTasks.isEmpty()) {
			return false;
		}
		
		for (UserMapTask userTask : userTasks) {
			if(userTask == null) {
				continue;
			} 
			
			int status = userTask.getStatus();
			if(status == TaskStatus.UNACCEPT || status == TaskStatus.REWARDS) {
				continue;
			}
			
			MapTaskConfig taskConfig = mapTaskManager.getMapTaskConfig(userTask.getTaskId());
			if(taskConfig == null) {
				continue;
			} 
			
			if(!taskConfig.hasEventType(EventType.COLLECT)) {
				continue;
			}
			
			for (TaskEvent taskEvent : userTask.getTaskEvents()) {
				if(taskEvent.getType() != EventType.COLLECT) {
					continue;
				}
				if(taskEvent.getStatus() != EventStatus.PROGRESS) {
					continue;
				}
				if(taskEvent.getAmount() <= 0) {
					continue;
				}
				if(taskEvent.getCondition() == baseId) {
					return true;
				}
			}
		}
		return false;
	}
	
	private int getHasPropsCount(long playerId, int baseId) {
		int totalCount = 0;
		int backpack = BackpackType.DEFAULT_BACKPACK;
		List<UserProps> userPropsList = propsManager.listUserPropByBaseId(playerId, baseId, backpack);
		if(userPropsList != null && !userPropsList.isEmpty()) {
			for (UserProps userProps : userPropsList) {
				totalCount += userProps.getCount();
			}
		}
		
		return totalCount;
	}

	@SuppressWarnings("unchecked")
	
	public void updateFightMonsterTask(long playerId, int monsterId) {
		List<UserMapTask> userTasks = mapTaskManager.listUserMapTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return ;
		}
		
		int type = EventType.KILLS ;
		Set<UserMapTask> modifyTaskSet = new HashSet<UserMapTask>();
		for (UserMapTask userTask : userTasks) {
			if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
				continue;
			}
			
			int taskId = userTask.getTaskId();
			MapTaskConfig taskConfig = mapTaskManager.getMapTaskConfig(taskId);
			if(taskConfig == null || !taskConfig.hasEventType(type)) {
				continue;
			} 

			Set<Integer> fightMonsterIds = taskConfig.getFightMonsterIds();
			if(!fightMonsterIds.contains(monsterId)) {
				continue;
			}
			
			boolean updateTaskStatus = false;
			ChainLock lock = LockUtils.getLock(userTask);
			try {
				lock.lock();
				if(userTask.getStatus() != TaskStatus.ACCEPTED) {
					continue;
				}
				
				TaskEvent[] taskEvents = userTask.getTaskEvents();
				updateTaskStatus = taskEvents == null || taskEvents.length <= 0;
				if(taskEvents != null && taskEvents.length > 0){
					for (TaskEvent taskEvent : taskEvents) {
						if(taskEvent.isComplete()) {
							continue;
						} 
	
						int amount = taskEvent.getAmount();
						int eventType = taskEvent.getType();
						if (eventType != type || amount <= 0 || taskEvent.getCondition() != monsterId) {
							continue;
						} 
						
						updateTaskStatus = true;
						taskEvent.setAmount(Math.max(0, amount - 1));
						taskEvent.updateTaskState();
					}
				}
				
				if(updateTaskStatus) {
					modifyTaskSet.add(userTask);
					userTask.updateUserTaskEvents();
					userTask.checkUserTaskStatus();
				}
			} finally {
				lock.unlock();
			}
		}
		
		if(!modifyTaskSet.isEmpty()) {
			dbService.submitUpdate2Queue(modifyTaskSet);
			TaskPushHelper.pushUserMapTask2Client(playerId, modifyTaskSet);
		}
	}

	
	public void updateFightCollectTask(long playerId, MonsterFightConfig monsterFight) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null || monsterFight == null) {
			return;
		}

		Player player = userDomain.getPlayer();
		if(player == null) {
			return;
		}
		
		List<UserMapTask> userTasks = mapTaskManager.listUserMapTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return;
		}
		
		Map<Integer, Integer> taskDropMap = monsterFight.getTaskDropMap();
		if(taskDropMap == null || taskDropMap.isEmpty()) {
			return;
		}
 
		FightCollectInfo collectInfo = this.getTotalFightCollectCount(playerId, userTasks, taskDropMap);
		Map<Integer, Integer> collectItems = collectInfo.getCollectItems();
		Map<Integer, Integer> totalCollects = collectInfo.getTotalCollectCounts();
		if(totalCollects.isEmpty() || collectItems.isEmpty()) { 
			return;
		}
		
		List<LoggerGoods> loggerGoodsList = new ArrayList<LoggerGoods>();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		Set<UserProps> userPropsSet = new HashSet<UserProps>();
		for (Entry<Integer, Integer> entry : collectItems.entrySet()) {
			int itemId = entry.getKey();
			int itemCount = entry.getValue();
			Integer totalCount = totalCollects.get(itemId);
			if(totalCount == null) {
				continue;
			}
			
			int hasCount = this.getHasPropsCount(playerId, itemId);
			int canAddCount = Math.min(totalCount - hasCount, itemCount);
			if(canAddCount <= 0) {
				continue;
			}
			
			int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
			PropsStackResult stackResult = PropsHelper.calcPropsStack(playerId, backpack, itemId, canAddCount, true);
			List<UserProps> newUserProps = stackResult.getNewUserProps();
			Map<Long, Integer> mergeProps = stackResult.getMergeProps();
			if(!newUserProps.isEmpty()) {
				ChainLock playerLock = LockUtils.getLock(player.getPackLock());
				try {
					playerLock.lock();
					int totalSize = newUserProps.size() + currentBackSize;
					if(!player.canAddNew2Backpack(totalSize, backpack)) {
						continue;
					}
					
					newUserProps = propsManager.createUserProps(newUserProps);
					propsManager.put2UserPropsIdsList(playerId, DEFAULT_BACKPACK, newUserProps);
				} catch (Exception e) {
					continue;
				} finally {
					playerLock.unlock();
				}
			}
			loggerGoodsList.add(LoggerGoods.incomeProps(itemId, canAddCount));
			
			if(!newUserProps.isEmpty()) {
				userPropsSet.addAll(newUserProps);
			}
			if(!mergeProps.isEmpty()) {
				userPropsSet.addAll(propsManager.updateUserPropsList(mergeProps));
			}
		}
		
		if(!userPropsSet.isEmpty()) {
			MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userPropsSet);
			
			GoodsLogger.goodsLogger(player, Source.REWARDS_TASK, loggerGoodsList.toArray(new LoggerGoods[loggerGoodsList.size()]) );
		}
	}
	
	private FightCollectInfo getTotalFightCollectCount(long playerId, Collection<UserMapTask> userTasks, Map<Integer, Integer> taskDropMap) {
		int type = EventType.KILL_COLLECT ;
		FightCollectInfo collectInfo = new FightCollectInfo();
		for (UserMapTask userTask : userTasks) {
			if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
				continue;
			}
			
			MapTaskConfig taskConfig = mapTaskManager.getMapTaskConfig(userTask.getTaskId());
			if(taskConfig == null) {
				continue;
			}
			
			List<TaskCondition> conditions = taskConfig.getTaskConditionByType(type);
			if(conditions == null || conditions.isEmpty()) {
				continue;
			}
			
			TaskEvent[] taskEvents = userTask.getTaskEvents();
			if(taskEvents == null || taskEvents.length <= 0) {
				continue;
			}
			
			for (TaskEvent taskEvent : taskEvents) {
				if(taskEvent.getType() != type || taskEvent.isComplete()) {
					continue;
				}
				
				int count = taskEvent.getAmount();
				int itemId = taskEvent.getCondition();
				Integer collectRate = taskDropMap.get(itemId);
				if(collectRate == null) {
					continue;
				}
				
				collectInfo.addTotalCount(itemId, count);
				int random = Tools.getRandomInteger(AttributeKeys.RATE_BASE);
				if(random < collectRate) {
					collectInfo.addCollectCount(itemId, 1);
				}
			}
		}
		
		return collectInfo;
	}
	
	@SuppressWarnings("unchecked")
	
	public void updateInstanceTask(long playerId, int instanceId) {
		List<UserMapTask> userTasks = mapTaskManager.listUserMapTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return;
		}
		
		int type = EventType.INSTANCE;
		Set<UserMapTask> modifyTaskSet = new HashSet<UserMapTask>();
		for (UserMapTask userTask : userTasks) {
			if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
				continue;
			}
			
			int taskId = userTask.getTaskId();
			MapTaskConfig taskConfig = mapTaskManager.getMapTaskConfig(taskId);
			if(taskConfig == null || !taskConfig.hasEventType(type)) {
				continue;
			}
			
			
			boolean updateTaskStatus = false;
			ChainLock lock = LockUtils.getLock(userTask);
			try {
				lock.lock();
				if(userTask.getStatus() != TaskStatus.ACCEPTED) {
					continue;
				}
				
				TaskEvent[] taskEvents = userTask.getTaskEvents();
				updateTaskStatus = taskEvents == null || taskEvents.length <= 0;
				if (taskEvents != null && taskEvents.length > 0) {
					for (TaskEvent taskEvent : taskEvents) {
						if (taskEvent.isComplete()) {
							continue;
						}

						int amount = taskEvent.getAmount();
						int eventType = taskEvent.getType();
						int condition = taskEvent.getCondition();
						if (eventType != type || amount <= 0 || condition != instanceId) {
							continue;
						}

						updateTaskStatus = true;
						taskEvent.setAmount(Math.max(0, amount - 1));
						taskEvent.updateTaskState();
					}
				}
				
				if(updateTaskStatus) {
					modifyTaskSet.add(userTask);
					userTask.updateUserTaskEvents();
					userTask.checkUserTaskStatus();
				}
			} finally {
				lock.unlock();
			}
			
		}

		if(!modifyTaskSet.isEmpty()) {
			dbService.submitUpdate2Queue(modifyTaskSet);
			TaskPushHelper.pushUserMapTask2Client(playerId, modifyTaskSet);
		}
	}

	@SuppressWarnings("unchecked")
	
	public void updateSelectCampTask(long playerId) {
		List<UserMapTask> userTasks = mapTaskManager.listUserMapTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return;
		}
		
		Set<UserMapTask> modifyTaskSet = new HashSet<UserMapTask>();
		for (UserMapTask userTask : userTasks) {
			if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
				continue;
			}
			
			int taskId = userTask.getTaskId();
			MapTaskConfig taskConfig = mapTaskManager.getMapTaskConfig(taskId);
			if(taskConfig == null || !taskConfig.hasEventType(EventType.ENTER_CAMP_TASK)) {
				continue;
			}
			
			boolean updateTaskStatus = false;
			ChainLock lock = LockUtils.getLock(userTask);
			try {
				lock.lock();
				if(userTask.getStatus() != TaskStatus.ACCEPTED) {
					continue;
				}
				
				TaskEvent[] taskEvents = userTask.getTaskEvents();
				updateTaskStatus = taskEvents == null || taskEvents.length <= 0;
				if(taskEvents != null && taskEvents.length > 0){
					for (TaskEvent taskEvent : taskEvents) {
						if(taskEvent.isComplete()) {
							continue;
						} 
	
						int amount = taskEvent.getAmount();
						int eventType = taskEvent.getType();
						if (eventType != EventType.ENTER_CAMP_TASK || amount <= 0) {
							continue;
						} 
						
						updateTaskStatus = true;
						taskEvent.setAmount(Math.max(0, amount - 1));
						taskEvent.updateTaskState();
					}
				}
				
				if(updateTaskStatus) {
					modifyTaskSet.add(userTask);
					userTask.updateUserTaskEvents();
					userTask.checkUserTaskStatus();
				}
			} finally {
				lock.unlock();
			}
			
		}
		
		if(!modifyTaskSet.isEmpty()) {
			dbService.submitUpdate2Queue(modifyTaskSet);
			TaskPushHelper.pushUserMapTask2Client(playerId, modifyTaskSet);
		}
	}
}
