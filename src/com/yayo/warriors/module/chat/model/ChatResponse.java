package com.yayo.warriors.module.chat.model;

import java.io.Serializable;

import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.types.Title;
import com.yayo.warriors.module.campbattle.type.CampTitle;
import com.yayo.warriors.module.chat.helper.ChatReference;
import com.yayo.warriors.module.chat.type.ChatChannel;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Camp;

/**
 * 聊天响应信息类
 * 
 * @author Hyint
 */
public class ChatResponse implements Serializable {
	private static final long serialVersionUID = -5511776335557854826L;

	/** 发言玩家ID. 当频道是系统频道时, 这个值为0, 客户端不需要判断 */
	private long playerId = 0L;

	/** 发言玩家角色名. 当频道是系统频道时, 这个值为0, 客户端不需要判断  */
	private String playerName;

	/** 聊天信息 */
	private String chatInfo;
	
	/** 目标玩家ID(私聊,否则为0) */
	private long targetId = 0L;
	
	/** 目标玩家角色名(不为null则为私聊) */
	private String targetName;

	/** 发言玩家所属阵营. 详细见: {@link Camp} */
	private Camp playerCamp = Camp.NONE;

	/** 增加公会职位 */
	private Title allianceTitle = Title.NOMAL; 

	/** 阵营战场称号. 详细见: {@link CampTitle} */
	private CampTitle campTitle = CampTitle.NONE;

			
	/** 聊天频道 */
	private int channel = ChatChannel.WORLD_CHANNEL.ordinal();

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public long getTargetId() {
		return targetId;
	}

	public void setTargetId(long targetId) {
		this.targetId = targetId;
	}

	public Camp getPlayerCamp() {
		return playerCamp;
	}

	public void setPlayerCamp(Camp playerCamp) {
		this.playerCamp = playerCamp;
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

	public CampTitle getCampTitle() {
		return campTitle;
	}

	public void setCampTitle(CampTitle campTitle) {
		this.campTitle = campTitle;
	}

	public Title getAllianceTitle() {
		return allianceTitle;
	}

	public void setAllianceTitle(Title allianceTitle) {
		this.allianceTitle = allianceTitle;
	}

	@Override
	public String toString() {
		return "ChatResponse [playerId=" + playerId + ", playerName=" + playerName
				+ ", playerCamp=" + playerCamp + ", targetId=" + targetId + ", targetName="
				+ targetName + ", chatInfo=" + chatInfo + ", campTitle=" + campTitle + ", channel="
				+ channel + "]";
	}

	/**
	 * 构建聊天响应对象
	 * 
	 * @param channel				聊天频道
	 * @param chatInfo				聊天信息
	 * @param playerId				角色ID
	 * @param playerName			角色名
	 * @param playerCamp			角色的阵营
	 * @return {@link ChatResponse} 聊天响应对象
	 */
	public static ChatResponse normalResponse(int channel, String chatInfo, UserDomain userDomain, Player target) {
		Player player = userDomain.getPlayer();
		ChatResponse chatResponse = new ChatResponse();
		chatResponse.channel = channel;
		chatResponse.chatInfo = chatInfo;
		chatResponse.playerId = player.getId();
		chatResponse.playerName = player.getName();
		chatResponse.playerCamp = player.getCamp();
		chatResponse.campTitle = ChatReference.getCampTitle(player.getId());
		if (target != null) {
			chatResponse.targetId = target.getId();
			chatResponse.targetName = target.getName();
		}
		return chatResponse;
	}

	/**
	 * 构建聊天响应对象
	 * 
	 * @param channel				聊天频道
	 * @param chatInfo				聊天信息
	 * @param playerId				角色ID
	 * @param playerName			角色名
	 * @param playerCamp			角色的阵营
	 * @return {@link ChatResponse} 聊天响应对象
	 */
	public static ChatResponse allianceResponse(int channel, String chatInfo, UserDomain userDomain, Player target) {
		Player player = userDomain.getPlayer();
		ChatResponse chatResponse = new ChatResponse();
		chatResponse.channel = channel;
		chatResponse.chatInfo = chatInfo;
		chatResponse.playerId = player.getId();
		chatResponse.playerName = player.getName();
		chatResponse.playerCamp = player.getCamp();
		chatResponse.campTitle = ChatReference.getCampTitle(player.getId());
		if (target != null) {
			chatResponse.targetId = target.getId();
			chatResponse.targetName = target.getName();
		}
		
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = ChatReference.getAllianceTitle(battle);
		if(playerAlliance != null) {
			chatResponse.allianceTitle = playerAlliance.getTitle();
		}
		return chatResponse;
	}
}
