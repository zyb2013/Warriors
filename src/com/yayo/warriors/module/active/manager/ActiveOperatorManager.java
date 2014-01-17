package com.yayo.warriors.module.active.manager;

import java.util.Collection;
import java.util.List;

import com.yayo.warriors.basedb.model.ActiveOperatorConfig;
import com.yayo.warriors.basedb.model.ActiveOperatorExChangeConfig;
import com.yayo.warriors.basedb.model.ActiveOperatorLevelConfig;
import com.yayo.warriors.basedb.model.ActiveOperatorRankConfig;
import com.yayo.warriors.module.active.entity.OperatorActive;
import com.yayo.warriors.module.active.entity.PlayerActive;
import com.yayo.warriors.module.user.entity.PlayerBattle;

/**
 * 运营活动管理类 
 * @author liuyuhua
 */
public interface ActiveOperatorManager {
	
	/**
	 * 玩家活动对象
	 * @param battle
	 * @return
	 */
	PlayerActive getPlayerActive(PlayerBattle battle);
	
	/**
	 * 创建运营活动
	 * @param active           运营活动对象集合
	 * @return {@link Boolean} true 成功 false 失败
	 */
	boolean createOrUpdateActive(Collection<OperatorActive> actives);
	
	/**
	 * 获取所有客户端可见运营活动(返回给客户端)
	 * @return {@link List}    运营活动集合(有序)
	 */
	List<OperatorActive> getClientShowActives();
	
	/**
	 * 获取所有的运营活动
	 * @return {@link List}    运营活动集合
	 */
	List<OperatorActive> getAllActives();
	
	/**
	 * 删除活动
	 * @param ids              活动ID集合
	 * @return {@link List}    已被删除的活动    
	 */
	List<Long> deleteActive(List<Long> ids);
	
	/**
	 * 获取运营活动配置
	 * @param activeId                      活动的ID
	 * @return {@link ActiveOperatorConfig} 运营活动对象
	 */
	ActiveOperatorConfig getActiveOperatorConfig(int activeId);
	
	/**
	 * 获取运营活动排行类型配置
	 * @param activeId                             活动的ID
	 * @return  {@link ActiveOperatorRankConfig}   运营活动对象
	 */
	ActiveOperatorRankConfig getActiveOperatorRankConfig(int activeId);
	
	/**
	 * 获取运营活动等级类型配置
	 * @param activeId                             活动的ID
	 * @return {@link ActiveOperatorLevelConfig}   运营活动等级对象
	 */
	ActiveOperatorLevelConfig getActiveOperatorLevelConfig(int activeId);
	
	/**
	 * 获取运营活动兑换类型配置
	 * @param activeId                              活动的ID
	 * @return {@link ActiveOperatorExChangeConfig} 运营活动等级对象
	 */
	ActiveOperatorExChangeConfig getActiveOperatorExChangeConfig(int activeId);
	
	/**
	 * 是否能领取
	 * @param aliveActiveId            活跃中的活动ID
	 * @return {@link OperatorActive}  活跃运营活动
	 */
	OperatorActive getOperatorActive(long aliveActiveId);
	
	/**
	 * 获取掉落编号集合
	 * @return {@link List} 掉落编号集合
	 */
	List<Integer> getDropList();
	
	
}
