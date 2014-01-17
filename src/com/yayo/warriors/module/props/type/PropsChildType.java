package com.yayo.warriors.module.props.type;

/**
 * 道具的子类型
 * 
 * @author Hyint
 */
public interface PropsChildType {

	/** 加HP的药品. */
	int HP_DRUG_ITEM = 1001;

	/** 加MP的药品 */
	int MP_DRUG_ITEM = 1002;

	/** 加SP的药品 */
	int SP_DROP_ITEM = 1003;

	/** 1004 - 自动补偿失去血量 */
	int HPBAG_DRUG_ITEM = 1004;

	/** 1005 - 自动补偿失去内力 */
	int MPBAG_DRUG_ITEM = 1005;

	/** 1006 - 自动补偿家将失去体力 */
	int PET_HPBAG_DRUG_ITEM = 1006;

	/** 1007 - 复活药 */
	int RESURRECT_ITEM = 1007;
	
	/** 1008 - 经验丹 */
	int EXP_ITEM = 1008;

	
	
	
	
	
	
	
	
	
	
	/** 2001 - 物攻宝石子类型 */
	int PHYSICAL_ATTACK_ITEM_TYPE = 2001;

	/** 2002 - 法功宝石子类型 */
	int THEURGY_ATTACK_ITEM_TYPE = 2002;

	/** 2003 - 物防宝石子类型 */
	int PHYSICAL_DEFENSE_ITEM_TYPE = 2003;

	/** 2004 - 法防宝石子类型 */
	int THEURGY_DEFENSE_ITEM_TYPE = 2004;

	/** 2005 - 生命宝石子类型 */
	int HPMAX_ITEM_TYPE = 2005;

	/** 2006 - 法力宝石子类型 */
	int MPMAX_ITEM_TYPE = 2006;

	/** 2007 - 物暴宝石子类型 */
	int PHYSICAL_CRITICAL_ITEM_TYPE = 2007;

	/** 2008 - 法暴宝石子类型 */
	int THEURGY_CRITICAL_ITEM_TYPE = 2008;

	/** 2009 - 命中宝石子类型 */
	int HIT_ITEM_TYPE = 2009;

	/** 2010 - 闪避宝石子类型 */
	int DODGE_ITEM_TYPE = 2010;
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	/** 3001 - 装备升星宝石 */
	int EQUIP_ASCENT_STAR_TYPE = 3001;

	/** 3002 - 装备洗练宝石类型 */
	int EQUIP_POLISHED_TYPE = 3002;
	
	/** 3003 - 装备升阶宝石(升阶符) */
	int EQUIP_ASCENT_RANK_TYPE = 3003;

	/** 3004 - 幸运晶. */
	int LUCKY_PROPS_TYPE = 3004;

	/** 3005 - 洗练锁道具 */
	int POLISHED_LOCK_TYPE = 3005;

	/** 3006 - 装备精炼石 */
	int EQUIP_REFINING_TYPE = 3006;
	
	/** 3007 - 装备升阶保护符 */
	int EQUIP_RANK_SAFE_TYPE = 3007;
	
	/** 3008 - 装备强化保护符 */
	int EQUIP_STAR_SAFE_TYPE = 3008;
	
	
	//----------------  4000+ ---------------------
	
	/** 4001 - 角色技能书 */
	int USER_SKILL_BOOK_TYPE = 4001;
	
	/** 4002 - 宠物技能书 */
	int PET_SKILL_BOOK_TYPE = 4002;
	
	//----------------  5000+ ---------------------
	/** 5001 - 离线经验丹 */
	int INC_OFFLINE_EXP_TYPE = 5001;
	
	/** 5002 - BUFF道具 */
	int INC_BUFF_TYPE = 5002;
	
	//----------------  6000+ ----------------------
	//----------------  7000+ ----------------------
	/** 7001 - 坐骑普通幻化丹*/
	int HORSE_FANCY_MEDICINE_TYPE = 7001;
	
	//----------------  8000+ ----------------------
	
	/** 8002 - 完美炼骨丹 */
	int MORTAL_PERFECT_PROPS_TYPE = 8002;
	
	/** 8003 - 经脉升阶道具龙脉 */
	int MERIDIAN_DRAGON_PROPS_TYPE = 8003;
	
	/** 8004 - 冲脉所需道具大经丹 */
	int MERIDIAN_MEDICINE_TYPE = 8004;
	
	/** 8005 - 真气丹*/
	int GAS_PROPS_TYPE = 8005;
	
	/** 8006 - 冲脉加概率道具 */
	int MERIDIAN_LUCK_PROPS_TYPE = 8006;
	
	/** 8007 - 肉身升级卷轴类型 */
	int MORTAL_LEVEL_UP_PROPS_TYPE = 8007;
	
	//----------------  9000+ ----------------------
	/** 9005 - 帮派令(创建帮派)*/
	int ALLIANCE_CREATE_PROPS_TYPE = 9005;
	
	/** 9006 -帮派物质捐献用的道具*/
	int ALLIANCE_DONATE_PROPS_TYPE = 9006;
	
	//----------------  10000+ ---------------------
	
	/** 10001 - 家将培养成长丹*/
	int PET_TRAIN_SAVVY_TYPE = 10002;
	
	//----------------  11000+ ---------------------
	/** 11001 - 家将蛋类型(随机)*/
	int PET_RAN_EGG_TYPE = 11001;
	
	/** 11002 - 家将蛋类型(固定)*/
	int PET_EGG_TYPE = 11002;
	
	//----------------  12000+ ---------------------
	/** 12001 - 铜币符类型*/
	int SILVER_PROPS_TYPE = 12001;
	
	/** 12002 - 扩展符类型*/
	int CONTAINER_PAGE_TYPE = 12002;

	/** 12003 - 小飞鞋类型*/
	int FLY_SHOES_TYPE = 12003;
	
	/** 12006 - 抽奖令类型*/
	int LOTTERY_PROPS_TYPE = 12006;
	
	/** 12007 - 座骑进化类型*/
	int HORSE_EVOLVE_TYPE = 12007;
	
	/** 12018 - 藏宝图类型*/
	int TREASURE_PROPS_TYPE = 12018;
	
	/** 12019 - 挖藏宝道具类型*/
	int TREASURE_DIG_PROPS_TYPE = 12019;
	
	/** 12022 - 押镖保护令*/
	int ESCORT_PROTECTION_TYPE = 12022;
	
	/** 12024 - 刷镖令*/
	int ESCORT_REFRESH_TYPE = 12024;
	
	/** 12025 - 家将契合丹类型*/
	int PET_MERGED_PROPS_TYPE = 12025;
	
	/** 12028 - 金砖(绑定元宝)类型*/
	int COUPON_PROPS_TYPE = 12028;

	/** 12040 - 帮派召集令 */
	int CONVENE_ALLIANCE = 12040;
	
	/** 12041 - 阵营召集令 */
	int CONVENE_CAMP = 12041;
	
	/** 12042 - 队伍召集令 */
	int CONVENE_TEAM = 12042;
	
	//----------------  13000+ ---------------------
	/** 13001 - 家将回复HP*/
	int PET_HP_DRUG_ITEM = 13001;
	
	/** 13002 - 家将回复精力*/
	int PET_DRUG_ENGRY_ITEM = 13002;
	
	//-----------------  15000+---------------------
	/** 15001 - 固定礼包*/
	int FASTEN_GIFI_TYPE = 15001;
	
	/** 15002 - 随机礼包*/
	int RAND_GIFI_TYPE = 15002;
	
	
	//------------------- 16000 +--------------------
	
	/** 16001 - VIP卡 */
	int VIP_CARD_PROPS_TYPE = 16001;
	
	//------------------- 120042 +-------------------

}
