package com.yayo.warriors.module.team.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 副本组队模型
 * 
 * @author huachaoping
 */
public class DungeonTeam implements Serializable {

	private static final long serialVersionUID = 4330094287410576682L;
	
	/** 副本Id */
	private int dungeonBaseId;
	
	/** 匹配的队伍 */
//	private final ConcurrentHashSet<Team> TEAM_MATCHS = new ConcurrentHashSet<Team>();
	
	/** 已点匹配 玩家的队列 */
//	private final ConcurrentLinkedQueue<Long> MATCHS = new ConcurrentLinkedQueue<Long>();
	
	/** 原队伍人员[队长Id - 队员列表] */
//	private final ConcurrentHashMap<Long, Collection<Long>> TEAM_MEMBERS = new ConcurrentHashMap<Long, Collection<Long>>();

	/** {队伍Id - [已准备的成员列表]} */
	private final ConcurrentHashMap<Integer, Collection<Long>> TEAM_READY_MEMBERS = new ConcurrentHashMap<Integer, Collection<Long>>(5);

	private DungeonTeam() {
	}
	
	/** 构造函数 */
	public static DungeonTeam valueOf(int dungeonBaseId) {
		DungeonTeam dungeonTeam   = new DungeonTeam();
		dungeonTeam.dungeonBaseId = dungeonBaseId;
		return dungeonTeam;
	}

	public int getDungeonBaseId() {
		return dungeonBaseId;
	}

	public void setDungeonBaseId(int dungeonBaseId) {
		this.dungeonBaseId = dungeonBaseId;
	}
	
	/** 查询队伍 已准备好的成员信息 */
	public Collection<Long> getTeamReadyMembers(int teamId) {
		return TEAM_READY_MEMBERS.get(teamId);
	}
	
	/** 移除准备玩家列表 */
	public void removeReadyCache(int teamId) {
		TEAM_READY_MEMBERS.remove(teamId);
	}
	
	/** 加入已准备列表 */
	public boolean add2ReadyMembers(Team team, long playerId) {
		if (!team.getMembers().contains(playerId)) {
			return false;
		}
		int teamId = team.getId();
		Collection<Long> members = TEAM_READY_MEMBERS.get(teamId);
		if (members == null) {
			members = new HashSet<Long>();
			TEAM_READY_MEMBERS.putIfAbsent(teamId, members);
			members = TEAM_READY_MEMBERS.get(teamId);
		}
		members.add(playerId);
		return true;
	}
	
	/** 满足组队条件, 获取组队成员 */
//	public synchronized List<Long> getMatchMembers(int size) {
//		List<Long> teamMembers = new ArrayList<Long>();
//		for (int i = 1; i <= size; i++) {
//			long playerId = MATCHS.poll();
//			teamMembers.add(playerId);
//		}
//		return teamMembers;
//	}
	
	/** 匹配队列大小 */
//	public int matchQueueSize() {
//		return MATCHS.size();
//	}
	
//	public Set<Team> getTeams() {
//		return this.TEAM_MATCHS;
//	}
	
	/** 移除匹配队伍信息 */
//	public synchronized void removeTeam(Team team) {
//		TEAM_MATCHS.remove(team);
//	}
	
	/** 移除原队伍信息 */
//	public synchronized void remove(long leaderId) {
//		TEAM_MEMBERS.remove(leaderId);
//	}
	
//	public boolean add2MatchQueue(long playerId) {
//		if (!MATCHS.contains(playerId)) {
//			MATCHS.add(playerId);
//			return true;
//		}
//		return false;
//	}
	
//	public void removeMember(long playerId) {
//		MATCHS.remove(playerId);
//	}
	
//	public boolean add2MatchTeams(Team team) {
//		if (!TEAM_MATCHS.contains(team)) {
//			TEAM_MATCHS.add(team);
//			return true;
//		}
//		return false;
//	}
	
//	public Collection<Long> get(long leaderId) {
//		return TEAM_MEMBERS.get(leaderId);
//	}
	
//	public void put(Team team) {
//		TEAM_MEMBERS.put(team.getLeaderId(), team.getMembers());
//	}
	
	/** 组队是否要重置 */
//	public boolean isResetTeam(Team team) {
//		for (long memberId : team.getMembers()) {
//		if (TEAM_MEMBERS.keySet().contains(memberId)) {
//			return true;
//		}
//		}
//		return false;
//	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dungeonBaseId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DungeonTeam))
			return false;
		DungeonTeam other = (DungeonTeam) obj;
		if (dungeonBaseId != other.dungeonBaseId)
			return false;
		return true;
	}
	
}
