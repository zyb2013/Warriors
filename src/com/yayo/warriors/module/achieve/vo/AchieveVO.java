package com.yayo.warriors.module.achieve.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 成就总览VO
 * 
 * @author huachaoping
 */
public class AchieveVO implements Serializable{

	private static final long serialVersionUID = -7414535638556447874L;
	
	// ---------- 每个类型的总成就数 ------------
	private int achieve0;
	private int achieve1;
	private int achieve2;
	private int achieve3;
	private int achieve4;
	private int achieve5;
	
	
	/** 等级成就达成数 */
	private int levelAchieveCount;
	
	/** 第一次成就达成数 */
	private int firstAchieveCount;
	
	/** 连续登录成就达成数 */
	private int loginAchieveCount;
	
	/** 总登录成就达成数 */
	private int totalAchieveCount;
	
	/** 在线时长成就达成数 */
	private int onlineAchieveCount;

	/** 杀怪成就达成数 */
	private int monsterAchieveCount;
	
	
	/** 已领奖ID列表 */
	private Object[] receivedId = {};
	
	/** 已达成成就ID列表 */
	private Object[] achievedIds = {};

	/** 未领奖ID */
	private Object[] nonReceivedId = {};
	
	
	/** 所有达成成就 */
	private transient List<Integer> ids = null;
	
	
	public int getAchieve0() {
		return achieve0;
	}

	public void setAchieve0(int achieve0) {
		this.achieve0 = achieve0;
	}

	public int getAchieve1() {
		return achieve1;
	}

	public void setAchieve1(int achieve1) {
		this.achieve1 = achieve1;
	}

	public int getAchieve2() {
		return achieve2;
	}

	public void setAchieve2(int achieve2) {
		this.achieve2 = achieve2;
	}

	public int getAchieve3() {
		return achieve3;
	}

	public void setAchieve3(int achieve3) {
		this.achieve3 = achieve3;
	}

	public int getAchieve4() {
		return achieve4;
	}

	public void setAchieve4(int achieve4) {
		this.achieve4 = achieve4;
	}

	public int getAchieve5() {
		return achieve5;
	}

	public void setAchieve5(int achieve5) {
		this.achieve5 = achieve5;
	}
	
	public List<Integer> getIds() {
		return ids;
	}

	public void setIds(Collection<Integer> ids) {
		this.ids = new ArrayList<Integer>(ids.size());
		this.ids.addAll(ids);
	}
	
	public Object[] getAchievedIds() {
		return achievedIds;
	}

	public void setAchievedIds(Object[] achievedIds) {
		this.achievedIds = achievedIds;
	}

	public Object[] getReceivedId() {
		return receivedId;
	}

	public void setReceivedId(Object[] receivedId) {
		this.receivedId = receivedId;
	}
	
	public int getLevelAchieveCount() {
		return levelAchieveCount;
	}

	public void setLevelAchieveCount(int levelAchieveCount) {
		this.levelAchieveCount = levelAchieveCount;
	}

	public int getMonsterAchieveCount() {
		return monsterAchieveCount;
	}

	public void setMonsterAchieveCount(int monsterAchieveCount) {
		this.monsterAchieveCount = monsterAchieveCount;
	}

	public int getFirstAchieveCount() {
		return firstAchieveCount;
	}

	public void setFirstAchieveCount(int firstAchieveCount) {
		this.firstAchieveCount = firstAchieveCount;
	}

	public int getLoginAchieveCount() {
		return loginAchieveCount;
	}

	public void setLoginAchieveCount(int loginAchieveCount) {
		this.loginAchieveCount = loginAchieveCount;
	}

	public int getTotalAchieveCount() {
		return totalAchieveCount;
	}

	public void setTotalAchieveCount(int totalAchieveCount) {
		this.totalAchieveCount = totalAchieveCount;
	}

	public int getOnlineAchieveCount() {
		return onlineAchieveCount;
	}

	public void setOnlineAchieveCount(int onlineAchieveCount) {
		this.onlineAchieveCount = onlineAchieveCount;
	}

	public Object[] getNonReceivedId() {
		return nonReceivedId;
	}

	public void setNonReceivedId(Object[] nonReceivedId) {
		this.nonReceivedId = nonReceivedId;
	}


}
