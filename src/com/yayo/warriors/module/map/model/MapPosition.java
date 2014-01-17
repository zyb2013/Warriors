package com.yayo.warriors.module.map.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import com.yayo.warriors.common.util.astar.DirectionUtil;


/**
 * 地图切割点
 * 注:一张大地图，更具目前玩家可视区域
 * 进行九宫格的方式切割，也就是一屏的为九个区域块
 * @author liuyuhua
 *
 */
public class MapPosition{
	
	/**坐标点所对应的PositionIndexer  {X_Y 坐标,PositionId}*/
	private Map<String,Integer> positionIndexer = new HashMap<String,Integer>(0);
	
	/**坐标点所对应的Position点,所对应的可是区域  {PositionId,PostionIds}*/
	private Map<Integer,Collection<Integer>> viewLimits = new HashMap<Integer,Collection<Integer>>(0);
	
	
	/**
	 * 构造方法
	 * @param positionIndexer
	 * @param viewLimits
	 * @param maxXLength
	 * @param maxYLength
	 * @return
	 */
	public static MapPosition valueOf(Map<String,Integer> positionIndexer,
			                          int maxXLength,
			                          int maxYLength){
		MapPosition position = new MapPosition();
		position.positionIndexer = positionIndexer;
		position.makePositionViews(maxXLength, maxXLength);
		return position;
	}


	/**
	 * 通过坐标获取所在的PositionId
	 * @param x  X 坐标
	 * @param y  Y 坐标
	 * @return
	 */
	public Integer getPositionIdByXY(int x,int y){
		return this.positionIndexer.get(this.format(x, y));
	}
	
	/**
	 * 通过PositionId获取该PositionId相应的可是范围
	 * @param positionId  切割点
	 * @return
	 */
	public Collection<Integer> getPositionViews(Integer positionId){
		Collection<Integer> views = new ArrayList<Integer>();
		if(this.viewLimits.get(positionId) == null){
			return views;
		}
		views.addAll(this.viewLimits.get(positionId));
		return views;
	}
	

	/**
	 * 创建可是区域试图
	 * @param maxXLength
	 * @param maxYLength
	 * @return
	 */
	protected void makePositionViews(int maxXLength,int maxYLength){
		
		if(maxXLength <= 0 || maxYLength <= 0){
			return;
		}
		int num = 0; //分割点
		
		int[][] postions = new int[maxXLength][maxYLength];
		
		///模拟创建PostionId
		for(int i = 0 ; i < maxXLength ; i++){
			for(int j = 0 ; j < maxYLength ; j++){
				postions[i][j] = num++;
			}
		}
		
		for(int i = 0 ; i < maxXLength ; i++){
			for(int j = 0 ; j < maxYLength ; j++){
				
				int postionId = postions[i][j];
				Collection<Integer> views = viewLimits.get(postionId);
				if(views == null) {
					views = new ArrayList<Integer>();
					viewLimits.put(postionId, views);
				}
				
				///小键盘 1 的位置
				try {
					int[] xy = DirectionUtil.getDirectionValue(0);
					views.add(postions[i + xy[0]][j + xy[1]]);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				
				try {
					views.add(postions[i-2][j+1]);
				} catch (Exception e) {
				}
				
				try {
					views.add(postions[i-2][j+2]);
				} catch (Exception e) {
				}
				
				try {
					views.add(postions[i-1][j+2]);
				} catch (Exception e) {
				}
				
				
				///小键盘 2 的位置
				try {
					
					int[] xy = DirectionUtil.getDirectionValue(1);
					views.add(postions[i + xy[0]][j + xy[1]]);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				
				try {
					views.add(postions[i+0][j+2]);
				} catch (Exception e) {
				}
				
				
				///小键盘 3 的位置
				try {
					int[] xy = DirectionUtil.getDirectionValue(2);
					views.add(postions[i + xy[0]][j + xy[1]]);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				
				
				try {
					views.add(postions[i+1][j+2]);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				
				try {
					views.add(postions[i+2][j+2]);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				
				try {
					views.add(postions[i+2][j+1]);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				
				
				///小键盘 4 的位置
				try {
					
					int[] xy = DirectionUtil.getDirectionValue(3);
					views.add(postions[i + xy[0]][j + xy[1]]);
					
				} catch (ArrayIndexOutOfBoundsException e) {
					
				}
				
				try {
					views.add(postions[i-2][j+0]);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				
				
				
				
				///TODO  注意 小键盘 5 的位置是自己
				try {
					
					int[] xy = DirectionUtil.getDirectionValue(4);
					views.add(postions[i + xy[0]][j + xy[1]]);
					
				} catch (ArrayIndexOutOfBoundsException e) {
					
				}
				
				///小键盘6的位置
				try {
					
					int[] xy = DirectionUtil.getDirectionValue(5);
					views.add(postions[i + xy[0]][j + xy[1]]);
					
				} catch (ArrayIndexOutOfBoundsException e) {
					
				}
				
				try {
					views.add(postions[i+2][j+0]);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				
				
				
				///小键盘7的位置
				try {
					
					int[] xy = DirectionUtil.getDirectionValue(6);
					views.add(postions[i + xy[0]][j + xy[1]]);
					
				} catch (ArrayIndexOutOfBoundsException e) {
					
				}
				
				try {
					views.add(postions[i-2][j-1]);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				
				try {
					views.add(postions[i-2][j-2]);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				
				try {
					views.add(postions[i-1][j-2]);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				
				
				///小键盘8的位置
				try {
					
					int[] xy = DirectionUtil.getDirectionValue(7);
					views.add(postions[i + xy[0]][j + xy[1]]);
					
				} catch (ArrayIndexOutOfBoundsException e) {
					
				}
				
				try {
					views.add(postions[i+0][j-2]);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				
				///小键盘9的位置
				try {
					
					int[] xy = DirectionUtil.getDirectionValue(8);
					views.add(postions[i + xy[0]][j + xy[1]]);
					
				} catch (ArrayIndexOutOfBoundsException e) {
					
				}
				
				
				try {
					views.add(postions[i+1][j-2]);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				
				try {
					views.add(postions[i+2][j-2]);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				
				try {
					views.add(postions[i+2][j-1]);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				
				
				this.viewLimits.put(postionId, views);
			}
		}
		
	}
	
	/**
	 * XY坐标格式化
	 * @param x  X坐标
	 * @param y  Y坐标
	 * @return
	 */
	protected String format(int x,int y){
		return x+"_"+y;
	}
	
	public static void main(String[] args) {
//		MapPosition postion = new MapPosition();
//		postion.makePositionViews(7, 7);
//		
//		Set<Entry<Integer, Collection<Integer>>> entrySet = postion.viewLimits.entrySet();
//		for (Entry<Integer, Collection<Integer>> entry : entrySet) {
//			System.err.println("key:"+entry.getKey());
//			System.err.println(entry.getValue());
//			
//		}
		
		
		ArrayList<Integer> array = new ArrayList<Integer>();
		try {
			
			int[] a = {1,2,3,4};
			
			array.add(a[0]);
			array.add(a[4]);
			array.add(a[1]);
			array.add(a[2]);
			
		} catch (Exception e) {
			
		}
		
		System.out.println(array.size());
		
	}

}
