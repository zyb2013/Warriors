package com.yayo.warriors.module.fight.parsers.effect.context;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.yayo.warriors.common.util.astar.DirectionUtil;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.util.MapUtils;

/**
 * 抽象的技能解析器
 * 
 * @author Hyint
 */
public abstract class AbstractSkillEffectParser implements SkillEffectParser {
	
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	@Autowired
	private SkillEffectContext context;

	@PostConstruct
	void init() {
		context.putParser(getType(), this);
	}

	/**
	 * 技能类型
	 * 
	 * @return {@link Integer}		返回值
	 */
	protected abstract int getType();
	
	/**
	 * 计算目标点. 目标点在 targetPoint的前一个格子
	 * 
	 * @param  sourcePoint			起始坐标点
	 * @param  targetPoint			终点坐标点
	 * @param  gameMap				地图对象
	 * @return {@link Point}		最终的坐标点
	 */
	public Point calcChargeAgainstPoint(Point sourcePoint, Point targetPoint, GameMap gameMap) {
//		BigMapConfig bigMap = resourceService.get(gameMap.getMapId(), BigMapConfig.class);
//		if(bigMap == null) {
//			return targetPoint;
//		}
		
//		Position p1 = MapUtils.getTilePositionToStage(sourcePoint.getX(), sourcePoint.getY(), bigMap);
//		Position p2 = MapUtils.getTilePositionToStage(targetPoint.getX(), targetPoint.getY(), bigMap);
//		int direction = DirectionUtil.calcDirection(p1.getX(), p1.getY(), p2.getX(), p2.getY());//计算出方向
		int direction = DirectionUtil.direction(targetPoint.x, targetPoint.y, sourcePoint.y, sourcePoint.y);
		int[] directionValue = DirectionUtil.getDirectionValue(direction);
		int finalX = targetPoint.getX() + directionValue[0];
		int finalY = targetPoint.getY() + directionValue[1];
		if(gameMap.isPathPass(finalX, finalY)) {
			return new Point(finalX, finalY);
		}
		return targetPoint;
	}

	/**
	 * 计算抓取目标点. 目标点在 sourcePoint的前一个格子
	 * 
	 * @param  sourcePoint			起始坐标点
	 * @param  targetPoint			终点坐标点
	 * @param  gameMap				地图对象
	 * @return {@link Point}		最终的坐标点
	 */
	public Point calcGarbbingHandPoint(Point sourcePoint, Point targetPoint, GameMap gameMap) {
//		BigMapConfig bigMap = resourceService.get(gameMap.getMapId(), BigMapConfig.class);
//		if(bigMap == null) {
//			return sourcePoint;
//		}
//		Position p1 = MapUtils.getTilePositionToStage(sourcePoint.getX(), sourcePoint.getY(), bigMap);
//		Position p2 = MapUtils.getTilePositionToStage(targetPoint.getX(), targetPoint.getY(), bigMap);
//		int direction = DirectionUtil.calcDirection(p1.getX(), p1.getY(), p2.getX(), p2.getY());//计算出方向
		int direction = DirectionUtil.direction(sourcePoint.x, sourcePoint.y, targetPoint.x, targetPoint.y);
		int[] directionValue = DirectionUtil.getDirectionValue(direction);
		int finalX = sourcePoint.getX() + directionValue[0];
		int finalY = sourcePoint.getY() + directionValue[1];
		if(gameMap.isPathPass(finalX, finalY)) {
			return new Point(finalX, finalY);
		}
		return sourcePoint;
	}

	/**
	 * 计算击退目标点
	 * 
	 * @param  sourcePoint			起始坐标点
	 * @param  targetPoint			终点坐标点
	 * @param  gameMap				地图对象
	 * @return {@link Point}		最终的坐标点
	 */
	public Point calcTargetKnockPoint(Point sourcePoint, Point targetPoint, GameMap gameMap) {
		int maxCursor = 4;
		Point newPoint = null;
		int sourceX = sourcePoint.getX();
		int sourceY = sourcePoint.getY();
		int targetX = targetPoint.getX();
		int targetY = targetPoint.getY();
		List<Point> distances = MapUtils.findExtendPoint(sourceX, sourceY, targetX, targetY, gameMap, maxCursor);
		if(distances != null && !distances.isEmpty()) {
			newPoint = distances.get( distances.size() - 1 );
		}

		newPoint = newPoint == null ? targetPoint : newPoint;
		int pointX = Math.max(0, newPoint.getX());
		int pointY = Math.max(0, newPoint.getY());
		return new Point(pointX, pointY);
	}
}
