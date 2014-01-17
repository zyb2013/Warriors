package com.yayo.warriors.module.chat.parser;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.warriors.basedb.model.PetMergedConfig;
import com.yayo.warriors.common.helper.PetPushHelper;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.pet.rule.PetAttributeRule;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.type.ElementType;

/**
 * 家将契合等级命令解析器
 * 
 * @author Hyint
 */
@Component
public class PetMergedParser extends AbstractGMCommandParser {
	
	@Autowired
	private PetManager petManager;
	@Autowired
	private PetPushHelper petPushHelper;
	@Autowired
	private ResourceService resourceService;

	
	protected String getCommand() {
		return GmType.MERGED_LEVEL;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		long playerId = userDomain.getPlayerId();
		Integer mergedLevel = Math.abs(Integer.valueOf(elements[2].trim()));
		List<PetDomain> petDomains = petManager.getPetDomains(playerId);
		if(petDomains == null || petDomains.isEmpty()) { //没有家将, 不需要处理
			return true;
		}
		
		PetMergedConfig mergedConfig = resourceService.get(mergedLevel, PetMergedConfig.class);
		if(mergedConfig == null) {
			return false;
		}
		
		GameMap gameMap = userDomain.getGameMap();
		Collection<Long> playerIds = gameMap.getAllSpireIdCollection(ElementType.PLAYER);
		for (PetDomain petDomain : petDomains) {
			try {
				long petId = petDomain.getPetId();
				PetBattle battle = petDomain.getBattle();
				battle.setMergedLevel(mergedLevel);
				battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
				petPushHelper.pushPetAttribute(playerIds, playerId, petId, PetAttributeRule.PET_MERGE_LEVEL);
			} catch (Exception e) {
				LOGGER.error("{}", e);
			}
		}
		return true;
	}

}
