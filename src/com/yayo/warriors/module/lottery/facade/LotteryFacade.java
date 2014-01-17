package com.yayo.warriors.module.lottery.facade;

import java.util.Map;

import com.yayo.warriors.module.lottery.constant.LotteryConstant;

/**
 *	抽奖facade接口定义
 */
public interface LotteryFacade {

	/**
	 * 抽奖
	 * @param playerId 			玩家UID
	 * @param lotteryId			抽奖基础数据id
	 * @param autoBuy			是否自动够买
	 * @param resultMap			返回客户端map
	 * @return Integer			状态值{@link LotteryConstant}
	 */
	int doLottery(Long playerId, int lotteryId, boolean autoBuy, Map<String,Object> resultMap);

	/**
	 * 取得抽奖历史
	 * @param playerId			角色id
	 * @param force				是否强制取历史
	 * @return
	 */
	Map<String, Object> lotteryCacheMsg(long playerId, boolean force);

	/**
	 * 从抽奖仓库中取出全部物品
	 * @param playerId			角色id
	 * @return
	 */
	int checkoutAllFromLotteryStorage(long playerId);

}
