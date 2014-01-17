package com.yayo.warriors.socket.handler.flyshoes;

import static com.yayo.common.socket.type.ResponseCode.*;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.module.flyshoes.facade.FlyShoesFacade;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;

@Component
public class FlyShoesHandler extends BaseHandler {

	@Autowired
	private FlyShoesFacade flyShoesFacade;
	@Autowired
	private SessionManager sessionManager;
	
	
	protected int getModule() {
		return Module.FLYSHOES;
	}

	
	protected void inititialize() {
		putInvoker(FlyShoesCmd.USE_FLY_SHOES,  new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				useFlyShoes(session, request, response);				
			}
		});
	}
	

	protected void useFlyShoes(IoSession session, Request request, Response response){
		long playerId = sessionManager.getPlayerId(session);
		long propsId = 0;
		int mapId = 0;
		int x = 0;
		int y = 0;
		
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.PROPS_ID)){
				propsId = ((Number) aso.get(ResponseKey.PROPS_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.MAPID)) {
				mapId = ((Number) aso.get(ResponseKey.MAPID)).intValue();
			}
			if(aso.containsKey(ResponseKey.X)) {
				x = ((Number) aso.get(ResponseKey.X)).intValue();
			}
			if(aso.containsKey(ResponseKey.Y)) {
				y = ((Number) aso.get(ResponseKey.Y)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = flyShoesFacade.useFlyShoes(playerId, propsId, mapId, x, y);
		response.setValue(result);
		session.write(response);
	}

}
