package com.yayo.warriors.socket.vo;

import java.io.Serializable;

import com.yayo.warriors.module.user.type.Job;

/**
 * 搜索好友VO
 * @author liuyuhua
 */
public class FriendsSearchVo implements Serializable {
	private static final long serialVersionUID = -7590651415985531570L;
	
	/** 玩家ID	 */
	private long playerId;

	/** 玩家名字*/
	private String name;
	
	/** 头像Id */
	private int iconId;
	
	/** 玩家等级*/
	private int level;
	
	/** 玩家职业*/
	private Job clazz;
	
	/** 是否好友 */
	private boolean friend;
	
	/**
	 * 构造函数
	 * @param name    名字
	 * @param level   等级
	 * @param clazz   职业
	 * @return
	 */
	public static FriendsSearchVo valueOf(long playerId,String name,int level,Job clazz,boolean friend){
		FriendsSearchVo vo = new FriendsSearchVo();
		vo.playerId = playerId;
		vo.name  = name;
		vo.level = level;
		vo.clazz = clazz;
		vo.friend = friend;
		return vo;
	}
	
	
	public static FriendsSearchVo valueOf(long playerId, String name, Job clazz, int iconId) {
		FriendsSearchVo vo = new FriendsSearchVo();
		vo.playerId = playerId;
		vo.name = name;
		vo.clazz = clazz;
		vo.iconId = iconId;
		return vo;
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Job getClazz() {
		return clazz;
	}

	public void setClazz(Job clazz) {
		this.clazz = clazz;
	}

	public boolean isFriend() {
		return friend;
	}

	public void setFriend(boolean friend) {
		this.friend = friend;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public int getIconId() {
		return iconId;
	}

	public void setIconId(int iconId) {
		this.iconId = iconId;
	}
	
}
