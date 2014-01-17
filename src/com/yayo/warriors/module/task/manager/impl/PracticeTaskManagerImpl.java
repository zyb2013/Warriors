
package com.yayo.warriors.module.task.manager.impl;

import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.basedb.adapter.TaskService;
import com.yayo.warriors.basedb.model.MonsterConfig;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.PracticeRewardConfig;
import com.yayo.warriors.module.monster.facade.MonsterFacade;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.task.entity.UserPracticeTask;
import com.yayo.warriors.module.task.manager.PracticeTaskManager;
import com.yayo.warriors.module.task.model.TaskCondition;
import com.yayo.warriors.module.task.rule.TaskRule;
import com.yayo.warriors.module.task.type.EventType;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;

@Service
public class PracticeTaskManagerImpl extends CachedServiceAdpter implements PracticeTaskManager {

	@Autowired
	private DbService dbService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private MonsterFacade monsterFacade;
	
	
	public UserPracticeTask getPracticeTask(UserDomain userDomain) {
		if(userDomain == null) {
			return null;
		}
		long playerId = userDomain.getPlayerId();
		PlayerBattle battle = userDomain.getBattle();
		UserPracticeTask userTask = this.get(playerId, UserPracticeTask.class);
		return checkPracticeTaskInfo(battle, userTask);
	}
	
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		this.removeEntityFromCache(messageInfo.getPlayerId(), UserPracticeTask.class);
	}

	@SuppressWarnings("unchecked")
	
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == UserPracticeTask.class) {
			UserPracticeTask practiceTask = commonDao.get(id, UserPracticeTask.class);
			if(practiceTask != null) {
				return (T) practiceTask;
			}
			
			try {
				practiceTask = UserPracticeTask.valueOf((Long) id);
				commonDao.save(practiceTask);
				return (T) practiceTask;
			} catch (Exception e) {
				return null;
			}
		}
		return super.getEntityFromDB(id, clazz);
	}

	
	public PracticeRewardConfig getPracticeRewardConfig(int completes) {
		return taskService.get(completes, PracticeRewardConfig.class);
	}

	
	public int getRandomPracticeTaskQuality() {
		return taskService.getRandomQuality();
	}

	private UserPracticeTask checkPracticeTaskInfo(PlayerBattle battle, UserPracticeTask userTask) {
		if(userTask == null || (!userTask.isNeedDailyRefresh() && !userTask.needRefreshTask())) {
			return userTask;
		}
		
		boolean updateIntime = false;
		ChainLock lock = LockUtils.getLock(userTask);
		try {
			lock.lock();
			if(!userTask.isNeedDailyRefresh() && !userTask.needRefreshTask()) {
				return userTask;
			}
			
			if(userTask.isNeedDailyRefresh()) {
				updateIntime = true;
				userTask.resetFinish();
			}
			
			if(userTask.needRefreshTask()) {
				updateIntime = true;
				int quality = Quality.WHITE.ordinal();
				userTask.refreshTask(this.obtainTaskCondition(battle), quality, battle.getLevel());
			}
		} finally {
			lock.unlock();
		}
		
		if(updateIntime) {
			dbService.updateEntityIntime(userTask);
		}
		return userTask;
	}
	
	
	private TaskCondition obtainTaskCondition(PlayerBattle battle) {
		int playerLevel = Math.max(TaskRule.MIN_PRACTICE_LEVEL, battle.getLevel());
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
		int killCount = TaskRule.getRandomPracticeKillCount();
		return TaskCondition.valueOf(EventType.KILLS, monsterId, killCount, String.valueOf(baseId));
	}	
}
