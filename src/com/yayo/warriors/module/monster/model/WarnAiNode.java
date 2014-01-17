package com.yayo.warriors.module.monster.model;


import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.monster.domain.MonsterDomain;

/**
 * 警戒AI，一直寻找可攻击的对象
 * @author 海明
 *
 */
public class WarnAiNode extends AbstractAiNode{
	
	@Override
	public boolean meetConditions(MonsterDomain monsterModel) {
		if(monsterModel.hasAttackTarget()){
			return EXECUTE_NEXT_AI;
		}
		monsterModel.findTargetPlayer();
		//存在可攻击的对象
//		if(monsterModel.hasAttackTarget()){
//			return EXECUTE_NEXT_AI ;
//		}
//		else{
//		//木有可攻击的对象
//			return EXECUTE_CURRENT_AI;
//		}
		return EXECUTE_NEXT_AI ;
	}

	@Override
	public void executeAi(MonsterDomain monsterModel) {
//		if(monsterModel.hasAttackTarget()){
//			return ;
//		}
//		//是否过了行动疲劳值（防止怪物不停乱走）
//		if(monsterModel.isOverTired(monsterModel.getClass())){
//			if(!monsterModel.hasRouteTarget()){
//				Point point = this.findRouteTarget(monsterModel);
//				monsterModel.setRouteTarget(point);
//				monsterModel.walkToTarget(WalkType.DIRECT);
//				if(monsterModel.isArrivalTarget()){
//				//当走动到目的地 添加疲劳值
//				monsterModel.addTired(MonsterAiDomain.WALK_CD_TIME, monsterModel.getMonsterFightConfig().getWalkDelay() );
//			}
//		}
	}
	
	/**
	 * 获取随机移动范围值
	 * @param patrolRange  范围值
	 * @return
	 */
	protected int getRandomRange(int patrolRange){
		int range = Tools.getRandomInteger(patrolRange * 2 + 1);
		return range - patrolRange;
	}
}
