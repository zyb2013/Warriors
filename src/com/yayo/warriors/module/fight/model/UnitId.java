package com.yayo.warriors.module.fight.model;

import java.io.Serializable;

import com.yayo.common.utility.EnumUtils;
import com.yayo.warriors.type.ElementType;

/**
 * 战斗单位ID
 * 
 * @author Hyint
 */
public class UnitId implements Serializable {
	private static final long serialVersionUID = 8148876580757858028L;

	/** 角色ID/怪物ID/召唤兽ID */
	private long id;

	/** 
	 * 行为类型. 
	 * 
	 * @see ElementType 
	 */
	private ElementType type;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ElementType getType() {
		return type;
	}

	public void setType(ElementType type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + (type == null ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		
		UnitId other = (UnitId) obj;
		return id == other.id && type == other.type;
	}

	/**
	 * 战斗单位ID
	 * 
	 * @param  id				战斗单位ID(角色ID/怪物ID/召唤兽ID)
	 * @param  type				战斗类型
	 * @return {@link UnitId}	战斗单元ID
	 */
	public static UnitId valueOf(long id, ElementType type) {
		UnitId unitId = new UnitId();
		unitId.id = id;
		unitId.type = type;
		return unitId;
	}

	/**
	 * 战斗单位ID
	 * 
	 * @param  id				战斗单位ID(角色ID/怪物ID/召唤兽ID)
	 * @param  type				战斗类型
	 * @return {@link UnitId}	战斗单元ID
	 */
	public static UnitId valueOf(long id, int type) {
		UnitId unitId = new UnitId();
		unitId.id = id;
		unitId.type = EnumUtils.getEnum(ElementType.class, type);
		return unitId;
	}
	
	@Override
	public String toString() {
		return "UnitId [id=" + id + ", type=" + type + "]";
	}
	
}
