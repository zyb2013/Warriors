package com.yayo.warriors.module.recharge.manager;

import com.yayo.warriors.module.recharge.entity.RechargeGift;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 充值礼包Manager类
 * 
 * @author Hyint
 */
public interface RechargeGiftManager {

	/**
	 * 获得充值礼包对象
	 * 
	 * @param  userDomain			用户域模型
	 * @return {@link RechargeGift}	充值礼包对象
	 */
	RechargeGift getRechargeGift(UserDomain userDomain);
}
