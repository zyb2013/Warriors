package com.yayo.warriors.socket.handler.lottery;

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
import com.yayo.warriors.module.lottery.constant.LotteryConstant;
import com.yayo.warriors.module.lottery.facade.LotteryFacade;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;

@Component
public class LotteryHandler extends BaseHandler{

	@Autowired
	private LotteryFacade lotteryFacade;
	
	
	protected int getModule() {
		return Module.LOTTERY;
	}

	
	protected void inititialize() {
		
		putInvoker(LotteryCmd.LOTTERY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				lottery(session, request, response);
			}
		});
		
		putInvoker(LotteryCmd.LOTTERY_CACHE_INFO, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				lotteryCacheInfo(session, request, response);
			}
		});
		
		putInvoker(LotteryCmd.LOTTERY_CHECK_OUT_ALL, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				checkOutAllProps(session, request, response);
			}
		});

	}
	
	protected void checkOutAllProps(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		try {
			int result = lotteryFacade.checkoutAllFromLotteryStorage(playerId);
			resultMap.put(ResponseKey.RESULT, result);
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			resultMap.put(ResponseKey.RESULT, LotteryConstant.FAILURE);
		}
		response.setValue(resultMap);
		
		session.write(response);
	}

	protected void lotteryCacheInfo(IoSession session, Request request, Response response) {
		long playerId = this.sessionManager.getPlayerId(session);
		Map<String,Object> rewardResults = null ;
		try {
			boolean force = true;
			ASObject aso = (ASObject)request.getValue();
			if(aso != null && aso.containsKey("force")) {
				force = ((Boolean)aso.get("force")).booleanValue();
			}
			rewardResults = lotteryFacade.lotteryCacheMsg(playerId, force);
			response.setValue(rewardResults);
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
		}
		
		session.write(response);
	}

	
	protected void lottery(IoSession session,Request request,Response response){
		long playerId = this.sessionManager.getPlayerId(session);
		Map<String,Object> rewardResults = new HashMap<String, Object>(2) ;
		int result = LotteryConstant.FAILURE;
		try {
			int lotteryId = 0;
			boolean autoBuy = false;
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey("lotteryId")) {
				lotteryId = ((Number)aso.get("lotteryId")).intValue();
			}
			if(aso.containsKey("autoBuy")) {
				autoBuy = ((Boolean)aso.get("autoBuy")).booleanValue();
			}
			result = lotteryFacade.doLottery(playerId, lotteryId, autoBuy, rewardResults);
			
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		rewardResults.put(ResponseKey.RESULT, result);
		response.setValue(rewardResults);
		
		session.write(response);
	}

}
