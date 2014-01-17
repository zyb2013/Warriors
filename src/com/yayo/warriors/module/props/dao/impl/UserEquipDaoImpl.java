package com.yayo.warriors.module.props.dao.impl;

import static com.yayo.warriors.module.pack.type.BackpackType.VALID_COUNT_BACKPACKS;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.props.dao.UserEquipDao;
import com.yayo.warriors.module.props.entity.UserEquip;

/**
 * 用户装备DAO 实现
 * 
 * @author Hyint
 */
@Repository
public class UserEquipDaoImpl extends CommonDaoImpl implements UserEquipDao {

	/**
	 * 查询用户装备ID列表 
	 * 
	 * @param  playerId			角色ID
	 * @param  backpack			背包号
	 * @return {@link List}		用户装备ID列表
	 */
	
	@SuppressWarnings("unchecked")
	public List<Long> getUserEquipIdList(long playerId, int backpack) {
		Criteria criteria = createCriteria(UserEquip.class);
		criteria.add(Restrictions.eq("playerId", playerId));
		criteria.add(Restrictions.eq("backpack", backpack));
		if(ArrayUtils.contains(VALID_COUNT_BACKPACKS, backpack))  {
			criteria.add(Restrictions.gt("count", 0));
		}
		criteria.setProjection(Projections.id());
		return criteria.list();
	}
}
