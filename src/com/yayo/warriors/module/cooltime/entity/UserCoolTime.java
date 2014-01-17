package com.yayo.warriors.module.cooltime.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.cooltime.model.CoolTime;

/**
 * 用户CD对象
 * 
 * @author Hyint
 */
@Entity
@Table(name="userCoolTime")
public class UserCoolTime extends BaseModel<Long> implements Serializable {
	private static final long serialVersionUID = 8143428554108153934L;
	
	/**
	 * 角色ID 
	 */
	@Id
	@Column(name="playerId")
	private Long id;
	
	/** CD时间. 格式: 冷却时间ID1_结束时间1(yyyyMMddHHmmss)|冷却时间ID2_结束时间2(yyyyMMddHHmmss)|... 
	 */
	@Lob
	private String coolTime = "";
	
	/** 是否忙碌中 */
	private transient volatile boolean busy = false;
	
	/** 冷却时间集合 */
	@Transient
	private transient volatile ConcurrentHashMap<Integer, CoolTime> coolTimeMaps;
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getCoolTime() {
		return coolTime;
	}

	public void setCoolTime(String coolTime) {
		this.coolTime = coolTime;
	}
	
	/**
	 * 获得冷却时间集合对象
	 *  
	 * @return {@link ConcurrentHashMap}	返回时间表集合
	 */
	private ConcurrentHashMap<Integer, CoolTime> getCoolTimes() {
		if(this.coolTimeMaps != null) {
			return this.coolTimeMaps;
		}
		
		synchronized (this) {
			if(this.coolTimeMaps != null) {
				return coolTimeMaps;
			}
			
			this.coolTimeMaps = new ConcurrentHashMap<Integer, CoolTime>(3);
			List<String[]> array = Tools.delimiterString2Array(this.coolTime);
			if(array == null || array.isEmpty()) {
				return this.coolTimeMaps;
			}
			
			String pattern = DatePattern.PATTERN_YYYYMMDDHHMMSS;
			for (String[] element : array) {
				if(element == null || element.length < 2) {
					continue;
				}
				
				Integer cdId = Integer.valueOf(element[0]);
				Date endTime = DateUtil.string2Date(element[1], pattern);
				if(cdId != null && endTime != null) {
					this.coolTimeMaps.put(cdId, CoolTime.valueOf(cdId, endTime.getTime()));
				}
			}
		}
		return this.coolTimeMaps;
	}
	
	/**
	 * 构建CD冷却对象
	 * 
	 * @param  playerId				角色ID
	 * @return {@link UserCoolTime}	用户CD时间对象
	 */
	public static UserCoolTime valueOf(long playerId) {
		UserCoolTime userCoolTime = new UserCoolTime();
		userCoolTime.id = playerId;
		return userCoolTime;
	}
	
	/**
	 * 是否在CD中..
	 * 
	 * @param  coolTimeIds		CDID列表
	 * @return {@link Boolean}	true-CD中, false-没在CD中
	 */
	public boolean isCoolTiming(int...coolTimeIds) {
		ConcurrentHashMap<Integer, CoolTime> coolTimes = this.getCoolTimes();
		for (int coolId : coolTimeIds) {
			CoolTime cd = coolTimes.get(coolId);
			if(cd != null && !cd.isTimeOut()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 更新冷却时间集合
	 */
	public synchronized UserCoolTime updateCoolTimes() {
		StringBuilder builder = new StringBuilder();
		Collection<CoolTime> values = this.getCoolTimes().values();
		for (CoolTime coolTime : values) {
			if(!coolTime.isTimeOut()) {
				builder.append(coolTime).append(Splitable.ELEMENT_DELIMITER);
			}
		}
		if(builder.length() > 0) {
			builder.charAt(builder.length() - 1);
		}
		this.coolTime = builder.toString();
		return this;
	}
	
	/**
	 * 增加冷却时间
	 * 
	 * @param coolTimeId		冷却时间CDID
	 * @param endTimeMillis		CD时长
	 */
	public void addCoolTime(int coolId, int len) {
		ConcurrentHashMap<Integer, CoolTime> coolTimes = getCoolTimes();
		CoolTime cd = coolTimes.get(coolId);
		if(cd == null && coolId >= 0 && len >= 0) {
			cd = CoolTime.valueOf(coolId, 0);
			coolTimes.put(coolId, cd);
		}
		
		if(cd != null) {
			cd.setEndTime(System.currentTimeMillis() + len);
		}
	}
	
	public CoolTime getCoolTime(int coolTimeId) {
		CoolTime coolTimeObj = null;
		if(coolTimeId >= 0) {
			ConcurrentHashMap<Integer, CoolTime> coolTimes = this.getCoolTimes();
			coolTimeObj = coolTimes.get(coolTimeId);
			if(coolTimeObj == null) {
				coolTimeObj = CoolTime.valueOf(coolTimeId, 0L);
				coolTimes.put(coolTimeId, coolTimeObj);
			}
		}
		return coolTimeObj;
	}
	
	public void putCoolTime(CoolTime coolTime) {
		if(coolTime != null) {
			getCoolTimes().put(coolTime.getId(), coolTime);
		}
	}
	/**
	 * 移除CD
	 * 
	 * @param coolTime
	 */
	public void removeCoolTime(int...coolTime) {
		ConcurrentHashMap<Integer, CoolTime> coolTimes = getCoolTimes();
		for (int coolId : coolTime) {
			coolTimes.remove(coolId);
		}
	}

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}

	@Override
	public String toString() {
		return "UserCoolTime [id=" + id + ", coolTime=" + coolTime + "]";
	}
}
