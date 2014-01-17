package com.yayo.warriors.common.util.astar;

import com.yayo.common.utility.RandomUtil;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.map.domain.GameMap;

/**
 * 坐标系方向(九宫格序列 八个方向)
 * 注解:该计算的方式是来源于小键盘上面的数字键
 * 1 2 3 4 5(自己所在位置) 6 7 8 9 的方向来进行计算 
 * 其中忽略5的位置,5的位置是自己所在位置
 * 目前算法采用的是地图偏斜45度的方式进行计算的方式
 * @author liuyuhua
 *
 */
public class DirectionUtil {
	
//	/**坐标计算 X坐标系 (斜切) 偏移值*/
//	protected final static Integer[] DELTA_X = { 0,  1,  1,  -1, 0,  1, -1, -1, 0};
//	
//	/**坐标计算 Y坐标系 (斜切) 偏移值*/
//	protected final static Integer[] DELTA_Y = {-1, -1,  0,  -1, 0,  1,  0,  1, 1};

	/**坐标计算 X坐标系 (正切) 偏移值*/
//	protected final static int[] DELTA_X = { -1,  0,  1,  -1, 0, 1, -1, 0, 1};
	
	/**坐标计算 Y坐标系 (正切) 偏移值*/
//	protected final static int[] DELTA_Y = { 1 ,  1,  1,   0, 0, 0, -1,-1, -1};
	
	private static byte[] DELTA_X = {0,  0,  1, 1, -1, 0, 1, -1, -1, 0, 0};
	private static byte[] DELTA_Y = {0, -1, -1, 0, -1, 0, 1,  0,  1, 1, 0};
	
	private static byte[] OFFSET_X = {0, -1, -1,  1,  0, 0, 0, -1,  0, 1, 0};
	private static byte[] OFFSET_Y = {0, -1,  0, -1, -1, 0, 1,  1,  1, 1, 0};
	
	/**
	 * 获取方向XY偏移值
	 * @param direction 方向
	 * @return
	 */
	public static int[] getDirectionValue(int direction){
		try{
			return new int[]{DELTA_X[direction],DELTA_Y[direction]};
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * 通过正切值获取向量朝向代号（方向代码和小键盘的数字布局一样-8：上、４：左、６：右、２：下等）
	 * @param targetX  目标点的X值, A*坐标
	 * @param targetY  目标点的Y值, A*坐标
	 * @param currentX 当前点的X值, A*坐标
	 * @param currentY 当前点的Y值, A*坐标
	 */	
	@Deprecated
	public static int calcDirection(int sourceX, int sourceY, int targetX, int targetY) {
		int tan = -1;
		try {
			tan = (targetY - sourceY) / (targetX - sourceX);
		} catch (Exception e) { //异常, 则是重合, 随机一个方向回去
			if(targetX == targetY) {
				int rdn = Tools.getRandomInteger(9) + 1;
				return rdn == 5 ? 9 : rdn; // 不能随机出当前位置
			}
		}
		
		int absTan = Math.abs(tan);
		int tan8Percent = (int)Math.tan(Math.PI / 8);
		int tan8muilt3Percent = (int)Math.tan(Math.PI * 3 / 8);
		if (absTan >= tan8muilt3Percent && targetY <= sourceY) {
			return 8;
		} else {
			if (absTan > tan8Percent && absTan < tan8muilt3Percent && targetX > sourceX && targetY < sourceY) {
				return 9;
			} else if (absTan <= tan8Percent && targetX >= sourceX) {
				return 6;
			} else if (absTan > tan8Percent && absTan < tan8muilt3Percent && targetX > sourceX && targetY > sourceY) {
				return 3;
			} else if (absTan >= tan8muilt3Percent && targetY >= sourceY) {
				return 2;
			} else if (absTan > tan8Percent && absTan < tan8muilt3Percent && targetX < sourceX && targetY > sourceY) {
				return 1;
			} else if (absTan <= tan8Percent && targetX <= sourceX) {
				return 4;
			} else if (absTan > tan8Percent && absTan < tan8muilt3Percent && targetX < sourceX && targetY < sourceY) {
				return 7;
			} else {
				int rdn = Tools.getRandomInteger(9) + 1;
				return rdn == 5 ? 9 : rdn; // 不能随机出当前位置
			}
		}
	}
	
	/** 通过A*格获取移动方法  */
	public static int direction(int startX, int startY, int endX, int endY)
	{
		if (startX < endX)
		{
			if (startY < endY)
			{
				return 6;
			}
			else if (startY > endY)
			{
				return 2;
			}
			else
			{
				return 3;
			}
		}
		else if (startX > endX)
		{
			if (startY < endY)
			{
				return 8;
			}
			else if (startY > endY)
			{
				return 4;
			}
			else
			{
				return 7;
			}
		}
		else
		{
			if (startY < endY)
			{
				return 9;
			}
			else if (startY > endY)
			{
				return 1;
			}
		}
		return 0;
	}
	
	/** 获取指定点方法下一点A*格坐标  */
	public static Point getRandomPos(int sx, int sy, Point targetPoint, GameMap gamMap)
	{
		Point point = new Point();
		int direction = DirectionUtil.direction(sx, sy, targetPoint.x, targetPoint.y);
		int i = 0;
		int patrolRange = 3;
		while(true){
			int rndDirection = direction + Tools.getRandomInteger(patrolRange * 2 + 1) - patrolRange;
			if(rndDirection < 0){
				rndDirection = (rndDirection + 10) % 10;
			} else if(rndDirection > 9){
				rndDirection = (rndDirection) % 10;
			}
			int step = Tools.getRandomInteger(3);
			int offsetX = DELTA_X[rndDirection] * step;
			int offsetY = DELTA_Y[rndDirection] * step;
			if(offsetX != 0 || offsetY != 0 ){
				point.x = targetPoint.x + offsetX;
				point.y = targetPoint.y + offsetY;
				if(gamMap.isPathPass(point.x, point.y)){
					targetPoint = point;
					break;
				}
			}
			if(i++ > 5){
				break;
			}
			
		}
		return targetPoint;
	}
	
	/**
	 * 获取 X轴偏移值
	 * @param direction 方向
	 * @return
	 */
	public static int getDeltaX(int direction){
		return DELTA_X[direction];
	}
	
	/**
	 * 获取 Y轴偏移值
	 * @param direction 方向
	 * @return
	 */
	public static int getDeltaY(int direction){
		return DELTA_Y[direction];
	}
	
	/**
	 * 获取X方向偏移值
	 * @param x          X坐标
	 * @param direction  方向
	 * @param times      位移次数
	 * @return
	 */
	public static int getDirectionValueX(int x,int direction,int times){
		try{
			int jsX = x;
			for(int i = 0 ; i < times ; i++){
				jsX += OFFSET_X[direction];
			}
			return  jsX;
		}catch(Exception e){
			return 0;
		}
	}
	
	/**
	 * 获取Y方向偏移值
	 * @param y          y坐标
	 * @param direction  方向
	 * @return
	 */
	public static int getDirectionValueY(int y,int direction,int times){
		try{
			int jsY = y;
			for(int i = 0 ; i < times ; i++){
				jsY += OFFSET_Y[direction];
			}
			return  jsY;
		}catch(Exception e){
			return 0;
		}
	}

}
