package com.yayo.warriors.module.team.manager;

import java.util.List;

import com.yayo.warriors.module.team.model.DungeonTeam;
import com.yayo.warriors.module.team.model.Team;


public interface TeamManager {

	
	Team getTeam(int teamId);
	
	Team getPlayerTeam(long playerId);
	
	void putTeam2Cache(Team team);
	
	boolean removeTeam(int teamId);
	
	DungeonTeam getDungeonTeam(int dungeonBaseId);
	
	List<Integer> listAreaTeamId(int branching, int mapId, boolean refresh);
	
	List<Long> listAreaMemberId(int branching, int mapId, boolean refresh);

}