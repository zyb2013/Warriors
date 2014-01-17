package com.yayo.warriors.module.friends.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.yayo.warriors.module.friends.entity.Friend;
import com.yayo.warriors.module.friends.type.FriendType;

/**
 * 用户好友列表
 * 
 * @author liuyuhua
 */
public class UserFriend implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private long playerId;
	
	/** 好友 {目标玩家ID,Friend}*/
	private final Map<Long,Friend> friendly = new HashMap<Long, Friend>(0);

	/** 黑名单 {目标玩家ID,Friend}*/
	private final Map<Long,Friend> black = new HashMap<Long, Friend>(0);
	
	/** 最近联系人{目标玩家ID,Friend}*/
	private final Map<Long,Friend> nearest = new HashMap<Long, Friend>(0);
	
	/** 玩家击杀怪物值 {目前击杀300只怪物以后,就增加十点友好度}*/
	private final Map<Long,Integer> friendlyLimt = new HashMap<Long, Integer>(0);

	/** 到达杀死怪物的上限后,增加一点友谊值*/
	private final int KILL_MONSTER_LIMIT = 300;
	
	/**
	 * 构造函数
	 * @param playerId    玩家的ID
	 * @return {@link UserFriend}
	 */
	public static UserFriend valueOf(Long playerId){
		UserFriend friend = new UserFriend();
		friend.playerId = playerId;
		return friend;
	}
	
	/**
	 * 获取该类型的所有好友
	 * @param type    好友类型
	 * @return {@link Collection<Friend>} 好友集合
	 */
	public Collection<Friend> getFriends(FriendType type){
		if(type == FriendType.BLACK){
			return black.values();
		}else if(type == FriendType.FRIENDLY){
			return friendly.values();
		}else if(type == FriendType.NEAREST){
			return black.values();
		}
		return null;
	}
	
	/**
	 * 是否好友
	 * @param targetId  好友的ID
	 * @param type      好友的类型
	 * @return true 是该类型的好友  false 反之
	 */
	public boolean isFriend(long targetId,FriendType type){
		if(type == FriendType.BLACK){
			return black.containsKey(targetId);
		}else if(type == FriendType.FRIENDLY){
			return friendly.containsKey(targetId);
		}else if(type == FriendType.NEAREST){
			return nearest.containsKey(targetId);
		}
		return false;
	}
	
	
	/**
	 * 用户好友列表中的玩家
	 * @param targetId     目标玩家ID
	 * @param type         好友类型
	 * @return true 删除成功 false 删除失败
	 */
	public synchronized Friend removeFriend(long targetId,FriendType type){
		if(type == FriendType.BLACK){
			Friend friend = black.remove(targetId);
			return friend;
		}else if(type == FriendType.FRIENDLY){
			Friend friend = friendly.remove(targetId);
			return friend;
		}else if(type == FriendType.NEAREST){
			Friend friend = nearest.remove(targetId);
			return friend;
		}
		return null;
	}
	
	
	/**
	 * 向结构中添加好友,该方法会自动构建
	 * @param friend
	 */
	public synchronized void addFriend(Friend friend){
		if(friend.getType() == FriendType.FRIENDLY){
			this.friendly.put(friend.getTargetId(), friend);
		}else if(friend.getType() == FriendType.BLACK){
			this.black.put(friend.getTargetId(), friend);
		}else if(friend.getType() == FriendType.NEAREST){
			this.nearest.put(friend.getTargetId(), friend);
		}
	}

	/**
	 * 获取好友对象信息
	 * @param targetId   目标玩家ID
	 * @param type       好友类型
 	 * @return {@link Friend} 好友对象
	 */
	public Friend getFriends(long targetId,FriendType type){
		if(type == FriendType.BLACK){
			return black.get(targetId);
		}else if(type == FriendType.FRIENDLY){
			return friendly.get(targetId);
		}else if(type == FriendType.NEAREST){
			return nearest.get(targetId);
		}
		return null;
	}
	
	/**
	 * 获取与好友的有好值
	 * @param targetId  目标玩家的ID
	 * @return
	 */
	public int getFriendlyValue(long targetId){
		Friend friend = this.friendly.get(targetId);
		if(friend == null){
			return 0;
		}
		return friend.getValue();
	}
	
	/**
	 * 更具类型返回集合
	 * @param type    类型
	 * @return
	 */
	public Collection<Friend> getFriend4Type(FriendType type){
		if(FriendType.FRIENDLY == type){
			return this.friendly.values();
		}else if(FriendType.BLACK == type){
			return this.black.values();
		}else if(FriendType.NEAREST == type){
			return this.nearest.values();
		}
		return Collections.emptyList();
	}
	
	/**
	 * 与好友组队击杀怪物,增加好友击杀值用于增加好友度
	 * @param targetId   目标玩家的ID
	 */
	public synchronized boolean plusKill4Monster(Long targetId){
		Integer limit = this.friendlyLimt.get(targetId);
		if(limit == null){
			limit = 0;
		}
		
		limit += 1;
		this.friendlyLimt.put(targetId, limit);
		
		if(limit >= KILL_MONSTER_LIMIT){
			Friend friend = this.friendly.get(targetId);
			friend.setValue(friend.getValue() + 10);
			this.friendlyLimt.put(targetId, 0);
			return true;
		}
		
		return false;
	}
	
	/**
	 * 更具类型获取 大小
	 * @param type    类型
	 * @return
	 */
	public int size4Type(FriendType type){
		if(FriendType.FRIENDLY == type){
			return this.friendly.size();
		}else if(FriendType.BLACK == type){
			return this.black.size();
		}else if(FriendType.NEAREST == type){
			return this.nearest.size();
		}
		return 0;
	}	
	
	/**
	 * 获取黑名单跟好友的playerId
	 * 
	 * @return {@link Collection}
	 */
	public Set<Long> getAllFriends() {
		Set<Long> idSet = new HashSet<Long>();
		idSet.addAll(friendly.keySet());
		idSet.addAll(black.keySet());
		return idSet;
	}
	
	/** 重置杀怪数量 */
	public synchronized void resetTargetMonster(long targetId) {
		this.friendlyLimt.remove(targetId);
	}
	
	
	//Getter and Setter...
	public long getPlayerId() {
		return playerId;
	}
	
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (playerId ^ (playerId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserFriend other = (UserFriend) obj;
		if (playerId != other.playerId)
			return false;
		return true;
	}
	
}
