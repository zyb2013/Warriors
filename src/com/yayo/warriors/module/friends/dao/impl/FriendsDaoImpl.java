package com.yayo.warriors.module.friends.dao.impl;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.impl.CommonDaoImpl;
import com.yayo.warriors.module.friends.dao.FriendsDao;
import com.yayo.warriors.module.friends.entity.Friend;
import com.yayo.warriors.module.friends.type.FriendState;
import com.yayo.warriors.module.friends.type.FriendType;

/**
 * 好友DAO实现类
 * @author liuyuhua
 */
@Repository
public class FriendsDaoImpl extends CommonDaoImpl implements FriendsDao{

	
	@SuppressWarnings("unchecked")
	public List<Long> getFriends(long playerId) {
		Criteria criteria = createCriteria(Friend.class);
		criteria.add(Restrictions.eq("playerId", playerId));
		criteria.add(Restrictions.eq("state", FriendState.active));
		criteria.setProjection(Projections.id());
		return criteria.list();
	}

	
	@SuppressWarnings("unchecked")
	public List<Long> getFriendsFocus(long targetId, FriendType type) {
		Criteria criteria = createCriteria(Friend.class);
		criteria.add(Restrictions.eq("targetId", targetId)); 
		criteria.add(Restrictions.eq("state", FriendState.active));
		criteria.add(Restrictions.eq("type", type));
		criteria.setProjection(Projections.property("playerId"));
		return criteria.list();
	}

	
	
	@SuppressWarnings("unchecked")
	public List<Long> getAllFocus(long playerId) {
		Criteria criteria = createCriteria(Friend.class);
		criteria.add(Restrictions.eq("targetId", playerId));
		criteria.add(Restrictions.eq("state", FriendState.active));
		criteria.setProjection(Projections.property("playerId"));
		return criteria.list();
	}
	
	
	
	public void createFriends(Friend friend) {
		if(friend != null){
			this.save(friend);
		}
	}
}
