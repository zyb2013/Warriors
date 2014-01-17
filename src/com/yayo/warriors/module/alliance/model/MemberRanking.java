package com.yayo.warriors.module.alliance.model;

/**
 * 帮派成员的排行对象
 * 
 * @author liuyuhua
 */
public class MemberRanking implements Comparable<MemberRanking> {

	/** 玩家ID*/
	private long playerId;
	
	/** 等级*/
	private Integer level = 0;
	
	/** 职位*/
	private Integer title = 0;
	
	/** 0:不在线,1:在线*/
	private Integer online = 0;

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getTitle() {
		return title;
	}

	public void setTitle(Integer title) {
		this.title = title;
	}

	public Integer getOnline() {
		return online;
	}

	public void setOnline(Integer online) {
		this.online = online;
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (playerId ^ (playerId >>> 32));
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MemberRanking other = (MemberRanking) obj;
		if (playerId != other.playerId)
			return false;
		return true;
	}

	
	public String toString() {
		return "MemberRanking [playerId=" + playerId + ", online=" + online
				+ ", level=" + level + ", title=" + title + "]";
	}

	/**
	 * 构造方法
	 * @param playerId   玩家的ID
	 * @param level      玩家的等级
	 * @param title      玩家的职位
	 * @param online     玩家是否在线
	 * @return {@link @MemberRanking} 帮派成员排序
	 */
	public static MemberRanking valueOf(long playerId, int level, int title, int online) {
		MemberRanking ranking = new MemberRanking();
		ranking.playerId = playerId;
		ranking.level  = level;
		ranking.title  = title;
		ranking.online = online;
		return ranking;
	}

	
	public int compareTo(MemberRanking o) {
		if(o == null) {
			return -1;
		}
		
		Integer targetOnline = o.getOnline();
		Integer targetLevel = o.getLevel();
		Integer targetTitle = o.getTitle();
		
		int onlineCompare = targetOnline.compareTo(this.online);
		if(onlineCompare > 0){
			return onlineCompare;
		}else if(onlineCompare == 0){
			int titleCompare = targetTitle.compareTo(this.title);
			if(titleCompare > 0) {
				return titleCompare;
			} else if(titleCompare == 0) {
				return targetLevel.compareTo(this.level);
			}
		}

		return -1;
	}
	
	
}
