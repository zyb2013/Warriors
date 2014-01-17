package com.yayo.warriors.module.meridian.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.meridian.dao.MeridianDao;
import com.yayo.warriors.module.meridian.entity.Meridian;

/**
 * 经脉DAO
 * 
 * @author huachaoping
 */
@Repository
public class MeridianDaoImpl extends CommonDaoImpl implements MeridianDao {

	
	/**
	 * 获得所有经脉玩家ID
	 * 
	 * @return {@link List}
	 */
	@SuppressWarnings("unchecked")
	
	public List<Long> getMeridianPlayerIds() {
		Criteria criteria = createCriteria(Meridian.class);
		criteria.setProjection(Projections.id());
		return criteria.list();
	}

	
}
