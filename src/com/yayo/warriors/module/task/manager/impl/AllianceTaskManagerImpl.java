package com.yayo.warriors.module.task.manager.impl;

import java.io.Serializable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.AllianceTaskConfig;
import com.yayo.warriors.basedb.model.MonsterConfig;
import com.yayo.warriors.basedb.model.NpcConfig;
import com.yayo.warriors.module.alliance.rule.AllianceRule;
import com.yayo.warriors.module.monster.facade.MonsterFacade;
import com.yayo.warriors.module.npc.facade.NpcFacade;
import com.yayo.warriors.module.npc.model.Npc;
import com.yayo.warriors.module.task.entity.UserAllianceTask;
import com.yayo.warriors.module.task.manager.AllianceTaskManager;
import com.yayo.warriors.module.task.model.AllianceTask;
import com.yayo.warriors.module.task.model.AllianceTaskEvent;
import com.yayo.warriors.module.task.type.EventStatus;
import com.yayo.warriors.module.task.type.EventType;
import com.yayo.warriors.module.user.entity.PlayerBattle;


@Component
public class AllianceTaskManagerImpl extends CachedServiceAdpter implements AllianceTaskManager{

	@Autowired
	private ResourceService resourceService;
	@Autowired
	private DbService dbService;
	@Autowired
	private MonsterFacade monsterFacade;
	@Autowired
	private NpcFacade npcFacade;
	
	private final static int[] KILL_MONSTER_COUNT = {50,100,150};
	
	
	public UserAllianceTask getUserAllianceTask(PlayerBattle battle) {
		if(battle == null || battle.getLevel() < AllianceRule.CREATE_AND_ADD_ALLIANCE_LEVEL_LIMIT){
			return null;
		}
		
		UserAllianceTask userAllianceTask =  this.get(battle.getId(), UserAllianceTask.class);
		List<AllianceTaskConfig> configs = this.getAllianceTaskConfigs();
		boolean falg = false;
		if(configs != null && !configs.isEmpty()){
		    if(userAllianceTask.getTasks().size() < configs.size()){
		    	ChainLock lock = LockUtils.getLock(userAllianceTask);
		    	try {
		    		lock.lock();
		    		falg = this.buildAllTasks(battle,userAllianceTask,configs);
				} catch (Exception e) {
				}finally{
					lock.unlock();
				}
		    }
		}else{
		}

	    if(falg){
	    	dbService.submitUpdate2Queue(userAllianceTask);
	    }
	    
		return userAllianceTask;
	}
	
	
	private boolean buildAllTasks(PlayerBattle battle,UserAllianceTask userAllianceTask,List<AllianceTaskConfig> configs){
		if(battle == null || userAllianceTask == null){
			return false;
		}
		
		if(userAllianceTask.getTasks().size() == configs.size()){
			return false;
		}
		
    	for(AllianceTaskConfig config : configs){
    		int taskId = config.getId();
    		AllianceTask allianceTask = userAllianceTask.getTask(taskId);
    		if(allianceTask != null){
    			continue;
    		}
    		
    		allianceTask = this.buildAllianceTask(battle, userAllianceTask, config);
    		if(allianceTask != null){
    			userAllianceTask.addTask(allianceTask);
    		}
    	}
    	
    	return true;
	} 
	
	
	
	public AllianceTask buildAllianceTask(PlayerBattle battle, UserAllianceTask userAllianceTask, AllianceTaskConfig config){
		if(battle == null || config == null || userAllianceTask == null){
			return null;
		}
		
		int taskId = config.getId();
		int completeCount =  userAllianceTask.getRewardsTasks().get(taskId) == null ? 0 : userAllianceTask.getRewardsTasks().get(taskId); //完成的次数
		if(completeCount >= config.getCompleteCount()){
			return null;
		}
		
		long playerId = battle.getId();
		AllianceTask allianceTask = AllianceTask.valueOf(playerId, taskId, completeCount);
		List<String[]> eventConfigs = config.getEventsList();
		if(eventConfigs == null || eventConfigs.isEmpty()){
			return null;
		}
		
		for(String[] eventConfig : eventConfigs){
			if(eventConfig.length < 3){
				continue;
			}
			
			int type = Integer.parseInt(eventConfig[0]);
			int condition = Integer.parseInt(eventConfig[1]);
			int totalAmonnt = Integer.parseInt(eventConfig[2]);
			int baseId = 0;
			
			if(type == EventType.TALK){
				Npc npc = npcFacade.getRandomNpcByLevel(battle.getLevel());
				if(npc != null){
					baseId = npc.getId();
					condition = npc.getId();
				}
			}
			
			if(type == EventType.KILLS){
				MonsterConfig monsterConfig = monsterFacade.getRandomMonsterFight(battle.getLevel());
				if(monsterConfig != null){
					if(monsterConfig.getMonsterFight() != null){
						baseId = monsterConfig.getId();
						condition = monsterConfig.getMonsterFight().getBaseId();
					}
				}
				totalAmonnt = KILL_MONSTER_COUNT[Tools.getRandomInteger(KILL_MONSTER_COUNT.length)];
			}
			
			if(type == EventType.COLLECT){
				Npc npc = npcFacade.getRandomCollect(battle.getLevel());
				if(npc != null ){
					NpcConfig npcConfig = npc.getNpcConfig();
					if(npcConfig != null && npcConfig.getCollectPropsId() > 0){
						baseId = npc.getId();
						condition = npcConfig.getCollectPropsId();
					}
				}
			}
			
			AllianceTaskEvent event = AllianceTaskEvent.valueOf(type, condition, totalAmonnt, totalAmonnt, EventStatus.PROGRESS, baseId);
			allianceTask.addTaskEvent(event);
		}
		
		return allianceTask;
	}
	

	
	public List<AllianceTaskConfig> getAllianceTaskConfigs() {
		return (List<AllianceTaskConfig>) resourceService.listAll(AllianceTaskConfig.class);
	}
	
	@SuppressWarnings("unchecked")
	
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == UserAllianceTask.class) {
			Long playerId = (Long) id;
			UserAllianceTask userAllianceTask = commonDao.get(playerId, UserAllianceTask.class);
			if(userAllianceTask != null) {
				return (T) userAllianceTask;
			}
			
			try {
				userAllianceTask = UserAllianceTask.valueOf(playerId);
				commonDao.save(userAllianceTask);
				return (T) userAllianceTask;
			} catch (Exception e) {
				return null;
			}
		}
		return super.getEntityFromDB(id, clazz);
	}

	
	public AllianceTaskConfig getAllianceTaskConfig(int taskId) {
		return resourceService.get(taskId, AllianceTaskConfig.class);
	}

}
