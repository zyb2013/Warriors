package com.yayo.warriors.module.trade.model;

import java.io.Serializable;

/**
 * 交易道具信息
 * @author huachaoping
 */
public class TradeProps implements Serializable {

	private static final long serialVersionUID = 8646786711817482554L;
	
	/** 
	 * 用户道具id
	 * 道具 - 对应userPropsId
	 * 装备 - 对应userEquipId
	 */
	private Long userPropId;
	
	/** 道具基础Id */
	private int baseId;
	
	/** 道具所在位置 */
	private int index = -1;
	
	/** 道具数量 */
	private int count;
	
	/**
	 * 用户道具类型 
	 * 0-道具, 1-装备
	 */
	private int goodType;
	
	/**
	 * 构造函数
	 * @param playerId
	 * @param baseId
	 * @param count
	 * @param index
	 * @return
	 */
	public static TradeProps valueOf(long userPropId, int baseId, int count, int goodType) {
		TradeProps tradeProps  = new TradeProps();
		tradeProps.userPropId  = userPropId;
		tradeProps.count       = count;
		tradeProps.baseId      = baseId;
		tradeProps.goodType    = goodType;
		return tradeProps;
	}
	
	public Long getUserPropId() {
		return userPropId;
	}
	
	public int getBaseId() {
		return baseId;
	}
	
	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public void setUserPropsId(Long userPropId) {
		this.userPropId = userPropId;
	}

	public int getGoodType() {
		return goodType;
	}

	public void setGoodType(int goodType) {
		this.goodType = goodType;
	}

	public void setUserPropId(Long userPropId) {
		this.userPropId = userPropId;
	}

	// 增加交易道具数量
	public void increaseCount(int count) {
		this.count += count;
	}
	
	// 减少交易道具数量
	public void decreaseCount(int count) {
		this.count -= count;
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result
				+ ((userPropId == null) ? 0 : userPropId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof TradeProps)) 
			return false;
		TradeProps other = (TradeProps) obj;
		if (index != other.index) return false;
		if (userPropId == null) {
			if (other.userPropId != null) 
				return false;
		} else if (!userPropId.equals(other.userPropId))
			return false;
		return true;
	}
}
