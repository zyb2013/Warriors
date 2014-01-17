package com.yayo.warriors.module.recharge.event;

import com.yayo.common.event.Event;
import com.yayo.warriors.event.IdentityEvent;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 角色充值事件
 * 
 * @author Hyint
 */
public class ChargeEvent implements IdentityEvent {

	public static final String NAME = "ChargeEvent:Create";
	
	/** 登陆的角色ID */
	private UserDomain userDomain;
	
	
	public String getName() {
		return NAME;
	}

	
	public long getOwnerId() {
		return userDomain.getPlayerId();
	}
	
	public static Event<ChargeEvent> valueOf(UserDomain userDomain) {
		ChargeEvent body = new ChargeEvent();
		body.userDomain = userDomain;
		return Event.valueOf(NAME, body);
	}
	

	
}
