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
import com.yayo.warriors.module.task.model.CampTask;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.handler.task.TaskCmd;

/**
 * 阵营任务主动推送类
 * @author liuyuhua
 */
@Component
public class CampTaskPushHelper {
	
	@Autowired
	private Pusher pusher;
	private static final Logger logger = LoggerFactory.getLogger(CampTaskPushHelper.class); 
	private static ObjectReference<CampTaskPushHelper> ref = new ObjectReference<CampTaskPushHelper>();
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	/**
	 * 获得阵营任务推送类帮助类
	 * @return {@link CampTaskPushHelper}
	 */
	public static CampTaskPushHelper getInstance() {
		return ref.get();
	}
	
	/**
	 * 推送玩家的阵营任务给客户端
	 * @param playerId     玩家的ID
	 * @param camptasks    阵营任务集合
	 */
	public static void pushCampTask2Client(long playerId,Collection<CampTask> camptasklist){
		if(camptasklist != null && !camptasklist.isEmpty()){
			getInstance().pusher.pushMessage(playerId, Response.defaultResponse(Module.TASK, TaskCmd.PUSH_CAMP_TASK_2_CLIENT, camptasklist.toArray()) );
			if(logger.isDebugEnabled()){
				logger.debug("推送玩家:[{}] 任务信息:{} ", playerId,camptasklist);
			}
		}
	}

}
