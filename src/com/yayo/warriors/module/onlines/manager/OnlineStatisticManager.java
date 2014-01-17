package com.yayo.warriors.module.onlines.manager;

import java.util.Date;
import java.util.List;

import com.yayo.warriors.module.onlines.entity.CampOnline;
import com.yayo.warriors.module.onlines.entity.OnlineStatistic;
import com.yayo.warriors.module.onlines.entity.RegisterDetailStatistic;
import com.yayo.warriors.module.onlines.entity.RegisterStatistic;
import com.yayo.warriors.module.onlines.entity.UserLevelStatistic;
import com.yayo.warriors.module.server.listener.LoginListener;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.module.user.type.Job;

/**
 * 在线统计接口
 * 
 * @author Hyint
 */
public interface OnlineStatisticManager extends LoginListener, LogoutListener {

	/**
	 * 获得在线统计对象
	 * 
	 * @param  statisticId					统计ID
	 * @return {@link OnlineStatistic}		在线统计信息	
	 */
	OnlineStatistic getOnlineStatistic(long statisticId);
	
	/**
	 * 列出在线统计对象列表
	 * 
	 * @param  recordDate					统计的日期. 格式: 年-月-日
	 * @param  recordTime					统计的时间. 格式: 时:分
	 * @return {@link List}					在线统计对象列表
	 */
	List<OnlineStatistic> listOnlineStatistic(String recordDate, String recordTime);
	
	/**
	 * 列出在线统计对象列表
	 * 
	 * @param  List<String[]>				String[0]:日期， 格式:yyyy-MM-dd	String[1]:时间， 格式:HH:mm(不包含)
	 * @return {@link List<OnlineStatistic>}在线统计对象列表
	 */
	List<OnlineStatistic> listOnlineStatistic(List<String[]> dateTimes);
	
	/**
	 * 列出在线统计对象列表
	 * 
	 * @param  dateTimes					String:日期， 格式:yyyy-MM-dd
	 * @return {@link List<OnlineStatistic>}在线统计对象列表
	 */
	List<OnlineStatistic> listOnlineStatisticByDate(String dateTimes);
	
	/**
	 * 列出在线统计对象列表
	 * 
	 * @param  dateTimes					String:日期， 格式:yyyy-MM-dd
	 * @return {@link List<RegisterStatistic>}注册统计对象列表
	 */
	List<RegisterStatistic> listRegisterStatisticByDate(String dateTimes);
	
	/**
	 * 列出在线统计对象列表
	 * 
	 * @param  date					String:日期， 格式:yyyy-MM-dd
	 * @return {@link List<CampOnline>}注册统计对象列表
	 */
	List<CampOnline> listCampOnlineByDate(String date);
	
	/**
	 * 取得开服至今最大的在线人数
	 * @return	[Integer]
	 */
	int getSinceMaxCount();
	
	/**
	 * 取得总注册人数
	 * @return
	 */
	int getTotalRegCount();
	
	/**
	 * 增加职业注册人数
	 */
	void addJobRegisterRecord(Job job);
	
	/**
	 * 增加阵营注册人数
	 * 
	 * @param camp		角色阵营
	 */
	void addCampRegisterRecord(Camp camp);
	
	/**
	 * 增加阵营在线
	 * 
	 * @param camp		角色阵营
	 */
	void addCampOnline(Camp camp);
	
	/**
	 * 减少阵营在线
	 * @param camp
	 */
	void subCampOnline(Camp camp);
	
	/**
	 * 取得阵营人数
	 * @param camp
	 * @return
	 */
	int getCampPlayerCount(Camp camp);
	
	/**
	 * 获取在线玩家等级统计
	 * @param date ({@link String} 年月日)  时间
	 * @return {@link UserLevelStatistic}  在线玩家统计
	 */
	UserLevelStatistic getUserLevelStatistics(String date);
	
	/**
	 * 获取注册明细统计
	 * @param startDate   开始时间
	 * @param endDate     结束时间
	 * @return
	 */
	List<RegisterDetailStatistic> loadRegisterDetailStatistics(Date startDate, Date endDate);
}
