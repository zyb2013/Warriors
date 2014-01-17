package com.yayo.warriors.socket.handler.treasure;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.type.ResponseCode;
import com.yayo.warriors.module.treasure.facade.TreasureFacade;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;

@Component
public class TreasureHandler extends BaseHandler {
	@Autowired
	private TreasureFacade treasureFacade;

	
	protected int getModule() {
		return Module.TREASURE;
	}

	
	protected void inititialize() {
		putInvoker(TreasureCmd.REFRESH_QUALITY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				refreshQuality(session, request, response);
			}
		});
		
		putInvoker(TreasureCmd.DIG_TREANSURE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				digTreasure(session, request, response);
			}
		});
		
		putInvoker(TreasureCmd.OPEN_TREANSURE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				Long playerId = sessionManager.getPlayerId(session);
				int npcId = 0;
				ASObject params = (ASObject)request.getValue();
				try {
					if(params.containsKey(ResponseKey.NPCID)){
						npcId = ( (Number)params.get(ResponseKey.NPCID) ).intValue();
					}
				} catch (Exception e) {
					response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
					session.write(response);
					return ;
				}
				if(playerId != null){
					Map<String, Object> resultMap = new HashMap<String, Object>(3);
					int result = treasureFacade.rewardTreasure(playerId, npcId, resultMap);
					resultMap.put(ResponseKey.RESULT, result);
					
					response.setValue(resultMap);
					session.write(response);
				}
			}
		});
		
		putInvoker(TreasureCmd.EXIST_TREANSURE_MAP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				Long playerId = sessionManager.getPlayerId(session);
				if(playerId != null){
					int result = treasureFacade.existTreansureMap(playerId);
					response.setValue(result);
					session.write(response);
				}
			}
		});
	}

	private void refreshQuality(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		long userPropsId = 0;
		int quality = 0;
		ASObject params = (ASObject)request.getValue();
		try {
			if(params.containsKey(ResponseKey.USER_PROPSID)){
				userPropsId = ( (Number)params.get(ResponseKey.USER_PROPSID) ).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return ;
		}
			
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		int result = treasureFacade.refreshQuality(playerId, userPropsId, quality, resultMap);
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		session.write(response);
	}

	private void digTreasure(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		long userPropsId = 0;
		long digUserPropsId = 0;
		ASObject params = (ASObject)request.getValue();
		try {
			if(params.containsKey(ResponseKey.REWARD_ID)){
				userPropsId = ( (Number)params.get(ResponseKey.REWARD_ID) ).longValue();
			}
			if(params.containsKey(ResponseKey.USER_PROPSID)){
				digUserPropsId = ( (Number)params.get(ResponseKey.USER_PROPSID) ).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return ;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		int result = treasureFacade.digTreansure(playerId, userPropsId, digUserPropsId, resultMap);
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		session.write(response);
	}

}
