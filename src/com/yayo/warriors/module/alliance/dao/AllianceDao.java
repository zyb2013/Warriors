package com.yayo.warriors.module.alliance.dao;

import java.util.List;
import java.util.Map;

import com.yayo.common.db.dao.CommonDao;

/**
 * 帮派Dao
 * @author liuyuhua
 */
public interface AllianceDao extends CommonDao{
	
	/**
	 * 获取所有帮派的ID列表
	 * @return  ID列表集合
	 */
	public List<Long> getAllianceIds();
	
	/**
	 * 获取帮派所有成员ID
	 * @param allianceId   帮派的ID
	 * @return
	 */
	public List<Long> getAllianceMember(long allianceId);
	
	/**
	 * 获取所有公会的名字
	 * @return 获取所有公会的名字
	 */
	public Map<String,Long> getAllianceNames();

	
}
