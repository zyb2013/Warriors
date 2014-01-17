package com.yayo.warriors.socket.handler.friends;

import static com.yayo.common.socket.type.ResponseCode.*;
import static com.yayo.warriors.socket.handler.friends.FriendsKey.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.type.ResponseCode;
import com.yayo.common.utility.EnumUtils;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.friends.constant.FriendConstant;
import com.yayo.warriors.module.friends.entity.Friend;
import com.yayo.warriors.module.friends.entity.FriendsTreasure;
import com.yayo.warriors.module.friends.facade.FriendsFacade;
import com.yayo.warriors.module.friends.type.FriendType;
import com.yayo.warriors.module.market.manager.MarketManager;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.vo.FriendsSearchVo;
import com.yayo.warriors.socket.vo.FriendsVo;
import com.yayo.warriors.util.ParamUtils;

import flex.messaging.io.amf.ASObject;

/**
 * 好友模块接口
 * 
 * @author liuyuhua
 */
@Component
public class FriendsHandler extends BaseHandler {

	@Autowired
	private FriendsFacade friendFacade;
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MarketManager marketManager;

	
	
	protected int getModule() {
		return Module.FRIENDS;
	}

	
	protected void inititialize() {
		// 加载好友模块信息
		putInvoker(FriendsCmd.LOAD_LIST, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadlist(session, request, response);
			}
		});

		// 添加好友
		putInvoker(FriendsCmd.ADD_FRIENDLY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				addfriendly(session, request, response);
			}
		});

		// 删除好友
		putInvoker(FriendsCmd.DEL_FRIENDLY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				delfriendly(session, request, response);
			}
		});

		// 添加黑名单
		putInvoker(FriendsCmd.ADD_BLACK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				addblack(session, request, response);
			}
		});

		// 删除黑名单
		putInvoker(FriendsCmd.DEL_BLACK, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				delblack(session, request, response);
			}
		});

		// 添加最近
		putInvoker(FriendsCmd.ADD_NEAREST, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				addnearest(session, request, response);
			}
		});

		// 删除最近
		putInvoker(FriendsCmd.DEL_NEAREST, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				delnearest(session, request, response);
			}
		});

		// 查找
		putInvoker(FriendsCmd.SEARCH_NAME, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				search(session, request, response);
			}
		});

		// 速配好友        策划需求屏蔽
//		putInvoker(FriendsCmd.SPEED_ADD_FRIEND, new Invoker() {
//			
//			public void invoke(IoSession session, Request request, Response response) {
//				speedAddFriend(session, request, response);
//			}
//		});
		
		// 好友祝福
		putInvoker(FriendsCmd.FRIENDS_BLESS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				friendsBless(session, request, response);
			}
		});
		
		// 领取祝福经验
		putInvoker(FriendsCmd.GET_BLESS_EXP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getBlessExp(session, request, response);
			}
		});
		
		// 加载祝福经验数值
		putInvoker(FriendsCmd.LOAD_BLESS_EXP, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadBlessExp(session, request, response);
			}
		});
		
		// 是否领取祝福
		putInvoker(FriendsCmd.LOAD_BLESS_STATE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				isRewardState(session, request, response);
			}
		});
		
		// 征集好友
		putInvoker(FriendsCmd.COLLECT_FRIENDS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				collectFriends(session, request, response);
			}
		});
		
		// 好友赠酒
		putInvoker(FriendsCmd.FRIENDS_PRESENT, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				presentWine(session, request, response);
			}
		});
		
		// 好友敬酒
		putInvoker(FriendsCmd.FRIENDS_GREET, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				friendsGreet(session, request, response);
			}
		});
		
		// 饮酒奖励
		putInvoker(FriendsCmd.FRIENDS_DRINKED, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				friendsDrinked(session, request, response);
			}
		});
		
		// 加载酒量
		putInvoker(FriendsCmd.LOAD_FRIENDS_WINE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadWine(session, request, response);
			}
		});
		
		// 批量加好友
		putInvoker(FriendsCmd.ADD_FRIENDS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				addFriends(session, request, response);
			}
		});
		
		// 批量祝福
		putInvoker(FriendsCmd.BATCH_FRIENDS_BLESS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				batchBlessFriends(session, request, response);
			}
		});
		
		// 查看好友的酒坛
		putInvoker(FriendsCmd.LOAD_OTHERS_WINE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadOthersWine(session, request, response);
			}
		});
		
		// 查看敬酒历史记录
		putInvoker(FriendsCmd.LOAD_GREET_HISTORY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				loadGreetHistory(session, request, response);
			}
		});

	}

	/**
	 * 搜索在线玩家
	 * 
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void search(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		String keywords = null;
		
		try {
			ASObject aso = (ASObject) request.getValue();
			keywords = (String) aso.get(KEYWORDS);
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		if (keywords == null) {
			return;
		}
		
		ResultObject<Collection<FriendsSearchVo>> result = friendFacade.searchPlayerName(keywords, playerId);
		if (result.getResult() != FriendConstant.SUCCESS) {
			response.setValue(result.getResult());
			session.write(response);
			return;
		}

		response.setValue(result.getValue().toArray());
		session.write(response);

	}

	/**
	 * 加载好友列表
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void loadlist(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		Integer type = null;
		try {
			ASObject aso = (ASObject) request.getValue();
			type = ((Number) aso.get(FRIENDS_TYPE)).intValue();
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		FriendType friendType = EnumUtils.getEnum(FriendType.class, type);
		ResultObject<Collection<Friend>> result = friendFacade.loadAllFriend(playerId, friendType);

		if (result.getResult() != FriendConstant.SUCCESS) {
			response.setValue(result.getResult());
			session.write(response);
			return;
		}

		Collection<Friend> friends = result.getValue();
		Collection<FriendsVo> sender = new ArrayList<FriendsVo>();
		for (Friend friend : friends) {
			long targetId = friend.getTargetId();
			UserDomain userDomain = userManager.getUserDomain(targetId);
			if(userDomain == null){
				continue;
			}
			
			Player player = userDomain.getPlayer();
			PlayerBattle battle = userDomain.getBattle();
			boolean online =  userManager.isOnline(targetId);
			int isMarket = marketManager.isMarket(targetId) ? 1 : 0;
			boolean isGreet = friendFacade.isGreet(playerId, targetId);
			
			FriendsVo vo = FriendsVo.valueOf(friend, player, battle, isGreet, online, isMarket);
			sender.add(vo);
		}
		
		response.setValue(sender.toArray());
		session.write(response);
	}

	/**
	 * 添加好友
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void addfriendly(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		String playerName = null;
		try {
			ASObject aso = (ASObject) request.getValue();
			playerName = (String) aso.get(NAME);
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		
		Player player = userManager.getPlayer(playerName);
		if(player == null){
			response.setValue(FriendConstant.PLAYER_NOT_FOUND);
			session.write(response);
			return;
		}

		ResultObject<Friend> result = friendFacade.addFriend(playerId, player.getId());
		if (result.getResult() != FriendConstant.SUCCESS) {
			response.setValue(result.getResult());
			session.write(response);
			return;
		}

		Friend friend = result.getValue();
		boolean online = userManager.isOnline(player.getId());
		UserDomain userDomain = userManager.getUserDomain(player.getId());
		int isMarket = marketManager.isMarket(friend.getTargetId()) ? 1 : 0;
		PlayerBattle battle = userDomain.getBattle();
		
		FriendsVo vo = FriendsVo.valueOf(friend, player, battle, false, online, isMarket);
		response.setValue(vo);
		session.write(response);
	}

	/**
	 * 删除好友
	 * 
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void delfriendly(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		Long targetId = null; // 目标ID

		try {
			ASObject aso = (ASObject) request.getValue();
			targetId = ((Number) aso.get(TARGET_ID)).longValue();
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		ResultObject<Long> result = friendFacade.deleteFriend(playerId, targetId,FriendType.FRIENDLY);
		response.setValue(result.getResult());
		session.write(response);
	}

	/**
	 * 添加黑名单
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void addblack(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		String playerName = null;

		try {
			ASObject aso = (ASObject) request.getValue();
			playerName = (String) aso.get(NAME);
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		Player player = userManager.getPlayer(playerName);
		if (player == null) {
			response.setValue(FriendConstant.PLAYER_NOT_FOUND);
			session.write(response);
			return;
		}

		long targetId = player.getId();
		ResultObject<Friend> result = friendFacade.addBlack(playerId, targetId);
		if (result.getResult() != FriendConstant.SUCCESS) {
			response.setValue(result.getResult());
			session.write(response);
			return;
		}

		Friend friend = result.getValue();
		boolean isOnline = userManager.isOnline(targetId);
		UserDomain userDomain = userManager.getUserDomain(targetId);
		PlayerBattle battle = userDomain.getBattle();
		FriendsVo vo = FriendsVo.valueOf(friend, player, battle, false, isOnline, 0);
		response.setValue(vo);
		session.write(response);

	}

	/**
	 * 删除黑名单
	 * 
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void delblack(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		Long targetId = null; // 目标ID

		try {
			ASObject aso = (ASObject) request.getValue();
			targetId = ((Number) aso.get(TARGET_ID)).longValue();
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		ResultObject<Long> result = friendFacade.deleteFriend(playerId, targetId,FriendType.BLACK);
		response.setValue(result.getResult());
		session.write(response);
	}

	/**
	 * 添加最近联系人
	 * 
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void addnearest(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		String playerName = null;

		try {
			ASObject aso = (ASObject) request.getValue();
			playerName = (String) aso.get(NAME);
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

	    Player player = userManager.getPlayer(playerName);

		if (player == null) {
			response.setValue(FriendConstant.PLAYER_NOT_FOUND);
			session.write(response);
			return;
		}

		Long targetId = player.getId();
		ResultObject<Friend> result = friendFacade.addNearest(playerId, targetId);

		if (result.getResult() != FriendConstant.SUCCESS) {
			response.setValue(result.getResult());
			session.write(response);
			return;
		}

		Friend friend = result.getValue();
		boolean isOnline = userManager.isOnline(targetId);
		UserDomain userDomain = userManager.getUserDomain(targetId);
		PlayerBattle battle = userDomain.getBattle();
		FriendsVo vo = FriendsVo.valueOf(friend, player, battle, false, isOnline, 0);
		response.setValue(vo);
		session.write(response);
	}

	/**
	 * 删除最近联系人
	 * 
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void delnearest(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		Long targetId = null; // 目标ID

		try {
			ASObject aso = (ASObject) request.getValue();
			targetId = ((Number) aso.get(TARGET_ID)).longValue();
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		ResultObject<Long> result = friendFacade.deleteFriend(playerId, targetId,FriendType.NEAREST);
		response.setValue(result.getResult());
		session.write(response);
	} 

	/**
	 * 速配好友
	 * @param session
	 * @param request
	 * @param response
	 */
//	protected void speedAddFriend(IoSession session, Request request, Response response) {
//		Long playerId = sessionManager.getPlayerId(session);
//		Integer sex = null;
//
//		try {
//			ASObject aso = (ASObject) request.getValue();
//			sex = ((Number) aso.get(SEX)).intValue();
//		} catch (Exception ex) {
//			response.setValue(FriendConstant.FAILURE);
//			session.write(response);
//			return;
//		}
//		
//		ResultObject<Long> idResult = friendFacade.getRandomPlayerId(playerId, sex);
//		if (idResult.getResult() != FriendConstant.SUCCESS) {
//			response.setValue(idResult.getResult());
//			session.write(response);
//			return;
//		}
//		
//		//加好友
//		long targetPlayerId = idResult.getValue();
//		UserDomain userDomain = userManager.getUserDomain(targetPlayerId);
//		Player targetPlayer = userDomain.getPlayer();
//		if (targetPlayer == null) {
//			response.setValue(FriendConstant.PLAYER_NOT_FOUND);
//			session.write(response);
//			return;
//		}
//		
//		ResultObject<Friend> friendResult = friendFacade.addFriend(playerId, targetPlayerId);
//
//		if (friendResult.getResult() != FriendConstant.SUCCESS) {
//			response.setValue(friendResult.getResult());
//			session.write(response);
//			return;
//		}
//
//		Friend friend = friendResult.getValue();
//		boolean online = userManager.isOnline(targetPlayerId);
//		int isMarket = marketManager.isMarket(friend.getTargetId()) ? 1 : 0;
//		PlayerBattle battle = userDomain.getBattle();
//		FriendsVo vo = FriendsVo.valueOf(friend.getId(), friend.getTargetId(),
//			targetPlayer.getName(), friend.getType().ordinal(), isMarket, false,
//			online, friend.getValue(), battle.getLevel(), battle.getJob().ordinal());
//
//		response.setValue(vo);
//		session.write(response);
//	}
	
	/**
	 * 好友祝福
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void friendsBless(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception ex) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = friendFacade.getBless(playerId, targetId);
		response.setValue(result);
		session.write(response);
	}
	
	/**
	 * 领取奖励
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void getBlessExp(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		
		int result = friendFacade.rewardBlessExp(playerId);
		response.setValue(result);
		session.write(response);
	}
	
	/**
	 * 加载祝福瓶信息
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void loadBlessExp(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		
		FriendsTreasure treasure = friendFacade.getFriendsBless(playerId);
		
		response.setValue(treasure.getBlessExp());
		session.write(response);
	}
	
	/**
	 * 是否可领取祝福瓶
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void isRewardState(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		Map<String, Object> resultMap = friendFacade.friendsCollected(playerId);
 		response.setValue(resultMap);
		session.write(response);
	}
	
	
	/**
	 * 征集好友
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void collectFriends(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		Collection<FriendsSearchVo> voList = friendFacade.listRandomPlayer(playerId);
		response.setValue(voList.toArray());
		session.write(response);
	}
	
	
	/**
	 * 好友赠酒
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void presentWine(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		String userProps = "";
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
			if (aso.containsKey(ResponseKey.USER_PROPS)) {
				userProps = (String) aso.get(ResponseKey.USER_PROPS);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		long result = friendFacade.friendsPresentWine(playerId, targetId, userProps);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (result < FriendConstant.SUCCESS) {
			resultMap.put(ResponseKey.RESULT, result);
		} else {
			resultMap.put(ResponseKey.RESULT, FriendConstant.SUCCESS);
			resultMap.put(ResponseKey.ID, result);
		}
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	/**
	 * 
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void friendsGreet(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = friendFacade.greetFriends(playerId, targetId);
		response.setValue(result);
		session.write(response);
	}
	
	
	/**
	 * 
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void friendsDrinked(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int result = friendFacade.drinkWine(playerId);
		response.setValue(result);
		session.write(response);
	}
	
	
	/**
	 * 
	 * 
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void loadWine(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		FriendsTreasure treasure = friendFacade.getFriendsBless(playerId);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(ResponseKey.VALUES, treasure.getWineMeasure());
		result.put(ResponseKey.TYPE, treasure.isDrinked());
		response.setValue(result);
		session.write(response);
 	}
	
	
	/**
	 * 批量祝福
	 * 
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void batchBlessFriends(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		List<Long> playerIds = null;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.PLAYER_IDS)) {
				Object[] objs = ParamUtils.getParameter(aso, ResponseKey.PLAYER_IDS, Object[].class);
				playerIds = new ArrayList<Long>();
				if (objs != null) {
					for (Object obj : objs) {
						playerIds.add(Long.valueOf(obj.toString()));
					}
				}
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = friendFacade.blessFriends(playerId, playerIds);
		response.setValue(result);
		session.write(response);
	}
	
	
	/**
	 * 批量加好友
	 * 
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void addFriends(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		Object[] players = null;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.PLAYER_IDS)) {
				players = ParamUtils.getParameter(aso, ResponseKey.PLAYER_IDS, Object[].class);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Collection<FriendsVo> sender = new ArrayList<FriendsVo>();
		
		if (players != null && players.length > 0) {
			for (Object obj : players) {
				long targetId = Long.valueOf(obj.toString());
				ResultObject<Friend> result = friendFacade.addFriend(playerId, targetId);
				if (result.getResult() < FriendConstant.SUCCESS) {
					continue;
				}
				
				UserDomain userDomain = userManager.getUserDomain(targetId);
				if(userDomain == null){
					continue;
				}
				
				Friend friend = result.getValue();
				Player player = userDomain.getPlayer();
				PlayerBattle battle = userDomain.getBattle();
				boolean online =  userManager.isOnline(targetId);
				int isMarket = marketManager.isMarket(targetId) ? 1 : 0;
				boolean isGreet = friendFacade.isGreet(playerId, targetId);
				FriendsVo vo = FriendsVo.valueOf(friend, player, battle, isGreet, online, isMarket);
				sender.add(vo);
			}
		}
			
		response.setValue(sender.toArray());
		session.write(response);
	}
	
	
	/**
	 * 查看好友酒坛
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void loadOthersWine(IoSession session, Request request, Response response) {
		long playerId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.PLAYER_ID)) {
				playerId = ((Number) aso.get(ResponseKey.PLAYER_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		FriendsTreasure treasure = friendFacade.getFriendsBless(playerId);
		response.setValue(treasure.getWineMeasure());
		session.write(response);
	}
	
	
	/**
	 * 查看敬酒历史记录
	 * @param session
	 * @param request
	 * @param response
	 */
	protected void loadGreetHistory(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		FriendsTreasure treasure = friendFacade.getFriendsBless(playerId);
		Map<Long, Long> historyMap = treasure.getGreetHistoryMap();
		
		List<FriendsVo> voList = new ArrayList<FriendsVo>();
		if (historyMap != null && !historyMap.isEmpty()) {
			for (Map.Entry<Long, Long> entry : historyMap.entrySet()) {
				UserDomain userDomain = userManager.getUserDomain(entry.getKey());
				if (userDomain == null) {
					continue;
				}
				
				boolean isGreet = friendFacade.isGreet(playerId, entry.getKey());
				voList.add(FriendsVo.valueOf(userDomain.getPlayer(), isGreet, entry.getValue()));
			}
		}
		
		response.setValue(voList.toArray());
		session.write(response);
	}
	
}
