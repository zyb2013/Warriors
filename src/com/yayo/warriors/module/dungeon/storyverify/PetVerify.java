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
 * 验证玩家是否拥有指定家将
 * @author liuyuhua
 */
@Component
public class PetVerify implements VerifyHandler{

	@Autowired
	private SotryDungeonVerify dungeonVerify;
	
	@Autowired
	private PetManager petManager;
	
	
	public int getType() {
		return StoryVerifType.PET_TYPE;
	}

	
	public boolean verify(UserDomain userDomain, StoryVerify storyVerify) {
		if(userDomain == null || storyVerify == null){
			return false;
		}
		
		List<PetDomain> petDomains = petManager.getPetDomains(userDomain.getId());
		if(petDomains == null || petDomains.isEmpty()){
			return false;
		}
		
		for(PetDomain petDomain : petDomains){
			if(petDomain.getPet().getBaseId() == storyVerify.getParam1()){
				return true;
			}
		}
		
		return false;
	}

	
	@PostConstruct
	public void registerVerify() {
		this.dungeonVerify.putVerifyHandler(getType(), this);
	}

}
