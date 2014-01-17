package com.yayo.warriors.module.onhook.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.event.AbstractReceiver;
import com.yayo.warriors.event.CreatePlayerEvent;
import com.yayo.warriors.module.onhook.facade.TrainFacade;

/**
 * 角色创建事件
 * 
 * @author Administrator
 */
@Component
public class CreatePlayerEventReceiver extends AbstractReceiver<CreatePlayerEvent> {

	@Autowired
	private TrainFacade trainFacade;
	
	@Override
	public void doEvent(CreatePlayerEvent event) {
//		long playerId = event.getOwnerId();
//		int startResult = onhookFacade.startTrain(playerId);
//		LOGGER.debug("创建角色:[{}] 开启自动闭关返回值:[{}] ", playerId, startResult);
	}

	@Override
	public String[] getEventNames() {
		return new String[] { CreatePlayerEvent.NAME };
	}

}
