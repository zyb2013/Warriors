package com.yayo.warriors.module.onhook.facade;

import java.util.Collection;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.onhook.constant.TrainConstant;
import com.yayo.warriors.module.onhook.vo.TrainVo;
import com.yayo.warriors.module.search.vo.CommonSearchVo;

/**
 * 挂机接口
 * @author huachaoping
 */
public interface TrainFacade {
	
	/**
	 * 加载玩家闭关所得经验和真气
	 * 
	 * @param playerId     玩家ID
	 * @return {@link ResultObject}
	 */
	ResultObject<TrainVo> loadClosedInfo(long playerId);
	
	/**
	 * 领取闭关奖励 
	 * 
	 * @param playerId      玩家ID
	 * @param userItems     用户道具信息: 用户道具ID_数量
	 * @param propsId       基础道具ID
	 * @param multiple      领取倍数
	 * @param autoBuyCount  自动购买数量
	 * @return {@link CommonConstant}
	 */
	int receiveReward(long playerId, String userItems, int propsId, int multiple, int autoBuyCount);
	
	/**
	 * 开启闭关接口
	 * 
	 * @param playerId     玩家ID
	 * @return {@link TrainConstant}
	 */
	int startTrain(long playerId);
	
	/** 
	 * 玩家打坐或取消
	 * 
	 * @param playerId     玩家ID
	 * @return {@link TrainConstant}
	 */
	int processSingleTrain(long playerId);
	
	/**
	 * 打坐双修领取奖励                
	 * 
	 * @param playerId     玩家ID
	 * @return {@link TrainConstant}
	 */
	int receiveAward(long playerId);
	
	/**
	 * 邀请对方双修
	 * 
	 * @param playerId     玩家ID
	 * @param targetId     目标ID
	 * @return {@link TrainConstant}
	 */
	int inviteCoupleTrain(long playerId, long targetId);
	
	/**
	 * 同意双修
	 * 
	 * @param playerId     玩家ID
	 * @param targetId     目标ID
	 * @return {@link TrainConstant}
	 */
	int acceptCoupleTrain(long playerId, long targetId);
	
	/**
	 * 拒绝双修
	 * 
	 * @param playerId     玩家ID
	 * @param targetId     目标ID
	 * @return {@link TrainConstant}
	 */
	int rejectCoupleTrain(long playerId, long targetId);
	
//	/**
//	 * 取消双修
//	 * 
//	 * @param playerId     玩家ID
//	 * @param targetId     目标ID
//	 * @return {@link TrainConstant}
//	 */
//	int cancleCoupleTrain(long playerId, long targetId);
	
	/**
	 * 人物状态(0-站立, 1-打坐, 2-双修)
	 * 
	 * @param playerId     玩家ID
	 * @return {@link TrainConstant}
	 */
	int isTrainStatus(long playerId);
	
	/**
	 * 取消打坐
	 * 
	 * @param playerId     玩家ID
	 */
	void cancelSingleTrain(long playerId);
	
	/**
	 * 查找双修玩家
	 * 
	 * @param playerId     玩家ID
	 * @param keywords     关键字
	 * @return
	 */
	Collection<CommonSearchVo> getSearchPlayer(long playerId, String keywords);
	
	/**
	 * 保存双修方向
	 * 
	 * @param playerId
	 * @param direction
	 * @return {@link TrainConstant}
	 */
	void savePlayerDirection(long playerId, int direction);

}
