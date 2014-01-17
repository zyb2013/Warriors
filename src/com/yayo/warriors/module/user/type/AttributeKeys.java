package com.yayo.warriors.module.user.type;

import java.util.Date;

/**
 * 角色的属性定义对象
 * 
 * @author Hyint
 */
public interface AttributeKeys {
	
	/** 属性倍率 */
	int RATE_BASE = 1000;
	
	//-------------------------------------------------------------------
	//--------------------------一级属性-----------------------------------
	/** 力量 */
	int STRENGTH = 1000;

	/** 敏捷 */
	int DEXERITY = 1001;

	/** 智力 */
	int INTELLECT = 1002;

	/** 体力 */
	int CONSTITUTION = 1003;

	/** 精神 */
	int SPIRITUALITY = 1004;
	
	//--------------------------二级属性-----------------------------------------------------
	/** 命中率 */
	int HIT = 1100;
	
	/** 闪避值.*/
	int DODGE = 1101;
	
	/** 移动速度 */
	int MOVE_SPEED = 1102;
	
	/** 法术攻击 */
	int THEURGY_ATTACK = 1103;
	
	/** 法术防御 */
	int THEURGY_DEFENSE = 1104;
	
	/** 法术暴击 */
	int THEURGY_CRITICAL = 1105;
	
	/** 物理攻击 */
	int PHYSICAL_ATTACK = 1106;
	
	/** 物理防御 */
	int PHYSICAL_DEFENSE = 1107;
	
	/** 物理暴击 */
	int PHYSICAL_CRITICAL = 1108;

	/** 眩晕抗性效果 */
	int IMMOBILIZE_DEFENSE = 1109;
	
	//-------------------特殊装备附加属性-------------------
	/** 提升物理攻击百分比 */
	int PHYSICAL_ATTACK_RATIO = 1110;

	/** 提升法术攻击百分比 */
	int THEURGY_ATTACK_RATIO = 1111;
	
	/** 提升物理防御百分比 */
	int PHYSICAL_DEFENSE_RATIO = 1112;

	/** 提升法术防御百分比 */
	int THEURGY_DEFENSE_RATIO = 1113;
	
	/** 提升HPMax百分比 */
	int HPMAX_RATIO = 1114;
	
	/** 提升MPMax百分比 */
	int MPMAX_RATIO = 1115;
	
	/** 提升物理暴击百分比 */
	int PHYSICAL_CRITICAL_RATIO = 1116;

	/** 提升法术暴击百分比 */
	int THEURGY_CRITICAL_RATIO = 1117;
	
	/** 提升自身命中值百分比 */ 
	int HIT_RATIO = 1118;
	
	/** 提升自身闪避值百分比 */
	int DODGE_RATIO = 1119;

	// --------------------------- 三级属性 --------------------------------------
	/** 穿透 */
	int PIERCE = 1200;

	/** 格挡 */
	int BLOCK = 1201;

	/** 急速 */
	int RAPIDLY = 1202;

	/** 坚韧 */
	int DUCTILITY = 1203;
	
	// --------------------------- 角色属性---------------------------------------
	/** HP */
	int HP = 1300;
	
	/** MP */
	int MP = 1301;
	
	/** 真气 . */
	int GAS = 1302;

	/** HP最大值 */
	int HP_MAX = 1303;
	
	/** MP_MAX */
	int MP_MAX = 1304;
	
	/** 真气 最大值. */
	int GAS_MAX = 1305;

	/** 角色的当前经验. */
	int EXP = 1306;

	/** 升级到下一级的经验. */
	int EXP_MAX = 1307;

	/** 等级. */
	int LEVEL = 1308;

	/** HP 便携包. */
	int HP_BAG = 1309;

	/** MP 便携包. */
	int MP_BAG = 1310;

	/** 战斗模式. 详细见: {@link FightMode} */
	int FIGHT_MODE = 1311;

	/** 银币. */
	int SILVER = 1312;

	/** 礼券. */
	int COUPON = 1313;
	
	/** 金币. */
	int GOLDEN = 1314;
	
	/** 称号 */
	int TITLE = 1315;
	
	/** 头像 */
	int ICON = 1316;
	
	/** 性别 */
	int SEX = 1317;
	
	/** 名字 */
	int NAME = 1318;
	
	/** 职业 */
	int JOB = 1319;
	
	/**阵营(刀阵营/剑阵营)*/
	int CAMP = 1320;
	
	/** 家将HP便携包 */
	int PET_HP_BAG = 1321;

	/** 战斗经验加成 */
	int FIGHT_EXP_RATE = 1322;

	/** 打坐经验加成 */
	int TRAIN_EXP_RATE = 1323;

	/** 打坐真气加成 */
	int TRAIN_GAS_RATE = 1324;
	
	/** 角色领取特殊等级物品奖励信息. 格式: [领取信息1,领取信息2] (已领取才记录下来, 未领取则不记录)*/
	int PLAYER_RECEIVE_INFO = 1325;
	
	/** 角色的级别. */
	int CAPACITY = 1326;
	
	/** 角色的防沉迷状态, -1未知, 0未成年, 1你懂的 */
	int INDULGE_STATE = 1327;
	
	/** 阵营官衔 */
	int CAMP_TITLE = 1328;
	
	// --------------------------- 一些特殊的属性  ------------------------
	/** 服务器ID标识 */
	int SERVER_ID = 2000;
	
	/** 背包最大格子数 */
	int BACKPACK_SIZE = 2001;
	
	/** 创建时间 */
	int CREATE_TIME = 2002;
	
	/** 登陆时间({@link Date}对象) */
	int LOGIN_TIME = 2003;
	
	/** 登出时间({@link Date}对象)  */
	int LOGOUT_TIME = 2004;
	
	/** 禁止登陆信息. (格式: 封禁状态_封禁起始时间(单位:秒)_封禁时长(单位:秒) )*/
	int FORBID_LOGIN = 2005;

	/** 禁止聊天信息. (格式: 封禁状态_封禁起始时间(单位:秒)_封禁时长(单位:秒) )*/
	int FORBID_CHAT = 2006;
	
	/** 登陆次数. */
	int LOGIN_COUNT = 2007;

	/** 连续登陆天数. */
	int LOGIN_DAYS = 2008;
	
	/** 总共登陆的天数 */
	int TOTAL_LOGIN_DAYS = 2009;
	
	/** 总共在线时间. 单位: 秒 */
	int ONLINE_TIMES = 2010;
	
	/** 总共在线时间长度. 单位: 秒 */
	int TOTAL_ONLINE_TIMES = 2011;

	/** 联盟ID */
	int ALLIANCE_ID = 2012;
	
	/** 联盟ID */
	int ALLIANCE_NAME = 2013;
	
	/** 玩家的衣服(怪物,玩家的外观)*/
	int CLOTHING = 2014;
	
	/** 步战武器(玩家的外观)*/
	int WEAPON_FOOT = 2015;
	
	/** 骑战武器(玩家的外观)*/
	int WEAPON_RIDE = 2016;
	
	/** 坐骑(玩家的外观)*/
	int MOUNT = 2017;
	
	/** 角色是否已上坐骑. ( {@link Boolean} ) */
	int RIDE = 2018;
	
	/**X轴坐标*/
	int X = 2019;
	
	/**Y轴坐标*/
	int Y = 2020;
	
	/** 基础ID(NPC,传送点,采集物等,家将(宠物))*/
	int BASE_ID = 2021;
	
	/** 组队ID */
	int TEAM_ID = 2022;

	/** 仓库的大小 */
	int STORAGE_SIZE = 2023;
	
	/** 排行榜称号信息 */
	int RANK_TITLE = 2024;
	
	/** vip信息 */
	int VIP_INFO = 2025;
	
	/** 新手引导完成的步骤 */
	int GUIDE_INFO = 2026;
	
	/** 角色综合战斗力 */
	int FIGHT_TOTAL_CAPACITY = 2027;
	/** 人物的战斗力 */
	int PLAYER_FIGHT_CAPACITY = 2028;
	/** 角色的基础战斗力加成 */
	int PLAYER_BASIC_CAPACITY = 2029;
	/** 角色肉身战斗力加成 */
	int PLAYER_MORTAL_CAPACITY = 2030;
	/** 角色经脉战斗力加成 */
	int PLAYER_MERIDIAN_CAPACITY = 2031;
	/** 角色的坐骑战斗力加成 */
	int FIGHT_HORSE_CAPACITY = 2032;
	/** 角色的坐骑星际战斗力加成 */
	int FIGHT_HORSE_STAR_CAPACITY = 2033;
	/** 角色装备战斗力 */
	int EQUIP_TOTAL_CAPACITY = 2034;
	/** 角色装备等级战斗力加成 */
	int EQUIP_BASE_CAPACITY = 2035;
	/** 角色装备星级战斗力加成 */
	int EQUIP_STAR_CAPACITY = 2036;
	/** 角色装备附加属性战斗力加成 */
	int EQUIP_ADDITION_CAPACITY = 2037;
	/** 角色装备套装属性战斗力加成 */
	int EQUIP_SUIT_CAPACITY = 2038;
	/** 剧情副本赠送的附加点数战斗力 */
	int DUNGEON_REWARD_CAPACITY = 2039;
	/** 押解(押镖状态)*/
	int ESCORT = 2040;
	/** 角色总战斗力 */
	int PLAYER_FIGHT_TOTAL_CAPACITY = 2041;
	
	/** 人物方向(现在只用于同屏双修) */
	int DIRECTION = 2041;                         // 超平增加 ---- 2012.7.6
	
	/** 怪物配置id */
	int MONSTER_CONFIG_ID = 2042;
	
	/** 装备镶嵌宝石战斗力*/
	int EQUIP_ENCHANGE_CAPACITY = 2043;
	
// ---------------  一些比较特殊的属性值 ---------
	/** 品质*/
	int QUALITY = 5001;
	
	/** 打坐状态*/
	int TRAIN_STATUS = 5002;
	
	/** 装备闪光效果*/
	int EQUIP_BLINK = 5003;
	
	/** 时装是否显示*/
	int FASHION_EQUIP_VIEW = 5004; 
	
	/** 家将附身效果*/
	int USER_PET_MERGED = 5005;
	//-------------家将(宠物)的特殊属性值-------
	/** 家将精力*/
	int PET_ENERGY = 3000;
	
	/** 家将精力最大值*/
	int PET_ENERGY_MAX = 3001;
	
	/** 家将成长值*/
	int PET_GROW = 3002;
	
	/** 家将悟性值*/
	int PET_SAVVY = 3003;

	/** 家将成长祝福值*/
	int PET_GROW_BLESS = 3004;
	
	/** 家将悟性祝福值*/
	int PET_SAVVY_BLESS = 3005;
	
	/** 家将的状态(空闲,战斗...)*/
	int PET_STATUS = 3006;
	
	/** 家将的技能*/
	int PET_SKILL = 3007;
	
	/** 家将契合度等级*/
	int PET_MERGED_LEVEL = 3008;
	
	/** 家将契合度祝福值*/
	int PET_MERGED_BLESS = 3009;
	
	/** 家将孔数*/
	int PET_SLOT_SIZE = 3010;
	
	/** 家将开始训练时间(单位:秒)*/
	int PET_START_TRAING_TIME = 3011;
	
	/** 家将本轮修炼的时间总和(单位:秒)*/
	int TOTLE_TRAING_TIME = 3012;
	
	/** 家将战斗力*/
	int PET_FIGHTINT_CAPACITY = 3013;
	
//	/** 荣誉点数. */
//	int MEMBER_HONOR = 10004;
//	/** 大都声望 */
//	int CAPITAL_FAME = 10009;
//	/** 跨服声望 */
//	int CROSS_SERVER_FAME = 10010;
//	/** 角色的组队ID */
//	int ROLE_TEAM_ID = 10012;
	
	//-------------怪物------------
	
//	int MOSTER_EXP = 4001 ;
//	/** 默认需要初始化的属性 */
//	public static Integer[] VIEW_ATTRIBUTE_KEYS = { HIT, DODGE, MOVE_SPEED, THEURGY_ATTACK,	THEURGY_DEFENSE,
//													THEURGY_CRITICAL, PHYSICAL_ATTACK, PHYSICAL_DEFENSE,
//													PHYSICAL_CRITICAL, PIERCE, BLOCK, RAPIDLY, DUCTILITY,
//													EXP, EXP_MAX, LEVEL };
}
