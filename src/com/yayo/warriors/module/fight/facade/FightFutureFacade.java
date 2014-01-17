package com.yayo.warriors.module.fight.facade;

import java.util.Set;

import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 战斗善后处理(处理经验, 任务, 等其他功能)
 * 
 * @author Hyint
 */
public interface FightFutureFacade {
	
	/**
	 * 处理角色死亡操作
	 * 
	 * @param userDomain		用户域模型
	 * @param attackerUnit		攻击者的战斗单位
	 */
	void processPlayerDead(UserDomain userDomain, UnitId attackerUnit);
	
	/**
	 * 处理战斗上下文的善后工作. 包括奖励, 任务, 经验等
	 * 	
	 * @param context			战斗上下文
	 */
	void executeFightContext(ISpire attackUnit, Context context);

	/**
	 * 处理怪物的伤害
	 * 
	 * @param  monsterDomain	怪物的域模型
	 * @param  attacker			攻击者
	 * @param  hurtHp			伤害值
	 */
	void processMonsterFightHurt(MonsterDomain monsterDomain, UnitId attacker, int hurtHp);
	
	/**
	 * 处理怪物死亡
	 * 
	 * @param iSpire			怪物域模型
	 * @param castId			释放技能的单位ID
	 * @param unitType			释放技能的单位类型
	 */
	void processMonsterDead(ISpire iSpire, long castId, int unitType, Set<Long> teamMembers);
}
