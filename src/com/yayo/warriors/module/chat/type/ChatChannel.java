package com.yayo.warriors.module.chat.type;

/**
 * 聊天频道
 * 
 * @author Hyint
 */
public enum ChatChannel {

	/** 0 - 当前频道(普通频道) */ 
	CURRENT_CHANNEL(true),

	/** 1 - 世界频道. 1条分线就是一个世界 */
	WORLD_CHANNEL(true), 
	
	/** 2 - 帮派信息频道 */
	ALLIANCE_CHANNEL(true),
	
	/** 3 - 组队/队伍频道 */
	TEAM_CHANNEL(true),

	/** 4 - 私聊频道 */
	PRIVATE_CHANNEL(true),
	
	/** 5 - 阵营频道 */
	CAMP_CHANNEL(true),
	
	/** 6 - 小喇叭频道*/
	BUGLET_CHANNEL(true),
	
	/** 7 - 系统频道 */
	SYSTEM_CHANNEL(false);
	
	/** 是否可以发送消息 */
	private boolean canSend = false;
	
	ChatChannel(boolean canSend) {
		this.canSend = canSend;
	}

	public boolean isCanSend() {
		return canSend;
	}

	public void setCanSend(boolean canSend) {
		this.canSend = canSend;
	}
}
