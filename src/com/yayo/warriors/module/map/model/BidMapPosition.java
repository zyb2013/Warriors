package com.yayo.warriors.module.map.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yayo.warriors.common.util.astar.DirectionUtil;

/**
 * @author liuyuhua
 */
public class BidMapPosition {
	
	/** 坐标点所对应的PositionIndexer  
	 * {String,Integer} -> {坐标,可视区域点}*/
	private Map<String, Integer> positionIndexer = new HashMap<String, Integer>(0);
	
	/** 坐标点所对应的Position点,所对应的可是区域
	 *  {Integer,Collection<Integer>} -> {可是区域点,可是范围(多个可视区域点)集合}*/
	private Map<Integer,Collection<Integer>> viewsRegion = new HashMap<Integer,Collection<Integer>>(0);
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BidMapPosition.class);
	
	/**
	 * 构造方法
	 * @param positionIndexer         地图切割对象
	 * @param wide                    宽
	 * @param high                    高
	 * @return {@link BidMapPosition} 对象
	 */
	public static BidMapPosition valueOf(ConcurrentHashMap<String, Integer> positionIndexer,int wide,int high){
		BidMapPosition position = new BidMapPosition();
		position.positionIndexer = positionIndexer;
	    //position.makePositionViews(wide, wide);
		int length = wide >= high ? wide : high;
		position.makePositionViews(length, length);
	    if(LOGGER.isDebugEnabled()){
	    	LOGGER.debug("地图: wide:{},high:{},length:{}",new Object[]{wide,high,length});
	    }
		return position;
	}
	
	/**
	 * 通过坐标获取所在的PositionId
	 * @param x                X 坐标
	 * @param y                Y 坐标
	 * @return {@link Integer} 可视区域点
	 */
	public Integer getPositionIdByXY(int x,int y){
		String key = this.format(x, y);
		Integer positionId = this.positionIndexer.get(key);
		return positionId;
	}
	
	/**
	 * 通过PositionId获取该PositionId相应的可是范围
	 * @param positionId  切割点
	 * @return
	 */
	public Collection<Integer> getPositionViews(Integer positionId){
		Collection<Integer> views = new ArrayList<Integer>();
		Collection<Integer> result = this.viewsRegion.get(positionId);
		if(result == null || result.isEmpty()){
			return views;
		}
		views.addAll(result);
		return views;
	}
	
	/**
	 * XY坐标格式化
	 * @param x  X坐标
	 * @param y  Y坐标
	 * @return
	 */
	private String format(int x,int y){
		return x+"_"+y;
	}
	
	/**
	 * 获取二维数组中的数值
	 * @param views        可视区域集合
	 * @param postion      二维数组
	 * @param x            x坐标值
	 * @param y            y坐标值
	 * @return {@link Integer} 二维数组中的值
	 */
	private void buildArrayValue(Collection<Integer> views,int[][] postion,int x,int y){
		if(views == null){
			return;
		}
		if(postion == null || postion.length <= 0){
			return;
		}
		try{
			 int point = postion[x][y];
			 if(!views.contains(point)){
				 views.add(point);
			 }
		}catch (Exception e) {
			LOGGER.error("{}", e);
		}
	}
	
	/**
	 * 创建可视区域
	 * @param wide     宽
	 * @param high     高
	 */
	private void makePositionViews(int wide,int high){
		if(wide <= 0 || high <= 0){
			throw new IllegalArgumentException("构建可视区域参数不能为0");
		}
		
		/* 先构建可视区域点集合
		 * 注解：
		 * 这个地方 通过长、高就能够知道地图的大小
		 * 然后个格子就是地图的一个A*点,通过A星点
		 * 的方式来知道地图格子,并且构建每个格子
		 * 之间的可是区域关系
		 * */
		int positionId = 0;
		int[][] postions = new int[wide][high];
		for(int i = 0 ; i < wide ; i++){
			for(int j = 0 ; j < high ; j++){
				postions[i][j] = positionId++;
			}
		}
		
		for(int i = 0 ; i < wide ; i++){
			for(int j = 0 ; j < high ; j++){
				int postionId = postions[i][j];
				Collection<Integer> views = this.viewsRegion.get(postionId);
				if(views == null) {
					views = new ArrayList<Integer>();
					this.viewsRegion.put(postionId, views);
					views = this.viewsRegion.get(postionId);
				}
				
				/* 小键盘 1 的位置*/
				{
				    int[] xy = DirectionUtil.getDirectionValue(0);
				    if(xy != null){
				    	int x = xy[0];
				    	int y = xy[1];
				    	this.buildArrayValue(views, postions, i + x, j + y);
				    	this.buildArrayValue(views, postions, i - 2, j + 1);
				    	this.buildArrayValue(views, postions, i - 2, j + 2);
				    	this.buildArrayValue(views, postions, i - 1, j + 2);
				    }
				}
				
				/* 小键盘 2 的位置*/
				{
					int[] xy = DirectionUtil.getDirectionValue(1);
					if(xy != null){
				    	int x = xy[0];
				    	int y = xy[1];
						this.buildArrayValue(views, postions, i + x, j + y);
						this.buildArrayValue(views, postions, i + 0, j + 2);
					}
				}
				
				/* 小键盘 3 的位置*/
				{
					int[] xy = DirectionUtil.getDirectionValue(2);
					if(xy != null){
				    	int x = xy[0];
				    	int y = xy[1];
						this.buildArrayValue(views, postions, i + x, j + y);
						this.buildArrayValue(views, postions, i + 1, j + 2);
						this.buildArrayValue(views, postions, i + 2, j + 2);
						this.buildArrayValue(views, postions, i + 2, j + 1);
					}
				}
				
				/* 小键盘 4 的位置*/
				{
					int[] xy = DirectionUtil.getDirectionValue(3);
					if(xy != null){
				    	int x = xy[0];
				    	int y = xy[1];
						this.buildArrayValue(views, postions, i + x, j + y);
						this.buildArrayValue(views, postions, i - 2, j + 0);
					}
				}
				
				/* 小键盘 5 的位置(该位置是自己)*/
				{
					int[] xy = DirectionUtil.getDirectionValue(4);
					if(xy != null){
				    	int x = xy[0];
				    	int y = xy[1];
						this.buildArrayValue(views, postions, i + x, j + y);
					}
				}
				
				/* 小键盘 6 的位置*/
				{
					int[] xy = DirectionUtil.getDirectionValue(5);
					if(xy != null){
				    	int x = xy[0];
				    	int y = xy[1];
						this.buildArrayValue(views, postions, i + x, j + y);
						this.buildArrayValue(views, postions, i + 2, j + 0);
					}
				}
				
				/* 小键盘 7 的位置*/
				{
					int[] xy = DirectionUtil.getDirectionValue(6);
					if(xy != null){
				    	int x = xy[0];
				    	int y = xy[1];
						this.buildArrayValue(views, postions, i + x, j + y);
						this.buildArrayValue(views, postions, i - 2, j - 1);
						this.buildArrayValue(views, postions, i - 2, j - 2);
						this.buildArrayValue(views, postions, i - 1, j - 2);
					}
				}
				
				/* 小键盘 8 的位置*/
				{
					int[] xy = DirectionUtil.getDirectionValue(7);
					if(xy != null){
				    	int x = xy[0];
				    	int y = xy[1];
						this.buildArrayValue(views, postions, i + x, j + y);
						this.buildArrayValue(views, postions, i + 0, j - 2);
					}
				}
				
				/* 小键盘 9 的位置*/
				{
					int[] xy = DirectionUtil.getDirectionValue(8);
					if(xy != null){
				    	int x = xy[0];
				    	int y = xy[1];
						this.buildArrayValue(views, postions, i + x, j + y);
						this.buildArrayValue(views, postions, i + 1, j - 2);
						this.buildArrayValue(views, postions, i + 2, j - 2);
						this.buildArrayValue(views, postions, i + 2, j - 1);
					}
				}
			}
		}
	}

}
