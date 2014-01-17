package com.yayo.warriors.module.rank.dao;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;

import com.yayo.common.db.dao.CommonDao;
import com.yayo.warriors.module.rank.entity.RankEntry;
import com.yayo.warriors.module.rank.type.RankType;
import com.yayo.warriors.module.user.type.Job;

public interface RankDao extends CommonDao{
	public <T> List<T> listRankSources(DetachedCriteria dc, int fromIdx, int fetchCount);
	public <T> List<T> listRankSources(String sql, Class<T> clazz, int fromIdx, int fetchCount);
	public List<RankEntry> listRankEntry(RankType rankType, Job job);
	public void clearRankEntries();
}
