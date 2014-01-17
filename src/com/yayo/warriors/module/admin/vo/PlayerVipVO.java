package com.yayo.warriors.module.admin.vo;

import java.io.Serializable;

import static com.yayo.warriors.module.vip.model.VipFunction.*;

import com.yayo.warriors.module.vip.entity.PlayerVip;
import com.yayo.warriors.module.vip.model.VipDomain;
 
public class PlayerVipVO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long playerId ;
	private int vipLevel ;
	private long vipEndTime ;
	private boolean receiveVipGift;
	private int flyingShoesRemainTimes;
	
	
	public PlayerVipVO(VipDomain vipDomain) {
		PlayerVip playerVip = vipDomain.getPlayerVip();
		this.playerId = playerVip.getId();
		this.vipLevel = playerVip.getVipLevel();
		this.vipEndTime = playerVip.getVipEndTime();
		this.receiveVipGift = vipDomain.booleanValue(ReceiveVipGift);
		if (vipLevel > 0) {
			this.flyingShoesRemainTimes = vipDomain.intValue(FlyingShoesRemainTimes);
		}
	}


	public long getPlayerId() {
		return playerId;
	}


	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}


	public int getVipLevel() {
		return vipLevel;
	}


	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}


	public long getVipEndTime() {
		return vipEndTime;
	}


	public void setVipEndTime(long vipEndTime) {
		this.vipEndTime = vipEndTime;
	}


	public int getFlyingShoesRemainTimes() {
		return flyingShoesRemainTimes;
	}


	public void setFlyingShoesRemainTimes(int flyingShoesRemainTimes) {
		this.flyingShoesRemainTimes = flyingShoesRemainTimes;
	}


	public boolean isReceiveVipGift() {
		return receiveVipGift;
	}


	public void setReceiveVipGift(boolean receiveVipGift) {
		this.receiveVipGift = receiveVipGift;
	}	
	
	
}
