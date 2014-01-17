package com.yayo.warriors.module.active.facade;


import java.util.List;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.active.entity.OperatorActive;
import com.yayo.warriors.module.active.vo.OperatorActiveVo;

/**
 * 运营活动
 * @author liuyuhua
 */
public interface ActiveOperatorFacade {
	
	/**
	 * 创建运营活动
	 * @param vo           活动内容
	 * @return             返回公共常量
	 */
	int createOrUpdateActive(List<OperatorActiveVo> vos);
	
	/**
	 * 删除运营活动
	 * @param ids              运营活动ID集合
	 * @return {@link Integer} 活动模块公共返回常量
	 */
	int deleteActive(List<Long> ids);
	
	/**
	 * 分页查询运营活动列表
	 * @param start             起始页
	 * @param count             总条数
	 * @return {@link List}     活动显示集合
	 */
	List<OperatorActive> sublistActive(int start, int count);
	
	/**
	 * 领取运营活动奖励
	 * @param playerId           玩家的ID     
	 * @param aliveActiveId      活跃中的运营活动的ID
	 * @param rewardId           奖励的ID
	 * @return {@link Integer}   活动模块公共返回常量
	 */
	int rewardRankActive(long playerId, long aliveActiveId, int activeId);
	
	/**
	 * 领取冲级活动奖励
	 * @param playerId           玩家的ID     
	 * @param aliveActiveId      活跃中的运营活动的ID
	 * @param rewardId           奖励的ID
	 * @return {@link Integer}   活动模块公共返回常量
	 */
	int rewardLevelActive(long playerId, long aliveActiveId,int activeId);
	
	/**
	 * 领取兑换活动奖励
	 * @param playerId            玩家的ID
	 * @param aliveActiveId       活跃中的运营活动的ID   
	 * @param activeId            奖励的ID
	 * @param items               兑换的物品
	 * @return {@link Integer}    活动模块公共返回常量
	 */
	ResultObject<Integer> rewardExChange(long playerId,long aliveActiveId,int activeId,String items);
	
	/**
	 * 客户端验证活动是否可以领取
	 * @param playerId                玩家的ID
	 * @param aliveActiveId           活跃中的运营活动的ID
	 * @param type                    类型
	 * @param activeIds 格式{1_2_3_3} 活动ID  
	 * @return
	 */
	String clientRewardActiveVrifi(long playerId,long aliveActiveId, int type,String activeIds);
	

}
