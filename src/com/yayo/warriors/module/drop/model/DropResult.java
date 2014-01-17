package com.yayo.warriors.module.drop.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 构建掉落奖励结果
 * 
 * @author Hyint
 */
public class DropResult {
	
	/**
	 * 奖励ID
	 */
	private int rewardId;
	
	/**
	 * 是否要公告
	 */
	private boolean notice;
	
	/**
	 * 掉落个数
	 */
	private int amount;
	
	/**
	 * 掉落信息列表
	 */
	private List<Drop> drops = new ArrayList<Drop>();

	
	public int getRewardId() {
		return rewardId;
	}

	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}

	public boolean isNotice() {
		return notice;
	}

	public void setNotice(boolean notice) {
		this.notice = notice;
	}

	public List<Drop> getDrops() {
		return drops;
	}

	public void setDrops(List<Drop> drops) {
		this.drops = drops;
	}

	public void addDrops(Drop drop) {
		if(drop != null) {
			this.drops.add(drop);
		}
	}
	/**
	 * 构建掉落结果对象
	 * 
	 * @param  rewardId				奖励ID
	 * @param  noticeId				公告ID
	 * @param  amount				奖励数量
	 * @return {@link DropResult}	掉落返回值
	 */
	public static DropResult valueOf(int rewardId, boolean notice, int amount) {
		DropResult dropResult = new DropResult();
		dropResult.amount = amount;
		dropResult.rewardId = rewardId;
		dropResult.notice = notice;
		return dropResult;
	}
	
	@Override
	public String toString() {
		return "DropResult [rewardId=" + rewardId + ", notice=" + notice + ", amount=" + amount + ", drops=" + drops + "]";
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
}
