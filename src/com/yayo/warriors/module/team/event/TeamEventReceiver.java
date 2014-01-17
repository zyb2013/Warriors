package com.yayo.warriors.module.team.event;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.util.ConcurrentHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.event.AbstractReceiver;
import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.team.model.MemberVO;
import com.yayo.warriors.module.team.model.Team;
import com.yayo.warriors.module.team.type.AllocateMode;
import com.yayo.warriors.module.team.type.EventType;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.team.TeamCmd;

/**
 * 组队事件接收器
 * 
 * @author Hyint
 */
@Component
public class TeamEventReceiver extends AbstractReceiver<TeamEvent> {
	
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private SessionManager sessionManager;
	
	@Override
	public String[] getEventNames() {
		return new String[] { TeamEvent.NAME };
	}

	@Override
	public void doEvent(TeamEvent event) {
		if(event == null) {
			LOGGER.error("组队事件:[{}] 不存在", event);
			return;
		}
		
		EventType type = event.getType();
		if(type == null) {
			LOGGER.error("组队事件类型:[{}] 不存在", type);
			return;
		}
		
		Long[] playerIds = event.getMemberIdArr();
		if(playerIds == null) {
			return;
		}
		
		Team team = event.getTeam();
		if(team == null) {
			LOGGER.error("队伍事件, 队伍不存在");
			return;
		}
		
		int reason = event.getReason();
		boolean isLeader = event.isLeader();
		
		switch (type) {
			case JOIN_EVENT:		join(team, playerIds);						break;
			case LEFT_EVENT:		leave(team, reason, isLeader, playerIds);	break;
			case SWAP_EVENT:		swapLeader(team, playerIds);				break;
			case DISBAND_EVENT:		disband(team, reason, playerIds);			break;
		}
	}

	/**
	 * 角色加入组队
	 * 
	 * @param team			组队对象
	 * @param playerIds	 	加入组队的角色ID列表
	 */
	private void join(Team team, Long...playerIds) {
		if(playerIds.length <= 0) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("成员加入组队事件. 角色ID:[{}], 不推送", playerIds);
			}
			return;
		}
		
		int teamId = team.getId();
		long leaderId = team.getLeaderId();
		Set<Long> oldJoinMemnberIds = new HashSet<Long>();	//旧的加入列表
		List<Long> newJoinMemberIds = Arrays.asList(playerIds);
		ConcurrentHashSet<Long> teamMembers = team.getMembers();
		for (Long playerId : teamMembers) {
			if(!newJoinMemberIds.contains(playerId)) {
				oldJoinMemnberIds.add(playerId);
			}
		}
		
		//新加入队伍的角色列表
		List<MemberVO> memberVOList = voFactory.getTeamMemberVOList(newJoinMemberIds);
		if(memberVOList == null || newJoinMemberIds.isEmpty()) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("加入组队:[{}] 邀请事件, 成员VO对象列表不存在");
			}
			return;
		}
		
		//原来已经在队伍里面的成员, 将看到新加入的成员
		int teamMode = team.getTeamMethod();
		AllocateMode allocateMode = team.getAllocateMode();
		Map<String, Object> resultMap = new HashMap<String, Object>(5);
		resultMap.put(ResponseKey.TEAM_ID, teamId);
		resultMap.put(ResponseKey.LEADER_ID, leaderId);
		resultMap.put(ResponseKey.TEAM_MODE, teamMode);
		resultMap.put(ResponseKey.ALLOCATE_TYPE, allocateMode);
		resultMap.put(ResponseKey.MEMBERS, memberVOList.toArray());
		Response response = Response.defaultResponse(Module.TEAM, TeamCmd.PUSH_MEMBER_JOIN_MESSAGE, resultMap);
		sessionManager.write(oldJoinMemnberIds, response);
		
		List<MemberVO> totalMemberVOList = voFactory.getTeamMemberVOList(teamMembers);
		if(totalMemberVOList != null && !totalMemberVOList.isEmpty()) {
			Map<String, Object> newResultMap = new HashMap<String, Object>();
			newResultMap.put(ResponseKey.TEAM_ID, teamId);
			newResultMap.put(ResponseKey.TEAM_MODE, teamMode);
			newResultMap.put(ResponseKey.LEADER_ID, leaderId);
			newResultMap.put(ResponseKey.ALLOCATE_TYPE, allocateMode);
			newResultMap.put(ResponseKey.MEMBERS, totalMemberVOList.toArray());
			Response newJoinResponse = Response.defaultResponse(Module.TEAM, TeamCmd.PUSH_MEMBER_JOIN_MESSAGE, newResultMap);
			sessionManager.write(newJoinMemberIds, newJoinResponse);
		}
	}
	
	/**
	 * 离开组队
	 * 
	 * @param  team				队伍对象
	 * @param  reason			离开原因
	 * @param  playerIds		离开队伍的角色ID
	 */
	private void leave(Team team, int reason, boolean isLeader, Long...playerIds) {
		if(playerIds.length <= 0) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("成员离开组队事件. 角色ID:[{}], 不推送", playerIds);
			}
			return;
		}
		
		int teamId = team.getId();
		long leaderId = team.getLeaderId();
		List<Long> memberIdList = Arrays.asList(playerIds);
		List<MemberVO> memberVOList = voFactory.getTeamMemberVOList(memberIdList);
		Map<String, Object> resultMap = new HashMap<String, Object>(5);
		resultMap.put(ResponseKey.REASON, reason);
		resultMap.put(ResponseKey.TEAM_ID, teamId);
		resultMap.put(ResponseKey.ISLEADER, isLeader);
		resultMap.put(ResponseKey.LEADER_ID, leaderId);
		resultMap.put(ResponseKey.MEMBERS, memberVOList.toArray());
		Response response = Response.defaultResponse(Module.TEAM, TeamCmd.PUSH_MEMBER_LEAVE_MESSAGE);
		response.setValue(resultMap);
		sessionManager.write(team.getMembers(), response);
	}
	
	/**
	 * 推送转换队长信息
	 * 
	 * @param  team				组队信息
	 * @param  playerIds		角色ID数组
	 */
	private void swapLeader(Team team, Long...playerIds) {
		if(playerIds.length <= 0) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("成员交换组队事件. 角色ID:[{}], 不推送", playerIds);
			}
			return;
		}
		
		int teamId = team.getId();
		long lastLeaderId = playerIds[0];
		long currLeaderId = team.getLeaderId();
		MemberVO lastLeaderMemberVO = voFactory.getTeamMemberVO(lastLeaderId);
		MemberVO currLeaderMemberVO = voFactory.getTeamMemberVO(currLeaderId);
		if(lastLeaderMemberVO == null || currLeaderMemberVO == null) {
			LOGGER.error("LAST:[{}] , CURR:[{}] ", lastLeaderMemberVO, currLeaderMemberVO);
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.TEAM_ID, teamId);
		resultMap.put(ResponseKey.TARGET, lastLeaderMemberVO);
		resultMap.put(ResponseKey.LEADER, currLeaderMemberVO);
		Response response = Response.defaultResponse(Module.TEAM, TeamCmd.PUSH_MEMBER_SWAP_MESSAGE);
		response.setValue(resultMap);
		sessionManager.write(team.getMembers(), response);
	}
	
	/**
	 * 解散组队
	 * 
	 * @param team
	 * @param playerIds
	 */
	private void disband(Team team, int reason, Long...playerIds) {
		if(playerIds.length <= 0) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("成员解散组队事件. 角色ID:[{}]", playerIds);
			}
			return;
		} 
		
		if(team.isEmpty()) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("队伍中没有成员了, 不需要推送解散信息");
			}
			return;
		}
		
		int teamId = team.getId();
//		List<Long> memberIdList = Arrays.asList(playerIds);
//		List<MemberVO> memberVOList = voFactory.getTeamMemberVOList(memberIdList);
		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put(ResponseKey.REASON, reason);
		resultMap.put(ResponseKey.TEAM_ID, teamId);
		resultMap.put(ResponseKey.LEADER_ID, team.getLeaderId());
//		resultMap.put(ResponseKey.MEMBERS, memberVOList.toArray(new MemberVO[0]));
		Response response = Response.defaultResponse(Module.TEAM, TeamCmd.PUSH_MEMBER_DISBAND_MESSAGE);
		response.setValue(resultMap);
		sessionManager.write(team.getMembers(), response);

	}
}
