package com.yayo.warriors.module.battlefield.vo;

import java.io.Serializable;

import javax.persistence.Transient;

import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.task.type.TaskStatus;

/**
 * 战场采集任务VO
 * @author jonsai
 *
 */
public class CollectTaskVO implements Serializable {
	private static final long serialVersionUID = -4319614929462341475L;
	
	/** 任务的状态 {@link TaskStatus}	 */
	private int status = TaskStatus.UNACCEPT;
	
	/** 采集物id(寻路用) */
	private int npcId;
	
	/** 采集物 */
	private int baseId;
	
	/** 任务的数量 */
	private int amount;

	/** 任务的总数量 */
	private int totalAmount;

	//---------------------------------------------
	/**
	 * 任务是否完成
	 * @return
	 */
	public boolean isCompleted(UserProps userProps){
		return status == TaskStatus.ACCEPTED && userProps.getCount() >= totalAmount;
	}
	
	public void reset(){
		this.amount = 0;
	}
	
	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getTotalAmount() {
		return totalAmount;
	}
	
	public void increaseAmount(int amount) {
		this.amount += amount;
	}

	public void setTotalAmount(int totalAmount) {
		this.totalAmount = totalAmount;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public int getNpcId() {
		return npcId;
	}

	public void setNpcId(int npcId) {
		this.npcId = npcId;
	}
	
}
