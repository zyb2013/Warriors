package com.yayo.warriors.module.chat.helper;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.campbattle.facade.CampBattleFacade;
import com.yayo.warriors.module.campbattle.type.CampTitle;
import com.yayo.warriors.module.user.entity.PlayerBattle;

/**
 * 聊天软引用对象
 * 
 * @author Hyint
 */
@Component
public class ChatReference {

	private static final ObjectReference<ChatReference> REF = new ObjectReference<ChatReference>();
	@Autowired
	private CampBattleFacade battleFacade;
	@Autowired
	private AllianceManager allianceManager;
	
	@PostConstruct
	protected void init() {
		REF.set(this);
	}

	public static ChatReference getInstance() {
		return REF.get();
	}
	
	public static CampTitle getCampTitle(long playerId) {
		return getInstance().battleFacade.getCampBattleTitle(playerId, null);
	}

	public static PlayerAlliance getAllianceTitle(PlayerBattle battle) {
		return getInstance().allianceManager.getPlayerAlliance(battle);
	}
}
