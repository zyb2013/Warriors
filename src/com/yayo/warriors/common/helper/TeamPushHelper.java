package com.yayo.warriors.common.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.team.facade.TeamFacade;
import com.yayo.warriors.module.team.model.Team;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.team.TeamCmd;

/**
 * 组队推送帮助类
 * 
 * @author Hyint
 */
@Component
public class TeamPushHelper {

//	@Autowired
//	private VOFactory voFactory;
	@Autowired
	private TeamFacade teamFacade;
	@Autowired
	private UserManager userManager;
	@Autowired
	private SessionManager sessionManager;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TeamPushHelper.class);
	private static ObjectReference<TeamPushHelper> ref = new ObjectReference<TeamPushHelper>();
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	private static TeamPushHelper getInstance() {
		return ref.get();
	}
	
	/**
	 * 得到组队的响应对象
	 * 
	 * @param  cmd				组队命令
	 * @return {@link Response}	响应对象
	 */
	private static Response getTeamResponse(int cmd) {
		return Response.defaultResponse(Module.TEAM, cmd);
	}
	
	/***
	 * 推送玩家被邀请信息
	 * 
	 * @param targetId			接受者角色ID
	 * @param inviteId			邀请者角色ID
	 * @param roleJob           邀请者的职业
	 * @param level             邀请者的等级
	 * @param playerName		邀请者角色名
	 * @param type              邀请-0|申请-1
	 */
	public static void pushPlayerInvite(long targetId, PlayerBattle playerBattle, String playerName, int inviteType) {
		Response response = getTeamResponse(TeamCmd.PUSH_INVITE_MESSAGE);
		Map<String, Object> resultMap = new HashMap<String, Object>(5);
		resultMap.put(ResponseKey.PLAYER_ID, playerBattle.getId());
		resultMap.put(ResponseKey.PLAYER_NAME, playerName);
		resultMap.put(ResponseKey.JOB, playerBattle.getJob().ordinal());
		resultMap.put(ResponseKey.LEVEL, playerBattle.getLevel());
		resultMap.put(ResponseKey.TYPE, inviteType);
		response.setValue(resultMap);
		getInstance().sessionManager.write(targetId, response);
	}

	/***
	 * 推送邀请被拒绝
	 * 
	 * @param targetId			接受信息的玩家ID
	 * @param playerName		拒绝的玩家姓名
	 */
	public static void pushPlayerReject(long targetId, String playerName) {
		Response response = getTeamResponse(TeamCmd.PUSH_PROCESS_INVITE_MESSAGE);
		response.setValue(playerName);
		getInstance().sessionManager.write(targetId, response);
	}
	
	/**
	 * 推送属性给组队玩家
	 * 
	 * @param playerId		属性发生改变的角色ID
	 */
	public static void pushAttribute2TeamMembers(long playerId) {
		Team team = getInstance().teamFacade.getPlayerTeam(playerId);
		if(team == null) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("角色:[{}] 没有队伍, 不需要推送属性变化信息", playerId);
			}
			return;
		}
		
		List<Long> playerIdList = new ArrayList<Long>(team.getMembers());
		if(playerIdList == null || playerIdList.isEmpty()) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("队伍:[{}] 成员人数为0, 不需要推送", team.getId());
			}
			return;
		}
		
		Object[] attributes = AttributeRule.TEAM_ATTRIBUTE_CHANGES;
		Object[] values = getInstance().userManager.getPlayerAttributes(playerId, attributes);
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.PLAYER_ID, playerId);
		resultMap.put(ResponseKey.PARAMS, attributes);
		resultMap.put(ResponseKey.VALUES, values);
		Response response = Response.defaultResponse(Module.TEAM, TeamCmd.PUSH_MEMBER_ATTRIBUTE_CHANGES, resultMap);
		getInstance().sessionManager.write(playerIdList, response);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("推送角色:[{}] 属性:[{}] 给区域玩家:[{}]");
		}
	}
	
	/**
	 * 推送队员被踢信息
	 * 
	 * @param targetId         对方Id
	 * @param TeamId           组队Id
	 * @param leaderId         队长Id
	 * @param reason           原因
	 */
	public static void pushMemberKicked(long targetId, int teamId, long leaderId, int reason) {
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.TEAM_ID, teamId);
		resultMap.put(ResponseKey.LEADER_ID, leaderId);
		resultMap.put(ResponseKey.REASON, reason);
		Response response = Response.defaultResponse(Module.TEAM, TeamCmd.PUSH_MEMBER_KICKED_MESSAGE, resultMap);
		getInstance().sessionManager.write(targetId, response);
	}
	
	/**
	 * 推送副本匹配成功信息
	 * 
	 * @param playerIds        玩家ID 
	 * @param dungeonBaseId    副本基础Id
	 * @param matchSuc         匹配成功?
	 */
	public static void pushMatchSuccess(List<Long> playerIds, boolean matchSuc, int dungeonBaseId) {
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.DUNGEON_BASE_ID, dungeonBaseId);
		resultMap.put(ResponseKey.TYPE, matchSuc);
		Response response = Response.defaultResponse(Module.TEAM, TeamCmd.PUSH_MATCH_SUCCESS_MESSAGE, resultMap);
		getInstance().sessionManager.write(playerIds, response);
	}
	
	/**
	 * 推送匹配状态信息给组队
	 * 
	 * @param leaderId         队长Id
	 * @param isMatch          是否匹配 
	 * @param playerIds        队伍人员 
	 */
//	public static void pushCancleMatchMessage(long leaderId, boolean isMatch, Long... playerIds) {
//		Response response = Response.defaultResponse(Module.TEAM, TeamCmd.PUSH_CANCAL_MATCH_MESSAGE);
//		List<Long> teamMemberId = Arrays.asList(playerIds);
//		Map<String, Object> resultMap = new HashMap<String, Object>();
//		resultMap.put(ResponseKey.LEADER_ID, leaderId);
//		resultMap.put(ResponseKey.TYPE, isMatch);
//		response.setValue(resultMap);
//		getInstance().sessionManager.write(teamMemberId, response);
//	}
	
	/**
	 * 推送玩家是否准备副本
	 * 
	 * @param readyOrNot       是否准备   
	 * @param playerName       玩家名字
	 * @param playerIds        组队玩家列表   
	 */
	public static void pushPlayerReadyMessage(long playerId, String name, boolean readyOrNot, Collection<Long> teamMemberId) {
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.PLAYER_ID, playerId);
		resultMap.put(ResponseKey.PLAYER_NAME, name);
		resultMap.put(ResponseKey.TYPE, readyOrNot);
		Response response = Response.defaultResponse(Module.TEAM, TeamCmd.PUSH_READY_MESSAGE, resultMap);
		getInstance().sessionManager.write(teamMemberId, response);
	}
	
	/**
	 * 推送组队可以进入副本了
	 * 
	 * @param DungeonBaseId    副本基础Id
	 * @param playerIds        组队玩家列表
	 */
	public static void pushTeamMembers2Dungeon(int dungeonBaseId, Collection<Long> teamMemberId) {
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.DUNGEON_BASE_ID, dungeonBaseId);
		Response response = Response.defaultResponse(Module.TEAM, TeamCmd.PUSH_TEAMMEMBERS_DUNGEON, resultMap);
		getInstance().sessionManager.write(teamMemberId, response);
	}
	
	/**
	 * 推送组队成员上线信息
	 * 
	 * @param playerId          角色id
	 */
	public static void pushTeamMemberLogin(long playerId) {
		Team team = getInstance().teamFacade.getPlayerTeam(playerId);
		if (team == null) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("角色[{}] 没有队伍, 不用推送组队上线信息", playerId);
			}
			return;
		}
		List<Long> teamMemberList = new ArrayList<Long>(team.getMembers());
		teamMemberList.remove(playerId);
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.TEAM_ID, team.getId());
		resultMap.put(ResponseKey.PLAYER_ID, playerId);
		Response response = Response.defaultResponse(Module.TEAM, TeamCmd.PUSH_TEAMMEMBER_LOGIN, resultMap);
		getInstance().sessionManager.write(teamMemberList, response);
	}
	
	/**
	 * 推送组队成员下线信息
	 * 
	 * @param playerId          角色id
	 */
	public static void pushTeamMemberLogout(long playerId) {
		Team team = getInstance().teamFacade.getPlayerTeam(playerId);
		if (team == null) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("角色[{}] 没有组队, 不用推送组队成员下线信息", playerId);
			}
			return;
		}
		
		boolean isTeamOnline = getInstance().teamFacade.isTeamOnline(playerId, team.getId());
		if (!isTeamOnline) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("角色[{}] 下线, 队伍无人在线, 自动解散队伍", playerId);
			}
			return;
		}
		
		List<Long> teamMemberList = new ArrayList<Long>(team.getMembers());
		teamMemberList.remove(playerId);
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.TEAM_ID, team.getId());
		resultMap.put(ResponseKey.PLAYER_ID, playerId);
		Response response = Response.defaultResponse(Module.TEAM, TeamCmd.PUSH_TEAMMEMBER_LOGOUT, resultMap);
		getInstance().sessionManager.write(teamMemberList, response);
	}
	
	/**
	 * 推送组队成员转场信息
	 * 
	 * @param playerId          角色ID    
	 * @param mapId             地图ID
	 */
	public static void pushMemberChangeScreen(long playerId) {
		Team team = getInstance().teamFacade.getPlayerTeam(playerId);
		if (team == null) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("角色[{}] 没有组队, 不用推送组队成员转场信息", playerId);
			}
			return;
		}
		
		UserDomain userDomain = getInstance().userManager.getUserDomain(playerId);
		if(userDomain == null || userDomain.getMotion() == null) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("角色[{}] 移动对象不存在, 不推送任何信息", playerId);
			}
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.TEAM_ID, team.getId());
		resultMap.put(ResponseKey.PLAYER_ID, playerId);
		resultMap.put(ResponseKey.MAPID, userDomain.getMotion().getMapId());
		Response response = Response.defaultResponse(Module.TEAM, TeamCmd.PUSH_MEMBER_CHANGE_SCREEN, resultMap);
		getInstance().sessionManager.write(team.getMembers(), response);
	}
	
}
