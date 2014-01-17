package com.yayo.warriors.event;

import com.yayo.common.event.Event;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 角色升级事件
 * 
 * @author Hyint
 */
public class LevelUpEvent implements IdentityEvent {

	public static final String NAME = "UserEvent:LevelUp";

	/** 升级前的战斗属性 */
	private Fightable beforable;
	
	/** 用户域模型对象 */
	private UserDomain userDomain;
	
	
	public String getName() {
		return NAME;
	}

	
	public long getOwnerId() {
		return userDomain.getPlayerId();
	}

	public long getPlayerId() {
		return userDomain.getPlayerId();
	}

	public Fightable getBeforable() {
		return beforable;
	}

	public UserDomain getUserDomain() {
		return userDomain;
	}

	/**
	 * 角色等级升级事件
	 * 
	 * @param  playerId					角色ID
	 * @param  userDomain				用户域模型
	 * @param  levels					等级升级的列表
	 * @return {@link LevelUpEvent}		角色升级事件
	 */
	public static Event<LevelUpEvent> valueOf(Fightable fightable, UserDomain userDomain) {
		LevelUpEvent levelUpEvent = new LevelUpEvent();
		levelUpEvent.beforable = fightable;
		levelUpEvent.userDomain = userDomain;
		return new Event<LevelUpEvent> (NAME, levelUpEvent);
	}
}
