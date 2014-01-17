package com.yayo.warriors.module.drop.model;

import com.yayo.warriors.type.GoodsType;

/**
 * 掉落对象
 * 
 * @author Hyint
 */
public class Drop {
	
	/** 奖励物品数量 */
	private int amount = 0;

	/** 
	 * 奖励物品序列号 
	 * 
	 * <pre>
	 * 装备奖励类型: 装备ID
	 * 道具奖励类型: 道具ID
	 * </pre>
	 */
	private int dropInfo = -1;
	
	/**
	 * 物品类别<br>
	 * 
	 * <pre>
	 *  0. 道具类型
	 *  1. 装备类型
	 *  2. 货币类型
	 * </pre>
	 * 
	 * @see ItemReward
	 */
	private int type = GoodsType.PROPS;

	/**
	 * 绑定状态
	 */
	private boolean binding;
	
	/**
	 * 消失时间. 从创建到消失的时间.(单位: 毫秒)
	 */
	private long endTime = 0;
	
	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	
	public int getDropInfo() {
		return dropInfo;
	}

	public void setDropInfo(int dropInfo) {
		this.dropInfo = dropInfo;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public boolean isBinding() {
		return binding;
	}

	public void setBinding(boolean binding) {
		this.binding = binding;
	}

	/**
	 * 构建掉落对象
	 * 
	 * @param type				掉落类型
	 * @param dropInfo			掉落个数
	 * @param amount			掉落数量
	 * @param dieoutTime		超时时间. 单位: 毫秒
	 * @return
	 */
	public static Drop valueOf(int type, int dropInfo, int amount, long dieoutTime) {
		Drop drop = new Drop();
		drop.type = type;
		drop.amount = amount;
		drop.dropInfo = dropInfo;
		drop.endTime = System.currentTimeMillis() + dieoutTime;
		return drop;
	}

	/**
	 * 构建掉落对象
	 * 
	 * @param type				掉落类型
	 * @param dropInfo			掉落个数
	 * @param dieoutTime		超时时间. 单位: 毫秒
	 * @return
	 */
	public static Drop valueOf(int type, int dropInfo, long dieoutTime) {
		Drop drop = new Drop();
		drop.type = type;
		drop.amount = 1;
		drop.dropInfo = dropInfo;
		drop.endTime = System.currentTimeMillis() + dieoutTime;
		return drop;
	}

	/**
	 * 构建掉落对象
	 * 
	 * @param type				掉落类型
	 * @param dropInfo			掉落个数
	 * @param dieoutTime		超时时间. 单位: 毫秒
	 * @return
	 */
	public static Drop valueOf(int type, int dropInfo, long dieoutTime, boolean binding) {
		Drop drop = new Drop();
		drop.type = type;
		drop.amount = 1;
		drop.binding = binding;
		drop.dropInfo = dropInfo;
		drop.endTime = System.currentTimeMillis() + dieoutTime;
		return drop;
	}

	@Override
	public String toString() {
		return "Drop [amount=" + amount + ", dropInfo=" + dropInfo + ", type=" + type
				+ ", binding=" + binding + ", endTime=" + endTime + "]";
	}
}
