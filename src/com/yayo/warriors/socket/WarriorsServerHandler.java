package com.yayo.warriors.socket;

import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.config.ServerConfig;
import com.yayo.common.socket.firewall.ClientType;
import com.yayo.common.socket.firewall.Firewall;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.type.ResponseCode;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.server.facade.ContainerFacade;
import com.yayo.warriors.module.syscfg.manager.SystemConfigManager;
import com.yayo.warriors.module.user.type.KickCode;
import com.yayo.warriors.socket.handler.user.UserCmd;
import com.yayo.warriors.util.GameConfig;


@Component
public class WarriorsServerHandler implements IoHandler {
	
	@Autowired
	private Firewall firewall;
	@Autowired
	@Qualifier("serverHandler")
	private IoHandler ioHandler;
	@Autowired
	private GameConfig configHelper;
	@Autowired
	private ContainerFacade containFacade;
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private SystemConfigManager systemConfigManager;
	
	public void exceptionCaught(IoSession session, Throwable throwable) throws Exception {
		if(throwable instanceof WriteException) {
			return;
		}
		Throwable ex = throwable;
		StringBuilder builder = new StringBuilder();
		while(ex != null) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			for(StackTraceElement st : stackTrace) {
				builder.append("\t").append(st.toString()).append("\n");
			}
			
			if(ex == ex.getCause()) {
				break;
			} else {
				ex = ex.getCause();
				if(ex != null) {
					builder.append("CAUSE\n").append(ex.getMessage()).append(ex).append("\n");;
				}
			}
		}
		if(throwable instanceof IOException && builder.indexOf("sun.nio.ch.SocketChannelImpl.read") > 0) {
			return;
		} 
	}

	
	public void messageReceived(IoSession session, Object message) throws Exception {
		if (isOK(session, message)) {
			ioHandler.messageReceived(session, message);
			return;
		}
	}

	
	public void messageSent(IoSession session, Object message) throws Exception {
		ioHandler.messageSent(session, message);
	}

	
	public void sessionClosed(IoSession session) throws Exception {
		Long playerId = sessionManager.getPlayerId(session);
		try {
			if(playerId != null && playerId > 0L) {
				String remoteIp = sessionManager.getRemoteIp(session);
				containFacade.onLogoutUpdateListener(playerId, remoteIp);
			}
			
		} catch (Exception ex){
		}
		
		firewall.removeBlockCounter(session);
		ioHandler.sessionClosed(session);	// 清理SESSION
	}

	
	public void sessionCreated(IoSession session) throws Exception {
		ioHandler.sessionCreated(session);
	}

	
	public void sessionIdle(IoSession session, IdleStatus idleStatus) throws Exception {
		if(idleStatus == IdleStatus.BOTH_IDLE) {
			Long playerId = sessionManager.getPlayerId(session);
			if(playerId != null && playerId > 0){
			}
		}
		ioHandler.sessionIdle(session, idleStatus);
	}

	
	public void sessionOpened(IoSession session) throws Exception {
		if(!configHelper.isOpenIp(null)) {
			String remoteIp = sessionManager.getRemoteIp(session);
			if(!ServerConfig.isAllowMisIp(remoteIp) && !configHelper.isOpenIp(remoteIp)){
				return;
			}
		}
		
		sessionManager.getRemoteIp(session);
		ioHandler.sessionOpened(session);
	}
	
	private static final int[] ANNOUS_VISIT_CMD = { UserCmd.HEART_BEAT, UserCmd.CREATE_CHARACTER, UserCmd.ACCOUNT_LOGIN, 
													UserCmd.LIST_BRANCHINGES, UserCmd.SELECT_CHARACTER, 
													UserCmd.BINDING_NEW_SESSION };
			
	private boolean isOK(IoSession session, Object message) {
		if(message == null || !(message instanceof Request)) {
			return false;
		}
		
		String remoteIp = sessionManager.getRemoteIp(session);
		if(systemConfigManager.isIpBlocked(remoteIp) ){
			Long playerId = sessionManager.getPlayerId(session);
			UserPushHelper.pushKickOff(playerId, KickCode.BLOCK_LOGIN, session);
			return false;
		}
		
		Request request = (Request) message;
		int sn = request.getSn();
		int cmd = request.getCmd();
		int module = request.getModule();
		
		if(module != Module.USER && !ArrayUtils.contains(ANNOUS_VISIT_CMD, cmd)) {
			if (firewall.getClientType(session) != ClientType.MIS) { 	
				if(sessionManager.getPlayerId(session) <= 0L) { 		
					Response response = Response.valueOf(sn, module, cmd);
					response.setMessageType(request.getMessageType());
					response.setStatus(ResponseCode.RESPONSE_CODE_NO_RIGHT);
					sessionManager.write(session, response);
					return false;
				}
			}
		}

		if(module == Module.ADMIN && firewall.getClientType(session) != ClientType.MIS) {	
			Response response = Response.valueOf(sn, module, cmd);
			response.setMessageType(request.getMessageType());
			response.setStatus(ResponseCode.RESPONSE_CODE_NO_RIGHT);
			sessionManager.write(session, response);
			return false;
		}
		return true;
	}

}
