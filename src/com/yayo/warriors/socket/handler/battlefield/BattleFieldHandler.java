package com.yayo.warriors.socket.handler.battlefield;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.battlefield.facade.BattleFieldFacade;
import com.yayo.warriors.module.battlefield.vo.CollectTaskVO;
import com.yayo.warriors.module.campbattle.constant.CampBattleConstant;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.vo.ChangeScreenVo;

import flex.messaging.io.amf.ASObject;

@Component
public class BattleFieldHandler extends BaseHandler {
	@Autowired
	private BattleFieldFacade battleFieldFacade;

	
	protected int getModule() {
		return Module.BATTLE_FIELD;
	}

	
	protected void inititialize() {
		putInvoker(BattleFieldCmd.ENTER_BATTLEFIELD, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				Long playerId = sessionManager.getPlayerId(session);
				
				Map<String, Object> resultMap = new HashMap<String, Object>(3);
				int result = battleFieldFacade.enterBattleField(playerId, resultMap);
				resultMap.put(ResponseKey.RESULT, result );
				response.setValue(resultMap);
				
				session.write(response);
			}
		});
		
		putInvoker(BattleFieldCmd.EXIT_BATTLE_FIELD, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				Long playerId = sessionManager.getPlayerId(session);
				
				ResultObject<ChangeScreenVo> resultObject = battleFieldFacade.exitBattleField(playerId);
				Map<String, Object> resultMap = new HashMap<String, Object>(2);
				resultMap.put(ResponseKey.RESULT, resultObject.getResult() );
				resultMap.put("changeScreenVO", resultObject.getValue());
				response.setValue(resultMap);
				
				session.write(response);
			}
		});
		
		putInvoker(BattleFieldCmd.REWARD, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				Long playerId = sessionManager.getPlayerId(session);
				
				int result = battleFieldFacade.reward(playerId);
				response.setValue(result);
				
				session.write(response);
			}
		});
		
		putInvoker(BattleFieldCmd.ACCEPT_COLLECT_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				Long playerId = sessionManager.getPlayerId(session);
				
				int result = battleFieldFacade.acceptCollectTask(playerId);
				response.setValue(result);
				
				session.write(response);
			}
		});
		
		putInvoker(BattleFieldCmd.REWARD_COLLECT_TASK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				Long playerId = sessionManager.getPlayerId(session);
				
				ASObject aso = (ASObject)request.getValue();
				long userPropsId = aso != null && aso.containsKey(ResponseKey.USER_PROPSID) ? ((Number)aso.get(ResponseKey.USER_PROPSID)).longValue() : 0;
				int result = battleFieldFacade.rewardCollectTask(playerId, userPropsId);
				
				response.setValue(result);
				session.write(response);
			}
		});
		
		putInvoker(BattleFieldCmd.BATTLE_REQUEST_CMD, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				Map<String, Object> resultMap = new HashMap<String, Object>(4);
				ASObject aso = (ASObject)request.getValue();
				int result = CampBattleConstant.FAILURE;
				if(aso != null){
					Long playerId = sessionManager.getPlayerId(session);
					int type = aso.containsKey(ResponseKey.TYPE) ? ((Number)aso.get(ResponseKey.TYPE)).intValue() : 0;
					result = battleFieldFacade.battleRequestCmd(playerId, type, resultMap);
					resultMap.put(ResponseKey.TYPE, type);
				}
				resultMap.put(ResponseKey.RESULT, result);
				response.setValue(resultMap);
				
				session.write(response);
			}
		});
	}

}
