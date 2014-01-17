package com.yayo.warriors.module.friends.facade;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.friends.entity.Friend;
import com.yayo.warriors.module.friends.entity.FriendsTreasure;
import com.yayo.warriors.module.friends.type.FriendType;
import com.yayo.warriors.module.server.listener.LoginListener;
import com.yayo.warriors.socket.vo.FriendsSearchVo;

/**
 * 好友\黑名单接口
 * @author liuyuhua
 */
public interface FriendsFacade extends LoginListener {

	/**
	 * 加载所有好友
	 * @param playerId   玩家的ID
	 * @param type       类型 
	 * @return
	 */
	public ResultObject<Collection<Friend>> loadAllFriend(Long playerId,FriendType type);
	
	/**
	 * 添加好友申请
	 * @param playerId   玩家的ID
	 * @param targetId   目标玩家的ID
	 * @return 返回结果
	 */
	public ResultObject<Friend> addFriend(Long playerId,Long targetId);

	
	/**
	 * 删除好友
	 * @param playerId   玩家的ID
	 * @param targetId   目标玩家的ID
	 * @param type       玩家类型
	 */
	public ResultObject<Long> deleteFriend(Long playerId,Long targetId,FriendType type);
	
	/**
	 * 添加黑名单
	 * @param playerId   玩家的ID
	 * @param targetId   目标玩家的ID
	 * @param 信息
	 */
	public ResultObject<Friend> addBlack(Long playerId,Long targetId);
	
	/**
	 * 添加最近联系人
	 * @param playerId   玩家的ID
	 * @param targetId   目标玩家的ID
	 * @return
	 */
	public ResultObject<Friend> addNearest(Long playerId,Long targetId);
	
	/**
	 * 查找玩家 名字
	 * @return Collection<FriendsSearchVo> - {玩家名字,玩家ID} 
	 */
	public ResultObject<Collection<FriendsSearchVo>> searchPlayerName(String keywords,long playerId); 
	
	/**
	 * 随机选中玩家ID
	 * @param mapId
	 * @return
	 */
	public ResultObject<Long> getRandomPlayerId(Long playerId,Integer sex);
	
	/**
	 * 获得好友祝福实体
	 * @param playerId
	 * @return
	 */
	public FriendsTreasure getFriendsBless(long playerId);
	
	/**
	 * 好友祝福(祝福瓶)
	 * @param playerId         发送祝福的玩家ID
	 * @param targetId         收到祝福的玩家ID
	 * @return
	 */
	public int getBless(long playerId,long targetId);
	
	/**
	 * 领取祝福瓶经验
	 * @param playerId         玩家ID
	 * @return
	 */
	public int rewardBlessExp(long playerId);
	
	
	/**
	 * 一键征友, 服务器随机12名玩家
	 * @param playerId
	 * @return {@link FriendsSearchVo}
	 */
	public Collection<FriendsSearchVo> listRandomPlayer(Long playerId);
	
	
	/**
	 * 获取祝福瓶状态
	 * @param playerId
	 * @return {@link Boolean}
	 */
	public Map<String, Object> friendsCollected(long playerId);
	
	
	
	/**
	 * 好友赠酒(增加好友度)
	 * 
	 * @param playerId                  用户ID
	 * @param targetId                  目标ID 
	 * @param userProps                 用户道具ID
	 * @return {@link CommonConstant}
	 */ 
	public long friendsPresentWine(long playerId, long targetId, String userItems);
	
	
	/**
	 * 好友敬酒
	 * 
	 * @param playerId                  
	 * @param targetId
	 * @return {@link CommonConstant}
	 */
	public int greetFriends(long playerId, long targetId);
	
	
	/**
	 * 喝酒领奖
	 * 
	 * @param playerId
	 * @return {@link CommonConstant}
	 */
	public int drinkWine(long playerId);
	
	
	/**
	 * 是否敬酒
	 * 
	 * @param playerId
	 * @param targetId
	 * @return
	 */
	public boolean isGreet(long playerId, long targetId);
	
	
	/**
	 * 一键祝福好友
	 * 
	 * @param playerId
	 * @param playerIds
	 * @return
	 */
	public int blessFriends(long playerId, List<Long> playerIds);
	
}
