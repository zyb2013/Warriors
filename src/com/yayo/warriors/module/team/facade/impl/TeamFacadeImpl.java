package com.yayo.warriors.module.team.facade.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.team.constant.TeamConstant.*;
import static com.yayo.warriors.module.achieve.model.AchieveType.FIRST_ACHIEVE;
import static com.yayo.warriors.module.achieve.model.FirstType.FIRST_CREATE_TEAM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.mina.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.event.EventBus;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.model.DungeonConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.DungeonPushHelper;
import com.yayo.warriors.common.helper.TeamPushHelper;
import com.yayo.warriors.module.achieve.facade.AchieveFacade;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.team.constant.TeamConstant;
import com.yayo.warriors.module.team.event.TeamEvent;
import com.yayo.warriors.module.team.facade.TeamFacade;
import com.yayo.warriors.module.team.manager.TeamManager;
import com.yayo.warriors.module.team.model.DungeonTeam;
import com.yayo.warriors.module.team.model.Team;
import com.yayo.warriors.module.team.rule.TeamRule;
import com.yayo.warriors.module.team.type.TeamReason;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.module.user.type.PlayerStateKey;

/**
 * 组队接口
 * 
 * @author Hyint
 */
@Component
public class TeamFacadeImpl implements TeamFacade, LogoutListener {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	private EventBus eventBus;
	@Autowired
	private UserManager userManager;
	@Autowired
	private TeamManager teamManager;
	@Autowired
	private DungeonManager dungeonManager;
	@Autowired
	private DungeonPushHelper dungeonHelper;
	@Autowired
	private AchieveFacade achieveFacade;
	
	/**
	 * 获得组队信息
	 * 
	 * @param  teamId			队伍ID
	 * @return {@link Team}		组队信息
	 */
	
	public Team getTeam(int teamId) {
		return teamManager.getTeam(teamId);
	}

	
	public void onLoginEvent(UserDomain userDomain, int branching) {
		TeamPushHelper.pushTeamMemberLogin(userDomain.getPlayerId());		
	}

	/**
	 * 验证是否有组队成员在线
	 * (true-有成员在线, false-队伍无人在线)
	 * @param teamId            队伍Id
	 * @return
	 */
	
	public boolean isTeamOnline(long playerId, int teamId) {
		Team team = teamManager.getTeam(teamId);
		if (team == null) {
			return false;
		}
		
		long leaderId = team.getLeaderId();
		for (long memberId : team.getMembers()) {
			if (memberId == playerId) {
				continue;
			}
			
			boolean online = userManager.isOnline(memberId);
			if (leaderId == playerId) {
				swapLeader(leaderId, memberId);
			}
			if (online) {
				return true;
			}
		}
		disband(team, TeamReason.LEAVE_LOGOUT, team.getLeaderId());
		return false;
	}
	
	/**
	 * 角色登出保存数据接口
	 * 
	 * @param playerId	角色ID
	 */
	
	public void onLogoutEvent(UserDomain userDomain) {
		Player player = userDomain.getPlayer();
		TeamPushHelper.pushTeamMemberLogout(player.getId());
		player.removeAttribute(PlayerStateKey.TEAM_BE_INVITES);    
	}
	
	/** 
	 * 创建组队 
	 * 
	 * @param  playerId			角色ID
	 * @return {@link Team}		组队对象
	 */
	
	public Team createTeam(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return null;
		}
		
		Player player = userDomain.getPlayer();
		Team playerTeam = this.getPlayerTeam(playerId);
		if(playerTeam != null) {
			return playerTeam;
		}
		
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			int teamId = player.getTeamId();
			Team team = teamManager.getTeam(teamId);
			if(team != null) {
				return team;
			}
			
			team = Team.valueOf(playerId);
			teamManager.putTeam2Cache(team);
			player.setTeamId(team.getId());
			achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_CREATE_TEAM);
			return team;
		} catch (Exception e) {
			LOGGER.error("角色:[{}] 创建组队异常{}", playerId, e);
			return null;
		} finally {
			lock.unlock();
		}
	}


	/**
	 * 查询玩家的组队信息
	 * 
	 * @param  playerId			角色ID
	 * @return {@link Team}		组队对象
	 */
	
	public Team getPlayerTeam(long playerId) {
		return teamManager.getPlayerTeam(playerId);
	}

	/**
	 * 邀请玩家加入组队
	 * 
	 * @param  playerId			发起邀请的角色ID
	 * @param  targetId			被邀请人的角色ID
	 * @return {@link Integer}	组队返回值
	 */
	
	public int invite(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();		//主动发起邀请的玩家
		Player target = targetDomain.getPlayer();	//被邀请人
		if(!userManager.isOnline(targetId)) {
			return TARGET_OFF_LINE;
		}
		
		if (player.getCamp() != target.getCamp()) {
			return CAMP_DIFFERENCE;
		}
		
		Set<Long> inviters = getMenberBeInvites(target);
		if (inviters.contains(playerId)) {
			return INVITE_SENDED;
		}
		
		PlayerDungeon dungeon = dungeonManager.getPlayerDungeon(playerId);
		if (dungeon == null) {
			return PLAYER_NOT_FOUND;
		} else if (dungeon.isDungeonStatus()) {
			return TEAM_IN_DUNGEON;
		}
		
		Team team = this.getPlayerTeam(playerId);
		if(team != null && team.contains(playerId)) {	//已经有组队了. 需要判断是否有这个人
			if(team.getLeaderId() != playerId) {
				return MUST_TEAM_LEADER;
			} else if(team.size() >= TeamRule.MAX_TEAM_MEMBERS) {
				return TEAM_MEMBER_FULLED;
			} else if(team.contains(targetId)) {
				return TARGET_IN_YOUR_TEAM;
			}
		}
		
		// 玩家已加入队伍
		Team targetTeam = this.getPlayerTeam(targetId);
		if(targetTeam != null && targetTeam.contains(targetId)) {
			return TARGET_IN_TEAM;
		}

		int result = checkBranching(player, target);
		if(result < SUCCESS) {
			return result;
		}
		
		inviters.add(playerId);	                         //设置被邀请人, 谁邀请了他
		
		PlayerBattle playerBattle = userDomain.getBattle();
		TeamPushHelper.pushPlayerInvite(targetId, playerBattle, player.getName(), 0);    // 邀请 - 0
		return SUCCESS;
	}
	
	/**
	 * 玩家的被邀请列表
	 * 
	 * @param  player		角色对象
	 * @return Set<Long>	已邀请的角色
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Set<Long> getMenberBeInvites(Player player) {
		PlayerStateKey key = PlayerStateKey.TEAM_BE_INVITES;
		Class<ConcurrentHashSet> returnType = ConcurrentHashSet.class;
		ConcurrentHashSet<Long> invites = player.getAttribute(key, returnType);
		if(invites != null) {
			return invites;
		}
		
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			invites = player.getAttribute(key, returnType);
			if(invites != null) {
				return invites;
			}
			invites = new ConcurrentHashSet<Long>();
			player.setAttribute(key, invites);
			return invites;
		} catch (Exception e) {
			LOGGER.error("玩家的被邀请: {}", e);
			return player.getAttribute(key, returnType);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 验证玩家的区域
	 * 
	 * @param  leader			队长所在的分线
	 * @param  target			目标玩家所在的分分线
	 * @return {@link Integer}	是否成功
	 */
	private int checkBranching(Player leader, Player target) {
		int playerBranching = leader.getBranching();
		int targetBranching = target.getBranching();
		if(playerBranching != targetBranching) {
			return BRANCHING_DIFFERENCE;
		}
		return SUCCESS;
	}
	
	/**
	 * 接受组队邀请.
	 * 
	 * @param  playerId			被邀请人ID
	 * @param  leaderId			邀请人的ID
	 * @return {@link Integer}	组队返回值
	 */
	
	public int accept(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		if(!userManager.isOnline(targetId)) {
			return TARGET_OFF_LINE;
		}
		
		PlayerDungeon dungeon = dungeonManager.getPlayerDungeon(playerId);
		if (dungeon == null) {
			return PLAYER_NOT_FOUND;
		} else if (dungeon.isDungeonStatus()) {
			return TEAM_IN_DUNGEON;
		}
		
		Player player = userDomain.getPlayer();
		Player target = targetDomain.getPlayer();
		if (player.getCamp() != target.getCamp()) {
			return CAMP_DIFFERENCE;
		}

		// 是否同一分线
		int result = checkBranching(target, player);
		if(result < SUCCESS) {
			this.getMenberBeInvites(player).remove(playerId);
			return result;
		}
		
		// 验证接受者是否已加入队伍了
		Team playerTeam = getPlayerTeam(playerId);
		if(playerTeam != null && playerTeam.contains(playerId)) {
			return PLAYER_IN_TEAM;
		}
		
		Set<Long> beInvites = this.getMenberBeInvites(player);
		if(beInvites == null || !beInvites.contains(targetId)){
			return INVITE_NOT_FOUND;
		}

		boolean newTeam = false;
		Team team = getPlayerTeam(targetId);
		if (team == null) {		// 队伍不存在, 创建新队伍
			newTeam = true;
			team = Team.valueOf(targetId);
			teamManager.putTeam2Cache(team);
		} else if (targetId == team.getLeaderId()) {
			if(team.size() >= TeamRule.MAX_TEAM_MEMBERS){ // 队伍人数已满
				return TEAM_MEMBER_FULLED;
			}
		} else { // 队长有队伍, 但是却不是队长
			return INVITE_NOT_FOUND;
		}

		// 加入队伍
		team.add(playerId);
		// 清除自己的邀请列表
		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			player.setTeamId(team.getId());
			player.removeAttribute(PlayerStateKey.TEAM_BE_INVITES);
		} finally {
			lock.unlock();
		}
		
		if (newTeam) {
			ChainLock leaderLock = LockUtils.getLock(target);
			try {
				leaderLock.lock();
				target.setTeamId(team.getId());
				target.removeAttribute(PlayerStateKey.TEAM_BE_INVITES);
			} finally {
				leaderLock.unlock();
			}
			eventBus.post(TeamEvent.join(team, targetId, playerId));
		} else {
			eventBus.post(TeamEvent.join(team, playerId));
		}
		
		flushTeamMemberAttributes(team.getMembers());    // 刷新属性
		achieveFacade.firstAchieved(playerId, FIRST_ACHIEVE, FIRST_CREATE_TEAM);
		return team.getId();
	}

	/**
	 * 拒绝组队邀请.
	 * 
	 * @param  playerId			被邀请人ID
	 * @param  leaderId			邀请人的ID
	 * @return {@link Integer}	组队返回值
	 */
	
	public int reject(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		Set<Long> invites = this.getMenberBeInvites(player);
		if(invites != null && invites.contains(targetId)) {
			invites.remove(targetId);
			TeamPushHelper.pushPlayerReject(targetId, player.getName());
		}
		return SUCCESS;
	}

	/**
	 * 解散组队
	 * 
	 * @param  playerId			角色ID
	 * @return {@link Integer}	组队返回值
	 */
	
	public int disband(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		if(player == null) {
			return PLAYER_NOT_FOUND;
		}
		
		PlayerDungeon dungeon = dungeonManager.getPlayerDungeon(playerId);
		if (dungeon == null) {
			return PLAYER_NOT_FOUND;
		}
		
		// 组队
		Team team = this.getPlayerTeam(playerId);
		if(team == null) {
			return TEAM_NOT_FOUND;
		}
		
		long leaderId = team.getLeaderId();
		if(leaderId != playerId) {
			return MUST_TEAM_LEADER;
		}
		
		this.disband(team, TeamReason.DISBAND_LEADER, playerId);
		
		if (dungeon.isDungeonStatus()) {
			dungeonHelper.pushCoerceleave(team.getMembers());
		}
		
		return SUCCESS;
	}

	/**
	 * 解散组队
	 * 
	 * @param team
	 */
	private void disband(Team team, int reason, long playerId) {
		teamManager.removeTeam(team.getId());
		for(Long memberId : team.getMembers()) {	// 清除队员所属组队
			UserDomain userDomain = userManager.getUserDomain(memberId);
			if(userDomain == null) {
				continue;
			}
			
			Player member = userDomain.getPlayer();
			if(member == null) {
				continue;
			}
			
			PlayerBattle battle = userDomain.getBattle();
			ChainLock lock = LockUtils.getLock(member, battle);
			try {
				lock.lock();
				member.setTeamId(TeamRule.NO_TEAM_ID);
				battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			} finally {
				lock.unlock();
			}
		}
		eventBus.post(TeamEvent.disband(team, TeamReason.DISBAND_LEADER, playerId));
	}
	
	/**
	 * 离开组队
	 * 
	 * @param  playerId 		角色ID
	 * @param  reason 			离开队伍的原因 {@link TeamReason}
	 * @return {@link Integer} 	离开结果, 成功时返回组队ID, 如果因为离开组队导致组队解散, 返回成功(0),错误参见{@link TeamConstant}
	 */
	
	public int leave(long playerId, int reason) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		if(player == null) {
			return PLAYER_NOT_FOUND;
		}
			
		PlayerDungeon dungeon = dungeonManager.getPlayerDungeon(playerId);
		if (dungeon == null) {
			return PLAYER_NOT_FOUND;
		} 
//		else if (dungeon.isDungeonStatus()) {
//			return TEAM_IN_DUNGEON;
//		}
		
		// 邀请组队
		Team team = this.getPlayerTeam(playerId);
		if (team == null) {
			return TEAM_NOT_FOUND;
		} else if (!team.contains(playerId)) {
			return NOT_IN_TEAM;
		}
		
		// 队伍人数不足, 需要解散
		if(team.size() <= 1) {
			disband(team, reason, playerId);
			return SUCCESS;
		}
		
		// 队长退出了, 转队长
		player.setTeamId(TeamRule.NO_TEAM_ID);
		boolean isLeader = (team.getLeaderId() == playerId);
		if(isLeader) { // 根据队伍排位决定要转给那个队员
			Long nextLeader = null;
			for(Long memberId : team.getMembers()){
				if(memberId != playerId) {
					nextLeader = memberId;
					break;
				}
			}
			
			if(nextLeader != null) { // 交换队长
				team.setLeaderId(nextLeader);
			}
		}
		
		
		team.remove(playerId);
		this.isTeamOnline(playerId, team.getId());
		userDomain.getBattle().setFlushable(Flushable.FLUSHABLE_NORMAL);
		eventBus.post(TeamEvent.leave(team, reason, isLeader, playerId));
		
		// 增加组队在副本中的操作
		if (dungeon.isDungeonStatus()) {
			dungeonHelper.pushCoerceleave(Arrays.asList(playerId));
		}
		
		flushTeamMemberAttributes(team.getMembers());
		return team.getId();
	}

	/**
	 * 踢出队员
	 * 
	 * @param  playerId 		队长ID
	 * @param  targetId 		队员ID
	 * @return {@link Integer}	离开结果, 成功时返回组队ID, 如果因为离开组队导致组队解散, 返回成功(0), 错误参见{@link TeamConstant}
	 */
	
	public int kick(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		PlayerDungeon dungeon = dungeonManager.getPlayerDungeon(targetId);
		if (dungeon == null) {
			return TARGET_NOT_FOUND;
		} 
//		else if (dungeon.isDungeonStatus()) {
//			return TEAM_IN_DUNGEON;
//		}
		
		// 组队
		Player target = targetDomain.getPlayer();
		Team team = this.getPlayerTeam(playerId);
		if(team == null) {
			return TEAM_NOT_FOUND;
		}
		
		int teamId = team.getId();
		long leaderId = team.getLeaderId();
		if(playerId != leaderId) {
			return MUST_TEAM_LEADER;
		} else if(!team.contains(playerId)) {
			return NOT_IN_TEAM;
		} else if(targetId == leaderId) {// 不能踢出自己
			return KICK_YOUR_SELF;
		}
			
		// 设置当前组队
		ChainLock lock = LockUtils.getLock(team, target, targetDomain.getBattle());
		try {
			lock.lock();
			target.setTeamId(TeamRule.NO_TEAM_ID);
			team.remove(targetId);
			targetDomain.getBattle().setFlushable(Flushable.FLUSHABLE_NORMAL);
		} finally {
			lock.unlock();
		}
		
		eventBus.post(TeamEvent.kick(team, targetId));
		TeamPushHelper.pushMemberKicked(targetId, teamId, leaderId, TeamReason.LEAVE_KICK);
		
		// 增加组队在副本中的操作
		if (dungeon.isDungeonStatus()) {
			dungeonHelper.pushCoerceleave(Arrays.asList(targetId));
		}
		
		flushTeamMemberAttributes(team.getMembers());
		return teamId;
	}

	/**
	 * 转让队长
	 * 
	 * @param  playerId 		原队长ID
	 * @param  targetId 		新队长ID
	 * @return {@link Integer}	组队模块返回值
	 */
	
	public int swapLeader(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}

		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}

		if(!userManager.isOnline(targetId)) {
			return TARGET_OFF_LINE;
		}
		
		// 角色的组队
		Player target = targetDomain.getPlayer();
		Team team = this.getPlayerTeam(playerId);
		if(team == null || !team.contains(playerId)) {
			return NOT_IN_TEAM;
		}
		
		int teamId = team.getId();
		long oldLeader = team.getLeaderId();
		if(playerId != oldLeader) {
			return MUST_TEAM_LEADER;
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("队伍: [{}] 角色:[{}] 转换队长给:[{}]", new Object[]{ teamId, playerId, targetId });
		}
		
		ChainLock lock = LockUtils.getLock(target, team);
		try {
			lock.lock();
			if(playerId != team.getLeaderId()) {
				return MUST_TEAM_LEADER;
			} else if(!team.contains(targetId)) {
				return NOT_IN_TEAM;
			}
			team.setLeaderId(targetId);
			target.removeAttribute(PlayerStateKey.TEAM_BE_INVITES);	//原来被别人邀请的列表
		} catch(Exception e) {
			LOGGER.error("转让队长异常: {}", e);
			return FAILURE;
		} finally {
			lock.unlock();
		}
		eventBus.post(TeamEvent.swapLeader(team, playerId));
		return SUCCESS;
	}
	
	/**
	 * 列出组队区域VO
	 * 
	 * @param  playerId			角色ID
	 * @param  refresh			是否刷新
	 * @return {@link List}		组队ID列表
	 */
	
	public List<Integer> listAreaTeamId(long playerId, boolean refresh) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return Collections.emptyList();
		}
		int mapId = userDomain.getMotion().getMapId();
		int branching = userDomain.getPlayer().getBranching();
		return teamManager.listAreaTeamId(branching, mapId, refresh);
	}

	/**
	 * 列出区域玩家列表
	 * 
	 * @param  playerId			角色ID
	 * @param  refresh			是否刷新
	 * @return {@link List}		组队ID列表
	 */
	
	public List<Long> listAreaMemberId(long playerId, boolean refresh) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return Collections.emptyList();
		}
		
		int mapId = userDomain.getMotion().getMapId();
		int branching = userDomain.getPlayer().getBranching();
		List<Long> memberIdList = teamManager.listAreaMemberId(branching, mapId, refresh);
		if (memberIdList.size() > 0) {
			memberIdList.remove(playerId);
		}
		return memberIdList;
	}

	
	/**
	 * 申请入队
	 * 
	 * @param  playerId             玩家ID
	 * @param  targetId             目标ID
	 * @return {@link Integer}
	 */
	
	public int applyTeam(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();		// 申请者
		Player target = targetDomain.getPlayer();	// 接收者
		if (!userManager.isOnline(targetId)) {
			return TARGET_OFF_LINE;
		}
		
		if (player.getCamp() != target.getCamp()) {
			return CAMP_DIFFERENCE;
		}
		
		Set<Long> inviters = getMenberBeInvites(player);
		if (inviters.contains(playerId)) {
			return INVITE_SENDED;
		}
		
		PlayerDungeon dungeon = dungeonManager.getPlayerDungeon(targetId);
		if (dungeon == null) {
			return PLAYER_NOT_FOUND;
		} else if (dungeon.isDungeonStatus()) {
			return TEAM_IN_DUNGEON;
		}
		
		Team tTeam = this.getPlayerTeam(targetId);
		Team pTeam = this.getPlayerTeam(playerId);
		if (tTeam == null) {
			return NOT_IN_TEAM;
		}
		if (pTeam != null || tTeam.contains(playerId)) {
			return PLAYER_IN_TEAM;
		}
		if (tTeam.getLeaderId() != targetId) {
			return MUST_TEAM_LEADER;
		}
		if (tTeam.size() >= TeamRule.MAX_TEAM_MEMBERS) {
			return TEAM_MEMBER_FULLED;
		}
		
		int result = checkBranching(player, target);
		if(result < SUCCESS) {
			return result;
		}
		
		inviters.add(targetId);	
		PlayerBattle playerBattle = userDomain.getBattle();
		TeamPushHelper.pushPlayerInvite(targetId, playerBattle, player.getName(), 1);     // 申请 - 1
		return tTeam.getId();
	}
	
	
	
	
	public int reply(long leaderId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(leaderId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		UserDomain targetDomain = userManager.getUserDomain(targetId);
		if(targetDomain == null) {
			return TARGET_NOT_FOUND;
		}
		
		if (!userManager.isOnline(targetId)) {
			return TARGET_OFF_LINE;
		}
		
		PlayerDungeon dungeon = dungeonManager.getPlayerDungeon(leaderId);
		if (dungeon == null) {
			return PLAYER_NOT_FOUND;
		} else if (dungeon.isDungeonStatus()) {
			return TEAM_IN_DUNGEON;
		}
		
		Player player = userDomain.getPlayer();
		Player target = targetDomain.getPlayer();
		if (player.getCamp() != target.getCamp()) {
			return CAMP_DIFFERENCE;
		}

		// 是否同一分线
		int result = checkBranching(target, player);
		if(result < SUCCESS) {
			this.getMenberBeInvites(target).remove(leaderId);
			return result;
		}
		
		Team targetTeam = getPlayerTeam(targetId);
		if(targetTeam != null && targetTeam.contains(leaderId)) {
			return PLAYER_IN_TEAM;
		}
		
		Set<Long> beInvites = this.getMenberBeInvites(target);
		if(beInvites == null || !beInvites.contains(leaderId)){
			return INVITE_NOT_FOUND;
		}
		
		Team leaderTeam = getPlayerTeam(leaderId);
		if (leaderTeam == null) {
			return NOT_IN_TEAM;
		} else if (leaderTeam.getLeaderId() != leaderId) {
			return MUST_TEAM_LEADER;
		}
		
		if (leaderTeam.size() >= TeamRule.MAX_TEAM_MEMBERS) {
			return TEAM_MEMBER_FULLED;
		}
		leaderTeam.add(targetId);
		
		ChainLock lock = LockUtils.getLock(target);
		try {
			lock.lock();
			target.setTeamId(leaderTeam.getId());
			target.removeAttribute(PlayerStateKey.TEAM_BE_INVITES);
		} finally {
			lock.unlock();
		}
		
		eventBus.post(TeamEvent.join(leaderTeam, targetId));
		flushTeamMemberAttributes(leaderTeam.getMembers());           // 刷新属性
		achieveFacade.firstAchieved(targetId, FIRST_ACHIEVE, FIRST_CREATE_TEAM);
		return leaderTeam.getId();
	}
	
	
//	/**
//	 * 副本匹配
//	 * 
//	 * @param dungeonBaseId     副本基础Id           
//	 * @param playerId          玩家Id
//	 * @return {@link Team}
//	 */
//	
//	public ResultObject<Boolean> matchMember2CreateTeam(int dungeonBaseId, long playerId) {
//		Player player = userFacade.getPlayer(playerId);
//		if (player == null) {
//			return ResultObject.ERROR(PLAYER_NOT_FOUND);
//		}
//		
//		DungeonTeam dungeonTeam = this.getDungeonTeam(dungeonBaseId);
//		DungeonConfig config = dungeonFacade.getDungeonConfig(dungeonBaseId);
//		if (config == null || dungeonTeam == null) {
//			return ResultObject.ERROR(BASEDATA_NOT_FOUND);
//		}
//		
//		Team team = this.getPlayerTeam(playerId);
//		
//		long leaderId = 0L;
//		if (team != null) {
//			leaderId = team.getLeaderId();
//			if (playerId != leaderId) {
//				return ResultObject.ERROR(MUST_TEAM_LEADER);
//			}
//			if (TeamRule.MAX_TEAM_MEMBERS <= team.size()) {
//				return ResultObject.ERROR(TEAM_MEMBER_FULLED);
//			}
//		}
//		
//		ChainLock lock = LockUtils.getLock(dungeonTeam);
//		List<Long> matchList = null;
//		boolean canAdd = false;
//		try {
//			lock.lock();
//			if (team == null) {
//				canAdd = dungeonTeam.add2MatchQueue(playerId);
//			} else {
//				dungeonTeam.put(team);
//				canAdd = dungeonTeam.add2MatchTeams(team);
//			}
//			if (!canAdd) {
//				return ResultObject.ERROR(PLAYER_MATCHING);
//			}
//			
//			matchList = matchQueue(playerId, dungeonTeam);	
//			if (matchList.isEmpty()) {
//				return ResultObject.SUCCESS(false);
//			}
//		} catch (Exception e) {
//			LOGGER.error("{}", e);
//		} finally {
//			lock.unlock();
//		}
//		leaderId = matchList.get(Tools.getRandomInteger(matchList.size()));
//		Team matchTeam = this.createTeam(leaderId);
//		for (long memberId : matchList) {
//			if (memberId == leaderId) {
//				continue;
//			}
//			Player teamMember = userFacade.getPlayer(memberId);
//			if (teamMember == null) {
//				continue;
//			}
//			teamMember.setTeamId(matchTeam.getId());
//			matchTeam.add(memberId);
//		}
//		eventBus.post(TeamEvent.join(matchTeam, matchTeam.getMembers().toArray(new Long[0])));
//		TeamPushHelper.pushMatchSuccess(matchList, true, dungeonBaseId);
//		return ResultObject.SUCCESS(true);
//	}
//	
//	/**
//	 * 取消匹配
//	 * 
//	 * @param  playerId         玩家ID         
//	 * @param  dungeonBaseId    副本ID
//	 */
//	
//	public int cancleMatch(long playerId, int dungeonBaseId) {
//		Player player = userFacade.getPlayer(playerId);
//		if (player == null) {
//			return PLAYER_NOT_FOUND;
//		}
//		
//		DungeonTeam dungeonTeam = this.getDungeonTeam(dungeonBaseId);
//		DungeonConfig config = dungeonFacade.getDungeonConfig(dungeonBaseId);
//		if (config == null || dungeonTeam == null) {
//			return BASEDATA_NOT_FOUND;
//		}
//		
//		Team team = this.getPlayerTeam(playerId);
//		ChainLock lock = LockUtils.getLock(dungeonTeam);
//		
//		try {
//			lock.lock();
//			if (team != null) {
//				long leaderId = team.getLeaderId();
//				if (playerId != leaderId) {
//					return MUST_TEAM_LEADER;
//				}
//				dungeonTeam.remove(leaderId);
//				dungeonTeam.removeTeam(team);
//				Long[] playerIds = team.getMembers().toArray(new Long[0]);
//				TeamPushHelper.pushCancleMatchMessage(leaderId, false ,playerIds);
//			} else {
//				dungeonTeam.removeMember(playerId);
//			}
//		} catch (Exception e) {
//			LOGGER.error("{}", e);
//		} finally {
//			lock.unlock();
//		}
//		return SUCCESS;
//	}
	
	/**
	 * 准备入副本
	 * 
	 * @param  playerId         玩家ID
	 * @param  dungeonBaseId    副本ID
	 * @return {@link TeamConstant}
	 */
	
	public int ready2EnterDungeon(long playerId, int dungeonBaseId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		if (player == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Team team = getPlayerTeam(playerId);
		if (team == null) {
			return NOT_IN_TEAM;
		}
		
//		if (team.getMembers().size() < TeamRule.MIN_TEAM_MEMBERS) {
//			return LACK_TEAM_MEMBERS;
//		}
		
		DungeonTeam dungeonTeam = teamManager.getDungeonTeam(dungeonBaseId);
		if (dungeonTeam == null) {
			return BASEDATA_NOT_FOUND;
		}
		
		ChainLock lock = LockUtils.getLock(dungeonTeam);
		boolean ready = false;
		try {
			lock.lock();
			ready = dungeonTeam.add2ReadyMembers(team, playerId);
		} catch (Exception e) {
			LOGGER.error("{}", e);
		} finally {
			lock.unlock();
		}
		if (!ready) {
			return NOT_IN_TEAM;
		}
		
		List<Long> teamMemberIds = new ArrayList<Long>(team.getMembers());
		teamMemberIds.remove(playerId);
		TeamPushHelper.pushPlayerReadyMessage(playerId, player.getName(), true, teamMemberIds);
		
		int size = dungeonTeam.getTeamReadyMembers(team.getId()).size();
		if (size == team.size()) {
			TeamPushHelper.pushTeamMembers2Dungeon(dungeonBaseId, Arrays.asList(team.getLeaderId()) );
		}
		return SUCCESS;
	}
	
	/**
	 * 拒绝入副本
	 * @param  playerId          玩家ID
	 * @param  dungeonBaseId     副本ID
	 * @return
	 */
	
	public int reject2EnterDungeon(long playerId, int dungeonBaseId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		if (player == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Team team = getPlayerTeam(playerId);
		if (team == null) {
			return NOT_IN_TEAM;
		}
		
		DungeonTeam dungeonTeam = teamManager.getDungeonTeam(dungeonBaseId);
		if (dungeonTeam == null) {
			return BASEDATA_NOT_FOUND;
		}
		dungeonTeam.removeReadyCache(team.getId());
		
		ConcurrentHashSet<Long> members = team.getMembers();
		List<Long> playerIds = new ArrayList<Long>(members);
		TeamPushHelper.pushPlayerReadyMessage(playerId, player.getName(), false, playerIds);
		return SUCCESS;
	}

	
	/**
	 * 副本挑战
	 * 
	 * @param playerId          玩家Id
	 * @param dungeonBaseId     副本ID
	 * @return
	 */
	
	public int startDungeon(long playerId, int dungeonBaseId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		if (player == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Team team = getPlayerTeam(playerId);
		if (team == null) {
			return NOT_IN_TEAM;
		} else if (team.getLeaderId() != playerId) {
			return MUST_TEAM_LEADER;
		}
		
//		if (team.getMembers().size() < TeamRule.MIN_TEAM_MEMBERS) {
//			return LACK_TEAM_MEMBERS;
//		}
		
		DungeonConfig dungeonConfig = dungeonManager.getDungeonConfig(dungeonBaseId);
		if (dungeonConfig == null) {
			return BASEDATA_NOT_FOUND;
		} else if (!dungeonConfig.isOpen()) {
			return DUNGEON_NOT_OPEN;
		}
		
		/** 判断是否满足进入条件*/
		int memberSize = 1; //自己也算一个
		if(team != null){
			memberSize += (team.getMembers().size() - 1);//不计算自己
		}
		if(dungeonConfig.getMinNumLimit() > memberSize){
			return LEAST_PLAYER_LIMIT;
		}
		if(dungeonConfig.getMaxNumLimit() < memberSize){
			return OVER_PLAYER_LIMIT;
		}
		/** end判断组队进入条件*/
		
		
		PlayerDungeon playerDungeon = null;
		for (long memberId : team.getMembers()) {
			playerDungeon = dungeonManager.getPlayerDungeon(memberId);
			if (playerDungeon == null) {
				continue;
			} else if (playerDungeon.isDungeonStatus()) {
				return PLAYER_IN_DUNGEON;
			} 
			boolean canEnter = dungeonManager.canEnterDungeon(memberId, dungeonBaseId);
			if (!canEnter) {
				return TEAM_ENTER_DUNGEON_LIMIT;
			}
		}
		
		Set<Long> teamMembers = team.getMembers();
		List<Long> playerIds = new ArrayList<Long>(teamMembers);
		TeamPushHelper.pushMatchSuccess(playerIds, true, dungeonBaseId);
		return SUCCESS;
	}
	
	
	
	
	/**
	 * 组队进入副本地图(挑战)
	 * @param playerId          玩家Id
	 * @param dungeonBaseId     副本基础Id
	 * @return
	 */
	
	public int teamEnterDungeon(long playerId, int dungeonBaseId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		if (player == null) {
			return PLAYER_NOT_FOUND;
		}
		
		DungeonTeam dungeonTeam = teamManager.getDungeonTeam(dungeonBaseId);
		if (dungeonTeam == null) {
			return BASEDATA_NOT_FOUND;
		}
		
		Team team = this.getPlayerTeam(playerId);
		if (team == null) {
			return TEAM_NOT_FOUND;
		} else if (team.getLeaderId() != playerId) {
			return MUST_TEAM_LEADER;
		}
		
		Collection<Long> readyMembers = dungeonTeam.getTeamReadyMembers(team.getId());
		if (readyMembers == null) {
			return FAILURE;
		} else if (readyMembers.size() != team.size()) {
			return FAILURE;
		}
		
		dungeonTeam.removeReadyCache(team.getId());
		
		List<Long> teamMemberIds = new ArrayList<Long>(team.getMembers());
		teamMemberIds.remove(team.getLeaderId());
		TeamPushHelper.pushTeamMembers2Dungeon(dungeonBaseId, teamMemberIds);
		return SUCCESS;
	}

	
	/**
	 * 取消进入副本流程
	 * 
	 * @param playerId          玩家Id
	 * @param dungeonBaseId     副本基础Id
	 * @return {@link TeamConstant}
	 */
	
	public int cancle2Dungeon(long playerId, int dungeonBaseId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return PLAYER_NOT_FOUND;
		}
		
		Player player = userDomain.getPlayer();
		if (player == null) {
			return PLAYER_NOT_FOUND;
		}
		
		DungeonTeam dungeonTeam = teamManager.getDungeonTeam(dungeonBaseId);
		if (dungeonTeam == null) {
			return BASEDATA_NOT_FOUND;
		}
		
		Team team = this.getPlayerTeam(playerId);
		if (team == null) {
			return TEAM_NOT_FOUND;
		} else if (team.getLeaderId() != playerId) {
			return MUST_TEAM_LEADER;
		}
		
		dungeonTeam.removeReadyCache(team.getId());
		return SUCCESS;
	}

	
	
	private void flushTeamMemberAttributes(Set<Long> teamMembers) {
		for (long memberId : teamMembers) {
			UserDomain domain = userManager.getUserDomain(memberId);
			if (domain == null) {
				continue;
			}
			
			ChainLock lock = LockUtils.getLock(domain.getBattle());
			try {
				lock.lock();
				domain.getBattle().setFlushable(Flushable.FLUSHABLE_NORMAL);
			} finally {
				lock.unlock();
			}
		}
	}


	
//	
//	/**
//	 * 取得已匹配的玩家集合
//	 * 
//	 * @param playerId          玩家ID 
//	 * @param dungeonBaseId     副本ID
//	 * @return
//	 */
//	private List<Long> matchQueue(long playerId, DungeonTeam dungeonTeam) {
//		List<Long> match = new ArrayList<Long>();
//		
//		Set<Team> teams = dungeonTeam.getTeams();
//		Team team = getPlayerTeam(playerId);
//		
//		int matchSize = 0;
//		for (Team theTeam : teams) {
//			if (team != null) {
//				if (team == theTeam) {continue;}
//				matchSize = team.size() + theTeam.size();
//			}
//			if (matchSize == TeamRule.MAX_TEAM_MEMBERS) {
//				dungeonTeam.removeTeam(team);
//				dungeonTeam.removeTeam(theTeam);
//				match.addAll(team.getMembers());
//				match.addAll(theTeam.getMembers());
//				disband(team, TeamReason.DISBAND_LEADER, team.getLeaderId());
//				disband(theTeam, TeamReason.DISBAND_LEADER, theTeam.getLeaderId());
//				break;
//			}
//			matchSize = theTeam.size() + dungeonTeam.matchQueueSize();
//			if (matchSize >= TeamRule.MAX_TEAM_MEMBERS) {
//				int size = TeamRule.MAX_TEAM_MEMBERS - theTeam.size();
//				dungeonTeam.removeTeam(theTeam);
//				match.addAll(theTeam.getMembers());
//				match.addAll(dungeonTeam.getMatchMembers(size));
//				disband(theTeam, TeamReason.DISBAND_LEADER, theTeam.getLeaderId());
//				break;
//			}
//		}
//		matchSize = dungeonTeam.matchQueueSize();
//		if (matchSize == TeamRule.MAX_TEAM_MEMBERS) {
//			match.addAll(dungeonTeam.getMatchMembers(matchSize));
//		}
//		return match;
//	}
//	
//
//	/**
//	 * 队伍重置
//	 * 
//	 * @param team
//	 * @param dungeonTeam
//	 */
//	private void resetTeam(Team team, DungeonTeam dungeonTeam) {
//		Set<Long> members = team.getMembers();
//		disband(team, TeamReason.DISBAND_LEADER, team.getLeaderId());
//		for (long memberId : members) {
//			Collection<Long> oldTeamMembers = dungeonTeam.get(memberId);
//			if (oldTeamMembers == null) {
//				continue;
//			}
//			
//			dungeonTeam.remove(memberId);
//			Team newTeam = createTeam(memberId);
//			for (long oldMemberId : oldTeamMembers) {
//				Player oldMember = userFacade.getPlayer(oldMemberId);
//				if (oldMember == null) {
//					continue;
//				}
//				newTeam.add(oldMemberId);
//				oldMember.setTeamId(newTeam.getId());
//			}
//			eventBus.post(TeamEvent.join(newTeam, oldTeamMembers.toArray(new Long[0])));
//		}
//	}
	
	
}
