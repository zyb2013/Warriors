package com.yayo.warriors.basedb.model;


import java.util.List;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.type.IndexName;

/**
 * 固定礼包配置
 * @author liuyuhua
 */
@Resource
public class PropsGiftConfig {

	@Id
	private int id;
	
	/** 礼包编号*/
	@Index(name = IndexName.PROPS_GIFT_NO_JOB, order = 0)
	private int giftNo;
	
	/** 职业(玩家的职业与该职业相同时候不讲理该道具) */
	@Index(name = IndexName.PROPS_GIFT_NO_JOB, order = 1)
	private int job;
	
	/** 奖励的类型 {@link GiftType}*/
	private int giftType;
	
	/** 物品ID 格式:{道具ID_数量|道具ID_数量}*/
	private String item;
	
	/** 数量*/
	private int number;
	
	/** 绑定类型 */
	private boolean binding;
	
	/** 奖励的物品 */
	private List<String[]> rewardGoods = null; 
	
	/**
	 * 获得礼包道具个数
	 * @return {@link Integer} 礼包奖励数量
	 */
	public int itemSize(){
		if(item != null){
			return getRewardGoods().size();
		}
		return 0;
	}
	
	//Getter and Setter...
	public List<String[]> getRewardGoods() {
		if(rewardGoods == null){
			synchronized (this) {
				if(rewardGoods == null){
					rewardGoods = Tools.delimiterString2Array(this.item);
				}
			}
		}
		return rewardGoods;
	}
	
	public void setRewardGoods(List<String[]> rewardGoods) {
		this.rewardGoods = rewardGoods;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getGiftNo() {
		return giftNo;
	}

	public void setGiftNo(int giftNo) {
		this.giftNo = giftNo;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public int getGiftType() {
		return giftType;
	}

	public void setGiftType(int giftType) {
		this.giftType = giftType;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
		this.rewardGoods = null;
	}

	public boolean isBinding() {
		return binding;
	}

	public void setBinding(boolean binding) {
		this.binding = binding;
	}


	@Override
	public String toString() {
		return "FastenGiftConfig [id=" + id + ", giftNo=" + giftNo + ", job="
				+ job + ", giftType=" + giftType + ", item=" + item
				+ ", number=" + number + ", binding=" + binding + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		PropsGiftConfig other = (PropsGiftConfig) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
