package com.yayo.warriors.socket.handler.team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.handler.BaseHandler;
import com.yayo.common.socket.handler.Invoker;
import com.yayo.common.socket.message.Request;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.type.ResponseCode;
import com.yayo.common.utility.CollectionUtils;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.team.constant.TeamConstant;
import com.yayo.warriors.module.team.facade.TeamFacade;
import com.yayo.warriors.module.team.model.MemberVO;
import com.yayo.warriors.module.team.model.QueryMemberVO;
import com.yayo.warriors.module.team.model.QueryTeamVO;
import com.yayo.warriors.module.team.model.Team;
import com.yayo.warriors.module.team.model.TeamVO;
import com.yayo.warriors.module.team.type.TeamReason;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;

import flex.messaging.io.amf.ASObject;


@Component
public class TeamHandler extends BaseHandler {

	@Autowired
	private VOFactory voFactory;
	@Autowired
	private TeamFacade teamFacade;
	@Autowired
	private UserManager userManager;

	
	protected int getModule() {
		return Module.TEAM;
	}

	
	protected void inititialize() {
		putInvoker(TeamCmd.SEND_INVITE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				sendInvite(session, request, response);
			}
		});
		
		putInvoker(TeamCmd.PROCESS_INVITE, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				processInvite(session, request, response);
			}
		});
		
		putInvoker(TeamCmd.SWAPE_LEADER, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				swapeLeader(session, request, response);
			}
		});
		
		putInvoker(TeamCmd.LEAVE_TEAM, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				leaveTeam(session, request, response);
			}
		});
		
		putInvoker(TeamCmd.KICK_OUT_TEAM, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				kickOutTeam(session, request, response);
			}
		});
		
		putInvoker(TeamCmd.DISBAND_TEAM, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				disbandTeam(session, request, response);
			}
		});

		putInvoker(TeamCmd.GET_TEAM_INFO, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				getTeamInfo(session, request, response);
			}
		});

		putInvoker(TeamCmd.CREATE_TEAM, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				createTeam(session, request, response);
			}
		});

		putInvoker(TeamCmd.LIST_AREA_TEAM, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				listAreaTeams(session, request, response);
			}
		});
		
		putInvoker(TeamCmd.LIST_AREA_MEMBERS, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				listAreaMembers(session, request, response);
			}
		});
		
		putInvoker(TeamCmd.APPLY_TEAM, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				apply2Team(session, request, response);
			}
		});
		
		putInvoker(TeamCmd.REPLY, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				reply(session, request, response);
			}
		});
		
		putInvoker(TeamCmd.READY_ENTER, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				ready2EnterDungeon(session, request, response);
			}
		});
		
		putInvoker(TeamCmd.REJECT_ENTER, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				reject2Dungeon(session, request, response);
			}
		});
		
		putInvoker(TeamCmd.START_DUNGEON, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				startDungeon(session, request, response);
			}
		});
		
		putInvoker(TeamCmd.TEAM_ENTER_DUNGEON, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				teamEnterDungeon(session, request, response);
			}
		});
		
		putInvoker(TeamCmd.CANCLE_ENTER_DUNGEON, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				cancleEnterDungeon(session, request, response);
			}
		});
		
		putInvoker(TeamCmd.ONLINE_LOAD_TEAM, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				playerOnlineLoadTeam(session, request, response);
			}
		});
		
		putInvoker(TeamCmd.CHECK_PLAYER_TEAM, new Invoker() {
			
			public void invoke(IoSession session, Request request, Response response) {
				checkPlayerTeam(session, request, response);
			}
		});
	}
	

	protected void listAreaMembers(IoSession session, Request request, Response response) {
		int start = 0;                 
		int pageSize = 0;              
		boolean refresh = false;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.START)) {
				start = ((Number) aso.get(ResponseKey.START)).intValue();
			}
			if(aso.containsKey(ResponseKey.PAGE_SIZE)) {
				pageSize = ((Number) aso.get(ResponseKey.PAGE_SIZE)).intValue();
			}
			if(aso.containsKey(ResponseKey.TYPE)) {
				refresh = (Boolean) aso.get(ResponseKey.TYPE);
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int total = 0;
		int index = (start - 1) * pageSize;                                          
		List<Long> memberIdList = null;
		List<Long> areaIdList = teamFacade.listAreaMemberId(playerId, refresh);
		if(areaIdList != null && !areaIdList.isEmpty()) {
			total = areaIdList.size();
			memberIdList = CollectionUtils.subListCopy(areaIdList, index, pageSize); 
		}
		
		List<QueryMemberVO> queryMemberVOList = voFactory.getQueryMemberVO(memberIdList);
		Map<String, Object> resultMap = new HashMap<String, Object>(4);
		resultMap.put(ResponseKey.RESULT, TeamConstant.SUCCESS);
		resultMap.put(ResponseKey.START, start);
		resultMap.put(ResponseKey.TOTAL, total);
		resultMap.put(ResponseKey.TEAMS, queryMemberVOList.toArray());
		
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void listAreaTeams(IoSession session, Request request, Response response) {
		int start = 0;
		int pageSize = 0;
		boolean refresh = false;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.START)) {
				start = ((Number) aso.get(ResponseKey.START)).intValue();
			}
			if(aso.containsKey(ResponseKey.PAGE_SIZE)) {
				pageSize = ((Number) aso.get(ResponseKey.PAGE_SIZE)).intValue();
			}
			if(aso.containsKey(ResponseKey.TYPE)) {
				refresh = (Boolean) aso.get(ResponseKey.TYPE);
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int total = 0;
		List<Integer> teamIds = null;
		List<Integer> teamIdList = teamFacade.listAreaTeamId(playerId, refresh);
		if(teamIdList != null && !teamIdList.isEmpty()) {
			total = teamIdList.size();
			teamIds = CollectionUtils.subListCopy(teamIdList, start, pageSize);
		}
		
		List<QueryTeamVO> queryTeamVOList = voFactory.getQueryTeamVO(playerId, teamIds);
		Map<String, Object> resultMap = new HashMap<String, Object>(4);
		resultMap.put(ResponseKey.RESULT, TeamConstant.SUCCESS);
		resultMap.put(ResponseKey.START, start);
		resultMap.put(ResponseKey.TOTAL, total);
		resultMap.put(ResponseKey.TEAMS, queryTeamVOList.toArray());
		
		response.setValue(resultMap);
		session.write(response);
	}

	
	protected void createTeam(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		Team team = teamFacade.createTeam(playerId);
		TeamVO teamVO = voFactory.getTeamVO(team);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, TeamConstant.SUCCESS);
		resultMap.put(ResponseKey.TEAMS, teamVO);
		response.setValue(resultMap);
		session.write(response);
	}

	
	protected void getTeamInfo(IoSession session, Request request, Response response) {
		int teamId = -1;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.TEAM_ID)) {
				teamId = ((Number) aso.get(ResponseKey.TEAM_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Team team = null;
		if(teamId > 0) {
			team = teamFacade.getTeam(teamId);
		} else {
			team = teamFacade.getPlayerTeam(playerId);
		}
		
		TeamVO teamVO = voFactory.getTeamVO(team);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, TeamConstant.SUCCESS);
		resultMap.put(ResponseKey.TEAMS, teamVO);
		response.setValue(resultMap);
		session.write(response);
	}

	
	protected void disbandTeam(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int result = teamFacade.disband(playerId);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		session.write(response);
	}

	
	protected void kickOutTeam(IoSession session, Request request, Response response) {
		long targetId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = teamFacade.kick(playerId, targetId);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		if (result < TeamConstant.SUCCESS) {
			resultMap.put(ResponseKey.RESULT, result);
		} else {
			resultMap.put(ResponseKey.RESULT, TeamConstant.SUCCESS);
			resultMap.put(ResponseKey.VALUES, result);
		}
		response.setValue(resultMap);
		session.write(response);
		
	}

	
	protected void leaveTeam(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int result = teamFacade.leave(playerId, TeamReason.LEAVE_ACTION);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		if (result < TeamConstant.SUCCESS) {
			resultMap.put(ResponseKey.RESULT, result);
		} else {
			resultMap.put(ResponseKey.RESULT, TeamConstant.SUCCESS);
			resultMap.put(ResponseKey.VALUES, result);
		}
		response.setValue(resultMap);
		session.write(response);
	}


	protected void swapeLeader(IoSession session, Request request, Response response) {
		long targetId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = teamFacade.swapLeader(playerId, targetId);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		session.write(response);
	}


	protected void processInvite(IoSession session, Request request, Response response) {
		long targetId = 0L;
		boolean accept = false;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
			if(aso.containsKey(ResponseKey.TYPE)) {
				accept = (Boolean) aso.get(ResponseKey.TYPE);
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = TeamConstant.FAILURE;
		if(accept) {
			result = teamFacade.accept(playerId, targetId);
		} else {
			result = teamFacade.reject(playerId, targetId);
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		if (result >= TeamConstant.SUCCESS) {
			resultMap.put(ResponseKey.RESULT, TeamConstant.SUCCESS);
			resultMap.put(ResponseKey.TEAM_ID, result);
		} else {
			resultMap.put(ResponseKey.RESULT, result);
		}
		response.setValue(resultMap);
		session.write(response);
	}


	protected void sendInvite(IoSession session, Request request, Response response) {
		long targetId = 0L;
		long playerId = sessionManager.getPlayerId(session);
		try {
			ASObject aso = (ASObject)request.getValue();
			if(aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = teamFacade.invite(playerId, targetId);
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.RESULT, result);
		resultMap.put(ResponseKey.TARGET_ID, targetId);
		
		response.setValue(resultMap);
		session.write(response);
	}
	

	protected void apply2Team(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = teamFacade.applyTeam(playerId, targetId);
		
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		if (result < TeamConstant.SUCCESS) {
			resultMap.put(ResponseKey.RESULT, result);
		} else {
			resultMap.put(ResponseKey.RESULT, CommonConstant.SUCCESS);
			resultMap.put(ResponseKey.TEAM_ID, result);
		}
		
		response.setValue(resultMap);
		session.write(response);
	}
	

	protected void reply(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		long targetId = 0L;
		boolean accept = false;
		
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.TARGET_ID)) {
				targetId = ((Number) aso.get(ResponseKey.TARGET_ID)).longValue();
			}
			if (aso.containsKey(ResponseKey.TYPE)) {
				accept = (Boolean) aso.get(ResponseKey.TYPE);
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		int result = TeamConstant.FAILURE;
		if (accept) {
			result = teamFacade.reply(playerId, targetId);
		} else {
			result = teamFacade.reject(targetId, playerId);
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		if (result == TeamConstant.SUCCESS) {
			resultMap.put(ResponseKey.RESULT, TeamConstant.SUCCESS);
			resultMap.put(ResponseKey.TEAM_ID, result);
		} else {
			resultMap.put(ResponseKey.RESULT, result);
		}
		response.setValue(resultMap);
		session.write(response);
	}
	

	

	protected void ready2EnterDungeon(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int dungeonBaseId = 0;
		
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.DUNGEON_BASE_ID)) {
				dungeonBaseId = ((Number) aso.get(ResponseKey.DUNGEON_BASE_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		} 
		
		int result = teamFacade.ready2EnterDungeon(playerId, dungeonBaseId);
		
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		session.write(response);
	}
	

	protected void reject2Dungeon(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int dungeonBaseId = 0;
		
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.DUNGEON_BASE_ID)) {
				dungeonBaseId = ((Number) aso.get(ResponseKey.DUNGEON_BASE_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		} 
		
		int result = teamFacade.reject2EnterDungeon(playerId, dungeonBaseId);
		
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		session.write(response);
	}
	
	
	protected void startDungeon(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int dungeonBaseId = 0;
		
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.DUNGEON_BASE_ID)) {
				dungeonBaseId = ((Number) aso.get(ResponseKey.DUNGEON_BASE_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		} 
		
		int result = teamFacade.startDungeon(playerId, dungeonBaseId);

		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		session.write(response);
	}
	

	protected void teamEnterDungeon(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int dungeonBaseId = 0;
		
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.DUNGEON_BASE_ID)) {
				dungeonBaseId = ((Number) aso.get(ResponseKey.DUNGEON_BASE_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		} 
		
		int result = teamFacade.teamEnterDungeon(playerId, dungeonBaseId);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		session.write(response);
	}
	

	protected void cancleEnterDungeon(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		int dungeonBaseId = 0;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.DUNGEON_BASE_ID)) {
				dungeonBaseId = ((Number) aso.get(ResponseKey.DUNGEON_BASE_ID)).intValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		} 
		
		int result = teamFacade.cancle2Dungeon(playerId, dungeonBaseId);
		Map<String, Object> resultMap = new HashMap<String, Object>(1);
		resultMap.put(ResponseKey.RESULT, result);
		response.setValue(resultMap);
		
		session.write(response);
	}
	

	protected void playerOnlineLoadTeam(IoSession session, Request request, Response response) {
		long playerId = sessionManager.getPlayerId(session);
		
		Team team = teamFacade.getPlayerTeam(playerId);
		if (team == null) {
			response.setValue(TeamConstant.SUCCESS);
			session.write(response);
			return;
		}
		
		List<MemberVO> memberList = voFactory.getTeamMemberVOList(team.getMembers()); 
		
		Map<String, Object> resultMap = new HashMap<String, Object>(5);
		resultMap.put(ResponseKey.TEAM_ID, team.getId());
		resultMap.put(ResponseKey.TEAM_MODE, team.getTeamMethod());
		resultMap.put(ResponseKey.LEADER_ID, team.getLeaderId());
		resultMap.put(ResponseKey.MEMBERS, memberList.toArray());
		resultMap.put(ResponseKey.ALLOCATE_TYPE, team.getAllocateMode());
		response.setValue(resultMap);
		session.write(response);
	}
	

	protected void checkPlayerTeam(IoSession session, Request request, Response response) {
		long playerId = 0L;
		try {
			ASObject aso = (ASObject) request.getValue();
			if (aso.containsKey(ResponseKey.PLAYER_ID)) {
				playerId = ((Number) aso.get(ResponseKey.PLAYER_ID)).longValue();
			}
		} catch (Exception e) {
			response.setStatus(ResponseCode.RESPONSE_CODE_ERROR);
			session.write(response);
			return;
		}
		
		Team team = teamFacade.getPlayerTeam(playerId);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(ResponseKey.PLAYER_ID, playerId);
		resultMap.put(ResponseKey.LEADER_ID, team == null ? 0 : team.getLeaderId());
		response.setValue(resultMap);
		session.write(response);
	}
	
}
