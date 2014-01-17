package com.yayo.warriors.module.chat.rule;

import com.yayo.common.utility.TimeConstant;

/**
 * 聊天规则表
 * 
 * @author Hyint
 */
public class ChatRule {
	
	/** 小喇叭发言需要的道具ID */
	public static final int BUGLET_ITEM_ID = 120005;

	/** 最大聊天信息字数 */
	public static final int MAX_CHAT_INFO_LENTH = 80;
	
	/** 世界聊天等级限制. */
	public static final int WORLD_CHAT_LEVEL = 11;
	
	/** 聊天时间间隔 */
	public static final int CHAT_COOL_TIME = TimeConstant.ONE_SECOND_MILLISECOND * 3;
}
