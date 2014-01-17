package com.yayo.warriors.module.recharge.facade;

import java.util.List;

import com.yayo.warriors.module.recharge.model.GiftContext;
import com.yayo.warriors.module.recharge.model.RechargeGiftVO;
import com.yayo.warriors.module.server.listener.LogoutListener;

/**
 * 充值礼包接口
 * 
 * @author Hyint
 */
public interface ChargeGiftFacade extends LogoutListener {

	/**
	 * 列出充值礼包VO对象
	 * 
	 * @param  playerId					角色ID
	 * @param  recalculate				是否重新计算
	 * @return {@link GiftContext}		充值礼包列表
	 */
	GiftContext getGiftContext(long playerId, boolean recalculate);
	
	/**
	 * 列出充值礼包VO列表
	 * 
	 * @param  playerId					角色ID
	 * @param  recalculate				是否重新计算
	 * @return {@link List}				充值礼包VO列表
	 */
	List<RechargeGiftVO> listRechargeGiftVO(long playerId, boolean recalculate);
	
	/**
	 * 领取充值礼包奖励
	 * 
	 * @param  playerId					角色ID
	 * @param  giftId					礼包ID
	 * @param  rewardId					奖励ID
	 * @return {@link Integer}			充值礼包模块返回值
	 */
	int rewardRechargeGift(long playerId, int giftId, int rewardId);
}
