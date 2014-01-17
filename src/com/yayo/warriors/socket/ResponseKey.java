package com.yayo.warriors.socket;

public interface ResponseKey {
	
	/** 性别 */
	String SEX = "sex";
	
	/** 阵营 */
	String CAMP = "camp";
	
	/** 图标 */
	String ICON = "icon";

	/** 阶 */
	String RANK = "rank";
	
	/** 职业 */
	String JOB = "job";
	
	/** 结果值 */
	String RESULT = "result";
	
	/** 目标对象 */
	String TARGET = "target";
	
	/** 角色对象*/
	String ROLES = "roles";
	
	/** 用户帐号 */
	String USERNAME = "userName";

	/** 帐号密码 */
	String PASSWORD = "password";

	/** 角色ID */
	String PLAYER_ID = "playerId";
	
	/** 目标ID */
	String TARGET_ID = "targetId";
	
	/** 序号 */
	String SERIAL_NUM = "serialNum";
	
	/** 目标类型 */
	String TARGET_TYPE = "targetType";
	
	/** 用户召唤兽ID */
	String USER_PET_ID = "userPetId";
	
	/** 角色名 */
	String PLAYER_NAME = "playerName";
	
	/** 频道 */
	String CHANNEL = "channel";
	
	/** 聊天信息 */
	String CHAT_INFO = "chatInfo";
	
	/** 目标名 */
	String TARGET_NAME = "targetName";

	/** 展示单位 */
	String SHOW_TERMS = "showTerms";

	/** 分线编号 */
	String BRANCHING = "branching";
	
	/** 类型 */
	String TYPE = "type";

	/** 信息 */
	String INFO = "info";
	
	/** 技能ID */
	String SKILL_ID = "skillId";

	/** 技能VO */
	String SKILL_VO = "skillVO";
	
	/** 用户道具ID */
	String USER_PROPSID = "userPropsId";
	
	/** 用户道具ID */
	String TARGET_PROPSID = "targetPropsId";
	
	/**衣服*/
	String CLOTHING = "clothing";
	
	/**家将的衣服*/
	String PET_CLOTHING = "petClothing";
	
	/**武器*/
	String WEAPON = "weapon";

	/** x坐标 */
	String X = "x";
	
	/** y坐标 */
	String Y = "y";
	
	/** 面向 */
	String FACE = "face";
	
	/** 家将的X坐标*/
	String PET_X = "pet_x";
	
	/** 家将的Y坐标*/
	String PET_Y = "pet_y";
	
	/** 家将,元宝培养*/
	String PET_AUTO = "petAuto";
	
	/** 家将的经验值*/
	String PET_EXP = "petExp";
	
	/** 家将等级*/
	String PET_LEVEL = "petLevel";
	
	/** NPC功能类型*/
	String FUNTYPE = "funtype";
	
	/** 行走路径*/
	String PATH = "path";
	
	/** 任务ID*/
	String TASKID = "taskId";
	
	/** 基础ID*/
	String BASEID = "baseId";
	
	/** 事件ID*/
	String EVENTID = "eventId";
	
	/** 任务事件的进度*/
	String PROGRESS = "progress";
	
	String PARAMS = "params";
	String VALUES = "values";
	
	String PET_PARAMS = "pet_params";
	String PET_VALUES = "pet_values";
	
	String UNITID = "unitId";

	String LEVEL = "level";
	
	String ACTIVE = "active";
	String ALIVE_ACTIVE_ID = "aliveActiveId";
	
	String AMOUNT = "amount";
	String PASSIVE = "passive";
	
	String REPORTS = "reports";
	String BUFFERS = "buffers";
	String GOODS_ID = "goodsId";
	String GOODS_TYPE = "goodsType";
	String PACKAGE_TYPE = "packageType";
	
	String USER_PROPS = "userProps";
	String USER_EQUIPS = "userEquips";
	String USER_EQUIP_ID = "userEquipId";
	String TARGET_EQUIP_ID = "targetEquipId";
	String ENTRY = "entry";
	String COOL_TIME = "coolTime";
	String MAPID = "mapId";
	String ID = "id";

	/** 索引号*/
	String INDEX = "index";

	String NPCID = "npcId";
	String COUNT = "count";
	String MALLID = "mallId";
	String SHOPID = "shopId";
	String CURRENCY = "currency";

	String TEAM_ID = "teamId";
	String TEAMS = "teams";

	String LEADER = "leader";
	String LEADER_ID = "leaderId";
	String MEMBERS = "members";
	String START = "start";
	String TOTAL = "total";
	String REASON = "reason";
	String ISLEADER = "isLeader";
	String ROLE_JOB = "roleJob";
	String MALL_TYPE = "mallType";
	
	/** 关键字*/
	String KEYWORDS = "keywords";
	
	/** 搜索结果*/
	String SEARCH_VALUE = "searchValue";
	
	/** BUFFID*/
	String BUFFER_ID = "bufferId";
	
	/** 帮派 护法人数*/
	String PROLAW_NUM =  "prolawNum";
	
	/** 帮派 长老人数*/
	String ELDER_NUM = "elderNum";
	
	/** 帮派 副帮主人数*/
	String DMASTER_NUM = "dmasterNum";
	
	/** 帮派今日捐献的道具数量*/
	String DONATE_PROPS_COUNT = "donatePropsCount";
	
	/** 帮派今日捐献的铜币数量*/
	String DONATE_SILVER_COUNT = "donateSilverCount";
	
	/** 研究技能的ID*/
	String RESEARCH_ID = "researchId";
	
	/** 玩家帮派对象*/
	String PLAYER_ALLIANCE = "playerAlliance";
	
	/** 帮派实体*/
	String ALLIANCE = "alliance";
	
	/** 帮派技能集合*/
	String SKILLS = "skills";
	
	/** 帮派ID*/
	String ALLIANCE_ID = "allianceId";
	
	/** 帮派资金*/
	String ALLIANCE_SILVER = "allianceSilver";
	
	/** 帮派名字*/
	String ALLIANCE_NAME = "allianceName";
	
	/** 帮主的名字*/
	String MASTER_NAME = "masterName";
	
	/** 帮主的ID*/
	String MASTER_ID = "masterId";
	
	/** 帮派记录*/
	String RECORD = "record"; 
	
	/** 帮派的职位*/
	String TITLE = "title";
	
	/** 邀请者*/
	String INVITE = "invite";
	
	/** 申请者的VO集合*/
	String APPLY_VO = "applyVo";
	
	/** 申请者ID*/
	String APPLY_ID = "applyId";
	
	/** 数量*/
	String NUMBER = "number";
	
	/** 帮派令牌*/
	String TOKEN_PROPS_COUNT = "tokenPropsCount";
	
	/** 帮派职位*/
	String ALLIANCE_TITLE = "title";
	
	/** 确认*/
	String CONFIRM = "confirm";
	
	/** 是否同意*/
	String AGREE = "agree";
	
	String USER_FACTION = "userfaction";
	
	String CONTENT = "content";
	
	/** 是否预备动作 */
	String PREPARE = "prepare";
	
	String REWARD_ID = "rewardId";
	
	/** 自动购买的数量*/
	String AUTO_BUY_COUNT = "autoBuyCount";
	
	/** 绑定的用户道具 */
	String BIND_USER_PROPS = "bindUserProps";

	/** 幸运石道具信息 */
	String LUCKY_USER_PROPS = "luckyUserProps";
	
	/** 未绑定的用户道具 */
	String UNBIND_USER_PROPS = "unBindUserProps";
	
	/** 保护型用户道具信息 */
	String SAFE_USER_PROPS1 = "safeUserProps1";

	/** 保护型用户道具信息 */
	String SAFE_USER_PROPS2 = "safeUserProps2";

	String TEAM_MODE = "teamMode";

	String ALLOCATE_TYPE = "allocateType";
	
	/** 地图转场点*/
	String CHANGE_SCREEN = "changeScreen";
	
	/** 阵营*/
	String CAMP_VALUE = "campValue";
	
	/** 副本原型ID*/
	String DUNGEON_BASE_ID = "dungeonBaseId";
	
	/** 副本实体ID*/
	String DUNGEON_ID = "dungeonId";
	
	/** 副本当前回合*/
	String ROUND = "round";
	
	/** 玩家副本信息*/
	String DUNGEON_INFO = "dungeonInfo";
	
	/** 玩家剧情副本信息*/
	String STORY_INFO = "storyInfo";

	/** 标记*/
	String FALG = "flag";
	
	/** 属性值 */
	String ATTRIBUTES = "attributes";
	
	/** 洗练锁用户道具信息 */
	String LOCK_PROPS = "lockProps";
	
	/** 贡献值*/
	String DONATE = "donate";
	
	/** 历史贡献值*/
	String HIS_DONATE = "hisDonate";
	
	/** 金币 */
	String GOLDEN = "golden";

	/** 礼金 */
	String COUPON = "coupon";

	/** 银币 */
	String SILVER = "silver";
	
	/** 道具ID */
	String PROPS_ID = "propsId";
	
	/** 家将槽数*/
	String SOLTSIZE = "soltSize";
	
	/** 家将的ID集合*/
	String PET_IDS = "petIds";
	
	/** 家将的ID*/
	String PET_ID = "petId";
	
	/** 家将的成长*/
	String PET_GROW = "petGrow";
	
	/** 家将契合度祝福值*/
	String PET_MERGED_BLESS = "petMergedBless";
	
	/** 家将蛋的VO*/
	String EGG_VO = "eggVo";
	
	/** 副家将的ID*/
	String DPET_ID = "dpetId";
	
	/** 家将精力*/
	String ENERGY = "energy";
	
	/** 家将缓存键值*/
	String KEY = "key";
	
	/** 上下坐骑*/
	String RIDING = "riding";
	
	/** 坐骑对象*/
	String HORSE = "horse";
	
	/** 坐骑的外观*/
	String MOUNT = "mount"; 
	
	/** 坐骑速度*/
	String SPEED = "speed";
	
	/** 元宝幻化(true 高级,false一般)*/
	String FANCY = "fancy";
	
	/** 攻击者ID */
	String ATTACK_ID = "attackId";
	
	/** 攻击者类型 */
	String ATTACK_TYPE = "attackType";
	
	/** 角色ID列表 */
	String PLAYER_IDS = "playerIds";

	/** 封禁的时长 */
	String BLOCK_TIME = "blockTime";
	
	/** 起始页*/
	String PAGE_START = "pageStart";
	
	/** 分页大小*/
	String PAGE_SIZE = "pageSize" ;
	
	/** 当前页*/
	String PAGE_NOW = "pageNow" ;
	
	/** 数据*/
	String DATA = "data";
	
	/** 起始等级*/
	String LEVEL_BEGIN = "levelBegin" ;
	
	/** 结束等级*/
	String LEVEL_END = "levelEnd";
	
	/** 名字*/
	String NAME = "name";

	/** 模式 */
	String MODE = "mode";

	/** 称号编号*/
	String TITLE_ID = "titleId";
	
	/** 任务列表 */
	String TASKS= "tasks";
	
	/** 用户任务 */
	String USER_TASK = "userTask";
	
	/** 完成的信息 */
	String COMPLETES = "completes";
	
	/** 完成的次数 */
	String COMPLETE_TIMES = "completeTimes";
	
	/** 用户任务ID */
	String USER_TASK_ID = "userTaskId";
	
	/** 时间*/
	String TIME = "time";
	
	/** 状态*/
	String STATE = "state";
	
	/** HP */
	String HP = "hp";
	
	/** 摆摊物品自增Id */
	String MARKET_ITEM_ID = "itemId";
	
	String TARGET_QUALITY = "targetQuality";
	String REFRESH_TIMES = "refreshTimes";
	String AUTOBUY_REFRESHBOOK = "autoBuyRefreshBook";
	
	/** 是否使用物品 */
	String IS_USE_ITEM = "isUseItem";
	String USE_PROPS = "isUseProps";
	String AUTO = "auto";
	/** 暴击 */
	String CRITICAL = "critical";
	
	/** 经验*/
	String EXP = "exp";
	
	/** 增加的经验*/
	String ADD_EXP = "addExp";
	
	/** 升级数量*/
	String LEVEUP_COUNT = "leveUpCount";
	
	String SUCCESS_COUNT = "successCount";
	String FAILURE_COUNT = "failureCount";
	
	String RECEIVE_BLESS = "receive_bless";
	String IS_COLLECT = "isCollect";
	String USE_GOLD = "useGold";
	String USE_BOOKS = "useBooks";
	String LOOP_TASK_VO = "loopTaskVo";
	String PRACTICE_TASK_VO = "practiceTaskVo";
	
	/** 护送任务*/
	String ESCORTTASK = "escortTask";
	
	/** 刷新*/
	String FLUSHABLE = "flushable";
	
	/** 开始时间*/
	String START_TIME = "startTime";
	
	/** 总共耗时*/
	String TOTLE_TIME = "totleTime";
	
	/** 方向 */
	String DIRECTION = "direction";
	
	/** CDKEY */
	String CDKEY = "cdkey";
	
	/** 倍数 */
	String MULTIPLE = "multiple";
	
	/** 战斗力 */
	String FIGHT_CAPACITY = "fightCapacity";
	
	/** 邮件ID */
	String MAIL_IDS = "mailIds";
	
	/** 邮件ID */
	String MAIL_ID = "mailId";
	
	/** 神武开关 */
	String SHENWU_SWITCH = "shenwuSwitch";

	/** 神武值 */
	String SHENWU_TEMPO = "shenwuTempo";

	/** 神武属性 */
	String SHENWU_ATTRIBUTES = "shenwuAttributes";
	
	/** 品质*/
	String QUALITY = "quality";
	
	/** 礼包ID */
	String GIFT_ID = "giftId";
	
	/** 礼包VO对象*/
	String GIFT_VO = "giftVO";
	
	/** 真气值 */
	String GAS = "gas";
	
	/**  */
	String IDS = "ids";
	
	/** 日期*/
	String DATE = "date";
	
	/** 开始时间*/
	String startDate = "startDate";
	
	/** 结束时间*/
	String endDate = "endDate";
	
}
