package com.yayo.warriors.module.task.manager.impl;

import java.io.Serializable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.warriors.basedb.adapter.TaskService;
import com.yayo.warriors.basedb.model.MapTaskConfig;
import com.yayo.warriors.module.task.dao.MapTaskDao;
import com.yayo.warriors.module.task.entity.TaskComplete;
import com.yayo.warriors.module.task.entity.UserMapTask;
import com.yayo.warriors.module.task.manager.MapTaskManager;
import com.yayo.warriors.module.task.manager.TaskManager;


@Service
public class MapTaskManagerImpl extends CachedServiceAdpter implements MapTaskManager {
	
	@Autowired
	private MapTaskDao mapTaskDao;
	@Autowired
	private TaskManager taskManager;
	@Autowired
	private TaskService taskService;
	@Autowired
	private ResourceService resourceService;

	private static final String PREFIX = "USER_MAP_TASK_";
	private static final String PLAYER_ID = "_PLAYERID_";
	private static final String TASK_CHAIN = "_TASK_CHAIN_";
	
	private String getHashKey(long playerId) {
		return PREFIX + playerId;
	}

	private String getSubKey(long playerId) {
		return PLAYER_ID + playerId;
	}
	private String getChainSubKey(int chain) {
		return TASK_CHAIN + chain;
	}
	
	public void removeUserMapTaskCache(long playerId) {
		cachedService.removeFromCommonCache(getHashKey(playerId));
	}
	
	
	
	public void createUserMapTask(UserMapTask userMapTask) {
		mapTaskDao.save(userMapTask);
	}
	
	@SuppressWarnings("unchecked")
	public List<Long> listUserMapTaskId(long playerId) {
		String hashKey = this.getHashKey(playerId);
		String subKey = this.getSubKey(playerId);
		List<Long> idList = (List<Long>) cachedService.getFromCommonCache(hashKey, subKey);
		if(idList == null) {
			idList = mapTaskDao.listUserMapTaskId(playerId);
			cachedService.put2CommonHashCache(hashKey, subKey, idList);
		}
		return idList;
	} 
	
	private long getUserMapTaskId(long playerId, int chain) {
		String hashKey = this.getHashKey(playerId);
		String subKey = this.getChainSubKey(chain);
		Long userTaskId = (Long) cachedService.getFromCommonCache(hashKey, subKey);
		if(userTaskId == null) {
			userTaskId = mapTaskDao.getUserMapTask(playerId, chain);
			cachedService.put2CommonHashCache(hashKey, subKey, userTaskId);
		}
		return userTaskId;
	}

	public UserMapTask getUserMapTaskByChain(long playerId, int chain) {
		long userTaskId = getUserMapTaskId(playerId, chain);
		return getUserMapTask(userTaskId);
	}
	public List<UserMapTask> listUserMapTask(long playerId) {
		List<Long> idList = listUserMapTaskId(playerId);
		return this.getEntityFromIdList(idList, UserMapTask.class);
	}
	public void removeAllTask(long playerId) {
		mapTaskDao.removeAll(playerId);
		TaskComplete taskComplete = taskManager.getTaskComplete(playerId);
		taskComplete.setCompletes("");
		mapTaskDao.update(taskComplete);
		this.removeUserMapTaskCache(playerId);	
	}

	
	
	public void fastCompleteTask(long playerId, TaskComplete taskComplete) {
		mapTaskDao.removeAll(playerId);
		mapTaskDao.update(taskComplete);
		this.removeUserMapTaskCache(playerId);	
	}

	
	public UserMapTask getUserMapTask(long userTaskId) {
		return this.get(userTaskId, UserMapTask.class);
	}

	
	public MapTaskConfig getMapTaskConfig(int taskId) {
		return resourceService.get(taskId, MapTaskConfig.class);
	}

	
	public MapTaskConfig getPreviousMapTask(int taskId) {
		return taskService.getPreviousMapTask(taskId);
	}

	@SuppressWarnings("unchecked")
	
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(id != null && clazz == TaskComplete.class) {
			TaskComplete taskComplete = commonDao.get((Long)id, TaskComplete.class);
			if(taskComplete != null) {
				return (T) taskComplete;
			}
			try {
				taskComplete = TaskComplete.valueOf((Long)id);
				commonDao.save(taskComplete);
				return (T) taskComplete;
			} catch (Exception e) {
				return null;
			}
			
		}
		return super.getEntityFromDB(id, clazz);
	}

	
	public long getUserMapTask(long playerId, int chain) {
		return mapTaskDao.getUserMapTask(playerId, chain);
	}
}
