package com.yayo.warriors.socket.handler.train;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.type.ResponseCode;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.onhook.facade.TrainFacade;
import com.yayo.warriors.module.onhook.vo.TrainVo;
import com.yayo.warriors.module.search.vo.CommonSearchVo;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;

@Component
public class TrainHandle extends BaseHandler {

	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private TrainFacade trainFacade;

	
	protected int getModule() {
		return Module.TRAIN;
	}

	
	protected void inititialize() {
		putInvoker(TrainCmd.LOAD_TRAIN_INFO, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadTrainInfo(session, request, response);
			}
		});

		putInvoker(TrainCmd.START_TRAIN, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				startTrain(session, request, response);
			}
		});

		putInvoker(TrainCmd.RECEIVE_TRAIN_REWARD, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				receiveReward(session, request, response);
			}
		});

		putInvoker(TrainCmd.START_SINGLE_TRAIN, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				startSingleTrain(session, request, response);
			}
		});

		putInvoker(TrainCmd.RECEIVE_AWARD, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				receiveAward(session, request, response);
			}
		});

		putInvoker(TrainCmd.INVITE_COUPLE_TRAIN, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				inviteCoupleTrain(session, request, response);
			}
		}); 
			
		putInvoker(TrainCmd.REJECT_COUPLE_TRAIN, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				rejectCoupleTrain(session, request, response);
			}
		});
		
		putInvoker(TrainCmd.ACCEPT_COUPLE_TRAIN, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				acceptCoupleTrain(session, request, response);
			}
		});
		
		putInvoker(TrainCmd.SEARCH_COUPLE_PLAYER, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				searchTrainPlayer(session, request, response);
			}
		});
		
		putInvoker(TrainCmd.SAVE_DIRECTION, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				saveDirection(session, request, response);
			}
		});
		
	}

	protected void loadTrainInfo(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		ResultObject<TrainVo> result = trainFacade.loadClosedInfo(playerId);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, result.getResult());
		resultMap.put(ResponseKey.VALUES, result.getValue());
		response.setValue(resultMap);
		session.write(response);
	}

	protected void startTrain(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		int result = trainFacade.startTrain(playerId);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		session.write(response);
	}

	protected void receiveReward(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int propsId = 0;
		int multiple = 0;;
		int autoBuyCount = -1;
		String userItems = "";
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.USER_PROPS)) {
				userItems = (String) aso.get(ResponseKey.USER_PROPS);
			}
			if (aso.containsKey(ResponseKey.PROPS_ID)) {
				propsId = ((Number) aso.get(ResponseKey.PROPS_ID)).intValue();
			}
			if (aso.containsKey(ResponseKey.MULTIPLE)) {
				multiple = ((Number) aso.get(ResponseKey.MULTIPLE)).intValue();
			}
			if (aso.containsKey(ResponseKey.AUTO_BUY_COUNT)) {
				autoBuyCount = ((Number) aso.get(ResponseKey.AUTO_BUY_COUNT)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = trainFacade.receiveReward(playerId, userItems, propsId, multiple, autoBuyCount);
		
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		session.write(response);
	}

	protected void startSingleTrain(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		int result = trainFacade.processSingleTrain(playerId);
		response.setValue(result);
		session.write(response);
	}

	protected void receiveAward(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);

		int result = trainFacade.receiveAward(playerId);
		response.setValue(result);
		session.write(response);
	}

	
	protected void inviteCoupleTrain(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = trainFacade.inviteCoupleTrain(playerId, targetId);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void rejectCoupleTrain(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = trainFacade.rejectCoupleTrain(playerId, targetId);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void acceptCoupleTrain(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = trainFacade.acceptCoupleTrain(playerId, targetId);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void searchTrainPlayer(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		String keywords = "";
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.KEYWORDS)) {
				keywords = (String) aso.get(ResponseKey.KEYWORDS);
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Collection<CommonSearchVo> result = trainFacade.getSearchPlayer(playerId, keywords);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, CommonConstant.SUCCESS);
		resultMap.put(ResponseKey.VALUES, result.toArray());

		response.setValue(resultMap);
		session.write(response);
	}
	
	protected void saveDirection(IoSession session, Request request, Response response) {
		int direction = 0;
		long playerId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.DIRECTION)) {
				direction = ((Number) aso.get(ResponseKey.DIRECTION)).intValue();
			}
			if (aso.containsKey(ResponseKey.PLAYER_ID)) {
				playerId = ((Number) aso.get(ResponseKey.PLAYER_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		trainFacade.savePlayerDirection(playerId, direction);
	}
}
