package com.yayo.warriors.basedb.model;

/**
 * 地图传送点
 * @author liuyuhua
 *
 */
public class ChangePointConfig{
	/**传送到下一幅地图*/
	private int linkMapId;
	
	/**X 轴坐标*/
	private int x;
	
	/**Y 轴坐标*/
	private int y;
	
	/**验证点X坐标*/
	private int verfiX;
	
	/**验证点Y坐标*/
	private int verfiY;

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
		return "ChangePointConfig [linkMapId=" + linkMapId + ", x=" + x
				+ ", y=" + y + ", verfiX=" + verfiX + ", verfiY=" + verfiY
				+ "]";
	}

}
