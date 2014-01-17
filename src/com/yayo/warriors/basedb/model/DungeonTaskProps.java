package com.yayo.warriors.basedb.model;

/**
 * 副本任务奖励的物品
 * @author liuyuhua
 */
public class DungeonTaskProps {
	
	/** 道具ID*/
	private int propsId;
	
	/** 数量*/
	private int count;

	/**
	 * 构造方法
	 * @param propsId   道具ID
	 * @param count     数量
	 * @return {@link DungeonTaskProps}
	 */
	public static DungeonTaskProps valueOf(int propsId,int count){
		DungeonTaskProps dungeonTaskProps = new DungeonTaskProps();
		dungeonTaskProps.propsId = propsId;
		dungeonTaskProps.count = count;
		return dungeonTaskProps;
	}
	
	//Getter and Setter...
	
	public int getPropsId() {
		return propsId;
	}

	public void setPropsId(int propsId) {
		this.propsId = propsId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + count;
		result = prime * result + propsId;
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
		DungeonTaskProps other = (DungeonTaskProps) obj;
		if (count != other.count)
			return false;
		if (propsId != other.propsId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DungeonTaskProps [propsId=" + propsId + ", count=" + count
				+ "]";
	}
	
}
