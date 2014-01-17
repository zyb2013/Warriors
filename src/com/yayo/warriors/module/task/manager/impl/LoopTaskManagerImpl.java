package com.yayo.warriors.module.task.manager.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.adapter.EquipService;
import com.yayo.warriors.basedb.adapter.PropsService;
import com.yayo.warriors.basedb.adapter.TaskService;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.LoopRewardConfig;
import com.yayo.warriors.basedb.model.LoopTaskConfig;
import com.yayo.warriors.basedb.model.MonsterConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.module.map.types.ScreenType;
import com.yayo.warriors.module.monster.facade.MonsterFacade;
import com.yayo.warriors.module.npc.facade.NpcFacade;
import com.yayo.warriors.module.npc.model.Npc;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.shop.manager.ShopManager;
import com.yayo.warriors.module.task.entity.UserLoopTask;
import com.yayo.warriors.module.task.manager.LoopTaskManager;
import com.yayo.warriors.module.task.model.TaskCondition;
import com.yayo.warriors.module.task.rule.TaskRule;
import com.yayo.warriors.module.task.type.EventType;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.type.GoodsType;

@Service
public class LoopTaskManagerImpl extends CachedServiceAdpter implements LoopTaskManager {
	
	@Autowired
	private DbService dbService;
	@Autowired
	private NpcFacade npcFacade;
	@Autowired
	private ShopManager shopManager;
	@Autowired
	private TaskService taskService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MonsterFacade monsterFacade;
	@Autowired
	private EquipService equipService;
	@Autowired
	private PropsService propsService;
	@Autowired
	private ResourceService resourceService;
	
	
	public LoopRewardConfig getLoopRewardConfig(int loopRewardId) {
		return resourceService.get(loopRewardId, LoopRewardConfig.class);
	}
	
	
	public Collection<LoopRewardConfig> listCanLoopRewardConfig() {
		return taskService.listCanLoopRewardConfig();
	}


	
	public int getRandomLoopTaskQuality() {
		return taskService.getRandomQuality();
	}

	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		long playerId = messageInfo.getPlayerId();
		this.removeEntityFromCache(playerId, UserLoopTask.class);
	}

	
	public UserLoopTask getUserLoopTask(long playerId) {
		return checkUserLoopTaskInfo(playerId > 0 ? this.get(playerId, UserLoopTask.class) : null);
	}
	private UserLoopTask checkUserLoopTaskInfo(UserLoopTask userTask) {
		if(userTask == null) {
			return userTask;
		} else if(!userTask.isNeedDailyRefresh() && !userTask.needRefreshTask()) {
			return userTask;
		}
		
		Long playerId = userTask.getId();
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return userTask;
		}
		
		PlayerBattle battle = userDomain.getBattle();
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			if(!userTask.isNeedDailyRefresh() && !userTask.needRefreshTask()) {
				return userTask;
			}
			
			if(userTask.isNeedDailyRefresh()) {
				userTask.resetFinish();
			}
			
			if(userTask.needRefreshTask()) {
				int quality = this.getRandomLoopTaskQuality();
				userTask.refreshTask(this.obtainLoopTaskCondition(userDomain), quality, battle.getLevel());
			}
		} finally {
			lock.unlock();
		}
		
		dbService.submitUpdate2Queue(userTask);
		return userTask;
	}
	
	public TaskCondition obtainLoopTaskCondition(UserDomain userDomain) {
		PlayerBattle battle = userDomain.getBattle();
		LoopTaskConfig task = taskService.obtainLoopTaskByRandom();
		if (task == null) {
			return null;
		}

		int playerLevel = Math.max(TaskRule.MIN_LOOP_TASK_LEVEL, battle.getLevel());
		switch (task.getType()) {
			case EventType.TALK:
			{
				Npc taskNpc = npcFacade.getRandomNpcByScreenType(ScreenType.CASTLE.ordinal());
				if(taskNpc != null) {
					return TaskCondition.valueOf(task, taskNpc.getId(), 1, null);
				}
			}
			case EventType.KILLS:
			{
				MonsterConfig monsterConfig = monsterFacade.getRandomMonsterFight(playerLevel);
				if(monsterConfig == null) {
					return null;
				}
				
				MonsterFightConfig monsterFight = monsterConfig.getMonsterFight();
				if(monsterFight == null) {
					return null;
				}
				
				int monsterId = monsterConfig.getId();
				int baseId = monsterFight.getBaseId();
				int killCount = TaskRule.getRandomLoopKillCount();
				return TaskCondition.valueOf(task, monsterId, killCount, String.valueOf(baseId));
			}
			case EventType.BUY_EQUIP_COUNT:
			{
				List<EquipConfig> equipConfigList = equipService.listEquipConfig(playerLevel, Quality.WHITE.ordinal());
				if(equipConfigList == null || equipConfigList.isEmpty()) {
					return null;
				}
				
				while(equipConfigList != null && !equipConfigList.isEmpty()) {
					EquipConfig equipConfig = equipConfigList.remove(Tools.getRandomInteger(equipConfigList.size()));
					long npcId = shopManager.findNpcByEquipOrProps(GoodsType.EQUIP, equipConfig.getId(), ScreenType.CASTLE.ordinal());
					if(npcId > 0) {
						return TaskCondition.valueOf(task, equipConfig.getId(), 1, String.valueOf(npcId));
					}
				}
			}
			case EventType.BUY_PROPS_COUNT: 
			{
				List<PropsConfig> propsList = propsService.listPropsConfig();
				while(propsList != null && !propsList.isEmpty()) {
					PropsConfig propsConfig = propsList.remove(Tools.getRandomInteger(propsList.size()));
					long npcId = shopManager.findNpcByEquipOrProps(GoodsType.PROPS, propsConfig.getId(), ScreenType.CASTLE.ordinal());
					if(npcId > 0) {
						return TaskCondition.valueOf(task, propsConfig.getId(), 1, String.valueOf(npcId));
					}
				}
			}
		}
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == UserLoopTask.class) {
			UserLoopTask userLoopTask = commonDao.get(id, UserLoopTask.class);
			if(userLoopTask != null) {
				return (T) userLoopTask;
			}
			
			try {
				userLoopTask = UserLoopTask.valueOf((Long) id);
				commonDao.save(userLoopTask);
				return (T) userLoopTask;
			} catch (Exception e) {
				return null;
			}
		}
		return super.getEntityFromDB(id, clazz);
	}
}
