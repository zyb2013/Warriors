package com.yayo.warriors.basedb.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;
import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.type.IndexName;

/**
 * 帮派建筑升级配置表
 * @author liuyuhua
 */
@Resource
public class AllianceBuildConfig {
	
	@Id
	private int id;
	
	/** 类型*/
	@Index(name = IndexName.ALLIANCEBUILD_TYPE_LEVEL, order = 0)
	private int type;
	
	/** 等级*/
	@Index(name = IndexName.ALLIANCEBUILD_TYPE_LEVEL, order = 1)
	private int level;
	
	/** 升级条件,所需要帮派的等级*/
	private int content;
	
	/** 升级所需要的铜币*/
	private int silver;
	
	/** 升级道具{物品原型ID_数量|物品原型ID_数量}*/
	private String items;
	
	/** 等级限制*/
	private int levelLimit;
	
	/** 升级所需要的道具*/
	@JsonIgnore
	private transient Map<Long,Integer> itemlist = null;
	
	
	
	
	/**
	 * 获取升级所需要的 令牌道具
	 * @return {@link List}
	 */
	public Map<Long,Integer> getItemList(){
		if(itemlist != null){
			return itemlist;
		}
		
		synchronized (this) {
			if(itemlist != null){
				return itemlist;
			}
			
			itemlist = new HashMap<Long, Integer>(); 
			List<String[]> list = Tools.delimiterString2Array(items);
			if(list == null || list.isEmpty()){
				return itemlist;
			}
			
			for(String[] tmp : list){
				if(tmp.length < 2){
					continue;
				}
				long propsId = Long.parseLong(tmp[0]);
				int count = Integer.parseInt(tmp[1]);
				itemlist.put(propsId, count);
			}
			
			return itemlist;
		}
	}

	//Getter and Setter...

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getContent() {
		return content;
	}

	public void setContent(int content) {
		this.content = content;
	}

	public int getSilver() {
		return silver;
	}

	public void setSilver(int silver) {
		this.silver = silver;
	}

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}

	public int getLevelLimit() {
		return levelLimit;
	}

	public void setLevelLimit(int levelLimit) {
		this.levelLimit = levelLimit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AllianceBuildConfig other = (AllianceBuildConfig) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
