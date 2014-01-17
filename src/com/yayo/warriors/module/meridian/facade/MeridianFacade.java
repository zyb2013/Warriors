package com.yayo.warriors.module.meridian.facade;

import java.util.Collection;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.meridian.constant.MeridianConstant;
import com.yayo.warriors.module.meridian.vo.AttributeVo;
import com.yayo.warriors.module.props.entity.BackpackEntry;

/**
 * 经脉接口
 * @author huachaoping
 * 
 */
public interface MeridianFacade {

	/**
	 * 查看玩家一点经脉加成属性总和
	 * 
	 * @param  playerId           		角色ID
	 * @return {@link AttributeVo}		属性VO
	 */
	AttributeVo loadMeridianAttr(Long playerId);
	
//	/**
//	 * 玩家当前经脉
//	 * 
//	 * @param  playerId            	 	玩家Id
//	 * @param  meridianType        	 	经脉类型
//	 * @return {@link Collection}		经脉点ID列表  
//	 */
//	Collection<Integer> loadCurrentMeridian(Long playerId, int meridianType);

	/**
	 * 运气冲穴
	 * 
	 * @param playerId             		 玩家Id
	 * @param meridianId           		 脉点Id
	 * @param userItems   	 			 用户道具信息, 格式 :用户道具ID_数量|...|..
	 * @return {@link MeridianConstant}	 经脉模块返回值
	 */
	ResultObject<Collection<BackpackEntry>> rushMeridian(Long playerId, int meridianId, String userItems, int autoCount);
	
	/**
	 * 突破瓶颈
	 * @param playerId                   玩家ID
	 * @param userPropsIdAndCount        用户道具信息, 格式 :用户道具ID_数量|...|..
	 * @return {@link MeridianConstant}  经脉模块返回值
	 */
	int breakthrough(Long playerId, String userPropsIdAndCount);
	
	/**
	 * 经脉阶段验证
	 * @param playerId                   玩家ID
	 * @return {@link MeridianConstant}  经脉模块返回值
	 */
	int validateMeridianStage(Long playerId);
	
	/**
	 * 范围内玩家加经验
	 * 
	 * @param playerId                   玩家ID
	 * @return {@link MeridianConstant}  经脉模块返回值
	 */
	int addPlayersExp(long playerId);
}
 