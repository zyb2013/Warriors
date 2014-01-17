package com.yayo.warriors.module.friends.entity;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.friends.FriendRule;

/**
 * 好友奖励实体
 * 
 * @author huachaoping
 */
@Entity
@Table(name="friendsTreasure")
public class FriendsTreasure extends BaseModel<Long>{

	private static final long serialVersionUID = 6201643052151873261L;

	/** 主键id */
	@Id
	@Column(name="playerId")
	private Long playerId;
	
	/** 已储存的祝福经验 */
	private int blessExp;
	
	/** 饮酒刷新时间 */
	private int cleanDate;
	
	/** 蓄酒量 */
	private int wineMeasure;
	
	/** 是否已领取祝福经验 */
	private boolean isReward;
	
	/** 是否已饮酒 */
	private boolean isDrinked;
	
	/** 一键征友, 玩家是否已操作参数 */
	private String params = "";
	
	/** 已敬酒的玩家ID: 玩家ID_玩家ID_.. */
	@Lob
	private String greetFriends = "";
	
	/** 已敬酒玩家缓存 */
	@Transient
	private transient Set<Long> greetSet = null;
	
	/** 敬酒历史记录 */
	@Transient
	private transient Map<Long, Long> greetHistoryMap = null; 
	
	
	/** 构造函数 */
	public static FriendsTreasure valueOf(long playerId) {
		FriendsTreasure bless = new FriendsTreasure();
		bless.playerId = playerId;
		return bless;
	}
	
	
	@Override
	public Long getId() {
		return playerId;
	}

	@Override
	public void setId(Long id) {
		this.playerId = id;
	}

	public int getBlessExp() {
		return blessExp;
	}

	public void setBlessExp(int storeExp) {
		this.blessExp = storeExp;
	}

	public void addExp(int exp) {
		this.blessExp += exp;
	}
	
	public void useExp(int exp) {
		this.blessExp -= exp;
	}
	
	public boolean isReward() {
		return isReward;
	}

	public void setReward(boolean isReward) {
		this.isReward = isReward;
	}
	
	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}
	
	public int getCleanDate() {
		return cleanDate;
	}

	public void setCleanDate(int cleanDate) {
		this.cleanDate = cleanDate;
	}

	public int getWineMeasure() {
		return wineMeasure;
	}

	public void setWineMeasure(int wineMeasure) {
		this.wineMeasure = wineMeasure;
	}

	public boolean isDrinked() {
		return isDrinked;
	}

	public void setDrinked(boolean isDrinked) {
		this.isDrinked = isDrinked;
	}

	public void setGreetFriends(String greetFriends) {
		this.greetFriends = greetFriends;
	}
	
	public void addWine(int value) {
		this.wineMeasure += value;
	}
	
	public Map<Long, Long> getGreetHistoryMap() {
		return greetHistoryMap;
	}
	
	/**
	 * 获取已征友操作的等级
	 * 
	 * @return {@link Set}
	 */
	public Set<Integer> getParamsSet() {
		Set<Integer> set = new HashSet<Integer>();
		if(StringUtils.isBlank(this.params)) {
			return set;
		}

		String[] array = this.params.split(Splitable.ATTRIBUTE_SPLIT);
		for (String element : array) {
			set.add(Integer.valueOf(element));
		}
		
		return set;
	}
	
	/**
	 * 更新参数
	 * 
	 * @param set
	 */
	public void updateParamsSet(Set<Integer> set) {
		StringBuilder builder = new StringBuilder();
		for (Integer level : set) {
			builder.append(level).append(Splitable.ATTRIBUTE_SPLIT);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		this.params = builder.toString();
	}
	
	
	public boolean clearData() {
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DATE);
		if (day != cleanDate) {
			cleanDate = day;
			isDrinked = false;
			greetFriends = "";
			return true;
		}
		return false;
	}
	
	
	public void updateGreetFriends(long playerId) {
		this.greetFriends = new StringBuffer().append(greetFriends).append(playerId).append("_").toString();
	}
	
	public void addGreetFriends(long playerId) {
		this.greetSet.add(playerId);
	}
	
	public Set<Long> getGreetFriends() {
		if (greetSet != null) {
			return greetSet;
		}
		
		greetSet = new HashSet<Long>();
		if (StringUtils.isEmpty(greetFriends)) {
			return greetSet;
		}
		
		String[] arrays = greetFriends.split(Splitable.ATTRIBUTE_SPLIT);
		for (String element : arrays) {
			greetSet.add(Long.valueOf(element));
		}
		return greetSet;
	}

	
	public void put2HistoryMap(long playerId) {
		if (this.greetHistoryMap == null) {
			this.greetHistoryMap = new LinkedHashMap<Long, Long>();
		}
		if (this.greetHistoryMap.size() >= FriendRule.HISTORY_RECORD) {
			Iterator<Entry<Long, Long>> it = this.greetHistoryMap.entrySet().iterator();
			this.greetHistoryMap.entrySet().remove(it.next());
		}
		
		this.greetHistoryMap.put(playerId, System.currentTimeMillis());
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((playerId == null) ? 0 : playerId.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof FriendsTreasure))
			return false;
		FriendsTreasure other = (FriendsTreasure) obj;
		return playerId != null && other.playerId != null && playerId.equals(other.playerId);
	}


}
