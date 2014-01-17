package com.yayo.warriors.module.chat.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.basedb.model.TaskConfig;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.task.entity.TaskComplete;
import com.yayo.warriors.module.task.manager.TaskManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.task.TaskCmd;

/**
 * 增加用户任务命令解析器
 * 
 * @author Hyint
 */
@Component
public class UserTaskParser extends AbstractGMCommandParser {
	
	@Autowired
	private Pusher pusher;
	@Autowired
	private TaskManager taskManager;
	@Autowired
	private ResourceService resourceService;
	
	
	protected String getCommand() {
		return GmType.USER_TASK;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		long playerId = userDomain.getPlayerId();
		Integer taskId = Integer.valueOf(elements[2]);
		Set<Integer> taskIds = new HashSet<Integer>();
		TaskConfig task = resourceService.get(taskId, TaskConfig.class);
		while(task != null) {
			taskId = task.getId();
			taskIds.add(taskId);
			task = null;
			List<TaskConfig> previousTask = taskManager.getPreviousTask(taskId);
			if(previousTask != null && !previousTask.isEmpty()) {
				for (TaskConfig previous : previousTask) {
					task = resourceService.get(previous.getId(), TaskConfig.class);
				}
			}
		}
		
		TaskComplete taskComplete = taskManager.getTaskComplete(playerId);
		taskComplete.getCompleteIdSet().clear();
		taskComplete.getCompleteIdSet().addAll(taskIds);
		taskComplete.updateCompleteSet();
		taskManager.fastCompleteTask(playerId, taskComplete);
		this.pushUserTask2Client(taskComplete);
		return true;
	}
	
	private void pushUserTask2Client(TaskComplete taskComplete) {
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.USER_TASK, null);
		resultMap.put(ResponseKey.COMPLETES, taskComplete.getCompleteIdSet().toArray() );
		pusher.pushMessage(taskComplete.getId(), Response.defaultResponse(Module.TASK, TaskCmd.LIST_USER_TASK, resultMap));
	}
}
