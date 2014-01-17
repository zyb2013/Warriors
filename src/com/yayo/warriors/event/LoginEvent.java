package com.yayo.warriors.event;

import com.yayo.common.event.Event;

/**
 * 用户登陆事件
 * 
 * @author Hyint
 */
public class LoginEvent implements IdentityEvent {

	public static final String NAME = "UserEvent:Login";
	
	/**
	 * 登陆的角色ID
	 */
	private long playerId;
	
	/** 角色选择登录的分线号 */
	private int branching;
	
	/** 客户端的IP */
	private String clientIp;
	
	
	public String getName() {
		return NAME;
	}

	
	public long getOwnerId() {
		return playerId;
	}
	
	public int getBranching() {
		return branching;
	}

	public String getClientIp() {
		return clientIp;
	}

	/**
	 * 构建登陆事件接口
	 * 
	 * @param  playerId			角色ID
	 * @param  branching		分线号
	 * @param  clientIp			客户端IP
	 * @return {@link Event}	事件信息
	 */
	public static Event<LoginEvent> valueOf(long playerId, int branching, String clientIp) {
		LoginEvent body = new LoginEvent();
		body.playerId = playerId;
		body.branching = branching;
		body.clientIp = clientIp;
		return Event.valueOf(NAME, body);
	}
	

	
}
