package com.yayo.warriors.module.monster.model;


import java.util.List;

import com.yayo.common.utility.RandomUtil;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.util.MapUtils;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.type.WalkType;

/**
 * 走路AI，人不犯我、我不犯人
 * @author haiming
 *
 */
public class WalkAiNode extends AbstractAiNode{

	@Override
	public boolean meetConditions(MonsterDomain monsterAiDomain) {
		if(monsterAiDomain == null){
			return EXECUTE_CURRENT_AI;
		}
		if(monsterAiDomain.hasAttackTarget()){
			return EXECUTE_NEXT_AI ;
		}
		if(monsterAiDomain.getHurtInfo().size() > 0){
			monsterAiDomain.redictAttackPlayer();
		}
		if(monsterAiDomain.hasAttackTarget()){
			return EXECUTE_NEXT_AI ;
		}
		return EXECUTE_CURRENT_AI;
	}

	@Override
	public void executeAi(MonsterDomain monsterEntity) {
		if(monsterEntity == null){
			return ;
		}
		if(monsterEntity.hasAttackTarget()){
			return ;
		}
		//是否过了行动疲劳值（防止怪物不停乱走）
		if(monsterEntity.isOverTired(MonsterDomain.WALK_CD_TIME)){
			MonsterFightConfig monsterFightConfig = monsterEntity.getMonsterFightConfig();
			if(!monsterEntity.hasRouteTarget()){
				Point point = this.findRouteTarget(monsterEntity);
				monsterEntity.setRouteTarget(point);
				monsterEntity.walkToTarget(WalkType.DIRECT);
				//当走动到目的地 添加疲劳值
				monsterEntity.addTired(MonsterDomain.WALK_CD_TIME, monsterFightConfig.getWalkDelay() + Tools.getRandomInteger(5) * 1000 );
//				if(monsterEntity.getId() == 11246){
//					System.err.println(String.format("怪物[%d]在行走了", monsterEntity.getId()));
//				}
			}
		}
	}
	
	/**
	 * 获取巡逻路径
	 * @param x           怪物当前X轴坐标
	 * @param y           怪物当前Y轴坐标
	 * @param direction   巡逻方向(AiData上的参数)
	 * @param patrolRange 巡逻格子范围(AiData上的参数) 
	 * @param map         地图数据
	 * @return 方向
	 */
	protected Point findRouteTarget(MonsterDomain monsterModel){
		GameMap gameMap = monsterModel.getGameMap() ;
		if(gameMap == null){
			return null;
		}
		IMonsterConfig monsterConfig = monsterModel.getMonsterConfig();
		int WALK_RADIUS = monsterModel.getMonsterFightConfig().getPatrolRange();
		int bornX = monsterConfig.getBornX();
		int bornY = monsterConfig.getBornY();
		Point targetPoint = new Point(bornX + gameMap.getRandomRange( WALK_RADIUS ) , bornY + gameMap.getRandomRange( WALK_RADIUS ) ) ;
//		List<Point> pointRote = MapUtils.findAllPointByStraight(monsterModel.getX(), monsterModel.getY(), targetPoint.x, targetPoint.y, gameMap);
//		if(pointRote != null && pointRote.size() > 2){
//			return pointRote.get(pointRote.size() - 1 );
//		}
		if( gameMap.isPathPass(targetPoint.x, targetPoint.y) ){
			return targetPoint;
		}
		return null;
	}
	
}
