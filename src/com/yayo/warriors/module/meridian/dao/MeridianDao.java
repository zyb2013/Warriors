package com.yayo.warriors.module.meridian.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.yayo.common.db.dao.CommonDao;

/**
 * 经脉DAO
 * 
 * @author huachaoping
 */
public interface MeridianDao extends CommonDao {
	
	/**
	 * 获得所有经脉玩家ID
	 * 
	 * @return {@link List}
	 */
	List<Long> getMeridianPlayerIds();
}
