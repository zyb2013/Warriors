package com.yayo.warriors.module.gift.entity;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.DateUtil;

@Entity
@Table(name="userOnlineGift")
public class UserOnlineGift extends BaseModel<Long> {

	private static final long serialVersionUID = -1854971585590501912L;
	
	@Id
	@Column(name="playerId")
	private Long id;
	
	/** 已领取的在线礼包Id */
	private int onlineGiftId;
	
	/** 领取时间 */
	private Date openTime;

	/** 在线礼包结束时间 */
	private Date endTime;
	
	/** 从领取时间开始的在线时间 */
	private long onlineTime = 0L;
	
	/** 数据清0时间 */
	private int cleanDate = 0;

	
	public static UserOnlineGift valueOf(Long playerId) {
		UserOnlineGift onlineGift = new UserOnlineGift();
		onlineGift.id = playerId;
		return onlineGift;
	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	public int getOnlineGiftId() {
		return onlineGiftId;
	}

	public void setOnlineGiftId(int onlineGiftId) {
		this.onlineGiftId = onlineGiftId;
	}

	public Date getOpenTime() {
		return openTime;
	}

	public void setOpenTime(Date openTime) {
		this.openTime = openTime;
	}

	public long getOnlineTime() {
		return onlineTime;
	}

	public void setOnlineTime(long onlineTime) {
		this.onlineTime = onlineTime;
	}
	
	public void addOnlineTime(long onlineTime) {
		this.onlineTime += onlineTime;
	}
	
	private boolean needToClean() {
		return cleanDate != Calendar.getInstance().get(Calendar.DATE);
	}
	
	/**
	 * 礼包数据清0
	 * 
	 * @return
	 */
	public synchronized boolean cleanData() {
		if (needToClean()) {
			cleanDate = Calendar.getInstance().get(Calendar.DATE);
			onlineTime = 0L;
			onlineGiftId = 0;
			openTime = new Date();
			
			if (endTime == null) {           // 如果到期时间为空, 开启在线礼包
				endTime = DateUtil.changeDateTime(openTime, 8, 0, 0, 0);
			}
			
			return true;
		}
		return false;
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
		if (!(obj instanceof UserOnlineGift))
			return false;
		UserOnlineGift other = (UserOnlineGift) obj;
		return other.id != null && id.equals(other.id);
	}


}
