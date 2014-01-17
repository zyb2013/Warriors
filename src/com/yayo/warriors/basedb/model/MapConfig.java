package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;
import com.yayo.common.basedb.InitializeBean;
import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.module.map.model.ChangePoint;
import com.yayo.warriors.module.map.types.ScreenType;
import com.yayo.warriors.type.IndexName;

/**
 * 地图源数据
 * @author liuyuhua
 */
@Resource
public class MapConfig implements InitializeBean {
	/**地图ID*/
	@Id
	private int id;
	
	/**地图 格子列*/
	private int col;
	
	/**地图 格子行*/
	private int row;
	
	/**地图  宽 单位:像素*/
	private int width;
	
	/**地图  高 单位:像素*/
	private int height;
	
	/**地图数据  (一维数组,需要自己切割)*/
	private String mapdata;
	
	/** 场景类型 {@link ScreenType}*/
	@Index(name = IndexName.MAP_SCREENTYPE)
	private int screenType;
	
	/** 进入场景的等级*/
	private int levelLimit;
	
	/**地图传送点*/
	private Collection<ChangePointConfig> changePoint;
	
	/** 地图掩码 */
	@JsonIgnore
	private byte[][] mapMask;
	
	@JsonIgnore
	private List<ChangePoint> pointlist = null;
	
	//Getter and Setter...
	
	public Collection<ChangePointConfig> getChangePoint() {
		return changePoint;
	}

	public List<ChangePoint> getPointlist() {
		return pointlist;
	}

	public void setPointlist(List<ChangePoint> pointlist) {
		this.pointlist = pointlist;
	}

	public void setChangePoint(Collection<ChangePointConfig> changePoint) {
		this.changePoint = changePoint;
	}

	public String getMapdata() {
		return mapdata;
	}

	public void setMapdata(String mapdata) {
		this.mapdata = mapdata;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getScreenType() {
		return screenType;
	}

	public void setScreenType(int screenType) {
		this.screenType = screenType;
	}

	public int getLevelLimit() {
		return levelLimit;
	}

	public void setLevelLimit(int levelLimit) {
		this.levelLimit = levelLimit;
	}

	/**
	 * 格式化以后的地图数据信息
	 * @return
	 */
	public byte[] getFormatMapData(){
		
		String[] datas = mapdata.split(",");
		byte[] tmp = new byte[datas.length];
		
		for(int i = 0 ; i < tmp.length ; i++){
			tmp[i] = Byte.valueOf(datas[i]);
		}
		return tmp;
	}

	public byte[][] getMapMask() {
		if(this.mapMask == null){
			byte[] mapArrayData = this.getFormatMapData();//地图原始数据
			setMapMask( buildMapMask(mapArrayData, this.col, this.row) );
		}
		return mapMask;
	}
	
	private byte[][] buildMapMask(byte[] mapdata,int col,int row) {
		byte[][] data = new byte[row][col];
		for(int i  = 0 ; i < row  ; i++) {
			for(int j = 0 ; j < col ; j++){
				data[i][j] = mapdata[i * row +j];
			}
		}
		return data;
	}

	public void setMapMask(byte[][] mapMask) {
		this.mapMask = mapMask;
	}
	
	
	public void afterPropertiesSet() {
		List<ChangePoint> changePoints = new ArrayList<ChangePoint>(3);
		for(ChangePointConfig pointConfig : this.getChangePoint()){
			ChangePoint point = ChangePoint.valueOf(pointConfig);
			changePoints.add(point);
		}
		
		this.pointlist = new ArrayList<ChangePoint>(changePoints);
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + col;
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapConfig other = (MapConfig) obj;
		if (col != other.col)
			return false;
		return true;
	}

	
	public String toString() {
		return "MapConfig [id=" + id + ", col=" + col + ", row=" + row
				+ ", width=" + width + ", height=" + height + ", mapdata="
				+ mapdata + ", screenType=" + screenType + ", levelLimit="
				+ levelLimit + ", changePoint=" + changePoint + "]";
	}

}
