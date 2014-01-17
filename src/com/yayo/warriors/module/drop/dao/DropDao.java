package com.yayo.warriors.module.drop.dao;

import java.util.Collection;

import com.yayo.common.db.dao.CommonDao;
import com.yayo.warriors.module.drop.entity.DropRecord;

/**
 * 掉落记录DAO接口
 * 
 * @author Hyint
 *
 */
public interface DropDao extends CommonDao {

	/**
	 * 更新掉落日志信息
	 * 
	 * @param dropRecords
	 */
	void updateDropRecords(Collection<DropRecord> dropRecords);
}
