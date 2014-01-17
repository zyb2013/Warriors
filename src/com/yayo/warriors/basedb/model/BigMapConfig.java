package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.type.IndexName;

/**
 * 大地图配置表
 * @author jonsai
 *
 */
@Resource
public class BigMapConfig {
	/** 地图id */
	@Id
	private int mapId;
	
	/** 地图名 */
	@Index(name = IndexName.BIG_MAP_NAME)
	private String name;
	
	/** 偏移X */
	private int offsetX;
	
	/** 偏移Y */
	private int offsetY;
	
	/** 功能道具限制 类型*/
	private String itemLimit;
	
	@JsonIgnore
	private transient volatile List<Integer> limitTypes = null;
	
	/**
	 * 地图中是否可以使用该类型的道具
	 * @param type  类型
	 * @return true 可以使用  false 不可以使用
	 */
	public boolean canUsePropsInMap(int type){
		if(limitTypes != null){
			if(limitTypes.contains(type)){//有限制不可以使用
				return false;
			}else{
				return true;
			}
		}
		
		synchronized (this) {
			if(limitTypes != null){
				if(limitTypes.contains(type)){//有限制不可以使用
					return false;
				}else{
					return true;
				}
			}
			
			limitTypes = new ArrayList<Integer>(5);
			if(this.itemLimit != null && !this.itemLimit.isEmpty()){
				String[] split =  this.itemLimit.split(Splitable.BETWEEN_ITEMS);
				for(String tmp : split){
					limitTypes.add(Integer.parseInt(tmp));
				}
			}
			
			
			if(limitTypes.contains(type)){//有限制不可以使用
				return false;
			}else{
				return true;
			}
		}
		
	}
	

	//Getter and Setter...
	
	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}

	public String getItemLimit() {
		return itemLimit;
	}

	public void setItemLimit(String itemLimit) {
		this.itemLimit = itemLimit;
	}

	@Override
	public String toString() {
		return "BigMapConfig [mapId=" + mapId + ", name=" + name + ", offsetX="
				+ offsetX + ", offsetY=" + offsetY + ", itemLimit=" + itemLimit
				+ "]";
	}
}
