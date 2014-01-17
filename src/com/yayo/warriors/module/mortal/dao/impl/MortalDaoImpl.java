package com.yayo.warriors.module.mortal.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.mortal.dao.MortalDao;
import com.yayo.warriors.module.mortal.entity.UserMortalBody;

/**
 * 肉身DAO
 * 
 * @author huachaoping
 */
@Repository
public class MortalDaoImpl extends CommonDaoImpl implements MortalDao {

	/**
	 * 查询所有肉身玩家
	 * 
	 * @return {@link List}
	 */
	@SuppressWarnings("unchecked")
	
	public List<Long> getMortalPlayers() {
		Criteria criteria = createCriteria(UserMortalBody.class);
		criteria.setProjection(Projections.id());
		return criteria.list();
	}

}
