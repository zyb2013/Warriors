package com.yayo.warriors.module.pet.rule;


import static com.yayo.warriors.module.user.type.AttributeKeys.*;

public class PetAttributeRule {
	
	public static final Object[] PET_EXP = {EXP,EXP_MAX}; 
	public static final Object[] PET_LEVEL_EXP = {LEVEL,EXP};
	public static final Object[] PET_HP = {HP,HP_MAX};
	public static final Object[] PET_ENGER = {PET_ENERGY_MAX,PET_ENERGY};
	public static  final Object[] PET_MERGE_LEVEL = { PET_MERGED_LEVEL };
	public static final Object[] PET_ATTRIBUTES = {LEVEL,HP,LEVEL,HP_MAX,PIERCE,BLOCK,
		                                           DODGE,HIT,EXP,EXP_MAX,RAPIDLY,STRENGTH,
		                                           DEXERITY,DUCTILITY,INTELLECT,CONSTITUTION,
		                                           SPIRITUALITY,THEURGY_ATTACK,THEURGY_DEFENSE,
		                                           THEURGY_CRITICAL,PHYSICAL_ATTACK,PHYSICAL_DEFENSE,PHYSICAL_CRITICAL};
	
}
