package com.yayo.warriors.module.fight.model;

import com.yayo.warriors.module.user.model.StatusElement;
import com.yayo.warriors.module.user.type.StatusType;

/**
 * 状态变化
 * 
 * @author Hyint
 */
public class ChangeStatus {

	/** 是否增加状态. true-增加状态, false-移除状态 */
	private boolean add;
	
	/** 新增或者移除的状态类型 */
	private StatusType statusType;

	/** 移除的状态 */
	private StatusElement statusElement;
	
	public boolean isAdd() {
		return add;
	}

	public void setAdd(boolean add) {
		this.add = add;
	}

	public StatusElement getStatusElement() {
		return statusElement;
	}

	public StatusType getStatusType() {
		return statusType;
	}

	public void setStatusType(StatusType statusType) {
		this.statusType = statusType;
	}
	
	public void setStatusElement(StatusElement statusElement) {
		this.statusElement = statusElement;
	}

	/**
	 * 新增状态
	 * 
	 * @param  statusElement		新增状态
	 * @return {@link ChangeStatus}	状态改变对象
	 */
	public static ChangeStatus add(StatusElement statusElement) {
		ChangeStatus status = new ChangeStatus();
		status.add = true;
		status.statusElement = statusElement;
		status.statusType = statusElement.getType();
		return status;
	}

	/**
	 * 移除状态
	 * 
	 * @param  statusType			状态类型
	 * @return {@link ChangeStatus}	状态改变对象
	 */
	public static ChangeStatus remove(StatusType statusType) {
		ChangeStatus status = new ChangeStatus();
		status.add = false;
		status.statusType = statusType;
		return status;
	}
}
