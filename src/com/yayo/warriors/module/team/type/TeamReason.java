package com.yayo.warriors.module.team.type;

/**
 * 离开/解散组队原因
 * 
 * @author Hyint
 */
public interface TeamReason {

	/** 解散队伍 - 队长解散组队 */
	int DISBAND_LEADER = 1;
	
	/** 离开队伍 - 主动离开*/
	int LEAVE_ACTION = 2;
	
	/** 离开队伍 - 被队长踢出 */
	int LEAVE_KICK = 3;
	
	/** 离开队伍 - 登出游戏 */
	int LEAVE_LOGOUT = 4;
}
