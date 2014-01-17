package com.yayo.warriors.module.monster.model;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.module.user.model.StatusElement;
import com.yayo.warriors.module.user.type.StatusType;

/**
 * 怪物状态
 * @author liuyuhua
 */
public class MonsterStatus {

	/** 怪物主键*/
	private Long monsterId;
	
	/** 角色的状态缓存 */
	private ConcurrentHashMap<StatusType, StatusElement> STATUS_CACHES = new ConcurrentHashMap<StatusType, StatusElement>(5);
	
	/**
	 * 构造函数
	 * @param monsterId   怪物的ID
	 * @return
	 */
	public static MonsterStatus valueOf(long monsterId){
		MonsterStatus monsterStatus = new MonsterStatus();
		monsterStatus.monsterId = monsterId;
		return monsterStatus;
	}


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
	 * 查询状态元素对象
	 * 
	 * @param statusType
	 * @return
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
	 * @param statusTypes		角色的状态可变参
	 */
	public void removeStatusElement(StatusType...statusTypes) {
		for (StatusType statusType : statusTypes) {
			this.STATUS_CACHES.remove(statusType);
		}
	}
	
	/**
	 * 删除过期状态
	 */
	public void removeTimeOutStatus(){
		if(this.STATUS_CACHES.isEmpty()){
			return;
		}
		
		/**
		 * 迭代删除,怪物身上的状态
		 */
		Iterator<Entry<StatusType, StatusElement>> it = this.STATUS_CACHES.entrySet().iterator();
		while(it.hasNext()){
			Entry<StatusType, StatusElement> value = it.next();
			StatusElement element = value.getValue();
			if(DateUtil.getCurrentSecond() >= element.getEndTime()){
				it.remove();
			}
		}
	}
	
	public Long getMonsterId() {
		return monsterId;
	}

	public void setMonsterId(Long monsterId) {
		this.monsterId = monsterId;
	}

}
