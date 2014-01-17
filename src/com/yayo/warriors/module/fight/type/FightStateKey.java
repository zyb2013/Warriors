package com.yayo.warriors.module.fight.type;




/**
 * 战斗的属性Key
 * 
 * @author Hyint
 */
public enum FightStateKey {
	
	/** 
	 * 战报列表. 
	 * 
	 * {@link List<FightReport>} 
	 */
	FIGHT_REPORTS,
	
	/** 
	 * 战斗中发生变更的属性. 
	 * 
	 * {@link Map<UnitId, Set<Integer>>}
	 */
	FIGHT_ATTR_CHANGES,
	
	/** 
	 * 攻击者属性值变化 
	 * 
	 * Map<Integer, Integer>
	 */
	ATTACK_ATTRVALUE_CHANGES,

	/** 
	 * 被攻击者属性值变化 
	 * 
	 * Map<Integer, Integer>
	 */
	TARGET_ATTRVALUE_CHANGES,
	
	/** 
	 * 战斗中发生变更的BUFF. 
	 * 
	 * 
	 * {@link Map<UnitId, List<ChangeBuffer>>}  
	 */
	FIGHT_CHANGE_BUFFERS,
	
	/**
	 * 战斗中变换的状态.
	 * 
	 * {@link Map<UnitId, List<ChangeStatus>>}
	 */
	FIGHT_CHANGE_STATUS,
	
	/**
	 * 触发了溅射的信息
	 * 
	 * {@link List<SpotterInfo>}
	 */
	FIGHT_TRIGGER_SPOTTER,
	
	/** 
	 * 战斗触发连击(触发连击的技能ID), 该技能只能触发一次
	 * 
	 * {@link Integer}
	 */
	FIGHT_TRIGGET_COMBO,
	
	/** 
	 * 被攻击的目标ID列表 
	 */
	FIGHT_TARGET_IDLIST,
	
	/** 
	 * 战斗技能的X坐标点 
	 * {@link Integer} 
	 */
	FIGHT_SKILL_POSITION_X,

	/** 
	 * 战斗技能的Y坐标点 {@link Integer} 
	 */
	FIGHT_SKILL_POSITION_Y,
}
