package com.yayo.warriors.module.rank.dao.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.rank.dao.RankDao;
import com.yayo.warriors.module.rank.entity.RankEntry;
import com.yayo.warriors.module.rank.type.RankType;
import com.yayo.warriors.module.user.type.Job;

@Repository
public class RankDaoImpl extends CommonDaoImpl implements RankDao {

	@SuppressWarnings("unchecked")
	
	public <T> List<T> listRankSources(DetachedCriteria  dc, int fromIdx, int fetchCount) {
		if(dc != null){
			Criteria criteria = dc.getExecutableCriteria( getSession() );
			if (fromIdx > 0) {
				criteria.setFirstResult(fromIdx);
			}
			if (fetchCount > 0) {
				criteria.setMaxResults(fetchCount);
			}
			return criteria.list();
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	
	public <T> List<T> listRankSources(String sql, Class<T> clazz, int fromIdx, int fetchCount) {
		if(StringUtils.isNotBlank(sql)){
			Query query = getSession().createSQLQuery(sql).addEntity(clazz);
			if (fromIdx > 0) {
				query.setFirstResult(fromIdx);
			}
			if (fetchCount > 0) {
				query.setMaxResults(fetchCount);
			}
			return query.list();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	
	public List<RankEntry> listRankEntry(RankType rankType, Job job) {
		Criteria criteria = getSession().createCriteria(RankEntry.class);
		criteria.add(Restrictions.eq("rankType", rankType));
		if(job == null){
			job = Job.COMMON;
		}
		criteria.add(Restrictions.eq("job", job));
		return criteria.list();
	}

	
	public void clearRankEntries() {
		//TODO 
	}
	
}