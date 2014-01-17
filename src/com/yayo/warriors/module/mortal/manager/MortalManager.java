package com.yayo.warriors.module.mortal.manager;

import java.util.List;

import com.yayo.warriors.basedb.model.MortalAddedConfig;
import com.yayo.warriors.basedb.model.MortalBodyConfig;
import com.yayo.warriors.module.mortal.entity.UserMortalBody;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.Fightable;

/**
 * 肉身管理接口
 * 
 * @author Hyint
 */
public interface MortalManager extends DataRemoveListener{
	
	/**
	 * 获得已加成属性
	 * 
	 * @param  playerBattle			角色战斗对象
	 * @return {@link Fightable}	战斗属性集合
	 */
	Fightable getAttributeValue(PlayerBattle playerBattle);
	
	/**
	 * 取得肉身对象
	 * 
	 * @param playerBattle     
	 * @return {@link UserMortalBody}
	 */
	UserMortalBody getUserMortalBody(PlayerBattle playerBattle);
	
	/**
	 * 获得肉身配置对象
	 * 
	 * @param  job						角色职业
	 * @param  type						肉身类型
	 * @param  level					肉身等级
	 * @return {@link MortalBodyConfig}	肉身配置信息
	 */
	MortalBodyConfig getMorbodyConfig(int job, int type, int level);
	
	/**
	 * 获的肉身加成配置对象
	 * 
	 * @param job                       角色职业
	 * @param level                     等级需求
	 * @return {@link MortalAddedConfig}
	 */
	MortalAddedConfig getMortalAddedConfig(int job, int level);
	
	
	/**
	 * 获得最小等级肉身
	 * 
	 * @param  角色战斗对象
	 * @return {@link Integer}
	 */
	int getMortalMinLevel(PlayerBattle playerBattle);
	
	
	/**
	 * 获得所有肉身玩家
	 *  
	 * @return {@link List}
	 */
	List<Long> getAllMortalPlayers();
}

