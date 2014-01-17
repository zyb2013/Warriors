package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.type.IndexName;

/**
 * 在线活动 
 * <per>
 * 有效逻辑配置:
 * 打坐倍数加成
 * 打怪倍数加成
 * 国运倍数加成
 * </per>
 * @author liuyuhua
 */
@Resource
public class ActiveOnlineConfig {
	
	@Id
	private int id;
	
	/** 活动类型*/
	@Index(name=IndexName.ACTIVE_ONLINE_TYPE, order = 0)
	private int type;
	
	/** 阵营*/
	@Index(name=IndexName.ACTIVE_ONLINE_TYPE, order = 1)
	private int camp;
	
	/** 活动名字*/
	private String activeName;
	
	/** 活动说明*/
	private String description;
	
	/** 收益的倍数*/
	private float profit;
	
	/** 开启时间*/
	private String openRegionTime;

	/** 开启周期区间 格式:1(星期日)|2(星期一)|3(星期二)|7(星期六)*/
	private String openRegionDay;
	
	/** 参与的最低等级*/
	private int level;
	
	/** 解析{@link DungeonConfig#openRegionDay}*/
	@JsonIgnore
	private transient List<Integer> regionDays = null;
	
	/**
	 * 是否开放
	 * @return true 可以进入 false 不可以进入
	 */
	public boolean isOpen(){
		if(openRegionTime == null || openRegionTime.isEmpty()){
			return false;
		}
		
		Calendar calendar = Calendar.getInstance();
		int currentHour = calendar.get(Calendar.HOUR_OF_DAY); 
		int currentMinute = calendar.get(Calendar.MINUTE); 
		int currentWeek = calendar.get(Calendar.DAY_OF_WEEK);
		
		if(!this.getRegionDays().contains(currentWeek)){ //不存在开发的周期
			return false;
		}
		
		String[] times = openRegionTime.split(Splitable.ELEMENT_SPLIT);
		boolean falg = false;
		for(String time : times){
			String[] split = time.split("-");
			String beginTime = split[0];
			String endTime = split[1];
			String[] splitBeginTime = beginTime.split(Splitable.DELIMITER_ARGS); 
			String[] splitEndTime = endTime.split(Splitable.DELIMITER_ARGS);
			int beginHour = Integer.parseInt(splitBeginTime[0]);
			int beginMinute = Integer.parseInt(splitBeginTime[1]);
			int endHour = Integer.parseInt(splitEndTime[0]);
			int endMinute = Integer.parseInt(splitEndTime[1]);
			
			if(currentHour >= beginHour && currentHour <= endHour){
				falg = true;
			}
			
			if(currentHour == beginHour){ //判断是否到达开始的时间
				if(currentMinute < beginMinute){
					falg = false;
				}
			}
			if(currentHour == endHour){ //判断是否达到结束的时间
				if(currentMinute > endMinute){
					falg = false;
				}
			}
			
			if(falg == true){
				return falg;
			}
		}
		
		return false;
	}
	
	/**
	 * 获取开放的具体时间
	 * @return {@link List} 集合
	 */
	private List<Integer> getRegionDays(){
		if(regionDays != null){
			return regionDays;
		}
		
		synchronized (this) {
			if(regionDays != null){
				return regionDays;
			}
			
			regionDays = new ArrayList<Integer>(7);
			if(openRegionDay != null && !openRegionDay.isEmpty()){
				String[] tmpDays = openRegionDay.split(Splitable.ELEMENT_SPLIT);
				for(String day : tmpDays){
					regionDays.add(Integer.parseInt(day));
				}
			}
			return regionDays;
		}
	}
	
	//Getter and Setter...

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCamp() {
		return camp;
	}

	public void setCamp(int camp) {
		this.camp = camp;
	}

	public String getActiveName() {
		return activeName;
	}

	public void setActiveName(String activeName) {
		this.activeName = activeName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public float getProfit() {
		return profit;
	}

	public void setProfit(float profit) {
		this.profit = profit;
	}

	public String getOpenRegionTime() {
		return openRegionTime;
	}

	public void setOpenRegionTime(String openRegionTime) {
		this.openRegionTime = openRegionTime;
	}

	public String getOpenRegionDay() {
		return openRegionDay;
	}

	public void setOpenRegionDay(String openRegionDay) {
		this.openRegionDay = openRegionDay;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public String toString() {
		return "ActiveOnlineConfig [id=" + id + ", type=" + type + ", camp="
				+ camp + ", activeName=" + activeName + ", description="
				+ description + ", profit=" + profit + ", openRegionTime="
				+ openRegionTime + ", openRegionDay=" + openRegionDay
				+ ", level=" + level + "]";
	}

}
