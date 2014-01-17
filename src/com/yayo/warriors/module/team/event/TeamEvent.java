package com.yayo.warriors.module.team.event;

import com.yayo.common.event.Event;
import com.yayo.warriors.module.team.model.Team;
import com.yayo.warriors.module.team.type.EventType;
import com.yayo.warriors.module.team.type.TeamReason;

/**
 * 组队事件
 * 
 * @author Hyint
 */
public class TeamEvent {

	public static final String NAME = "CommonEvent:Team";

	/** 队伍 */
	private Team team;

	/** 原因 */
	private int reason;
	
	/** 是否队长 */
	private boolean leader;
	
	/** 组队时间类型 */
	private EventType type = EventType.JOIN_EVENT;

	/** 队伍成员ID */
	private Long[] memberIdArr;

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}
	 
	public Long[] getMemberIdArr() {
		return memberIdArr;
	}

	public void setMemberIdArr(Long[] memberIdArr) {
		this.memberIdArr = memberIdArr;
	}

	public int getReason() {
		return reason;
	}

	public void setReason(int reason) {
		this.reason = reason;
	}

	public boolean isLeader() {
		return leader;
	}

	public void setLeader(boolean leader) {
		this.leader = leader;
	}

	private static TeamEvent valueOf(EventType type, Team team, Long...memberIdArr) {
		TeamEvent teamEvent = new TeamEvent();
		teamEvent.type = type;
		teamEvent.team = team;
		teamEvent.memberIdArr = memberIdArr;
		return teamEvent;
	}
	
	private static TeamEvent valueOf(EventType type, Team team, int reason, Long... memberIdArr) {
		TeamEvent teamEvent = new TeamEvent();
		teamEvent.type = type;
		teamEvent.team = team;
		teamEvent.reason = reason;
		teamEvent.memberIdArr = memberIdArr;
		return teamEvent;
	}
	
	/**
	 * 加入组队
	 * 
	 * @param  team				队伍对象
	 * @param  playerIds		角色ID数组
	 * @return
	 */
	public static Event<TeamEvent> join(Team team, Long...playerIds) {
		EventType eventType = EventType.JOIN_EVENT;
		TeamEvent body = valueOf(eventType, team, playerIds);
		return new Event<TeamEvent>(NAME, body);
	}

	/**
	 * 离开组队
	 * 
	 * @param  team				组队对象
	 * @param  reason			离开原因
	 * @param  playerIds		角色ID列表
	 * @return
	 */
	public static Event<TeamEvent> leave(Team team, int reason, boolean leader, Long...playerIds) {
		EventType eventType = EventType.LEFT_EVENT;
		TeamEvent body = valueOf(eventType, team, reason, playerIds);
		body.leader = leader;
		return new Event<TeamEvent>(NAME, body);
	}

	/**
	 * 解散组队
	 * 
	 * @param  team				组队对象
	 * @param  reason			离开原因
	 * @param  playerIds		角色ID列表
	 * @return
	 */
	public static Event<TeamEvent> disband(Team team, int reason, Long...playerIds) {
		EventType eventType = EventType.DISBAND_EVENT;
		TeamEvent body = valueOf(eventType, team, reason, playerIds);
		return new Event<TeamEvent>(NAME, body);
	}

	/**
	 * 踢出组队
	 * 
	 * @param  team				组队对象
	 * @param  playerIds		角色ID列表
	 * @return
	 */
	public static Event<TeamEvent> kick(Team team, Long...playerIds) {
		EventType eventType = EventType.LEFT_EVENT;
		TeamEvent body = valueOf(eventType, team, TeamReason.LEAVE_KICK, playerIds);
		return new Event<TeamEvent>(NAME, body);
	}
	
	/**
	 * 交换队长事件
	 * 
	 * @param  team				队伍ID
	 * @param  playerIds		角色ID
	 * @return {@link Event}	事件对象
	 */
	public static Event<TeamEvent> swapLeader(Team team, Long...playerIds) {
		EventType eventType = EventType.SWAP_EVENT;
		TeamEvent body = valueOf(eventType, team, playerIds);
		return new Event<TeamEvent>(NAME, body);
	}
}
