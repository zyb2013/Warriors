package com.yayo.warriors.module.chat.parser;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.yayo.warriors.common.helper.MonsterHelper;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.model.IMonsterConfig;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.type.ElementType;

/**
 * 刷新怪物命令解析器
 * 
 * @author Hyint
 */
@Component
public class MonsterParser extends AbstractGMCommandParser {
	
	
	protected String getCommand() {
		return GmType.MONSTER;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		//
		GameMap gameMap = userDomain.getGameMap();
		Set<ISpire> monsters = gameMap.getCanViewsSpireCollection(userDomain, ElementType.MONSTER);
		int monsterConfigId = Integer.valueOf( elements[2] );
		for(ISpire spire : monsters){
			MonsterDomain monsterAiDoamin = (MonsterDomain)spire;
			IMonsterConfig monsterConfig = monsterAiDoamin.getMonsterConfig();
			if(monsterConfig.getId() == monsterConfigId){
				monsterAiDoamin.resurrection(true);
				monsterAiDoamin.setStopRun(false);
				Set<ISpire> players = gameMap.getCanViewsSpireCollection(monsterAiDoamin, ElementType.PLAYER);
				MonsterHelper.refreshMonster(monsterAiDoamin, players);
				return true;
			}
		}
		return false;
	}

}
