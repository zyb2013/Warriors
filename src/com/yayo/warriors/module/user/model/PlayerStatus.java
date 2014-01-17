package com.yayo.warriors.module.user.model;

import java.util.concurrent.ConcurrentHashMap;

import com.yayo.warriors.module.fight.rule.FightRule;
import com.yayo.warriors.module.user.type.StatusType;

/**
 * 角色的状态
 * 
 * @author Hyint
 */
public class PlayerStatus {
	
	/** 角色的战斗状态结束时间 */
	private long fightEndTime = 0L;
	/** 角色的状态缓存 */
	private ConcurrentHashMap<StatusType, StatusElement> STATUS_CACHES = new ConcurrentHashMap<StatusType, StatusElement>(5);
	
	/**
	 * 更新战斗的结束时间
	 * 
	 * @param effectTime
	 */
	public void updateFightStatus() {
		fightEndTime = System.currentTimeMillis() + FightRule.FIGHT_TIMEOUT;
	}

	/**
	 * 验证释放在战斗中, 如果不在战斗中, 清空攻击列表
	 * 
	 * @return {@link Boolean}		true-战斗中, false-不在战斗中
	 */
	public boolean isFighting() {
		return this.fightEndTime > System.currentTimeMillis();
	}
	
	/**
	 * 取得战斗结束时间
	 * 
	 * @return {@link Long}			战斗结束时间
	 */
	public long getFightEndTime() {
		return fightEndTime;
	}
	
	
	/**
	 * 查询状态元素对象
	 * 
	 * @param  statusType				状态类型
	 * @return {@link StatusElement}	状态元素
	 */
	public StatusElement getStatusElement(StatusType statusType) {
		if(statusType == null) {
			return null;
		}
		
		StatusElement statusElement = STATUS_CACHES.get(statusType);
		if(statusElement == null){
			return null;
		}
		
		if(statusElement.isTimeOut()) {
			STATUS_CACHES.remove(statusType);
			return null;
		}
		return statusElement;
	}

	/**
	 * 把状态加入状态集合中
	 * 
	 * @param statusElement
	 */
	public void putStatusElement(StatusElement statusElement) {
		if(statusElement != null) {
			this.STATUS_CACHES.put(statusElement.getType(), statusElement);
		}
	}

	/**
	 * 需要移除的角色状态
	 * 
	 * @param statusTypes		角色的状态可变参
	 */
	public void removeStatusElement(StatusType...statusTypes) {
		for (StatusType statusType : statusTypes) {
			this.STATUS_CACHES.remove(statusType);
		}
	}
	
//	/** 移动需要判断的状态 */
//	private static StatusType[] MOVE_STATUS_CACHE = { StatusType.GIDD_STATUS,  	  StatusType.CHAOS_STATUS, 
//													  StatusType.SLEEP_STATUS, 	  StatusType.BLIND_STATUS, 
//													  StatusType.FIRST_JUMP_TYPE, StatusType.SECOND_JUMP_TYPE };
//	/** 战斗需要判断的状态 */
//	private static StatusType[] FIGHT_STATUS_CACHE = { StatusType.GIDD_STATUS,    StatusType.CHAOS_STATUS, 
//													   StatusType.SLEEP_STATUS,   StatusType.BLIND_STATUS };
//
//	/** 跳跃的状态验证 */
//	private static StatusType[] FIRST_JUMP_STATUS = { StatusType.GIDD_STATUS, StatusType.CHAOS_STATUS, 
//													  StatusType.SLEEP_STATUS,StatusType.BLIND_STATUS};
//
//	/** 攻击者战斗状态验证 */
//	public static final StatusType[] ATTACK_FIGHT_CACHE = { StatusType.GIDD_STATUS, StatusType.CHAOS_STATUS, 
//															StatusType.BLIND_STATUS, StatusType.SLEEP_STATUS};
//	/** 攻击者战斗状态验证(包含混乱) */
//	public static final StatusType[] ATTACK_CHAOS_CACHE = { StatusType.GIDD_STATUS, StatusType.CHAOS_STATUS, 
//															StatusType.BLIND_STATUS, StatusType.SLEEP_STATUS };
	/** 攻击必中的状态验证 */
	public static final StatusType[] JUMP_STATUS_CACHE = { StatusType.FIRST_JUMP_TYPE, StatusType.SECOND_JUMP_TYPE };
	
	/**
	 * 验证行为操作
	 * 
	 * @param statusTypes
	 * @return
	 */
	public boolean vilidateStatus(StatusType...statusTypes) {
		for (StatusType statusType : statusTypes) {
			StatusElement statusElement = this.getStatusElement(statusType);
			if(statusElement != null) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 角色是否正在跳跃中
	 * 
	 * @return {@link Boolean}	true-跳跃中, false-不在跳跃中
	 */
	public boolean isRoleJumping() {
		return !vilidateStatus(JUMP_STATUS_CACHE);
	}
}
