package com.yayo.warriors.module.chat.parser;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.common.helper.PetPushHelper;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.pet.rule.PetAttributeRule;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 家将历练值命令解析器
 * 
 * @author Hyint
 */
@Component
public class PetExpParser extends AbstractGMCommandParser {
	
	@Autowired
	private PetManager petManager;
	@Autowired
	private PetPushHelper petPushHelper;  

	
	protected String getCommand() {
		return GmType.PET_EXP;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		long playerId = userDomain.getPlayerId();
		Long addExp = Math.abs(Long.valueOf(elements[2].trim()));
		List<PetDomain> petDomains = petManager.getPetDomains(playerId);
		if(petDomains == null || petDomains.isEmpty()) { //没有家将, 不需要处理
			return true;
		}
		
		for (PetDomain petDomain : petDomains) {
			try {
				long petId = petDomain.getId();
				petDomain.getBattle().increaseExp(addExp);
				petPushHelper.pushPetAttribute(Arrays.asList(playerId), playerId, petId, PetAttributeRule.PET_ATTRIBUTES);
			} catch (Exception e) {
				LOGGER.error("{}", e);
			}
		}
		return true;
	}

}
