package com.yayo.warriors.module.horse.dao;

import java.util.List;

import com.yayo.common.db.dao.CommonDao;
import com.yayo.warriors.module.horse.entity.Horse;

/**
 * 坐骑数据访问
 * @author liuyuhua
 */
public interface HorseDao extends CommonDao{
	
	/**
	 * 创建坐骑
	 * @param horse
	 */
	void createHorse(Horse horse);
	
	/**
	 * 全表扫描 坐骑等级
	 * @return {@link List} 坐骑等级
	 */
	List<Integer> getAllPlayerHorseLevel();

}
