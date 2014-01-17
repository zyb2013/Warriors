package com.yayo.warriors.module.treasure.rule;

/**
 * 藏宝图规则类
 * @author jonsai
 *
 */
public abstract class TreasureRule {
	
	/** 藏宝图几点过期 */
	public static final int TREASURE_TIMEOUT_HOUR = 0;
	
	/** 藏宝图大于等于此等级 */
	public static final int TREASURE_MAP_LEVEL_LIMIT = 24;
	
	/** 金铲子id */
	public static final int GOLDEN_DIG_PROPS = 120035;
}
