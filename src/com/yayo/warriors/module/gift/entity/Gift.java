package com.yayo.warriors.module.gift.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.gift.model.GiftRewardInfo;
import com.yayo.warriors.module.gift.type.GiftType;
import com.yayo.warriors.type.GoodsType;


@Entity
@Table(name="gift")
public class Gift extends BaseModel<Integer> {

	private static final long serialVersionUID = -1034050018164488989L;
	
	@Id
	private Integer id;
	
	private int giftType;
	
	private String name = "";
	
	private String icon = "";
	
	@Lob
	private String description = "";
	
	private Date startTime;
	
	private Date endTime;
	
	private String conditions = "";
	
	private String rewards = "";
	
	@Transient
	private transient List<GiftRewardInfo> rewardInfo = null;
	
	@Transient
	private transient Map<String, String[]> conditionMap = null; 
	
	
	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public int getGiftType() {
		return giftType;
	}

	public void setGiftType(int giftType) {
		this.giftType = giftType;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public void setConditions(String conditions) {
		this.conditions = conditions;
	}
	
	public String getRewards() {
		return rewards;
	}

	public void setRewards(String rewards) {
		this.rewards = rewards;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public List<GiftRewardInfo> getGiftRewardInfos() {
		if (this.rewardInfo != null) {
			return this.rewardInfo;
		}
		
		List<GiftRewardInfo> propsInfo = null;
		synchronized (this) {
			if (this.rewardInfo != null) {
				return this.rewardInfo;
			}
			this.rewardInfo = new ArrayList<GiftRewardInfo>();
			Map<Integer, Integer> mergeProps = new HashMap<Integer, Integer>();       
			List<String[]> arrays = Tools.delimiterString2Array(this.rewards);
			if (arrays != null) {
				for (String[] array : arrays) {
					int goodsType = Integer.valueOf(array[0]);
					int baseId = Integer.valueOf(array[1]);
					int count = Integer.valueOf(array[2]);
					if (goodsType == GoodsType.PROPS) {
						int cacheNum = mergeProps.containsKey(baseId) ? mergeProps.get(baseId) : 0;
						mergeProps.put(baseId, count + cacheNum);
					} else if (goodsType == GoodsType.EQUIP) {
						this.rewardInfo.add(GiftRewardInfo.equipReward(baseId, count));
					} else if (goodsType == GoodsType.GOLDEN) {
						this.rewardInfo.add(GiftRewardInfo.goldenReward(count));
					} else if (goodsType == GoodsType.SILVER) {
						this.rewardInfo.add(GiftRewardInfo.silverReward(count));
					} else if (goodsType == GoodsType.COUPON) {
						this.rewardInfo.add(GiftRewardInfo.couponReward(count));
					}
				}
			}
			propsInfo = GiftRewardInfo.propsRewardList(mergeProps);
		}
		this.rewardInfo.addAll(propsInfo);
		return this.rewardInfo;
	}
	
	
	public Map<String, String[]> getConditions() {
		if (this.conditionMap != null) {
			return this.conditionMap;
		}
		
		this.conditionMap = Tools.delimiterString2Map(this.conditions);
		return this.conditionMap;
	}
	

	public boolean isValidTime() {
		if(startTime != null && startTime.after(new Date())){
			return false;
		}
		if(endTime != null && endTime.before(new Date())){
			return false;
		}
		return true;
	}
	
	
	public static Gift valueOf(String rewards) {
		Gift gift = new Gift();
		return gift;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Gift))
			return false;
		Gift other = (Gift) obj;
		return id != null && other.id != null && id.equals(other.id);
	}

}
