package com.yayo.warriors.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.event.AbstractReceiver;
import com.yayo.warriors.module.fight.facade.FightFutureFacade;

/**
 * 角色死亡事件
 * 
 * @author Hyint
 */
@Component
public class DeadEventReceiver extends AbstractReceiver<DeadEvent> {
	
	@Autowired
	private FightFutureFacade fightFutureFacade;
	
	@Override
	public String[] getEventNames() {
		return new String[] { DeadEvent.NAME };
	}

	@Override
	public void doEvent(DeadEvent deadEvent) {
		fightFutureFacade.processPlayerDead(deadEvent.getUserDomain(), deadEvent.getUnitId());
	}
}
