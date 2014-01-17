package com.yayo.warriors.socket.handler.recharge;

import static com.yayo.common.socket.type.ResponseCode.*;

import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.module.recharge.facade.ChargeGiftFacade;
import com.yayo.warriors.module.recharge.model.RechargeGiftVO;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;


@Component
public class ChargeGiftHandler extends BaseHandler {

	@Autowired
	private ChargeGiftFacade rechargeGiftFacade;
	
	
	protected int getModule() {
		return Module.RECHARGE_GIFT;
	}

	
	protected void inititialize() {
		putInvoker(ChargeGiftCmd.LIST_RECHARGE_GIFT, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				listRechargeGift(session, request, response);
			}
		});
		
		putInvoker(ChargeGiftCmd.REWARD_RECHARGE_GIFT, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				rewardRechargeGift(session, request, response);
			}
		});
	}

	
	protected void listRechargeGift(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		List<RechargeGiftVO> rechargeVOList = rechargeGiftFacade.listRechargeGiftVO(playerId, false);
		response.setValue(rechargeVOList.toArray());
		session.write(response);
	}

	
	protected void rewardRechargeGift(IoSession session, Request request, Response response) {
		int giftId = 0;
		int rewardId = 0;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.GIFT_ID)) {
				giftId = ((Number) aso.get(ResponseKey.GIFT_ID)).intValue();
			}
			if(aso.containsKey(ResponseKey.REWARD_ID)) {
				rewardId = ((Number) aso.get(ResponseKey.REWARD_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = rechargeGiftFacade.rewardRechargeGift(playerId, giftId, rewardId);
		response.setValue(result);
		session.write(response);
	}
}
