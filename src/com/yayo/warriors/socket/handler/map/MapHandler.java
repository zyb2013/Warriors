package com.yayo.warriors.socket.handler.map;

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
import com.yayo.warriors.module.map.constant.MapConstant;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.vo.ChangeScreenVo;

import flex.messaging.io.amf.ASObject;

@Component
public class MapHandler extends BaseHandler{

	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private SessionManager sessionMgr;
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	protected int getModule() {
		return Module.MAP;
	}

	
	protected void inititialize() {
		
		putInvoker(MapCmd.MAP_PLAYER_MOTION, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				motion(session,request,response);
			}
		});
		
		putInvoker(MapCmd.MAP_ENTERSCREEN, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				enterScreen(session,request,response);
			}
		});
		
		putInvoker(MapCmd.MAP_MOTION_PATH, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				motionPath(session,request,response);
			}
		});
		
		putInvoker(MapCmd.MAP_PLAYER_CHANGESCREEN, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				changeScreen(session, request, response, 0);
			}
		});
		
		putInvoker(MapCmd.CAMP_CHANGESCREEN, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				changeScreen(session, request, response, 1);
			}
		});
		
		putInvoker(MapCmd.CHECK_PLAYER_DEAD_STATE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				checkPlayerDeadState(session, request, response);
			}
		});

		putInvoker(MapCmd.ACCEPT_CONVENE_INVITE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				acceptConveneInvite(session, request, response);
			}
		});
	}
	
	protected void checkPlayerDeadState(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		mapFacade.checkPlayerDeadState(playerId);
	}
	protected void motion(IoSession session, Request request, Response response){
		Integer x = 0;
		Integer y = 0;
		long playerId = sessionMgr.getPlayerId(session);
		try {
			ASObject aso = (ASObject)request.getValue();
			x = Integer.parseInt(aso.get(MapKey.X).toString());
			y = Integer.parseInt(aso.get(MapKey.Y).toString());
		} catch (Exception e) {
			LOGGER.error("{}", e);
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = mapFacade.motion(playerId, x , y);
		if(result != MapConstant.SUCCESS){
			response.setValue(result);
			session.write(response);
			return;
		}
	}
	
	protected void enterScreen(IoSession session, Request request, Response response){
		Long playerId = sessionMgr.getPlayerId(session);
		
		int reslut = mapFacade.enterScreen(playerId);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("玩家:[{}] 通知服务器进入场景: [{}] SessionId:[{}] ", new Object[] { playerId, reslut, session.getId() });
		}
		
		if(reslut != MapConstant.SUCCESS){
			response.setValue(reslut);
			session.write(response);
			return;
		}
	}
	
	protected void motionPath(IoSession session, Request request, Response response){
		Long playerId = sessionMgr.getPlayerId(session);
		Object[] direction = null;
		try {
			ASObject aso = (ASObject)request.getValue();
			direction = (Object[])aso.get(MapKey.path);
		} catch (Exception e) {
			LOGGER.error("{}", e);
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int reslut = mapFacade.motionPath(playerId,direction);
		LOGGER.info("角色:[{}] 移动路径返回值: [{}]", playerId, reslut);
		if(reslut != MapConstant.SUCCESS){
			response.setValue(reslut);
			session.write(response);
			return;
		}
	}
	
	protected void changeScreen(IoSession session, Request request, Response response, int type){
		Long playerId = sessionMgr.getPlayerId(session);
		
		ResultObject<ChangeScreenVo> reslut = type == 0 ? mapFacade.changeScreen(playerId) : mapFacade.campChangeScreen(playerId);
		if(reslut.getResult() != MapConstant.SUCCESS){
			response.setValue(reslut.getResult());
			session.write(response);
		}else{
			response.setValue(reslut.getValue());
			session.write(response);
		}
	}

	private void acceptConveneInvite(IoSession session, Request request, Response response) {
		Long playerId = sessionMgr.getPlayerId(session);
		long targetId = 0;
		int type = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			targetId = ((Number)aso.get(ResponseKey.TARGET_ID)).longValue();
			type = ((Number)aso.get(ResponseKey.TYPE)).intValue();
		} catch (Exception e) {
			LOGGER.error("{}", e);
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = mapFacade.acceptConveneInvite(playerId, targetId, type);
		response.setValue(result);
		session.write(response);
	}

}
