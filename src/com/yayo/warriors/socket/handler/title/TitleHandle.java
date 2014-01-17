package com.yayo.warriors.socket.handler.title;

import java.util.HashMap;
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
import com.yayo.warriors.module.title.facade.TitleFacade;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;


@Component
public class TitleHandle extends BaseHandler{

	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private TitleFacade titleFacade ;
	
	
	protected int getModule() {
		return Module.TITLE;
	}

	
	protected void inititialize() {
		
		putInvoker(TitleCmd.TITLE_INFO, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				titleInfo(session,request,response);
			}
		});
		putInvoker(TitleCmd.TITLE_USE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				useTitle(session,request,response);
			}
		});
		
		putInvoker(TitleCmd.TITLE_REMOVE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				removeTitle(session,request,response);
			}
		});
	}
	
	
	protected void removeTitle(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, titleFacade.removeTitle(playerId));
		response.setValue(resultMap);
		session.write(response);
	}

	
	protected void useTitle(IoSession session, Request request, Response response) {
		int titleId = 0 ;
		long playerId = sessionManager.getPlayerId(session);
		try{
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TITLE_ID)) {
				titleId = ((Number) aso.get(ResponseKey.TITLE_ID)).intValue();
			}
		}catch(Exception e){
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
		}

		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, titleFacade.useTitle(playerId, titleId));
		response.setValue(resultMap);
		session.write(response);
	}


	public void titleInfo(IoSession session, Request request, Response response){
		long playerId = sessionManager.getPlayerId(session);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, titleFacade.hasTitle(playerId).toArray());
		response.setValue(resultMap);
		session.write(response);
	}
}
