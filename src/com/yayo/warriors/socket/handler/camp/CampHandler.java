package com.yayo.warriors.socket.handler.camp;

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
import com.yayo.warriors.module.camp.facade.CampFacade;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;


@Component
public class CampHandler extends BaseHandler {

	@Autowired
	private CampFacade campFacade;
	
	
	protected int getModule() {
		return Module.CAMP;
	}

	
	protected void inititialize() {
		putInvoker(CampCmd.JOIN_CAMP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				jionCamp(session,request,response);
			}
		});
	}
	
	
	protected void jionCamp(IoSession session, Request request, Response response){
		long playerId = sessionManager.getPlayerId(session);
		int campValue = 0;
		
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.CAMP_VALUE)) {
				campValue = ((Number) aso.get(ResponseKey.CAMP_VALUE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = this.campFacade.joinCamp(playerId, campValue);
		Map<String,Object> map = new HashMap<String,Object>(1);
		map.put(ResponseKey.RESULT, result);
		response.setValue(map);
		session.write(response);
		
	}

}
