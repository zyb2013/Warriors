package com.yayo.warriors.module.logger.type;

/**
 * 日志的来源
 * 
 * @author Hyint
 */
public enum Source {

	//---------------- 1 --> 100 --------------------
	/** 1 - 角色登陆 */
	PLAYER_LOGIN(1),

	/** 2 - 角色登出 */
	PLAYER_LOGOUT(2),
	
	/** 3 - 角色等级因为经验升级 */
	PLAYER_LEVEL_UP_EXP(3),
	
	/** 4 - 角色等级因为修改升级 */
	PLAYER_LEVEL_UP_UPDATE(4),
	
	/** 5 - 管理后台发放 */
	ADMIN_MANAGER(5),
	
	/** 6 - 充值服发送 */
	CHARGE_SERVER_SEND(6),
	
	/** 7 - 角色选择阵营 */
	PLAYER_SELECT_CAMP(7),
	
	/** 8 - 角色聊天 */
	PLAYER_CHAT(8), 
	
	/** 9 - 角色复活 */
	PLAYER_RECURRENT(9),
	
	/** 10 - 每日登录金币奖励 */
	PLAYER_DAILYLOGIN_REWARD_GOLDEN(10),
	
	/** 11 - GM命令增加 */
	GM_CODE_SEND(11),
	
	//-----------------101 --> 200 ------------------
	
	/** 101 - 接受主支线任务 */
	ACCEPT_TASK(101),

	/** 102 - 放弃主支线任务 */
	GIVEUP_TASK(102),

	/** 103 - 完成主支线任务 */
	COMPLETE_TASK(103),

	/** 104 - 领取主支线任务奖励 */
	REWARDS_TASK(104),
	
	/** 105 - 刷新日环任务品质  */
	LOOPTASK_REFRESH_QUALITY(105),
	
	/** 106 - 刷新试炼任务品质  */
	PRACTICETASK_REFRESH_QUALITY(106),
	
	/** 107 - 日环任务快速完成  */
	LOOPTASK_FASTCOMPLETE(107),
	
	//----------------201 --> 300 -------------------
	
	/** 201 - 角色冲脉 */
	LIGHT_MERIDIAN(201),
	
	/** 202 - 经脉升阶 */
	MERIDIAN_STAGEUP(202),
	
	/** 203 - 肉身升级 */
	MORTAL_LEVELUP(203),
	
	
	//---------------301 --> 400 --------------------
	
	/** 301 - 商城购买 */
	BUY_MALL_PROPS(301),
	
	/** 302 - 商店购买 */
	BUY_SHOP_PROPS(302),
	
	/** 303 - 玩家交易 */
	PLAYER_TRADE(303),
	
	/** 304 - 限购 */
	BUY_MALL_OFFER(304),
	
	//---------------401 --> 500 --------------------
	/** 401 - 丢弃装备 */
	PROPS_DROP_EQUIP(401),
	
	/** 402 - 丢弃道具 */
	PROPS_DROP_PROP(402),
	
	/** 403 - 出售装备 */
	PROPS_SELL_USEREQUIP(403),
	
	/** 404 - 出售道具 */
	PROPS_SELL_USERPROPS(404),
	
	/** 405 - 合并道具 */
	PROPS_MERGE_USERPROPS(405),
	
	/** 406 - 分拆道具 */
	PROPS_SPLITE_USERPROPS(406),
	
	/** 407 - 合成道具 */
	PROPS_SYNTH_STONEITEM(407),
	
	/** 408 - 装备分解  */
	PROPS_RESOLVE_USEREQUIP(408),
	
	/** 409 - 装备修理  */
	PROPS_REPAIR_USEREQUIP(409),
	
	/** 410 - 扩展背包  */
	BACKPACK_EXPAND(410),
	
	/** 411 - 炼化装备  */
	PROPS_ARITIFICE_USERPROPS(411),
	
	/** 412 - 直接使用道具  */
	PROPS_USE_PROPS(412),
	
	/** 413 - 装备升星  */
	PROPS_ASCENT_EQUIPSTAR(413),
	
	/** 414 - 装备升阶  */
	PROPS_ASCENT_EQUIPRANK(414),
	
	/** 415 - 装备镶嵌  */
	PROPS_ENCHANGE_EQUIP(415),
	
	/** 416 - 移除装备镶嵌的宝石  */
	PROPS_REMOVE_EQUIP_ENCHANGE(416),
	
	/** 417 - 洗练装备  */
	PROPS_POLISHED_EQUIP_ADDITIONS(417),
	
	/** 418 - 精炼用户装备  */
	PROPS_REFINE_EQUIP_ATTRIBUTE(418),
	
	/** 419 - 用户道具炼化  */
	PROPS_ARITIFICE_PROPS(419),
	
	/** 420 - 抽奖得到道具  */
	PROPS_DO_LOTTERY(420),
	
	/** 421 - 采集得到道具  */
	PROPS_COLLECT(421),

	/** 422 - 装备神武突破 */
	EQUIP_SHENWU_TEMPO(422),

	/** 423 - 装备神武打造 */
	EQUIP_SHENWU_FORGE(423),
	
	/** 424 - 装备继承星级*/
	EQUIP_EXTENDS_STAR(424),
	
	//---------------501 --> 600 --------------------
	/** 战斗经验 */
	EXP_FIGHT(501),
	
	/** 经验道具 */
	EXP_PROPS(502),
	
	/** 任务 */
	EXP_TASK(503),
	
	/** 冲脉 */
	EXP_RUSH_MERIDIAN(504),
	
	/** 冲脉范围内加经验 */
	EXP_MERIDIAN_RANGE_ADD(505),
	
	/** 领取祝福瓶经验 */
	EXP_REWARD_BLESS(506),
	
	/** 打坐 */
	EXP_TRAINING(507),
	
	/** vip祝福奖励 */
	EXP_VIP_BLESS_REWARD(508),
	
	/** 乱武战场奖励 */
	EXP_BATTLE_FIELD_REWARD(509),
	
	//--------------601 -- > 650------------------
	/** 601 - 藏宝图挖宝  */
	TREASURE_PROPS(601),
	
	/** 602 - 藏宝图刷新品质  */
	TREASURE_FRESH_QUALITY(602),
	
	//--------------660  --> 670------------------
	/** 660 - 完成剧情副本*/
	COMPLETE_STORY_DUNGEON(660),
	
	/** 661 - 副本*/
	DUNGEON(661),
	
	//--------------680  --> 690------------------
	/** 680 - 完成护送任务*/
	COMPLETE_ESCORT_TASK(680),
	
	//--------------691  --> 700------------------
	/** 691 - 帮派创建*/
	ALLIANCE_CREATE(691),
	
	/** 692 - 帮派捐献银币*/
	ALLIANCE_DONATE_SILVER(692),
	
	/** 693 - 帮派捐献道具*/
	ALLIANCE_DONATE_PROPS(693),
	
	//--------------701  --> 800------------------
	/** 701 - 坐骑等级提升*/
	HORSE_LEVEL_UP(701),
	
	/** 702 - 坐骑幻化*/
	HORSE_GENERALFANCY(702),
	
	//--------------801  --> 900------------------
	/**  801 - 开启家将令*/
	PET_OPE_EGG(801),
	
	/**  802 - 家将成长*/
	PET_SAVVY_TRAINING(802),
	
	/** 803 - 家将开启家将槽*/
	PET_OPEN_SOLT(803),
	
	/** 804 - 家将契合*/
	PET_MERGED_TRAINING(804),
	
	//--------------901  --> 1000-----------------
	/** 901 - 押镖保护令*/
	ESCORT_PROTECTION(901),
	
	/** 901 - 押镖刷新任务品质*/
	ESCORT_REFRESH_QUALITY(902),
	
	
	//---------------1001 --> 1100------------------
	
	/** 1001 - 掉落 */
	FIGHT_DROP(1001),
	
	//-------------- 1101 --> 1200-------------------
	/** 在线礼包 */
	ONLINE_GIFT(1101),
	
	/** 固定礼包 */
	FASTEN_GIFT(1102),
	
	/** 随机礼包 */
	RAND_GIFT(1103),
	
	/** 收藏礼包 */
	GARNER_GIFT(1104),
	
	//-------------- 1201 --> 1300-------------------

	/** 1201 - 摆摊购买 */
	MARKET_BUYS(1201),

	/** 1202 - 摆摊出售 */
	MARKET_SOLD(1202),
	
	//-------------- 1301 --> 1400-------------------
	/** 1301 - 学习技能 */
	PLAYER_LEARN_SKILL(1301),
	
	
	//-------------- 1401 --> 1500 --------------------
	
	/** 闭关消耗 */
	PLAYER_TRAINING(1401),
	
	//--------------- 1501 --> 1600 --------------------
	
	/** 元宝开通VIP */
	VIP_LEVEL_UP(1501),
	
	/** 领取VIP福利 */
	RECEIVE_VIP_REWARD(1502),
	
	//--------------- 1601 --> 1700 --------------------
	
	/** 阵营战官衔俸禄 */
	CAMP_BATTLE_TITLE_SALARY(1601),
	
	/** 阵营战积分或胜利奖励 */
	CAMP_BATTLE_SCORE_WIN_REWARD(1602),
	
	
	//------------------- 1701 --> 1800 ------------------
	
	/** 领取邮件附件奖励(元宝, 铜币, 物品,　装备) */
	RECEIVE_MAIL_REWARDS(1701),
	
	/** 领取CDKEY礼包奖励 */
	RECEIVE_CDKEY_REWARDS(1702),
	
	/** 领取普通礼包奖励 */
	RECEIVE_GIFT_REWARDS(1703),
	
	
	//------------------ 1801 --> 1900 ------------------
	
	/** 好友赠酒,　增加好友度 */
	FRIENDS_PRESENT(1801),
	
	/** 好友敬酒 */
	FRIENDS_GREET(1802),
	
	/** 饮酒奖励 */
	FRIENDS_DRINK_WINE(1803),
	
	//------------------ 2000 --> 2100 ------------------
	/** 运营活动*/
	ACTIVE_OPERATOR(2000),
	
	/** 领取成就奖励 */
	ACHIEVE_REWARD_RECEIVED(2001), 
	
	/** 领取充值礼包奖励 */
	RECHARGE_GIFT_REWARDS(2002)
	;
	private int code = -1;
	
	Source(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
