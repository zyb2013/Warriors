package com.yayo.warriors.module.title.facade;

import java.util.Collection;

import com.yayo.warriors.basedb.model.TitleDictionary;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.title.model.TitleType;
import com.yayo.warriors.module.title.vo.TitleVo;

/**
 * 称号逻辑
 * 
 * @author huachaoping
 */
public interface TitleFacade {

	/**
	 * 获得新称号
	 * 
	 * @param playerId
	 * @param titleType
	 * @param param
	 * @return {@link TitleDictionary} 如果是null没有获得新称号
	 */
	TitleDictionary obtainNewTitle(long playerId, TitleType titleType, Object param);

	/**
	 * 获取玩家称号列表
	 * 
	 * @param playerId
	 * @return {@link TitleVo}
	 */
	Collection<TitleVo> hasTitle(long playerId);

	/**
	 * 使用称号
	 * 
	 * @param playerId
	 * @param titleId
	 * @return {@link CommonConstant}
	 */
	int useTitle(long playerId, int titleId);

	/**
	 * 解除称号
	 * 
	 * @param playerId
	 * @return {@link CommonConstant}
	 */
	Integer removeTitle(long playerId);
	
	
	/**
	 * 获得等级称号
	 * 
	 * @param playerId                 
	 * @param currentLevel              到达的等级
	 */
	void obtainNewTitleRelationLevel(long playerId ,int currentLevel);
	
	/**
	 * 获得经脉称号
	 * 
	 * @param playerId
	 * @param passMeridians             贯通的经脉数(一条脉)
	 */
	void obtainNewTitleRelationMeridian(long playerId, int passMeridians);

}
