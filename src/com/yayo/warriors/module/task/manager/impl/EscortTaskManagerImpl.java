package com.yayo.warriors.module.task.manager.impl;

import java.io.Serializable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.adapter.TaskService;
import com.yayo.warriors.basedb.model.EscortGharryConfig;
import com.yayo.warriors.basedb.model.EscortTaskConfig;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.module.task.entity.UserEscortTask;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.task.rule.EscortTaskRule;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.type.FormulaKey;
import com.yayo.warriors.type.IndexName;

@Service
public class EscortTaskManagerImpl extends CachedServiceAdpter implements EscortTaskManager {
	@Autowired
	private TaskService taskService; 
	@Autowired
	private ResourceService resourceService;
	
	
	public UserEscortTask getEscortTask(PlayerBattle battle) {
		if(battle == null || battle.getLevel() < EscortTaskRule.MIN_ACCEPT_ESCORT_LEVEL){
			return null;
		}
		long playerId = battle.getId();
		return this.get(playerId, UserEscortTask.class);
	}

	
	
	public EscortTaskConfig getEscortTaskConfig(int taskId) {
		return resourceService.get(taskId, EscortTaskConfig.class);
	}

	
	@SuppressWarnings("unchecked")
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == UserEscortTask.class) {
			long playerId = (Long) id;
			UserEscortTask escortTask = commonDao.get(playerId, UserEscortTask.class);
			if(escortTask != null) {
				return (T) escortTask;
			}
			
			try {
				escortTask = UserEscortTask.valueOf(playerId);
				commonDao.save(escortTask);
				return (T) escortTask;
			} catch (Exception e) {
				return null;
			}
		}
		return super.getEntityFromDB(id, clazz);
	}

	
	public List<EscortTaskConfig> getEscortTaskConfig4Camp(int camp) {
		return resourceService.listByIndex(IndexName.ESCORT_TASK_CAMP_TYPE, EscortTaskConfig.class, camp);
	}

	
	public boolean isEscortStatus(PlayerBattle battle) {
		UserEscortTask userEscortTask = this.getEscortTask(battle);
		if(userEscortTask != null && !userEscortTask.isNoTask()){
			return true;
		}
		return false;
	}

	
	public int getRandomEscortTaskQuality() {
		return taskService.getRandomQuality();
	}


	
	public EscortGharryConfig getEscortGharryConfig(int quality) {
		return resourceService.get(quality, EscortGharryConfig.class);
	}


	
	public boolean isRide(PlayerBattle battle) {
		UserEscortTask userEscortTask = this.getEscortTask(battle);
		if(userEscortTask != null && !userEscortTask.isNoTask()){
			return true;
		}
		return false;
	}

	
	public int getMoveSpeed(PlayerBattle battle) {
		int speed = 0;
		UserEscortTask userEscortTask = this.getEscortTask(battle);
		if(userEscortTask != null && !userEscortTask.isNoTask()){
			EscortGharryConfig config = this.getEscortGharryConfig(userEscortTask.getQuality());
			speed = config.getSpeed();
			return speed;
		}
		return speed;
	}

	
	public int getEscortMount(PlayerBattle battle) {
		int model = 0;
		UserEscortTask userEscortTask = this.getEscortTask(battle);
		if(userEscortTask != null && !userEscortTask.isNoTask()){
			EscortGharryConfig config = this.getEscortGharryConfig(userEscortTask.getQuality());
			model = config.getModel();
			return model;
		}
		return model;
	}


	
	public void onLoginEvent(UserDomain userDomain, int branching) {
		if(userDomain == null) {
			return;
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		UserEscortTask userEscortTask = this.getEscortTask(battle);
		if(userEscortTask == null) {
			return;
		} 

		if(userEscortTask.isNoTask() || !userEscortTask.isplunder()) {
			return;
		}
		
		ChainLock lock = LockUtils.getLock(player, userEscortTask);
		try {
			lock.lock();
			if(!userEscortTask.isNoTask() && userEscortTask.isplunder()){
				player.setProtection(true);//已经被人劫过镖
			}
		}finally{
			lock.unlock();
		}
	}


	
	public int caclPlunderEscortExp(int exp) {
		return FormulaHelper.invoke(FormulaKey.PLUNDER_ESCORT_EXP, exp).intValue();
	}


	
	public int caclEscortProtectUnplunder(int exp) {
		return FormulaHelper.invoke(FormulaKey.ESCORT_PROTECT_UNPLUNDER, exp).intValue();
	}


	
	public int caclEscortUnprotectPlunder(int exp) {
		return FormulaHelper.invoke(FormulaKey.ESCORT_UNPROTECT_PLUNDER, exp).intValue();
	}


	
	public int caclEscortProtectPlunder(int exp) {
		return FormulaHelper.invoke(FormulaKey.ESCORT_PROTECT_PLUNDER, exp).intValue();
	}

}
