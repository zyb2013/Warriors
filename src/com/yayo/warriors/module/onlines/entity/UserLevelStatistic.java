package com.yayo.warriors.module.onlines.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;

/**
 * 角色在线统计 记录
 * @author liuyuhua
 */
@Entity
@Table(name="userLevelStatistic")
public class UserLevelStatistic extends BaseModel<String> {
	private static final long serialVersionUID = 4265890439671393298L;
	
	/** 记录时间,格式(年月日)*/
	@Id
	@Column(name="recordDate")
	private String id;
	
	/** 格式:{等级_玩家数_当前在线数|等级_玩家数_当前在线数}*/
	@Lob
	private String data;
	
	
	/**
	 * 获取格式化时间
	 * @return {@link String} 时间格式
	 */
	public String builRecordDate(){
		return DateUtil.date2String(new Date(), DatePattern.PATTERN_YYYYMMDDHHMM);
	}
	
	//Setter and Getter...
	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "UserLevelStatistic [id=" + id + ", data=" + data + "]";
	}

}
