package com.yayo.warriors.module.props.type;

/**
 * 道具类型
 * 
 * <pre>
 *  1.  药剂食物道具类型（回复消耗品）
 *  2.  装备镶嵌宝石类型(镶嵌类型) 
 *  3.  装备打造道具类型(打造道具. 宝石碎片) 
 *  4.  技能书道具类型(可以用于学习角色技能/宠物技能)
 *  5.  BUFF效果类型(使用该道具, 会获得一个BUFF, 这个BUFF会顶掉已存在的BUFF) 
 *  6.  宝箱道具类型(可以开启一些随机道具/掉落物品等) 
 *  7.  坐骑培养类型(用来实现坐骑模块的特殊业务)
 *  8.  角色培养道具(经脉、肉身等道具) 
 *  9.  任务道具类型 
 *  10. 家将类培养道具
 *  11. 家将令道具 
 *  12. 其他类型(根据特殊业务做特殊写死判断的, 按照子类型可以有多个等级来计算) 
 *  13. 家将类恢复品
 *  15. 礼包
 *  16. VIP卡
 * </pre>
 * 
 * @author Hyint
 */
public interface PropsType {

	/** 1. 药剂食物道具类型(回复消耗品) */
	int DRUG_PROPS_TYPE = 1;

	/** 2. 装备镶嵌宝石类型(镶嵌类型) */
	int ENCHANGE_PROPS_TYPE = 2;

	/** 3. 装备打造道具类型(打造道具. 宝石碎片) */
	int THINKEE_PROPS_TYPE = 3;
	
	/** 4. 技能书道具类型(可以用于学习角色技能/宠物技能) */
	int SKILL_PROPS_TYPE = 4;
	
	/** 5. BUFF效果类型(使用该道具, 会获得一个BUFF, 这个BUFF会顶掉已存在的BUFF) */
	int BUFFER_EFFECT_TYPE = 5;

	/** 6. 宝箱道具类型(可以开启一些随机道具/掉落物品等) */
	int CHEST_PROPS_TYPE = 6;

	/** 7. 坐骑培养类型(用来实现坐骑模块的特殊业务) */
	int AMOUNT_TRAIN_PROPS_TYPE = 7;

	/** 8. 角色培养道具(经脉、肉身等道具) */
	int PLAYER_TRAIN_PROPS_TYPE = 8;

	/** 9. 任务道具类型(该道具可以完成角色的某些任务) */
	int TASK_PROPS_TYPE = 9;
	
	/** 10. 家将类培养道具 */
	int MYRMIDON_TRAIN_TYPE = 10;
	
	/** 11. 家将令道具 */
	int MYRMIDON_TOKEN_TYPE = 11;
	
	/** 12. 其他类型(根据特殊业务做特殊写死判断的, 按照子类型可以有多个等级来计算) */
	int OTHER_PROPS_TYPE = 12;
	
	/** 13. 家将类恢复品 */
	int MYRMIDON_DRUG_TYPE = 13;
	
	/** 15. 礼包*/
	int GIFTS_TYPE = 15; 
	
}
