package com.yayo.warriors.module.active.model;

import com.yayo.warriors.basedb.model.ActiveMonsterConfig;
import com.yayo.warriors.module.monster.domain.MonsterDomain;

/**
 * 怪物活动玩法,模型容器
 * @author liuyuhua
 */
public class ActiveLive {
	
	/** 怪物域对象*/
	private MonsterDomain monsterDomain;
	
	/** 活动怪物配置*/
	private ActiveMonsterConfig activeMonsterConfig;
	
	/** 刷新时间、创建时间 都是同一个时间*/
	private long refreshTime = 0;
	
	/** 刷新次数*/
	private long refreshCount = 0;
	
	/**
	 * 构造方法
	 * @param monsterDomain               怪物对象
	 * @param activeMonsterConfig         活动怪物配置
	 * @return {@link ActiveLive}         怪物活动玩法
	 */
	public static ActiveLive valueOf(MonsterDomain monsterDomain,ActiveMonsterConfig activeMonsterConfig){
		ActiveLive round = new ActiveLive();
		round.monsterDomain = monsterDomain;
		round.refreshTime = System.currentTimeMillis();
		round.refreshCount = 1;
		round.activeMonsterConfig = activeMonsterConfig;
		return round;
	}
	
	/**
	 * 是否可以刷新
	 * @return  true 可以刷新 false 不可以刷新
	 */
	public boolean canRefresh(){
		long cdTime = activeMonsterConfig.getRefuTime();//刷星间隔时间
		long currentTime = System.currentTimeMillis();
		if(currentTime - refreshTime >= cdTime){
			refreshTime = currentTime;//重置刷新时间
			return true;
		}
		return false;
	}
	
	//Getter and Setter...
	public MonsterDomain getMonsterDomain() {
		return monsterDomain;
	}

	public void setMonsterDomain(MonsterDomain monsterDomain) {
		this.monsterDomain = monsterDomain;
	}

	public long getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}

	public long getRefreshCount() {
		return refreshCount;
	}

	public void setRefreshCount(long refreshCount) {
		this.refreshCount = refreshCount;
	}

	public ActiveMonsterConfig getActiveMonsterConfig() {
		return activeMonsterConfig;
	}

	public void setActiveMonsterConfig(ActiveMonsterConfig activeMonsterConfig) {
		this.activeMonsterConfig = activeMonsterConfig;
	}

	@Override
	public String toString() {
		return "ActiveLive [monsterDomain=" + monsterDomain
				+ ", activeMonsterConfig=" + activeMonsterConfig
				+ ", refreshTime=" + refreshTime + ", refreshCount="
				+ refreshCount + "]";
	}
	
}
