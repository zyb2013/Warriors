package com.yayo.warriors.socket.handler.gift;

import static com.yayo.common.socket.type.ResponseCode.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.gift.facade.GiftFacade;
import com.yayo.warriors.module.gift.vo.GiftVo;
import com.yayo.warriors.module.gift.vo.OnlineGiftVo;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;


@Component
public class GiftHandle extends BaseHandler {
	
	@Autowired
	private GiftFacade giftFacade;
	

	
	protected int getModule() {
		return Module.GIFT;
	}

	
	protected void inititialize() {
		
		putInvoker(GiftCmd.OBTAIN_ONLINE_GIFT_PROPS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				obtainOnlineGift(session, request, response);
			}
		});
		
		
		putInvoker(GiftCmd.LOAD_GIFT_STATE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadOnlineGiftState(session, request, response);
			}
		});
		
		putInvoker(GiftCmd.RECEIVE_CDKEY_GIFT, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				receiveGift(session, request, response);
			}
		});
		
	}

	
	protected void obtainOnlineGift(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int onlineGiftId = -1;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.BASEID)) {
				onlineGiftId = ((Number) aso.get(ResponseKey.BASEID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Collection<BackpackEntry>> result = giftFacade.rewardOnlineGift(playerId, onlineGiftId);
		if (result.getValue() != null) {
			int backpackType = BackpackType.DEFAULT_BACKPACK;
			MessagePushHelper.pushUserProps2Client(playerId, backpackType, false, result.getValue());
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, result.getResult());
		resultMap.put(ResponseKey.BASEID, onlineGiftId);
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void loadOnlineGiftState(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		OnlineGiftVo onlineGift = giftFacade.loadGiftState(playerId);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, CommonConstant.SUCCESS);
		resultMap.put(ResponseKey.VALUES, onlineGift);
		response.setValue(resultMap);
		session.write(response);
	}
	
	protected void receiveGift(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		String cdKey = "";
		int giftId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.CDKEY)) {
				cdKey = (String) aso.get(ResponseKey.CDKEY);
			}
			if (aso.containsKey(ResponseKey.GIFT_ID)) {
				giftId = ((Number) aso.get(ResponseKey.GIFT_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<Integer> result = giftFacade.receiveCDKeyGift(playerId, giftId, cdKey);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, result.getResult());
		resultMap.put(ResponseKey.GIFT_ID, result.getValue());
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void loadEffectGift(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		Collection<GiftVo> voList = giftFacade.loadEffectGifts(playerId);
		response.setValue(voList.toArray());
		session.write(response);
	}
	
	
	protected void receiveEffectGift(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int giftId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.GIFT_ID)) {
				giftId = ((Number) aso.get(ResponseKey.GIFT_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		ResultObject<String> result = giftFacade.receiveEffectGift(playerId, giftId);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, result.getResult());
		resultMap.put(ResponseKey.GIFT_ID, giftId);
		
		response.setValue(resultMap);
		session.write(response);
	}
	
}
