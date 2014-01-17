package com.yayo.warriors.module.user.type;

/**
 * 踢玩家下线的类型
 * 
 * @author Hyint
 */
public enum KickCode {
	
	/** 0-账号重复登录, 或者在其他地方上线 */
	LOGIN_DUPLICATE,
	
	/** 1-服务器即将停服*/
	SERVER_CLOSEING,
	
	/** 2 - 玩家被封禁 */
	BLOCK_LOGIN,
	
	/** 3 - 服务器维护中 */
	SERVER_MAINTENANCE,
}
