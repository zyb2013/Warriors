package com.yayo.warriors.module.task.manager.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.basedb.adapter.TaskService;
import com.yayo.warriors.basedb.model.TaskConfig;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.task.dao.TaskDao;
import com.yayo.warriors.module.task.entity.TaskComplete;
import com.yayo.warriors.module.task.entity.UserTask;
import com.yayo.warriors.module.task.manager.TaskManager;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
@Service
public class TaskManagerImpl extends CachedServiceAdpter implements TaskManager, ApplicationListener<ContextRefreshedEvent> {
	
	@Autowired
	private TaskDao taskDao;
	@Autowired
	private UserManager userManager;
	@Autowired
	private TaskService taskService;
	@Autowired
	private ResourceService resourceService;
	@Autowired(required=false)
	@Qualifier("task.repaire.ids")
	private String repaireTaskIds = "";
	

	private static final String PREFIX = "USER_TASK_";
	private static final String PLAYER_ID = "_PLAYERID_";
	private static final String TASK_CHAIN = "_TASK_CHAIN_";
	
	
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(StringUtils.isBlank(repaireTaskIds)) {
			return;
		}
		
		Set<UserTask> userTasks = new HashSet<UserTask>();	
		String[] taskIdArrays = repaireTaskIds.split(Splitable.ATTRIBUTE_SPLIT);
		for (String taskIdStr : taskIdArrays) {
			if(StringUtils.isBlank(taskIdStr)) {
				continue;
			}
			
			int taskId = Integer.valueOf(taskIdStr);
			TaskConfig taskConfig = getTaskConfig(taskId);
			if(taskConfig == null) {
				continue;
			}
			
			int chain = taskConfig.getChain();
			List<UserTask> userTaskList = taskDao.listUserTask(chain, taskId);
			if(userTaskList == null || userTaskList.isEmpty()) {
				continue;
			}
			
			userTasks.addAll(userTaskList);
			for (UserTask userTask : userTaskList) {
				userTasks.add(userTask);
			}
		}
		
		if(!userTasks.isEmpty()) {
			try {
				taskDao.delete(userTasks);
				for (UserTask userTask : userTasks) {
					this.removeEntityFromCache(userTask.getId(), UserTask.class);
					this.removeUserTaskCache(userTask.getPlayerId());
				}
			} catch (Exception e) {
			}
		}
	}
	
	private String getHashKey(long playerId) {
		return PREFIX + playerId;
	}
	private String getSubKey(long playerId) {
		return PLAYER_ID + playerId;
	}

	private String getChainSubKey(int chain) {
		return TASK_CHAIN + chain;
	}
	
	public void removeUserTaskCache(long playerId) {
		cachedService.removeFromCommonCache(getHashKey(playerId));
	}
	
	
	
	public void createUserTask(UserTask userTask, Collection<UserProps> userPropsList) {
		taskDao.createTask(userTask, userPropsList);
		this.put2EntityCache(userTask);
	}

	
	@SuppressWarnings("unchecked")
	public List<Long> listUserTaskId(long playerId) {
		String hashKey = this.getHashKey(playerId);
		String subKey = this.getSubKey(playerId);
		List<Long> idList = (List<Long>) cachedService.getFromCommonCache(hashKey, subKey);
		if(idList == null) {
			idList = taskDao.listUseraTaskId(playerId);
			cachedService.put2CommonHashCache(hashKey, subKey, idList);
		}
		return idList;
	} 
	
	
	public void initilaizeCreateTaskIdList(long playerId) {
		cachedService.put2CommonHashCache(this.getHashKey(playerId), this.getSubKey(playerId), new ArrayList<Long>(0));
	}

	private long getUserTaskId(long playerId, int chain) {
		String hashKey = this.getHashKey(playerId);
		String subKey = this.getChainSubKey(chain);
		Long userTaskId = (Long) cachedService.getFromCommonCache(hashKey, subKey);
		if(userTaskId == null) {
			userTaskId = taskDao.getUserTaskId(playerId, chain);
			cachedService.put2CommonHashCache(hashKey, subKey, userTaskId);
		}
		return userTaskId;
	}


	public UserTask getUserTaskByChain(long playerId, int chain) {
		long userTaskId = getUserTaskId(playerId, chain);
		return getUserTask(userTaskId);
	}
	
	
	public List<UserTask> listUserTask(long playerId) {
		List<Long> idList = listUserTaskId(playerId);
		return this.getEntityFromIdList(idList, UserTask.class);
	}
	
	public void removeAllTask(long playerId) {
		taskDao.removeAll(playerId);
		TaskComplete taskComplete = this.getTaskComplete(playerId);
		taskComplete.setCompletes("");
		taskDao.update(taskComplete);
		this.removeUserTaskCache(playerId);	
	}

	
	
	public void fastCompleteTask(long playerId, TaskComplete taskComplete) {
		taskDao.removeAll(playerId);
		taskDao.update(taskComplete);
		this.removeUserTaskCache(playerId);	
	}

	
	public UserTask getUserTask(long userTaskId) {
		return this.get(userTaskId, UserTask.class);
	}

	
	public TaskConfig getTaskConfig(int taskId) {
		return resourceService.get(taskId, TaskConfig.class);
	}

	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		List<Long> userTaskIds = this.listUserTaskId(messageInfo.getPlayerId());
		this.removeEntityFromCache(userTaskIds, UserTask.class);
	}

	
	public List<TaskConfig> getPreviousTask(int taskId) {
		return taskService.getPreviousTask(taskId);
	}

	
	public TaskComplete getTaskComplete(long playerId) {
		TaskComplete taskComplete = null;
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain != null) {
			taskComplete = this.get(playerId, TaskComplete.class);
		}
		return taskComplete;
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

	
	public long getUserTask(long playerId, int chain) {
		return taskDao.getUserTaskId(playerId, chain);
	}
}
