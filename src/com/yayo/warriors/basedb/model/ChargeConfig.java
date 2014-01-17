package com.yayo.warriors.basedb.model;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.module.recharge.type.ChargeGiftType;
import com.yayo.warriors.type.IndexName;

/**
 * 充值礼包配置信息.
 * <strong>
 * 2012年8月8日 17:04:41
 * 凡平: 如果表和奖励之间填错, 变成三维的, 那么技术没办法维护, 由策划负责人
 * </strong>
 * 
 * @author Hyint
 */
@Resource
public class ChargeConfig implements Comparable<ChargeConfig>{

	/** 充值礼包ID */
	@Id
	private int id;
	
	/** 充值条件序号. */
	private int serial;
	
	/**
	 * 礼包的充值类型. 
	 * 
	 * 会按照这个值来进行解析
	 * <li>1-首次充值 </li>
	 * <li>2-累积充值 </li>
	 * <li>3-循环活动</li>
	 */
	private int type;

	/** 充值礼包名字 */
	private String name;	
	
	/** 充值礼包类型. 策划自定义的 */
	@Index(name=IndexName.GIVE_CHARGE_TYPE)
	private int giftType;
	
	/** 周期天数. -1: 永久生效. >= 0 则表示生效的天数*/
	private int cycle = -1;
	
	/** 活动循环顺序 */
	private int sequence = 0;

	/** 面板排序. 1-首冲, 2-单个奖励, 3-多个奖励 */
	private int panelType;
	
	/** 充值类型. */
	private int chargeType = ChargeGiftType.SINGLE_RECHARGE;
	
	/** 周期总值 */
	@JsonIgnore
	private transient int totalCycle = 0;
	
	/** 充值条件列表 */
	@JsonIgnore
	private transient List<ChargeConditionConfig> conditions;
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getGiftType() {
		return giftType;
	}

	public void setGiftType(int giftType) {
		this.giftType = giftType;
	}

	public int getSerial() {
		return serial;
	}

	public void setSerial(int serial) {
		this.serial = serial;
	}

	public int getCycle() {
		return cycle;
	}

	public void setCycle(int cycle) {
		this.cycle = cycle;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	
	public List<ChargeConditionConfig> getConditions() {
		return conditions;
	}

	public void setConditions(List<ChargeConditionConfig> conditions) {
		this.conditions = conditions;
	}

	public int getChargeType() {
		return chargeType;
	}

	public void setChargeType(int chargeType) {
		this.chargeType = chargeType;
	}
	
	
	public int compareTo(ChargeConfig o) {
		if(this.sequence == o.getSequence()) {
			return 0;
		}
		if(this.sequence < o.getSequence()) {
			return -1;
		}
		return 1;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getPanelType() {
		return panelType;
	}

	public boolean isSingleRewardViews() {
		return this.panelType != 3;
	}
	
	public void setPanelType(int panelType) {
		this.panelType = panelType;
	}

	public boolean canCycle() {
		return this.sequence > 0;
	}
	
	public int getTotalCycle() {
		return totalCycle;
	}

	public void setTotalCycle(int totalCycle) {
		this.totalCycle = totalCycle;
	}

	
	public String toString() {
		return "RechargeConfig [id=" + id + ", serial=" + serial + ", name=" + name + ", giftType="
				+ giftType + ", cycle=" + cycle + ", sequence=" + sequence + ", conditions="
				+ conditions + "]";
	}

	
}
