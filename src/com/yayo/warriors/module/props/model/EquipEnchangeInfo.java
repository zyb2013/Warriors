package com.yayo.warriors.module.props.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 装备镶嵌属性信息
 * 
 * @author Hyint
 */
public class EquipEnchangeInfo {

	/** 返回值 */
	private int result;
	
	/** 需要扣除的游戏币 */
	private int silver;
	
	/** 镶嵌需要扣除的用户道具信息 . {用户道具ID, 已扣除的道具数量 }*/
	private Map<Long, Integer> enchangeItems = new HashMap<Long, Integer>(0);
	
	/** 镶嵌的孔属性信息. { 下标, 道具ID } */
	private Map<Integer, Integer> enchangeHoles = new HashMap<Integer, Integer>(0);

	public int getResult() {
		return result;
	}

	public EquipEnchangeInfo updateResult(int result) {
		this.result = result;
		return this;
	}
	
	public void setResult(int result) {
		this.result = result;
	}

	public Map<Long, Integer> getEnchangeItems() {
		return enchangeItems;
	}

	public Map<Integer, Integer> getEnchangeHoles() {
		return enchangeHoles;
	}

	public int getSilver() {
		return silver;
	}

	public void addSilver(int addSilver) {
		this.silver += addSilver;
	}

	public void setSilver(int silver) {
		this.silver = silver;
	}

	public int getEnchangeItemCount(long userItemId) {
		Integer count = this.enchangeItems.get(userItemId);
		return count == null ? 0 : count;
	}

	public void addEnchangeItemCount(long userItemId, int addCount) {
		Integer count = this.enchangeItems.get(userItemId);
		count = count == null ? 0 : count;
		this.enchangeItems.put(userItemId, count + addCount);
	}
	
	public boolean hasHoleInfo(int index) {
		return enchangeHoles.containsKey(index);
	}
	
	public void addHoleInfo(int index, int itemId) {
		this.enchangeHoles.put(index, itemId);
	}
	
}
