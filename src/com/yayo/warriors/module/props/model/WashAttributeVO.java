package com.yayo.warriors.module.props.model;

import java.util.Map;

/**
 * 洗练属性VO
 * 
 * @author Hyint
 */
public class WashAttributeVO {
	
	/** 用户装备ID */
	private long userEquipId;
	
	/** 是否忙碌中 */
	private volatile boolean busy;
	
	/** 附加属性信息 */
	private Map<Integer, AttributeVO> attributes;
	
	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	
	public long getUserEquipId() {
		return userEquipId;
	}

	public void setUserEquipId(long userEquipId) {
		this.userEquipId = userEquipId;
	}

	public Map<Integer, AttributeVO> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<Integer, AttributeVO> attributes) {
		this.attributes = attributes;
	}

	/**
	 * 构建洗练对象
	 * 
	 * @param  userEquipId				用户装备ID
	 * @param  attributes				附加的属性VO
	 * @return {@link WashAttributeVO}	洗练属性对象
	 */
	public static WashAttributeVO valueOf(long userEquipId, Map<Integer, AttributeVO> attributes) {
		WashAttributeVO washAttributeVO = new WashAttributeVO();
		washAttributeVO.userEquipId = userEquipId;
		washAttributeVO.attributes = attributes;
		return washAttributeVO;
	}
	
	@Override
	public String toString() {
		return "WashAttributeVO [userEquipId=" + userEquipId + ", attributes=" + attributes + "]";
	}
}
