package com.yayo.warriors.module.meridian.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yayo.warriors.module.logger.model.LoggerGoods;

/**
 * 经脉道具检查信息
 * 
 * @author Hyint
 */
public class MeridianItemCheck {
	
	/** 基础道具ID */
	private int baseId;
	
	/** 道具总数 */
	private int totalCount;
	
	/** 日志记录专用 */
	private List<LoggerGoods> logGoods = new ArrayList<LoggerGoods>(0);
	
	/** 用户道具信息. { 用户道具ID, 用户道具数量 } */
	private Map<Long, Integer> userItems = new HashMap<Long, Integer>(0);
	
	
	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}
	
	public int getTotalCount() {
		return totalCount;
	}

	public void addTotalCount(int totalCount) {
		this.totalCount += totalCount;
	}

	public Map<Long, Integer> getUserItems() {
		return userItems;
	}

	public int getItemCount(long userItemId) {
		Integer count = this.userItems.get(userItemId);
		return count == null ? 0 : count;
	}

	public void addItemCount(long userItemId, int count) {
		Integer cacheCount = this.userItems.get(userItemId);
		cacheCount =  cacheCount == null ? 0 : cacheCount;
		this.userItems.put(userItemId, cacheCount + count);
		this.addTotalCount(count);
	}

	public List<LoggerGoods> getLogGoods() {
		return logGoods;
	}
	
	public void addLogGoods(long goodsId, int baseId, int count) {
		logGoods.add(LoggerGoods.outcomeProps(goodsId, baseId, count));
	}

	public void addLogGoodsAutoBuy(int baseId, int count, long golden, long silver) {
		logGoods.add(LoggerGoods.outcomePropsAutoBuyMoney(baseId, count, golden, silver));
	}

}
