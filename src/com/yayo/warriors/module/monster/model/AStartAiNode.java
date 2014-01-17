package com.yayo.warriors.module.monster.model;



import java.util.List;

import com.yayo.warriors.common.util.astar.DirectionUtil;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.util.MapUtils;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.type.WalkType;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * A*巡路
 * @author 海明
 *
 */
public class AStartAiNode extends AbstractAiNode{

	@Override
	public boolean meetConditions(MonsterDomain monsterAiDomain) {
		if(monsterAiDomain == null){
			return EXECUTE_CURRENT_AI;
		}
//		if(monsterAiDomain.hasRouteTarget()){
//			return EXECUTE_NEXT_AI ;
//		}
		//当有人打过我
		if(monsterAiDomain.getHurtInfo().size() > 0){
			//我就看看是谁打我的
			monsterAiDomain.redictAttackPlayer();
		}
		if(monsterAiDomain.hasAttackTarget()){
			ISpire target = monsterAiDomain.getCanAttackPlayer();
			GameScreen currentScreen = target.getCurrentScreen();
			GameMap gameMap = currentScreen != null ? currentScreen.getGameMap() : null; 
			//验证打我的人是不是和我是同一张地图
			if(monsterAiDomain.getGameMap() != gameMap) {
				monsterAiDomain.removeAttackTarget();
			}
			//验证怪物追击的距离
			if(MapUtils.checkPosScopeInfloat(target.getX(), target.getY(), monsterAiDomain.getX(),
					monsterAiDomain.getY(), monsterAiDomain.getMonsterFightConfig().getPursueRange() ) ){
				
				int attactdistance = monsterAiDomain.getCurrentUseSkillAttackDistance() ;
				if(attactdistance == -1){	//不在攻击范围内
//					System.err.println(String.format("时间[%d], 开始执行A星节点, 不在攻击距离[%d]内", System.nanoTime(), attactdistance));
					return EXECUTE_NEXT_AI;
				}
				
				//在攻击范围内
				if(MapUtils.checkPosScopeInfloat(target, monsterAiDomain, Math.max(attactdistance, 1) ) ){
//					synchronized (monsterAiDomain) {
//						monsterAiDomain.removeRouteTarget();
//					}
//					GameScreen gameScreen = monsterAiDomain.getGameMap().getGameScreen(monsterAiDomain);
//					if(monsterAiDomain.getRandomSteps() < MonsterAiDomain.maxRndSteps && gameScreen != null && MapUtils.isSpireStand(monsterAiDomain, gameScreen.getSpireCollection(ElementType.PLAYER, ElementType.MONSTER)) ){
//						monsterAiDomain.increaseRandomSteps();
//						return EXECUTE_CURRENT_AI;
//					}
					monsterAiDomain.clearPath();
					return EXECUTE_NEXT_AI ;
				}
				
				if(!monsterAiDomain.checkTargetPoint(target, 2)){
					return EXECUTE_CURRENT_AI;
				}
				
			} else {
				//超过追击范围就消除仇恨
				monsterAiDomain.removeAttackTarget();
				monsterAiDomain.getMonsterBattle().removeHurtInfo(target);
			}
		}
		return EXECUTE_NEXT_AI;
	}

	@Override
	public void executeAi(MonsterDomain monsterModel) {
		if(monsterModel == null){
			return ;
		}
		ISpire target = monsterModel.getCanAttackPlayer();
		if(target == null) {	//没有攻击目标直接返回
			return ;
		}
		if(monsterModel.shouldGoHome()){
			monsterModel.removeAttackTarget();
			monsterModel.atHome();
			return ;
		}
		if( monsterModel.checkTargetPoint(target, 2) ){
			return ;
		}
		if( monsterModel.getGameMap() != target.getGameMap()){
			monsterModel.removeAttackTarget();
			return ;
		}

		int attactdistance = monsterModel.getCurrentUseSkillAttackDistance() ;
		//检查是否到可攻击的距离
		if(!MapUtils.checkPosScopeInfloat(target, monsterModel, Math.max(attactdistance, 2))){
//				MonsterFightConfig monsterFightConfig = monsterModel.getMonsterFightConfig();
//				IMonsterConfig monsterConfig = monsterModel.getMonsterConfig();
//				int i = 0;
				Point aim = getNextPoint(monsterModel, target);
//				while(true){
//					aim = getNextPoint(target);
//					if(!MapUtils.checkPosScopeInfloat(monsterModel.getX(), monsterModel.getY(), monsterConfig.getBornX(), monsterConfig.getBornY(), monsterFightConfig.getPursueRange() ) ){
//						if(i++ > DirectionUtil.DELTA_X_RND.length){
//							break;
//						}
//					} else {
//						break;
//					}
//				}
				if(monsterModel.isRouteTargetChange(aim)){
//					monsterModel.setStopMoveAfterSteps(0);
//					System.err.println(String.format("时间[%d], 怪物[%d]A星行走到目标, 不在攻击距离[%d]内", System.nanoTime(), monsterModel.getId(), Math.max(attactdistance, 1)));
					monsterModel.setRouteTarget(aim);
//					List<Integer> path = monsterModel.getPath();
					monsterModel.walkToTarget(WalkType.ASTAR);
//					int maxPathLen = 7;
//					synchronized (path) {
//						if(path.size() > maxPathLen){
//							path.subList(maxPathLen, path.size() -1).clear();
//						}
//					}
//				if(monsterModel.getId() == 11246){
//					System.err.println(String.format("怪物[%d]在行走了", monsterModel.getId()));
//				}
				}
		}
	}
		
		//添加移动CD，因为很难确保前端怪物走动会不会与后台不同步
//		monsterModel.addMotionCd();
	
	private Point getNextPoint(MonsterDomain monsterModel, ISpire target){
		Point nextPoint = new Point(target.getX(), target.getY());
		if(target instanceof UserDomain){
			UserDomain userDomain = (UserDomain)target;
			List<Integer> path = userDomain.getMotion().getPath();
			synchronized (path) {
//				if(path.size() > 6){
//					nextPoint = new Point(path.get(4), path.get(5)) ;
//				}
				if(path.size() > 4){
					nextPoint = new Point(path.get(2), path.get(3)) ;
				}
				if(path.size() > 2){
					nextPoint = new Point(path.get(0), path.get(1)) ;
				}
			}
			Point rndPoint = DirectionUtil.getRandomPos(monsterModel.getX(), monsterModel.getY(), nextPoint, userDomain.getGameMap() );
			if(userDomain.getGameMap().isPathPass(rndPoint.x, rndPoint.y)){
				return rndPoint;
			}
		}
		return nextPoint;
	}
}
