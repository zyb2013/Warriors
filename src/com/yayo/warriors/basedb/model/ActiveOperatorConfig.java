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
 * 运营活动表
 * @author liuyuhua
 */
@Resource
public class ActiveOperatorConfig {
	
	/** 活动的ID,唯一标示*/
	@Id
	private int id;
	
	/** 类型(排行,冲级...)*/
	@Index(name=IndexName.ACTIVE_OPERATOR_TYPE, order = 0)
	private int type;
	
	/** 活动名字*/
	private String name;
	
	/** 掉落编号*/
	private String drop;
	
	/** 掉落列表*/
	@JsonIgnore
	private transient List<Integer> dorplist = null;
	
	/**
	 * 获取掉落编号
	 * @return 掉落编号
	 */
	public List<Integer> getDorpList(){
		if(dorplist != null){
			return dorplist;
		}
		
		synchronized (this) {
			if(dorplist != null){
				return dorplist;
			}
			
			dorplist = new ArrayList<Integer>();
			if(drop == null || drop.isEmpty()){
				return dorplist;
			}
			
			String[] split = drop.split(Splitable.ELEMENT_SPLIT);
			for(String tmp : split){
				int dropNum = Integer.parseInt(tmp); //掉落编号
				if(dropNum == 0){
					continue;
				}
				dorplist.add(dropNum);
			}
			
			return dorplist;
		}
	}
	
	//Getter and Setter...

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDrop() {
		return drop;
	}

	public void setDrop(String drop) {
		this.drop = drop;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ActiveOperatorConfig [id=" + id + ", type=" + type + ", name="
				+ name + ", drop=" + drop + "]";
	}

}
