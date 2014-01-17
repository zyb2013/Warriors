package com.yayo.warriors.module.monster.model;


import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.common.helper.MonsterHelper;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.campbattle.rule.CampBattleRule;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.type.WalkType;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 基础AI
 * @author 海明
 *
 */
public class BasicAiNode extends AbstractAiNode{

	@Override
	public boolean meetConditions(MonsterDomain monsterDomain) {
		if(monsterDomain == null){
			return EXECUTE_CURRENT_AI;
		}
		
		if(monsterDomain.getMonsterBattle().isDead()){
			monsterDomain.atHome();
			return EXECUTE_CURRENT_AI ;
		}
		
		if(monsterDomain.goHomeing()){
			//---------2012/7/17 屏蔽怪物回家途中不反击玩家(阵营战怪物除外)------------------------
			int attactdistance = monsterDomain.getCurrentUseSkillAttackDistance();
			GameMap gameMap = monsterDomain.getGameMap();
			if(attactdistance >= 2 && gameMap != null && gameMap.getMapId() == CampBattleRule.CAMP_BATTLE_MAPID){
				monsterDomain.redictAttackPlayer();
				if(monsterDomain.hasAttackTarget()){
					monsterDomain.atHome();
					monsterDomain.clearPath();
					return EXECUTE_NEXT_AI;
				}
			}
			return EXECUTE_CURRENT_AI ;
		}
		
		if(!monsterDomain.isOverTired(MonsterDomain.REVIVE_ACTION_DELAY)){
			return EXECUTE_CURRENT_AI;
		}
		
		if(monsterDomain.hasAttackTarget()){
			return EXECUTE_NEXT_AI;
		}
		
		if(monsterDomain.shouldGoHome()){
			monsterDomain.goHome();
		}
		
		if(monsterDomain.goHomeing()){
			return EXECUTE_CURRENT_AI ;
		}
		
		return EXECUTE_NEXT_AI;
	}

	@Override
	public void executeAi(MonsterDomain monsterDomain) {
		if(monsterDomain == null){
			return ;
		}
		IMonsterConfig monsterConfig = monsterDomain.getMonsterConfig();	//怪物基础数据
		MonsterBattle monsterBattle = monsterDomain.getMonsterBattle();	//怪物战斗属性
		
		MonsterFightConfig monsterFightConfig = monsterDomain.getMonsterFightConfig();
		if(monsterBattle.isDead()){
			monsterDomain.atHome();
			monsterDomain.clearPath();
			
			if(!monsterFightConfig.canRevive() && monsterDomain.isRemoveCorpse()){
				return ;
			}
			//处理尸体
			handleCorpse(monsterDomain);
			
			//准备复活怪物
			if(!monsterDomain.isPrepareResurrection() && monsterFightConfig.canRevive()){
				monsterDomain.prepareResurrection();
			}
			//复活怪物
			if(monsterDomain.isTimeToResurrection() && monsterFightConfig.canRevive()){
				monsterDomain.resurrection();
				
				MonsterHelper.refreshMonster(monsterDomain, monsterDomain.getCanWatchSpires() );
				MonsterHelper.changeMap(monsterDomain, monsterDomain.getGameMap(), monsterConfig.getBornX(), monsterConfig.getBornY());
			}
		}
		
		if(monsterDomain.goHomeing()){
			ISpire spire = monsterDomain.getCanAttackPlayer();
			if(spire != null){
				if(spire instanceof UserDomain){
					UserDomain userDomain = (UserDomain)spire ;
					if(userDomain.getBattle().isDead()){
						monsterDomain.removeAttackTarget();
					}
				}
			}
			
			Point point = new Point(monsterConfig.getBornX(), monsterConfig.getBornY());
			if(monsterDomain.isRouteTargetChange(point)){
				monsterDomain.setRouteTarget(point);
				monsterDomain.walkToTarget(WalkType.OPTIMIZE);
			}
			if(monsterDomain.checkIsAtHome()){
//				monsterAiDomain.clear();	//回到家里就满血了
				monsterDomain.fullHP();
				monsterDomain.addTired(MonsterDomain.WALK_CD_TIME, monsterFightConfig.getWalkDelay() );
			}
		}
		
	}
	
	/**
	 * 处理尸体
	 * @param monsterAiDomain
	 */
	private void handleCorpse(MonsterDomain monsterAiDomain){
		boolean removeCorpse = false;
		synchronized (monsterAiDomain) {
			if(monsterAiDomain.isRemoveCorpse()){
				return ;
			}
			
			//准备移除尸体
			if(!monsterAiDomain.isPrepareRemoveCorpse()){
				monsterAiDomain.prepareRemoveCorpse();
			}
			//移除尸体
			if(monsterAiDomain.needToRemoveCorpse() && monsterAiDomain.removeCorpse()){
				removeCorpse = true;
			}
		}
		
		if(removeCorpse){
//			System.err.println(String.format("移除怪物尸体, id:{%d}", monsterAiDomain.getId()));
			MonsterHelper.removeMoster(monsterAiDomain,monsterAiDomain.getCanWatchSpires());
//			if( GameConfig.getLimitPlayerMap(monsterAiDomain.getMapId()) == null ){
//				monsterAiDomain.setStopRun(true);	//停止运行此怪物
//			}
		}
	}

}
