package com.yayo.warriors.module.flyshoes.facade;


/**
 * 小飞鞋
 * @author liuyuhua
 */
public interface FlyShoesFacade {
	
	/**
	 * 使用小飞鞋
	 * 
	 * @param  playerId   		玩家的ID
	 * @param  propsId    		道具的ID
	 * @param  mapId      		地图的ID
	 * @param  x         	 	X轴坐标
	 * @param  y          		Y轴坐标
	 * @return {@link Integer}	公共返回常量
	 */
	int useFlyShoes(long playerId, long propsId, int mapId, int x, int y);

}
