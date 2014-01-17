package com.yayo.warriors.module.drop.model;

import com.yayo.warriors.type.GoodsType;

/**
 * 掉落奖励对象
 * 
 * @author Hyint
 */
public class DropRewards {
	
	/** 物品类型, 详细见: {@link GoodsType} */
	private int type;
	
	/** 物品的基础ID */
	private int baseId;
	
	/** 数量 */
	private int amount;
	
	/** 结束时间 */
	private long endTime;

	/** 是否推送公告 */
	private boolean notice;

	/** 绑定状态 */
	private boolean binding;
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public boolean isNotice() {
		return notice;
	}

	public void setNotice(boolean notice) {
		this.notice = notice;
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

	private DropRewards (){}
	
	/**
	 * 掉落奖励对象
	 * 
	 * @param  drop					掉落对象
	 * @param  notice				是否发送公告
	 * @return {@link DropRewards}	掉落奖励对象
	 */
	public static DropRewards valueOf(Drop drop, boolean notice) {
		DropRewards reward = new DropRewards();
		reward.notice = notice;
		reward.type = drop.getType();
		reward.amount = drop.getAmount();
		reward.binding = drop.isBinding();
		reward.baseId = drop.getDropInfo();
		reward.endTime = drop.getEndTime();
		return reward;
	}

	@Override
	public String toString() {
		return "DropRewards [type=" + type + ", baseId=" + baseId + ", amount=" + amount
				+ ", endTime=" + endTime + ", notice=" + notice + ", binding=" + binding + "]";
	}
 
	
}
