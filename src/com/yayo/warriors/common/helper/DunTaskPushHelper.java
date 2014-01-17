package com.yayo.warriors.common.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.module.duntask.model.DunTask;
import com.yayo.warriors.module.duntask.util.DunTaskFactory;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.duntask.DunTaskCmd;

@Component
public class DunTaskPushHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(DunTaskPushHelper.class);
	
	@Autowired
	private Pusher pusher;
	
	/**
	 * 接取副本任务
	 * @param playerId   玩家的ID
	 * @param duntask    副本任务对象
	 */
	public void acceptDunTask(long playerId,Collection<DunTask> duntasks){
		if(duntasks != null){
			Response response = Response.defaultResponse(Module.DUNGEONTASK, DunTaskCmd.PUT_ACCEPT_DUNTASK);
			response.setValue(DunTaskFactory.buildDunTask4Array(duntasks).toArray());
			pusher.pushMessage(playerId, response);
		}
	}
	
	/**
	 * 更新进度
	 * @param playerId     玩家的ID
	 * @param taskId       任务的ID
	 * @param eventId      事件的ID
	 * @param progress     进度数量
	 */
	public void updateProgress(long playerId,long taskId,int eventId,int progress){
		Map<String,Object> result = new HashMap<String,Object>(3);
		result.put(ResponseKey.TASKID, taskId);
		result.put(ResponseKey.EVENTID,eventId);
		result.put(ResponseKey.PROGRESS,progress);
		Response response = Response.defaultResponse(Module.DUNGEONTASK, DunTaskCmd.PUT_UPDATE_PROGRESS, result);
		pusher.pushMessage(playerId, response);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("玩家[{}],副本任务[{}],事件[{}],当前进度[{}].",new Object[]{playerId,taskId,eventId,progress});
		}
	}
	
	
}
