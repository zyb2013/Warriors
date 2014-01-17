package com.yayo.warriors.module.task.facade.Impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.task.constant.TaskConstant.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.model.CampTaskConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.CampTaskPushHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.ExpLogger;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.types.ScreenType;
import com.yayo.warriors.module.npc.facade.NpcFacade;
import com.yayo.warriors.module.npc.model.Npc;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.task.entity.UserCampTask;
import com.yayo.warriors.module.task.facade.CampTaskFacade;
import com.yayo.warriors.module.task.manager.CampTaskManager;
import com.yayo.warriors.module.task.model.CampTask;
import com.yayo.warriors.module.task.model.TaskEvent;
import com.yayo.warriors.module.task.rule.CampTaskRule;
import com.yayo.warriors.module.task.type.EventStatus;
import com.yayo.warriors.module.task.type.EventType;
import com.yayo.warriors.module.task.type.TaskStatus;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.socket.ResponseKey;


@Component
public class CampTaskFacadeImpl implements CampTaskFacade{

	@Autowired
	private CampTaskManager campTaskManager;
	@Autowired
	private DbService dbService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private NpcFacade npcFacade;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private VOFactory voFactory;
	
	
	public ResultObject<CampTask> accept(long playerId, int taskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		if(userDomain.getGameMap().getScreenType() != ScreenType.CAMP.ordinal()){
			return ResultObject.ERROR(NO_EXIST_CAMP_MAP);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < CampTaskRule.MIX_ACCEPT_CAMP_TASK_LEVEL){
			return ResultObject.ERROR(PLAYER_LEVEL_NOT_ENOUGH);
		}
		
		UserCampTask userCampTask = campTaskManager.getUserCampTask(playerId);
		if(userCampTask == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		CampTaskConfig config = campTaskManager.getCampTaskConfig(taskId);
		if(config == null){
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		Camp userCamp = userDomain.getPlayer().getCamp();
		if(userCamp == Camp.NONE || userCamp.ordinal() != config.getCamp()){
			return ResultObject.ERROR(TASK_CAMP_ERR);
		}
		
		if(userCampTask.getCampTask(taskId) != null){
			return ResultObject.ERROR(DUPLICATE_ACCEPT_TASK);
		}
		
		if(userCampTask.getRewardsTasks().contains(taskId)){
			return ResultObject.ERROR(TASK_WAS_REWARDED);
		}
		
		int taskSize = userCampTask.getCampTask().size();
		int rewardsSize = userCampTask.getRewardsTasks().size();
		if((taskSize + rewardsSize) > CampTaskRule.MAX_ACCEPT_CAMP_TASK){
			return  ResultObject.ERROR(MAX_COUNT_INVALID);
		}
		
		int level = battle.getLevel();
		if(level < config.getMinLevel()){
			return ResultObject.ERROR(TASK_CAMP_LEVEL_LIMIT_ERR);
		}
		
		if(config.getExtaskId() > 0) {
			if(!userCampTask.getRewardsTasks().contains(config.getExtaskId())){
				return ResultObject.ERROR(PRIVIOUS_UNCOMPLETED);
			}
		}
		
		CampTask campTask = CampTask.valueOf(playerId, taskId);
		List<String[]> events = config.getEventsList();
		if(!events.isEmpty()){
			for(String[] event : events){
				int type = Integer.parseInt(event[0]);
				int condition = Integer.parseInt(event[1]);
				int totalAmonnt = Integer.parseInt(event[2]);
			    campTask.addTaskEvent(TaskEvent.valueOf(type, condition, totalAmonnt , totalAmonnt, EventStatus.PROGRESS));
			}
		}
		
		ChainLock lock = LockUtils.getLock(userCampTask);
		try {
			lock.lock();
			if(userCampTask.getCampTask(taskId) != null){
				return ResultObject.ERROR(DUPLICATE_ACCEPT_TASK);
			}
			
			if(userCampTask.getRewardsTasks().contains(taskId)){
				return ResultObject.ERROR(TASK_WAS_REWARDED);
			}
			
			taskSize = userCampTask.getCampTask().size();
			rewardsSize = userCampTask.getRewardsTasks().size();
			if((taskSize + rewardsSize) > CampTaskRule.MAX_ACCEPT_CAMP_TASK){
				return  ResultObject.ERROR(TASK_WAS_REWARDED);
			}
		
			userCampTask.addCampTask(campTask);
		
		}finally{
			lock.unlock();
		}
		
		this.dbService.submitUpdate2Queue(userCampTask);
		return ResultObject.SUCCESS(campTask);
	}
	

	
	public int cancel(long playerId, int taskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < CampTaskRule.MIX_ACCEPT_CAMP_TASK_LEVEL){
			return PLAYER_LEVEL_NOT_ENOUGH;
		}
		
		UserCampTask userCampTask = campTaskManager.getUserCampTask(playerId);
		if(userCampTask == null){
			return PLAYER_NOT_FOUND;
		}
		
		CampTask campTask = userCampTask.getCampTask(taskId);
		if(campTask == null){
			return TASK_NOT_FOUND;
		}
		
		ChainLock lock = LockUtils.getLock(userCampTask);
		try {
			lock.lock();
			campTask = userCampTask.getCampTask(taskId);
			if(campTask == null){
				return TASK_NOT_FOUND;
			}
			
			userCampTask.removeCampTask(campTask);
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userCampTask);
		return SUCCESS;
	}

	
	@SuppressWarnings("unchecked")
	public int rewards(long playerId, int taskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		if(userDomain.getGameMap().getScreenType() != ScreenType.CAMP.ordinal()){
			return NO_EXIST_CAMP_MAP;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		Player player = userDomain.getPlayer();
		if(battle.getLevel() < CampTaskRule.MIX_ACCEPT_CAMP_TASK_LEVEL){
			return PLAYER_LEVEL_NOT_ENOUGH;
		}
		
		UserCampTask userCampTask = campTaskManager.getUserCampTask(playerId);
		if(userCampTask == null){
			return PLAYER_NOT_FOUND;
		}
		
		ChainLock lock = LockUtils.getLock(userCampTask,player,battle);
		try {
			lock.lock();
			CampTask campTask = userCampTask.getCampTask(taskId);
			if(campTask == null){
				return TASK_NOT_FOUND;
			}
			campTask.checkTaskStatus(); 
			if(campTask.getStatus() != TaskStatus.COMPLETED){
				return TASK_UNCOMPLETE;
			}
			
			CampTaskConfig config = campTaskManager.getCampTaskConfig(taskId);
			if(config == null){
				return BASEDATA_CONFIG_ERROR;
			}
			
			userCampTask.removeCampTask(campTask);
			userCampTask.addRewardCampTask(taskId);
			int rewardExp = config.getExp();
			int silver = config.getSilver();
			battle.increaseExp(rewardExp);
			player.increaseSilver(silver);
			if(rewardExp != 0){		
				ExpLogger.campTaskExp(userDomain, config, rewardExp);
			}
			if(silver != 0){
				SilverLogger.inCome(Source.REWARDS_TASK, silver, player);
			}
		}finally{
			lock.unlock();
		}
		
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeKeys.EXP,AttributeKeys.SILVER);
		dbService.submitUpdate2Queue(userCampTask,battle);
		return SUCCESS;
	}


	
	public ResultObject<CampTask> completeTalkTask(long playerId, int npcId, int campTaskId) {
		UserCampTask userCampTask = campTaskManager.getUserCampTask(playerId);
		if(userCampTask == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Npc npc = npcFacade.getNpc(npcId);
		if(npc == null) {
			return ResultObject.ERROR(TARGET_NOT_FOUND);
		}
		int baseNpcId = npc.getBaseId();
		
		ChainLock lock = LockUtils.getLock(userCampTask);
		CampTask campTask = null;
		try {
			lock.lock();
			campTask = userCampTask.getCampTask(campTaskId);
			if(campTask == null){
				return ResultObject.ERROR(TASK_NOT_FOUND);
			}
			
			if(campTask.getStatus() == TaskStatus.COMPLETED){
				return ResultObject.ERROR(TASK_COMPLETED);
			}
			
			List<TaskEvent> taskEvents = campTask.getTaskEvents();
			for (TaskEvent taskEvent : taskEvents) {
				if(taskEvent == null || taskEvent.isComplete()) {
					continue;
				} 

				int eventType = taskEvent.getType();
				int condition = taskEvent.getCondition();
				if(eventType != EventType.TALK || condition != baseNpcId) {
					continue;
				}
				
				taskEvent.setAmount(Math.max(taskEvent.getAmount() - 1, 0));
				taskEvent.updateTaskState();
			}
			
			campTask.checkTaskStatus(); 
			campTask.updateEvents(); 
			userCampTask.updateCampTask(campTask);
		}finally{
			lock.unlock();
		}
		
		return ResultObject.SUCCESS(campTask);
	}


	
	public void updateFightMonsterTask(long playerId, int monsterId) {
		UserCampTask userCampTask = campTaskManager.getUserCampTask(playerId);
		if(userCampTask == null){
			return;
		}
		
		Set<CampTask> modifyTaskSet = new HashSet<CampTask>(1);
		ChainLock lock = LockUtils.getLock(userCampTask);
		try {
			lock.lock();
			for(CampTask campTask : userCampTask.getCampTask()){
				if(campTask == null || campTask.getStatus() != TaskStatus.ACCEPTED){
					continue;
				}
				List<TaskEvent> events = campTask.getTaskEvents();
				boolean updateTaskStatus = false; 
				for(TaskEvent event : events){
					if(event.getType() != EventType.KILLS || event.isComplete()){
						continue;
					}
					
					int amount = event.getAmount();
					if (amount <= 0 || event.getCondition() != monsterId) {
						continue;
					} 
					updateTaskStatus = true;
					event.setAmount(Math.max(0, amount - 1));
					event.updateTaskState();
				}
				
				if(updateTaskStatus){
					campTask.checkTaskStatus();
					campTask.updateEvents();
					userCampTask.updateCampTask(campTask);
					modifyTaskSet.add(campTask);
				}
			}
		}finally{
			lock.unlock();
		}

		
		if(!modifyTaskSet.isEmpty()){
			CampTaskPushHelper.pushCampTask2Client(playerId, modifyTaskSet);
			dbService.submitUpdate2Queue(userCampTask);
		}
	}


	
	public void updateFightPlayerTask(long playerId, Camp camp) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return;
		}
		
		if(Camp.NONE == camp || userDomain.getPlayer().getCamp() == camp){
			return;
		}
		
		UserCampTask userCampTask = campTaskManager.getUserCampTask(playerId);
		if(userCampTask == null){
			return;
		}
		
		PlayerMotion motion = userDomain.getMotion();
		Set<CampTask> modifyTaskSet = new HashSet<CampTask>(1);
		ChainLock lock = LockUtils.getLock(userCampTask);
		try {
			lock.lock();
			for(CampTask campTask : userCampTask.getCampTask()){
				if(campTask == null || campTask.getStatus() != TaskStatus.ACCEPTED){
					continue;
				}
				List<TaskEvent> events = campTask.getTaskEvents();
				boolean updateTaskStatus = false; 
				for(TaskEvent event : events){
					if(event.getType() != EventType.KILL_CAMP_PLAYER || event.isComplete()){
						continue;
					}
					
					if(event.getCondition() != motion.getMapId()){
						continue;
					}
					
					int amount = event.getAmount();
					if (amount <= 0) {
						continue;
					} 
					updateTaskStatus = true;
					event.setAmount(Math.max(0, amount - 1));
					event.updateTaskState();
				}
				
				if(updateTaskStatus){
					campTask.checkTaskStatus();
					campTask.updateEvents();
					userCampTask.updateCampTask(campTask);
					modifyTaskSet.add(campTask);
				}
			}
		}finally{
			lock.unlock();
		}
		
		if(!modifyTaskSet.isEmpty()){
			CampTaskPushHelper.pushCampTask2Client(playerId, modifyTaskSet);
			dbService.submitUpdate2Queue(userCampTask);
		}
	}


	
	public List<CampTask> getCampTasks(long playerId) {
		List<CampTask> result = new ArrayList<CampTask>(1);
		UserCampTask userCampTask = campTaskManager.getUserCampTask(playerId);
		if(userCampTask == null){
			return result;
		}
		
		Set<CampTask> camptasks = userCampTask.getCampTask();
		if(camptasks.isEmpty()){
			return result;
		}
		
		result.addAll(camptasks);
		return result;
	}


	
	public ResultObject<Map<String, Object>> getCampTaskResult(long playerId) {
		UserCampTask userCampTask = campTaskManager.getUserCampTask(playerId);
		if(userCampTask == null){
			return ResultObject.ERROR(TASK_CAMP_LEVEL_ENOUGH_CANT_OPEN);
		}
		Map<String, Object> result = new HashMap<String, Object>(3);
		Set<CampTask> camptasks = userCampTask.getCampTask();
		result.put(ResponseKey.TASKS, camptasks.toArray());
		result.put(ResponseKey.REWARD_ID, userCampTask.getRewardsTasks().toArray());
		result.put(ResponseKey.LEVEL, userCampTask.getLevel());
		return ResultObject.SUCCESS(result);
	}


	
	public int collectProps(long playerId, int baseId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		UserCampTask userCampTask = campTaskManager.getUserCampTask(playerId);
		if(userCampTask == null){
			return TASK_NOT_FOUND;
		} 
		
		Player player = userDomain.getPlayer();
		Set<CampTask> camptasks = userCampTask.getCampTask();
		if(camptasks == null || camptasks.isEmpty()) {
			return TASK_NOT_FOUND;
		} else if(!calcCanCollect(camptasks, baseId)){
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

	/**
	 * 获得任务总共收集的道具数量
	 * 
	 * @param  userTask			用户任务对象
	 * @param  baseId			需要采集的物品
	 * @return {@link Boolean}	总数量
	 */
	private boolean calcCanCollect(Collection<CampTask> userTasks, int baseId) {
		if(userTasks == null || userTasks.isEmpty()) {
			return false;
		}
		
		for(CampTask campTask : userTasks) {
			if(userTasks == null) {
				continue;
			}
			int status = campTask.getStatus();
			if(status == TaskStatus.UNACCEPT || status == TaskStatus.REWARDS) {
				continue;
			}
			CampTaskConfig taskConfig = campTaskManager.getCampTaskConfig(campTask.getTaskId());
			if(taskConfig == null) {
				continue;
			} 
			
			if(!taskConfig.hasEventType(EventType.COLLECT)) {
				continue;
			}
			
			for (TaskEvent taskEvent : campTask.getTaskEvents()) {
				if(taskEvent.getType() != EventType.COLLECT) {
					continue;
				} else if(taskEvent.getStatus() != EventStatus.PROGRESS) {
					continue;
				} else if(taskEvent.getAmount() <= 0) {
					continue;
				} else if(taskEvent.getCondition() == baseId) {
					return true;
				}
			}
		}
		
		return false;
	}


	
	public int complete(long playerId, int taskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		UserCampTask userCampTask = campTaskManager.getUserCampTask(playerId);
		CampTask campTask = userCampTask.getCampTask(taskId);
		if(campTask == null){
			return TASK_NOT_FOUND;
		}
		
		if(campTask.getStatus() == TaskStatus.COMPLETED){
			return TASK_COMPLETED;
		}
		
		boolean hasTaskEventUpdate = false;
		try {
			List<TaskEvent> events = campTask.getTaskEvents();
			if(events != null && !events.isEmpty()){
				for(TaskEvent taskEvent : events){
					if(taskEvent.getStatus() != EventStatus.PROGRESS) {
						continue;
					}
					boolean success = false;
					switch (taskEvent.getType()) {
						case EventType.COLLECT	:success = updatePropsEventStatus(userDomain, taskEvent);			break;		
					}
					hasTaskEventUpdate = success ? true : hasTaskEventUpdate;
				}
			}
		} catch (Exception e) {
		}
		
		if(hasTaskEventUpdate){
			ChainLock lock = LockUtils.getLock(campTask);
			try {
				lock.lock();
				campTask.checkTaskStatus();
			}finally{
				lock.unlock();
			}
		}
		
		return SUCCESS;
	}
	
	/**
	 * 检测道具状态
	 * @param useDomain       玩家对象 
	 * @param taskEvent       任务事件
	 * @return {@link Boolean} true 成功 false 反之
	 */
	private boolean updatePropsEventStatus(UserDomain useDomain, TaskEvent taskEvent){
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
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, userPropsList);
		return true;
	}
	
}
