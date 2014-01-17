package com.yayo.warriors.module.shop.dao.impl;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.shop.dao.ShopDao;
import com.yayo.warriors.module.shop.entity.LaveMallProp;

@Repository
public class ShopDaoImpl extends CommonDaoImpl implements ShopDao {

	public Long getMallPropId(int mallId) {
		Criteria criteria = createCriteria(LaveMallProp.class);
		criteria.add(Restrictions.eq("mallId", mallId));
		criteria.setProjection(Projections.id());
		return (Long) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public Collection<Long> getMallPropIds() {
		Criteria criteria = createCriteria(LaveMallProp.class);
		criteria.setProjection(Projections.id());
		return criteria.list();
	}
	
	public void createLaveMallProps(LaveMallProp prop) {
		if(prop != null){
			this.save(prop);
		}
	}

}
