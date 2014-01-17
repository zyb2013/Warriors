package com.yayo.warriors.socket.handler.chat;

import static com.yayo.common.socket.type.ResponseCode.*;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.module.chat.facade.ChatFacade;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;


@Component
public class ChatHandler extends BaseHandler {

	@Autowired
	private ChatFacade chatFacade;
	
	
	protected int getModule() {
		return Module.CHAT;
	}

	
	protected void inititialize() {
		putInvoker(ChatCmd.CHAT_SEND, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				doChatSend(session, request, response);
			}
		});
	}


	public void doChatSend(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int channel = 0;
		String chatInfo = "";
		String targetName = "";
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.CHANNEL)) {
				channel = ((Number)aso.get(ResponseKey.CHANNEL)).intValue();
			}
			if(aso.containsKey(ResponseKey.TARGET_NAME)) {
				targetName = (String)aso.get(ResponseKey.TARGET_NAME);
			}
			if(aso.containsKey(ResponseKey.CHAT_INFO)) {
				chatInfo = (String) aso.get(ResponseKey.CHAT_INFO);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = chatFacade.doPlayerChat(playerId, channel, chatInfo, targetName);
		response.setValue(result);
		session.write(response);
	}
}
