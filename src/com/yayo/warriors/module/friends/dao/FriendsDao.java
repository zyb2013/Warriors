package com.yayo.warriors.module.friends.dao;

import java.util.List;

import com.yayo.common.db.dao.CommonDao;
import com.yayo.warriors.module.friends.entity.Friend;
import com.yayo.warriors.module.friends.type.FriendType;

/**
 * 好友Dao
 * @author liuyuhua
 */
public interface FriendsDao extends CommonDao{
	
	/**
	 * 查询好友信息
	 * @param playerId  玩家的ID
	 * @return {@link List<Long>}
	 */
	List<Long> getFriends(long playerId);
	
	/**
	 * 玩家被关注信息
	 * @param playerId   玩家的ID
	 * @return {@link List<Long>}
	 */
	List<Long> getFriendsFocus(long targetId, FriendType type);
	
	/**
	 * 查询玩家所有的被关注信息(包括好友, 黑名单, 联系人)
	 * @param playerId   玩家ID
	 * @return {@link List<Long>}
	 */
	List<Long> getAllFocus(long playerId);
	
	/**
	 * 创建好友
	 * @param friend    好友对象
	 */
	void createFriends(Friend friend);

}
