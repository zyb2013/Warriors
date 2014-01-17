package com.yayo.warriors.module.gift.vo;

import java.io.Serializable;

/**
 * 在线礼包VO
 * 
 * @author huachaoping
 */
public class OnlineGiftVo implements Serializable{
	
	private static final long serialVersionUID = -6320404226798795338L;

	/** 即将领取的礼包ID */
	private int giftId;
	
	/** 是否过期 */
	private boolean outTime;
	
	/** 剩余倒计时 */
	private long remainTime;
	
	
	/**
	 * 
	 * @param giftId
	 * @param outTime
	 * @param receive
	 * @return
	 */
	public static OnlineGiftVo valueOf(int giftId, boolean outTime, long remainTime) {
		OnlineGiftVo onlineGiftVo = new OnlineGiftVo();
		onlineGiftVo.giftId = giftId;
		onlineGiftVo.outTime = outTime;
		onlineGiftVo.remainTime = remainTime;
		return onlineGiftVo;
	}

	public int getGiftId() {
		return giftId;
	}

	public void setGiftId(int giftId) {
		this.giftId = giftId;
	}

	public boolean isOutTime() {
		return outTime;
	}

	public void setOutTime(boolean outTime) {
		this.outTime = outTime;
	}

	public long getRemainTime() {
		return remainTime;
	}

	public void setRemainTime(long remainTime) {
		this.remainTime = remainTime;
	}

}
