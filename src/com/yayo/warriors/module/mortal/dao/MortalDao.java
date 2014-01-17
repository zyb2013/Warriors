package com.yayo.warriors.module.mortal.dao;

import java.util.List;

import com.yayo.common.db.dao.CommonDao;

/**
 * 肉身DAO
 * 
 * @author huachaoping
 */
public interface MortalDao extends CommonDao{
	
	
	/**
	 * 查询所有肉身玩家
	 * 
	 * @return {@link List}
	 */
	List<Long> getMortalPlayers();
}
