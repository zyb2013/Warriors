package com.yayo.warriors.module.pet.helper;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.pet.rule.PetAttributeCalc;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;

@Component
public class PetHelper {
	
	private static final ObjectReference<PetHelper> ref = new ObjectReference<PetHelper>();
	@Autowired
	private PetAttributeCalc attributeCalc;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PetManager petManager;
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	private static PetHelper getInstance() {
		return ref.get();
	}
	public static void calcAttribute(PetBattle petBattle){
		getInstance().attributeCalc.calcAttribute(petBattle);
	}

	public static UserDomain getUserDomain(long userPetId){
		PetDomain petDomain = getInstance().petManager.getPetDomain(userPetId);
		return getInstance().userManager.getUserDomain(petDomain.getPlayerId());
	}
}
