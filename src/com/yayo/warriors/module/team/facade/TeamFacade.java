package com.yayo.warriors.module.team.facade;

import java.util.List;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.server.listener.LoginListener;
import com.yayo.warriors.module.team.constant.TeamConstant;
import com.yayo.warriors.module.team.model.Team;
import com.yayo.warriors.module.team.type.TeamReason;

/**
 * 组队接口
 * 
 * @author Hyint
 */
public interface TeamFacade extends LoginListener {
	
	/**
	 * 获得组队信息
	 * 
	 * @param  teamId				队伍ID
	 * @return {@link Team}			组队信息
	 */
	Team getTeam(int teamId);
	
	/** 
	 * 创建组队 
	 * 
	 * @param  playerId				角色ID
	 * @return {@link Team}			组队对象
	 */
	Team createTeam(long playerId);
	
	/**
	 * 查询玩家的组队信息
	 * 
	 * @param  playerId				角色ID
	 * @return {@link Team}			组队对象
	 */
	Team getPlayerTeam(long playerId);
	
	/**
	 * 邀请玩家加入组队
	 * 
	 * @param  playerId				发起邀请的角色ID
	 * @param  targetId				被邀请人的角色ID
	 * @return {@link ResultObject}	组队返回值
	 */
	int invite(long playerId, long targetId);
	
	/**
	 * 接受组队邀请.
	 * 
	 * @param  playerId				被邀请人ID
	 * @param  leaderId				邀请人的ID
	 * @return {@link Integer}		组队返回值
	 */
	int accept(long playerId, long leaderId);
	
	/**
	 * 拒绝组队邀请.
	 * 
	 * @param  playerId				被邀请人ID
	 * @param  targetId				邀请人的ID
	 * @return {@link Integer}		组队返回值
	 */
	int reject(long playerId, long targetId);
	
	/**
	 * 解散组队
	 * 
	 * @param  playerId				角色ID
	 * @return {@link Integer}		组队返回值
	 */	
	int disband(long playerId);

	/**
	 * 离开组队
	 * 
	 * @param  playerId 			角色ID
	 * @param  case 				离开队伍的原因 {@link TeamReason}
	 * @return {@link Integer} 		离开结果, 成功时返回组队ID, 如果因为离开组队导致组队解散, 返回成功(0),错误参见{@link TeamConstant}
	 */
	int leave(long playerId, int reason);
	
	/**
	 * 踢出队员
	 * 
	 * @param  playerId 			队长ID
	 * @param  targetId 			队员ID
	 * @return {@link Integer}		离开结果, 成功时返回组队ID, 如果因为离开组队导致组队解散, 返回成功(0), 错误参见{@link TeamConstant}
	 */
	int kick(long playerId, long targetId);
	
	/**
	 * 转让队长
	 * 
	 * @param  playerId 			原队长ID
	 * @param  targetId 			新队长ID
	 * @return {@link Integer}		组队模块返回值
	 */
	int swapLeader(long playerId, long targetId);
	
	/**
	 * 列出组队区域的组队ID
	 * 
	 * @param  playerId				角色ID
	 * @param  refresh				是否刷新
	 * @return {@link List}			组队ID列表
	 */
	List<Integer> listAreaTeamId(long playerId, boolean refresh);
	
	/**
	 * 列出区域玩家ID列表
	 * 
	 * @param  playerId				角色ID
	 * @param  refresh				是否刷新
	 * @return {@link List}			组队ID列表
	 */
	List<Long> listAreaMemberId(long playerId, boolean refresh);
	
	/**
	 * 验证是否有组队成员在线
	 * (true-有成员在线, false-队伍无人在线)
	 * @param playerId              玩家ID
	 * @param teamId            	队伍ID
	 * @return {@link Boolean} 
	 */
	boolean isTeamOnline(long playerId, int teamId);
	
	/**
	 * 申请入队
	 * 
	 * @param  playerId             玩家ID
	 * @param  targetId             目标ID
	 * @return {@link Integer}
	 */
	int applyTeam(long playerId, long targetId);
	
	/**
	 * 回复申请
	 * 
	 * @param  leaderId
	 * @param  targetId
	 * @return {@link Integer}
	 */
	int reply(long leaderId, long targetId);
	
	// --------------  副本组队机制  ----------------
	
	
//	/**
//	 * 副本匹配
//	 * 
//	 * @param dungeonBaseId     副本ID
//	 * @param playerId          玩家ID
//	 * @return {@link Boolean}
//	 */
//	ResultObject<Boolean> matchMember2CreateTeam(int dungeonBaseId, long playerId);
	
//	/**
//	 * 取消匹配
//	 * 
//	 * @param playerId          玩家ID
//	 * @param dungeonBaseId     副本ID
//	 * @return {@link TeamConstant}
//	 */
//	int cancleMatch(long playerId, int dungeonBaseId);
	
	/**
	 * 准备入副本
	 * 
	 * @param playerId          玩家ID
	 * @param dungeonBaseId     副本ID
	 * @return {@link TeamConstant}
	 */
	int ready2EnterDungeon(long playerId, int dungeonBaseId);
	
	/**
	 * 拒绝入副本
	 * 
	 * @param playerId          玩家ID
	 * @param dungeonBaseId     副本ID
	 * @return {@link TeamConstant}
	 */
	int reject2EnterDungeon(long playerId, int dungeonBaseId);
	
	/**
	 * 开始挑战副本(推送队员需做准备)
	 * 
	 * @param playerId          玩家Id
	 * @param dungeonBaseId     副本基础Id
	 * @return {@link TeamConstant}
	 */
	int startDungeon(long playerId, int dungeonBaseId);
	
	/**
	 * 组队进入副本地图(挑战)
	 * 
	 * @param playerId          玩家Id
	 * @param dungeonBaseId     副本基础Id
	 * @return {@link TeamConstant}
	 */
	int teamEnterDungeon(long playerId, int dungeonBaseId);
	
	/**
	 * 取消进入副本流程
	 * 
	 * @param playerId          玩家Id
	 * @param dungeonBaseId     副本基础Id
	 * @return {@link TeamConstant}
	 */
	int cancle2Dungeon(long playerId, int dungeonBaseId);
	
}
