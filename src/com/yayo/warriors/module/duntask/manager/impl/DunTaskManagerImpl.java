package com.yayo.warriors.module.duntask.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.mina.util.ConcurrentHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.warriors.basedb.model.DungeonTaskConfig;
import com.yayo.warriors.common.helper.DunTaskPushHelper;
import com.yayo.warriors.module.duntask.manager.DunTaskManager;
import com.yayo.warriors.module.duntask.model.DunTask;
import com.yayo.warriors.module.duntask.model.DunTaskEvent;
import com.yayo.warriors.module.duntask.types.TaskState;
import com.yayo.warriors.module.duntask.types.TaskTypes;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 副本任务Manager实现类 
 * @author liuyuhua
 */
@Service
public class DunTaskManagerImpl implements DunTaskManager, DataRemoveListener{

	@Autowired
	private UserManager userManager;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private DunTaskPushHelper dunTaskPushHelper;
	
	private static final ConcurrentHashMap<Long, ConcurrentHashSet<DunTask>> PLAYERDUNTASK = new ConcurrentHashMap<Long, ConcurrentHashSet<DunTask>>(0); 
	
	
	public void removeAll(long playerId) {
		ConcurrentHashSet<DunTask> duntasks = PLAYERDUNTASK.get(playerId);
		if(duntasks == null){
			return;
		}
		duntasks.clear();
	}
	
	
	public List<DunTask> accpet(long playerId, Collection<Integer> taskBaseIds) {
		UserDomain userDomain = this.userManager.getUserDomain(playerId);
		if(userDomain == null){
			return null;
		}
		
		if(taskBaseIds == null || taskBaseIds.isEmpty()){
			return null;
		}
		
		List<DunTask> tasks = new ArrayList<DunTask>();
		for(int taskBaseId : taskBaseIds){
			DungeonTaskConfig config = this.getDungeonTaskConfig(taskBaseId);
			DunTask task = DunTask.valueOf(playerId, config.getId(), config.getEventConfig());
			this.saveTask(playerId, task);
			tasks.add(task);
		}
		return tasks;
	}

	
	public DungeonTaskConfig getDungeonTaskConfig(int taskBaseId) {
		return resourceService.get(taskBaseId,DungeonTaskConfig.class);
	}
	
	
	public void onDataRemoveEvent(MessageInfo messageInfo) {
		long playerId = messageInfo.getPlayerId();
		ConcurrentHashSet<DunTask> dunTasks = PLAYERDUNTASK.get(playerId);
		if(dunTasks == null || !dunTasks.isEmpty()){
			return;
		}
		PLAYERDUNTASK.remove(playerId);
		
	}

	
	public DunTask getDunTask(long playerId, long taskId) {
		ConcurrentHashSet<DunTask> duntasks = this.getPlayerDunTasks(playerId);
		for(DunTask task : duntasks){
			if(task.getId() == taskId){
				return task;
			}
		}
		return null;
	}

	
	/**
	 * 删除副本任务
	 * @param playerId   删除副本任务对象
	 * @param taskId     任务的ID
	 */
	
	public boolean remove(long playerId,long taskId){
		ConcurrentHashSet<DunTask> taskSet = this.getPlayerDunTasks(playerId);
		for(Iterator<DunTask> it = taskSet.iterator() ; it.hasNext();){
			DunTask task = it.next();
			if(task.getId() == taskId){
				it.remove();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 保存到缓存对象
	 * @param playerId   玩家的ID
	 * @param task       副本任务对象
	 */
	private void saveTask(long playerId,DunTask task){
		ConcurrentHashSet<DunTask> taskSet = this.getPlayerDunTasks(playerId);
		taskSet.add(task);
	}
	
	/**
	 * 获取玩家副本任务集合对象
	 * @param playerId   玩家的ID
	 * @return
	 */
	private ConcurrentHashSet<DunTask> getPlayerDunTasks(long playerId){
		ConcurrentHashSet<DunTask> taskSet = PLAYERDUNTASK.get(playerId);
		if(taskSet == null){
			taskSet = new ConcurrentHashSet<DunTask>();
			PLAYERDUNTASK.putIfAbsent(playerId, taskSet);
			taskSet = PLAYERDUNTASK.get(playerId);
		}
		return taskSet;
	}
	

	
	public void updateProgress(long playerId, int condition, TaskTypes type) {
		ConcurrentHashSet<DunTask> taskSet = this.getPlayerDunTasks(playerId);
		for(Iterator<DunTask> iterator = taskSet.iterator();iterator.hasNext();){
			DunTask task = iterator.next();
			if(task.isCompleted()){
				continue;
			}
			
			ChainLock lock = LockUtils.getLock(task);
			try {
				lock.lock();
				List<DunTaskEvent> events = task.getEvents();
				for(DunTaskEvent event : events){
					if(event.isCompleted()){
						continue;
					}
					if(event.getType() != type){
						continue;
					}
					if(event.getCondition() == condition){
						event.setCurrentCount(event.getCurrentCount() + 1);
						this.dunTaskPushHelper.updateProgress(playerId, task.getId(), event.getId() , event.getCurrentCount());
					}
				}
				if(task.isCompleted()){
					task.setState(TaskState.COMPLETED);
				}
			}finally{
				lock.unlock();
			}
		}
	}

	
	public Collection<DungeonTaskConfig> getAllDungeonConfig() {
		return this.resourceService.listAll(DungeonTaskConfig.class);
	}

	
	public Collection<DunTask> getAllDunTask(long playerId) {
		return this.getPlayerDunTasks(playerId);
	}

}
