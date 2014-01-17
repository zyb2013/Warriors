package com.yayo.warriors.module.task.model;

import java.util.HashMap;
import java.util.Map;

public class FightCollectInfo {
	
	private Map<Integer, Integer> totalItems = new HashMap<Integer, Integer>(0);
	
	private Map<Integer, Integer> collectItems = new HashMap<Integer, Integer>(0);
	
	public Map<Integer, Integer> getTotalCollectCounts() {
		return this.totalItems;
	}

	public int getTotalCount(int itemId) {
		Integer count = totalItems.get(itemId);
		return count == null ? 0 : count;
	}
	
	public void addTotalCount(int itemId, int addCount) {
		Integer count = totalItems.get(itemId);
		count = count == null ? 0 : count;
		totalItems.put(itemId, addCount + count);
	}
	
	public int getCollectCount(int itemId) {
		Integer count = collectItems.get(itemId);
		return count == null ? 0 : count;
	}
	
	public void addCollectCount(int itemId, int addCount) {
		Integer count = collectItems.get(itemId);
		count = count == null ? 0 : count;
		collectItems.put(itemId, count + addCount);
	}

	public Map<Integer, Integer> getTotalItems() {
		return totalItems;
	}

	public Map<Integer, Integer> getCollectItems() {
		return collectItems;
	}
}
