package com.yayo.warriors.module.vip.facade;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.admin.vo.PlayerVipVO;

public interface VipFacade {
	ResultObject<PlayerVipVO> obtainVip(long playerId, long userItemId, int baseId);
	PlayerVipVO loadVipInfo(long playerId);
	int vipReward(long playerId, int rewardType);
}
