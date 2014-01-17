package com.yayo.warriors.module.active.manager.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.module.active.manager.ActiveManager;
import com.yayo.warriors.module.active.model.ActiveMonster;
import com.yayo.warriors.module.monster.model.Monster;

/**
 * 活动管理类
 * 
 * @author huachaoping
 */
@Component
public class ActiveManagerImpl implements ActiveManager {
	
	/** 野外BOSS { 服务器分线 - {怪物fight基础ID - 怪物自增ID}} */
	private final Map<Integer, Map<Integer, ActiveMonster>> MONSTERS = new HashMap<Integer, Map<Integer, ActiveMonster>>(1);
	
	
	/**
	 * 将初始化的野外BOSS加入缓存
	 * 
	 * @param monster
	 */
	
	public void put2Monsters(int branching, Monster monster) {
		MonsterFightConfig monsterFight = monster.getMonsterFightConfig();
		if (monsterFight.isBoss()) {
			Map<Integer, ActiveMonster> map = MONSTERS.get(branching);
			if (map == null) {
				map = new HashMap<Integer, ActiveMonster>();
			}
			map.put(monsterFight.getId(), ActiveMonster.valueOf(monster.getId()));
			MONSTERS.put(branching, map);
		}
	}
	
	
	/**
	 * 获得怪物的自增ID
	 * 
	 * @param monsterId
	 * @return {@link Long}
	 */
	
	public long getMonsterAutoId(int branching, int monsterId) {
		Map<Integer, ActiveMonster> map = MONSTERS.get(branching);
		ActiveMonster monster = null;
		if (map != null) {
			monster = map.get(monsterId);
		}
		if (monster != null) {
			return monster.getAutoId();
		}
		return 0L;
	}
	
	
	/**
	 * 移除怪物
	 * 
	 * @param monsterId
	 */
	
	public void removeMonster(int monsterId) {
		MONSTERS.remove(monsterId);
	}
	
	
	/**
	 * 更改怪物击杀者
	 * 
	 * @param monsterId
	 * @param playerName
	 */
	public void modifyMonsterKiller(int branching, int monsterId, String playerName) {
		Map<Integer, ActiveMonster> map = MONSTERS.get(branching);
		ActiveMonster monster = null;
		if (map != null) {
			monster = map.get(monsterId);
		}
		if (monster != null) {
			monster.setMonsterKiller(playerName);
		}
	}


	/**
	 * 获得怪物击杀者
	 * 
	 * @param monsterId
	 * @return
	 */
	
	public String getMonsterKiller(int branching, int monsterId) {
		Map<Integer, ActiveMonster> map = MONSTERS.get(branching);
		ActiveMonster monster = null;
		if (map != null) {
			monster = map.get(monsterId);
		}
		if (monster != null) {
			return monster.getMonsterKiller();
		}
		return StringUtils.EMPTY;
	}

}
