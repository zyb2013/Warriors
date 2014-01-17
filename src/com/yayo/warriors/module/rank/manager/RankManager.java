package com.yayo.warriors.module.rank.manager;

import java.util.Collection;
import java.util.List;

import com.yayo.warriors.module.rank.type.RankType;
import com.yayo.warriors.module.rank.vo.RankInfoVO;
import com.yayo.warriors.module.user.type.Job;

public interface RankManager {
	public void refreshAllRank();
	public List<RankInfoVO> getRankVO(RankType rankType, Job job);
	public void checkRankOpen(int playerLevel);
	int[] getRankTitleByPlayerId(long playerId);
	void freshRankTitle(Collection<Long> playerIds);
}
