package com.yayo.warriors.module.recharge.manager;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.yayo.warriors.module.recharge.entity.RechargeRecord;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 充值接口
 * 
 * @author Hyint
 */
public interface RechargeManager {

	/**
	 * 增加金币到记录信息中
	 * 
	 * @param  userDomain 		用户域模型
	 * @param  addGolden		增加的金币数量
	 */
	void addRecharge2Record(UserDomain userDomain, long addGolden);
	
	/**
	 * 列出充值记录ID列表
	 * 
	 * @param  playerId			角色ID
	 * @param  startDate		开始时间
	 * @param  endDate			结束时间
	 * @return {@link List}		主键ID列表
	 */
	List<Long> listRechargeRecordIds(long playerId, Date startDate, Date endDate);
	
	/**
	 * 查询充值记录对象
	 * 
	 * @param  recordId					充值记录ID
	 * @return {@link RechargeRecord}	充值记录对象
	 */
	RechargeRecord getRechargeRecord(long recordId);

	/**
	 * 查询充值记录对象
	 * 
	 * @param  recordIdList				充值记录ID列表
	 * @return {@link List}				充值记录对象列表
	 */
	List<RechargeRecord> listRechargeRecord(Collection<Long> recordIdList);
	
	/**
	 * 移除角色的充值缓存
	 * 
	 * @param playerId			角色ID
	 */
	void removeUserRechargeRecordIdCache(long playerId);
}
