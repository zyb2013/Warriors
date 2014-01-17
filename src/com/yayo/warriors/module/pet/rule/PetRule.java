package com.yayo.warriors.module.pet.rule;

import com.yayo.warriors.basedb.model.PetConfig;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.types.PetJob;
import com.yayo.warriors.module.user.type.Flushable;

public class PetRule {
	public static final int INIT_PET_SKILL_SLOT = 2;
	
	public static final int MAX_PET_SKILL_SLOT = 7;
	
	public static final int INIT_DEFAULT_LEVEL = 1;
	
	public static final int MAX_PET_LEVEL = 100;
	
	public static final int MAX_PET_SAVVY_LEVEL = 50;
	
	public static final int INIT_DEFAULT_EXP  = 0;
	
	public static final int INIT_PET_ENERGY = 100;

	public static final int MIN_PET_QUALITY_NOTICE = 300;
	
	public static final int INIT_PET_HP = 100;
	
	public static final int CACL_PET_ENERGY_TIME = 300;
	
	public static final int DECREASE_PET_ENERGY = 2;
	
	public static final int PET_DIE_DECREASE_ENERGY = 1;
	
	public static final int PET_GO_FIGHTING_DESCREASE_ENERGY = 5;
	
	public static final int USE_PET_EGG_PROPS_COUNT = 1;
	
	public static final int GROW_TRAIN_PROPS_COUNT = 1;
	
	public static final int SAVVY_TRAIN_PROPS_COUNT = 1;
	
	public static final int USE_PET_SLOT_PROPS_COUNT = 1;
	
	public static final int USE_ENGRY_AND_HP_ITEM_COUNT = 1;
	
	public static final int MAX_PET_OPEN_EGG_CACHE_COUNT = 12;
	
	public static final int MAX_PET_EGG_CACHE_TIME = 86400;
	
	public static final int OPEN_PET_SOLT_PROPS_BASE_ID = 120003;
	
	public static final int USE_PET_MIX_LEVEL = 19;
	
	public static final int PET_TRAING_MERGED_LEVEL = 20;
	public static Pet createPet(long playerId,PetConfig petConfig){
		int baseId  = petConfig.getId();
		int modelId = petConfig.getModel();
		int icon    = petConfig.getIcon();
		String name = petConfig.getName();
		String skill = petConfig.getSkill();
		Pet pet = Pet.valueOf(playerId, name, baseId, modelId, icon,INIT_PET_ENERGY,INIT_PET_SKILL_SLOT, skill);
		return pet;
	}
	

	public static PetBattle createPetBattle(Long petId,PetJob job,int quality){
		PetBattle petBattle = PetBattle.valueOf(petId, job, quality, INIT_PET_HP);
		petBattle.setFlushable(Flushable.FLUSHABLE_LEVEL_UP); 
		return petBattle;
	}

	
	public static PetBattle createPetBattle(PetJob job,int quality){
		PetBattle petBattle = PetBattle.valueOf(null, job, quality, INIT_PET_HP);
		petBattle.setFlushable(Flushable.FLUSHABLE_LEVEL_UP); 
		return petBattle;
	}

}
