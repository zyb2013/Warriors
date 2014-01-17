package com.yayo.warriors.module.chat.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.dungeon.manager.DungeonManager;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.map.types.ScreenType;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 切换场景命令解析器
 * 
 * @author Hyint
 */
@Component
public class ChangeScreenParser extends AbstractGMCommandParser {
	
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private DungeonManager dungeonManager;
	@Autowired
	private GameMapManager gameMapManager;

	
	protected String getCommand() {
		return GmType.GOTO;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		long playerId = userDomain.getPlayerId();
		Integer mapId = Integer.valueOf(elements[2].trim());
		Integer positionX = Integer.valueOf(elements[3].trim());
		Integer positionY = Integer.valueOf(elements[4].trim());
		
		GameMap beforeMap = userDomain.getGameMap();//玩家没有飞之前的地图
		if(beforeMap.getScreenType() == ScreenType.CAMP.ordinal()){
			return false;
		}
		
		PlayerBattle playerBattle = userDomain.getBattle();
		PlayerDungeon playerDungeon = dungeonManager.getPlayerDungeon(playerId);
		if(userDomain == null || playerDungeon == null){
			return false;
		}
		if(playerDungeon.isDungeonStatus()){
			return false;
		}
		
		GameMap gameMap = gameMapManager.getGameMapById(mapId, userDomain.getBranching());
		if(gameMap == null){
			return false;
		}
		
		if(playerBattle.getLevel() < gameMap.getLevelLimit()){
			return false;
		}
		
		return mapFacade.go(playerId, mapId, positionX, positionY, 3);
	}

}
