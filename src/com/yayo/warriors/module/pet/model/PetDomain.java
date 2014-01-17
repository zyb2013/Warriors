package com.yayo.warriors.module.pet.model;

import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.GameScreen;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.helper.PetHelper;
import com.yayo.warriors.module.pet.rule.PetExpCalc;
import com.yayo.warriors.type.ElementType;


public class PetDomain  implements ISpire {
	private Pet pet;
	private UnitId unitId;
	private PetBattle battle;
	private PetMotion motion;
	private GameScreen gameScreen;

	
	public UnitId getUnitId() {
		return unitId;
	}


	public static PetDomain valueOf(Pet pet,PetBattle battle){
		PetDomain petDomain = new PetDomain();
		petDomain.pet = pet;
		petDomain.battle = battle;
		petDomain.motion = PetMotion.valueOf(pet.getId());
		petDomain.unitId = UnitId.valueOf(pet.getId(), ElementType.PET);
		return petDomain;
	}

	public Pet getPet() {
		return pet;
	}


	public PetBattle getBattle() {
		PetExpCalc.caclePetExp(getPlayerId(), battle);
		PetHelper.calcAttribute(battle);
		return battle;
	}

	public long getPetId() {
		return this.pet.getId();
	}


	public PetMotion getMotion() {
		return motion;
	}

	public int getX() {
		return this.motion.getX();
	}

	public int getY() {
		return this.motion.getY();
	}
	
	public void setMotion(PetMotion motion) {
		this.motion = motion;
	}

	
	public String toString() {
		return "PetDomain [pet=" + pet + ", battle=" + battle + ", motion="
				+ motion + "]";
	}

	public long getPlayerId() {
		return this.pet.getPlayerId();
	}

	
	public long getId() {
		return this.getPetId();
	}


	public GameScreen getCurrentScreen() {
		if(this.gameScreen == null){
			return null;
		}
		GameMap gameMap = this.gameScreen.getGameMap();
		if(gameScreen.checkInThisGameScreen(this)){
			return this.gameScreen;
		}
		synchronized (this) {
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

	
	public ElementType getType() {
		return ElementType.PET;
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
			this.motion.setX(x) ; 
			this.motion.setY(y) ;
		}
		return true ;
	}

	public void enterScreen(GameScreen screen) {
		synchronized (this) {
			if(screen != null){
				screen.enterScreen(this);
				this.gameScreen = screen ;
			}
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
		
		PetDomain other = (PetDomain) obj;
		return unitId != null && other.unitId != null && unitId.equals(other.unitId);
	}
}
