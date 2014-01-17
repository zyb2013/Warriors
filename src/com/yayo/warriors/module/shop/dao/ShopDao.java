package com.yayo.warriors.module.shop.dao;

import java.util.Collection;

import com.yayo.common.db.dao.CommonDao;
import com.yayo.warriors.module.shop.entity.LaveMallProp;

public interface ShopDao extends CommonDao {

	Long getMallPropId(int mallId);
	
	Collection<Long> getMallPropIds();
	
	void createLaveMallProps(LaveMallProp prop);
	
}
