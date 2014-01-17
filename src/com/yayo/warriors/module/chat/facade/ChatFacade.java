package com.yayo.warriors.module.chat.facade;


/**
 * 聊天Facade
 * 
 * @author Hyint
 */
public interface ChatFacade {
	
//	/**
//	 * 系统聊天信息(该方法只能给管理后台调用)
//	 * 
//	 * @param chatRequest		聊天请求对象
//	 * @return {@link Integer}	系统聊天信息
//	 */
//	int doSystemChat(ChatRequest chatRequest);
	
	/**
	 * 客户端主动聊天
	 * 
	 * @param  playerId			角色ID
	 * @param  channel			频道信息
	 * @param  chatInfo			聊天信息
	 * @param  targetName		目标玩家名字
	 * @return {@link Integer}  聊天返回值
	 */
	int doPlayerChat(long playerId, int channel, String chatInfo, String targetName);
}
