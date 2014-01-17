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
import com.yayo.warriors.basedb.model.AllianceConfig;
import com.yayo.warriors.basedb.model.AllianceTaskConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.AllianceTaskPushHelper;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.alliance.model.DonateRecord;
import com.yayo.warriors.module.alliance.model.Record;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.npc.facade.NpcFacade;
import com.yayo.warriors.module.npc.model.Npc;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.PropsStackResult;
import com.yayo.warriors.module.props.rule.PropsHelper;
import com.yayo.warriors.module.task.entity.UserAllianceTask;
import com.yayo.warriors.module.task.facade.AllianceTaskFacade;
import com.yayo.warriors.module.task.manager.AllianceTaskManager;
import com.yayo.warriors.module.task.model.AllianceTask;
import com.yayo.warriors.module.task.model.AllianceTaskEvent;
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
public class AllianceTaskFacadeImpl implements AllianceTaskFacade {

	@Autowired
	private UserManager userManager;
	@Autowired
	private AllianceManager allianceManager;
	@Autowired
	private AllianceTaskManager allianceTaskManager;
	@Autowired
	private DbService dbService;
	@Autowired
	private NpcFacade npcFacade;
	@Autowired
	private PropsManager propsManager;
	
	
	
	public List<AllianceTask> getAllianceTasks(long playerId) {
		List<AllianceTask> allianceTasks = new ArrayList<AllianceTask>(3);
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return allianceTasks;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null || !playerAlliance.isExistAlliance()){
			return allianceTasks;
		}
		
 		UserAllianceTask userAllianceTask = allianceTaskManager.getUserAllianceTask(battle);
		if(userAllianceTask == null){
			return allianceTasks;
		}
		
		allianceTasks.addAll(userAllianceTask.getTasks());
		return allianceTasks;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public ResultObject<Map<String, Object>> rewards(long playerId, int taskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		String name = player.getName();
		if(playerAlliance == null || !playerAlliance.isExistAlliance()){
			return ResultObject.ERROR(ALLIANCE_TASK_NOT_FOUND);
		}
		
		UserAllianceTask userAllianceTask = this.allianceTaskManager.getUserAllianceTask(battle);
		if(userAllianceTask == null){
			return ResultObject.ERROR(ALLIANCE_TASK_NOT_FOUND);
		}
		
		AllianceTask allianceTask = userAllianceTask.getTask(taskId);
		if(allianceTask == null){
			return ResultObject.ERROR(ALLIANCE_TASK_NOT_FOUND);
		}
		
		if(allianceTask.getStatus() != TaskStatus.COMPLETED){
			return ResultObject.ERROR(TASK_UNCOMPLETE);
		}
		
		AllianceTaskConfig config = allianceTaskManager.getAllianceTaskConfig(taskId);
		if(config == null){
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		int rewardDonate = config.getDonate(); 
		int rewardSilver = config.getAllianceSilver(); 
		int rewardExp = config.caclExp(battle.getLevel());
		
		Alliance alliance = allianceManager.getAlliance(playerAlliance.getAllianceId());
		if(alliance == null || alliance.isDrop()){
			return ResultObject.ERROR(ALLIANCE_TASK_NOT_FOUND);
		}
		
		AllianceConfig allianceConfig = allianceManager.getAllianceConfig(alliance.getLevel());
		if(allianceConfig == null){
			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
		}
		
		DonateRecord donateRecord = alliance.getDonateRecord4PlayerId(playerId);
		
		ChainLock lock = LockUtils.getLock(alliance,userAllianceTask,playerAlliance,battle);
		AllianceTask newAllianceTask = null;
		try {
			lock.lock();
			
			userAllianceTask.removeTask(allianceTask);
			userAllianceTask.addRewardTask(taskId);
			alliance.increaseSilver(rewardSilver);
			playerAlliance.increaseDonate(rewardDonate);
			playerAlliance.increaseHisDonate(rewardDonate);
			battle.increaseExp(rewardExp);
			
			if(alliance.getSilver() > allianceConfig.getSilverLimit()){
				alliance.setSilver(allianceConfig.getSilverLimit());
			}
			
			if(userAllianceTask.getRewardsTasks().get(taskId) != null){
			    int count = userAllianceTask.getRewardsTasks().get(taskId);
			    if(count < config.getCompleteCount()){
			    	newAllianceTask = allianceTaskManager.buildAllianceTask(battle, userAllianceTask,config);
			    	if(newAllianceTask != null){
			    		userAllianceTask.addTask(newAllianceTask);
			    	}
			    }
			}
			
			alliance.addRecordLog(Record.log4Silver(name, rewardSilver));
			if(donateRecord == null){
				donateRecord = DonateRecord.valueOf(playerId, name, rewardDonate);
				alliance.getDonateRecoreds().add(donateRecord);
			}else{
				donateRecord.increaseDonate(rewardDonate);
			}
			alliance.sortDonateRecored();
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userAllianceTask,playerAlliance,alliance);
		
		
		List<Long> playerIdList = Arrays.asList(playerId);
		List<UnitId> unitIdList = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIdList, AttributeKeys.EXP);
		
		Map<String, Object> resultObject = new HashMap<String,Object>(3);
		resultObject.put(ResponseKey.RESULT, SUCCESS);
		resultObject.put(ResponseKey.TASKID, taskId);
		resultObject.put(ResponseKey.ALLIANCE_SILVER, alliance.getSilver());
		if(newAllianceTask != null){
			resultObject.put(ResponseKey.TASKS, newAllianceTask);
		}else{
			resultObject.put(ResponseKey.STATE, 1);
		}
		
		return ResultObject.SUCCESS(resultObject);
	}

	
	public void updateFightPlayer(long playerId, Camp camp) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return;
		}
		PlayerBattle battle = userDomain.getBattle();
		UserAllianceTask userAllianceTask = allianceTaskManager.getUserAllianceTask(battle);
		if(userAllianceTask == null){
			return;
		}
		if(Camp.NONE == camp || userDomain.getPlayer().getCamp() == camp){
			return;
		}
		
		PlayerMotion motion = userDomain.getMotion();
		Set<AllianceTask> modifyTaskSet = new HashSet<AllianceTask>(1);
		ChainLock lock = LockUtils.getLock(userAllianceTask);
		try {
			lock.lock();
			for(AllianceTask allianceTask : userAllianceTask.getTasks()){
				if(allianceTask == null || allianceTask.getStatus() != TaskStatus.ACCEPTED){
					continue;
				}
				List<AllianceTaskEvent> events = allianceTask.getTaskEvents();
				boolean updateTaskStatus = false; 
				for(AllianceTaskEvent event : events){
					if(event.getType() != EventType.KILL_CAMP_PLAYER_ALLIANCE_TASK || event.isComplete()){
						continue;
					}
					
					if(event.getCondition() != motion.getMapId()){
						continue;
					}
					
					int amount = event.getAmount();
					if (amount <= 0) {
						updateTaskStatus = true;
						event.updateTaskState();
						continue;
					} 
					
					updateTaskStatus = true;
					event.setAmount(Math.max(0, amount - 1));
					event.updateTaskState();
				}
				
				if(updateTaskStatus){
					allianceTask.checkTaskStatus();
					allianceTask.updateEvents();
					userAllianceTask.updateTask(allianceTask);
					modifyTaskSet.add(allianceTask);
				}
			}
		}finally{
			lock.unlock();
		}
		
		if(!modifyTaskSet.isEmpty()){
			AllianceTaskPushHelper.pushAllianceTask2Client(playerId, modifyTaskSet);
			dbService.submitUpdate2Queue(userAllianceTask);
		}
		
	}

	
	public void updatePlunderEscort(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		UserAllianceTask userAllianceTask = allianceTaskManager.getUserAllianceTask(battle);
		if(userAllianceTask == null){
			return;
		}
		
		Set<AllianceTask> modifyTaskSet = new HashSet<AllianceTask>(1);
		ChainLock lock = LockUtils.getLock(userAllianceTask);
		
		try {
			lock.lock();
			
			for(AllianceTask allianceTask : userAllianceTask.getTasks()){
				if(allianceTask == null || allianceTask.getStatus() != TaskStatus.ACCEPTED){
					continue;
				}
				List<AllianceTaskEvent> events = allianceTask.getTaskEvents();
				boolean updateTaskStatus = false; 
				for(AllianceTaskEvent event : events){
					if(event.getType() != EventType.KILL_CAMP_PLAYER_ALLIANCE_TASK){
						continue;
					}
					
					int amount = event.getAmount();
					if (amount <= 0) {
						updateTaskStatus = true;
						event.updateTaskState();
						continue;
					} 
					updateTaskStatus = true;
					event.setAmount(Math.max(0, amount - 1));
					event.updateTaskState();
				}
				
				if(updateTaskStatus){
					allianceTask.checkTaskStatus();
					allianceTask.updateEvents();
					userAllianceTask.updateTask(allianceTask);
					modifyTaskSet.add(allianceTask);
				}
			}
		}finally{
			lock.unlock();
		}
			
		if(!modifyTaskSet.isEmpty()){
			AllianceTaskPushHelper.pushAllianceTask2Client(playerId, modifyTaskSet);
			dbService.submitUpdate2Queue(userAllianceTask);
		}
	}

	
	public void updateTreaders(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		UserAllianceTask userAllianceTask = allianceTaskManager.getUserAllianceTask(battle);
		if(userAllianceTask == null){
			return;
		}
		
		Set<AllianceTask> modifyTaskSet = new HashSet<AllianceTask>(1);
		ChainLock lock = LockUtils.getLock(userAllianceTask);
		
		try {
			lock.lock();
			
			for(AllianceTask allianceTask : userAllianceTask.getTasks()){
				if(allianceTask == null || allianceTask.getStatus() != TaskStatus.ACCEPTED){
					continue;
				}
				List<AllianceTaskEvent> events = allianceTask.getTaskEvents();
				boolean updateTaskStatus = false; 
				for(AllianceTaskEvent event : events){
					if(event.getType() != EventType.TALK || event.isComplete()){
						continue;
					}
					
					int amount = event.getAmount();
					if (amount <= 0) {
						updateTaskStatus = true;
						event.updateTaskState();
						continue;
					} 
					updateTaskStatus = true;
					event.setAmount(Math.max(0, amount - 1));
					event.updateTaskState();
				}
				
				if(updateTaskStatus){
					allianceTask.checkTaskStatus();
					allianceTask.updateEvents();
					userAllianceTask.updateTask(allianceTask);
					modifyTaskSet.add(allianceTask);
				}
			}
		}finally{
			lock.unlock();
		}
			
		if(!modifyTaskSet.isEmpty()){
			AllianceTaskPushHelper.pushAllianceTask2Client(playerId, modifyTaskSet);
			dbService.submitUpdate2Queue(userAllianceTask);
		}
		

	}

	
	public void updateKillMonster(long playerId, int monsterBaseId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		UserAllianceTask userAllianceTask = allianceTaskManager.getUserAllianceTask(battle);
		if(userAllianceTask == null){
			return;
		}
		
		Set<AllianceTask> modifyTaskSet = new HashSet<AllianceTask>(1);
		ChainLock lock = LockUtils.getLock(userAllianceTask);
		
		try {
			lock.lock();
			
			for(AllianceTask allianceTask : userAllianceTask.getTasks()){
				if(allianceTask == null){
					continue;
				}
				
				List<AllianceTaskEvent> events = allianceTask.getTaskEvents();
				boolean updateTaskStatus = false; 
				for(AllianceTaskEvent event : events){
					if(event.getType() != EventType.KILLS){
						continue;
					}
					
					if(event.getCondition() != monsterBaseId){
						continue;
					}
					
					int amount = event.getAmount();
					if (amount <= 0) {
						updateTaskStatus = true;
						event.updateTaskState();
						continue;
					} 
					
					updateTaskStatus = true;
					event.setAmount(Math.max(0, amount - 1));
					event.updateTaskState();
				}
				
				if(updateTaskStatus){
					allianceTask.checkTaskStatus();
					allianceTask.updateEvents();
					userAllianceTask.updateTask(allianceTask);
					modifyTaskSet.add(allianceTask);
				}
			}
		}finally{
			lock.unlock();
		}
			
		if(!modifyTaskSet.isEmpty()){
			AllianceTaskPushHelper.pushAllianceTask2Client(playerId, modifyTaskSet);
			dbService.submitUpdate2Queue(userAllianceTask);
		}
	}

	
	public void updateDonate(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		UserAllianceTask userAllianceTask = allianceTaskManager.getUserAllianceTask(battle);
		if(userAllianceTask == null){
			return;
		}
		
		Set<AllianceTask> modifyTaskSet = new HashSet<AllianceTask>(1);
		ChainLock lock = LockUtils.getLock(userAllianceTask);
		
		try {
			lock.lock();
			
			for(AllianceTask allianceTask : userAllianceTask.getTasks()){
				if(allianceTask == null || allianceTask.getStatus() != TaskStatus.ACCEPTED){
					continue;
				}
				List<AllianceTaskEvent> events = allianceTask.getTaskEvents();
				boolean updateTaskStatus = false; 
				for(AllianceTaskEvent event : events){
					if(event.getType() != EventType.KILL_CAMP_PLAYER_ALLIANCE_TASK || event.isComplete()){
						continue;
					}
					
					int amount = event.getAmount();
					if (amount <= 0) {
						event.updateTaskState();
						continue;
					} 
					updateTaskStatus = true;
					event.setAmount(Math.max(0, amount - 1));
					event.updateTaskState();
				}
				
				if(updateTaskStatus){
					allianceTask.checkTaskStatus();
					allianceTask.updateEvents();
					userAllianceTask.updateTask(allianceTask);
					modifyTaskSet.add(allianceTask);
				}
			}
		}finally{
			lock.unlock();
		}
			
		if(!modifyTaskSet.isEmpty()){
			AllianceTaskPushHelper.pushAllianceTask2Client(playerId, modifyTaskSet);
			dbService.submitUpdate2Queue(userAllianceTask);
		}
		
		
	}

	
	public ResultObject<AllianceTask> acceptAllianceTask(long playerId, int allianceTaskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null || !playerAlliance.isExistAlliance()){
			return ResultObject.ERROR(PLAYER_NOT_EXSIT_ALLIANCE);
		}
		
		UserAllianceTask userAllianceTask = allianceTaskManager.getUserAllianceTask(battle);
		if(userAllianceTask == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		AllianceTask allianceTask = userAllianceTask.getTask(allianceTaskId);
		if(allianceTask == null){
			return ResultObject.ERROR(TASK_NOT_FOUND);
		}
		
		if(allianceTask.getStatus() == TaskStatus.ACCEPTED || allianceTask.getStatus() == TaskStatus.COMPLETED){
			return ResultObject.ERROR(DUPLICATE_ACCEPT_TASK);
		}
		
		ChainLock lock = LockUtils.getLock(userAllianceTask,allianceTask);
		try {
			lock.lock();
			
			if(allianceTask.getStatus() == TaskStatus.ACCEPTED || allianceTask.getStatus() == TaskStatus.COMPLETED){
				return ResultObject.ERROR(DUPLICATE_ACCEPT_TASK);
			}
			
			allianceTask.accept();
			userAllianceTask.updateTask(allianceTask);
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userAllianceTask);
		return ResultObject.SUCCESS(allianceTask);
	}
	

	
	public ResultObject<AllianceTask> completeTalkTask(long playerId, int npcId, int taskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		UserAllianceTask userAllianceTask = allianceTaskManager.getUserAllianceTask(battle);
		if(userAllianceTask == null){
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		AllianceTask allianceTask = userAllianceTask.getTask(taskId);
		if(allianceTask == null){
			return ResultObject.ERROR(TASK_NOT_FOUND);
		}
		
		if(allianceTask.getStatus() == TaskStatus.COMPLETED){
			return ResultObject.ERROR(TASK_COMPLETED);
		}
		
		Npc npc = npcFacade.getNpc(npcId);
		if(npc == null) {
			return ResultObject.ERROR(TARGET_NOT_FOUND);
		}
		int baseNpcId = npc.getBaseId();
		
		ChainLock lock = LockUtils.getLock(userAllianceTask);
		try {
			lock.lock();
			
			for(AllianceTaskEvent event : allianceTask.getTaskEvents()){
				if(event.getType() != EventType.TALK){
					continue;
				}
				
				if(event.getCondition() != baseNpcId){
					continue;
				}
				
				int amount = event.getAmount();
				if (amount <= 0) { 
					event.updateTaskState();
					continue;
				} 
				
				event.setAmount(Math.max(0, amount - 1));
				event.updateTaskState();
			}
			
			allianceTask.checkTaskStatus();
			allianceTask.updateEvents();
			userAllianceTask.updateTask(allianceTask);
			
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userAllianceTask);
		return ResultObject.SUCCESS(allianceTask);
	}

	
	public int collectProps(long playerId, int baseId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		UserAllianceTask userAllianceTask = allianceTaskManager.getUserAllianceTask(battle);
		if(userAllianceTask == null){
			return TASK_NOT_FOUND;
		} 
		
		Player player = userDomain.getPlayer();
		Set<AllianceTask> allianceTasks = userAllianceTask.getTasks();
		if(allianceTasks == null || allianceTasks.isEmpty()) {
			return TASK_NOT_FOUND;
		} else if(!calcCanCollect(allianceTasks, baseId)){
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
		
		Collection<BackpackEntry> backpackEntries = new ArrayList<BackpackEntry>(2);
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
	
	private boolean calcCanCollect(Collection<AllianceTask> userTasks, int baseId) {
		if(userTasks == null || userTasks.isEmpty()) {
			return false;
		}
		
		for(AllianceTask allianceTask : userTasks) {
			if(userTasks == null) {
				continue;
			}
			int status = allianceTask.getStatus();
			if(status == TaskStatus.UNACCEPT || status == TaskStatus.REWARDS) {
				continue;
			} 
			AllianceTaskConfig taskConfig = allianceTaskManager.getAllianceTaskConfig(allianceTask.getTaskId());
			if(taskConfig == null) {
				continue;
			} 
			
			if(!taskConfig.hasEventType(EventType.COLLECT)) {
				continue;
			}
			
			for (AllianceTaskEvent taskEvent : allianceTask.getTaskEvents()) {
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

	
	public int cancel(long playerId, int taskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		UserAllianceTask userAllianceTask = allianceTaskManager.getUserAllianceTask(battle);
		if(userAllianceTask == null){
			return TASK_NOT_FOUND;
		}
		
		AllianceTask allianceTask = userAllianceTask.getTask(taskId);
		if(allianceTask == null){
			return TASK_NOT_FOUND;
		}
		
		ChainLock lock = LockUtils.getLock(userAllianceTask);
		try {
			lock.lock();
			allianceTask = userAllianceTask.getTask(taskId);
			if(allianceTask == null){
				return TASK_NOT_FOUND;
			}
			userAllianceTask.removeTask(allianceTask);
		}finally{
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userAllianceTask);
		return SUCCESS;
	}


	
	public int complete(long playerId, int taskId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return PLAYER_NOT_FOUND;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		UserAllianceTask userAllianceTask = allianceTaskManager.getUserAllianceTask(battle);
		AllianceTask allianceTask = userAllianceTask.getTask(taskId);
		if(allianceTask == null){
			return TASK_NOT_FOUND;
		}
		
		if(allianceTask.getStatus() == TaskStatus.COMPLETED){
			return TASK_COMPLETED;
		}
		
		boolean hasTaskEventUpdate = false;
		try {
			List<AllianceTaskEvent> events = allianceTask.getTaskEvents();
			if(events != null && !events.isEmpty()){
				for(AllianceTaskEvent taskEvent : events){
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
			ChainLock lock = LockUtils.getLock(allianceTask);
			try {
				lock.lock();
				allianceTask.checkTaskStatus();
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
	private boolean updatePropsEventStatus(UserDomain useDomain, AllianceTaskEvent taskEvent){
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


	
	public String getUserAllianceTaskProgress(long playerId) {
		String result = "";
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return result;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		UserAllianceTask userAllianceTask = allianceTaskManager.getUserAllianceTask(battle);
		return userAllianceTask.getRewardstask();
	}
	

}
