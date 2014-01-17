package com.yayo.warriors.module.chat.type;

public interface GmType {

	/** GM 命令的起始符号 */
	String GM_START = "gm";
	
	/** 增加HP */
	String HP = "hp";

	/** 增加MP */
	String MP = "mp";

	/** 增加Gas */
	String GAS = "gas";
	
	/** 增加角色经验*/
	String EXP = "exp";

	/** 增加道具 */
	String ITEM = "item";

	/** 增加装备 */
	String EQUIP = "equip";

	/** 增加角色等级 */
	String LEVEL = "level";

	/** 增加技能 */
	String SKILL = "skill";

	/** 增加游戏币 */
	String SILVER = "silver";

	/** 增加金币 */
	String GOLDEN = "golden";
	
	/** 清除背包 */
	String CLEAR_BAG = "clearBag";
	
	/** 用户任务 */
	String USER_TASK = "userTask";
	
	/** 角色跳转到...*/
	String GOTO = "goto";
	
	/** 停服命令...*/
	String SHUT_DOWN = "shutdown";

	/** 家将经验值*/
	String PET_EXP = "petExp";
	
	/** 经脉等级 */
	String MERIDIAN = "meridian";

	/** 家将契合度等级 */
	String MERGED_LEVEL = "mergedLevel";
	
	/** 调用管理后台接口...*/
	String ADMIN = "admin";
	
	/** 刷怪指令 */
	String MONSTER = "monster";
	
	/** 增加帮派资金*/
	String ALLIANCE_SILVER = "allianceSilver";
	
	/** 增加帮派令牌数量*/
	String ALLIANCE_TOKEN = "allianceToken";
	
	/** 增加玩家帮派贡献值*/
	String PLAYER_DONATE = "donate";
	
	/** 好友酒坛 */
	String WINE_JAR = "winejar";
	
	/** 角色的礼金 */
	String COUPON = "coupon";
	
	/** 杀怪数量(成就) */
	String KILL_MONSTER = "monsterCount";
	
	/** 累计登录天数 */
	String LOGIN_DAYS = "loginDays";
	
	/** 连续登录天数 */
	String CONTINUE_DAYS = "continueDays";
	
	/** 角色新手引导 */
	String GUIDE = "guide";
	
	/** 在线时间 */
	String LOGIN_TIME = "onlineTime";
	
	/** 刷新排行榜 */
	String RANK = "rank";
	
	/** 座骑等级 */
	String HORSE = "horse";
	
	/** 好友祝福 */
	String BLESS = "bless";
}
