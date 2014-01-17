package com.yayo.warriors.module.props.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 合成宝石适配器
 * 
 * @author Hyint
 */
public class SynthResult {
	
	/** 合成成功的数量 */
	private int successCount;
	
	/** 合成总数量 */
	private int synchTotalCount;
	
	/** 新创建的绑定的用户道具数量 */
	private int newBindingCount = 0;
	
//	/** 新创建的未绑定的用户道具数量 */
//	private int newUnBindingCount = 0;
	
	/** 扣除的用户道具信息 */
	private Map<Long, Integer> costUserItems = new HashMap<Long, Integer>(0);

	public int getNewBindingCount() {
		return newBindingCount;
	}

	public void addNewBindingCount(int addBindingCount) {
		this.newBindingCount += addBindingCount;
	}

//	public int getNewUnBindingCount() {
//		return newUnBindingCount;
//	}
//
//	public void addNewUnBindingCount(int addUnBindCount) {
//		this.newUnBindingCount += addUnBindCount;
//	}

	public Map<Long, Integer> getCostUserItems() {
		return costUserItems;
	}

	public int getSuccessCount() {
		return successCount;
	}

	public int getFailureCount() {
		return Math.max(0, this.synchTotalCount - this.successCount);
	}

	public void addSuccessCount(int successCount) {
		this.successCount += successCount;
	}

	public int getSynchTotalCount() {
		return synchTotalCount;
	}

	public void setSynchTotalCount(int synchTotalCount) {
		this.synchTotalCount = synchTotalCount;
	}

	public void setCostUserItems(Map<Long, Integer> costUserItems) {
		this.costUserItems = costUserItems;
	}

	public int getCostUserItemCount(long userItemId) {
		Integer count = this.costUserItems.get(userItemId);
		return count == null ? 0 : count;
	}
	
	public void addCostUserItemCount(long userItemId, int addCount) {
		Integer count = this.costUserItems.get(userItemId);
		count = count == null ? 0 : count;
		this.costUserItems.put(userItemId, count + addCount);
	}
	
	
}
