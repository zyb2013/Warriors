package com.yayo.warriors.type;

/**
 * 游戏的行为类型
 * 
 * <pre>
 * 0:玩家
 * 1:宠物
 * 2:NPC
 * 3:群攻
 * </pre>
 */
public enum ElementType {
	
	/** 0 - 玩家. */
	PLAYER(true), 
	
	/** 1 - 召唤兽 */
	PET(false),
	
	/** 2 - 怪物/敌人 */
	MONSTER(true),
	
	/** 3 - NPC */
	NPC(false),
	
	/** 4 - NPC 采集物*/
	NPC_GATHER(false),
	
	/** 5 - NPC 传送点*/
	NPC_TRANSMIT(false),
	
	/** 6 - SCENERY 风景*/
	SCENERY(false), 
	
	/** 7 - 掉落奖励 */
	DROP_REWARD(false);
	
	/** 是否可以附加BUFF */
	private boolean canAddBuffer = false;
	
	ElementType(boolean addBuffer) {
		this.canAddBuffer = addBuffer;
	}

	public boolean isCanAddBuffer() {
		return canAddBuffer;
	}

	public void setCanAddBuffer(boolean canAddBuffer) {
		this.canAddBuffer = canAddBuffer;
	}
}
