package com.yayo.warriors.basedb.model;


import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.type.IndexName;

/**
 * 场景中的NPC,注意不是怪物
 * @author liuyuhua
 */
@Resource
public class NpcConfig {

	/**NPC ID*/
	@Id
	private Integer id;
	
	/** Npc的类型 类型也是唯一标识,相当于原型ID*/
	private Integer baseId;
	
	/** 元素类型 {@link ElementType}*/
	@Index(name=IndexName.NPC_SCREEN_TYPE, order=0)
	private int elementType;
	
	/** 地图 ID*/
	@Index(name=IndexName.NPC_MAPID)
	private Integer mapId;
	
	/** 头像图标*/
	private Integer icon;
	
	/** 名字*/
	private String name;
	
	/** 模型名字*/
	private Integer model;
	
	/** X 坐标*/
	private int bornX;
	
	/** Y 坐标*/
	private int bornY;
	
	/** 等级*/
	private int level;
	
	/** 场景类型*/
	@Index(name=IndexName.NPC_SCREEN_TYPE, order=1)
	private int screenType;
	
	/** 采集物的物品类型. 物品ID_获得概率|*/
	private String props;
	
	/** 再生时间(毫秒) */
	private int rebirthTime;
	
	/** 采集物品数组*/
	@JsonIgnore
	private int[] collects;
	
	
	private int[] getCollects() {
		if(this.collects != null) {
			return this.collects;
		}
		
		synchronized (this) {
			if(this.collects != null) {
				return this.collects;
			}
			
			this.collects = new int[2];
			if(StringUtils.isBlank(props)) {
				return this.collects;
			}
			
			String[] array = props.split(Splitable.ATTRIBUTE_SPLIT);
			if(array.length >= 2) {
				this.collects[0] = Integer.valueOf(array[0]);
				this.collects[1] = Integer.valueOf(array[1]);
			}
		}
		return this.collects;
	}
	
	/**
	 * 获取采集物道具ID
	 * @return {@link Integer} 采集物ID
	 */
	public int getCollectPropsId(){
		int[] col = this.getCollects();
		return col.length >= 2 ? col[0] : 0;
	}
	
	public int getRandomCollect() {
		int[] col = this.getCollects();
		int rate = Tools.getRandomInteger(AttributeKeys.RATE_BASE);
		return rate <= col[1] ? col[0] : 0;
	}
	
	public Integer getIcon() {
		return icon;
	}

	public void setIcon(Integer icon) {
		this.icon = icon;
	}

	public int getElementType() {
		return elementType;
	}

	public void setElementType(int elementType) {
		this.elementType = elementType;
	}

	public Integer getBaseId() {
		return baseId;
	}

	public void setBaseId(Integer typeId) {
		this.baseId = typeId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getMapId() {
		return mapId;
	}

	public void setMapId(Integer mapId) {
		this.mapId = mapId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getModel() {
		return model;
	}

	public void setModel(Integer model) {
		this.model = model;
	}

	public int getBornX() {
		return bornX;
	}

	public void setBornX(int x) {
		this.bornX = x;
	}

	public int getBornY() {
		return bornY;
	}

	public void setBornY(int y) {
		this.bornY = y;
	}
	
	public int getScreenType() {
		return screenType;
	}

	public void setScreenType(int screenType) {
		this.screenType = screenType;
	}

	public String getProps() {
		return props;
	}

	public void setProps(String props) {
		this.props = props;
	}

	public int getRebirthTime() {
		if(this.rebirthTime < 0){
			this.rebirthTime = 2000;
		}
		return rebirthTime;
	}

	public void setRebirthTime(int rebirthTime) {
		this.rebirthTime = rebirthTime;
	}

	@Override
	public String toString() {
		return "NpcConfig [id=" + id + ", baseId=" + baseId + ", elementType=" + elementType
				+ ", mapId=" + mapId + ", icon=" + icon + ", name=" + name + ", model=" + model
				+ ", bornX=" + bornX + ", bornY=" + bornY + ", level=" + level + "]";
	}
}
