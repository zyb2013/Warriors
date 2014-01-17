package com.yayo.warriors.module.fight.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * BUFF封装对象. 需要封转给客户端的
 * 
 * @author Hyint
 */
public class BufferSwrapper implements Serializable {
	private static final long serialVersionUID = -247115492714912150L;

	/** 所属的战斗单位: 角色/怪物/召唤兽 */
	private UnitId unitId;
	
	/** 战斗过程中发生改变的BUFF信息数组 */
	private ChangeBuffer[] changeBuffers;

	public UnitId getUnitId() {
		return unitId;
	}

	public void setUnitId(UnitId unitId) {
		this.unitId = unitId;
	}

	public ChangeBuffer[] getChangeBuffers() {
		return changeBuffers;
	}

	public void setChangeBuffers(ChangeBuffer[] changeBuffers) {
		this.changeBuffers = changeBuffers;
	}
	
	public static BufferSwrapper valueOf(UnitId unitId, Collection<ChangeBuffer> changeBuffers) {
		BufferSwrapper swrapper = new BufferSwrapper();
		swrapper.unitId = unitId;
		if(changeBuffers != null) {
			swrapper.changeBuffers = changeBuffers.toArray(new ChangeBuffer[changeBuffers.size()]);
		}
		return swrapper;
	}
	
	/**
	 * 构建{@link BufferSwrapper}数组
	 * 
	 * @param  values						战斗中变化的 Buffer 属性
	 * @return {@link BufferSwrapper[]}		变化的属性数组
	 */
	public static List<BufferSwrapper> valueOf(Map<UnitId, List<ChangeBuffer>> values) {
		if(values == null || values.isEmpty()) {
			return null;
		}
		
		List<BufferSwrapper> swrappers = new ArrayList<BufferSwrapper>();
		for (Entry<UnitId, List<ChangeBuffer>> entry : values.entrySet()) {
			UnitId unitId = entry.getKey();
			List<ChangeBuffer> changeBuffers = entry.getValue();
			if(unitId != null) {
				swrappers.add(valueOf(unitId, changeBuffers));
			}
		}
		return swrappers;
	}
}
