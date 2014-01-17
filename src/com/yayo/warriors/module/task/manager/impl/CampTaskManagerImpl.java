package com.yayo.warriors.module.task.manager.impl;

import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.warriors.basedb.model.CampTaskConfig;
import com.yayo.warriors.module.task.entity.UserCampTask;
import com.yayo.warriors.module.task.manager.CampTaskManager;
import com.yayo.warriors.module.task.rule.CampTaskRule;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;


@Component
public class CampTaskManagerImpl extends CachedServiceAdpter implements CampTaskManager{
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private ResourceService resourceService;
	
	
	public UserCampTask getUserCampTask(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null || userDomain.getBattle().getLevel() < CampTaskRule.MIX_ACCEPT_CAMP_TASK_LEVEL){
			return null;
		}
		
		return this.get(playerId, UserCampTask.class);
	}
	
	
	public CampTaskConfig getCampTaskConfig(int taskId) {
		return resourceService.get(taskId, CampTaskConfig.class);
	}
	
	@SuppressWarnings("unchecked")
	
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == UserCampTask.class) {
			Long playerId = (Long) id;
			UserCampTask userCampTask = commonDao.get(playerId, UserCampTask.class);
			if(userCampTask != null) {
				return (T) userCampTask;
			}
			
			try {
				userCampTask = UserCampTask.valueOf(playerId);
				commonDao.save(userCampTask);
				return (T) userCampTask;
			} catch (Exception e) {
				return null;
			}
		}
		return super.getEntityFromDB(id, clazz);
	}
}
