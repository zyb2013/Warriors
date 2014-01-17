package com.yayo.warriors.module.map.domain;


import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.type.ElementType;


public interface ISpire {
	int getX();
	int getY();
	long getId();
	UnitId getUnitId();
	GameScreen getCurrentScreen();
	GameMap getGameMap();
	void recordScreen(GameScreen gameScreen);
	ElementType getType();

	void changeScreen(GameScreen toGameScreen);
	boolean changeMap(GameMap targetGameMap, int x, int y);
	
	int getMapId();
}
