package com.yayo.warriors.module.map.model;

import com.yayo.warriors.basedb.model.ChangePointConfig;

/**
 * 
 * 场景传送点
 * @author liuyuhua
 */
public class ChangePoint{
	/**所连接的地图ID*/
	private int linkMapId;
	
	/**X 轴坐标*/
	private int x;
	
	/**Y 轴坐标*/
	private int y;
	
	/**验证点X坐标*/
	private int verfiX;
	
	/**验证点Y坐标*/
	private int verfiY;
	
	/**
	 * 构造函数
	 * @param linkMapId
	 * @param x
	 * @param y
	 * @param verfiX
	 * @param verfiY
	 * @param dungeonId
	 * @return
	 */
	public static ChangePoint valueOf(int linkMapId,int x , int y,int verfiX,int verfiY){
		ChangePoint point = new ChangePoint();
		point.linkMapId = linkMapId;
		point.x = x;
		point.y = y;
		point.verfiX = verfiX;
		point.verfiY = verfiY;
		return point;
	}
	
	/**
	 * 构造函数
	 * @param config
	 * @return
	 */
	public static ChangePoint valueOf(ChangePointConfig config){
		ChangePoint point = new ChangePoint();
		point.linkMapId = config.getLinkMapId();
		point.verfiX = config.getVerfiX();
		point.verfiY = config.getVerfiY();
		point.x = config.getX();
		point.y = config.getY();
		return point;
	}

	//Getter and Setter...

	public int getLinkMapId() {
		return linkMapId;
	}

	public void setLinkMapId(int linkMapId) {
		this.linkMapId = linkMapId;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getVerfiX() {
		return verfiX;
	}

	public void setVerfiX(int verfiX) {
		this.verfiX = verfiX;
	}

	public int getVerfiY() {
		return verfiY;
	}

	public void setVerfiY(int verfiY) {
		this.verfiY = verfiY;
	}

	@Override
	public String toString() {
		return "ChangePoint [linkMapId=" + linkMapId + ", x=" + x + ", y=" + y
				+ ", verfiX=" + verfiX + ", verfiY=" + verfiY + "]";
	}

}
