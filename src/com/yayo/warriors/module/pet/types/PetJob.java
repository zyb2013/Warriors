package com.yayo.warriors.module.pet.types;

import com.yayo.warriors.module.fight.type.FightCasting;


public enum PetJob {
	
	BALANCE(FightCasting.THEURGY),
	ONIMUSHA(FightCasting.PHYSICAL),
	AIRBENDER(FightCasting.THEURGY),
	ASSASSIN(FightCasting.PHYSICAL),
	MORTAL(FightCasting.THEURGY),
	HERO(FightCasting.THEURGY);
	private FightCasting casting;
	
	PetJob(FightCasting casting) {
		this.casting = casting;
	}

	public FightCasting getCasting() {
		return casting;
	}
	
}
