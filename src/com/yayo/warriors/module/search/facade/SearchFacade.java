package com.yayo.warriors.module.search.facade;

import java.util.Collection;

import com.yayo.warriors.module.search.vo.CommonSearchVo;

/**
 * 搜索接口
 * 
 * @author huachaoping
 */
public interface SearchFacade {
	
	/**
	 * 查找玩家 名字
	 * 
	 * @param  keywords 
	 * @return Collection<CommonSearchVo> - {玩家名字,玩家ID} 
	 */
	Collection<CommonSearchVo> searchPlayerName(String keywords); 
	
	
	/**
	 * 查找同屏玩家名字
	 * 
	 * @param  keywords
	 * @return Collection<CommonSearchVo> - {玩家名字,玩家ID} 
	 */
	Collection<CommonSearchVo> searchScreenPlayer(long playerId, String keywords);
	
}
