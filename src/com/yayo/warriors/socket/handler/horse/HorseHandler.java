package com.yayo.warriors.socket.handler.horse;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.horse.constant.HorseConstant.*;

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
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.horse.entity.Horse;
import com.yayo.warriors.module.horse.facade.HorseFacade;
import com.yayo.warriors.module.horse.vo.HorseVo;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;


@Component
public class HorseHandler extends BaseHandler{

	@Autowired
	private HorseFacade horseFacade;
	
	
	protected int getModule() {
		return Module.HORSE;
	}

	
	protected void inititialize() {
		
		putInvoker(HorseCmd.LOAD_HORSE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadHorse(session, request, response);
			}
		});
		
		putInvoker(HorseCmd.WINUP_HORSE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				ridingHorse(session, request, response);
			}
		});
		
		putInvoker(HorseCmd.DIS_HORSE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				disHorse(session, request, response);
			}
		});
		
		putInvoker(HorseCmd.PROPS_FANCY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				//propsFancy(session, request, response);
			}
		});
		
		putInvoker(HorseCmd.GOLDEN_FANCY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				//goldenFancy(session, request, response);
			}
		});
		
		putInvoker(HorseCmd.DEFIND_FANCY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				//defindFancy(session, request, response);
			}
		});
		
		putInvoker(HorseCmd.VIEW_HORSE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				viewHorse(session, request, response);
			}
		});
		
		putInvoker(HorseCmd.DEFIND_PROPS_FANCY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				definePropsFancy(session, request, response);
			}
		});
		
	}
	protected void definePropsFancy(IoSession session,Request request,Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		String userItems = "";
		int autoBuyCount = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.USER_PROPS)) {
				userItems = (String) aso.get(ResponseKey.USER_PROPS);
			}
			if(aso.containsKey(ResponseKey.AUTO_BUY_COUNT)) {
				autoBuyCount = ((Number)aso.get(ResponseKey.AUTO_BUY_COUNT)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<HorseVo> result = this.horseFacade.definePropsFancy(playerId, userItems, autoBuyCount);
		Map<String,Object> map = new HashMap<String,Object>(2);
		map.put(ResponseKey.RESULT, result.getResult());
		if(result.getResult() == SUCCESS){
			HorseVo horseVo = result.getValue();
			map.put(ResponseKey.HORSE, horseVo);
		}
		response.setValue(map);
		session.write(response);
	}
	
	
	protected void viewHorse(IoSession session,Request request,Response response){
		long targetId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number)aso.get(ResponseKey.TARGET_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Map<String,Object>> result = this.horseFacade.viewHorse(targetId);
		if(result.getResult() == SUCCESS){
			response.setValue(result.getValue());
			session.write(response);
		}else{
			response.setValue(result.getResult());
			session.write(response);
		}
	}
	
	
	protected void loadHorse(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		HorseVo horseVO = this.horseFacade.getHorseVO(playerId);
		int result = horseVO == null ? HORSE_NOT_FOUND : SUCCESS;
		Map<String,Object> map = new HashMap<String,Object>();
		map.put(ResponseKey.RESULT, result);
		map.put(ResponseKey.HORSE, horseVO);
		response.setValue(map);
		session.write(response);
	}
	
	protected void disHorse(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		ResultObject<Horse> result = horseFacade.dismountHorse(playerId);
		if(result.getResult() == SUCCESS){
			Map<String,Object> map = new HashMap<String,Object>(2);
			map.put(ResponseKey.MOUNT, result.getValue().getModel());
			map.put(ResponseKey.SPEED, horseFacade.getPlayerSpeed(playerId));
			response.setValue(map);
			session.write(response);
		}else{
			response.setValue(result.getResult());
			session.write(response);
		}
		
	}
	
	protected void ridingHorse(IoSession session, Request request, Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		int mount = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.MOUNT)){
				mount = ((Number)aso.get(ResponseKey.MOUNT)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Integer> result = horseFacade.winupHorse(playerId,mount);
		Map<String,Object> map = new HashMap<String,Object>(3);
		map.put(ResponseKey.RESULT, result.getResult());
		if(result.getResult() == SUCCESS){
			map.put(ResponseKey.MOUNT, result.getValue());
			map.put(ResponseKey.SPEED, horseFacade.getPlayerSpeed(playerId));
		}
		
		response.setValue(map);
		session.write(response);
		
	}

}
