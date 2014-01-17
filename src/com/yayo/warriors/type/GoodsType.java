package com.yayo.warriors.type;

/**
 * 物品的类型(主要是背包中的物品的分类)
 * 
 * @author Hyint
 */
public interface GoodsType {
	
	/** -1 - 不存在 */
	int NONE = -1;
	
	/** 0 - 道具类型 */
	int PROPS = 0;
	
	/** 1 - 装备类型 */
	int EQUIP = 1;
	
	/** 2 - 游戏币类型 */
	int SILVER = 2;

	/** 3 - 元宝类型 */
	int GOLDEN = 3;
	
	/** 4 - 绑定元宝类型 */
	int COUPON = 4;

	int[] CURRENCYS = { SILVER, GOLDEN };
}
