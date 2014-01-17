package com.yayo.warriors.module.chat.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.chat.constant.ChatConstant.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.type.SessionType;
import com.yayo.common.utility.EnumUtils;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.chat.facade.ChannelFacade;
import com.yayo.warriors.module.chat.facade.ChatFacade;
import com.yayo.warriors.module.chat.model.Channel;
import com.yayo.warriors.module.chat.model.ChatResponse;
import com.yayo.warriors.module.chat.rule.ChatRule;
import com.yayo.warriors.module.chat.type.ChatChannel;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.team.manager.TeamManager;
import com.yayo.warriors.module.team.model.Team;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.socket.handler.chat.gm.GMHelper;

/**
 * 聊天Facade实现类
 * 
 * @author Hyint
 */
@Component
public class ChatFacadeImpl implements ChatFacade {
	@Autowired
	private GMHelper gmHelper;
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private DbService dbService;
	@Autowired
	private TeamManager teamManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private ChannelFacade channelFacade;
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private AllianceManager allianceManager;
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	
	public int doPlayerChat(long playerId, int channel, String chatInfo, String targetName) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		if (player.isForbid2Chat()) {
			return PLAYER_CHAT_FORBID;
		}

		ChatChannel chatChannel = EnumUtils.getEnum(ChatChannel.class, channel);
		if (chatChannel == null) {
			return CHANNEL_NOT_FOUND;
		} else if (!chatChannel.isCanSend()) {
			return CHANNEL_CHAT_FORBID;
		}

		try {
			if(gmHelper.executeCode(userDomain, chatInfo)) {
				return SUCCESS;
			}
		} catch (Exception e) {
			LOGGER.error("{}", e);
		}
		
		if(!validateChatInfo(chatInfo)) {
			return CHATINFO_TOO_LENGTH;
		} else if(validateChatCoolTime(playerId)){
			return PLAYER_CHAT_COOLTIME;
		}
		
		int result = SUCCESS;
		switch (chatChannel) {
//			TODO 2011年11月26日17:29:59 屏蔽此代码
//			case CAMP_CHANNEL:		return doCampChat(player, channel, chatInfo, showTerms);
			case WORLD_CHANNEL:		result = doWorldChat(userDomain, channel, chatInfo);				break;
			case TEAM_CHANNEL:		result = doTeamChat(userDomain, channel, chatInfo);					break;
			case CAMP_CHANNEL:		result = doCampChat(userDomain, channel, chatInfo);					break;
			case BUGLET_CHANNEL:	result = doBugletChat(userDomain, channel, chatInfo);				break;
			case ALLIANCE_CHANNEL:	result = doAllianceChat(userDomain, channel, chatInfo);				break;
			case CURRENT_CHANNEL:	result = doCurrentChat(userDomain, channel, chatInfo);				break;
			case PRIVATE_CHANNEL:	result = doPrivateChat(userDomain, channel, chatInfo, targetName);	break;
		}
		
		if(result != SUCCESS) {
			removeChatCoolTime(playerId);
		}
		return result;
	}
	
	/**
	 * 阵营聊天
	 * 
	 * @param userDomain
	 * @param channel
	 * @param chatInfo
	 * @return
	 */
	private int doCampChat(UserDomain userDomain, int channel, String chatInfo) {
		Player player = userDomain.getPlayer();
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("角色:[{}] 发送阵营聊天信息:[{}] ", player.getId(), chatInfo);
		}
		
		if(player.getCamp() == null || player.getCamp() == Camp.NONE) {
			return FAILURE;
		}
		
		Channel channels = Channel.valueOf(channel, player.getCamp().ordinal());
		Collection<Long> players = channelFacade.getChannelPlayers(channels);
		if(players == null || players.isEmpty()) {
			return SUCCESS;
		}
		
		ChatResponse chatResponse = toChatResponse(userDomain, null, channel, chatInfo);
		MessagePushHelper.pushChat2Client(players, chatResponse);
		return SUCCESS;
	}

	/**
	 * 小喇叭聊天
	 * 
	 * @param  userDomain		用户域模型对象
	 * @param  channel			聊天频道
	 * @param  chatInfo			聊天信息
	 * @return {@link Integer}	聊天返回值
	 */
	private int doBugletChat(UserDomain userDomain, int channel, String chatInfo) {
		Player player = userDomain.getPlayer();
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("角色:[{}] 发送世界聊天信息:[{}] ", player.getId(), chatInfo);
		}
		
		int propsId = ChatRule.BUGLET_ITEM_ID;
		long playerId = userDomain.getPlayerId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		List<UserProps> propsList = propsManager.listUserPropByBaseId(playerId, propsId, backpack);
		if(propsList == null || propsList.isEmpty()) {
			return ITEM_NOT_FOUND;
		}

		UserProps costProps = null;
		for (UserProps entity : propsList) {
			if(entity.getCount() <= 0 || entity.isOutOfExpiration() || entity.isTrading()) {
				continue;
			}
			
			ChainLock lock = LockUtils.getLock(entity);
			try {
				lock.lock();
				if(entity.getCount() <= 0 || entity.isOutOfExpiration() || entity.isTrading()) {
					continue;
				}
				entity.decreaseItemCount(1);
				dbService.submitUpdate2Queue(entity);
				if(entity.getCount() <= 0) {
					propsManager.put2UserPropsIdsList(playerId, BackpackType.DROP_BACKPACK, entity);
					propsManager.removeFromUserPropsIdsList(playerId, backpack, entity);
				}
			} finally {
				lock.unlock();
			}
			costProps = entity;
			break;
		}
		
		if(costProps == null) {
			return ITEM_NOT_FOUND;
		}
		
		Set<Long> onlinePlayers = sessionManager.getOnlinePlayerIdList();
		if(!onlinePlayers.isEmpty()) {
			ChatResponse chatResponse = toChatResponse(userDomain, null, channel, chatInfo);
			MessagePushHelper.pushChat2Client(onlinePlayers, chatResponse);
		}
		
		MessagePushHelper.pushUserProps2Client(playerId, backpack, false, costProps);
		GoodsLogger.goodsLogger(player, Source.PLAYER_CHAT, LoggerGoods.outcomeProps(costProps.getId(), propsId, 1));
		return SUCCESS;
	}

	/**
	 * 验证聊天信息出长度
	 * 
	 * @param  chatInfo			聊天信息
	 * @return {@link Boolean}	是否可以聊天
	 */
	public boolean validateChatInfo(String chatInfo) {
		return chatInfo == null || chatInfo.length() < ChatRule.MAX_CHAT_INFO_LENTH;
	}

	/**
	 * 验证角色的CD时间
	 * 
	 * @param  playerId			角色ID
	 * @return {@link Boolean}	是否可以聊天
	 */
	public boolean validateChatCoolTime(long playerId) {
		IoSession session = sessionManager.getIoSession(playerId);
		if(session == null){
			return false;
		}
		
		Long cdTime = (Long)session.getAttribute(SessionType.LAST_CHAT_KEY);
		long currentMillis = System.currentTimeMillis();
		if(cdTime == null || cdTime <= currentMillis) {
			session.setAttribute(SessionType.LAST_CHAT_KEY, currentMillis + ChatRule.CHAT_COOL_TIME);
			return false;
		}
		return true;
	}
	
	/**
	 * 移除聊天冷却时间
	 * 
	 * @param  session				连接对象
	 * @param  endTime				冷却的结束时间
	 */
	public void removeChatCoolTime(long playerId) {
		IoSession session = sessionManager.getIoSession(playerId);
		if(session != null){
			session.removeAttribute(SessionType.LAST_CHAT_KEY);
		}
	}

	/**
	 * 当前频道聊天
	 * 
	 * @param player
	 * @param chatRequest
	 * @return
	 */
	private int doCurrentChat(UserDomain userDomain, int channel, String chatInfo) {
		long playerId = userDomain.getPlayerId();
		Collection<Long> playerIdList = mapFacade.getScreenViews(playerId);
		ChatResponse chatResponse = toChatResponse(userDomain, null, channel, chatInfo);
		MessagePushHelper.pushChat2Client(playerIdList, chatResponse);
		return SUCCESS;
	}

	/**
	 * 私聊
	 * 
	 * @param player
	 * @param chatRequest
	 * @return
	 */
	private int doPrivateChat(UserDomain userDomain, int channel, String chatInfo, String targetName) {
		if (StringUtils.isBlank(targetName)) {
			return TARGET_NOT_FOUND;
		}

		Player target = userManager.getPlayer(targetName);
		if (target == null) {
			return TARGET_NOT_FOUND;
		}
		
		long targetId = target.getId();
		if(!userManager.isOnline(targetId)) {
			return TARGET_OFF_LINE;
		}
		
		long playerId = userDomain.getPlayerId();
		ChatResponse chatResponse = toChatResponse(userDomain, target, channel, chatInfo);
		MessagePushHelper.pushChat2Client(Arrays.asList(playerId, targetId), chatResponse);
		return SUCCESS;
	}

	/**
	 * 世界聊天
	 * 
	 * @param <T>
	 */
	private int doWorldChat(UserDomain userDomain, int channel, String chatInfo) {
		Player player = userDomain.getPlayer();
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("角色:[{}] 发送世界聊天信息:[{}] ", player.getId(), chatInfo);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle.getLevel() < ChatRule.WORLD_CHAT_LEVEL) {
			return LEVEL_INVALID;
		}
		
		Collection<Long> players = channelFacade.getBranchingPlayers(player.getBranching());
		if(players == null || players.isEmpty()) {
			return SUCCESS;
		}
		
		ChatResponse chatResponse = toChatResponse(userDomain, null, channel, chatInfo);
		MessagePushHelper.pushChat2Client(players, chatResponse);
		return SUCCESS;
	}

	/**
	 * 团队聊天
	 * 
	 * @param <T>
	 * @return
	 */
	private int doTeamChat(UserDomain userDomain, int channel, String chatInfo) {
		long playerId = userDomain.getPlayerId();
		Team team = teamManager.getPlayerTeam(playerId);
		if(team == null) {
			return FAILURE;
		}
		
		List<Long> onlinePlayers = new ArrayList<Long>(team.getMembers());
		ChatResponse chatResponse = toChatResponse(userDomain, null, channel, chatInfo);
		MessagePushHelper.pushChat2Client(onlinePlayers, chatResponse);
		return SUCCESS;
	}

	/**
	 * 工会聊天
	 * 
	 * @param <T>
	 * @param player
	 * @param chatRequest
	 * @return
	 */
	private int doAllianceChat(UserDomain userDomain, int channel, String chatInfo) {
		PlayerBattle battle = userDomain.getBattle();
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null) {
			return FAILURE;
		} 

		long allianceId = playerAlliance.getAllianceId();
		Alliance alliance = allianceManager.getAlliance(allianceId);
		if(alliance == null) {
			return FAILURE;
		}
		
		
		List<Long> memberIds = allianceManager.getAllianceMembers(allianceId,false);
		if(memberIds == null || memberIds.isEmpty()) {
			return FAILURE;
		}
		
		ChatResponse chatResponse = toAllianceChat(userDomain, null, channel, chatInfo);
		MessagePushHelper.pushChat2Client(memberIds, chatResponse);
		return SUCCESS;
	}

//	TODO 2011年11月26日17:29:40 屏蔽此代码
//	/**
//	 * 阵营聊天
//	 * 
//	 * @param <T>
//	 * @param player
//	 * @param chatRequest
//	 * @return
//	 */
//	private int doCampChat(Player player, int channel, String chatInfo, ShowTerm[] showTerms) {
//		Channel campChannel = Channel.valueOf(ChannelType.CLAZZ_CHANNEL.ordinal());
//		Collection<Long> onlinePlayers = channelFacade.getChannelPlayers(campChannel);
//		ChatResponse chatResponse = toChatResponse(player, null, channel, chatInfo, showTerms);
//		onlinePlayers = onlinePlayers == null ? Arrays.asList(player.getId()) : onlinePlayers;
//		messagePushHelper.pushChat2Client(onlinePlayers, chatResponse);
//		return SUCCESS;
//	}

	/**
	 * 构建聊天响应信息
	 * 
	 * @param  userDomain 			角色的域模型
	 * @param  target 				私聊接收者
	 * @param  chatRequest 			聊天请求对象
	 * @return {@link ChatResponse} 聊天响应对象
	 */
	private ChatResponse toChatResponse(UserDomain userDomain, Player target, int channel, String chatInfo) {
		return ChatResponse.normalResponse(channel, chatInfo, userDomain, target);
	}

	/**
	 * 构建聊天响应信息
	 * 
	 * @param  userDomain 			角色的域模型
	 * @param  target 				私聊接收者
	 * @param  chatRequest 			聊天请求对象
	 * @return {@link ChatResponse} 聊天响应对象
	 */
	private ChatResponse toAllianceChat(UserDomain userDomain, Player target, int channel, String chatInfo) {
		return ChatResponse.allianceResponse(channel, chatInfo, userDomain, target);
	}
}
