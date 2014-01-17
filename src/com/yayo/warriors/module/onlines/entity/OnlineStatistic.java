package com.yayo.warriors.module.onlines.entity;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;

/**
 * 在线记录信息
 * 
 * @author Hyint
 */
@Entity
@Table(name="onlineStatistic")
public class OnlineStatistic extends BaseModel<Long> {
	private static final long serialVersionUID = -5035757079450391860L;

	/** 主键 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	/** 最小在线人数 */
	private int minCount = 0;

	/** 最大在线人数 */
	private int maxCount = 0;
	
	/** 记录的时间. 格式: 年-月-日 */
	private String recordDate = "";
	
	/** 记录的时间. 时:分 */
	private String recordTime = "";
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	

	@Override
	public Long getIdentity() {
		return id;
	}

	public String getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(String recordDate) {
		this.recordDate = recordDate;
	}

	public String getRecordTime() {
		return recordTime;
	}

	public void setRecordTime(String recordTime) {
		this.recordTime = recordTime;
	}

	public Integer getMinCount() {
		return minCount;
	}

	public void setMinCount(Integer minCount) {
		this.minCount = minCount;
	}

	public Integer getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(Integer maxCount) {
		this.maxCount = maxCount;
	}

	@Override
	public String toString() {
		return "OnlineRecord [id=" + id + ", recordDate=" + recordDate + ", recordTime="
				+ recordTime + ", minCount=" + minCount + ", maxCount=" + maxCount + "]";
	}

	/**
	 * 构建在线统计信息
	 * 
	 * @param	minCount				最大在线人数
	 * @param 	maxCount				最小在线人数
	 * @return {@link OnlineStatistic}	在线统计对象
	 */
	public static OnlineStatistic valueOf(int minCount, int maxCount) {
		OnlineStatistic onlineRecord = new OnlineStatistic();
		onlineRecord.maxCount = maxCount;
		onlineRecord.minCount = minCount;

		Date currentTime = Calendar.getInstance().getTime();
		onlineRecord.recordTime = DateUtil.date2String(currentTime, DatePattern.PATTERN_HH_MM);
		onlineRecord.recordDate = DateUtil.date2String(currentTime, DatePattern.PATTERN_YYYY_MM_DD);
		return onlineRecord;
	}
}
