package com.yayo.warriors.module.pet.facade;

import java.util.Map;
import java.util.Set;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.pet.constant.PetConstant;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.model.PetZoom;


public interface PetFacade {
	
	

	int updatePetMotion(long playerId,long petId,int x,int y);
	

	ResultObject<Integer> petGoFighting(long playerId,long petId);
	

	ResultObject<Integer> caclPetEnergy(long playerId);
	

    ResultObject<Long> goBack(long playerId);
     
  
     int getPetSoltSize(long playerId);
     
   
     Set<Long> getPlayerPetIds(long playerId);
     
     
   
     ResultObject<Integer> freePet(long playerId,long petId);
     
 
     void removePetFamousCache(long playerId);
     
  
     ResultObject<Long> mixPet(long playerId,long mPetId,long dPetId);
     
 
     PetZoom getPetZoom(long playerId); 
     
   
     ResultObject<PetZoom> openEggDraw(long playerId,String eggItem);

     ResultObject<Long> drawEggPet(long playerId,long petId);
     

     ResultObject<Integer> freeEggPet(long playerId,Object[] keys);
     
     
    
     ResultObject<Integer> openPetSolt(long playerId,String propsIds,int autoBuyCount);
     

     int useProps(long playerId,long petId,long propsId);
     
   
     Set<Integer> loadFamous(long playerId);
     
 
     ResultObject<Map<String,Object>> addPetExp(long playerId, long petId, int exp);
     
  
     ResultObject<Map<String,Object>> trainingMerged(long playerId,long petId,String userItem,int autoBuyCount);
     
     
  
     int mergedPet(long playerId,long petId);
     
 
     ResultObject<Boolean> trainingPetSavvy(long playerId,long petId,String userItem,int autoBuyCount);
  
     int comebackPet(long playerId);
   
     ResultObject<Pet> startTraingPet(long playerId,long petId);
 
     int finishTraingPet(long playerId,long petId);
     
  
     ResultObject<Map<String,Object>> calcTraingPet(long playerId,long petId);
     
}
