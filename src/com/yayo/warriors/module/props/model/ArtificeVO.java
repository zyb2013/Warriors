package com.yayo.warriors.module.props.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 道具炼化VO对象
 * 
 * @author Hyint
 */
public class ArtificeVO {
	
	/** 扣除的用户道具信息 */
	private Map<Long, Integer> costUserProps = new HashMap<Long, Integer>(0);

	/** 使用的扣除道具信息 */
	private Map<Integer, Integer> totalCostItems = new HashMap<Integer, Integer>(0);
	
	public Map<Long, Integer> getCostUserProps() {
		return costUserProps;
	}
	
	public Map<Integer, Integer> getTotalCostItems() {
		return totalCostItems;
	}

	public int getCostUserPropsCount(long userPropsId) {
		Integer count = this.costUserProps.get(userPropsId);
		return count == null ? 0 : count;
	}
	
	public void addCostUserProps(long userPropsId, int addCount) {
		Integer count = this.costUserProps.get(userPropsId);
		count = count == null ? 0 : count;
		this.costUserProps.put(userPropsId, count + addCount);
	}
	
	public static ArtificeVO valueOf(Map<Integer, Integer> materials) {
		ArtificeVO artificeVO = new ArtificeVO();
		if(materials != null && !materials.isEmpty()) {
			artificeVO.totalCostItems.putAll(materials);
		}
		return artificeVO;
	}
}
