package com.yayo.warriors.event;

import com.yayo.common.event.Event;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 角色死亡事件
 * 
 * @author Hyint
 */
public class DeadEvent implements IdentityEvent {

	public static final String NAME = "UserEvent:Dead";
	
	private UserDomain userDomain;
	
	private UnitId unitId;
	
	public String getName() {
		return NAME;
	}

	
	public long getOwnerId() {
		return userDomain.getPlayerId();
	}

	public long getPlayerId() {
		return userDomain.getPlayerId();
	}

	public UserDomain getUserDomain() {
		return userDomain;
	}

	public UnitId getUnitId() {
		return unitId;
	}

	public DeadEvent(UserDomain userDomain, UnitId unitId) {
		this.unitId = unitId;
		this.userDomain = userDomain;
	}
	
	public static Event<DeadEvent> valueOf(UserDomain userDomain, UnitId unitId) {
		return new Event<DeadEvent>(NAME, new DeadEvent(userDomain, unitId));
	}
}
