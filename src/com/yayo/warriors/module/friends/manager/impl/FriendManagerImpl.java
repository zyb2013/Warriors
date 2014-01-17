package com.yayo.warriors.module.friends.manager.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.cache.CachedServiceAdpter;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.model.FriendAddedConfig;
import com.yayo.warriors.module.friends.dao.FriendsDao;
import com.yayo.warriors.module.friends.entity.Friend;
import com.yayo.warriors.module.friends.entity.FriendsTreasure;
import com.yayo.warriors.module.friends.helper.FriendHelper;
import com.yayo.warriors.module.friends.manager.FriendManager;
import com.yayo.warriors.module.friends.model.UserFriend;
import com.yayo.warriors.module.friends.type.FriendState;
import com.yayo.warriors.module.friends.type.FriendType;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 好友管理
 * 
 * @author huachaoping
 */
@Component
public class FriendManagerImpl extends CachedServiceAdpter implements LogoutListener, FriendManager{

	/** 玩家的好友列表 */
	private final ConcurrentLinkedHashMap<Long, UserFriend> userFriends = new ConcurrentLinkedHashMap.Builder<Long, UserFriend>().maximumWeightedCapacity(4000).build();
	
	@Autowired
	private FriendsDao friendsDao;
	@Autowired
	private DbService dbService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ResourceService resourceService;
	
	/** 日志格式 */
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/** 好友模块的HashKey */
	private static final String HASH_KEY = "FRIENDS_";
	/** 玩家的SubKey前缀 */
	private static final String PLAYER_FOCUS = "PLAYEFOCUS_";
	/**  */
	private static final String ALL_FOCUS = "ALLFOCUS_";
	
	
	
	public void onLogoutEvent(UserDomain userDomain) {
		this.userFriends.remove(userDomain.getPlayerId());
	}

	
	public void createFriend(long playerId,Friend friend) {
		try {
			friendsDao.save(friend);
			UserFriend userFriend = this.getUserFriend(playerId);
			userFriend.addFriend(friend);
		} catch (Exception e) {
			friend = null;
			return;
		}
	}

	
	public Friend plusKill4Monster(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return null;
		}
		
		UserFriend userFriend = this.getUserFriend(playerId);
		if(userFriend == null){
			return null;
		}

		Friend friend = userFriend.getFriends(targetId, FriendType.FRIENDLY);
		if(friend == null){
			return null;
		}
		
		if (userFriend.plusKill4Monster(targetId)) {
			this.dbService.submitUpdate2Queue(friend);
			FriendHelper.plusFriendValue(userDomain.getPlayer(), userDomain.getPlayerId(), friend, 10);
		}
		return friend;
	}

	
	public Friend raiseFriendly(long playerId, long targetId, int value) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return null;
		}
		
		UserFriend userFriend = this.getUserFriend(playerId);
		Friend friend = userFriend.getFriends(targetId, FriendType.FRIENDLY);
		if (friend == null) {
			return null;
		}

		ChainLock lock = LockUtils.getLock(friend);
		try {
			lock.lock();
			friend.increaseValue(value);
		} finally {
			lock.unlock();
		}
		
		this.dbService.submitUpdate2Queue(friend);
		return friend;
	}

	
	public int getFriendlyValue(long playerId, long targetId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return 0;
		}
		
		UserFriend userFriend = this.getUserFriend(playerId);
		if(userFriend == null){
			return 0;
		}
		
		return userFriend.getFriendlyValue(targetId);
	}

	
	@Deprecated
	public Friend plusHatred(long playerId, long targetId) {
//		UserDomain userDomain = userManager.getUserDomain(playerId);
//		if(userDomain == null){
//			return null;
//		}
//		
//		UserFriend userFriend = this.getUserFriend(playerId);
//		if(userFriend == null){
//			return null;
//		}
//
//		Friend friend = userFriend.getFriends(targetId, FriendType.HATRED);
//		if (friend == null) {
//			friend = Friend.valueOf(playerId, targetId, FriendType.HATRED);
//			userFriend.addFriend(friend);
//			friend = userFriend.getFriends(targetId, FriendType.HATRED);
//		}
//		
//		ChainLock lock = LockUtils.getLock(friend);
//		try {
//			lock.lock();
//			friend.increaseValue(FRIEND_INCEASE_VALUE);
//		}finally{
//			lock.unlock();
//		}
//		
//		friendsDao.createFriends(friend);
		return null;
	}

	
	@Deprecated
	public Friend allayHatred(long playerId, long targetId) {
//		UserDomain userDomain = userManager.getUserDomain(playerId);
//		if(userDomain == null){
//			return null;
//		}
//		
//		UserFriend userFriend = this.getUserFriend(playerId);
//		if(userFriend == null){
//			return null;
//		}
//		Friend friend = userFriend.getFriends(targetId, FriendType.HATRED);
//		if (friend == null) {
//			return null;
//		}
//
//		ChainLock lock = LockUtils.getLock(friend);
//		try {
//			lock.lock();
//			if(friend.getValue() <= 0){
//				return friend;
//			}
//			friend.decreaseValue(FRIEND_INCEASE_VALUE);
//		} finally {
//			lock.unlock();
//		}
//		
//		this.dbService.submitUpdate2Queue(friend);
		return null;
	}
	
	
	public boolean isFriend(long playerId, long targetId, FriendType type) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return false;
		}
		
		UserFriend userFriend = this.getUserFriend(playerId);
		if(userFriend == null){
			return false;
		}
		
		return userFriend.isFriend(targetId, type);
	}
	

	/**
	 * 加载玩家好友列表
	 * @param playerId           玩家的ID
	 * @return {@link UserFriend}
	 */
	private UserFriend getUserFriend(long playerId) {
		UserFriend userFriend = userFriends.get(playerId);
		if(userFriend != null){
			return userFriend;
		}
		
		userFriend = UserFriend.valueOf(playerId);
		List<Long> friendIds = friendsDao.getFriends(playerId);
		if (friendIds == null || friendIds.isEmpty()) {
			return userFriend;
		}
			
		for(long friendId : friendIds){
			Friend friend = friendsDao.get(friendId, Friend.class);
			if(friend != null){
				userFriend.addFriend(friend);
			}
		}
			
		userFriends.putIfAbsent(playerId, userFriend);
		return userFriends.get(playerId);
	}

	
	
	public boolean removeFriend(long playerId,long targetId, FriendType type) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return false;
		}
		
		UserFriend userFriend = this.getUserFriend(playerId);
		if(userFriend == null){
			return false;
		}
		
		Friend friend = userFriend.removeFriend(targetId, type);
		if(friend == null){
			return false;
		}
		ChainLock lock = LockUtils.getLock(friend);
		try {
			lock.lock();
			friend.setState(FriendState.drop);
		}finally{
			lock.unlock();
		}
		dbService.submitUpdate2Queue(friend);
		return true;
	}

	
	public Collection<Friend> getFirends(long playerId, FriendType type) {
		UserFriend userFriend = this.getUserFriend(playerId);
		if(userFriend != null) {
			return userFriend.getFriend4Type(type);
		}
		return Collections.emptyList();
	}

	
	public int size4Type(long playerId, FriendType type) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null){
			return 0;
		}
		
		UserFriend userFriend = this.getUserFriend(playerId);
		if(userFriend == null){
			return 0;
		}
		return userFriend.size4Type(type);
	}

	/**
	 * 获取好友祝福瓶对象
	 * @param playerId  好友的ID
	 */
	public FriendsTreasure getFriendsTreasure(long playerId){
		if (playerId > 0L) {
			return this.get(playerId, FriendsTreasure.class);
		}
		return null;
	}
	
	
	
	
	public Fightable getFriendAddedValue(int friendlyValue) {
		Fightable fightable = new Fightable();
		Collection<FriendAddedConfig> configList = resourceService.listAll(FriendAddedConfig.class);
		for (FriendAddedConfig config : configList) {
			if (config.isFriendlyAdded(friendlyValue)) {
				Map<Integer, Integer> attrMap = config.getAttributeMap();
				fightable.putAll(attrMap);
				break;
			}
		}
		return fightable;
	}
	
	
	
	public Collection<Long> getfriendIds(long playerId) {
		Set<Long> idSet = null;
		if (playerId > 0L) {
			UserFriend userFriend = getUserFriend(playerId);
			idSet = (Set<Long>) userFriend.getAllFriends();
		}
		return idSet;
	}
	
	
	
	public Friend getPlayerFriend(long playerId, long targetId) {
		UserFriend userFriend = getUserFriend(playerId);
		return userFriend.getFriends(targetId, FriendType.FRIENDLY);
	}
	
	
	
	public void resetKillMonsterCount(long playerId, long targetId) {
		UserFriend userFriend = getUserFriend(playerId);
		userFriend.resetTargetMonster(targetId);
	}
	
	
	/**
	 * 从数据库取实体对象. 如果不需要从数据库去取, 或者需要不存在则新创建, 则重写该接口
	 * 
	 * @param <T>
	 * @param <PK>
	 * @param  id 			主键id
	 * @param  clazz 		实体类
	 * @return T			实体对象
	 */
	@SuppressWarnings("unchecked")
	protected <T, PK extends Serializable> T getEntityFromDB(PK id, Class<T> clazz) {
		if(clazz != null && clazz == FriendsTreasure.class){
			FriendsTreasure friendsTreasure = friendsDao.get(id, FriendsTreasure.class);
			try {
				if(friendsTreasure == null){
					friendsTreasure = FriendsTreasure.valueOf((Long)id);
					friendsDao.save(friendsTreasure);
				}
			} catch (Exception e) {
				friendsTreasure = null;
				logger.error("角色:[{}] 创建好友祝福瓶信息异常:{}", id, e);
			}
			return (T)friendsTreasure;
		}
		
		return super.getEntityFromDB(id, clazz);
	}

	
	@SuppressWarnings("unchecked")
	public Set<Long> getFriendsFocus(long playerId) {
		String subKey =  this.focusSubKey(playerId);
		Set<Long> result = (Set<Long>) this.cachedService.getFromCommonCache(HASH_KEY, subKey);
		if(result == null){
			List<Long> ids = friendsDao.getFriendsFocus(playerId, FriendType.FRIENDLY);
			result = new HashSet<Long>(ids);
			this.cachedService.put2CommonHashCache(HASH_KEY, subKey, result);
			result = (Set<Long>) this.cachedService.getFromCommonCache(HASH_KEY, subKey);
		}
		return result;
	}
	

	
	@SuppressWarnings("unchecked")
	public Set<Long> getAllFocus(long targetId) {
		String subkey = ALL_FOCUS + targetId;
		Set<Long> result = (Set<Long>) cachedService.getFromCommonCache(HASH_KEY, subkey);
		if (result == null) {
			List<Long> playerIds = friendsDao.getAllFocus(targetId);
			result = new HashSet<Long>(playerIds);
			cachedService.put2CommonHashCache(HASH_KEY, subkey, result);
			result = (Set<Long>) this.cachedService.getFromCommonCache(HASH_KEY, subkey);
		}
		return result;
	}

	/**
	 * 关注Key
	 * @param playerId
	 * @return
	 */
	public String focusSubKey(long playerId){
		return PLAYER_FOCUS + playerId;
	}

	
	
	
	public void removeFriendFocusCache(long playerId) {
		cachedService.removeFromCommonHashCache(HASH_KEY, focusSubKey(playerId));
		cachedService.removeFromCommonHashCache(HASH_KEY, ALL_FOCUS + playerId);
	}


}
