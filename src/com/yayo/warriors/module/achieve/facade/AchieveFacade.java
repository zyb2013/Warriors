package com.yayo.warriors.module.achieve.facade;

import java.util.List;

import com.yayo.warriors.module.achieve.model.AchieveType;
import com.yayo.warriors.module.achieve.vo.AchieveVO;

public interface AchieveFacade {
	
	void commonAchieved(long playerId, AchieveType achieveType, int achieveValue);
	void killMonsterAchieved(long playerId, int killMonsterCount);
	void firstAchieved(long playerId, AchieveType achieveType, int firstType);
	List<Integer> listAchievesByType(long playerId, int achieveType);
	AchieveVO getAllAchieves(long playerId);
	int receiveAchieveReward(long playerId, int achieveId);
	void onlineAchieved(long playerId);
	int getAchieveValue(long playerId, int achieveType);
	List<Integer> receiveRewards(long playerId);
	void checkLoginAchieved(long playerId);
	List<Integer> getNonReceivedIds(long playerId);
	
}
