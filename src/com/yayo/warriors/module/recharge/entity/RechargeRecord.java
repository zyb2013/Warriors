package com.yayo.warriors.module.recharge.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Splitable;

/**
 * 充值记录信息
 * 
 * @author Hyint
 */
@Entity
@Table(name="rechargeRecord")
public class RechargeRecord extends BaseModel<Long> {
	private static final long serialVersionUID = -3049184822133201123L;
	
	/** 自增ID */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	/** 角色ID */
	private Long playerId = 0L;
	
	/** 记录的时间. yyyy-MM-dd. */
	@Temporal(TemporalType.DATE)
	private Date recordTime = null;
	
	/** 单日充值信息, 金额1_金额2_..... */
	@Lob
	private String recharge;
	
	/** 单日累积充值金额 */
	private long totalRecharge;
	
	/** 充值列表 */
	@Transient
	private transient List<Integer> chargeList; 
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getRecharge() {
		return recharge;
	}

	public void setRecharge(String recharge) {
		this.recharge = recharge;
	}

	public long getTotalRecharge() {
		return totalRecharge;
	}

	public void setTotalRecharge(long totalRecharge) {
		this.totalRecharge = totalRecharge;
	}

	public Long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	public Date getRecordTime() {
		return recordTime;
	}

	public void setRecordTime(Date recordTime) {
		this.recordTime = recordTime;
	}

	/**
	 * 获得充值信息列表
	 * 
	 * @return {@link List}		充值金额列表
	 */
	public List<Integer> getChargeList() {
		if(this.chargeList != null) {
			return this.chargeList;
		}
		synchronized (this) {
			if(this.chargeList != null) {
				return this.chargeList;
			}
			this.chargeList = new LinkedList<Integer>();
			if(StringUtils.isBlank(this.recharge)) {
				return this.chargeList;
			}
			
			String[] elements = this.recharge.split(Splitable.ATTRIBUTE_SPLIT);
			for (String element : elements) {
				if(!StringUtils.isBlank(element)) {
					this.chargeList.add(Integer.valueOf(element));
				}
			}
		}
		return this.chargeList;
	}
	
	/**
	 * 增加元宝到列表中
	 * 
	 * @param addGolden
	 */
	public void addRecharge2List(int addGolden) {
		this.totalRecharge += addGolden;
		this.getChargeList().add(addGolden);
		
	}
	
	/**
	 * 更新充值信息
	 */
	public void updateChargeList() {
		StringBuffer builder = new StringBuffer();
		List<Integer> list = this.getChargeList();
		for (Integer charges : list) {
			builder.append(charges).append(Splitable.ATTRIBUTE_SPLIT);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		this.recharge = builder.toString();
	}
	
	/**
	 * 获得记录的时间
	 * 
	 * @param  recordDate		记录的时间
	 * @return {@link Date}		时间对象
	 */
	public static Date toRecordDate(Date recordDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(recordDate);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	@Override
	public String toString() {
		return "RechargeRecord [id=" + id + ", playerId=" + playerId + ", recordTime=" + recordTime
				+ ", recharge=" + recharge + ", totalRecharge=" + totalRecharge + "]";
	}
	
	
}
