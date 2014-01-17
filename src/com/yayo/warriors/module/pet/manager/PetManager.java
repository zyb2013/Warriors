package com.yayo.warriors.module.pet.manager;

import java.util.List;
import java.util.Set;

import com.yayo.warriors.basedb.model.PetConfig;
import com.yayo.warriors.basedb.model.PetMergedConfig;
import com.yayo.warriors.basedb.model.PetTrainConfig;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.pet.model.PetZoom;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.UserDomain;


public interface PetManager {
	PetDomain createPetDomain(Pet pet,PetBattle battle);
	
	PetDomain createUnDrawPetDomain(Pet pet,PetBattle battle);
	
	 PetDomain getPetDomain(long petId);

	 List<PetDomain> getPetDomains(long playerId);
	
	 PetDomain getFightingPet(long playerId);
	 
	 int goFighting(UserDomain userDomain, PetDomain petDomain);
	
	 PetDomain goBack(long playerId);
	
	 boolean disbandPet(long playerId,long petId);

	 Object[] getPetAttributes(long playerId,long petId,Object...params);
	
	 boolean isPlayerPet(long playerId,long petId);
	 
	 boolean checkFighting(long playerId,long petId);
	 
	 PetDomain caclPetEnergy(long playerId);
	 
	 Set<Long> getPlayerPetIds(long playerId);
	
	 List<Integer> getFamousPetIds(long playerId);
	
	 boolean remove(long playerId,long petId);
  
     PetConfig getPetConfig(int petBaseId);
 	
 	PetTrainConfig getPetTrainConfig(int level);
 	
 	PetMergedConfig getPetMergedConfig(int level);
 	
    PetDomain getPetMerged(PlayerBattle playerBattle);
    
    PetDomain mergedPet(long playerId);

    int mergedPet(UserDomain userDomain,PetDomain petDomain);

    Fightable getMergedAttribute(PlayerBattle playerBattle);

    int getUserPetMerged(PlayerBattle battle);
 
    PetZoom getPetZooms(long playerId);
    
    void addPetInfo(PetDomain petDomain);
    

}
