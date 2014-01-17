package com.yayo.warriors.module.vip.model;

import com.yayo.warriors.basedb.model.VipConfig;
import com.yayo.warriors.module.vip.entity.PlayerVip;

/**
 * VIP域
 * 
 * @author huachaoping
 */
public class VipDomain {

	/** 玩家VIP信息 */
	private PlayerVip playerVip ;
	
	/** VIP基础信息 */
	private VipConfig vipConfig ;
	
	
	public Long longValue(VipFunction vipFunction) {
		return vipFunction.getValue(this, Long.class) ;
	}
	
	public int intValue(VipFunction vipFunction) {
		return vipFunction.getValue(this, Integer.class) ;
	}
	
	public boolean booleanValue(VipFunction vipFunction){
		return vipFunction.getValue(this, Boolean.class) ;
	}
	
	public float floatValue(VipFunction vipFunction) {
		return vipFunction.getValue(this, Float.class);
	}
	
	/**
	 * 修改参数
	 * @param num          需要修改的值(例如使用次数, 奖励是否已领取)
	 * @param vipFunction  VIP功能信息
	 * @return
	 */
	public int alterNum(int num , VipFunction vipFunction){
		playerVip.alterNum(vipFunction.name(), num);
		return num ;
	}
	
	private VipDomain(PlayerVip playerVip, VipConfig vipConfig){
		this.playerVip = playerVip ;
		this.vipConfig = vipConfig ;
	}
	
	public static VipDomain valueOf(PlayerVip playerVip, VipConfig vipConfig) {
		return new VipDomain(playerVip, vipConfig);
	}

	public int vipLevel() {
		return this.playerVip.getVipLevel();
	}
	
	public PlayerVip getPlayerVip() {
		return playerVip;
	}

	public VipConfig getVipConfig() {
		return vipConfig;
	}
	
	/**
	 * 查询VIP福利
	 * @param vipFunction        VIP功能类
	 * @return {@link Integer}   
	 */
	public int getParameters(VipFunction vipFunction) {
		return playerVip.intValue(vipFunction.name());
	}
	
	/**
	 * 查询用户是否VIP
	 * 
	 * @return {@link Boolean}
	 */
	public boolean isVip() {
		long endSec = playerVip.getVipEndTime();
		long curSec = System.currentTimeMillis();
		return vipLevel() > 0 && curSec < endSec;
	}
	
	
	/**
	 * 计算VIP加成
	 * 
	 * @param baseValue
	 * @param vipFunction
	 * @return {@link Integer}
	 */
	public int calsVipExperience(int baseValue, VipFunction vipFunction) {
		if (this.vipConfig != null) {
			return (int) (this.floatValue(vipFunction) * baseValue);
		}
		return 0;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((playerVip == null) ? 0 : playerVip.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VipDomain other = (VipDomain) obj;
		return playerVip != null && other.playerVip != null && playerVip.equals(other.playerVip);
	}
}