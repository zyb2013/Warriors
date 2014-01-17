package com.yayo.warriors.module.pack.type;

/**
 * 背包类型定义
 * 
 * @author Hyint
 */
public interface BackpackType {
	
	/** 已出售背包 */
	int SOLD_BACKPACK = -2;
	
	/** 已丢弃背包 */
	int DROP_BACKPACK = -1;
	
	/** 默认背包 */
	int DEFAULT_BACKPACK = 0;
	
	/** 仓库背包 */
	int STORAGE_BACKPACK = 1;
	
	/** 已穿着的装备背包 */
	int DRESSED_BACKPACK = 2;
	
	/** 摆摊背包*/
	int MARKET_BACKPACK  = 3;
	
	/** 抽奖背包*/
	int LOTTERY_BACKPACK  = 4;
	
	/** 用户快捷栏位 */
	int USER_PANEL_BACKPACK = 10;

	/** 用户挂机设置 */
	int USER_HOOK_BACKPACK = 11;
	
	/** 用户系统设置 */
	int USER_SYSTEM_BACKPACK = 12;
	
	/** 用户组队设置 */
	int USER_TEAM_BACKPACK = 13;
	
	/** 需要刷新角色属性的背包号 */
	final int[] FLUSHABLE_BACKPACK = { DRESSED_BACKPACK };
	/** 可以在任何场合中使用的道具背包号 */
	final int[] CANUSE_BACKPACKS = { DEFAULT_BACKPACK, STORAGE_BACKPACK };
	/** 可以执行星级继承的背包*/
	final int[] CAN_EXTENDS_BACKPACK = { DEFAULT_BACKPACK, DRESSED_BACKPACK };
	/** 可以执行打造的背包*/
	final int[] CAN_FORGE_BACKPACK = { DEFAULT_BACKPACK, DRESSED_BACKPACK };
	/** 可以更新背包位置信息的背包号 */
	final int[] SAVE_POSITION_BACKPACKS = { DEFAULT_BACKPACK, STORAGE_BACKPACK };
	/** 拆分物品的背包号数组 */
	final int[] SPLIT_PROPS_BACKPACKS = { DEFAULT_BACKPACK, STORAGE_BACKPACK };
	/** 合并物品的背包号数组 */
	final int[] MERGE_PROPS_BACKPACKS = { DEFAULT_BACKPACK, STORAGE_BACKPACK };
	/** 整理背包的背包号数组 */
	final int[] CAN_SETTLE_BACKPACKS = { DEFAULT_BACKPACK, STORAGE_BACKPACK ,LOTTERY_BACKPACK};
	/** 交换背包号, 或者移动到新的背包号中*/
	final int[] SWAP_BACKPACK_BACKPACKS = { DEFAULT_BACKPACK, STORAGE_BACKPACK, LOTTERY_BACKPACK };
	/** 可以执行出售的背包 */
	final int[] CAN_SOLD_BACKPACKS = { DEFAULT_BACKPACK, STORAGE_BACKPACK };
	/** 可以丢弃物品的背包信息 */
	final int[] CAN_DROP_PACKAGES = { DEFAULT_BACKPACK, STORAGE_BACKPACK,LOTTERY_BACKPACK };
	/** 可以保存的背包位置信息 */
	final int[] CAN_SAVE_PACKAGES = { USER_PANEL_BACKPACK, USER_HOOK_BACKPACK, USER_SYSTEM_BACKPACK, USER_TEAM_BACKPACK };
	/** 管理后台可以增加的背包号 */
	final int[] ADMIN_ADD_BACKPACKS = { DEFAULT_BACKPACK, STORAGE_BACKPACK };
	/** 可以维修的装备背包号 */
	final int[] REPAIR_BACKPACKS = { DEFAULT_BACKPACK, DRESSED_BACKPACK };
	/** 需要验证数量的背包号 */
	final int[] VALID_COUNT_BACKPACKS = { DEFAULT_BACKPACK, STORAGE_BACKPACK, DRESSED_BACKPACK, LOTTERY_BACKPACK };
	/** 总共可以查询的背包号*/
	final int[] QUERY_BACKPACKS = { DEFAULT_BACKPACK, STORAGE_BACKPACK, DRESSED_BACKPACK, DROP_BACKPACK, SOLD_BACKPACK, LOTTERY_BACKPACK, MARKET_BACKPACK };
	/** 所有的背包信息 */
	final int[] TOTAL_BACKPACKS ={ SOLD_BACKPACK, 	DROP_BACKPACK, DEFAULT_BACKPACK, STORAGE_BACKPACK, DRESSED_BACKPACK, 
								   MARKET_BACKPACK, LOTTERY_BACKPACK };
}

