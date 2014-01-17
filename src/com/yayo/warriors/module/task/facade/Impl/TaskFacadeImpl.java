package com.yayo.warriors.module.task.facade.Impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.task.constant.TaskConstant.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.TaskConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.TaskPushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.dungeon.model.DungeonInfo;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.horse.entity.Horse;
import com.yayo.warriors.module.horse.manager.HorseManager;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.log.TaskLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.meridian.entity.Meridian;
import com.yayo.warriors.module.meridian.manager.MeridianManager;
import com.yayo.warriors.module.mortal.entity.UserMortalBody;
import com.yayo.warriors.module.mortal.manager.MortalManager;
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
import com.yayo.warriors.module.props.model.CreateResult;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.task.entity.TaskComplete;
import com.yayo.warriors.module.task.entity.UserTask;
import com.yayo.warriors.module.task.facade.TaskFacade;
import com.yayo.warriors.module.task.manager.TaskManager;
import com.yayo.warriors.module.task.model.FightCollectInfo;
import com.yayo.warriors.module.task.model.QuestObject;
import com.yayo.warriors.module.task.model.RewardVO;
import com.yayo.warriors.module.task.model.TaskCondition;
import com.yayo.warriors.module.task.model.TaskEvent;
import com.yayo.warriors.module.task.model.TaskResult;
import com.yayo.warriors.module.task.model.TaskRewardResult;
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
public class TaskFacadeImpl implements TaskFacade {
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
	private HorseManager horseManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private MortalManager mortalManager;
	@Autowired
	private DungeonManager dungeonManager;
	@Autowired
	private MeridianManager meridianManager;
	@Autowired
	private AllianceManager allianceManager;
	
	
	public UserTask getUserTask(long userTaskId) {
		return taskManager.getUserTask(userTaskId);
	}

	
	public ResultObject<UserTask> completeTalkTask(long playerId, int npcId, long userTaskId) {
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
		UserTask userTask = taskManager.getUserTask(userTaskId);
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
		
		dbService.submitUpdate2Queue(userTask);
		return ResultObject.SUCCESS(userTask);
	}

	
	public Collection<UserTask> listFilterUserTask(long playerId, int... status) {
		Collection<UserTask> userTasks = new HashSet<UserTask>();
		List<UserTask> userTaskList = taskManager.listUserTask(playerId);
		if(userTaskList != null && !userTaskList.isEmpty()) {
			for (UserTask userTask : userTaskList) {
				if(!ArrayUtils.contains(status, userTask.getStatus())) {
					userTasks.add(userTask);
				}
			}
		}
		return userTasks;
	}


	public ResultObject<TaskResult> accept(long playerId, int taskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerMotion motion = userDomain.getMotion();
		if(player == null || motion == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} else if(battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
		if(taskConfig == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} else if(!taskConfig.isMatchingLevel(battle.getLevel())) {
			return ResultObject.ERROR(LEVEL_INVALID);
		} else if(!taskConfig.isCampConfirm(player.getCamp())) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int startNpcId = taskConfig.getAcceptNpc();
		Npc npc = npcFacade.getNpc(startNpcId);
		if(npc == null) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		if(motion.getMapId() != npc.getMapId().intValue()) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		TaskComplete taskComplete = taskManager.getTaskComplete(playerId);
		if(taskComplete == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} 

		List<TaskConfig> previousTask = taskManager.getPreviousTask(taskId);
		if(!taskComplete.isContainTask(previousTask)) {
			return ResultObject.ERROR(PRIVIOUS_UNCOMPLETED);
		}
		
		int chain = taskConfig.getChain();
		UserTask userTask = taskManager.getUserTaskByChain(playerId, chain);
		if(userTask == null) { 
			return acceptNewTask(userDomain, taskComplete, taskConfig);
		} else {
			return acceptUpdateTask(userDomain, userTask, taskComplete, taskConfig);
		}
	}

	private void checkAcceptUserTaskCondition(PlayerBattle battle, UserTask userTask, TaskConfig taskConfig) {
		int taskType = EventType.INSTANCE;
		if(!taskConfig.hasEventType(taskType) || userTask.getStatus() != TaskStatus.ACCEPTED) {
			return;
		}
		
		PlayerDungeon dungeon = dungeonManager.getPlayerDungeon(battle);
		if(dungeon == null) {
			return;
		}
		
		TaskEvent[] taskEvents = userTask.getTaskEvents();
		if(taskEvents == null || taskEvents.length <= 0) {
			return;
		}
		
		boolean updateState = false;
		Map<Integer, DungeonInfo> dungeonRecord = dungeon.getDungeonHisRecord();
		for (TaskEvent taskEvent : taskEvents) {
			if(taskEvent.isComplete()) {
				continue;
			} 

			int amount = taskEvent.getAmount();
			int eventType = taskEvent.getType();
			if (eventType != taskType || amount <= 0) {
				continue;
			}
					
			int taskCondition = taskEvent.getCondition(); 
			if(taskCondition <= 0) {
				if(taskEvent.getAmount() <= calcTotalEnterDungeonTimes(dungeonRecord)) {
					updateState = true;
					taskEvent.setAmount(0);
					taskEvent.updateTaskState();
				}
			} else { 
				DungeonInfo dungeonInfo = dungeonRecord.get(taskCondition);
				if(dungeonInfo != null) {
					updateState = true;
					taskEvent.setAmount(Math.max(0, amount - dungeonInfo.getTimes()));
					taskEvent.updateTaskState();
				}
			}
		}
		
		if(updateState) {
			userTask.updateUserTaskEvents();
			userTask.checkUserTaskStatus();
		}
	}
	

	private boolean checkTaskStatusAfterAccept(UserTask userTask, TaskConfig taskConfig) {
		boolean hasChange = false;
		long playerId = userTask.getPlayerId();
		for (Integer conditionType : taskConfig.getConditionTypes()) {
			switch (conditionType) {
				case EventType.ENCHANGE_EQUIP_TASK: {
					int enchangeCount = getEnchangeCount(playerId);
					hasChange = updateEnchangeEquipTask(userTask, enchangeCount) ? true : hasChange; break;
				}
				case EventType.HORSE_LEVEL_TASK: 	hasChange = checkHorseLevelTask(userTask)    ? true : hasChange; break;
				case EventType.JOIN_ALLIANCE_TASK: 	hasChange = checkJoinAllianceTask(userTask)  ? true : hasChange; break;
				case EventType.RUSH_MERIDIAN_TASK: 	hasChange = checkRushMeridianTask(userTask)  ? true : hasChange; break;
				case EventType.MORTAL_LEVELUP_TASK: hasChange = checkMortalLevelUpTask(userTask) ? true : hasChange; break;
				case EventType.PET_GROW_LEVEL_TASK: hasChange = checkPetGrowLevelTask(userTask)  ? true : hasChange; break;
				case EventType.PET_SAVVY_LEVEL_TASK:hasChange = checkPetSavvyLevelTask(userTask) ? true : hasChange; break;
			}
		}
		return hasChange;
	}

	private ResultObject<TaskResult> acceptNewTask(UserDomain userDomain, TaskComplete taskComplete, TaskConfig taskConfig) {
		UserTask userTask = null;
		int taskId = taskConfig.getId();
		int chain = taskConfig.getChain();
		long playerId = userDomain.getId();
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		String taskEvents = taskConfig.getTaskEvents();
		TaskResult taskResult = TaskResult.valueOf(userDomain);
		Map<Integer, Integer> questItemMap = taskConfig.getQuestItemMap();
		QuestObject<PropsStackResult> initQuestItems = initQuestItems(playerId, questItemMap);
		PropsStackResult propsStackResult = initQuestItems.getRewardInfos();
		int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
		List<UserProps> newUserProps = propsStackResult.getNewUserProps();
		ChainLock lock = LockUtils.getLock(player.getPackLock(), taskComplete);
		try {
			lock.lock();
			userTask = taskManager.getUserTaskByChain(playerId, chain);
			if(userTask != null) {
				return ResultObject.ERROR(FAILURE);
			}
			
			List<TaskConfig> previousTasks = taskManager.getPreviousTask(taskId);
			if(!taskComplete.isContainTask(previousTasks)) {
				return ResultObject.ERROR(PRIVIOUS_UNCOMPLETED);
			}
			
			int totalSize = newUserProps.size() + currentBackSize;
			if(!player.canAddNew2Backpack(totalSize, DEFAULT_BACKPACK)) {
				return ResultObject.ERROR(BACKPACK_FULLED);
			}
			
			userTask = new UserTask();
			userTask.setChain(chain);
			userTask.setTaskId(taskId);
			userTask.setPlayerId(playerId);
			userTask.setTaskEvent(taskEvents);
			userTask.setStatus(TaskStatus.ACCEPTED);
			this.checkAcceptUserTaskCondition(battle, userTask, taskConfig);
			taskManager.createUserTask(userTask, newUserProps);
			taskManager.removeUserTaskCache(playerId);
			propsManager.put2UserPropsIdsList(playerId, backpack, newUserProps);
		} catch (Exception e) {
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		
		if(this.checkTaskStatusAfterAccept(userTask, taskConfig)) {
			dbService.submitUpdate2Queue(userTask);
		}	
		
		taskResult.setSingleTask(userTask);
		if(!newUserProps.isEmpty()) {
			taskResult.addBackpackEntries(voFactory.getUserPropsEntries(newUserProps));
		}
		
		TaskLogger.taskLogger(player, taskConfig, Source.ACCEPT_TASK, initQuestItems.getGoodsInfos());
		return ResultObject.SUCCESS(taskResult);
	}

	private ResultObject<TaskResult> acceptUpdateTask(UserDomain userDomain, UserTask userTask, TaskComplete taskComplete, TaskConfig taskConfig) {
		int taskId = taskConfig.getId();
		long playerId = userDomain.getId();
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		String taskEvents = taskConfig.getTaskEvents();
		TaskResult taskResult = TaskResult.valueOf(userDomain);
		Map<Integer, Integer> questItemMap = taskConfig.getQuestItemMap();
		QuestObject<PropsStackResult> initQuestItems = initQuestItems(playerId, questItemMap);
		PropsStackResult propsStackResult = initQuestItems.getRewardInfos();
		int currentBackSize = propsManager.getBackpackSize(playerId, backpack);
		ChainLock lock = LockUtils.getLock(player.getPackLock(), userTask, taskComplete);
		List<UserProps> newUserProps = propsStackResult.getNewUserProps();
		try {
			lock.lock();
			if(userTask.getTaskId() == taskId) {
				if(userTask.getStatus() == TaskStatus.ACCEPTED || userTask.getStatus() == TaskStatus.COMPLETED) {
					return ResultObject.ERROR(DUPLICATE_ACCEPT_TASK);
				} else if(userTask.getStatus() == TaskStatus.REWARDS && taskComplete.isContainTask(taskConfig)) {
					return ResultObject.ERROR(DUPLICATE_ACCEPT_TASK);
				}
			} else {
				List<TaskConfig> previousTasks = taskManager.getPreviousTask(taskId);
				if(!taskComplete.isContainTask(previousTasks)) {
					return ResultObject.ERROR(PRIVIOUS_UNCOMPLETED);
				} else if(taskComplete.isContainTask(taskConfig)) {
					return ResultObject.ERROR(TASK_WAS_REWARDED);
				}
			}
			
			if(!newUserProps.isEmpty()) {
				int totalSize = newUserProps.size() + currentBackSize;
				if(!player.canAddNew2Backpack(totalSize, backpack)) {
					return ResultObject.ERROR(BACKPACK_FULLED);
				}
				newUserProps = propsManager.createUserProps(newUserProps);
				propsManager.put2UserPropsIdsList(playerId, backpack, newUserProps);
			}
			
			userTask.setTaskId(taskId);
			userTask.setTaskEvent(taskEvents);
			userTask.setStatus(TaskStatus.ACCEPTED);
			this.checkAcceptUserTaskCondition(battle, userTask, taskConfig);
		} catch (Exception e) {
			return ResultObject.ERROR(FAILURE);
		} finally {
			lock.unlock();
		}
		
		
		this.checkTaskStatusAfterAccept(userTask, taskConfig);
		taskResult.setSingleTask(userTask);
		dbService.submitUpdate2Queue(userTask);
		if(!newUserProps.isEmpty()) {
			taskResult.addBackpackEntries(voFactory.getUserPropsEntries(newUserProps));
		}
		
		TaskLogger.taskLogger(player, taskConfig, Source.ACCEPT_TASK, initQuestItems.getGoodsInfos());
		return ResultObject.SUCCESS(taskResult);
	}
	
	
	private QuestObject<PropsStackResult> initQuestItems(long playerId, Map<Integer, Integer> questItemMap) {
		PropsStackResult propsStackResult = PropsStackResult.valueOf();
		if(questItemMap == null || questItemMap.isEmpty()) {
			return QuestObject.valueOf(propsStackResult);
		}
		
		List<LoggerGoods> rewardInfos = new ArrayList<LoggerGoods>();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		for (Entry<Integer, Integer> entry : questItemMap.entrySet()) {
			Integer baseId = entry.getKey();
			Integer amount = entry.getValue();
			if(amount == null || baseId == null || amount <= 0) {
				continue;
			}
			
			PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
			if(propsConfig == null) {
				continue;
			}
			
			Date expirateDate = propsConfig.getExpirateDate(false);
			UserProps userProps = UserProps.valueOf(playerId, baseId, amount, backpack, expirateDate, true);
			propsStackResult.getNewUserProps().add(userProps);
			rewardInfos.add(LoggerGoods.incomeProps(baseId, amount));
		}
		return QuestObject.valueOf(propsStackResult, rewardInfos);
	}
	

	
	public ResultObject<TaskResult> cancel(long playerId, long userTaskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		UserTask userTask = taskManager.getUserTask(userTaskId);
		if(userTask == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} else if(!userTask.canCancelTask()) {
			return ResultObject.ERROR(CANNOT_CANCEL_TASK);
		} else if(userTask.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		}
		
		int taskId = userTask.getTaskId();
		TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
		if(taskConfig == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} else if(!taskConfig.isCancel()) {
			return ResultObject.ERROR(TASK_CANCEL_FAILURE);
		}
		
		String taskEvents = taskConfig.getTaskEvents();
		TaskResult taskResult = TaskResult.valueOf(userDomain);
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
			dbService.submitUpdate2Queue(userTask);
		} finally {
			lock.unlock();
		}
		
		List<LoggerGoods> goodsInfos = new ArrayList<LoggerGoods>();
		taskResult.addBackpackEntries(removeQuestItems(playerId, taskConfig, goodsInfos));
		taskResult.addBackpackEntries(removeTaskCollectItems(playerId, taskConfig, goodsInfos));
		TaskLogger.taskLogger(player, taskConfig, Source.GIVEUP_TASK, goodsInfos.toArray(new LoggerGoods[goodsInfos.size()]));
		return ResultObject.SUCCESS(taskResult);
	}
	 
	
	@SuppressWarnings("unchecked")
	private Collection<BackpackEntry> removeTaskCollectItems(long playerId, TaskConfig taskConfig, List<LoggerGoods> infoList) {
		int backpack = BackpackType.DEFAULT_BACKPACK;
		List<TaskCondition> taskConditions = taskConfig.getTaskConditions();
		if(taskConditions == null || taskConditions.isEmpty()) {
			return Collections.emptyList();
		}
		
		Map<Long, UserProps> propsMap = new HashMap<Long, UserProps>(0);
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
	private Collection<BackpackEntry> removeQuestItems(long playerId, TaskConfig taskConfig, List<LoggerGoods> infoList) {
		int backpack = BackpackType.DEFAULT_BACKPACK;
		Map<Integer, Integer> questItems = taskConfig.getQuestItemMap();
		if(questItems == null || questItems.isEmpty()) {
			return Collections.emptyList();
		}
		
		Map<Long, UserProps> propsMap = new HashMap<Long, UserProps>();
		for (Entry<Integer, Integer> entry : questItems.entrySet()) {
			int baseId = entry.getKey();
			int amount = entry.getValue();
			List<UserProps> propsList = propsManager.listUserPropByBaseId(playerId, baseId, backpack);
			if(propsList == null || propsList.isEmpty()) {
				continue;
			}
			
			int subCount = amount;
			UserDomain userDomain = userManager.getUserDomain(playerId);
			ChainLock lock = LockUtils.getLock(userDomain.getPackLock());
			try {
				lock.lock();
				for (UserProps userProps : propsList) {
					if(subCount <= 0) {
						break;
					}
					
					long userPropsId = userProps.getId();
					int count = userProps.getCount();
					int canReduce = Math.min(count, subCount);
					if(canReduce <= 0) {
						continue;
					}
					
					subCount -= canReduce;
					userProps.decreaseItemCount(canReduce);
					propsMap.put(userPropsId, userProps);
					propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
					infoList.add(LoggerGoods.outcomeProps(userPropsId, baseId, canReduce));
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
	
	public ResultObject<TaskRewardResult> rewards(long playerId, long userTaskId) {
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
		
		UserTask userTask = taskManager.getUserTask(userTaskId);
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
		TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
		if(taskConfig == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		}
		
		TaskComplete taskComplete = taskManager.getTaskComplete(playerId);
		if(taskComplete == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} else if(taskComplete.isContainTask(taskConfig)) {
			return ResultObject.ERROR(TASK_WAS_REWARDED);
		}
		
		PlayerMotion motion = userDomain.getMotion();
		Npc npc = npcFacade.getNpc(taskConfig.getCompleteNpc());
		if(npc == null) {
			return ResultObject.ERROR(TARGET_NOT_FOUND);
		} else if(npc.getMapId() != motion.getMapId()) {
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
				int totalSize = currentBackSize + newPropsList.size() + newUserEquipList.size();
				if(!player.canAddNew2Backpack(totalSize, DEFAULT_BACKPACK)) {
					return ResultObject.ERROR(BACKPACK_FULLED);
				}
				
				CreateResult<UserProps, UserEquip> cache = propsManager.createUserEquipAndUserProps(newPropsList, newUserEquipList);
				newPropsList = cache.getCollections1();
				newUserEquipList = cache.getCollections2();
				propsManager.put2UserPropsIdsList(playerId, backpack, newPropsList);
				propsManager.put2UserEquipIdsList(playerId, backpack, newUserEquipList);
			}
			
			taskComplete.addTaskId(taskId, true);
			userTask.setStatus(TaskStatus.REWARDS);
			battle.increaseGas(taskRewardVO.getAddGas());
			battle.increaseExp(taskRewardVO.getAddExp());
			player.increaseSilver(taskRewardVO.getAddSilver());
			if(taskRewardVO.getAddExp() != 0) {
				ExpLogger.mainTaskExp(userDomain, taskConfig, taskRewardVO.getAddExp());
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
		if(mergePropMap != null && !mergePropMap.isEmpty()) {
			Collection<UserProps> mergeList = propsManager.updateUserPropsList(mergePropMap);
			backpackEntries.addAll(mergeList);
		}
		
		if(!newPropsList.isEmpty()) {
			backpackEntries.addAll(newPropsList);
		}

		if(!newUserEquipList.isEmpty()) {
			backpackEntries.addAll(newUserEquipList);
		}
		
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		LoggerGoods[] goodsRewardInfo = taskRewardVO.getGoodsRewardInfo();
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeRule.TASK_EXP_ARR);
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, backpackEntries);
		
		//相关日志
		SilverLogger.inCome(Source.REWARDS_TASK, taskRewardVO.getAddSilver(), player);
		TaskLogger.rewardTask(player, taskConfig, taskRewardVO, goodsRewardInfo);
		return ResultObject.SUCCESS(TaskRewardResult.valueOf(userDomain.getBattle().getLevel(), userTask));
	}
	

	private TaskRewardVO constructTaskReward(Player player, PlayerBattle battle, TaskConfig taskConfig) {
		TaskRewardVO taskRewardVO = new TaskRewardVO();
		taskRewardVO.increaseGas(taskConfig.getGas());
		List<RewardVO> rewardList = taskConfig.getRewardList();
		taskRewardVO.increaseExp(player.calcIndulgeProfit(taskConfig.getExp()));
		taskRewardVO.increaseSilver(player.calcIndulgeProfit(taskConfig.getSilver()));
		if(rewardList != null && !rewardList.isEmpty() && player.isGoodsReward()) {
			for (RewardVO rewardVO : rewardList) {
				int type = rewardVO.getType();
				int count = rewardVO.getCount();
				int baseId = rewardVO.getBaseId();
				boolean binding = rewardVO.isBinding();
				int starLevel = rewardVO.getStarLevel();
				if(count > 0 && type == GoodsType.EQUIP) {
					this.processEquipReward(battle, baseId, count, starLevel, binding, taskRewardVO);
				} else if(count > 0 && type == GoodsType.PROPS) {
					this.processPropsReward(battle, baseId, count, binding, taskRewardVO);
				}
			}
		}
		return taskRewardVO;
	}
	
	/**
	 * 处理装备奖励
	 * 
	 * @param battle			角色战斗对象
	 * @param equipId			基础装备ID
	 * @param count				获得的数量	
	 * @param taskRewardVO		任务奖励VO
	 */
	private void processEquipReward(PlayerBattle battle, int equipId, int count, int starLevel, boolean binding, TaskRewardVO taskRewardVO) {
		long playerId = battle.getId();
		int playerJob = battle.getJob().ordinal();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		EquipConfig equipConfig = propsManager.getEquipConfig(equipId);
		if(equipConfig == null) {
			return;
		} 

		if(equipConfig.getJob() == Job.COMMON.ordinal() || equipConfig.getJob() == playerJob) {
			for (int i = 0; i < count; i++) {
				taskRewardVO.addNewEquip(EquipHelper.newUserEquip2Star(playerId, backpack, equipId, binding, starLevel));
			}
		}
	}


	private void processPropsReward(PlayerBattle battle, int propsId, int count, boolean binding, TaskRewardVO taskRewardVO) {
		Job playerJob = battle.getJob();
		long playerId = battle.getId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		PropsConfig propsConfig = propsManager.getPropsConfig(propsId);
		if(propsConfig != null && (propsConfig.getJob() == Job.COMMON.ordinal() || propsConfig.getJob() == playerJob.ordinal())) {
			PropsStackResult stackResult = PropsHelper.calcPropsStack(playerId, backpack, propsId, count, binding);
			taskRewardVO.addMergeProps(stackResult.getMergeProps());
			taskRewardVO.addNewPropsList(stackResult.getNewUserProps());
		}
	}

	
	public ResultObject<TaskResult> complete(long playerId, long userTaskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		UserTask userTask = taskManager.getUserTask(userTaskId);
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
		TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
		if(taskConfig == null) {
			return ResultObject.ERROR(TASK_NOT_FOUND);
		} 
	
		PlayerMotion motion = userDomain.getMotion();
		Npc npc = npcFacade.getNpc(taskConfig.getCompleteNpc());
		if(npc == null) {
			return ResultObject.ERROR(TARGET_NOT_FOUND);
		} else if(npc.getMapId() != motion.getMapId()) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		Set<Integer> useCompleteTypes = taskConfig.getUseCompleteTypes();
		if(useCompleteTypes == null || useCompleteTypes.isEmpty()) {
			return ResultObject.ERROR(FAILURE);
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
		TaskResult taskResult = TaskResult.valueOf(userDomain);
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
						case EventType.MESSENGE			:success = updateMessageStatus(userDomain, taskEvent, propsEntries);			break;
						case EventType.COLLECT			:success = updatePropsEventStatus(userDomain, taskEvent, propsEntries);			break;		
						case EventType.KILL_COLLECT		:success = updatePropsEventStatus(userDomain, taskEvent, propsEntries);			break;		
						case EventType.PLAYER_LEVEL		:success = updatePlayerLevelEventStatus(userDomain, taskEvent);					break;		
						case EventType.BUY_PROPS_COUNT	:success = updatePropsEventStatus(userDomain, taskEvent, propsEntries);			break;		
						case EventType.BUY_EQUIP_COUNT	:success = updateBuyEquipEventStatus(userDomain, taskEvent, equipEntries);		break;		
						case EventType.ENTER_CAMP_TASK	:success = updatePlayerCampTaskEventStatus(userDomain, taskEvent);				break;
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
	
	private boolean updatePlayerCampTaskEventStatus(UserDomain userDomain, TaskEvent taskEvent) {
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

	private void lockAndUpdateTaskStatus(UserTask userTask) {
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			userTask.checkUserTaskStatus();
		} finally {
			lock.unlock();
		}
	}
	
	private void updateTaskBusyStatus(UserTask userTask, boolean busy, boolean isLock) {
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

			removeEquipToCompleteTask(playerId, costEquipMap, equipEntries);
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private void removeEquipToCompleteTask(long playerId, Map<Long, UserEquip> costEquipMap, Set<BackpackEntry> equipEntries) {
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
			propsManager.removeFromEquipIdsList(playerId, DEFAULT_BACKPACK, userEquips);
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
			costPropsMap.put(userPropsId, canCostCount + cacheCount);
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
		List<UserTask> userTasks = taskManager.listUserTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return;
		}
		
		int type = EventType.ASCENT_EQUIP_STAR;
		Set<UserTask> modifyTaskSet = new HashSet<UserTask>();
		for (UserTask userTask : userTasks) {
			if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
				continue;
			}
			
			int taskId = userTask.getTaskId();
			TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
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
			TaskPushHelper.pushUserTask2Client(playerId, modifyTaskSet);
		}
	}

	public void updateEquipPolishTask(long playerId) {
		updateTaskInfo(playerId, EventType.POLISH_EQUIP_COUNT, 1);
	}
	
	
	
	public List<Integer> getTaskCompleteIds(long playerId) {
		List<Integer> idList = new ArrayList<Integer>();
		TaskComplete taskComplete = taskManager.getTaskComplete(playerId);
		if(taskComplete != null) {
			idList.addAll(taskComplete.getCompleteIdSet());
		}
		return idList;
	}

	
	public int collectProps(long playerId, int baseId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		List<UserTask> userTasks = taskManager.listUserTask(playerId);
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
			ChainLock playerLock = LockUtils.getLock(player);
			try {
				playerLock.lock();
				int createSize = newUserProps.size();
				if(!player.canAddNew2Backpack(createSize + currentBackSize, DEFAULT_BACKPACK)) {
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

	private boolean calcCanCollect(List<UserTask> userTasks, int baseId) {
		if(userTasks == null || userTasks.isEmpty()) {
			return false;
		}
		
		for (UserTask userTask : userTasks) {
			if(userTask == null) {
				continue;
			} 
			
			int status = userTask.getStatus();
			if(status == TaskStatus.UNACCEPT || status == TaskStatus.REWARDS) {
				continue;
			}
			
			TaskConfig taskConfig = taskManager.getTaskConfig(userTask.getTaskId());
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
		List<UserTask> userTasks = taskManager.listUserTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return ;
		}
		
		int type = EventType.KILLS ;
		Set<UserTask> modifyTaskSet = new HashSet<UserTask>();
		for (UserTask userTask : userTasks) {
			if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
				continue;
			}
			
			int taskId = userTask.getTaskId();
			TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
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
			TaskPushHelper.pushUserTask2Client(playerId, modifyTaskSet);
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
		
		List<UserTask> userTasks = taskManager.listUserTask(playerId);
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
				ChainLock playerLock = LockUtils.getLock(player);
				try {
					playerLock.lock();
					int totalSize = newUserProps.size() + currentBackSize;
					if(! player.canAddNew2Backpack(totalSize , DEFAULT_BACKPACK)) {
						continue;
					}
					
					newUserProps = propsManager.createUserProps(newUserProps);
					propsManager.put2UserPropsIdsList(playerId, backpack, newUserProps);
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
	
	private FightCollectInfo getTotalFightCollectCount(long playerId, Collection<UserTask> userTasks, Map<Integer, Integer> taskDropMap) {
		int type = EventType.KILL_COLLECT ;
		FightCollectInfo collectInfo = new FightCollectInfo();
		for (UserTask userTask : userTasks) {
			if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
				continue;
			}
			
			TaskConfig taskConfig = taskManager.getTaskConfig(userTask.getTaskId());
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
		PlayerDungeon playerDungeon = dungeonManager.getPlayerDungeon(playerId);
		if(playerDungeon == null) {
			return;
		}
		
		Map<Integer, DungeonInfo> dungeonHisRecord = playerDungeon.getDungeonHisRecord();
		if(dungeonHisRecord == null) {
			return;
		}
		
		List<UserTask> userTasks = taskManager.listUserTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return;
		}
		
		int taskEventType = EventType.INSTANCE;
		Set<UserTask> modifyTaskSet = new HashSet<UserTask>();
		for (UserTask userTask : userTasks) {
			if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
				continue;
			}
			
			int taskId = userTask.getTaskId();
			TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
			if(taskConfig == null || !taskConfig.hasEventType(taskEventType)) {
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
						if (eventType != taskEventType || amount <= 0) {
							continue;
						}
						
						int taskCondition = taskEvent.getCondition();
						if(taskCondition > 0) {
							if(taskCondition != instanceId) { 
								continue;
							}
							
							DungeonInfo dungeonInfo = dungeonHisRecord.get(instanceId);
							if(dungeonInfo == null) {
								break;
							}
							
							if(amount <= dungeonInfo.getTimes()) {
								taskEvent.setAmount(0);
								updateTaskStatus = true;
								taskEvent.updateTaskState();
							}
						} else {
							if(amount <= calcTotalEnterDungeonTimes(dungeonHisRecord)) {
								taskEvent.setAmount(0);
								updateTaskStatus = true;
								taskEvent.updateTaskState();
							}
						}
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
			TaskPushHelper.pushUserTask2Client(playerId, modifyTaskSet);
		}
	}

	private int calcTotalEnterDungeonTimes(Map<Integer, DungeonInfo> dungeonHisRecord) {
		int total = 0;
		if(dungeonHisRecord != null) {
			for (DungeonInfo dungeonInfo : dungeonHisRecord.values()) {
				total += dungeonInfo.getTimes();
			}
		}
		return total;
	}
	public void updateSelectCampTask(long playerId) {
		updateTaskInfo(playerId, EventType.ENTER_CAMP_TASK, 1);
	}

	
	public boolean updateLettoryTask(long playerId, int count) {
		try {
			return updateTaskInfo(playerId, EventType.LETTORY_TASK, count);
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean checkPetGrowLevelTask(UserTask userTask) {
		if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
			return false;
		}
		
		int taskId = userTask.getTaskId();
		int taskEventType = EventType.PET_GROW_LEVEL_TASK;
		TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
		if(taskConfig == null || !taskConfig.hasEventType(taskEventType)) {
			return false;
		} 
		
		
		int maxPetGrowLevel = 0;
		long playerId = userTask.getPlayerId();
		List<PetDomain> petDomains = petManager.getPetDomains(playerId);
		if(petDomains != null && !petDomains.isEmpty()) {
			for (PetDomain petDomain : petDomains) {
				PetBattle battle = petDomain.getBattle();
				maxPetGrowLevel = Math.max(maxPetGrowLevel, battle.getSavvy());
			}
		}
		
		boolean updateTaskStatus = false;
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			if(userTask.getStatus() != TaskStatus.ACCEPTED) {
				return false;
			}
			
			TaskEvent[] taskEvents = userTask.getTaskEvents();
			updateTaskStatus = taskEvents == null || taskEvents.length <= 0;
			if(taskEvents != null && taskEvents.length > 0){
				for (TaskEvent taskEvent : taskEvents) {
					if(taskEvent.isComplete()) {
						continue;
					} 
					
					if(taskEvent.getType() != taskEventType) {
						continue;
					}
					
					int amount = taskEvent.getAmount();
					if(amount <= 0) {
						continue;
					}
					
					int totalAmont = taskEvent.getTotalAmont();
					int finalAmount = Math.max(0, totalAmont - maxPetGrowLevel);
					if(finalAmount == amount) {
						continue;
					}
					
					taskEvent.setAmount(finalAmount);
					updateTaskStatus = true;
					taskEvent.updateTaskState();
				}
			}
			
			if(updateTaskStatus) {
				userTask.updateUserTaskEvents();
				userTask.checkUserTaskStatus();
				return true;
			}
		} finally {
			lock.unlock();
		}
		return false;
	}

	private boolean checkJoinAllianceTask(UserTask userTask) {
		if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
			return false;
		}
		
		int taskId = userTask.getTaskId();
		int taskEventType = EventType.JOIN_ALLIANCE_TASK;
		TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
		if(taskConfig == null || !taskConfig.hasEventType(taskEventType)) {
			return false;
		} 
		
		long playerId = userTask.getPlayerId();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return false;
		}
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null || !playerAlliance.isExistAlliance()) {
			return false;
		}
		
		boolean updateTaskStatus = false;
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			if(userTask.getStatus() != TaskStatus.ACCEPTED) {
				return false;
			}
			
			TaskEvent[] taskEvents = userTask.getTaskEvents();
			updateTaskStatus = taskEvents == null || taskEvents.length <= 0;
			if(taskEvents != null && taskEvents.length > 0){
				for (TaskEvent taskEvent : taskEvents) {
					if(taskEvent.isComplete()) {
						continue;
					} 
					
					if(taskEvent.getType() != taskEventType) {
						continue;
					}
					
					if(taskEvent.getAmount() <= 0) {
						continue;
					}
					
					updateTaskStatus = true;
					taskEvent.setAmount(0);
					taskEvent.updateTaskState();
				}
			}
			
			if(updateTaskStatus) {
				userTask.updateUserTaskEvents();
				userTask.checkUserTaskStatus();
				return true;
			}
		} finally {
			lock.unlock();
		}
		return false;
	}

	private boolean checkPetSavvyLevelTask(UserTask userTask) {
		if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
			return false;
		}
		
		int taskId = userTask.getTaskId();
		int taskEventType = EventType.PET_SAVVY_LEVEL_TASK;
		TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
		if(taskConfig == null || !taskConfig.hasEventType(taskEventType)) {
			return false;
		} 


		long playerId = userTask.getPlayerId();
		List<PetDomain> petDomains = petManager.getPetDomains(playerId);
		if(petDomains == null || petDomains.isEmpty()) {
			return false;
		}
	
		int maxPetSavvyLevel = 0;
		for (PetDomain petDomain : petDomains) {
			PetBattle battle = petDomain.getBattle();
			if(battle != null) {
				maxPetSavvyLevel = Math.max(maxPetSavvyLevel, battle.getSavvy());
			}
		}
		
		boolean updateTaskStatus = false;
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			if(userTask.getStatus() != TaskStatus.ACCEPTED) {
				return false;
			}
			
			TaskEvent[] taskEvents = userTask.getTaskEvents();
			updateTaskStatus = taskEvents == null || taskEvents.length <= 0;
			if(taskEvents != null && taskEvents.length > 0){
				for (TaskEvent taskEvent : taskEvents) {
					if(taskEvent.isComplete()) {
						continue;
					} 

					if(taskEvent.getType() != taskEventType) {
						continue;
					}
					
					int amount = taskEvent.getAmount();
					if(amount <= 0) {
						continue;
					}
					
					int totalAmont = taskEvent.getTotalAmont();
					int finalAmount = Math.max(0, totalAmont - maxPetSavvyLevel);
					if(finalAmount == amount) {
						continue;
					}
					
					updateTaskStatus = true;
					taskEvent.setAmount(finalAmount);
					taskEvent.updateTaskState();
				}
			}
			
			if(updateTaskStatus) {
				userTask.updateUserTaskEvents();
				userTask.checkUserTaskStatus();
				return true;
			}
		} finally {
			lock.unlock();
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	
	public boolean updatePetGrowLevelTask(long playerId) {
		List<UserTask> userTasks = taskManager.listUserTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return false;
		}
		
		try {
			Set<UserTask> modifyTaskSet = new HashSet<UserTask>();
			for (UserTask userTask : userTasks) {
				 if(checkPetGrowLevelTask(userTask)) {
					 modifyTaskSet.add(userTask);
				 }
			}
			
			if(!modifyTaskSet.isEmpty()) {
				dbService.submitUpdate2Queue(modifyTaskSet);
				TaskPushHelper.pushUserTask2Client(playerId, modifyTaskSet);
				return true;
			}
			
		} catch (Exception e) {
		}
		return false;
	}
	
	public boolean updateAddFriendTask(long playerId) {
		try {
			return updateTaskInfo(playerId, EventType.ADD_FRIEND_TASK, 1);
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean updateTaskInfo(long playerId, int taskEventType, int count) {
		return updateTaskInfo(playerId, taskEventType, -1, count);
	}
	@SuppressWarnings("unchecked")
	private boolean updateTaskInfo(long playerId, int taskEventType, int condition, int count) {
		List<UserTask> userTasks = taskManager.listUserTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return false;
		}
		
		Set<UserTask> modifyTaskSet = new HashSet<UserTask>();
		for (UserTask userTask : userTasks) {
			if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
				continue;
			}
			
			int taskId = userTask.getTaskId();
			TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
			if(taskConfig == null || !taskConfig.hasEventType(taskEventType)) {
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
						if (eventType != taskEventType || amount <= 0) {
							continue;
						}
						
						int taskCondition = taskEvent.getCondition();
						if(taskCondition > 0 && taskCondition != condition) {
							continue;
						}

						updateTaskStatus = true;
						taskEvent.setAmount(Math.max(0, amount - count));
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
			TaskPushHelper.pushUserTask2Client(playerId, modifyTaskSet);
			return true;
		}
		return false;
	}
	private int getEnchangeCount(long playerId) {
		int enchangeCount = 0;
		List<UserEquip> userEquips = new ArrayList<UserEquip>();
		userEquips.addAll(propsManager.listUserEquip(playerId, DEFAULT_BACKPACK));
		userEquips.addAll(propsManager.listUserEquip(playerId, DRESSED_BACKPACK));
		for (UserEquip userEquip : userEquips) {
			enchangeCount += userEquip.getEnchangeCount();
		}
		return enchangeCount;
	}
	
	private boolean updateEnchangeEquipTask(UserTask userTask, int enchangCount) {
		if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
			return false;
		}
			
		int taskId = userTask.getTaskId();
		int taskEventType = EventType.ENCHANGE_EQUIP_TASK;
		TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
		if(taskConfig == null || !taskConfig.hasEventType(taskEventType)) {
			return false;
		} 

		boolean updateTaskStatus = false;
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			if(userTask.getStatus() != TaskStatus.ACCEPTED) {
				return false;
			}
			
			TaskEvent[] taskEvents = userTask.getTaskEvents();
			updateTaskStatus = taskEvents == null || taskEvents.length <= 0;
			if(taskEvents != null && taskEvents.length > 0){
				for (TaskEvent taskEvent : taskEvents) {
					if(taskEvent.isComplete()) {
						continue;
					} 

					if (taskEvent.getType() != taskEventType) {
						continue;
					} 
					
					int amount = taskEvent.getAmount();
					if(amount <= 0) {
						continue;
					}
					
					int totalAmont = taskEvent.getTotalAmont();
					int finalAmount = Math.max(0, totalAmont - enchangCount);
					if(finalAmount == amount) {
						continue;
					}
					
					updateTaskStatus = true;
					taskEvent.setAmount(finalAmount);
					taskEvent.updateTaskState();
				}
			}
			
			if(updateTaskStatus) {
				userTask.updateUserTaskEvents();
				userTask.checkUserTaskStatus();
				return true;
			}
		} finally {
			lock.unlock();
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	
	public boolean updateEnchanceEquipTask(long playerId) {
		List<UserTask> userTasks = taskManager.listUserTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return false;
		}
		
		try {
			int enchangeCount = this.getEnchangeCount(playerId);
			Set<UserTask> modifyTaskSet = new HashSet<UserTask>();
			for (UserTask userTask : userTasks) {
				 if(updateEnchangeEquipTask(userTask, enchangeCount)) {
					 modifyTaskSet.add(userTask);
				 }
			}
			
			if(!modifyTaskSet.isEmpty()) {
				dbService.submitUpdate2Queue(modifyTaskSet);
				TaskPushHelper.pushUserTask2Client(playerId, modifyTaskSet);
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	
	public boolean updateJoinAllianceTask(long playerId) {
		List<UserTask> userTasks = taskManager.listUserTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return false;
		}
		
		try {
			Set<UserTask> modifyTaskSet = new HashSet<UserTask>();
			for (UserTask userTask : userTasks) {
				if(checkJoinAllianceTask(userTask)) {
					modifyTaskSet.add(userTask);
				}
			}
			
			if(!modifyTaskSet.isEmpty()) {
				dbService.submitUpdate2Queue(modifyTaskSet);
				TaskPushHelper.pushUserTask2Client(playerId, modifyTaskSet);
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	private boolean checkRushMeridianTask(UserTask userTask) {
		if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
			return false;
		}
		
		int taskId = userTask.getTaskId();
		int taskEventType = EventType.RUSH_MERIDIAN_TASK;
		TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
		if(taskConfig == null || !taskConfig.hasEventType(taskEventType)) {
			return false;
		} 
		
		long playerId = userTask.getPlayerId();
		Meridian meridian = meridianManager.getMeridian(playerId);
		if(meridian == null) {
			return false;
		}
		
		boolean updateTaskStatus = false;
		int meridianCount = meridian.getMeridianIds();
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			if(userTask.getStatus() != TaskStatus.ACCEPTED) {
				return false;
			}
			
			TaskEvent[] taskEvents = userTask.getTaskEvents();
			updateTaskStatus = taskEvents == null || taskEvents.length <= 0;
			if(taskEvents != null && taskEvents.length > 0){
				for (TaskEvent taskEvent : taskEvents) {
					if(taskEvent.isComplete()) {
						continue;
					} 
					
					if(taskEvent.getType() != taskEventType) {
						continue;
					}
					
					int amount = taskEvent.getAmount();
					if(amount <= 0) {
						continue;
					}
					
					int maxAmount = taskEvent.getTotalAmont();
					int finalAmount = Math.max(0, maxAmount - meridianCount);
					if(finalAmount == amount) {
						continue;
					}
					
					updateTaskStatus = true;
					taskEvent.setAmount(finalAmount);
					taskEvent.updateTaskState();
				}
			}
			
			if(updateTaskStatus) {
				userTask.updateUserTaskEvents();
				userTask.checkUserTaskStatus();
				return true;
			}
		} finally {
			lock.unlock();
		}
		return false;
	}

	private boolean checkMortalLevelUpTask(UserTask userTask) {
		if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
			return false;
		}
		
		int taskId = userTask.getTaskId();
		int taskEventType = EventType.MORTAL_LEVELUP_TASK;
		TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
		if(taskConfig == null || !taskConfig.hasEventType(taskEventType)) {
			return false;
		} 
		
		long playerId = userTask.getPlayerId();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		UserMortalBody userMortalBody = mortalManager.getUserMortalBody(userDomain.getBattle());
		if(userMortalBody == null) {
			return false;
		}
		
		boolean updateTaskStatus = false;
		int maxLevel = userMortalBody.getMaxLevel();
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			if(userTask.getStatus() != TaskStatus.ACCEPTED) {
				return false;
			}
			
			TaskEvent[] taskEvents = userTask.getTaskEvents();
			updateTaskStatus = taskEvents == null || taskEvents.length <= 0;
			if(taskEvents != null && taskEvents.length > 0){
				for (TaskEvent taskEvent : taskEvents) {
					if(taskEvent.isComplete()) {
						continue;
					} 
					
					if(taskEvent.getType() != taskEventType) {
						continue;
					}
					
					int amount = taskEvent.getAmount();
					if(amount <= 0) {
						continue;
					}
					
					int maxAmount = taskEvent.getTotalAmont();
					int finalAmount = Math.max(0, maxAmount - maxLevel);
					if(finalAmount == amount) {
						continue;
					}
					
					updateTaskStatus = true;
					taskEvent.setAmount(finalAmount);
					taskEvent.updateTaskState();
				}
			}
			
			if(updateTaskStatus) {
				userTask.updateUserTaskEvents();
				userTask.checkUserTaskStatus();
				return true;
			}
		} finally {
			lock.unlock();
		}
		return false;
	}
	
	
	@SuppressWarnings("unchecked")
	
	public boolean updateRushMeridianTask(long playerId) {
		List<UserTask> userTasks = taskManager.listUserTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return false;
		}
		
		try {
			Set<UserTask> modifyTaskSet = new HashSet<UserTask>();
			for (UserTask userTask : userTasks) {
				if(checkRushMeridianTask(userTask)) {
					modifyTaskSet.add(userTask);
				}
			}
			
			if(!modifyTaskSet.isEmpty()) {
				dbService.submitUpdate2Queue(modifyTaskSet);
				TaskPushHelper.pushUserTask2Client(playerId, modifyTaskSet);
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	
	public boolean updateMortalLevelUpTask(long playerId) {
		List<UserTask> userTasks = taskManager.listUserTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return false;
		}
		
		try {
			Set<UserTask> modifyTaskSet = new HashSet<UserTask>();
			for (UserTask userTask : userTasks) {
				if(checkMortalLevelUpTask(userTask)) {
					modifyTaskSet.add(userTask);
				}
			}
			
			if(!modifyTaskSet.isEmpty()) {
				dbService.submitUpdate2Queue(modifyTaskSet);
				TaskPushHelper.pushUserTask2Client(playerId, modifyTaskSet);
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	
	public boolean updatePetSavvyLevelTask(long playerId) {
		List<UserTask> userTasks = taskManager.listUserTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return false;
		}
		
		try {
			Set<UserTask> modifyTaskSet = new HashSet<UserTask>();
			for (UserTask userTask : userTasks) {
				if(checkPetSavvyLevelTask(userTask)) {
					modifyTaskSet.add(userTask);
				}
			}
			
			if(!modifyTaskSet.isEmpty()) {
				dbService.submitUpdate2Queue(modifyTaskSet);
				TaskPushHelper.pushUserTask2Client(playerId, modifyTaskSet);
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	
	public boolean updateHorseLevelTask(long playerId) {
		List<UserTask> userTasks = taskManager.listUserTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return false;
		}
		
		try {
			Set<UserTask> modifyTaskSet = new HashSet<UserTask>();
			for (UserTask userTask : userTasks) {
				if(checkHorseLevelTask(userTask)) {
					modifyTaskSet.add(userTask);
				}
			}
			
			if(!modifyTaskSet.isEmpty()) {
				dbService.submitUpdate2Queue(modifyTaskSet);
				TaskPushHelper.pushUserTask2Client(playerId, modifyTaskSet);
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}
	
	private boolean checkHorseLevelTask(UserTask userTask) {
		if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
			return false;
		}
		
		int taskId = userTask.getTaskId();
		int taskEventType = EventType.HORSE_LEVEL_TASK;
		TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
		if(taskConfig == null || !taskConfig.hasEventType(taskEventType)) {
			return false;
		} 


		long playerId = userTask.getPlayerId();
	
		Horse horse = horseManager.getHorse(playerId);
		if(horse == null) {
			return false;
		}
		
		boolean updateTaskStatus = false;
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			if(userTask.getStatus() != TaskStatus.ACCEPTED) {
				return false;
			}
			
			TaskEvent[] taskEvents = userTask.getTaskEvents();
			updateTaskStatus = taskEvents == null || taskEvents.length <= 0;
			if(taskEvents != null && taskEvents.length > 0){
				for (TaskEvent taskEvent : taskEvents) {
					if(taskEvent.isComplete()) {
						continue;
					} 

					if(taskEvent.getType() != taskEventType) {
						continue;
					}
					int amount = taskEvent.getAmount();
					if(amount <= 0) {
						continue;
					}
					
					int totalAmont = taskEvent.getTotalAmont();
					int finalAmount = Math.max(0, totalAmont - horse.getLevel());
					if(finalAmount == amount) {
						continue;
					}
					
					updateTaskStatus = true;
					taskEvent.setAmount(finalAmount);
					taskEvent.updateTaskState();
				}
			}
			
			if(updateTaskStatus) {
				userTask.updateUserTaskEvents();
				userTask.checkUserTaskStatus();
				return true;
			}
		} finally {
			lock.unlock();
		}
		return false;
	}
	
	public boolean updateHorseSeniorFancyTask(long playerId, int count) {
		try {
			return updateTaskInfo(playerId, EventType.HORSE_SENIOR_FANCY_TASK, count);
		} catch (Exception e) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	
	public boolean updateBuyMallItemTask(long playerId, int propsId, int buyCount) {
		List<UserTask> userTasks = taskManager.listUserTask(playerId);
		if(userTasks == null || userTasks.isEmpty()) {
			return false;
		}
		
		try {
			int type = EventType.BUY_MALL_ITEM_TASK;
			Set<UserTask> modifyTaskSet = new HashSet<UserTask>();
			for (UserTask userTask : userTasks) {
				if(userTask == null || userTask.getStatus() != TaskStatus.ACCEPTED) {
					continue;
				}
				
				int taskId = userTask.getTaskId();
				TaskConfig taskConfig = taskManager.getTaskConfig(taskId);
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
							if (eventType != type || amount <= 0 || taskEvent.getCondition() != propsId) {
								continue;
							} 
							
							updateTaskStatus = true;
							taskEvent.setAmount(Math.max(0, amount - buyCount));
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
				TaskPushHelper.pushUserTask2Client(playerId, modifyTaskSet);
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}
	
	
}
