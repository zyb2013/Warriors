package com.yayo.warriors.common.helper;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.task.entity.UserLoopTask;
import com.yayo.warriors.module.task.entity.UserMapTask;
import com.yayo.warriors.module.task.entity.UserPracticeTask;
import com.yayo.warriors.module.task.entity.UserTask;
import com.yayo.warriors.module.task.model.MapTaskResult;
import com.yayo.warriors.module.task.model.TaskResult;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.task.TaskCmd;
import com.yayo.warriors.type.ElementType;

/**
 * 任务推送帮助类
 * 
 * @author Hyint
 */
@Component
public class TaskPushHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskPushHelper.class); 
	private static ObjectReference<TaskPushHelper> ref = new ObjectReference<TaskPushHelper>();
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	/**
	 * 获得任务推送类帮助类
	 * 
	 * @return {@link TaskPushHelper}
	 */
	public static TaskPushHelper getInstance() {
		return ref.get();
	}
	
	@Autowired
	private Pusher pusher;
	
	/**
	 * 推送任务信息
	 * 
	 * @param playerId			角色ID
	 * @param userTasks			任务对象列表
	 */
	public static void pushUserTask2Client(long playerId, Collection<UserTask> userTasks) {
		if(userTasks != null && !userTasks.isEmpty()) {
			getInstance().pusher.pushMessage(playerId, Response.defaultResponse(Module.TASK, TaskCmd.PUSH_TASK_2_CLIENT, userTasks.toArray()) );
			if(LOGGER.isDebugEnabled()){
				UserTask[] taskArray = userTasks.toArray(new UserTask[userTasks.size()]);
				LOGGER.debug("推送玩家:[{}] 任务信息:{} ", playerId, Arrays.toString(taskArray));
			}
		}
	}
	
	/**
	 * 推送任务信息
	 * 
	 * @param playerId			角色ID
	 * @param userTasks			任务对象列表
	 */
	public static void pushUserMapTask2Client(long playerId, Collection<UserMapTask> userTasks) {
		if(userTasks != null && !userTasks.isEmpty()) {
			getInstance().pusher.pushMessage(playerId, Response.defaultResponse(Module.TASK, TaskCmd.PUSH_MAPTASK_2_CLIENT, userTasks.toArray()) );
			if(LOGGER.isDebugEnabled()){
				UserMapTask[] taskArray = userTasks.toArray(new UserMapTask[userTasks.size()]);
				LOGGER.debug("推送玩家:[{}] 图环任务信息:{} ", playerId, Arrays.toString(taskArray));
			}
		}
	}

	/**
	 * 处理推送信息到客户端
	 * 
	 * @param taskResult
	 */
	public static void processPostResult2Client(TaskResult taskResult) {
		if(taskResult == null) {
			return;
		}
		
		UserDomain userDomain = taskResult.getUserDomain();
		Map<Long, UserTask> userTaskMap = taskResult.getTasks();
		Set<Object> attributes = taskResult.getAttributes();
		List<BackpackEntry> entries = taskResult.getEntries();
		Collection<Long> playerIds = taskResult.getPlayerIds();

		long playerId = userDomain.getId();
		if(userTaskMap != null && !userTaskMap.isEmpty()) {
			pushUserTask2Client(playerId, userTaskMap.values());
		}
		
		if(attributes != null && !attributes.isEmpty()) {
			UserPushHelper.pushAttribute2AreaMember(playerId, playerIds, Arrays.asList(userDomain.getUnitId()), attributes.toArray());
		}
		
		if(entries != null && !entries.isEmpty()) {
			int backpack = BackpackType.DEFAULT_BACKPACK;
			MessagePushHelper.pushUserProps2Client(playerId, backpack, false, entries);
		}
	}

	/**
	 * 处理推送信息到客户端
	 * 
	 * @param taskResult
	 */
	public static void processPostResult2Client(MapTaskResult taskResult) {
		if(taskResult == null) {
			return;
		}
		
		UserDomain userDomain = taskResult.getUserDomain();
		Map<Long, UserMapTask> userTaskMap = taskResult.getTasks();
		Set<Object> attributes = taskResult.getAttributes();
		List<BackpackEntry> entries = taskResult.getEntries();
		Collection<Long> playerIds = taskResult.getPlayerIds();
		
		long playerId = userDomain.getPlayerId();
		if(userTaskMap != null && !userTaskMap.isEmpty()) {
			pushUserMapTask2Client(playerId, userTaskMap.values());
		}
		
		if(attributes != null && !attributes.isEmpty()) {
			UserPushHelper.pushAttribute2AreaMember(playerId, playerIds, Arrays.asList(userDomain.getUnitId()), attributes.toArray());
		}
		
		if(entries != null && !entries.isEmpty()) {
			int backpack = BackpackType.DEFAULT_BACKPACK;
			MessagePushHelper.pushUserProps2Client(playerId, backpack, false, entries);
		}
	}
	
	/**
	 * 推送循环任务到客户端
	 * 
	 * @param  playerId			角色ID	
	 * @param  userLoopTask		用户循环任务
	 */
	public static void pushUserLoopTask2Client(long playerId, UserLoopTask userLoopTask) {
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("推送日环任务: {}", userLoopTask);
		}
		Response response = Response.defaultResponse(Module.TASK, TaskCmd.PUSH_LOOPTASK, userLoopTask);
		getInstance().pusher.pushMessage(playerId, response);
	}

	/**
	 * 推送试练任务到客户端
	 * 
	 * @param  playerId			角色ID	
	 * @param  userTask			用户试练任务
	 */
	public static void pushUserPracticeTask2Client(long playerId, UserPracticeTask userTask) {
		Response response = Response.defaultResponse(Module.TASK, TaskCmd.PUSH_PRACTICE_TASK_2_CLIENT, userTask);
		getInstance().pusher.pushMessage(playerId, response);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("推送试练任务: {}", userTask);
		}
	}
	
	/**
	 * 推送劫镖信息到客户端
	 * 
	 * @param playerId        被劫者 角色ID
	 * @param targetId        劫镖者 角色ID
	 * @param name            劫镖者 名字
	 * @param elementType     元素类型
	 */
	public static void pushEscortPlunder(long playerId, long targetId , String name , ElementType elementType) {
		Map<String,Object> result = new HashMap<String,Object>(3);
		result.put(ResponseKey.PLAYER_ID, targetId);
		result.put(ResponseKey.NAME, name);
		result.put(ResponseKey.TYPE, elementType.ordinal());
		Response response = Response.defaultResponse(Module.TASK, TaskCmd.PUSH_ESCORT_PLUNDER, result);
		getInstance().pusher.pushMessage(playerId, response);
	}
	
	
	/**
	 * 推送上镖车
	 * 
	 * @param playerId        玩家的ID
	 * @param mount           模型外观
	 * @param speed           玩家当前移动速度
	 * @param riding          是否在骑乘状态
	 * @param falg            标记(0 无状态 , 1:坐骑, 2:镖车)
	 * @param playerlist      接收者集合
	 */
	public static void pushEscortRiding(long playerId,int mount,int speed,boolean riding,int falg,Collection<Long> playerlist){
		if(playerlist != null && !playerlist.isEmpty()){
			Map<String,Object> result = new HashMap<String, Object>(5);
			result.put(ResponseKey.PLAYER_ID, playerId);
			result.put(ResponseKey.MOUNT, mount);
			result.put(ResponseKey.SPEED, speed);
			result.put(ResponseKey.RIDING,riding);
			result.put(ResponseKey.FALG, falg);
			Response response = Response.defaultResponse(Module.TASK, TaskCmd.PUSH_ESCORT_RIDING,result);
			getInstance().pusher.pushMessage(playerlist, response);
		}
	}
	
	
}
