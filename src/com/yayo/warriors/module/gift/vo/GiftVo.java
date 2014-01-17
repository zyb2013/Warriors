package com.yayo.warriors.module.gift.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.yayo.warriors.module.gift.entity.Gift;


/**
 * 用户礼包VO(条件限制)
 * 
 * @author huachaoping
 */
public class GiftVo implements Serializable{

	private static final long serialVersionUID = 7324591191131276129L;
	
	/** 礼包ID */
	private long giftId;
	
	/** 是否已领取 */
	private int received;
	
	/** 礼包类型 */
	private int giftType;
	
	/** 礼包名称 */
	private String name;
	
	/** 礼包图片名称 */
	private String iconName = "";
	
	/** 礼包描述 */
	private String description;
	
	/** 礼包的领取条件 */
	private Object[] conditions;
	
	/** 奖励信息 */
	private Object[] rewardInfos;
	
	
	public static GiftVo valueOf(Gift gift, int received) {
		GiftVo giftVo = new GiftVo();
		giftVo.giftId = gift.getId();
		giftVo.giftType = gift.getGiftType();
		giftVo.received = received;
		giftVo.name = gift.getName();
		giftVo.iconName = gift.getIcon();
		giftVo.description = gift.getDescription();
		giftVo.rewardInfos = gift.getGiftRewardInfos().toArray();
		List<ConditionInfo> info = new ArrayList<ConditionInfo>();
		for (String[] element : gift.getConditions().values()) {
			info.add(ConditionInfo.valueOf(element[0], element[1], element[2]));
		}
		giftVo.conditions = info.toArray();
		return giftVo;
	}
	

	public long getGiftId() {
		return giftId;
	}

	public void setGiftId(long giftId) {
		this.giftId = giftId;
	}

	public int getReceived() {
		return received;
	}

	public void setReceived(int received) {
		this.received = received;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object[] getConditions() {
		return conditions;
	}

	public void setConditions(Object[] conditions) {
		this.conditions = conditions;
	}

	public Object[] getRewardInfos() {
		return rewardInfos;
	}

	public void setRewardInfos(Object[] rewardInfos) {
		this.rewardInfos = rewardInfos;
	}

	public int getGiftType() {
		return giftType;
	}

	public void setGiftType(int giftType) {
		this.giftType = giftType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}
	
}
