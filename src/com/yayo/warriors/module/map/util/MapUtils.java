package com.yayo.warriors.module.map.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.yayo.warriors.basedb.model.BigMapConfig;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.common.util.astar.DirectionUtil;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.map.model.Position;

/**
 * 地图工具类
 *
 */
public class MapUtils {
	
	/** 地图格子的宽度 */
	public static final int TILE_WIDTH = 60;
	/** 地图格子的一半宽度 */
	public static final int TITE_HALF_WIDTH = 30;
	/** 地图格子的一半高度 */
	public static final int TITE_HALF_HEIGHT = 15;
	
	
	/**
	 * 获得两个绝对坐标点之间的距离
	 * 
	 * @param  sourceX			起始X坐标点
	 * @param  sourceY			起始Y坐标点
	 * @param  targetX			目标X坐标点
	 * @param  targetY			目标Y坐标点
	 * @return 
	 */
	protected static double getDistance(int sourceX, int sourceY, int targetX, int targetY) {
		double powerX = Math.pow(Math.abs(sourceX - targetX), 2);
		double powerY = Math.pow(Math.abs(sourceY - targetY), 2);
		return Math.sqrt(powerX + powerY);
	}
	
	/**
	 * 检查位置之间的范围 (对范围扩大1.2倍)
	 * 
	 * @param  source			起始点坐标
	 * @param  target			目标点坐标
	 * @param  scope			是否在指定范围内
	 * @return {@link Boolean}	true-在指定范围内, false-不在指定范围内
	 */
	public static boolean checkPosScopeInfloat(int sourceX, int sourceY, int targetX, int targetY, int scope) {
		return checkPosScopeInfloat(sourceX, sourceY, targetX, targetY, scope, true);
	}
	
	public static boolean checkPosScopeInfloat(int sourceX, int sourceY, int targetX, int targetY, int scope, boolean isScale) {
		double powerX = Math.pow(Math.abs(sourceX - targetX), 2);
		double powerY = Math.pow(Math.abs(sourceY - targetY), 2);
		double sqrtPositions = Math.sqrt(powerX + powerY);
		return sqrtPositions <= (isScale ? (double)scope * 1.2 : scope);
	}
	
	/**
	 * 检查位置之间的范围 (对范围扩大1.2倍)
	 * 
	 * @param  ISpire			起始点坐标
	 * @param  ISpire			目标点坐标
	 * @param  scope			是否在指定范围内
	 * @return {@link Boolean}	true-在指定范围内, false-不在指定范围内
	 */
	public static boolean checkPosScopeInfloat(ISpire spire1, ISpire spire2, int scope) {
		if(spire1 == null || spire2 == null || scope < 0){
			return false;
		}
		return checkPosScopeInfloat(spire1.getX(), spire1.getY(), spire2.getX(), spire2.getY(), scope, true);
	}
	
	/**
	 * 检查位置之间的范围 
	 * 
	 * @param  ISpire			起始点坐标
	 * @param  ISpire			目标点坐标
	 * @param  scope			是否在指定范围内
	 * @return {@link Boolean}	true-在指定范围内, false-不在指定范围内
	 */
	public static boolean checkPosScopeInfloatNoScale(ISpire spire1, ISpire spire2, int scope) {
		if(spire1 == null || spire2 == null || scope < 0){
			return false;
		}
		return checkPosScopeInfloat(spire1.getX(), spire1.getY(), spire2.getX(), spire2.getY(), scope, false);
	}

	/**
	 * 计算两个坐标之间的距离
	 * @param sx
	 * @param sy
	 * @param tx
	 * @param ty
	 * @return
	 */
	public static int calcDistance(int sx ,int sy , int tx , int ty ){
		double powerX = Math.pow(Math.abs(sx - tx), 2);
		double powerY = Math.pow(Math.abs(sy - ty), 2);
		return (int)Math.sqrt(powerX + powerY);
	}
	
	/**
	 * 是否有精灵站立在目标点
	 * @param tx
	 * @param ty
	 * @param spires
	 * @return
	 */
	public static boolean isSpireStand(int tx, int ty, Collection<ISpire> spires){
		if(spires != null){
			for(ISpire spire : spires){
				if(spire.getX() == tx && spire.getY() == ty){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 是否有精灵站立在目标点
	 * @param tx
	 * @param ty
	 * @param spires
	 * @return
	 */
	public static boolean isSpireStand(ISpire target, Collection<ISpire> spires){
		if(spires != null){
			for(ISpire spire : spires){
				if(target != spire && spire.getX() == target.getX() && spire.getY() == target.getY()){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 移动增量计算（以步长折分）
	 * @param startPoint 起点
	 * @param endPoint   终点
	 * @param stepLength 每步步长
	 */
//	public static Point moveIncrement(Point startPoint, Point endPoint, int step )
//	{
//		int seDistance       = Point.distance(startPoint, endPoint); 	// startPoint 到 endPoint 的距离
//		int scaleStepLength  = step / seDistance;
//		int x                = (endPoint.x - startPoint.x) * scaleStepLength;
//		int y                = (endPoint.y - startPoint.y) * scaleStepLength;
//		
//		return new Point(x,y);
//	}
	
	/**
	 * 路径中是否有障碍 
	 * @param animal	当前人物
	 * @param current	当前准备行走到的目标点
	 * @return true 为有障碍
	 * 
	 */			
	public static boolean hasBarrier(ISpire spire, Point current)
	{
//		var scene  : GameScene = GameScene(animal.parent.parent);
//		int step       = Tile.TITE_HALF_HEIGHT;
//		Point start  : Point     = new Point(animal.originX, animal.originY);
//		Point end         = Tile.getTilePointToStage(current.x, current.y);
//		Point source      = end.clone();
//		int distance		   = Point.distance(start, end);
//		
//		Point increment		   = Vector2Extension.moveIncrement(start, end, step);
//		increment.x			   = -increment.x;
//		increment.y			   = -increment.y;
//		int moveDistance	   = 0;
//		
//		var verificationPoint : Point;
//		while (moveDistance < distance)
//		{
//			moveDistance += step;
//			source = source.add(increment);
//			verificationPoint = Tile.getTileStageToPoint(source.x, source.y);
//			
//			if (scene.map && scene.map.getTargetTile(verificationPoint.x, verificationPoint.y) == Tile.PATH_BARRIER)
//			{
//				return true;
//			}
//		}
		return false;
	}

	
	/**
	 * 获取45度A*单元格矩阵坐标
	 * 
	 * @param  px    				目标点X坐标
	 * @param  py    				目标点Y坐标
	 * @return {@link Position}     矩阵点坐标
	 */
	public static Position getTileStageToPosition(int px, int py, BigMapConfig bigMapConfig) {
    	//界面坐标 计算以屏幕左上为原点的世界坐标
		int dataTempy = px  - py * 2;
		if(dataTempy < 0) {
			dataTempy -= TILE_WIDTH;
		}
		
		int dataTempx  = py * 2 + px;
		int dataTempx1 = (dataTempx + TITE_HALF_WIDTH) / TILE_WIDTH;
		int dataTempy1 = (dataTempy + TITE_HALF_WIDTH) / TILE_WIDTH;
		return Position.valueOf(bigMapConfig.getOffsetX() + dataTempx1, bigMapConfig.getOffsetY() + dataTempy1);
    }
	
	/**
	 * 获取45度A*单元格矩阵坐标转舞台从标（获得的是格子的中心点坐标）
	 * @param  px    				舞台X坐标
	 * @param  py    				舞台Y坐标
	 * @return {@link Position}     矩阵点坐标
	 */
	public static Position getTilePositionToStage(int px, int py, BigMapConfig bigMapConfig) {
		int nOffX = px - bigMapConfig.getOffsetX();		
		int nOffY = py - bigMapConfig.getOffsetY();
		int positionX = nOffX * TITE_HALF_WIDTH  + nOffY * TITE_HALF_WIDTH;  // 斜坐标 x每加1   竖坐标x+1/2  y+1/2
		int positionY = nOffX * TITE_HALF_HEIGHT - nOffY * TITE_HALF_HEIGHT; // 斜坐标 y每加1   竖坐标x+1/2  y-1/2
		return Position.valueOf(positionX, positionY);
    }
	
	/**
	 * 获取地图可视区域的删除区域
	 * @param oldviews
	 * @param newviews
	 * @return 
	 */
	public static Collection<Integer> comparableRemove(Collection<Integer> oldviews,Collection<Integer> newviews){
		Collection<Integer> removeViews = new ArrayList<Integer>();
		for(Integer view : oldviews){
			if(!newviews.contains(view)){
				removeViews.add(view);
			}		
		}
		return removeViews;
	}
	
	/**
	 * 获取地图可视区域的新增区域
	 * @param oldviews
	 * @param newviews
	 * @return
	 */
	public static Collection<Integer> comparableAdd(Collection<Integer> oldviews,Collection<Integer> newviews){
		Collection<Integer> addViews = new ArrayList<Integer>();
		for(Integer view : newviews){
			if(!oldviews.contains(view)){
				addViews.add(view);
			}		
		}
		return addViews;
	}
	
	/**
	 * 怪物走直线
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static List<Point> findAllPointByDistance(int x1 , int y1 , int x2 , int y2, GameMap gameMap) {
		List<Point> list = new ArrayList<Point>();
		BigMapConfig bigMap = NoticePushHelper.getConfig(gameMap.getMapId(), BigMapConfig.class);
		if(bigMap == null) {
			return list;
		}
		
		list.add(new Point(x1, y1));
		int tx = x1;
		int ty = y1;
		int step = 0;
		do {
			if(step++ > 50){
				break;
			}
			int direction = DirectionUtil.direction(tx, ty, x2, y2);	//计算出方向
			int[] directionValue = DirectionUtil.getDirectionValue(direction);
			if(directionValue[0] == 0 && directionValue[1] == 0){
				continue;
			}
			tx = tx + directionValue[0];
			ty = ty + directionValue[1];
			if(!gameMap.isPathPass(tx, ty)) {
				break;
			}
//			System.err.println(String.format("source:[%d,%d] target:[%d,%d] t:[%d,%d] direction:%d offset:%s", x1, y1, x2, y2, tx, ty, direction, Arrays.toString(directionValue) ) );
			
			list.add(new Point(tx, ty));
			if(tx == x2 && ty == y2){
				break;
			}
			
		} while(true);
		
//		System.err.println(String.format("source:[%d,%d] target:[%d,%d] End:[%d,%d] size:%d path:%s", x1, y1, x2, y2, list.get(list.size() -1).x, list.get(list.size() -1).y, list.size(), Arrays.toString( list.toArray() ) ) );
		
		return list;
	}
	
	/** 直接路径所有坐标点  */
	public static List<Point> findAllPointByStraight(int x1 , int y1 , int x2 , int y2, GameMap gameMap) {
		List<Point> list = new ArrayList<Point>();
		if(x1 == x2 && y1 == y2){
//			return list;
		}else if (x1 == x2) {
			int step = (y2 > y1) ? 1 : -1;
			for(int y = y1; y != y2; y += step){
				if(!gameMap.isPathPass(x2, y)){
					break;
				}
				list.add(new Point(x2, y));
			}
			list.add(new Point(x2, y2));
    
		} else if (y1 == y2) {
			int step = (x2 > x1) ? 1 : -1;
			for(int x = x1; x != x2; x += step){
				if(!gameMap.isPathPass(x, y2)){
					break;
				}
				list.add(new Point(x, y2));
			}
			list.add(new Point(x2, y2));
    
		} else {
			if( Math.abs(x2-x1) == Math.abs(y2-y1) ){
				int xStep = (x2 > x1) ? 1 : -1;
				int yStep = (y2 > y1) ? 1 : -1;
				int xx = x1, yy = y1;
				while(true){
					if(xx == x2 && yy == y2){
						list.add( new Point(xx, yy) );
						break;
					}
					if(!gameMap.isPathPass(xx, yy)){
						break;
					}
					list.add( new Point(xx, yy) );
					xx += xStep;
					yy += yStep;
				}
			}
		}
	      
		return list ;
	}
	
	/**
	 * 向指定坐标延伸几个格子. 返回的列表, 从当前坐标点开始, 到一直眼神的目标的格数总数列表
	 * 
	 * @param  x1			起始X坐标
	 * @param  y1			起始Y坐标	
	 * @param  x2			结束X坐标
	 * @param  y2			结束Y坐标
	 * @param  gameMap		地图对象
	 * @param  times		计算的格子数			
	 * @return {@link List}	坐标点列表
	 */
	public static List<Point>  findExtendPoint(int x1 , int y1 , int x2 , int y2, GameMap gameMap, int times){
//		List<Point> points = new ArrayList<Point>();
//		Position p1 = getTilePositionToStage(x1, y1, bigMap);
//		Position p2 = getTilePositionToStage(x2, y2, bigMap);
//		final int direction = DirectionUtil.calcDirection(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		final int direction = DirectionUtil.direction(x1, y1, x2, y2);
		int[] directionValue = DirectionUtil.getDirectionValue(direction);
//		points.add(new Point(x2, y2));
		int targetX = x2, targetY = y2;
		for (int index = 1; index <= times; index++) {
//			points.add(new Point(x2 + directionValue[0] * index, y2 + directionValue[1] * index));
			targetX += directionValue[0];
			targetY += directionValue[1];
		}
		return findAllPointByDistance(x1, y1, targetX, targetY, gameMap);
	}
	
	public static int calcInterval(int x, int y, int x2, int y2) {
		int fx = x - x2 ;
		int fy = y - y2 ;
		if(fx <= 0 && fy >= 0){
			return 1 ;
		}else if(fx > 0 && fy > 0){
			return 2 ;
		}else if(fx < 0 && fy < 0){
			return 3 ;
		}else if(fx > 0 && fy < 0){
			return 4 ;
		}
		return 0;
	}
	
	public static void main(String[] args) {
//		List<Point> ps = findAllPointByStraight(12,19,12,2);
//		System.out.println(ps.toString());
//		ps = findAllPointByStraight(20,19,12,19);
//		System.out.println(ps.toString());
//		ps = findAllPointByStraight(12,12,17,17);
//		System.out.println(ps.toString());
//		ps = findAllPointByStraight(17,12,12,17);
//		System.out.println(ps.toString());
//		ps = findAllPointByStraight(1,1,5,5);
//		System.out.println(ps.toString());
//
//		ps = findAllPointByStraight(144, 61, 137, 67);
//		System.out.println(ps.toString());
		
	}
}
