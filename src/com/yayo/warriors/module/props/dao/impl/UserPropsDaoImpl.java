package com.yayo.warriors.module.props.dao.impl;

import static com.yayo.warriors.module.pack.type.BackpackType.*;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.props.dao.UserPropsDao;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;

/**
 * 用户道具DAO实现类
 * 
 * @author Hyint
 */
@Repository
public class UserPropsDaoImpl extends CommonDaoImpl implements UserPropsDao {
	
	/**
	 * 查询用户道具ID列表 
	 * 
	 * @param  playerId			角色ID
	 * @param  backpack			背包号
	 * @return {@link List}		用户道具ID列表
	 */
	
	@SuppressWarnings("unchecked")
	public List<Long> getUserPropsIdList(long playerId, int backpack) {
		Criteria criteria = createCriteria(UserProps.class);
		criteria.add(Restrictions.eq("playerId", playerId));
		criteria.add(Restrictions.eq("backpack", backpack));
		if(ArrayUtils.contains(VALID_COUNT_BACKPACKS, backpack))  {
			criteria.add(Restrictions.gt("count", 0));
		}
		criteria.setProjection(Projections.id());
		return criteria.list();
	}

	
	public void createUserProps(UserProps userProps) {
		this.save(userProps);
	}

	
	public void spliteUserProps(UserProps createProps, UserProps updateProps) {
		this.save(createProps);
		this.update(updateProps);
	}

	
	public void createUserEquipAndProps(Collection<UserEquip> userEquips, Collection<UserProps> propsList) {
		if(userEquips != null && !userEquips.isEmpty()) {
			for (UserEquip userEquip : userEquips) {
				this.save(userEquip);
			}
		}
		if(propsList != null && !propsList.isEmpty()) {
			for (UserProps userProps : propsList) {
				this.save(userProps);
			}
		}
	}

	
	public void updateUserProps(Collection<UserProps> newUserPropList, Collection<UserProps> updateUserProps) {
		if(newUserPropList != null && !newUserPropList.isEmpty()) {
			this.createUserProps(newUserPropList);
		}
		if(updateUserProps != null && !updateUserProps.isEmpty()) {
			this.update(updateUserProps);
		}
	}

	/**
	 * 创建用户装备
	 * 
	 * @param userEquips		用户装备数组
	 */
	
	public void createUserEquip(UserEquip... userEquips) {
		for (UserEquip userEquip : userEquips) {
			this.save(userEquip);
		}
	}

	
	public void createUserEquip(Collection<UserEquip> userEquips) {
		for (UserEquip userEquip : userEquips) {
			this.save(userEquip);
		}
	}

	
	public void createUserProps(Collection<UserProps> userProps) {
		//TODO 
		if(userProps != null && !userProps.isEmpty()) {
			for (UserProps props : userProps) {
				this.createUserProps(props);
			}
		}
	}
	
	
	
	
}
