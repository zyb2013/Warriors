package com.yayo.warriors.module.map.manager;

import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.user.model.UserDomain;


public interface GameMapManager {

	GameMap getGameMapById(int mapId, int branch);

	boolean isExistTemporaryMap(long tempId, int branch);
	
	long getTemporaryGameMapId();
	GameMap createTemporaryMap(int mapId, long tempId, int branch);

	GameMap getTemporaryMap(long tempId, int branch);

	void removeTemporaryMap(long tempId, int branch);
	void removePlayer(UserDomain userDomain);

	boolean isFinish();
	
	Point randomPoint(GameMap gameMap);

}
