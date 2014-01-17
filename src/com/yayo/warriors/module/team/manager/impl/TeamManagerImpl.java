package com.yayo.warriors.module.team.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.TimeConstant;
import com.yayo.warriors.basedb.model.DungeonConfig;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.team.manager.TeamManager;
import com.yayo.warriors.module.team.model.DungeonTeam;
import com.yayo.warriors.module.team.model.Team;
import com.yayo.warriors.module.team.rule.TeamRule;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.type.ElementType;


@Service
public class TeamManagerImpl extends CachedServiceAdpter implements TeamManager {

	private final ConcurrentHashMap<Integer, Team> TEAM_MAPS = new ConcurrentHashMap<Integer, Team>(5);
	private final ConcurrentLinkedHashMap<Integer, DungeonTeam> DUNGEONTEAM_MAPS = new ConcurrentLinkedHashMap.Builder<Integer, DungeonTeam>().maximumWeightedCapacity(1000).build();
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private GameMapManager gameMapManager;
	@Autowired
	private DungeonManager dungeonManager;
	
	
	public Team getTeam(int teamId) {
		return TEAM_MAPS.get(teamId);
	}

	public Team getPlayerTeam(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return null;
		}
		
		Player player = userDomain.getPlayer();
		if(player == null || player.getTeamId() <= 0L) {
			return null;
		}
		
		int teamId = player.getTeamId();
		Team team = this.getTeam(teamId);
		if(team == null || team.isEmpty()) {
			player.setTeamId(TeamRule.NO_TEAM_ID);	
			TEAM_MAPS.remove(teamId);
			return null;
		}
		return team;
	}

	public void putTeam2Cache(Team team) {
		TEAM_MAPS.put(team.getId(), team);
	}

	public boolean removeTeam(int teamId) {
		TEAM_MAPS.remove(teamId);
		return true;
	}
	
	public DungeonTeam getDungeonTeam(int dungeonBaseId) {
		DungeonConfig config = dungeonManager.getDungeonConfig(dungeonBaseId);
		if (config == null) {
			return null;
		}
		DungeonTeam dungeonTeam = DUNGEONTEAM_MAPS.get(dungeonBaseId);
		if (dungeonTeam == null) {
			dungeonTeam = DungeonTeam.valueOf(dungeonBaseId);
			DUNGEONTEAM_MAPS.putIfAbsent(dungeonBaseId, dungeonTeam);
			dungeonTeam = DUNGEONTEAM_MAPS.get(dungeonBaseId);
		}
		return dungeonTeam;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Integer> listAreaTeamId(int branching, int mapId, boolean refresh) {
		final String SUBKEY = getAreaTeamSubKey(branching, mapId);
		List<Integer> teamIdList = (List<Integer>) cachedService.getFromCommonCache(HASHKEY, SUBKEY);
		if(teamIdList != null && !refresh) {
			return teamIdList;
		}
	
		ChainLock lock = getLock(mapId, branching);
		try {
			lock.lock();
			teamIdList = (List<Integer>) cachedService.getFromCommonCache(HASHKEY, SUBKEY);
			if(teamIdList != null && !refresh) {
				return teamIdList;
			}
			
			GameMap gameMap = gameMapManager.getGameMapById(mapId, branching); 
			List<Integer> teams = new ArrayList<Integer>();
			Collection<Long> players = null;
			if (gameMap != null) {
				players = gameMap.getAllSpireIdCollection(ElementType.PLAYER);
			}
			
			if(players != null && !players.isEmpty()) {
				for (Long playerId : players) {
					Team playerTeam = this.getPlayerTeam(playerId);
					if(playerTeam == null) {
						continue;
					}
					if(!teams.contains(playerTeam.getId())) {
						teams.add(playerTeam.getId());
					}
				}
			}
			cachedService.put2CommonHashCache(HASHKEY, SUBKEY, teams, TimeConstant.ONE_MINUTE_MILLISECOND);
			return teams;
		} catch (Exception e) {
			return (List<Integer>) cachedService.getFromCommonCache(HASHKEY, SUBKEY);
		} finally {
			lock.unlock();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Long> listAreaMemberId(int branching, int mapId, boolean refresh) {
		final String SUBKEY = getAreaMemberSubKey(branching, mapId);
		List<Long> memberIdList = (List<Long>) cachedService.getFromCommonCache(HASHKEY, SUBKEY);
		if(memberIdList != null && !refresh) {
			return memberIdList;
		}
	
		ChainLock lock = getLock(mapId, branching);
		try {
			lock.lock();
			memberIdList = (List<Long>) cachedService.getFromCommonCache(HASHKEY, SUBKEY);
			if(memberIdList != null && !refresh) {
				return memberIdList;
			}
			
			GameMap gameMap = gameMapManager.getGameMapById(mapId, branching);
			memberIdList = new ArrayList<Long>();
			if (gameMap != null) {
				memberIdList.addAll(gameMap.getAllSpireIdCollection(ElementType.PLAYER));
			}
			cachedService.put2CommonHashCache(HASHKEY, SUBKEY, memberIdList, TimeConstant.ONE_MINUTE_MILLISECOND);
			return memberIdList;
		} catch (Exception e) {
			return Collections.emptyList();
		} finally {
			lock.unlock();
		}
	}

	private static final String HASHKEY = "TEAM_";
	private static final String TEAMS = "_TEAMS_";
	private static final String MAP_ID = "MAPID_";
	private static final String MEMBERS = "_MEMBERS_";
	private static final String BRANCHING = "BRANCHING_";
	private static final ConcurrentHashMap<String, Object> LOCKERS = new ConcurrentHashMap<String, Object>(5);
	
	private ChainLock getLock(int mapId, int branching) {
		String key = getAreaTeamSubKey(branching, mapId);
		Object objectLock = LOCKERS.get(key);
		if(objectLock == null) {
			LOCKERS.putIfAbsent(key, new Object());
			objectLock = LOCKERS.get(key);
		}
		return LockUtils.getLock(objectLock);
	}
	
	private String getAreaTeamSubKey(int branching, int mapId) {
		StringBuilder builder = new StringBuilder();
		builder.append(HASHKEY).append(TEAMS).append(BRANCHING).append(branching).append(MAP_ID).append(mapId);
		return builder.toString();
	}

	private String getAreaMemberSubKey(int branching, int mapId) {
		StringBuilder builder = new StringBuilder();
		builder.append(HASHKEY).append(MEMBERS).append(BRANCHING).append(branching).append(MAP_ID).append(mapId);
		return builder.toString();
	}
	
}
