package com.yayo.warriors.module.friends.manager;

import java.util.Collection;
import java.util.Set;

import com.yayo.warriors.module.friends.entity.Friend;
import com.yayo.warriors.module.friends.entity.FriendsTreasure;
import com.yayo.warriors.module.friends.type.FriendType;
import com.yayo.warriors.module.user.model.Fightable;

/**
 * 好友Manager
 * @author liuyuhua
 */
public interface FriendManager {

	/**
	 * 获取关注该玩家的角色ID列表
	 * @param playerId   玩家的ID
	 * @param type       好友类型
	 * @return {@link Set<Long>} 角色列表集合
	 */
	Set<Long> getFriendsFocus(long playerId);
	
	/**
	 * 查询玩家所有的被关注信息(包括好友, 黑名单, 联系人)
	 * @param playerId   玩家ID
	 * @return {@link Set<Long>}
	 */
	Set<Long> getAllFocus(long targetId);
	
	/**
	 * 获取好友祝福瓶对象
	 * @param playerId  好友的ID
	 */
	FriendsTreasure getFriendsTreasure(long playerId);
	
	/**
	 * 获取相应类型的好友数量
	 * @param playerId    玩家的ID
	 * @param type        好友类型
	 * @return {@link Integer} 好友数量
	 */
	int size4Type(long playerId,FriendType type);
	
	/**
	 * 更具类型获取所有好友
	 * @param playerId     玩家的ID
	 * @param type         好友的类型
	 * @return {@link Collection<Friend>}好友集合
	 */
	Collection<Friend> getFirends(long playerId,FriendType type);
	
	/**
	 * 创建好友
	 * @param friend
	 */
	void createFriend(long playerId,Friend friend);
	
	/**
	 * 删除好友
	 * @param  playerId   玩家的ID
	 * @param  targetId   好友ID
	 * @param  type       好友类型
	 * @return true 成功 false 失败
	 */
	boolean removeFriend(long playerId,long targetId,FriendType type);
	
	/**
	 * 与好友组队击杀怪物,增加好友击杀值用于增加好友度
	 * @param playerId   玩家的ID
	 * @param targetId   目标玩家的ID
	 */
	Friend plusKill4Monster(long playerId,long targetId);
	
	/**
	 * 增加好友度
	 * @param playerId   玩家的ID
	 * @param targetId   目标玩家的ID
	 * @param value      需要增加的值
	 */
	Friend raiseFriendly(long playerId,long targetId,int value);
	
	/**
	 * 获取好友值
	 * @param playerId   玩家的ID
	 * @param targetId   目标玩家的ID
	 * @return 好友度(值)
	 */
	int getFriendlyValue(long playerId,long targetId);
	
	/**
	 * 被目标玩家杀死,增加对目标玩家的仇恨值
	 * 添加仇人(内部调用,不对客户端公开接口)
	 * @param playerId   玩家的ID
	 * @param targetId   目标玩家的ID
	 * @return true 操作成功 false操作失败
	 */
	Friend plusHatred(long playerId,long targetId);
	
	/**
	 * 杀死目标玩家,减少对目标玩家的仇恨值
	 * 减少对仇人的仇恨值(内部调用,不对客户端公开接口)
	 * @param playerId   玩家的ID
	 * @param targetId   目标玩家的ID
	 * @return true 操作成功 false操作失败
	 */
	Friend allayHatred(long playerId,long targetId);
	
	/**
	 * 是否 好友
	 * @param playerId   玩家的ID
	 * @param targetId   好友的ID
	 * @param type       好友类型
	 * @return true 是该类型的好友 false 反之
	 */
	 boolean isFriend(long playerId,long targetId,FriendType type);
	 
	 /**
	  * 移除好友关注缓存
	  * @param playerId  玩家ID
	  */
	 void removeFriendFocusCache(long playerId);
	 
	 
	 /**
	  * 获得黑名单跟好友的Ids
	  * @param playerId
	  * @return {@link Collection}
	  */
	 Collection<Long> getfriendIds(long playerId);
	 
	 
	 /**
	  * 
	  * @param friendlyValue
	  * @return
	  */
	 Fightable getFriendAddedValue(int friendlyValue);
	 
	 
	 /**
	  * 查询好友
	  * @param playerId
	  * @param targetId
	  * @return {@link Friend}
	  */
	 Friend getPlayerFriend(long playerId, long targetId);
	 
	 
	 /**
	  * 重置杀怪数量
	  * @param playerId
	  * @param targetId
	  */
	 void resetKillMonsterCount(long playerId, long targetId);

}
