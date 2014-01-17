package com.yayo.warriors.module.onlines.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;
import com.yayo.warriors.module.user.type.Camp;

/**
 * 阵营在线统计
 * @author jonsai
 *
 */
@Entity
@Table(name="campOnline")
public class CampOnline extends BaseModel<Long> {
	private static final long serialVersionUID = -2996319126643030154L;
	/** 主键 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	/** 豪杰总人数 */
	int knifeTotal;
	
	/** 豪杰在线人数 */
	int knifeOnline;
	
	/** 侠客总人数 */
	int swordTotal;
	
	/** 侠客在线人数  */
	int swordOnline;
	
	/** 记录的时间. 格式: 年-月-日 */
	private String recordDate = "";

	/** 记录的时间. 时:分 */
	private String recordTime = "";
	
	//--------------------
	/**
	 * 构造器
	 * @param knifeTotal
	 * @param swordTotal
	 * @param campOnlineArray
	 * @return
	 */
	public static CampOnline valueOf(int knifeTotal, int swordTotal, AtomicInteger[] campOnlineArray){
		CampOnline campOnline = new CampOnline();
		campOnline.knifeTotal = knifeTotal;
		campOnline.knifeOnline = campOnlineArray[Camp.KNIFE_CAMP.ordinal()].get();
		campOnline.swordTotal = swordTotal;
		campOnline.swordOnline = campOnlineArray[Camp.SWORD_CAMP.ordinal()].get();
		
		Date currentTime = Calendar.getInstance().getTime();
		campOnline.recordTime = DateUtil.date2String(currentTime, "HH:00");
		campOnline.recordDate = DateUtil.date2String(currentTime, DatePattern.PATTERN_YYYY_MM_DD);
		
		return campOnline;
	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public int getKnifeTotal() {
		return knifeTotal;
	}

	public void setKnifeTotal(int knifeTotal) {
		this.knifeTotal = knifeTotal;
	}

	public int getKnifeOnline() {
		return knifeOnline;
	}

	public void setKnifeOnline(int knifeOnline) {
		this.knifeOnline = knifeOnline;
	}

	public int getSwordTotal() {
		return swordTotal;
	}

	public void setSwordTotal(int swordTotal) {
		this.swordTotal = swordTotal;
	}

	public int getSwordOnline() {
		return swordOnline;
	}

	public void setSwordOnline(int swordOnline) {
		this.swordOnline = swordOnline;
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
