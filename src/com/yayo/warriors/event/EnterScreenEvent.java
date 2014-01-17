package com.yayo.warriors.event;

import com.yayo.common.event.Event;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 进入场景事件
 * @author liuyuhua
 */
public class EnterScreenEvent implements IdentityEvent {

	public static final String NAME = "MAP:EnterScreen";
	
	private UserDomain userDomain;
	private GameMap toMap ;
	
	
	public String getName() {
		return NAME;
	}

	
	public long getOwnerId() {
		return userDomain.getPlayerId();
	}
	
	
	public UserDomain getUserDomain() {
		return userDomain;
	}

	public GameMap getToMap() {
		return toMap;
	}

	public static Event<EnterScreenEvent> valueOf(UserDomain userDomain, GameMap gameMap) {
		EnterScreenEvent event = new EnterScreenEvent();
		event.userDomain = userDomain;
		event.toMap = gameMap;
		return new Event<EnterScreenEvent>(NAME, event);
	}

}
