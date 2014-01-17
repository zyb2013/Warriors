package com.yayo.warriors.module.pet.dao;

import java.util.List;

import com.yayo.common.db.dao.CommonDao;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;

public interface PetDao extends CommonDao{
	
	boolean createPetInfo(Pet pet,PetBattle battle);
	
	List<Long> getPlayerPetIds(long playerId);
	
	List<Integer> getPlayerAllIds(long playerId);
	
	List<Long> getPlayerUnDrawPet(long playerId);
	
	

}
