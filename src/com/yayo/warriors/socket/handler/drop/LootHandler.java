package com.yayo.warriors.socket.handler.drop;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.type.ResponseCode;
import com.yayo.warriors.module.drop.facade.LootFacade;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;

@Component
public class LootHandler extends BaseHandler {

	@Autowired
	private LootFacade fightLootFacade;
	
	
	protected int getModule() {
		return Module.LOOT;
	}

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	
	protected void inititialize() {
		putInvoker(LootCmd.PICKUP_REWARD, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				pickupReward(session, request, response);
			}
		});
	}
	protected void pickupReward(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long rewardId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso != null) {
				rewardId = ((Number)aso.get(ResponseKey.REWARD_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = fightLootFacade.pickupLootReward(playerId, rewardId);
		if(logger.isDebugEnabled()) {
			logger.debug("角色:[{}] 拾取奖励返回值:[{}] ", playerId, rewardId);
		}
		response.setValue(result);
		session.write(response);
	}
}
