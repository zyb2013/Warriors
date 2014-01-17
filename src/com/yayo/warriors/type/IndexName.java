package com.yayo.warriors.type;

/**
 * 基础数据索引名定义(以模块名开始，在索引名是加上索引属性名)
 * @author jonsai
 *
 */
public interface IndexName {
	
	/** 角色升级表	职业-等级索引 */
	String USER_JOB_LEVEL = "USER_JOB_LEVEL";
	
	/** 固定礼包表	职业-礼包编号  */
	String PROPS_GIFT_NO_JOB = "PROPS_GIFT_NO_JOB";
	
	/** 道具表	道具子类型  */
	String PROPS_CHILDTYPE = "PROPS_CHILDTYPE";
	
	/** 技能升级表	职业-礼包编号  */
	String SKILL_LEARN_ITEMID = "SKILL_LEARN_ITEMID";
	
	/** NPC表	地图编号  */
	String NPC_MAPID = "NPC_MAP_ID";
	
	/** 怪物表	地图编号  */
	String MONSTER_MAPID = "MONSTER_MAP_ID";
	
	/** 肉身等级表 职业-类型-等级 */
	String MORTAL_JOB_TYPE_LEVEL = "MORTAL_JOB_TYPE_LEVEL";
	
	/** 肉身加成表 职业-等级 */
	String MORTAL_JOB_LEVEL = "MORTAL_JOB_LEVEL";
	
	/** 副本怪物表	 副本ID-回合数(波数) */
	String MONSTERDUNGEON_DUNGEONID_ROUND = "MONSTERDUNGEON_DUNGEONID_ROUND";
	
	/** 帮派建筑物升级表	 类型-等级 */
	String ALLIANCEBUILD_TYPE_LEVEL = "ALLIANCEBUILD_TYPE_LEVEL";
	
	/** 抽奖格子表	 规则id */
	String LOTTERYGRIDRATE_CONFIGID = "LOTTERYGRIDRATE_CONFIGID";
	
	/** 抽奖道具奖励表	 道具奖励生成id */
	String LOTTERY_PROPS_ROLL_ID = "LOTTERY_PROPS_ROLL_ID";
	
	/** 商城表  道具道具id  */
	String MALL_PROPSID = "MALL_PROPSID";

	/** 技能效果ID */
	String SKILL_EFFECT_SKILLID = "SKILL_EFFECT_SKILLID";
	
	/** 技能效果表  效果类型  */
	String SKILLEFFECT_EFFECT_TYPE = "SKILLEFFECT_EFFECT_TYPE";
	
	/** 护送任务 阵营类型*/
	String ESCORT_TASK_CAMP_TYPE = "ESCORT_TASK_CAMP_TYPE";
	
	/** 藏宝基础表  宝藏id  */
	String TREASURE_REWARDID = "TREASURE_REWARDID";
	
	/** 藏宝基础表  宝藏id-品质  */
	String TREASURE_REWARDID_QUALITY = "TREASURE_REWARDID_QUALITY";
	
	/** 藏宝事件生成表  宝藏id-铲子类型  */
	String TREASURE_EVENT_REWARDID_AND_DIG_PROPID = "TREASURE_EVENT_REWARDID_DIG_PROPID";
	
	/** 藏宝事件生成表  宝藏id-铲子类型-npcID  */
	String TREASURE_EVENT_REWARDID_AND_DIG_PROPID_AND_NPCID = "TREASURE_EVENT_REWARDID_DIG_PROPID_NPC_ID";
	
	/** 藏宝事件表  事件id  */
	String TREASURE_DROP_NO = "TREASURE_EVENT_ID";
	
	/** 地图表	场景类型  */
	String MAP_SCREENTYPE = "MAP_SCREENTYPE";
	
	/** 地图阵营传送表	阵营传送点  */
	String MAP_CAMP_CHANGE_POINT = "MAP_CAMP_CHANGE_POINT";
	
	/** 大地图表	地图名  */
	String BIG_MAP_NAME = "BIG_MAP_NAME";
	
	/** 坐骑模型*/
	String HORSE_MODEL = "HORSE_MODEL";
	
	/** 装备洗练索引 */
	String EQUIP_WASH_ADDITION = "EQUIP_WASH_ADDITION";

	/** 装备升阶规则索引 */
	String EQUIP_RANKRULE_ATTRIBUTE = "EQUIP_RANKRULE_ATTRIBUTE";
	
	/** 技能分类 */
	String SKILL_CLASSIFY = "SKILL_CLASSIFY_";
	
	/** 技能学习的索引ID列 */
	String SKILL_LEARN_SKILLID = "SKILL_LEARN_SKILLID";
	
	/** NPC 的场景类型索引 */
	String NPC_SCREEN_TYPE = "NPC_SCREEN_TYPE";
	
	/** 活动怪物ID */
	String ACTIVE_MONSTERID = "ACTIVE_MONSTERID";
	
	/** 帮派技能等级*/
	String ALLIANCE_SKILL_LEVEL = "ALLIANCE_SKILL_LEVE";
	
	/** 阵营称号奖励 */
	String CAMP_TITLE_REWARD = "CAMP_TITLE_REWARD";
	
	/** 阵营据点  */
	String CAMP_TYPE_POINT = "CAMP_TYPE_POINT";
	
	/** 装备神武配置 */
	String EQUIP_SHENWUID_CONFIG = "EQUIP_SHENWUID_CONFIG";
	
	/** 装备神武属性 */
	String EQUIP_SHENWUID_ATTRIBUTE = "EQUIP_SHENWUID_ATTRIBUTE";
	/** 装备神武属性 */
	String EQUIP_SHENWUID_ATTRIBUTE1 = "EQUIP_SHENWUID_ATTRIBUTE1";
	
	/** 在线活动类型*/
	String ACTIVE_ONLINE_TYPE = "ACTIVE_ONLINE_TYPE";
	
	/** 在线活动怪物玩法规则*/
	String ACTIVE_MONSTER_RULE = "ACTIVE_MONSTER_RULE";
	
	/** 成就达成类型 */
	String ACHIEVE_TYPE = "ACHIEVE_TYPE";

	/** 乱武战场据点  */
	String BATTLE_FIELD_TYPE_CAMP = "BATTLE_FIELD_TYPE_CAMP";
	
	/** 乱武战场阵营采集物  */
	String BATTLE_FIELD_CAMP_COLLECT = "BATTLE_FIELD_CAMP_COLLECT";
	
	/** 运营活动公告*/
	String ACTIVE_OPERATOR_NOTICE = "ACTIVE_OPERATOR_NOTICE";
	
	/** 商城活动开启 */
	String MALL_ACTIVE_OPEN = "MALL_ACTIVE_OPEN";
	
	/** 商城活动物品 */
	String MALL_ACTIVE_PROPS = "MALL_ACTIVE_PROPS";
	
	/** 运营活动类型*/
	String ACTIVE_OPERATOR_TYPE = "ACTIVE_OPERATOR_TYPE";
	
	/** 运营充值活动ID索引*/
	String ACTIVE_CHARGE_BASEID = "ACTIVE_CHARGE_BASEID";
	
	/** 充值序号*/
	String RECHARGE_SERIAL = "RECHARGE_SERIAL";
	
	/** 充值礼包类型 */
	String GIVE_CHARGE_TYPE = "GIVE_CHARGE_TYPE";

	/** 奖励ID */
	String REWARDS_ID = "REWARDS_ID";
	
	String TASK_MONSTER_ID = "TASK_MONSTER_ID";
}