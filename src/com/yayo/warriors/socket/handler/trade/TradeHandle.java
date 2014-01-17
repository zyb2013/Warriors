package com.yayo.warriors.socket.handler.trade;

import static com.yayo.common.socket.type.ResponseCode.*;

import java.util.HashMap;
import java.util.Map;

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
import com.yayo.warriors.module.trade.constant.TradeConstant;
import com.yayo.warriors.module.trade.facade.TradeFacade;
import com.yayo.warriors.module.trade.rule.TradeReason;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;
@Component
public class TradeHandle extends BaseHandler {
	
	
	@Autowired
	private TradeFacade tradeFacade;
	@Autowired
	private SessionManager sessionManager;

	
	protected int getModule() {
		return Module.TRADE;
	}
	
	protected void inititialize() {
		putInvoker(TradeCmd.INVITE_TRADE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				inviteTrade(session, request, response);
			}
		});
		
		putInvoker(TradeCmd.PROCESS_TRADE_INVITE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				processTradeInvite(session, request, response);
			}
		});
		
		putInvoker(TradeCmd.ADD_TRADE_PROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				addTradeProps(session, request, response);
			}
		});
		
		putInvoker(TradeCmd.ADD_TRADE_EQUIP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				addTradeEquip(session, request, response);
			}
		});
		
		putInvoker(TradeCmd.ADD_TRADE_CURRENCY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				addTradeCurrency(session, request, response);
			}
		});
		
		putInvoker(TradeCmd.LOCK_PROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				lockTradeProps(session, request, response);
			}
		});
		
		putInvoker(TradeCmd.CLICK_TO_TRADE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				completeTrade(session, request, response);
			}
		});
		
		putInvoker(TradeCmd.CANCLE_TRADE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				cancleTrade(session, request, response);
			}
		});
		
		putInvoker(TradeCmd.REMOVE_USER_PROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				removeProps(session, request, response);
			}
		});
	}
	
	protected void inviteTrade(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = tradeFacade.inviteTrade(playerId, targetId);
		
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	protected void processTradeInvite(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId   = 0L;
		boolean isAgree = false;
		String targetName = "";
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
			if (aso.containsKey(ResponseKey.TYPE)) {
				isAgree = (Boolean) aso.get(ResponseKey.TYPE);
			}
			if (aso.containsKey(ResponseKey.TARGET_NAME)) {
				targetName = (String) aso.get(ResponseKey.TARGET_NAME);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = TradeConstant.FAILURE;
		if (isAgree) {
			result = tradeFacade.acceptTrade(playerId, targetId);
		} else {
			result = tradeFacade.rejectTrade(playerId, targetId);
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.RESULT, result);
		if (result == TradeConstant.SUCCESS) {
			resultMap.put(ResponseKey.TYPE, isAgree);
			resultMap.put(ResponseKey.PLAYER_ID, targetId);
			resultMap.put(ResponseKey.PLAYER_NAME, targetName);
		}
		resultMap.put(ResponseKey.PLAYER_ID, targetId);
		resultMap.put(ResponseKey.PLAYER_NAME, targetName);
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	protected void addTradeProps(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		long userPropsId = 0L;
		int count        = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
			if (aso.containsKey(ResponseKey.USER_PROPSID)) {
				userPropsId = ((Number) aso.get(ResponseKey.USER_PROPSID)).longValue();
			}
			if (aso.containsKey(ResponseKey.COUNT)) {
				count = ((Number) aso.get(ResponseKey.COUNT)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = tradeFacade.addUserProps(playerId, targetId, userPropsId, count);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void addTradeEquip(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		long userEquipId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
			if (aso.containsKey(ResponseKey.USER_EQUIP_ID)) {
				userEquipId = ((Number) aso.get(ResponseKey.USER_EQUIP_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = tradeFacade.addUserEquip(playerId, targetId, userEquipId);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void addTradeCurrency(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		int currency = -1;
		int count = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
			if (aso.containsKey(ResponseKey.CURRENCY)) {
				currency = ((Number) aso.get(ResponseKey.CURRENCY)).intValue();
			}
			if (aso.containsKey(ResponseKey.COUNT)) {
				count = ((Number) aso.get(ResponseKey.COUNT)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = tradeFacade.addCurrency(playerId, targetId, currency, count);
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.RESULT, result);
		resultMap.put(ResponseKey.CURRENCY, currency);
		resultMap.put(ResponseKey.COUNT, count);
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	protected void lockTradeProps(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}	
		 
		int result = tradeFacade.lockTrade(playerId, targetId);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		 
		response.setValue(resultMap);
		session.write(response);
	}
	
	protected void completeTrade(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = tradeFacade.completeTrade(playerId, targetId);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		 
		response.setValue(resultMap);
		session.write(response);
	}
	
	protected void cancleTrade(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = tradeFacade.cancleTrade(playerId, targetId, TradeReason.CANCLE_ACTION);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		 
		response.setValue(resultMap);
		session.write(response);
	}
	protected void removeProps(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		long goodsId = 0L;
		int goodsType = -1;
		int count = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
			if (aso.containsKey(ResponseKey.GOODS_ID)) {
				goodsId = ((Number) aso.get(ResponseKey.GOODS_ID)).longValue();	
			}
			if (aso.containsKey(ResponseKey.COUNT)) {
				count = ((Number) aso.get(ResponseKey.COUNT)).intValue();
			}
			if (aso.containsKey(ResponseKey.GOODS_TYPE)) {
				goodsType = ((Number) aso.get(ResponseKey.GOODS_TYPE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = tradeFacade.removeUserProps(playerId, targetId, goodsId, count, goodsType);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		session.write(response);
	}
	

	
}
