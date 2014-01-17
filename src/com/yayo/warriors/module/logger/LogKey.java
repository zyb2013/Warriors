package com.yayo.warriors.module.logger;

/**
 * 日志Key
 * 
 * @author Hyint
 */
public interface LogKey {
	
	/** 时间 */
	String TIME = "time";

	/** 物品信息 */
	String INFO = "info";
	
	/** 来源 */
	String SOURCE = "source";
	
	/** 角色ID */
	String PLAYERID = "playerId";

	/** 增加/减少的金币 */
	String GOLDEN = "golden";

	/** 增加/减少的礼金 */
	String COUPON = "coupon";

	/** 增加/减少的游戏币 */
	String SILVER = "silver";
	
	/** 角色名 */
	String PLAYER_NAME = "playerName";
	
	/** 角色账号名 */
	String USER_NAME = "userName";
	
	/** 当前的金币数量 */
	String CURRENT_GOLDEN = "currentGolden";

	/** 当前的礼金数量 */
	String CURRENT_COUPON = "currentCoupon";

	/** 当前的游戏币数量 */
	String CURRENT_SILVER = "currentSilver";
	
	/** ip地址 */
	String IP = "ip";
	
	/** 角色的当前HP量 */
	String HP = "hp";
	
	/** 角色的当前MP量 */
	String MP = "mp";
	
	/** 角色的当前gas量 */
	String GAS = "gas";
	
	/** 角色的当前经验值 */
	String EXP = "exp";
	
	/** 角色的HP便携包值 */
	String HPBAG = "hpBag";
	
	/** 角色的MP便携包值 */
	String MPBAG = "mpBag";
	
	/** 角色的等级 */
	String LEVEL = "level";
	
	/** 角色的HPMax值 */
	String HPMAX = "hpMax";
	
	/** 角色的MPMax值 */
	String MPMAX = "mpMax";
	
	/** 角色的GasMax值 */
	String GASMAX = "gasMax";
	
	/** 角色所在的分线 */
	String BRANCHING = "branching";
	
	/** 战斗模式.(0-和平)*/
	String FIGHT_MODE = "fightMode";
	
	/** 连续登录天数*/
	String LOGINDAYS = "loginDays";
	
	/** 角色登录日期*/
	String LOGINTIME = "loginTime";
	
	/** 角色登出时间*/
	String LOGOUTTIME = "logoutTime";
	
	/** 角色登录次数*/
	String LOGINCOUNT = "loginCount";
	
	/** 角色当前在线时间*/
	String ONLINETIMES = "onlineTimes";
	
	/** 角色创建时间*/
	String CREATE_TIME = "createTime";
	
	/** 基础任务id*/
	String TASKID = "taskId";
	
	/** 基础任务类型*/
	String TASKTYPE = "taskType";
	
	/** 基础任务名*/
	String TASKNAME = "taskName";
	
	/** 增加前的经验 */
	String BEFORE_EXP = "beforeExp";
	
	/** 增加的经验 */
	String ADDEXP = "addExp";
	
	/** 升级前等级 */
	String BEFORE_LEVEL = "beforeLevel";
	
	/** 升级后等级 */
	String AFTER_LEVEL = "afterLevel";
	
	/** 升级时间 */
	String UPGRADE_TIME = "upgradeTime";
	
	/** 帮派的名字*/
	String ALLIANCE_NAME = "allianceName";
	
	/** 帮派当前等级*/
	String CURRENT_ALLIANCE_LEVEL = "currentAllianceLevel";
	
	/** 副本id */
	String MAP_ID = "mapId";
	
	/** 交易目标(名字) */
	String TRADE_TARGET = "tradeTarget";
	
	/** 当前家将成长等级*/
	String CURRENT_SAVVY_LEVEL = "currentSavvyLevel";
	
	/** 当前家将契合度等级*/
	String CURRENT_MERGED_LEVEL = "currentMergedLevel";
	
	/** 当前家将契合度祝福值*/
	String CURRENT_MERGED_BLESS = "currentMergedBless";
	
	/** 当前家将契合度叠加祝福值*/
	String CURRENT_MERGED_BLESS_PERCENT = "currentMergedBlessPercent";
	
	/** 道具的ID*/
	String PROPS_ID = "propsId";
	
	/** 使用数量*/
	String USE_PROPS_COUNT = "usePropsCount";
	
	/** 货币类型 */
	String CURRENCY = "currency";
	
	/** 掉落数量 */
	String DROP_COUNT = "dropCount";
	
	/** 掉落怪物名称 */
	String MONSTER_NAME = "monsterName";
	
	/** 运营活动ID*/
	String ACTIVE_ID = "activeId";
	
	/** 奖励ID*/
	String REWARD_ID = "rewardId";
	
	/** 基础道具*/
	String BASE_USER_PROPS = "userProps";
	
	/** 基础装备*/
	String BASE_USER_EQUIP = "userEquip";
	
	/** 活动的名字*/
	String ACTIVE_NAME = "activeName";
	
	/** 类型*/
	String TYPE = "type";
	
	/** 道具ID */
	String ITEM_ID = "itemId";

	/** 道具名 */
	String ITEM_NAME = "itemName";
	
	/** 基础ID*/
	String BASE_ID = "baseId";
	
	/** 战斗力*/
	String FIGHT_CAPACITY = "fightCapacity";
	
	/** 副本名称*/
	String DUNGEON_NAME = "dungeonName";
	
	/** 杀人数 */
	String KILLP_LAYERS = "killPlayers";
	/** 死亡数 */
	String DEATHS = "deaths";
	/** 战斗荣誉 */
	String FIGHT_HONOR = "fightHonor";
	/** 采集荣誉 */
	String COLLECT_HONOR = "collectHonor";
	/** 战场时间 */
	String BATTLE_FIELD_DATE = "battleDate";
	/** 房间数 */
	String ROOM_NUM = "roomNum";
	/** 玩家人数 */
	String PLAYER_NUM = "playerNum";
}