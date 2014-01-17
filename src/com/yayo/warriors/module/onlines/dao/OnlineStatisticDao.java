package com.yayo.warriors.module.onlines.dao;

import java.util.Date;
import java.util.List;

import com.yayo.common.db.dao.CommonDao;
import com.yayo.warriors.module.onlines.entity.RegisterDetailStatistic;

/**
 * 在线统计DAO接口
 * 
 * @author Hyint
 */
public interface OnlineStatisticDao extends CommonDao {

	/**
	 * 列出在线统计对象列表
	 * 
	 * @param  recordDate				统计的日期. 格式: 年-月-日
	 * @param  recordTime				统计的时间. 格式: 时:分
	 * @return {@link List}				在线统计对象列表
	 */
	List<Long> listOnlineStatisticId(String recordDate, String recordTime);
	
	/**
	 * 取得开服至今最大的在线人数
	 * @return	[Integer]
	 */
	int getSinceMaxCount();
	
	/**
	 * 列出注册统计
	 * @param recordDate
	 * @param recordTime
	 * @return
	 */
	List<Long> listRegisterStatisticId(String recordDate, String recordTime);
	
	/**
	 * 列出阵营在线统计
	 * @param recordDate
	 * @param recordTime
	 * @return
	 */
	List<Long> listCampOnlineId(String recordDate, String recordTime);
	
	/**
	 * 加载注册用户数量
	 * @return
	 */
	List<Object> loadRegisterCount();
	
	/**
	 * 获取所有玩家等级
	 * @return {@link List} 等级集合
	 */
	List<Integer> getAllPlayerLevel();
	
	/**
	 * 加载全服玩家总共登陆次数
	 * @return
	 */
	long loadTotleLoginCount();
	
	/**
	 * 加载指定时间内登陆的玩家
	 * @param startDate 开始时间
	 * @param endDate   结束时间
	 * @return
	 */
	int loadLoginCount4Time(Date startDate,Date endDate);
	
	/**
	 * 加载指定时间内创建的玩家
	 * @param startDate 开始时间
	 * @param endDate   结束时间
	 * @return
	 */
	int loadCreateCount4Time(Date startDate,Date endDate);
	
	/**
	 * 查找注册明细统计
	 * @param startDate 开始时间 
	 * @param endDate   结束时间
	 * @return
	 */
	List<RegisterDetailStatistic> loadRegisterDetailStatistics(Date startDate,Date endDate);
}
