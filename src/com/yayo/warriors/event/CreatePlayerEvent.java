package com.yayo.warriors.event;

import com.yayo.common.event.Event;

/**
 * 角色创建事件
 * 
 * @author Hyint
 */
public class CreatePlayerEvent implements IdentityEvent {

	public static final String NAME = "UserEvent:Create";
	
	/**
	 * 登陆的角色ID
	 */
	private long playerId;
	
	
	public String getName() {
		return NAME;
	}

	
	public long getOwnerId() {
		return playerId;
	}
	
	public static Event<CreatePlayerEvent> valueOf(long playerId) {
		CreatePlayerEvent body = new CreatePlayerEvent();
		body.playerId = playerId;
		return Event.valueOf(NAME, body);
	}
	

	
}
