package com.yayo.warriors.module.alliance.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.alliance.dao.AllianceDao;
import com.yayo.warriors.module.alliance.entity.Alliance;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.types.AllianceState;

/**
 * 帮派Dao实现类
 * @author liuyuhua
 */
@Repository
public class AllianceDaoImpl extends CommonDaoImpl implements AllianceDao{

	
	@SuppressWarnings("unchecked")
	public List<Long> getAllianceIds() {
		Criteria criteria = createCriteria(Alliance.class);
		criteria.add(Restrictions.eq("state", AllianceState.ACTIVE));
		criteria.setProjection(Projections.id());
		return criteria.list();
	}

	
	@SuppressWarnings("unchecked")
	public List<Long> getAllianceMember(long allianceId) {
		Criteria criteria = createCriteria(PlayerAlliance.class);
		criteria.add(Restrictions.eq("allianceId", allianceId));
		criteria.setProjection(Projections.id());
		return criteria.list();
	}

	
	@SuppressWarnings({ "unchecked"})
	public Map<String,Long> getAllianceNames() {
//		Criteria criteria = createCriteria(Alliance.class);
//		criteria.add(Restrictions.eq("state", AllianceState.ACTIVE));
//		criteria.setProjection(Projections.property("name"));
//		criteria.setProjection(Projections.property("id"));
		Query query = getSession().createQuery("select name, id from Alliance where state = " + AllianceState.ACTIVE);
		List<Object> list = query.list();
		Map<String, Long> resultMap = new HashMap<String, Long>();
		if(list != null && list.size() > 0){
			for(Object obj: list){
				Object[] objs = (Object[])obj;
				if(objs.length >= 2){
					resultMap.put(objs[0].toString(), Long.valueOf(objs[1].toString()));
				}
			}
		}
		return resultMap;
	}

}
