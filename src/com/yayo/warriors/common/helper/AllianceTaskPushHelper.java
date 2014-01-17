package com.yayo.warriors.common.helper;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.task.model.AllianceTask;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.handler.task.TaskCmd;


@Component
public class AllianceTaskPushHelper {

	@Autowired
	private Pusher pusher;
	private static final Logger logger = LoggerFactory.getLogger(AllianceTaskPushHelper.class); 
	private static ObjectReference<AllianceTaskPushHelper> ref = new ObjectReference<AllianceTaskPushHelper>();
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	/**
	 * 获得帮派任务推送类帮助类
	 * @return {@link CampTaskPushHelper}
	 */
	public static AllianceTaskPushHelper getInstance() {
		return ref.get();
	}
	
	/**
	 * 推送玩家的帮派任务给客户端
	 * @param playerId         玩家的ID
	 * @param allianceTasks    阵营任务集合
	 */
	public static void pushAllianceTask2Client(long playerId,Collection<AllianceTask> allianceTasks){
		if(allianceTasks != null && !allianceTasks.isEmpty()){
			getInstance().pusher.pushMessage(playerId, Response.defaultResponse(Module.TASK, TaskCmd.PUSH_ALLIANCE_TASK_2_CLIENT, allianceTasks.toArray() ) );
			if(logger.isDebugEnabled()){
				AllianceTask[] allianceTask = allianceTasks.toArray(new AllianceTask[allianceTasks.size()]);
				logger.debug("推送玩家:[{}] 帮派任务信息:{} ", playerId,allianceTask);
			}
		}
	}
	
}
