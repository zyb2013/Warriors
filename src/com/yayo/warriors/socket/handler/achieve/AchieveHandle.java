package com.yayo.warriors.socket.handler.achieve;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.type.ResponseCode;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.achieve.facade.AchieveFacade;
import com.yayo.warriors.module.achieve.vo.AchieveVO;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;


@Component
public class AchieveHandle extends BaseHandler {

	@Autowired
	private AchieveFacade achieveFacade;
	@Autowired
	private SessionManager sessionManager;
	
	
	
	protected int getModule() {
		return Module.ACHIEVE;
	}

	
	protected void inititialize() {
		
		putInvoker(AchieveCmd.LOAD_ALL_ACHIEVED, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadAllAchieved(session, request, response);
			}
		});
		
		putInvoker(AchieveCmd.RECEIVE_ACHIEVED, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				receiveAchieved(session, request, response);
			}
		});
		
		putInvoker(AchieveCmd.ONLINE_ACHIEVED, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				onlineAchieved(session, request, response);
			}
		});
		
		
		putInvoker(AchieveCmd.LOAD_TYPE_ACHIEVED, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadTypeAchieved(session, request, response);
			}
		});
		
		
		putInvoker(AchieveCmd.RECEIVE_REWARDS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				receiveRewards(session, request, response);
			}
		});
		
		
		putInvoker(AchieveCmd.LOGIN_RECEIVED, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loginAchieved(session, request, response);
			}
		});
	}
	
	protected void loadAllAchieved(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		AchieveVO achieveVO = achieveFacade.getAllAchieves(playerId);
		
		List<Integer> achieveIds = achieveVO.getIds();
		achieveVO.setAchievedIds(achieveIds.toArray());
		
		List<Integer> nonReceveIds = achieveFacade.getNonReceivedIds(playerId);
		achieveVO.setNonReceivedId(nonReceveIds.toArray());
		
		achieveIds.removeAll(nonReceveIds);
		achieveVO.setReceivedId(achieveIds.toArray());        
		
		response.setValue(achieveVO);
		session.write(response);
	}
	
	
	protected void receiveAchieved(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int achieveId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.ID)) {
				achieveId = ((Number) aso.get(ResponseKey.ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = achieveFacade.receiveAchieveReward(playerId, achieveId);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, result);
		resultMap.put(ResponseKey.ID, achieveId);
		response.setValue(resultMap);
		session.write(response);
	}
	
	

	protected void onlineAchieved(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		achieveFacade.onlineAchieved(playerId);
	}
	
	

	protected void loadTypeAchieved(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int achieveType = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TYPE)) {
				achieveType = ((Number) aso.get(ResponseKey.TYPE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		List<Integer> result = achieveFacade.listAchievesByType(playerId, achieveType);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.VALUES, result.toArray());
		
		int value = achieveFacade.getAchieveValue(playerId, achieveType);
		if (value > -1) {
			resultMap.put(ResponseKey.COUNT, value);
		}
		
		response.setValue(resultMap);
		session.write(response);
	}
	

	protected void receiveRewards(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		List<Integer> result = achieveFacade.receiveRewards(playerId);
		if (result == null) {
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, CommonConstant.SUCCESS);
		resultMap.put(ResponseKey.IDS, result.toArray());
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	
	protected void loginAchieved(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		achieveFacade.checkLoginAchieved(playerId);
	}
	
}
