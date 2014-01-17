package com.yayo.warriors.module.gift.facade;

import java.util.Collection;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.constant.CommonConstant;
import com.yayo.warriors.module.gift.vo.GiftVo;
import com.yayo.warriors.module.gift.vo.OnlineGiftVo;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.user.entity.Player;

/**
 * 礼包接口
 * 
 * @author huachaoping
 */
public interface GiftFacade {
	
	
	/**
	 * 领取在线礼包
	 * 
	 * @param playerId                  玩家ID
	 * @param onlineGiftId              在线礼包ID
	 * @return {@link CommonConstant}
	 */
	ResultObject<Collection<BackpackEntry>> rewardOnlineGift(long playerId, int onlineGiftId);
	
	
//	/**
//	 * 开启在线礼包
//	 * 
//	 * @param playerId                  玩家Id
//	 * @return {@link CommonConstant}
//	 */
//	int openOnlineGift(long playerId);
	
	
	/**
	 * 获取在线礼包状态
	 * 
	 * @param playerId
	 * @return
	 */
	OnlineGiftVo loadGiftState(long playerId);
	
	
	/**
	 * 更新在线时间
	 * 
	 * @param player
	 */
	void saveGiftOnlineTime(Player player);
	
	
	/**
	 * 领取CDKEY礼包
	 * 
	 * @param playerId
	 * @param giftId
	 * @param cdKey
	 * @return {@link CommonConstant}
	 */
	ResultObject<Integer> receiveCDKeyGift(long playerId, int giftId, String cdKey);
	
	
	/**
	 * 获得有效的礼包
	 * 
	 * @param  playerId
	 * @return {@link GiftVo}
	 */
	Collection<GiftVo> loadEffectGifts(long playerId);
	
	
	/**
	 * 领取条件礼包(达成条件可领取, 不包括CDKEY礼包)
	 * 
	 * @param playerId 
	 * @param giftId
	 * @return {@link CommonConstant}
	 */
	ResultObject<String> receiveEffectGift(long playerId, int giftId);
}
