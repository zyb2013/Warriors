package com.yayo.warriors.basedb.model;

import com.yayo.common.utility.EnumUtils;
import com.yayo.warriors.module.duntask.types.TaskTypes;

/**
 * 副本事件
 * @author liuyuhua
 */
public class DungeonTaskEventConfig {
	
	/** 事件ID*/
	private int id;
	
	/** 副本任务类型*/
	private TaskTypes type;

	/** 完成条件(数量)*/
	private int completeCount;
	
	/** 条件*/
	private int condition;
	
	
	/**
	 * 构造方法
	 * @param index            事件序号
	 * @param type             类型
	 * @param completeCount    完成数量
	 * @param condition        条件
	 * @return {@link DungeonTaskConfig}
	 */
	public static DungeonTaskEventConfig valueOf(int index,int type,int completeCount,int condition){
		DungeonTaskEventConfig config = new DungeonTaskEventConfig();
		config.id = index;
		config.condition = condition;
		config.completeCount = completeCount;
		try {
			config.type = EnumUtils.getEnum(TaskTypes.class,type);
		} catch (Exception e) {
			throw new RuntimeException("构造基础副本任务对象,事件类型不存在,请检查副本任务事件配置");
		}
		
		return config;
	}
	
	//Getter and Setter...

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public TaskTypes getType() {
		return type;
	}

	public void setType(TaskTypes type) {
		this.type = type;
	}

	public int getCompleteCount() {
		return completeCount;
	}

	public void setCompleteCount(int completeCount) {
		this.completeCount = completeCount;
	}

	public int getCondition() {
		return condition;
	}

	public void setCondition(int condition) {
		this.condition = condition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + completeCount;
		result = prime * result + id;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		DungeonTaskEventConfig other = (DungeonTaskEventConfig) obj;
		if (completeCount != other.completeCount)
			return false;
		if (id != other.id)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DungeonTaskEventConfig [id=" + id + ", type=" + type
				+ ", completeCount=" + completeCount + ", condition="
				+ condition + "]";
	}
}
