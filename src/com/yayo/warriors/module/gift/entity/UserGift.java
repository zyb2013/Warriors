package com.yayo.warriors.module.gift.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Splitable;

/**
 * 用户礼包
 * 
 * @author huachaoping
 */
@Entity
@Table(name="userGift")
public class UserGift extends BaseModel<Long> {

	private static final long serialVersionUID = -1034050018164488989L;

	/** 主键 */
	@Id
	@Column(name="playerId")
	private Long id;
	
	/** 领取的GiftId */
	@Lob
	private String receivedGiftId = "";
	
	/** 领取时间 */
	private Date receiveTime;	
	
	@Transient
	private transient Set<Integer> received = null;
	
	
	public static UserGift valueOf(long playerId) {
		UserGift userGift = new UserGift();
		userGift.id = playerId;
		return userGift;
	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getReceivedGiftId() {
		return receivedGiftId;
	}

	public void setReceivedGiftId(String receivedGiftId) {
		this.receivedGiftId = receivedGiftId;
	}

	public Date getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(Date receiveTime) {
		this.receiveTime = receiveTime;
	}

	
	public void add2Received(int giftId) {
		this.getReceiveIdSet().add(giftId);
		updateReceivedGift();
	}
	
	public void updateReceivedGift() {
		StringBuilder builder = new StringBuilder();
		Set<Integer> set = this.getReceiveIdSet();
		for (Integer element : set) {
			builder.append(element).append(Splitable.ATTRIBUTE_SPLIT);
		}
		if (builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		this.receivedGiftId = builder.toString();
	}
	
	public Set<Integer> getReceiveIdSet() {
		if (this.received != null) {
			return this.received;
		}
		
		synchronized (this) {
			if(this.received != null) {
				return this.received;
			}
			this.received = new HashSet<Integer>();
			if(StringUtils.isBlank(this.receivedGiftId)) {
				return this.received;
			}

			String[] array = this.receivedGiftId.split(Splitable.ATTRIBUTE_SPLIT);
			for (String element : array) {
				this.received.add(Integer.valueOf(element));
			}
		}
		return this.received;
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
		if (!(obj instanceof UserGift))
			return false;
		UserGift other = (UserGift) obj;
		return id != null && other.id != null && id.equals(other.id);
	}
	
}
