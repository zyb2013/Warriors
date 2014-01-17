package com.yayo.warriors.module.friends.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.friends.FriendRule;
import com.yayo.warriors.module.friends.entity.Friend;
import com.yayo.warriors.module.friends.entity.FriendsTreasure;
import com.yayo.warriors.module.friends.facade.FriendsFacade;
import com.yayo.warriors.module.friends.manager.FriendManager;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.friends.FriendsCmd;


@Component
public class FriendHelper {
	
	@Autowired
	private SessionManager sessionMgr;
	@Autowired
	private FriendManager friendManager;
	@Autowired
	private FriendsFacade friendsFacade;
	@Autowired
	private UserManager userManager;
	
	private static ObjectReference<FriendHelper> ref = new ObjectReference<FriendHelper>();
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	private static FriendHelper getInstance() {
		return ref.get();
	}
	
	/**
	 * 添加好友
	 * @param targetId     被添加目标ID
	 * @param applyerId    添加者ID
	 * @param applyerName  添加者名字
	 * @param roleJob      添加者的职业
	 * @param level        添加者的等级
	 */
	public static void applyFriend(Long targetId, Long applyerId, int roleJob, int level, String applyerName){
		Response response = Response.defaultResponse(Module.FRIENDS, FriendsCmd.PUT_APPLET_FRIEND);
		Map<String,Object> sender = new HashMap<String, Object>(4);
		sender.put(ResponseKey.PLAYER_ID, applyerId);
		sender.put(ResponseKey.PLAYER_NAME, applyerName);
		sender.put(ResponseKey.JOB, roleJob);
		sender.put(ResponseKey.LEVEL, level);
		response.setValue(sender);
		getInstance().sessionMgr.write(targetId, response);
	}
	
	/**
	 * 增加好友,友好度
	 * @param targetId     被添加目标ID
	 * @param friendId     好友主键
	 * @param value        友好值
	 */
	public static void plusFriendValue(Player player, long targetId, Friend friend, int value){
		Response response = Response.valueOf(Response.DEFAULT_SN, Module.FRIENDS, FriendsCmd.PUT_PLUS_FRIENDLY_VALUE);
		Map<String, Object> sender = new HashMap<String, Object>(3);
		if (friend != null) {
			sender.put(ResponseKey.ID, friend.getId());
		}
		if (player.getId() != targetId) {
			sender.put(ResponseKey.NAME, player.getName());
		}
		sender.put(ResponseKey.VALUES, value);
		response.setValue(sender);
		getInstance().sessionMgr.write(targetId, response);
	}
	
//	/**
//	 * 修改仇恨度,仇恨值
//	 * @param targetId     被添加目标ID
//	 * @param friendId     好友主键
//	 * @param value        友好值
//	 */
//	public static void updateHatredValue(Long targetId, Long friendId, int value){
//		Response response = Response.valueOf(Response.DEFAULT_SN, Module.FRIENDS, FriendsCmd.UPDATE_PLUS_HATRED_VALUE);
//		Map<String, Object> sender = new HashMap<String, Object>(2);
//		sender.put(ResponseKey.ID, friendId);
//		sender.put(ResponseKey.VALUES, value);
//		response.setValue(sender);
//		getInstance().sessionMgr.write(targetId, response);
//	}
	
	/**
	 * 推送好友升级信息给玩家
	 * @param playerId     玩家ID
	 */
	public static void pushFriendLevelUp(UserDomain userDomain) {
		if(userDomain == null) {
			return;
		}
		
		Player player = userDomain.getPlayer();
		long playerId = userDomain.getPlayerId();
		PlayerBattle battle = userDomain.getBattle();
		if (FriendRule.blessLevelLimit(battle.getLevel())) {
			return;
		}
		
		FriendsTreasure treasure = getInstance().friendsFacade.getFriendsBless(playerId);
		if (treasure == null) {
			return;
		}
		
		Set<Long> focusList = getInstance().friendManager.getFriendsFocus(playerId);
		List<Long> cacheList = new ArrayList<Long>();
		for (Long focusId : focusList) {
			UserDomain domain = getInstance().userManager.getUserDomain(focusId);
			if (domain == null) {
				continue;
			}
			
			FriendsTreasure fTreasure = getInstance().friendManager.getFriendsTreasure(focusId);
			if (fTreasure.isReward()) {
				cacheList.add(focusId);
			} else if (fTreasure.getBlessExp() >= FriendRule.BLESS_EXP_LIMIT) {
				cacheList.add(focusId);
			} else if (FriendRule.blessLevelLimit(domain.getBattle().getLevel())) {
				cacheList.add(focusId);
			}
		}
		focusList.removeAll(cacheList);
		Response response = Response.defaultResponse(Module.FRIENDS, FriendsCmd.PUSH_FRIEND_LEVEL_UP);
		Map<String, Object> sender = new HashMap<String, Object>(5);
		sender.put(ResponseKey.PLAYER_ID, playerId);
		sender.put(ResponseKey.PLAYER_NAME, player.getName());
		sender.put(ResponseKey.JOB, battle.getJob());
		sender.put(ResponseKey.LEVEL, battle.getLevel());
		sender.put(ResponseKey.PARAMS, treasure.getBlessExp());
		response.setValue(sender);
		getInstance().sessionMgr.write(focusList, response);
	}
	
	/**
	 * 推送玩家被祝福
	 * @param targetId
	 * @param playerId
	 * @param playerName
	 */
	public static void pushFriendBless(Long playerId, String playerName, Collection<Long> playerIds) {
		Response response = Response.defaultResponse(Module.FRIENDS, FriendsCmd.PUSH_FRIEND_BLESS);
		Map<String,Object> sender = new HashMap<String, Object>(2);
		sender.put(ResponseKey.PLAYER_ID, playerId);
		sender.put(ResponseKey.PLAYER_NAME, playerName);
		response.setValue(sender);
		getInstance().sessionMgr.write(playerIds, response);
	}
	
	/**
	 * 推送好友在线信息
	 * 
	 * @param playerId
	 */
	public static void pushFriendOnlineState(UserDomain userDomain, boolean online) {
		if (userDomain == null) {
			return;
		}
		
		long playerId = userDomain.getPlayerId();
		Set<Long> allFocus = getInstance().friendManager.getAllFocus(playerId);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.TYPE, online);
		resultMap.put(ResponseKey.PLAYER_ID, playerId);
		Response response = Response.defaultResponse(Module.FRIENDS, FriendsCmd.PUSH_FRIEND_ONLINE_STATE);
		response.setValue(resultMap);
		getInstance().sessionMgr.write(allFocus, response);
	}
	
	
	/**
	 * 推送敬酒
	 * 
	 * @param player
	 * @param targetId
	 */
	public static void pushFriendGreetWine(long playerId, long targetId) {
		Response response = Response.defaultResponse(Module.FRIENDS, FriendsCmd.PUSH_FRIEND_PRESENT_WINE);
		UserDomain userDomain = getInstance().userManager.getUserDomain(playerId);
		if (userDomain == null) {
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.PLAYER_ID, userDomain.getPlayer().getId());
		resultMap.put(ResponseKey.PLAYER_NAME, userDomain.getPlayer().getName());
		response.setValue(resultMap);
		getInstance().sessionMgr.write(targetId, response);
	}
	
	
	/**
	 * 推送好友面板等级变化
	 * 
	 * @param userDomain
	 */
	public static void pushFriendLevelChange(UserDomain userDomain) {
		if (userDomain == null) {
			return;
		}
		
		long playerId = userDomain.getPlayerId();
		Set<Long> focusList = getInstance().friendManager.getFriendsFocus(playerId);
		if (focusList == null || focusList.isEmpty()) {
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.PLAYER_ID, playerId);
		resultMap.put(ResponseKey.LEVEL, userDomain.getBattle().getLevel());
		Response response = Response.defaultResponse(Module.FRIENDS, FriendsCmd.PUSH_FRIEND_LEVEL_CHANGE, resultMap);
		getInstance().sessionMgr.write(focusList, response);
	}
	
	
	/**
	 * 推送玩家摊位状态
	 * 
	 * @param playerId
	 * @param isMarket
	 */
	public static void pushFriendsMarketState(long playerId, boolean isMarket) {
		Set<Long> focusList = getInstance().friendManager.getFriendsFocus(playerId);
		if (focusList == null || focusList.isEmpty()) {
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.PLAYER_ID, playerId);
		resultMap.put(ResponseKey.STATE, isMarket);
		Response response = Response.defaultResponse(Module.FRIENDS, FriendsCmd.PUSH_FRIENDS_MARKET_STATE, resultMap);
		getInstance().sessionMgr.write(focusList, response);
	}
	
}
