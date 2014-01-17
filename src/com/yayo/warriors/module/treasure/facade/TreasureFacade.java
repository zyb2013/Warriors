package com.yayo.warriors.module.treasure.facade;

import java.util.Map;

import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.treasure.constant.TreasureConstant;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 藏宝图服务接口定义
 * @author jonsai
 *
 */
public interface TreasureFacade {

	/**
	 * 打开藏宝图(如果没有生成坐标就生成坐标)
	 * 
	 * @param	playerId				角色id
	 * @param	userPropsId				藏宝图用户道具id
	 * @param	resultMap				返回map
	 * @return	{@link TreasureConstant}返回值
	 */
	int openTreansureProps(long playerId, long userPropsId, Map<String, Object> resultMap);
	
	/**
	 * 刷新藏宝图品质
	 * 
	 * @param	playerId				角色id
	 * @param	userPropsId				藏宝图用户道具id
	 * @param	quality					>=0:刷到目标品质为止	<0:不限制目标品质
	 * @param	resultMap				返回map
	 * @return	{@link TreasureConstant}返回值
	 */
	int refreshQuality(long playerId, long userPropsId, int quality, Map<String, Object> resultMap);
	
	/**
	 * 挖宝藏
	 * @param	playerId				角色id
	 * @param	userPropsId				藏宝图用户道具id
	 * @param	digUserPropsId			挖宝用户道具id
	 * @param	resultMap				返回map
	 * @return	{@link TreasureConstant}返回值
	 */
	int digTreansure(long playerId, long userPropsId, long digUserPropsId, Map<String, Object> resultMap);
	
	/**
	 * 拿取宝藏
	 * @param playerId					角色id
	 * @param npcId						npcId
	 * @param resultMap					返回map
	 * @return {@link TreasureConstant} 返回值
	 */
	int rewardTreasure(long playerId, int npcId, Map<String, Object> resultMap);
	
	/**
	 * 退出藏宝图地图
	 * @param playerId
	 * @return
	 */
	int existTreansureMap(long playerId);
	
	/**
	 * 是否是寻宝怪物
	 * @param monsterId					怪物id
	 * @return	true:是, false:否
	 */
	boolean isTreasureMonster(long monsterId);
	
	/**
	 * 奖励藏宝图怪物经验
	 * @param attackUser				杀怪角色对象
	 * @param userPetId					杀怪角色家将
	 * @param totalFightExp				杀怪经验
	 * @param monsterId					怪物id
	 * @return
	 */
	int rewardMonsterExp(UserDomain attackUser, long userPetId, int totalFightExp, long monsterId);
	
	/**
	 * 丢弃藏宝图
	 * @param userDomain
	 * @param userPropsId
	 * @return
	 */
	int dropUserTreasure(UserDomain userDomain, long userPropsId);
}
