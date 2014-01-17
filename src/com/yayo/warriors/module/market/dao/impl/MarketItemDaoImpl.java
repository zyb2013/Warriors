package com.yayo.warriors.module.market.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.market.dao.MarketItemDao;
import com.yayo.warriors.module.market.entity.MarketItem;
import com.yayo.warriors.module.market.type.ItemType;
import com.yayo.warriors.module.market.type.MarketState;

/**
 * 商店道具DAO
 * 
 * @author Hyint
 */
@Repository
public class MarketItemDaoImpl extends CommonDaoImpl implements MarketItemDao {

	
	@SuppressWarnings("unchecked")
	public List<Long> listMarketItemId(long playerId) {
		Criteria criteria = createCriteria(MarketItem.class);
		criteria.add(Restrictions.eq("playerId", playerId));
		criteria.add(Restrictions.eq("state", MarketState.NORMAL));
		criteria.setProjection(Projections.id());
		return criteria.list();
	}

	
	
	
	@SuppressWarnings("unchecked")
	public List<Long> listMarketOwner(ItemType itemType) {
		Criteria criteria = createCriteria(MarketItem.class);
		criteria.add(Restrictions.eq("type", itemType));
		criteria.add(Restrictions.eq("state", MarketState.NORMAL));
		criteria.setProjection(Projections.property("playerId"));
		return criteria.list();
	}

	
}
