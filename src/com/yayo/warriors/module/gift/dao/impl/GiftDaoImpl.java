package com.yayo.warriors.module.gift.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.gift.dao.GiftDao;
import com.yayo.warriors.module.gift.entity.Gift;

/**
 * 礼包DAO
 * 
 * @author huachaoping
 */
@Repository
public class GiftDaoImpl extends CommonDaoImpl implements GiftDao{

	
	/**
	 * 根据礼包类型获取礼包主键列表
	 * 
	 * @param giftType            礼包类型
	 * @return {@link List}       主键列表
	 */
	@SuppressWarnings("unchecked")
	
	public List<Integer> getGiftByType(int giftType) {
		Criteria criteria = createCriteria(Gift.class);
		criteria.add(Restrictions.eq("giftType", giftType));
		criteria.setProjection(Projections.id());
		return criteria.list();
	}
	
	
}
