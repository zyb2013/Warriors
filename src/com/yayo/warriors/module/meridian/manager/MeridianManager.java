package com.yayo.warriors.module.meridian.manager;

import java.util.List;
import java.util.Map;

import com.yayo.warriors.basedb.model.MeridianConfig;
import com.yayo.warriors.module.meridian.entity.Meridian;
import com.yayo.warriors.module.server.listener.DataRemoveListener;

/**
 * 经脉管理接口类
 * 
 * @author Hyint
 */
public interface MeridianManager extends DataRemoveListener {

	/**
	 * 获得玩家对象的经脉对象
	 * 
	 * @param  playerId	 				角色ID
	 * @return {@link Meridian}			经脉对象
	 */
	Meridian getMeridian(long playerId);
	
	/**
	 * 获得该职业经脉数据
	 *    
	 * @param  job                   	职业
	 * @param  type                  	类型
	 * @return {@link List}				静脉点ID列表
	 */
	List<Integer> getMerdianConfigByType(int job, int type);
	
	/**
	 * 获得该职业经脉类型所加成的属性类型
	 * 
	 * @param job                       职业
	 * @param type                      类型
	 * @return {@link Object[]}         经脉加成属性类型
	 */
	Object[] getMeridianAttrKeyByType(int job, int type);
	
	/**
	 * 获得经脉点数据
	 * 
	 * @param  meridianId           	经脉点ID
	 * @return {@link MeridianConfig}	基础经脉对象
	 */
	MeridianConfig getMeridianConfig(int meridianId);
	
	/**
	 * 获得每个类型经脉数量
	 * 
	 * @return {@link Map}
	 */
	Map<Integer, Integer> getMeridianCounts();
	
	/**
	 * 获得所有经脉玩家ID
	 * 
	 * @return {@link List}
	 */
	List<Long> getAllMeridians();
}
