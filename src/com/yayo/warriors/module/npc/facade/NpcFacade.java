package com.yayo.warriors.module.npc.facade;

import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.npc.model.Npc;

public interface NpcFacade {

	/**
	 * 获取NPC
	 * @param npcId  NPC基础数据ID
	 * @return
	 */
	public Npc getNpc(int npcId);
	
	/**
	 * 随机查询主城场景中的NPC
	 * 
	 * @param  screenType			场景类型
	 * @return {@link Npc}			NPC对象
	 */
	Npc getRandomNpcByScreenType(int screenType);
	
	/**
	 * 更具玩家的等级,随机野外、主城等地图的NPC
	 * @param level                 玩家的等级
	 * @return {@link Npc}			NPC对象
	 */
	Npc getRandomNpcByLevel(int level);
	
	/**
	 * 处理采集
	 * 
	 * @param	gameMap				游戏地图
	 * @param	npc					npc对象
	 */
	void handleCollect(GameMap gameMap, Npc npc);
	
	/**
	 * 隐藏NPC(主要用于)
	 * @param gameMap
	 * @param npcId
	 * @param hideTime
	 */
	void hideNpc(GameMap gameMap, int npcId, int hideTime);
	
	/**
	 * 根据玩家等级获取随机采集物
	 * @param level                 玩家等级
	 * @return {@link Npc}          Npc对象
	 */
	Npc getRandomCollect(int level);
	
}
