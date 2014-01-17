package com.yayo.warriors.module.map.domain;

import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.type.ElementType;

public class DefaultMotion implements ISpire {
	
	private int x ;
	
	private int y ;
	
	private UnitId unitId;
	
	private GameScreen gameScreen ;
	
	public DefaultMotion(UnitId unitId, int x, int y) {
		this.x = x ;
		this.y = y ;
		this.unitId = unitId ;
	}

	
	public int getX() {
		return this.x ;
	}

	
	public int getY() {
		return this.y;
	}

	public UnitId getUnitId() {
		return unitId;
	}

	
	public GameScreen getCurrentScreen() {
		synchronized (this) {
			GameMap gameMap = this.gameScreen.getGameMap();
			if(gameScreen.checkInThisGameScreen(this)){		
				return this.gameScreen;
			}
			this.gameScreen = gameMap.getGameScreen(this) ;
		}
		return gameScreen;
	}

	
	public GameMap getGameMap() {
		GameScreen currentScreen = getCurrentScreen();
		return currentScreen != null ? currentScreen.getGameMap() : null;
	}

	
	public int getMapId() {
		GameScreen currentScreen = getCurrentScreen();
		return currentScreen != null ? currentScreen.getMapId() : 0;
	}

	
	public void recordScreen(GameScreen gameScreen) {
		this.gameScreen = gameScreen ;
	}

	
	public long getId() {
		return unitId == null ? -1 : unitId.getId();
	}

	
	public ElementType getType() {
		return unitId == null ? null : unitId.getType();
	}

	
	public void changeScreen(GameScreen toGameScreen) {
		synchronized (this) {
			this.leaveScreen();
			enterScreen(toGameScreen);
		}
	}

	
	public boolean changeMap(GameMap targetGameMap, int x, int y) {
		synchronized (this) {
			GameScreen screen = targetGameMap.getGameScreen(x, y);
			if(screen == null){
				return false ;
			}
		
			this.leaveScreen();
			this.enterScreen(screen);
			this.x = x ; 
			this.y = y ;
		}
		
		return true ;
	}

	public void enterScreen(GameScreen screen) {
		synchronized (this) {
			screen.enterScreen(this);
			this.gameScreen = screen ;
		}
	}

	public void leaveScreen(){
		synchronized (this) {
			if(gameScreen != null){
				this.gameScreen.leaveScreen(this);
			}
		}
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((unitId == null) ? 0 : unitId.hashCode());
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		
		DefaultMotion other = (DefaultMotion) obj;
		return unitId != null && other.unitId != null && unitId.equals(other.unitId);
	}

}
