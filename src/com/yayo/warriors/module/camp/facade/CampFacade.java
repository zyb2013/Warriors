package com.yayo.warriors.module.camp.facade;
import com.yayo.warriors.module.camp.constant.CampConstant;
import com.yayo.warriors.module.user.type.Camp;

/**
 * 阵营
 * @author liuyuhua
 */
public interface CampFacade {
	
	/**
	 * 加入阵营
	 * @param playerId   玩家的ID
	 * @param campValue  {@link Camp}阵营值
	 * @return {@link CampConstant} 阵营公共返回常量
	 */
	public int joinCamp(long playerId,int campValue);

}
