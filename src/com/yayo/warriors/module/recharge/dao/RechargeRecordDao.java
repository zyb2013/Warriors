package com.yayo.warriors.module.recharge.dao;

import java.util.Date;
import java.util.List;

import com.yayo.common.db.dao.CommonDao;

/**
 * 充值DAO接口
 * 
 * @author Hyint
 */
public interface RechargeRecordDao extends CommonDao {

	/**
	 * 获得充值记录ID列表
	 * 
	 * @param playerId		角色ID
	 * @param startTime		开始时间
	 * @param endTime		结束时间
	 * @return
	 */
	List<Long> getRechargeRecordIds(long playerId, Date startTime, Date endTime);
	
	/**
	 * 获得充值记录对象
	 * 
	 * @param  playerId		角色ID
	 * @param  recordDate	日期对象
	 * @return {@link Long}	ID
	 */
	Long getRechargeRecord(long playerId, Date recordDate);
}
