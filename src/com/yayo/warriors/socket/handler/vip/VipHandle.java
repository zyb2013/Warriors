package com.yayo.warriors.socket.handler.vip;

import static com.yayo.common.socket.type.ResponseCode.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.vip.facade.VipFacade;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;


@Component
public class VipHandle extends BaseHandler {
	
	
	@Autowired
	private VipFacade vipFacade;

	
	protected int getModule() {
		return Module.VIP;
	}

	
	protected void inititialize() {

		putInvoker(VipCmd.OBTAIN_VIP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				obtainVip(session, request, response);
			}
		});
		
		
		putInvoker(VipCmd.LOAD_VIP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadVIP(session, request, response);
			}
		});
		
		
		putInvoker(VipCmd.VIP_REWARD, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				vipReward(session, request, response);
			}
		});
	}

	
	protected void obtainVip(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int baseId = 0;
		long userItemId = -1L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.USER_PROPSID)) {
				userItemId = ((Number) aso.get(ResponseKey.USER_PROPSID)).longValue();
			}
			if (aso.containsKey(ResponseKey.BASEID)) {
				baseId = ((Number) aso.get(ResponseKey.BASEID)).intValue();
			}
		} catch(Exception ex) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	
	protected void loadVIP(IoSession session, Request request, Response response) {
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, CommonConstant.FAILURE);
		response.setValue(resultMap);
		session.write(response);
	}
	
	

	protected void vipReward(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int rewardType = -1;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TYPE)) {
				rewardType = ((Number) aso.get(ResponseKey.TYPE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = vipFacade.vipReward(playerId, rewardType);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		
		response.setValue(resultMap);
		session.write(response);
	}
}
