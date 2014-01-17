package com.yayo.warriors.socket.vo;

import java.io.Serializable;
import java.util.Map;

import com.yayo.warriors.module.friends.entity.Friend;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;

/**
 * 发给客户端的好友集合 
 * 
 * @author liuyuhua
 */
public class FriendsVo implements Serializable {
	
	private static final long serialVersionUID = -1091030350419082661L;

	/** 唯一标识*/
	private Long id;
	
	/** 阵营 */
	private int camp;
	
	/** 玩家ID*/
	private Long playerId;
	
	/** 玩家名字*/
	private String name;
	
	/** 职业 */
	private int userJob;
	
	/** 类型*/
	private int friendType;
	
	/** 好友值 */
	private int value;
	
	/** 等级 */
	private int level;
	
	/** 是否有摊位  0表示没有*/
	private int market;
	
	/** 是否敬酒*/
	private boolean greet;
	
	/** 是否在线*/
	private boolean online;
	
	/** 敬酒时间 */
	private long greetTime;
	
	/**
	 * 构建好友列表VO
	 * @param friend
	 * @param player
	 * @param battle
	 * @param isGreet
	 * @param online
	 * @param market
	 * @return
	 */
	public static FriendsVo valueOf(Friend friend, Player player, PlayerBattle battle, boolean isGreet, boolean online, int market) {
		FriendsVo vo = new FriendsVo();
		vo.market = market;
		vo.online = online;
		vo.greet = isGreet;
		vo.id = friend.getId();
		vo.name = player.getName();
		vo.level = battle.getLevel();
		vo.value = friend.getValue();
		vo.playerId = player.getId();
		vo.camp = player.getCamp().ordinal();
		vo.userJob = battle.getJob().ordinal();
		vo.friendType = friend.getType().ordinal();
		return vo;
	}

	/**
	 * 构建敬酒历史记录VO
	 * @param player
	 * @param time
	 * @return
	 */
	public static FriendsVo valueOf(Player player, boolean isGreet, long time) {
		FriendsVo vo = new FriendsVo();
		vo.playerId = player.getId();
		vo.name = player.getName();
		vo.camp = player.getCamp().ordinal();
		vo.greetTime = time;
		vo.greet = isGreet;
		return vo;
	}
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getUserJob() {
		return userJob;
	}

	public void setUserJob(int userJob) {
		this.userJob = userJob;
	}

	public int getFriendType() {
		return friendType;
	}

	public void setFriendType(int friendType) {
		this.friendType = friendType;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getMarket() {
		return market;
	}

	public void setMarket(int market) {
		this.market = market;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public boolean isGreet() {
		return greet;
	}

	public void setGreet(boolean isGreet) {
		this.greet = isGreet;
	}
	
	public int getCamp() {
		return camp;
	}

	public void setCamp(int camp) {
		this.camp = camp;
	}
	
	public long getGreetTime() {
		return greetTime;
	}

	public void setGreetTime(long greetTime) {
		this.greetTime = greetTime;
	}
	
	@Override
	public String toString() {
		return "FriendsVo [id=" + id + ", playerId=" + playerId + ", name="
				+ name + ", userJob=" + userJob + ", friendType=" + friendType
				+ ", value=" + value + ", level=" + level + ", market="
				+ market + ", online=" + online + "]";
	}


}
