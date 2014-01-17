package com.yayo.warriors.module.props.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 合成对象
 * 
 * @author Hyint
 */
public class SynthObject {
	
	/** 返回值 */
	private int result = 0;
	
	/** 基础道具ID */
	private int baseItemId = -1;
	
	/** 扣除绑定的数量 */
	private int bindingCount;
	
//	/** 扣除未绑定的数量 */
//	private int unBindingCount;
	
	/** 绑定的用户道具 */
	private Map<Long, Integer> bindingItems = new HashMap<Long, Integer>(0);
	
//	/** 未绑定的用户道具信息 */
//	private Map<Long, Integer> unbindingItems = new HashMap<Long, Integer>(0);
	
	public Map<Long, Integer> getBindingItems() {
		return bindingItems;
	}

//	public Map<Long, Integer> getUnbindingItems() {
//		return unbindingItems;
//	}

	/**
	 * 增加绑定的用户数量
	 * 
	 * @param userItemId	用户道具ID
	 * @param addCount		增加的用户道具数量
	 */
	public void addBindingCount(long userItemId, int addCount) {
		addBindingCount(addCount);
		int cacheCount = getBindingMapCount(userItemId);
		bindingItems.put(userItemId, cacheCount + addCount);
	}
	
	
	public int getBindingMapCount(long userItemId){ 
		Integer cacheCount = bindingItems.get(userItemId);
		return cacheCount == null ? 0 : cacheCount;
	}

//	/**
//	 * 增加未绑定的用户数量
//	 * 
//	 * @param userItemId	用户道具ID
//	 * @param addCount		增加的用户道具数量
//	 */
//	public void addUnBindingCount(long userItemId, int addCount) {
//		addUnBindingCount(addCount);
//		Integer cacheCount = unbindingItems.get(userItemId);
//		cacheCount = cacheCount == null ? 0 : cacheCount;
//		unbindingItems.put(userItemId, cacheCount + addCount);
//	}
//	
	public int getTotalCount() {
		return bindingCount/* + unBindingCount*/;
	}

	public int getBaseItemId() {
		return baseItemId;
	}

	public void setBaseItemId(int baseItemId) {
		this.baseItemId = baseItemId;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public int getBindingCount() {
		return bindingCount;
	}

	private void addBindingCount(int bindingCount) {
		this.bindingCount += bindingCount;
	}

//	public int getUnBindingCount() {
//		return unBindingCount;
//	}
//
//	private void addUnBindingCount(int unBindingCount) {
//		this.unBindingCount += unBindingCount;
//	}

	@Override
	public String toString() {
		return "SynthObject [result=" + result + ", baseItemId=" + baseItemId + ", bindingCount="
				+ bindingCount /*+ ", unBindingCount=" + unBindingCount*/ + ", bindingItems="
				+ bindingItems + /*", unbindingItems=" + unbindingItems +*/ "]";
	}
	
	public SynthObject updateResult(int result) {
		this.result = result;
		return this;
	}
}
