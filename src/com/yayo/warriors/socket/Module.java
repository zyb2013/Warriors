package com.yayo.warriors.socket;



/**
 * 模块列表
 * 
 * @author hyint
 */
public interface Module {
	
	/** 管理后台 */
	int ADMIN = 10000;
	
	/** 聊天 */
	int CHAT = 1;
	
	/** 场景频道 */
	int CHANNEL = 2;
	
	/**用户 角色*/
	int USER = 3;
	
	/**游戏 地图*/
	int MAP = 4;
	
	/**任务*/
	int TASK = 5;
	
	/** 道具模块 */
	int PROPS = 6;
	
	/** 技能 */
	int SKILL = 7;
	
	/** 战斗 */
	int FIGHT = 8;
	
	/** 商店模块 */
	int SHOP = 9;
	
	/** 好友*/
	int FRIENDS = 10;
	
	/** 组队模块 */
	int TEAM = 11;

// TODO 删除BUFF模块	
//	/** BUFF 模块 */
//	int BUFFER = 12;
	
	/** 帮派(公会)*/
	int ALLIANCE = 13;
	
	/** 经脉*/
	int MERIDIAN = 14;
	
	/** 掉落模块 */
	int LOOT = 15;
	
	/** 挂机模块*/
	int TRAIN = 16;
	
	/** 小飞鞋*/
	int FLYSHOES = 17;
	
	/** 阵营*/
	int CAMP = 18;
	
	/** 副本(地下城)*/
	int DUNGEON = 19;
	
	/** 家将(宠物)*/
	int PET = 20;
	
	/** 肉身 */
	int MORTAL = 21;
	
	/** 坐骑*/
	int HORSE  = 22;
	
	/** 交易 */
	int TRADE = 23;
	
	/** 摆摊 */
	int MARKET = 24;
	
	/** 称号系统*/
	int TITLE = 25 ;
	
	/** 副本任务*/
	int DUNGEONTASK = 26;
	
	/** 游戏公告*/
	int NOTICE = 27 ;
	
	/** 排行榜 */
	int RANK = 28 ;
	
	/** 抽奖*/
	int LOTTERY = 29;
	
	/** VIP */
	int VIP = 30;
	
	/** 礼包 */
	int GIFT = 31;
	
	/** 藏宝图 */
	int TREASURE = 32;
	
	/** 活动 */
	int ACTIVE = 33;
	
	/** 阵营战 */
	int CAMP_BATTLE = 34;
	
	/** 邮件 */
	int MAIL = 35;
	
	/** 乱武战场	 */
	int BATTLE_FIELD = 36;
	
	/** 成就 */
	int ACHIEVE = 37;
	
	/** 充值礼包 */
	int RECHARGE_GIFT = 38;
}
