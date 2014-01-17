package com.yayo.warriors.module.fight.facade;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.fight.model.FightEvent;
import com.yayo.warriors.type.ElementType;

/**
 * 战斗接口
 * 
 * @author Hyint
 */
public interface FightFacade {

	/**
	 * 角色主动发起战斗
	 * 
	 * @param  playerId								主动发起的玩家ID
	 * @param  targetId								被攻击者的ID
	 * @param  unitType   							被攻击者的类型
	 * @param  skillId								释放的技能ID
	 * @param  positionX							指定的X坐标
	 * @param  positionY							指定的Y坐标
	 * @return {@link ResultObject<FightEvent>}		战斗模块返回值
	 */
	ResultObject<FightEvent> playerFight(long playerId, long targetId, 
			int unitType, int skillId, int positionX, int positionY);
	
	/**
	 * 家将攻击
	 * 
	 * @param  playerId								角色ID
	 * @param  attackId								攻击者ID
	 * @param  targetId								被攻击者ID
	 * @param  targetType							被攻击者的类型
	 * @param  skillId								使用的技能ID
	 * @param  positionX							指定的X坐标
	 * @param  positionY							指定的Y坐标
	 * @return {@link ResultObject}					返回值信息
	 */
	ResultObject<FightEvent> userPetFight(long playerId, long attackId, long targetId,
							int targetType, int skillId, int positionX, int positionY);

	/**
	 * 怪物攻击玩家. 根据技能ID攻击, 如果是AOE的话, 会根据x, y来遍历玩家
	 * 
	 * @param  monsterId							怪物ID
	 * @param  targetId								被攻击者的ID
	 * @param  unitType								战斗单位类型
	 * @param  xPoint								释放技能的X坐标点
	 * @param  yPoint								释放技能的Y坐标点
	 * @param  skillId								使用的技能ID
	 * @return {@link Integer}						战斗模块返回值
	 */
	int monsterFight(long monsterId, long targetId, ElementType unitType, int xPoint, int yPoint, int skillId);
	
	/**
	 * 更新角色的跳跃信息
	 * 
	 * @param playerId								角色ID
	 */
	void updatePlayerJumpInfo(long playerId);
}
