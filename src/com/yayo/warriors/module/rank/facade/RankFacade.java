package com.yayo.warriors.module.rank.facade;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.rank.type.RankType;
import com.yayo.warriors.module.rank.vo.RankVO;
import com.yayo.warriors.module.user.type.Job;


public interface RankFacade {
	public ResultObject<RankVO> getRankVO(long playerId, RankType rankType, Job job, int pageSize, int pageNow);
	public ResultObject<RankVO> getRankVOByPlayerName(long playerId, String targetPlayerName, RankType rankType, Job job, int pageSize);
}
