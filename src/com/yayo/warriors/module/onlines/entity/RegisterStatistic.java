package com.yayo.warriors.module.onlines.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.module.user.type.Job;

/**
 * 注册账号信息
 * 
 * @author jonsai
 */
@Entity
@Table(name="registerStatistic")
public class RegisterStatistic extends BaseModel<Long> {
	private static final long serialVersionUID = 5007682920782046859L;

	/** 主键 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	/** 0 - 无阵营人数 */
	int campNone;
	
	/** 1 - 豪杰营人数 */
	int campKnife;
	
	/** 2 - 侠客营人数*/
	int campSword;

	/** 1 - 天龙 */
	int jobTianlong;

	/** 2 - 天山 */
	int jobTianshan;
	
	/** 3 - 星宿 */
	int jobXingxiu;
	
	/** 4 - 逍遥 */
	int jobXiaoyao;
	
	/** 记录的时间. 格式: 年-月-日 */
	private String recordDate = "";
	
	/** 记录的时间. 时:分 */
	private String recordTime = "";

	/**
	 * 构建注册统计信息
	 * 
	 * @param	regStatMap				注册记录map
	 * @return {@link RegisterStatistic}	在线统计对象
	 */
	public static RegisterStatistic valueOf(Map<Integer, Integer> regStatMap) {
		RegisterStatistic record = new RegisterStatistic();
		record.campNone = regStatMap.containsKey(Camp.NONE.ordinal()) ? regStatMap.get(Camp.NONE.ordinal()) : 0 ;
		record.campKnife = regStatMap.containsKey(Camp.KNIFE_CAMP.ordinal()) ? regStatMap.get(Camp.KNIFE_CAMP.ordinal()) : 0 ;
		record.campSword = regStatMap.containsKey(Camp.SWORD_CAMP.ordinal()) ? regStatMap.get(Camp.SWORD_CAMP.ordinal()) : 0 ;
		
		record.jobTianlong = regStatMap.containsKey(Job.TIANLONG.ordinal() + 10) ? regStatMap.get(Job.TIANLONG.ordinal() + 10) : 0 ;
		record.jobTianshan = regStatMap.containsKey(Job.TIANSHAN.ordinal() + 10) ? regStatMap.get(Job.TIANSHAN.ordinal() + 10) : 0 ;
		record.jobXiaoyao = regStatMap.containsKey(Job.XIAOYAO.ordinal() + 10) ? regStatMap.get(Job.XIAOYAO.ordinal() + 10) : 0 ;
		record.jobXingxiu = regStatMap.containsKey(Job.XINGXIU.ordinal() + 10) ? regStatMap.get(Job.XINGXIU.ordinal() + 10) : 0 ;

		Date currentTime = Calendar.getInstance().getTime();
		record.recordTime = DateUtil.date2String(currentTime, DatePattern.PATTERN_HH_MM);
		record.recordDate = DateUtil.date2String(currentTime, DatePattern.PATTERN_YYYY_MM_DD);
		return record;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getCampNone() {
		return campNone;
	}

	public void setCampNone(int campNone) {
		this.campNone = campNone;
	}

	public int getCampKnife() {
		return campKnife;
	}

	public void setCampKnife(int campKnife) {
		this.campKnife = campKnife;
	}

	public int getCampSword() {
		return campSword;
	}

	public void setCampSword(int campSword) {
		this.campSword = campSword;
	}

	public int getJobTianlong() {
		return jobTianlong;
	}

	public void setJobTianlong(int jobTianlong) {
		this.jobTianlong = jobTianlong;
	}

	public int getJobTianshan() {
		return jobTianshan;
	}

	public void setJobTianshan(int jobTianshan) {
		this.jobTianshan = jobTianshan;
	}

	public int getJobXingxiu() {
		return jobXingxiu;
	}

	public void setJobXingxiu(int jobXingxiu) {
		this.jobXingxiu = jobXingxiu;
	}

	public int getJobXiaoyao() {
		return jobXiaoyao;
	}

	public void setJobXiaoyao(int jobXiaoyao) {
		this.jobXiaoyao = jobXiaoyao;
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
	
	
}
