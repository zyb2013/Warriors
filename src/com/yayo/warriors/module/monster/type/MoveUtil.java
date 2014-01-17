package com.yayo.warriors.module.monster.type;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.yayo.common.utility.TimeConstant;
import com.yayo.warriors.common.util.astar.DirectionUtil;
import com.yayo.warriors.module.map.util.MapUtils;


/**
 * 移动工具类
 * @author jonsai
 *
 */
public class MoveUtil{
	
	/** 移动像素 */
	/** */
	public final static float px_1 = 33.541f;
	/** */
	public final static float px_2 = 60;
	/** */
	public final static float px_3 = 30;
	/** 移动延时缓存, key:后三位移动像素、前面部分是移动速度 value:延时(毫秒) */
	private static final ConcurrentLinkedHashMap<Float, Integer> costTimeMap = new ConcurrentLinkedHashMap.Builder<Float, Integer>().maximumWeightedCapacity(30).build();
	
	private static final float FRAME_TIME = TimeConstant.ONE_SECOND_MILLISECOND / 30;
	
	/**
	 * 移动一格需要的时间
	 * @param sx
	 * @param sy
	 * @param tx
	 * @param ty
	 * @param moveSpeed
	 * @return
	 */
	public static int getMoveStepTime(int sx, int sy, int tx, int ty, double moveSpeed){
		float calcMovePixel = calcMovePixel(sx, sy, tx, ty);
		float key = ((int)moveSpeed << 6) + calcMovePixel;
		Integer value = costTimeMap.get(key);
		if(value == null){
			value= (int)(calcMovePixel / moveSpeed * FRAME_TIME) - 10;
			costTimeMap.put(key, value);
		}
		return value;
	}
	
	/**
	 * 移动一格的像素
	 * @param sx
	 * @param sy
	 * @param tx
	 * @param ty
	 * @return
	 */
	public static float calcMovePixel(int sx, int sy, int tx, int ty){
		int direction = DirectionUtil.direction(sx, sy, tx, ty);
		if(Math.abs(sx - tx) <= 1 && Math.abs(sy - ty) <= 1){
			if(direction == 1 || direction == 9){
				return px_1;
			} else if(direction == 2 || direction == 8){
				return px_3;
			} else if(direction == 3 || direction == 7){
				return px_1;
			} else if(direction == 4 || direction == 6){
				return px_2;
			} else if(direction == 5){
				return 0;
			}
		}
		
		int calcDistance = MapUtils.calcDistance(sx, sy, tx, ty);
		return calcDistance * 45;	//不是移动一格，走太快了
	}
	
	
	public static void main(String... args){
		int moveStepTime = getMoveStepTime(292, 175, 291, 175, 5);
		System.err.println(moveStepTime);
		int direction = DirectionUtil.direction(292, 175, 291, 175);
		System.err.println(direction);
		direction = DirectionUtil.direction(292, 175, 293, 175);
		System.err.println(direction);
	}
}
