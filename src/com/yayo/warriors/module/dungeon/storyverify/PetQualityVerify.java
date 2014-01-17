package com.yayo.warriors.module.dungeon.storyverify;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.module.dungeon.model.StoryVerify;
import com.yayo.warriors.module.dungeon.types.StoryVerifType;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 家将品质验证 
 * @author liuyuhua
 */
@Component
public class PetQualityVerify implements VerifyHandler{
	
	@Autowired
	private SotryDungeonVerify dungeonVerify;
	
	@Autowired
	private PetManager petManager;

	
	public int getType() {
		return StoryVerifType.PET_QUALITY_TYPE;
	}

	
	public boolean verify(UserDomain userDomain, StoryVerify storyVerify) {
		if(userDomain == null || storyVerify == null){
			return false;
		}
		
		List<PetDomain> petDomains = petManager.getPetDomains(userDomain.getId());
		if(petDomains == null || petDomains.isEmpty()){
			return false;
		}
		
		if(storyVerify.getParam1() > 0){ //需要验证指定家将,指定品质
			for(PetDomain petDomain : petDomains){
				if(petDomain.getPet().getBaseId() == storyVerify.getParam1() && petDomain.getBattle().getQuality() >= storyVerify.getParam2()){
					return true;
				}
			}
		}else{
			for(PetDomain petDomain : petDomains){
				if(petDomain.getBattle().getQuality() >= storyVerify.getParam2()){
					return true;
				}
			}
		}
		
		return false;
	}

	
	@PostConstruct
	public void registerVerify() {
		this.dungeonVerify.putVerifyHandler(getType(), this);
	}

}
