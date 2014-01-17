package com.yayo.warriors.module.horse.facade;

import java.util.Map;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.horse.entity.Horse;
import com.yayo.warriors.module.horse.vo.HorseVo;

/**
 * 坐骑接口
 * @author liuyuhua
 */
public interface HorseFacade {

	/**
	 * 是否骑乘
	 * 
	 * @param  playerId  		玩家的ID
	 * @return {@link Boolean} 	true-骑乘, false-下马
	 */
	boolean isRide(long playerId);
	
	/**
	 * 坐骑的外观
	 * 
	 * @param  playerId  		玩家的ID
	 * @return {@link Integer} 	坐骑的外观
	 */
	int getHorseMount(long playerId);
	
	/**
	 * 获取玩家速度
	 * 
	 * @param  playerId   		玩家的ID
	 * @return {@link Integer} 	玩家的速度
	 */
	int getPlayerSpeed(long playerId);
	
	/**
	 * 查询坐骑VO对象
	 * 
	 * @param  playerId			玩家的ID
	 * @return {@link HorseVo}	坐骑VO对象
	 */
	HorseVo getHorseVO(long playerId);
	
	/**
	 * 角色上马
	 * 
	 * @param  playerId  		玩家的ID
	 * @param  mount     		坐骑外观
	 * @return {@link Integer} 	坐骑外观
	 */
	ResultObject<Integer> winupHorse(long playerId, int mount);

	/**
	 * 角色下马
	 * 
	 * @param  playerId  		玩家的ID
	 * @return {@link Integer} 	坐骑公共返回常量
	 */
	ResultObject<Horse> dismountHorse(long playerId);
	

	/**
	 * 查看其它玩家的坐骑
	 * @param targetId               角色的ID
	 * @return {@link Map}           当前坐骑等级及角色名字集合
	 */
	ResultObject<Map<String,Object>> viewHorse(long targetId);
	
	/**
	 * 自定义物品幻化坐骑
	 * @param playerId                     玩家的ID
	 * @param userItems                    道具
	 * @param autoBuyCount                 需要自动购买的数量
	 * @return {@link HorseVo} 坐骑VO
	 */
	ResultObject<HorseVo> definePropsFancy(long playerId,String userItems, int autoBuyCount);
	
	
}
