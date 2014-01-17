package com.yayo.warriors.type;

import com.yayo.common.utility.TimeConstant;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.util.GameConfig;

/**
 * 防沉迷状态
 * 
 * @author huachaoping
 */
public enum AdultState {
	
	/** 未知 */
	NONE(-1, new StateCtrl() {

		
		public double addRatio(Player player) {
			long curOnlineTime = player.getCurrentTotalOnlineTimes();
			long healthTime = TimeConstant.ONE_HOUR_SECOND * 3;    
			long tiredTime  = TimeConstant.ONE_HOUR_SECOND * 5;
			if (curOnlineTime >= tiredTime) {
				return 0;                              
			} else if (curOnlineTime >= healthTime) {
				return 0.5;           
			}
			return 1;
		}

		
		public boolean isGoodsReward(Player player) {
			return addRatio(player) > 0;
		}

	}),
	
	
	
	/** 未成年 */
	TEEN(0, new StateCtrl() {
		
		
		public double addRatio(Player player) {
			long curOnlineTime = player.getCurrentTotalOnlineTimes();
			long healthTime = TimeConstant.ONE_HOUR_SECOND * 3;    
			long tiredTime  = TimeConstant.ONE_HOUR_SECOND * 5;    
			if (curOnlineTime >= tiredTime) {
				return 0;                              
			} else if (curOnlineTime >= healthTime) {
				return 0.5;           
			}
			return 1;
		}
		
		
		public boolean isGoodsReward(Player player) {
			return addRatio(player) > 0;
		}
		
	}),
	
	/** 成年 */
	ADULT(1, new StateCtrl() {
		
		public double addRatio(Player player) {
			return 1;
		}
		
		
		public boolean isGoodsReward(Player player) {
			return addRatio(player) > 0;
		}
		
	});
	
	private int code;
	private StateCtrl stateCtrl;
	
	private AdultState(int code, StateCtrl stateCtrl) {
		this.code = code;
		this.stateCtrl = stateCtrl;
	}

	
	public int getCode() {
		return code;
	}

	
	public double addRatio(Player player) {
		if (!GameConfig.isIndulgeEnble()) {
			return 1;
		}
		return this.stateCtrl.addRatio(player);
	}
	
	public boolean isGoodsReward(Player player) {
		if (!GameConfig.isIndulgeEnble()) {
			return true;
		}
		return this.stateCtrl.addRatio(player) > 0;
	}
	
	/**
	 * 根据code获取枚举对像
	 * 
	 * @param code
	 * @return
	 */
	public static AdultState getElementEnumById(int code) {
		for(AdultState elementEnum : AdultState.values()) {
			if(elementEnum.getCode() == code){
				return elementEnum;
			}
		}
		return null ;
	}
	
	
	public interface StateCtrl {
		
		/**
		 * 获取玩家的防沉迷收益比例
		 * 
		 * @param  player      
		 * @param  configHelper   
		 * @return {@link Double}
		 */
		public double addRatio(Player player);
		
		/**
		 * 是否可以获得物品奖励
		 * 
		 * @param  player			角色对象
		 * @param  configHelper     游戏配置对象
		 * @return {@link Boolean}	物品奖励
		 */
		public boolean isGoodsReward(Player player);
	}

}
