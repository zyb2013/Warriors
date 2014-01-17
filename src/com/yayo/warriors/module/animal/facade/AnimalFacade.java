package com.yayo.warriors.module.animal.facade;

import java.util.Map;

import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.type.ElementType;

/**
 * Animal的定义:
 * 目前场景中,出了英雄自己以外的可以动的所有东西
 * 包括,其他玩家,怪物,NPC,采集点,等所有数据的抽象
 * 这些信息需要 
 * @author liuyuhua
 */
public interface AnimalFacade {

	/**
	 * 获取信息(其他玩家,怪物,NPC)
	 * @param id    唯一标识ID
	 * @param type  类型(玩家,怪物,NPC)
	 * @return
	 */
	public Map<String, Object> getAnimal(ISpire spire , ElementType type);
	
}
