package com.yayo.warriors.module.onlines.entity;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.yayo.common.db.model.BaseModel;

/**
 * 注册明细统计 
 * @author liuyuhua
 */
@Entity
@Table(name="registerDetailStatistic")
public class RegisterDetailStatistic extends BaseModel<Long> {
	private static final long serialVersionUID = -3141115130336768746L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	/** 时间区间内服务器总登陆次数*/
	private long loginCount;
	
	/** 时间区间内服务器登陆人数*/
	private int userCount;
	
	/** 时间区间内创建角色的数量*/
	private int createCount;
	
	/** 注册明细开始时间*/
	private Date startTime;
	
	/** 注册明细结束时间*/
	private Date endTime;
	
	/**
	 * 注册明细统计
	 * @param loginCount   登陆次数
	 * @param userCount    登陆人数
	 * @param createCount  建角色的数量
	 * @param startTime    开始时间
	 * @param endTime      结束时间
	 * @return {@link RegisterDetailStatistic} 注册明细统计对象
	 */
	public static RegisterDetailStatistic valueOf(long loginCount, int userCount, int createCount, Date startTime, Date endTime){
		RegisterDetailStatistic statistic = new RegisterDetailStatistic();
		statistic.loginCount = loginCount;
		statistic.userCount = userCount;
		statistic.createCount = createCount;
		statistic.startTime = startTime;
		statistic.endTime = endTime;
		return statistic;
	}
	
	
	//Getter and Setter...
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	public long getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(long loginCount) {
		this.loginCount = loginCount;
	}

	public int getUserCount() {
		return userCount;
	}

	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}

	public int getCreateCount() {
		return createCount;
	}

	public void setCreateCount(int createCount) {
		this.createCount = createCount;
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

	@Override
	public String toString() {
		return "RegisterDetailStatistic [id=" + id + ", loginCount="
				+ loginCount + ", userCount=" + userCount + ", createCount="
				+ createCount + ", startTime=" + startTime + ", endTime="
				+ endTime + "]";
	}

}
