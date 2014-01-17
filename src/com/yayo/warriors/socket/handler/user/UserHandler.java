package com.yayo.warriors.socket.handler.user;

import static com.yayo.common.socket.type.ResponseCode.*;
import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.user.constant.UserConstant.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.event.EventBus;
import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.type.ResponseCode;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.event.LoginEvent;
import com.yayo.warriors.module.chat.facade.ChannelFacade;
import com.yayo.warriors.module.chat.model.Channel;
import com.yayo.warriors.module.chat.type.ChatChannel;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.onlines.manager.OnlineStatisticManager;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.facade.UserFacade;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.BindSessionResult;
import com.yayo.warriors.module.user.model.LoginResult;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.module.user.type.KickCode;
import com.yayo.warriors.module.user.vo.BranchingVO;
import com.yayo.warriors.module.user.vo.LoginVO;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.util.ParamUtils;

import flex.messaging.io.amf.ASObject;


@Component
public class UserHandler extends BaseHandler {

	@Autowired
	private EventBus eventBus;
	@Autowired
	private UserFacade userFacade;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ChannelFacade channelFacade;
	@Autowired
	private OnlineStatisticManager onlineStatisticManager;
	
	protected int getModule() {
		return Module.USER;
	}

	
	protected void inititialize() {
		putInvoker(UserCmd.HEART_BEAT, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				response.setValue( (byte)0 );
				session.write(response);
			}
		});
		
		putInvoker(UserCmd.ACCOUNT_LOGIN, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				accountLogin(session, request, response);
			}
		});

		putInvoker(UserCmd.CREATE_CHARACTER, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				createPlayer(session, request, response);
			}
		});

		putInvoker(UserCmd.LIST_BRANCHINGES, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				listBranchinges(session, request, response);
			}
		});

		putInvoker(UserCmd.SELECT_CHARACTER, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				selectCharacter(session, request, response);
			}
		});

		putInvoker(UserCmd.GET_PLAYER_ATTRIBUTE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getPlayerAttribute(session, request, response);
			}
		});

		putInvoker(UserCmd.BINDING_NEW_SESSION, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				bindingNewSession(session, request, response);
			}
		});
		
		putInvoker(UserCmd.CHANGE_CHANGE_MODE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				changePeaceMode(session, request, response);
			}
		});
		
		putInvoker(UserCmd.BACK_REVIVE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				backRevive(session, request, response);
			}
		});
		
		putInvoker(UserCmd.PROPS_REVIVE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				propsRevive(session, request, response);
			}
		});
		
		putInvoker(UserCmd.CAMP_BATTLE_REVIVE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				
			}
		});
		
		putInvoker(UserCmd.GUIDE_SAVE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				saveGuideStep(session, request, response);
			}
		});

		putInvoker(UserCmd.RECEIVE_GUIDE_REWARDS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				receiveGuideRewards(session, request, response);
			}
		});
		
		putInvoker(UserCmd.LIST_CAMP_PLAYERCOUNT, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				listCampPlayerCount(session, request, response);
			}
		});
		
		putInvoker(UserCmd.SAVE_ADULT_MESSAGE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				saveAdultMessage(session, request, response);
			}
		});
		
		putInvoker(UserCmd.SETTING_FASHIONSHOW, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				settingFashionShow(session, request, response);
			}
		});
		
		putInvoker(UserCmd.GET_PLAYER_ONLINETIME, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getPlayerOnlineTime(session, request, response);
			}
		});
		
		putInvoker(UserCmd.UPDATE_PLAYER_CAPACITY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				updatePlayerCapacity(session, request, response);
			}
		});
		
		putInvoker(UserCmd.REQ_OFFLINE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				Long playerId = sessionManager.getPlayerId(session);
				if(playerId != null){
					UserPushHelper.pushKickOff(playerId, KickCode.BLOCK_LOGIN, session);
				}
			}
		});
		
	}
	

	protected void updatePlayerCapacity(IoSession session, Request request, Response response) {
		int type = -1;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TYPE)) {
				type =  ((Number) aso.get(ResponseKey.TYPE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = userFacade.updatePlayerCapacity(playerId, type);
		response.setValue(result);
		session.write(response);
	}

	protected void getPlayerOnlineTime(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		Object[] value = userManager.getPlayerAttributes(playerId, AttributeKeys.ONLINE_TIMES);
		response.setValue(value[0]);
		session.write(response);
	}
	
	

	protected void settingFashionShow(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		boolean state = false;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.STATE)) {
				state =  (Boolean) aso.get(ResponseKey.STATE);
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		userFacade.saveFashionShow(playerId, state);
	}
	
	
	protected void saveAdultMessage(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int state = -1;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.STATE)) {
				state = ((Number) aso.get(ResponseKey.STATE)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
	
		int result = userFacade.saveAdultMessage(playerId, state);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, result);
		resultMap.put(ResponseKey.STATE, state);
		response.setValue(resultMap);
		session.write(response);
		
	}
	
	
	protected void listCampPlayerCount(IoSession session, Request request, Response response) {
		int knifeCount = onlineStatisticManager.getCampPlayerCount(Camp.KNIFE_CAMP);
		int swordCampCount = onlineStatisticManager.getCampPlayerCount(Camp.SWORD_CAMP);
		response.setValue(new BranchingVO[]{ BranchingVO.valueOf(Camp.KNIFE_CAMP.ordinal(), knifeCount),
											 BranchingVO.valueOf(Camp.SWORD_CAMP.ordinal(), swordCampCount) });
		session.write(response);
	}


	protected void receiveGuideRewards(IoSession session, Request request, Response response) {
		int rewardInfoId = -1;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if(aso.containsKey(ResponseKey.INFO)) {
				rewardInfoId = ((Number) aso.get(ResponseKey.INFO)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = userFacade.receiveGuideRewards(playerId, rewardInfoId);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, result);
		resultMap.put(ResponseKey.TYPE, rewardInfoId);
		response.setValue(resultMap);
		session.write(response);
	}


	protected void propsRevive(IoSession session, Request request, Response response){
		long playerId = sessionManager.getPlayerId(session);
		long propsId = 0;
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PROPS_ID)) {
				propsId = ((Number)aso.get(ResponseKey.PROPS_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = this.userFacade.propsRevive(playerId, propsId);
		
		response.setValue(result);
		session.write(response);
	}
	


	protected void backRevive(IoSession session, Request request, Response response){
		long playerId = sessionManager.getPlayerId(session);
	
		int result = this.userFacade.backRevive(playerId);
		response.setValue(result);
		session.write(response);
	}
	
	
	protected void changePeaceMode(IoSession session, Request request, Response response) {
		int fightMode = -1;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.MODE)) {
				fightMode = ParamUtils.getParameter(aso, ResponseKey.MODE, Integer.class).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		ResultObject<Integer> resultObject = userFacade.updateFightMode(playerId, fightMode, true);
		
		resultMap.put(ResponseKey.RESULT, resultObject.getResult());
		if(resultObject.getValue() != null) {
			resultMap.put(ResponseKey.MODE, resultObject.getValue());
		}
		response.setValue(resultMap);
		session.write(response);
	}

	protected void bindingNewSession(IoSession session, Request request, Response response) {
		String serialNum = "";
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.SERIAL_NUM)) {
				serialNum = (String) aso.get(ResponseKey.SERIAL_NUM);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		if(playerId > 0L) {
			UserPushHelper.pushKickOff(KickCode.LOGIN_DUPLICATE, Arrays.asList(playerId));
			return;
		}
		
		String clientIp = sessionManager.getRemoteIp(session);
		Map<String, Object> resultMap = new HashMap<String, Object>(8);
		ResultObject<BindSessionResult> resultObject = userFacade.bindNewSession(session, serialNum, clientIp);
		resultMap.put(ResponseKey.RESULT, resultObject.getResult());
		BindSessionResult bindSessionResult = resultObject.getValue();
		if(bindSessionResult != null) {
			resultMap.put(ResponseKey.X, bindSessionResult.getPositionX());
			resultMap.put(ResponseKey.Y, bindSessionResult.getPositionY());
			resultMap.put(ResponseKey.ID, bindSessionResult.getPlayerId());
			resultMap.put(ResponseKey.VALUES, bindSessionResult.getValues());
			resultMap.put(ResponseKey.PARAMS, bindSessionResult.getParams());
			resultMap.put(ResponseKey.MAPID, bindSessionResult.getMapId());
			resultMap.put(ResponseKey.DUNGEON_ID, bindSessionResult.getDungeonId());
			resultMap.put(ResponseKey.DUNGEON_BASE_ID, bindSessionResult.getDungeonBaseId());
		}
		response.setValue(resultMap);
		session.write(response);
	}
	
	protected void listBranchinges(IoSession session, Request request, Response response) {
		response.setValue(channelFacade.getBranchingOnlineInfo().toArray());
		session.write(response);
	}


	protected void getPlayerAttribute(IoSession session, Request request, Response response) {
		Object[] params = null;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.PLAYER_ID)) {
				playerId = ParamUtils.getParameter(aso, ResponseKey.PLAYER_ID, Long.class).longValue();
			}
			if(aso.containsKey(ResponseKey.PARAMS)) {
				params = ParamUtils.getParameter(aso, ResponseKey.PARAMS, Object[].class);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		Object[] values = userFacade.getPlayerAttribute(playerId, params);
		resultMap.put(ResponseKey.UNITID, UnitId.valueOf(playerId, ElementType.PLAYER));
		resultMap.put(ResponseKey.PARAMS, params);
		resultMap.put(ResponseKey.VALUES, values);
		response.setValue(resultMap);
		session.write(response);
	}


	protected void accountLogin(IoSession session, Request request, Response response) {
		String userName = "";
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.USERNAME)) {
				userName = ParamUtils.getParameter(aso, ResponseKey.USERNAME, String.class);
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		LoginResult loginResult = userFacade.getLoginResult(userName);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		List<LoginVO> loginResultList = loginResult.getLoginVoList();
		int result = loginResultList == null || loginResultList.isEmpty() ? ACCOUNT_NOT_FOUND : SUCCESS;
		resultMap.put(ResponseKey.RESULT, result);
		if (result >= CommonConstant.SUCCESS) {
			resultMap.put(ResponseKey.ROLES, loginResult.getLoginVoList().toArray() );
			UserPushHelper.pushKickOff(KickCode.LOGIN_DUPLICATE, loginResult.getOnlinePlayers());
		}
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void createPlayer(IoSession session, Request request, Response response) {
		int sex = -1;
		int job = -1;
		int icon = -1;
		String userName = "";
		String password = "";
		String playerName = "";
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.USERNAME)) {
				userName = ParamUtils.getParameter(aso, ResponseKey.USERNAME, String.class);
			}
			if (aso.containsKey(ResponseKey.PASSWORD)) {
				password = ParamUtils.getParameter(aso, ResponseKey.PASSWORD, String.class);
			}
			if (aso.containsKey(ResponseKey.PLAYER_NAME)) {
				playerName = ParamUtils.getParameter(aso, ResponseKey.PLAYER_NAME, String.class);
			}
			if (aso.containsKey(ResponseKey.SEX)) {
				sex = ParamUtils.getParameter(aso, ResponseKey.SEX, Integer.class).intValue();
			}
			if (aso.containsKey(ResponseKey.ICON)) {
				icon = ParamUtils.getParameter(aso, ResponseKey.ICON, Integer.class).intValue();
			}
			if (aso.containsKey(ResponseKey.JOB)) {
				job = ParamUtils.getParameter(aso, ResponseKey.JOB, Integer.class).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		int result = userFacade.createPlayer(userName, password, playerName, job, sex, icon);
		resultMap.put(ResponseKey.RESULT, result);
		if(result >= CommonConstant.SUCCESS) {
			LoginResult loginResult = userFacade.getLoginResult(userName);
			resultMap.put(ResponseKey.ROLES, loginResult.getLoginVoList().toArray() );
			UserPushHelper.pushKickOff(KickCode.LOGIN_DUPLICATE, loginResult.getOnlinePlayers());
		}
		response.setValue(resultMap);
		session.write(response);
	}

	
	protected void selectCharacter(IoSession session, Request request, Response response) {
		int branching = 0;
		long selectPlayerId = 0L;
		String userName = "";
		String password = "";
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.USERNAME)) {
				userName = ParamUtils.getParameter(aso, ResponseKey.USERNAME, String.class);
			}
			if (aso.containsKey(ResponseKey.PASSWORD)) {
				password = ParamUtils.getParameter(aso, ResponseKey.PASSWORD, String.class);
			}
			if (aso.containsKey(ResponseKey.PLAYER_ID)) {
				selectPlayerId = ParamUtils.getParameter(aso, ResponseKey.PLAYER_ID, Long.class).longValue();
			}
			if (aso.containsKey(ResponseKey.BRANCHING)) {
				branching = ParamUtils.getParameter(aso, ResponseKey.BRANCHING, Integer.class).intValue();
			}
		} catch (Exception e) {
			response.setStatus(RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}

		if(playerId > 0L) {
			UserPushHelper.pushKickOff(KickCode.LOGIN_DUPLICATE, Arrays.asList(playerId));
			return;
		}
		
		String clientIp = sessionManager.getRemoteIp(session);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		ResultObject<String> resultObject = userFacade.selectPlayer(userName, password, selectPlayerId, branching, clientIp);
		resultMap.put(ResponseKey.RESULT, resultObject.getResult());
		if(resultObject.getValue() != null) {
			resultMap.put(ResponseKey.SERIAL_NUM, resultObject.getValue());
		}
		response.setValue(resultMap);
		session.write(response);
	}

	public void bindLoginPlayerSession(Player player, IoSession session, int branching, String clientIp) {
		Long playerId = player.getId();
		IoSession exitSession = sessionManager.getIoSession(playerId);
		if (exitSession != null && exitSession.getId() != session.getId()) {
			UserPushHelper.pushKickOff(playerId, KickCode.LOGIN_DUPLICATE, exitSession);
		}

		sessionManager.put2OnlineList(playerId, session);					
		userManager.savePlayerLoginState(player);							
		this.enterGameChannels(player, branching);							
		eventBus.post(LoginEvent.valueOf(playerId, branching, clientIp));	
	}

	
	private void enterGameChannels(Player player, int branching) {
		channelFacade.enterChannel(player, new Channel[] {
			Channel.valueOf(ChatChannel.WORLD_CHANNEL.ordinal(), branching),
			Channel.valueOf(ChatChannel.CAMP_CHANNEL.ordinal(), player.getCamp().ordinal()),
		});
	}
	
	private void saveGuideStep(IoSession session, Request request, Response response) {
		Long playerId = sessionManager.getPlayerId(session);
		ASObject aso = (ASObject)request.getValue();
		int stepId = -1;
		if(aso != null && aso.containsKey(ResponseKey.ID)){
			stepId = ((Number)aso.get(ResponseKey.ID)).intValue();
		}
		int result = userFacade.saveGuideStep(playerId, stepId);
		response.setValue(result);
		session.write(response);
	}
	
}
