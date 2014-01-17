package com.yayo.warriors.module.user.type;



/**
 * 角色的状态Key
 * 
 * @author Hyint
 */
public enum PlayerStateKey {
	
	/** 组队ID */
	TEAM_ID,
	
	/** 逻辑分线. (逻辑存在,登陆以后由玩家选择决定) */
	BRANCHING,
	
	/** 禁止聊天VO. {@link ForbidVO} */
	FORBIT_CHAT_VO,
	
	/** 封禁角色登陆.  {@link ForbidVO} */
	FORBIT_LOGIN_VO,
	
	/** 组队被邀请列表 */
	TEAM_BE_INVITES,
	
	/** 藏宝图退出future对象 */
	TREASURE_EXIT_FUTURE,
	
}
