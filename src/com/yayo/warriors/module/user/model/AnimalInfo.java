package com.yayo.warriors.module.user.model;

import java.io.Serializable;
import java.util.Arrays;

import com.yayo.warriors.module.fight.model.UnitId;

/**
 * 动物的属性(HP/MP/SP详细信息)的详细信息
 * 
 * @author Hyint
 */
public class AnimalInfo implements Serializable {
	private static final long serialVersionUID = -2468524486460821109L;

	/** 单位ID */
	private UnitId unitId;

	/** 属性数组 */
	private Object[] params;

	/** 属性值数组 */
	private Object[] values;

	public UnitId getUnitId() {
		return unitId;
	}

	public void setUnitId(UnitId unitId) {
		this.unitId = unitId;
	}
 
	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public Object[] getValues() {
		return values;
	}

	public void setValues(Object[] values) {
		this.values = values;
	}

	public static AnimalInfo valueOf(UnitId unitId, Object[] params, Object[] values) {
		AnimalInfo animalInfo = new AnimalInfo();
		animalInfo.unitId = unitId;
		animalInfo.params = params;
		animalInfo.values = values;
		return animalInfo;
	}

	@Override
	public String toString() {
		return "AnimalInfo [unitId=" + unitId + ", params=" + Arrays.toString(params) + ", values=" + Arrays.toString(values) + "]";
	}
	
	
}
