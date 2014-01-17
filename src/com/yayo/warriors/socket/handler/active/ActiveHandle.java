package com.yayo.warriors.socket.handler.active;

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
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.active.constant.ActiveConstant;
import com.yayo.warriors.module.active.entity.OperatorActive;
import com.yayo.warriors.module.active.facade.ActiveFacade;
import com.yayo.warriors.module.active.facade.ActiveOperatorFacade;
import com.yayo.warriors.module.active.manager.ActiveOperatorManager;
import com.yayo.warriors.module.active.rule.ActiveOperatorType;
import com.yayo.warriors.module.active.vo.ActiveBossRefreshVO;
import com.yayo.warriors.module.active.vo.ActiveDungeonVO;
import com.yayo.warriors.module.active.vo.ActiveTaskVO;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;


@Component
public class ActiveHandle extends BaseHandler {
	
	@Autowired
	private ActiveOperatorFacade activeOperatorFacade;
	@Autowired
	private ActiveOperatorManager activeOperatorManager;
	@Autowired
	private ActiveFacade activeFacade;
	@Autowired
	private SessionManager sessionManager;
	
	
	protected int getModule() {
		return Module.ACTIVE;
	}

	
	protected void inititialize() {
		
		putInvoker(ActiveCmd.BOSS_REFRESH_ACTIVE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				bossRefresh(session, request, response);
			}
		});
			
		putInvoker(ActiveCmd.DAILY_TASK_ACTIVE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				dailyTask(session, request, response);
			}
		});
		
		
		putInvoker(ActiveCmd.DAILY_DUNGEON_ACTIVE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				dailyDungeon(session, request, response);
			}
		});
		
		///运营活动
		
		putInvoker(ActiveCmd.SUBLIS_OPERATOR_ACTIVE,new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				sublisOperatorActive(session, request, response);
			}
		});
		
		putInvoker(ActiveCmd.REWARD_RANK_ACTIVE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				rewardRankActive(session, request, response);
			}
		});
		
		putInvoker(ActiveCmd.REWARD_LEVEL_ACTIVE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				rewardLevelActive(session, request, response);
			}
		});
		
		putInvoker(ActiveCmd.REWARD_EXCHANGE_ACTIVE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				rewardExchangeActive(session, request, response);
			}
		});
		
		putInvoker(ActiveCmd.CLIENT_REWARD_VIRIFI, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				clientRewardVirifi(session, request, response);
			}
		});
	}
	

	protected void rewardExchangeActive(IoSession session, Request request, Response response){
		long playerId = sessionManager.getPlayerId(session);
		long aliveActiveId = 0;
		int rewardId = 0;
		String items = "";
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.REWARD_ID)) {
				rewardId = ((Number)aso.get(ResponseKey.REWARD_ID)).intValue();
			}
			if(aso.containsKey(ResponseKey.ALIVE_ACTIVE_ID)) {
				aliveActiveId = ((Number)aso.get(ResponseKey.ALIVE_ACTIVE_ID)).intValue();
			}
			
			if(aso.containsKey(ResponseKey.USER_PROPSID)) {
				items = (String)aso.get(ResponseKey.USER_PROPSID);
			}
			
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Integer> result = activeOperatorFacade.rewardExChange(playerId, aliveActiveId, rewardId, items);
		Map<String, Object> resultObject = new HashMap<String, Object>(4);
		resultObject.put(ResponseKey.RESULT, result.getResult());
		if(result.isOK()){
			resultObject.put(ResponseKey.TYPE, ActiveOperatorType.EXCHANGE);
			resultObject.put(ResponseKey.REWARD_ID, rewardId);
			resultObject.put(ResponseKey.COUNT, result.getValue());
		}
		
		response.setValue(resultObject);
		session.write(response);
		
	}
	
	protected void clientRewardVirifi(IoSession session, Request request, Response response){
		long playerId = sessionManager.getPlayerId(session);
		int type = 0;
		long aliveActiveId = 0;
		String info = "";
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.INFO)) {
				info = (String)aso.get(ResponseKey.INFO);
			}
			if(aso.containsKey(ResponseKey.TYPE)) {
				type = ((Number)aso.get(ResponseKey.TYPE)).intValue();
			}
			
			if(aso.containsKey(ResponseKey.ALIVE_ACTIVE_ID)) {
				aliveActiveId = ((Number)aso.get(ResponseKey.ALIVE_ACTIVE_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		String resultInfo = activeOperatorFacade.clientRewardActiveVrifi(playerId, aliveActiveId, type, info);
		HashMap<String, Object> resultObject = new HashMap<String, Object>(3);
		resultObject.put(ResponseKey.RESULT, ActiveConstant.SUCCESS);
		resultObject.put(ResponseKey.TYPE, type);
		resultObject.put(ResponseKey.INFO, resultInfo);
		response.setValue(resultObject);
		session.write(response);
	}
	
	
	protected void rewardLevelActive(IoSession session, Request request, Response response){
		long playerId = sessionManager.getPlayerId(session);
		long aliveActiveId = 0;
		int rewardId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.REWARD_ID)) {
				rewardId = ((Number)aso.get(ResponseKey.REWARD_ID)).intValue();
			}
			if(aso.containsKey(ResponseKey.ALIVE_ACTIVE_ID)) {
				aliveActiveId = ((Number)aso.get(ResponseKey.ALIVE_ACTIVE_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = activeOperatorFacade.rewardLevelActive(playerId, aliveActiveId, rewardId);
		Map<String, Object> resultObject = new HashMap<String, Object>(1);
		resultObject.put(ResponseKey.RESULT, result);
		if(result == ActiveConstant.SUCCESS){
			resultObject.put(ResponseKey.TYPE, ActiveOperatorType.LEVEL);
			resultObject.put(ResponseKey.REWARD_ID, rewardId);
		}
		response.setValue(resultObject);
		session.write(response);
	}
	

	protected void rewardRankActive(IoSession session, Request request, Response response){
		long playerId = sessionManager.getPlayerId(session);
		long aliveActiveId = 0;
		int rewardId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.REWARD_ID)) {
				rewardId = ((Number)aso.get(ResponseKey.REWARD_ID)).intValue();
			}
			if(aso.containsKey(ResponseKey.ALIVE_ACTIVE_ID)) {
				aliveActiveId = ((Number)aso.get(ResponseKey.ALIVE_ACTIVE_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = activeOperatorFacade.rewardRankActive(playerId, aliveActiveId, rewardId);
		Map<String, Object> resultObject = new HashMap<String, Object>(1);
		resultObject.put(ResponseKey.RESULT, result);
		if(result == ActiveConstant.SUCCESS){
			resultObject.put(ResponseKey.TYPE, ActiveOperatorType.RANKING);
			resultObject.put(ResponseKey.REWARD_ID, rewardId);
		}
		response.setValue(resultObject);
		session.write(response);
	}


	protected void sublisOperatorActive(IoSession session, Request request, Response response){
		int pageStart = 0;
		int pageSize  = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PAGE_START)) {
				pageStart = ((Number)aso.get(ResponseKey.PAGE_START)).intValue();
			}
			if(aso.containsKey(ResponseKey.PAGE_SIZE)) {
				pageSize = ((Number)aso.get(ResponseKey.PAGE_SIZE)).intValue();
			}
			
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int startIndex = pageStart - 1;
		startIndex = startIndex < 0 ? 0 : startIndex; //客户端有+1的操作,这里必须帮忙过滤
		List<OperatorActive> result = activeOperatorFacade.sublistActive(startIndex, pageSize);
		int number = activeOperatorManager.getClientShowActives().size();
		
		Map<String,Object> resultObject = new HashMap<String, Object>(3);
		resultObject.put(ResponseKey.DATA, result.toArray());
		resultObject.put(ResponseKey.PAGE_START, pageStart);
		resultObject.put(ResponseKey.NUMBER, number);
		response.setValue(resultObject);
		session.write(response);
	}

	protected void bossRefresh(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		List<ActiveBossRefreshVO> result = activeFacade.monsterRefreshActive(playerId);
		response.setValue(result.toArray());
		session.write(response);
	}
	
	

	protected void dailyDungeon(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		List<ActiveDungeonVO> result = activeFacade.dailyDungeonActive(playerId);
		response.setValue(result.toArray());
		session.write(response);
	}
	

	protected void dailyTask(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		List<ActiveTaskVO> result = activeFacade.dailyTaskActive(playerId);
		response.setValue(result.toArray());
		session.write(response);
	}
}
