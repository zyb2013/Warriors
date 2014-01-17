package com.yayo.warriors.module.recharge.event;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.event.AbstractReceiver;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.module.recharge.facade.ChargeGiftFacade;
import com.yayo.warriors.module.recharge.model.RechargeGiftVO;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.handler.recharge.ChargeGiftCmd;

/**
 * 充值事件事件
 * 
 * @author Hyint
 */
@Component
public class ChargeEventReceiver extends AbstractReceiver<ChargeEvent> {
	
	@Autowired
	private Pusher pusher;
	@Autowired
	private ChargeGiftFacade chargeGiftFacade;
	@Override
	public String[] getEventNames() {
		return new String[]{ ChargeEvent.NAME};
	}

	@Override
	public void doEvent(ChargeEvent event) {
		long playerId = event.getOwnerId();
		List<RechargeGiftVO> giftVOList = chargeGiftFacade.listRechargeGiftVO(playerId, true);
		if(giftVOList != null && !giftVOList.isEmpty()) {
			int module = Module.RECHARGE_GIFT;
			int cmd = ChargeGiftCmd.PUSH_RECHARGE_GIFT;
			pusher.pushMessage(playerId, Response.defaultResponse(module, cmd, giftVOList.toArray()));
		}
	}
}
