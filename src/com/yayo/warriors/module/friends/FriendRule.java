package com.yayo.warriors.module.friends;

/**
 * 好友规则
 * 
 * @author huachaoping
 */
public class FriendRule {
	
	/** 添加上限 */
	public static final int LIMIT = 200;
	
	/** 征友数量 */
	public static final int COUNT = 12;

	/** 好友祝福所得经验 */
	public static final int BLESS_EXP = 1000;
	
	/** 领取祝福瓶经验等级 */
	public static final int LEVEL_LIMIT = 36;
	
	/** 敬酒限制 */
	public static final int GREET_LIMIT = 37;
	
	/** 酒瓶容量限制 */
	public static final int WINE_LIMIT = 1000;
	
	/** 历史记录数 */
	public static final int HISTORY_RECORD = 20;
	
	/** 祝福经验上限 */
	public static final int BLESS_EXP_LIMIT = 500000;
	
	
	/** 
	 * 祝福等级限制 
	 * @param playerLevel  玩家等级
	 * @return {@link Boolean}
	 */
	public static boolean blessLevelLimit(int playerLevel) {
		return playerLevel < 12 || playerLevel > 36;
	}
	
	
	/**
	 * 获得下一个征友的等级
	 * 
	 * @param level    玩家等级
	 * @return
	 */
	public static int alterParam(int level) {
		switch(level) {
			case 0 :   return 12;
			case 12:   return 20;
			case 20:   return 25;
			case 25:   return 30;
		}
		return 1000;               
	}
	
}
