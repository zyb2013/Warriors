package com.yayo.warriors.module.task.model;

import com.yayo.warriors.type.GoodsType;

public class RewardVO {
	
	private int type;
	
	private int baseId;
	
	private int count;
	
	private int starLevel;

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

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getStarLevel() {
		return starLevel;
	}

	public void setStarLevel(int starLevel) {
		this.starLevel = starLevel;
	}

	public boolean isBinding() {
		return binding;
	}

	public void setBinding(boolean binding) {
		this.binding = binding;
	}

	public static RewardVO props(int baseId, int count, boolean binding) {
		RewardVO rewardVO = new RewardVO();
		rewardVO.count = count;
		rewardVO.baseId = baseId;
		rewardVO.binding = binding;
		rewardVO.type = GoodsType.PROPS;
		return rewardVO;
	}

	public static RewardVO equip(int baseId, int count, int star, boolean binding) {
		RewardVO rewardVO = new RewardVO();
		rewardVO.type = GoodsType.EQUIP;
		rewardVO.count = count;
		rewardVO.baseId = baseId;
		rewardVO.binding = binding;
		rewardVO.starLevel = star;
		return rewardVO;
	}

	@Override
	public String toString() {
		return "RewardVO [type=" + type + ", baseId=" + baseId + ", count=" + count + "]";
	}
}
