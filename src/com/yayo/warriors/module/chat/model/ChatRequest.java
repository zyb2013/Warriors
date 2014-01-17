package com.yayo.warriors.module.chat.model;

import java.io.Serializable;

import com.yayo.warriors.module.chat.type.ChatChannel;


/**
 * 聊天请求信息类
 */
public class ChatRequest implements Serializable {
	private static final long serialVersionUID = 354763105350651678L;

	/** 聊天频道 */
	private int channel = ChatChannel.WORLD_CHANNEL.ordinal();
	
	/** 目标玩家角色名(私聊,否则为null) */ 
	private String targetName;
	
	/** 聊天信息 */        
	private String chatInfo;
	
	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String getChatInfo() {
		return chatInfo;
	}

	public void setChatInfo(String chatInfo) {
		this.chatInfo = chatInfo;
	}

	@Override
	public String toString() {
		return "ChatRequest [channel=" + channel + ", targetName=" + targetName + ", chatInfo=" + chatInfo + "]";
	}

	/**
	 * 构建聊天请求对象
	 * 
	 * @param  channel				聊天频道
	 * @param  chatInfo				聊天信息
	 * @return {@link ChatRequest}	聊天请求对象
	 */
	public static ChatRequest valueOf(int channel, String chatInfo) {
		ChatRequest chat = new ChatRequest();
		chat.channel = channel;
		chat.chatInfo = chatInfo;
		return chat;
	}
	
	/**
	 * 构建聊天请求对象
	 * 
	 * @param  channel				聊天频道
	 * @param  chatInfo				聊天信息
	 * @param  targetName			私聊玩家的名字
	 * @return {@link ChatRequest}	聊天请求对象
	 */
	public static ChatRequest valueOf(int channel, String chatInfo, String targetName) {
		ChatRequest chat = new ChatRequest();
		chat.channel = channel;
		chat.chatInfo = chatInfo;
		chat.targetName = targetName;
		return chat;
	}
	
}
