package com.yayo.warriors.socket.handler.task;

import static com.yayo.common.socket.type.ResponseCode.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.type.ResponseCode;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.TaskPushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.alliance.constant.AllianceConstant;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.task.constant.TaskConstant;
import com.yayo.warriors.module.task.entity.UserEscortTask;
import com.yayo.warriors.module.task.entity.UserMapTask;
import com.yayo.warriors.module.task.entity.UserTask;
import com.yayo.warriors.module.task.facade.AllianceTaskFacade;
import com.yayo.warriors.module.task.facade.CampTaskFacade;
import com.yayo.warriors.module.task.facade.EscortTaskFacade;
import com.yayo.warriors.module.task.facade.LoopTaskFacade;
import com.yayo.warriors.module.task.facade.MapTaskFacade;
import com.yayo.warriors.module.task.facade.PracticeTaskFacade;
import com.yayo.warriors.module.task.facade.TaskFacade;
import com.yayo.warriors.module.task.facade.TaskMainFacade;
import com.yayo.warriors.module.task.model.AllianceTask;
import com.yayo.warriors.module.task.model.CampTask;
import com.yayo.warriors.module.task.model.MapTaskResult;
import com.yayo.warriors.module.task.model.MapTaskRewardResult;
import com.yayo.warriors.module.task.model.QualityResult;
import com.yayo.warriors.module.task.model.TaskResult;
import com.yayo.warriors.module.task.model.TaskRewardResult;
import com.yayo.warriors.module.task.type.TaskStatus;
import com.yayo.warriors.module.task.vo.LoopTaskVo;
import com.yayo.warriors.module.task.vo.PracticeTaskVO;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.type.ElementType;

import flex.messaging.io.amf.ASObject;


@Component
public class TaskHandler extends BaseHandler{
	@Autowired
	private TaskFacade taskFacade;
	@Autowired
	private LoopTaskFacade loopFacade;
	@Autowired
	private MapTaskFacade mapTaskFacade;
	@Autowired
	private TaskMainFacade taskMainFacade;
	@Autowired
	private EscortTaskFacade escortTaskFacade;
	@Autowired
	private PracticeTaskFacade practiceTaskFacade;
	@Autowired
	private CampTaskFacade campTaskFacade;
	@Autowired
	private AllianceTaskFacade allianceTaskFacade;
	
	
	protected int getModule() {
		return Module.TASK;
	}

	
	protected void inititialize() {
		putInvoker(TaskCmd.LIST_USER_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				listUserTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.QUERY_USER_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				queryUserTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.ACCEPT_USER_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				acceptUserTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.CANCEL_USER_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				cancelUserTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.COMPLETE_USER_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				completeUserTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.REWARDS_USER_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				rewardUserTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.COMPLETE_TALK_USERTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				completeTalkUserTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.COLLECT, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				collect(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.COMPLETE_TALK_LOOPTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				completeTalkLoopTask(session, request, response);
			}
		});

		
		putInvoker(TaskCmd.COMPLETE_LOOPTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				completeLookTask(session, request, response);
			}
		});

		putInvoker(TaskCmd.REFRESH_LOOPTASK_QUALITY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				refreshLoopTaskQuality(session, request, response);
			}
		});
		
		
		putInvoker(TaskCmd.QUERY_LOOPTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				queryLoopTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.ACCEPT_LOOPTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				acceptLoopTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.FAST_COMPLETE_LOOPTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				fastCompleteLookTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.REWARD_LOOPTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				rewardLoopTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.GIVEUP_LOOPTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				giveUpLoopTask(session, request, response);
			}
		});	
		
		putInvoker(TaskCmd.GET_LOOP_REWARD, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getReward(session, request, response);
			}
		});	
		
		putInvoker(TaskCmd.ESCORT_DUTIES_INFO, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				escortDutiesInfo(session, request, response);
			}
		});	

		putInvoker(TaskCmd.ACCEPT_ESCORT_DUTIES, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				acceptEscortTask(session, request, response);
			}
		});	
		
		putInvoker(TaskCmd.GIVE_UP_ESCORT_DUTIES, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				giveUpEscortTask(session, request, response);
			}
		});			
		
		putInvoker(TaskCmd.COMPLETE_ESCORT_DUTIES, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				completeAndRewardEscortTask(session, request, response);
			}
		});	
		
		putInvoker(TaskCmd.REFRESH_ESCORT_TASK_QUALITY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				refreshEscortTaskQuality(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.REFRESH_ESCORT_QUALITY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				refreshEscortTaskRandQuality(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.REFRESH_ESCORT_QUALITY_TO_ORANGE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				refreshEscortTaskToOrange(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.CAMP_TASK_INFO, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadCampTask(session, request, response);
			}
		});	
		
		putInvoker(TaskCmd.CAMP_TASK_APPEPT, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				acceptCampTask(session, request, response);
			}
		});	
		
		putInvoker(TaskCmd.CAMP_TASK_GIVPUP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				giveupCampTask(session, request, response);
			}
		});	
		
		putInvoker(TaskCmd.CAMP_TASK_SUBMIT, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				submitCamptask(session, request, response);
			}
		});	
		
		putInvoker(TaskCmd.COMPLETE_CAMP_TALK_CAMPTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				completeCampTaskTalk(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.LIST_MAP_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				listUserMapTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.QUERY_MAP_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				queryUserMapTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.ACCEPT_MAPTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				acceptUserMapTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.CANCEL_MAPTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				cancelUserMapTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.COMPLETE_MAPTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				completeUserMapTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.REWARDS_MAPTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				rewardUserMapTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.COMPLETE_TALK_MAPTASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				completeTalkUserMapTask(session, request, response);
			}
		});
		
		
		putInvoker(TaskCmd.QUERY_PRACTICE_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				queryPracticeTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.ACCEPT_PRACTICE_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				acceptPracticeTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.REWARD_PRACTICE_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				rewardPracticeTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.GIVEUP_PRACTICE_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				giveUpPracticeTask(session, request, response);
			}
		});	
		
		putInvoker(TaskCmd.REFRESH_PRACTICE_TASK_QUALITY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				refreshPracticeTaskQuality(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.GET_PRACTICE_REWARD, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getPracticeReward(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.FAST_COMPLETE_PRACTICE_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				fastCompletePracticeTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.LIST_ALLIANCE_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				listAllianceTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.REWARD_ALLIANCE_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				rewardAllianceTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.COMPLETE_ALLIANCE_TALK_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				completeAllianceTalkTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.CANCEL_ALLIANCE_TASK_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				cancelAllianceTask(session, request, response);
			}
		});
		
		putInvoker(TaskCmd.ACCEPT_ALLIANCE_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				acceptAllianceTask(session, request, response);
			}
		});
		
	}
	
	
	protected void fastCompletePracticeTask(IoSession session, Request request, Response response) {
		response.setValue(CommonConstant.FAILURE);
		session.write(response);
	}


	protected void acceptAllianceTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int taskId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.TASKID)) {
				taskId = ((Number)aso.get(ResponseKey.TASKID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<AllianceTask> result = allianceTaskFacade.acceptAllianceTask(playerId, taskId);
		HashMap<String,Object> resultObject = new HashMap<String, Object>(2);
		resultObject.put(ResponseKey.RESULT, result.getResult());
		if(result.getResult() == AllianceConstant.SUCCESS){
			resultObject.put(ResponseKey.TASKS, result.getValue());
		}
		response.setValue(resultObject);
		session.write(response);
	}
	
	
	protected void cancelAllianceTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int taskId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.TASKID)) {
				taskId = ((Number)aso.get(ResponseKey.TASKID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		HashMap<String,Object> map = new HashMap<String, Object>(2);
		int result = allianceTaskFacade.cancel(playerId, taskId);
		map.put(ResponseKey.RESULT, result);
		map.put(ResponseKey.TASKID, taskId);
		response.setValue(map);
		session.write(response);
	}
	

	protected void completeAllianceTalkTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int taskId = 0;
		int npcId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.TASKID)) {
				taskId = ((Number)aso.get(ResponseKey.TASKID)).intValue();
			}
			if(aso.containsKey(ResponseKey.NPCID)) {
				npcId = ((Number)aso.get(ResponseKey.NPCID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		HashMap<String,Object> map = new HashMap<String, Object>(2);
		ResultObject<AllianceTask> result = allianceTaskFacade.completeTalkTask(playerId, npcId, taskId);
		
		
		if(result.getResult() == AllianceConstant.SUCCESS){
			String progress = allianceTaskFacade.getUserAllianceTaskProgress(playerId);
			map.put(ResponseKey.RESULT, AllianceConstant.SUCCESS);
			map.put(ResponseKey.TASKS, result.getValue());
			map.put(ResponseKey.PROGRESS, progress);
			
		}else{
			map.put(ResponseKey.RESULT, result.getResult());
		}
		
		response.setValue(map);
		session.write(response);
	}
	
	
	protected void rewardAllianceTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int taskId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.TASKID)) {
				taskId = ((Number)aso.get(ResponseKey.TASKID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		allianceTaskFacade.complete(playerId, taskId);
		ResultObject<Map<String, Object>> result = allianceTaskFacade.rewards(playerId, taskId);
		if(result.getResult() == TaskConstant.SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			Map<String, Object> map = new HashMap<String, Object>(1);
			map.put(ResponseKey.RESULT, result.getResult());
			response.setValue(map);
			session.write(response);
		}
	}
	

	protected void listAllianceTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		List<AllianceTask> result = allianceTaskFacade.getAllianceTasks(playerId);
		Map<String,Object> mapResult = new HashMap<String, Object>(3);
		mapResult.put(ResponseKey.RESULT, TaskConstant.SUCCESS);
		if(result != null && !result.isEmpty()){
			mapResult.put(ResponseKey.FALG, 1); 
			mapResult.put(ResponseKey.TASKS, result.toArray());
		}else{
			mapResult.put(ResponseKey.FALG, 0);
		}
		
		response.setValue(mapResult);
		session.write(response);
	}
	
	protected void refreshEscortTaskRandQuality(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		String userItems = "";
		int autoBuyCount = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.USER_PROPS)) {
				userItems = (String) aso.get(ResponseKey.USER_PROPS);
			}
			if(aso.containsKey(ResponseKey.AUTO_BUY_COUNT)) {
				autoBuyCount = ((Number)aso.get(ResponseKey.AUTO_BUY_COUNT)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Map<String, Object> resultObject = new HashMap<String, Object>(2);
		ResultObject<UserEscortTask> result = escortTaskFacade.refreshRandQuality(playerId, userItems, autoBuyCount);
		resultObject.put(ResponseKey.RESULT, result.getResult());
		if(result.getResult() == TaskConstant.SUCCESS){
			resultObject.put(ResponseKey.TASKS, result.getValue());
		}
		
		response.setValue(resultObject);
		session.write(response);
	}
	
	

	protected void refreshEscortTaskToOrange(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		String userItems = "";
		int autoBuyCount = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.USER_PROPS)) {
				userItems = (String) aso.get(ResponseKey.USER_PROPS);
			}
			if(aso.containsKey(ResponseKey.AUTO_BUY_COUNT)) {
				autoBuyCount = ((Number)aso.get(ResponseKey.AUTO_BUY_COUNT)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Map<String, Object> resultObject = new HashMap<String, Object>(2);
		ResultObject<UserEscortTask> result = escortTaskFacade.refreshOrange(playerId, userItems, autoBuyCount);
		resultObject.put(ResponseKey.RESULT, result.getResult());
		if(result.getResult() == TaskConstant.SUCCESS){
			resultObject.put(ResponseKey.TASKS, result.getValue());
		}
		
		response.setValue(resultObject);
		session.write(response);
	}

	
	protected void refreshEscortTaskQuality(IoSession session, Request request, Response response) {
		int refreshTimes = 200;
		boolean autoBuyRefreshBook = false ;
		int targetQuality = Quality.PURPLE.ordinal();
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.TARGET_QUALITY)) {
				targetQuality = ((Number)aso.get(ResponseKey.TARGET_QUALITY)).intValue();
			}
			if(aso.containsKey(ResponseKey.AUTOBUY_REFRESHBOOK)) {
				autoBuyRefreshBook = ((Boolean)aso.get(ResponseKey.AUTOBUY_REFRESHBOOK));
			}
			if(aso.containsKey(ResponseKey.REFRESH_TIMES)) {
				refreshTimes = ((Number)aso.get(ResponseKey.REFRESH_TIMES)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Map<String,Object>> result = escortTaskFacade.refreshTaskQuality(playerId, targetQuality, refreshTimes, autoBuyRefreshBook);
		if(result.getResult() == TaskConstant.SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			response.setValue(result.getResult());
			session.write(response);
		}
	}
	
	
	
	protected void completeAndRewardEscortTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int npcId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.NPCID)) {
				npcId = ((Number)aso.get(ResponseKey.NPCID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<UserEscortTask> result = escortTaskFacade.completeAndreward(playerId, npcId);
		if(result.getResult() == TaskConstant.SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			response.setValue(result.getResult());
			session.write(response);
		}
		
	}
	
	

	protected void giveUpEscortTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		ResultObject<UserEscortTask> result = escortTaskFacade.giveup(playerId);
		if(result.getResult() == TaskConstant.SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			response.setValue(result.getResult());
			session.write(response);
		}
	}
	
	
	protected void acceptEscortTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int taskId = 0;
		long propsId = 0;
		boolean autoBuy = false;
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.TASKID)) {
				taskId = ((Number)aso.get(ResponseKey.TASKID)).intValue();
			}
			if(aso.containsKey(ResponseKey.PROPS_ID)) {
				propsId = ((Number)aso.get(ResponseKey.PROPS_ID)).intValue();
			}
			if(aso.containsKey(ResponseKey.AUTO)) {
				autoBuy = (Boolean)aso.get(ResponseKey.AUTO);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		
		ResultObject<UserEscortTask> result = escortTaskFacade.accept(playerId,taskId,propsId,autoBuy);
		if(result.getResult() == TaskConstant.SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			response.setValue(result.getResult());
			session.write(response);
		}
	}
	
	
	
	protected void escortDutiesInfo(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		boolean flushable = false;
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.FLUSHABLE)) {
				flushable = (Boolean)aso.get(ResponseKey.FLUSHABLE);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<UserEscortTask> result = escortTaskFacade.loadUserEscortTask(playerId,flushable);
		if(result.getResult() == TaskConstant.SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			response.setValue(result.getResult());
			session.write(response);
		}
		
	}
	
	
	protected void submitCamptask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int taskId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.TASKID)) {
				taskId = ((Number)aso.get(ResponseKey.TASKID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		campTaskFacade.complete(playerId, taskId); 
		int result = campTaskFacade.rewards(playerId, taskId);
		if(result == TaskConstant.SUCCESS){
			response.setValue(taskId);
			session.write(response);
		}else{
			response.setValue(result);
			session.write(response);
		}

		
	}
	
	
	protected void giveupCampTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int taskId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.TASKID)) {
				taskId = ((Number)aso.get(ResponseKey.TASKID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = campTaskFacade.cancel(playerId, taskId);
		Map<String,Object> map = new HashMap<String, Object>(2);
		if(result == TaskConstant.SUCCESS){
			map.put(ResponseKey.RESULT, result);
			map.put(ResponseKey.TASKID, taskId);
			response.setValue(map);
		}else{
			map.put(ResponseKey.RESULT, result);
			response.setValue(map);
		}
		
		session.write(response);
	}
	
	
	protected void completeCampTaskTalk(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int taskId = 0;
		int npcId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.TASKID)) {
				taskId = ((Number)aso.get(ResponseKey.TASKID)).intValue();
			}
			if(aso.containsKey(ResponseKey.NPCID)) {
				npcId = ((Number)aso.get(ResponseKey.NPCID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<CampTask> result = this.campTaskFacade.completeTalkTask(playerId, npcId, taskId);
		if(result.getResult() == TaskConstant.SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			response.setValue(result.getResult());
			session.write(response);
		}
	}
	
	
	protected void loadCampTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		ResultObject<Map<String, Object>> result = campTaskFacade.getCampTaskResult(playerId);
		if(result.getResult() == TaskConstant.SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			response.setValue(result.getResult());
			session.write(response);
		}
	}
	
	
	protected void acceptCampTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int taskId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.TASKID)) {
				taskId = ((Number)aso.get(ResponseKey.TASKID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<CampTask> result = campTaskFacade.accept(playerId, taskId);
		if(result.getResult() == TaskConstant.SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			response.setValue(result.getResult());
			session.write(response);
		}
	}
	
	
	
	
	
	protected void getPracticeReward(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int completeTimes = -1 ;
		
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.COMPLETE_TIMES)) {
				completeTimes = ((Number)aso.get(ResponseKey.COMPLETE_TIMES)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		ResultObject<PracticeTaskVO> rewardResult = practiceTaskFacade.getReward(playerId, completeTimes);
		resultMap.put(ResponseKey.RESULT, rewardResult.getResult());
		resultMap.put(ResponseKey.PRACTICE_TASK_VO, rewardResult.getValue());
		response.setValue(resultMap);
		session.write(response);
	}


	protected void getReward(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int completeTimes = -1 ;
	
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.COMPLETE_TIMES)) {
				completeTimes = ((Number)aso.get(ResponseKey.COMPLETE_TIMES)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		ResultObject<LoopTaskVo> rewardResult = loopFacade.getReward(playerId,completeTimes);
		resultMap.put(ResponseKey.RESULT, rewardResult.getResult());
		resultMap.put(ResponseKey.LOOP_TASK_VO, rewardResult.getValue());
		response.setValue(resultMap);
		session.write(response);
	}

	
	protected void giveUpLoopTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int result = CommonConstant.FAILURE;
		response.setValue(result);
		session.write(response);
	}
	
	
	protected void giveUpPracticeTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int result = CommonConstant.FAILURE; 
		response.setValue(result);
		session.write(response);
	}

	
	protected void refreshLoopTaskQuality(IoSession session, Request request, Response response) {
		int refreshTimes = 1;
		boolean autoBuyBook = false ;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.AUTOBUY_REFRESHBOOK)) {
				autoBuyBook = ((Boolean)aso.get(ResponseKey.AUTOBUY_REFRESHBOOK));
			}
			if(aso.containsKey(ResponseKey.REFRESH_TIMES)) {
				refreshTimes = ((Number)aso.get(ResponseKey.REFRESH_TIMES)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Map<String, Object> result = new HashMap<String, Object>(4);
		QualityResult<LoopTaskVo> refreshObject = loopFacade.refreshTaskQuality(playerId, refreshTimes, autoBuyBook);
		result.put(ResponseKey.USE_GOLD, refreshObject.getUseGold());
		result.put(ResponseKey.USE_BOOKS, refreshObject.getUseBooks());
		result.put(ResponseKey.RESULT, refreshObject.getResult());
		result.put(ResponseKey.LOOP_TASK_VO, refreshObject.getEntityVO());
		response.setValue(result);
		session.write(response);
	}

	
	protected void refreshPracticeTaskQuality(IoSession session, Request request, Response response) {
		int refreshTimes = 200;
		boolean autoBuyRefreshBook = false ;
		int targetQuality = Quality.PURPLE.ordinal();
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.TARGET_QUALITY)) {
				targetQuality = ((Number)aso.get(ResponseKey.TARGET_QUALITY)).intValue();
			}
			if(aso.containsKey(ResponseKey.AUTOBUY_REFRESHBOOK)) {
				autoBuyRefreshBook = ((Boolean)aso.get(ResponseKey.AUTOBUY_REFRESHBOOK));
			}
			if(aso.containsKey(ResponseKey.REFRESH_TIMES)) {
				refreshTimes = ((Number)aso.get(ResponseKey.REFRESH_TIMES)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Map<String, Object> result = new HashMap<String, Object>(4);
		QualityResult<PracticeTaskVO> refreshObject = practiceTaskFacade.refreshQuality(playerId, 
													targetQuality, refreshTimes, autoBuyRefreshBook);
		result.put(ResponseKey.USE_GOLD, refreshObject.getUseGold());
		result.put(ResponseKey.USE_BOOKS, refreshObject.getUseBooks());
		result.put(ResponseKey.RESULT, refreshObject.getResult());
		result.put(ResponseKey.PRACTICE_TASK_VO, refreshObject.getEntityVO());
		response.setValue(result);
		session.write(response);
	}

	protected void completeTalkUserTask(IoSession session, Request request, Response response) {
		int npcId = 0;
		long userTaskId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.NPCID)) {
				npcId = ((Number)aso.get(ResponseKey.NPCID)).intValue();
			}
			if(aso.containsKey(ResponseKey.USER_TASK_ID)) {
				userTaskId = ((Number)aso.get(ResponseKey.USER_TASK_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
	
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		ResultObject<UserTask> resultObject = taskFacade.completeTalkTask(playerId, npcId, userTaskId);
		resultMap.put(ResponseKey.RESULT, resultObject.getResult());
		if(resultObject.getValue() != null) {
			resultMap.put(ResponseKey.USER_TASK, resultObject.getValue());
		}
		response.setValue(resultMap);
		session.write(response);
	}

	
	protected void completeTalkUserMapTask(IoSession session, Request request, Response response) {
		int npcId = 0;
		long userTaskId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.NPCID)) {
				npcId = ((Number)aso.get(ResponseKey.NPCID)).intValue();
			}
			if(aso.containsKey(ResponseKey.USER_TASK_ID)) {
				userTaskId = ((Number)aso.get(ResponseKey.USER_TASK_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		ResultObject<UserMapTask> resultObject = mapTaskFacade.completeTalkTask(playerId, npcId, userTaskId);
		resultMap.put(ResponseKey.RESULT, resultObject.getResult());
		if(resultObject.getValue() != null) {
			resultMap.put(ResponseKey.USER_TASK, resultObject.getValue());
		}
		response.setValue(resultMap);
		session.write(response);
	}

	
	protected void queryLoopTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		LoopTaskVo loopTaskVO = loopFacade.refreshUserLoopTask(playerId);
		Map<String,Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.LOOP_TASK_VO, loopTaskVO);
		resultMap.put(ResponseKey.STATE, loopTaskVO != null && loopTaskVO.isCanAcceptd());
		response.setValue(resultMap);
		session.write(response);
	}


	protected void queryPracticeTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		PracticeTaskVO practiceTaskVO = practiceTaskFacade.refreshPracticeTask(playerId);
		Map<String,Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.PRACTICE_TASK_VO, practiceTaskVO);
		resultMap.put(ResponseKey.STATE, practiceTaskVO != null && practiceTaskVO.isCanAcceptd());
		response.setValue(resultMap);
		session.write(response);
	}

	
	protected void acceptLoopTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		ResultObject<LoopTaskVo> resultObject = loopFacade.accept(playerId);
		Map<String,Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, resultObject.getResult());
		resultMap.put(ResponseKey.LOOP_TASK_VO, resultObject.getValue());
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void acceptPracticeTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		ResultObject<PracticeTaskVO> resultObject = practiceTaskFacade.accept(playerId);
		Map<String,Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, resultObject.getResult());
		resultMap.put(ResponseKey.PRACTICE_TASK_VO, resultObject.getValue());
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void fastCompleteLookTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		ResultObject<LoopTaskVo> resultObject = loopFacade.fastComplete(playerId);
		Map<String,Object> result = new HashMap<String, Object>(2);
		result.put(ResponseKey.RESULT, resultObject.getResult());
		result.put(ResponseKey.LOOP_TASK_VO, resultObject.getValue());
		response.setValue(result);
		session.write(response);
	}

	
	protected void rewardLoopTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		ResultObject<Collection<BackpackEntry>> resultObject = loopFacade.complete(playerId);
		ResultObject<LoopTaskVo> rewardsObject = loopFacade.rewardsUserLoopTask(playerId);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		int result = rewardsObject.getResult();
		resultMap.put(ResponseKey.RESULT, result);
		resultMap.put(ResponseKey.LOOP_TASK_VO, rewardsObject.getValue());
		response.setValue(resultMap);
		session.write(response);
		pushBackpackEntriesAndAttribute(playerId, resultObject.getValue());
	}
 
	
	protected void rewardPracticeTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		ResultObject<PracticeTaskVO> rewardsObject = practiceTaskFacade.rewardUserPracticeTask(playerId);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		int result = rewardsObject.getResult();
		resultMap.put(ResponseKey.RESULT, result);
		resultMap.put(ResponseKey.PRACTICE_TASK_VO, rewardsObject.getValue());
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	private void pushBackpackEntriesAndAttribute(long playerId, Collection<BackpackEntry> backpackEntries, Object...attributes) {
		if(backpackEntries != null && !backpackEntries.isEmpty()) {
			int backpack = BackpackType.DEFAULT_BACKPACK;
			MessagePushHelper.pushUserProps2Client(playerId, backpack, false, backpackEntries);
		}
		
		if(attributes.length > 0) {
			UnitId unitId = UnitId.valueOf(playerId, ElementType.PLAYER);
			UserPushHelper.pushAttribute2AreaMember(playerId, Arrays.asList(playerId), Arrays.asList(unitId), attributes);
		}
	}
	
	
	protected void completeLookTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		ResultObject<Collection<BackpackEntry>> resultObject = loopFacade.complete(playerId);
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, resultObject.getResult());
		resultMap.put("userLoopTask", loopFacade.refreshUserLoopTask(playerId));
		response.setValue(resultMap);
		session.write(response);
		pushBackpackEntriesAndAttribute(playerId, resultObject.getValue());
	}
	

	protected void completeTalkLoopTask(IoSession session, Request request, Response response) {
		int npcId = 0;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.NPCID)) {
				npcId = ((Number)aso.get(ResponseKey.NPCID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<LoopTaskVo> resultObject = loopFacade.completeTalkTask(playerId, npcId);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, resultObject.getResult());
		resultMap.put("loopTaskVo", resultObject.getValue());
		response.setValue(resultMap);
		session.write(response);
	}

	
	protected void collect(IoSession session, Request request, Response response) {
		int npcId = 0;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.NPCID)) {
				npcId = ((Number) aso.get(ResponseKey.NPCID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		ResultObject<int[]> resultObject = taskMainFacade.collect(playerId, npcId);
		int[] value = resultObject.getValue();
		int result = resultObject.getResult();
		resultMap.put(ResponseKey.RESULT, result);
		if(value != null) {
			resultMap.put(ResponseKey.BASEID, value[0]);
			resultMap.put(ResponseKey.COUNT, value[1]);
		}
		
		response.setValue(resultMap);
		session.write(response);
	}


	protected void listUserTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		Object[] completeIdArray = taskFacade.getTaskCompleteIds(playerId).toArray();
		Collection<UserTask> userTasks = taskFacade.listFilterUserTask(playerId, TaskStatus.REWARDS,TaskStatus.UNACCEPT);
		resultMap.put(ResponseKey.COMPLETES, completeIdArray);
		resultMap.put(ResponseKey.USER_TASK, userTasks.toArray());
		response.setValue(resultMap);
		session.write(response);
	}

	
	protected void listUserMapTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Object[] completeIdArray = mapTaskFacade.getTaskCompleteIds(playerId).toArray();
		Collection<UserMapTask> userMapTasks = mapTaskFacade.listFilterUserMapTask(playerId, TaskStatus.REWARDS, TaskStatus.UNACCEPT);
		resultMap.put(ResponseKey.COMPLETES, completeIdArray);
		resultMap.put(ResponseKey.USER_TASK, userMapTasks.toArray());
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void queryUserTask(IoSession session, Request request, Response response) {
		long userTaskId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.USER_TASK_ID)) {
				userTaskId = ((Number) aso.get(ResponseKey.USER_TASK_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		UserTask userTask = taskFacade.getUserTask(userTaskId);
		
		response.setValue(userTask);
		session.write(response);
	}
	
	
	protected void queryUserMapTask(IoSession session, Request request, Response response) {
		long userTaskId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.USER_TASK_ID)) {
				userTaskId = ((Number) aso.get(ResponseKey.USER_TASK_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		UserMapTask userTask = mapTaskFacade.getUserMapTask(userTaskId);
		
		response.setValue(userTask);
		session.write(response);
	}

	
	protected void acceptUserTask(IoSession session, Request request, Response response) {
		int taskId = 0;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.TASKID)) {
				taskId = ((Number) aso.get(ResponseKey.TASKID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<TaskResult> acceptResult = taskFacade.accept(playerId, taskId);
		response.setValue(getResultMap(acceptResult));
		session.write(response);
		TaskPushHelper.processPostResult2Client(acceptResult.getValue());
	}

	
	protected void acceptUserMapTask(IoSession session, Request request, Response response) {
		int taskId = 0;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.TASKID)) {
				taskId = ((Number) aso.get(ResponseKey.TASKID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<MapTaskResult> acceptResult = mapTaskFacade.accept(playerId, taskId);
		response.setValue(getMapResultMap(acceptResult));
		session.write(response);
		TaskPushHelper.processPostResult2Client(acceptResult.getValue());
	}
	
	
	private Map<String, Object> getResultMap(ResultObject<TaskResult> resultObject) {
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, resultObject.getResult());
		TaskResult taskResult = resultObject.getValue();
		if(taskResult != null && taskResult.getSingleTask() != null) {
			resultMap.put(ResponseKey.USER_TASK, taskResult.getSingleTask());
		}
		return resultMap;
	}

	
	private Map<String, Object> getMapResultMap(ResultObject<MapTaskResult> resultObject) {
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, resultObject.getResult());
		MapTaskResult taskResult = resultObject.getValue();
		if(taskResult != null && taskResult.getSingleTask() != null) {
			resultMap.put(ResponseKey.USER_TASK, taskResult.getSingleTask());
		}
		return resultMap;
	}

	
	protected void cancelUserTask(IoSession session, Request request, Response response) {
		long userTaskId = 0;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.USER_TASK_ID)) {
				userTaskId = ((Number) aso.get(ResponseKey.USER_TASK_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<TaskResult> cancelResult = taskFacade.cancel(playerId, userTaskId);
		
		response.setValue(getResultMap(cancelResult));
		session.write(response);
		TaskPushHelper.processPostResult2Client(cancelResult.getValue());
	}

	
	protected void cancelUserMapTask(IoSession session, Request request, Response response) {
		long userTaskId = 0;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.USER_TASK_ID)) {
				userTaskId = ((Number) aso.get(ResponseKey.USER_TASK_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<MapTaskResult> cancelResult = mapTaskFacade.cancel(playerId, userTaskId);
		response.setValue(getMapResultMap(cancelResult));
		session.write(response);
		TaskPushHelper.processPostResult2Client(cancelResult.getValue());
	}

	
	protected void completeUserTask(IoSession session, Request request, Response response) {
		long userTaskId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.USER_TASK_ID)) {
				userTaskId = ((Number) aso.get(ResponseKey.USER_TASK_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<TaskResult> completeResult = taskFacade.complete(playerId, userTaskId);
		response.setValue(this.getResultMap(completeResult));
		session.write(response);
		TaskPushHelper.processPostResult2Client(completeResult.getValue());
	}


	protected void completeUserMapTask(IoSession session, Request request, Response response) {
		long userTaskId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.USER_TASK_ID)) {
				userTaskId = ((Number) aso.get(ResponseKey.USER_TASK_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<MapTaskResult> completeResult = mapTaskFacade.complete(playerId, userTaskId);
		response.setValue(this.getMapResultMap(completeResult));
		session.write(response);
		TaskPushHelper.processPostResult2Client(completeResult.getValue());
	}


	protected void rewardUserTask(IoSession session, Request request, Response response) {
		long userTaskId = 0;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.USER_TASK_ID)) {
				userTaskId = ((Number) aso.get(ResponseKey.USER_TASK_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		ResultObject<TaskResult> completeResult = taskFacade.complete(playerId, userTaskId);
		ResultObject<TaskRewardResult> rewardResult = taskFacade.rewards(playerId, userTaskId);
		resultMap.put(ResponseKey.RESULT, rewardResult.getResult());
		TaskRewardResult taskRewardResult = rewardResult.getValue();
		if(taskRewardResult != null) {
			resultMap.put(ResponseKey.LEVEL, taskRewardResult.getLevel());
			resultMap.put(ResponseKey.USER_TASK, taskRewardResult.getUserTask());
		}
		response.setValue(resultMap);
		session.write(response);
		TaskPushHelper.processPostResult2Client(completeResult.getValue());
	}


	protected void rewardUserMapTask(IoSession session, Request request, Response response) {
		long userTaskId = 0;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.USER_TASK_ID)) {
				userTaskId = ((Number) aso.get(ResponseKey.USER_TASK_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		ResultObject<MapTaskResult> completeResult = mapTaskFacade.complete(playerId, userTaskId);
		ResultObject<MapTaskRewardResult> rewardResult = mapTaskFacade.rewards(playerId, userTaskId);
		resultMap.put(ResponseKey.RESULT, rewardResult.getResult());
		MapTaskRewardResult taskRewardResult = rewardResult.getValue();
		if(taskRewardResult != null) {
			resultMap.put(ResponseKey.LEVEL, taskRewardResult.getLevel());
			resultMap.put(ResponseKey.USER_TASK, taskRewardResult.getUserMapTask());
		}
		response.setValue(resultMap);
		session.write(response);
		TaskPushHelper.processPostResult2Client(completeResult.getValue());
	}
	

}
