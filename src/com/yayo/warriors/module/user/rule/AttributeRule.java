package com.yayo.warriors.module.user.rule;

import static com.yayo.warriors.module.user.type.AttributeKeys.*;

/**
 * 角色属性数组列表定义类
 * 
 * @author Hyint
 */
public class AttributeRule {

	//  增加了角色的当前经验和最大经验---超平
	//  增加角色的vip信息    ---超平
	/** 登陆的角色信息集合 */
	public static final Object[] HEROVO_INFO = { NAME, HP, MP, HP_MAX, MP_MAX, GAS, GAS_MAX, EXP, EXP_MAX, HP_BAG, MP_BAG, PET_HP_BAG,
												 MOVE_SPEED, LEVEL, ALLIANCE_NAME, TITLE, RANK_TITLE, CLOTHING, GUIDE_INFO, ONLINE_TIMES,
												 WEAPON_FOOT, WEAPON_RIDE, MOUNT, RIDE, BACKPACK_SIZE, VIP_INFO, PLAYER_RECEIVE_INFO, INDULGE_STATE,
												 TEAM_ID, GOLDEN, SILVER, JOB, STORAGE_SIZE,FIGHT_MODE,CAMP,ALLIANCE_ID,ALLIANCE_NAME,FASHION_EQUIP_VIEW,
												 USER_PET_MERGED,PET_SLOT_SIZE, CAPACITY, COUPON, CREATE_TIME };
	
//	/** 角色属性不需要乘以1000的Attribute值 */
//	public static final Object[] ATTRIBUTES = { ATTACK_INC_RATIO, DAMAGE_DEC_RESIST, DEFENSE_DEC_RESIST, 
//												DAMAGE_REFLECT, DEFENSE_INC_RATIO, CRITICAL_DEC_RESIST, 
//												HUNTING_DEC_DAMAGE_RESIST, HUNTING_HIT_RESIST, 
//												JIUYANG_SKILL_RESIST, WUDANG_CRITICAL_RESIST, 
//												MINGJIAO_CRITICAL_RESIST, EMEI_CRITICAL_RESIST, 
//												WUDU_CRITICAL_RESIST };
	/** NPC的属性参数 */
	public static final Object[] NPC_PARAMS = {HP_MAX,HP, MP_MAX, MP};
	
	/** 推送角色的经验 */
	public static final Object[] PLAYER_EXP = { EXP, EXP_MAX, LEVEL };

	/** 换装推送的信息 */
	public static final Object[] CHANGE_FATION_VIEW = { CLOTHING, FASHION_EQUIP_VIEW };
	/** 任务奖励 */
	public static final Object[] TASK_EXP_ARR = { EXP, EXP_MAX, LEVEL, SILVER, GAS };

	/** 日环任务推送信息 */
	public static final Object[] LOOP_TASK_EXP_ARR = { EXP, EXP_MAX, LEVEL, SILVER, GAS, GAS_MAX };

	/** 怪物BUFF过期推送的信息 */
	public static final Object[] MONSTER_BUFFER_TIMEOUT_PARAMS = { HP_MAX, HP, MP_MAX, MP, MOVE_SPEED };

	/** 推送角色BUFF过期 */
	public static final Object[] PLAYER_BUFFER_TIMEOUT_PARAMS = { HP_MAX, HP, MP_MAX, MP, GAS_MAX, GAS, MOVE_SPEED };

	/** 地下城任务奖励 */
	public static final Object[] DUNGEON_TASK_EXP_ARR = { GOLDEN, GAS, GAS_MAX, EXP, EXP_MAX, LEVEL, SILVER };

	/** 提升技能等级需要推送的属性 */
	public static final Object[] LEARN_SKILL_ARRAY = { SILVER, GAS, GAS_MAX, HP, HP_MAX, MP, MP_MAX, };
	
	/** 动物的属性参数 */
	public static final Object[] MONSTER_PARAMS = { NAME, LEVEL, HP_MAX, HP, MP_MAX, MP, 
													ICON, CLOTHING,MOVE_SPEED,BASE_ID,CAMP, MONSTER_CONFIG_ID };
	
	/** 角色的属性参数 */
	public static final Object[] PLAYER_PARAMS = { NAME, LEVEL, HP_MAX,HP, MP_MAX,MP, ICON, 
												   CLOTHING, WEAPON_FOOT, WEAPON_RIDE, MOUNT, VIP_INFO, 
												   TITLE, RANK_TITLE, ALLIANCE_NAME, MOVE_SPEED,RIDE,FIGHT_MODE,
												   TRAIN_STATUS, JOB,CAMP,ALLIANCE_ID,ALLIANCE_NAME,EQUIP_BLINK,
												   FASHION_EQUIP_VIEW,USER_PET_MERGED,PET_SLOT_SIZE};
	
	/** 家将的属性参数*/
	public static final Object[] PET_PARAMS = {BASE_ID,LEVEL,HP,HP_MAX,QUALITY,PET_STATUS};
	
	/** 角色的外观属性 */
	public static final Object[] PLAYER_MODEL_PARAMS = { CLOTHING, WEAPON_FOOT, WEAPON_RIDE, MOUNT , FASHION_EQUIP_VIEW};
	
	/** 区域玩家可以看到的变化值 */
	public static final Object[] AREA_MEMBER_VIEWS_PARAMS = { HP, MP, HP_MAX, MP_MAX, LEVEL };

	/** 玩家可以看到的变化值 */
	public static final Object[] PUSH_MONEY_AND_HPMP_AREA = { HP, MP, HP_MAX, MP_MAX, LEVEL, SILVER, GOLDEN };

	/** 区域玩家可以看到的变化值, 包括战斗模式 */
	public static final Object[] AREA_MEMBER_VIEWS_PARAMS_MODE = { HP, MP, HP_MAX, MP_MAX, FIGHT_MODE };
	
	/** 组队信息属性值 */
	public static final Object[] TEAM_ATTR_PARAMS = { HP, MP, HP_MAX, MP_MAX, LEVEL, NAME, SERVER_ID, JOB, ICON };
	
	/** 战斗, 成员升级等, 推送的组队信息属性值 */
	public static final Object[] TEAM_ATTRIBUTE_CHANGES = { HP, MP, HP_MAX, MP_MAX, LEVEL };
	
	/** 角色战斗属性Key */
//	public static final Object[] ATTRIBUTE_KEYS = { HP_MAX, MP_MAX, GAS_MAX, HP, MP, GAS, DODGE, ATTACK, DEFENSE, 
//												    CRITICAL, MOVE_SPEED, ATTACK_SPEED, ATTACK_INC_RATIO, DEFENSE_INC_RATIO, 
//												    DAMAGE_REFLECT, DAMAGE_DEC_RESIST, DEFENSE_DEC_RESIST, HUNTING_HIT_RESIST, 
//												    CRITICAL_DEC_RESIST, JIUYANG_SKILL_RESIST, EMEI_CRITICAL_RESIST, LEVEL,
//												    WUDU_CRITICAL_RESIST, WUDANG_CRITICAL_RESIST, HUNTING_DEC_DAMAGE_RESIST, 
//												    MINGJIAO_CRITICAL_RESIST};
	
	/** 角色战斗属性key集合 */
	public static final Object[] ATTRIBUTE_KEYS = {STRENGTH,DEXERITY,INTELLECT,CONSTITUTION,SPIRITUALITY,
		HIT,DODGE,MOVE_SPEED,THEURGY_ATTACK,THEURGY_DEFENSE,THEURGY_CRITICAL,PHYSICAL_ATTACK,PHYSICAL_DEFENSE,PHYSICAL_CRITICAL,
		PHYSICAL_ATTACK_RATIO,THEURGY_ATTACK_RATIO,PHYSICAL_DEFENSE_RATIO,THEURGY_DEFENSE_RATIO,HPMAX_RATIO,MPMAX_RATIO,PHYSICAL_CRITICAL_RATIO,THEURGY_CRITICAL_RATIO,HIT_RATIO,DODGE_RATIO,
		PIERCE,BLOCK,RAPIDLY,DUCTILITY};
	
	/** 上下装模型改变 */
	public static final Object[] DRESS_ATTRIBUTE_CHANGES = { HP, HP_MAX, MP, MP_MAX, CLOTHING, WEAPON_FOOT, WEAPON_RIDE, MOUNT , FASHION_EQUIP_VIEW };
	
	/** 管理后台能修改的角色属性 */
	public static final Object[] ADMIN_UPDATE_PLAYER_ATTR = {GOLDEN, SILVER, BACKPACK_SIZE, TITLE, ICON, NAME, CAMP,    
															EXP, JOB, LEVEL, HP, MP, HP_MAX, MP_MAX};
	/** 升级/上下装推送的属性变化信息 */
	public static final Integer[] LEVELUP_DRESS_PUSHINFO = { PHYSICAL_ATTACK, THEURGY_ATTACK, PHYSICAL_DEFENSE, 
															THEURGY_DEFENSE, PHYSICAL_CRITICAL, THEURGY_CRITICAL, 
															HIT, DODGE,	PIERCE,	BLOCK, RAPIDLY,	DUCTILITY };
	
	/** 活动奖励的变化信息*/
	public static final Object[] ACTIVE_PLAYER_ATTR = {COUPON,SILVER,EXP};
	
	/** 充值礼包属性 */
	public static final Object[] RECHARGE_GIFT_ATTRIBUTES = {SILVER, COUPON, EXP, EXP_MAX, LEVEL};
}
