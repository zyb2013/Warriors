package com.yayo.warriors.module.market.rule;

/**
 * 摆摊规则类
 * @author huachaoping
 */
public class MarketRule {
	
	/** 摆摊物品上限 */
	public static final int MARKET_MAX = 20;
	
	/** 搜索关键字缓存时间*/
	public static final long SEARCH_CACHE = 60000;
	
	/** HashKey */
	public static final String HASH_KEY = "MARKET_";

	/** 玩家名字的SubKey前缀 */
	public static final String PLAYER_NAME = "PLAYERNAME_";
	
	/** 物品名称的subKey前缀 */
	public static final String PROPS_NAME = "PROPSNAME_";
	
}
