package com.yayo.warriors.common.helper;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.facade.MonsterFacade;
import com.yayo.warriors.module.monster.model.MonsterBattle;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.AnimalInfo;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.KickCode;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.user.UserCmd;
import com.yayo.warriors.type.ElementType;


@Component
public class UserPushHelper {
	@Autowired
	private Pusher pusher;
	@Autowired
	private PetManager petManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MonsterFacade monsterFacade;
	@Autowired
	private SessionManager sessionManager;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserPushHelper.class);
			
	private static ObjectReference<UserPushHelper> ref = new ObjectReference<UserPushHelper>();
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	private static UserPushHelper getInstance() {
		return ref.get();
	} 
	
	public static void pushKickOff(KickCode kickCode, Collection<Long> playerIdList) {
		if(playerIdList == null) {
			playerIdList = getInstance().sessionManager.getOnlinePlayerIdList();
		}
		
		if(playerIdList == null || playerIdList.isEmpty()){
			return ;
		}
		Response response = Response.defaultResponse(Module.USER, UserCmd.NOTICE_2_KICK, kickCode);
		getInstance().pusher.pushMessage(playerIdList, response);
	}

	
	public static void pushKickOff(long playerId, KickCode kickCode, IoSession session) {
		if(session != null && playerId > 0L) {
			Response response = Response.defaultResponse(Module.USER, UserCmd.NOTICE_2_KICK, kickCode);
			getInstance().pusher.pushMessage(Arrays.asList(playerId), response);
		}
	}
	
	
	
	public static AnimalInfo getPlayreAnimalInfo(UnitId unitId, Object...attributes) {
		Object[] attrValues = getInstance().userManager.getPlayerAttributes(unitId.getId(), attributes);
		return AnimalInfo.valueOf(unitId, attributes, attrValues);
	}


	public static AnimalInfo getMonsterAnimalInfo(UnitId unitId, Object... attributes) {
		if(attributes == null || attributes.length <= 0) {
			return null;
		}
		
		long monsterId = unitId.getId();
		MonsterDomain monsterDomain = getInstance().monsterFacade.getMonsterDomain(monsterId);
		if(monsterDomain == null) {
			return null;
		}
		
		MonsterBattle monsterBattle = monsterDomain.getMonsterBattle();
		if(monsterBattle == null) {
			return null;
		}
		
		Object[] attrValues = new Object[attributes.length];
		for (int index = 0; index < attributes.length; index++) {
			Integer attributeKey = (Integer) attributes[index];
			attrValues[index] = monsterBattle.getAttribute(attributeKey);
		}
		return AnimalInfo.valueOf(unitId, attributes, attrValues);
	}
	

	public static void pushAttribute2AreaMember(long playerId, Collection<Long> playerIdList, Collection<UnitId> unitIds, Object...attributes) {
		if(playerIdList == null || playerIdList.isEmpty() || unitIds == null || unitIds.isEmpty() || attributes.length <= 0) {
			return;
		}
		
		Set<AnimalInfo> animalInfos = new HashSet<AnimalInfo>();
		for (UnitId unitId : unitIds) {
			AnimalInfo animalInfo = null;
			if(unitId.getType() == ElementType.PLAYER) {
				animalInfo = UserPushHelper.getPlayreAnimalInfo(unitId, attributes);
			} else if(unitId.getType() == ElementType.MONSTER) {
				animalInfo = UserPushHelper.getMonsterAnimalInfo(unitId, attributes);
			} else if(unitId.getType() == ElementType.PET) {
				animalInfo = UserPushHelper.getPetAnimalInfo(unitId, attributes);
			}
			
			if(animalInfo != null) {
				animalInfos.add(animalInfo);
			}
		}

		if(animalInfos != null && !animalInfos.isEmpty()) {
			Map<String, Object> resultMap = new HashMap<String, Object>(1);
			resultMap.put(ResponseKey.INFO, animalInfos.toArray());
			Response response = Response.defaultResponse(Module.USER, UserCmd.PUSH_ATTRIBUTE_2_MEMBERS, resultMap);
			getInstance().pusher.pushMessage(playerIdList, response);
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("推送信息给角色列表: [{}], 推送信息:[{}] ", playerIdList, Arrays.toString(animalInfos.toArray(new AnimalInfo[animalInfos.size()])));
			}
		}
	}
	

	public static void pushPlayerLevelUp(Collection<Long> playerIds, UnitId unitId, int level) {
		if(playerIds == null || playerIds.isEmpty() || unitId == null) {
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.LEVEL, level);
		resultMap.put(ResponseKey.UNITID, unitId);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("角色:[{}] 升级到:[{}] 级..........", unitId, level);
		}
		
		Response response = Response.defaultResponse(Module.USER, UserCmd.PUSH_PLAYER_LEVELUP, resultMap);
		getInstance().pusher.pushMessage(playerIds, response);
		
	}
	

	public static void pushFightMode2Member(long playerId, int fightMode, int mapId) {
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.MAPID, mapId);
		resultMap.put(ResponseKey.MODE, fightMode);
		Response response = Response.defaultResponse(Module.USER, UserCmd.PUSH_MODE_WITH_CHGMAP, resultMap);
		getInstance().pusher.pushMessage(playerId, response);
	}
	

	public static void pushPlayerRecurrent(UnitId unitId, int pushHp, Collection<Long> playerIds) {
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.HP, pushHp);
		resultMap.put(ResponseKey.UNITID, unitId);
		Response response = Response.defaultResponse(Module.USER, UserCmd.PUSH_ROLE_RESURRECT, resultMap);
		getInstance().pusher.pushMessage(playerIds, response);
	}
	

	public static AnimalInfo getPetAnimalInfo(UnitId unitId, Object...attributes) {
		PetDomain petDomain = getInstance().petManager.getPetDomain(unitId.getId());
		if(petDomain == null) {
			return null;
		}
		
		PetBattle petBattle = petDomain.getBattle();
		Object[] attrValues = new Object[attributes.length];
		for (int index = 0; index < attributes.length; index++) {
			Integer attribute = (Integer) attributes[index];
			if(attribute == null) {
				attrValues[index] = 0;
				continue;
			}
			
			switch (attribute) {
				case AttributeKeys.HP:			attrValues[index] = petBattle.getHp();				break;
				case AttributeKeys.JOB:			attrValues[index] = petBattle.getJob().ordinal();	break;
				case AttributeKeys.EXP:			attrValues[index] = petBattle.getExp();				break;
				case AttributeKeys.LEVEL:		attrValues[index] = petBattle.getLevel();			break;
				case AttributeKeys.HP_MAX:		attrValues[index] = petBattle.getHpMax();			break;
				default: 						attrValues[index] = 0;								break;
			}
		}
		return AnimalInfo.valueOf(unitId, attributes, attrValues);
	}
	

	public static void pushPlayerAttributeChange(Fightable beforable, UserDomain userDomain) {
		long playerId = userDomain.getPlayerId();
		PlayerBattle battle = userDomain.getBattle();
		Fightable afterable = battle.getAndCopyAttributes();
		Map<Integer, Integer> pushAttributes = new HashMap<Integer, Integer>(AttributeRule.LEVELUP_DRESS_PUSHINFO.length);
		for (Integer attribute : AttributeRule.LEVELUP_DRESS_PUSHINFO) {
			int afterAttrValue = afterable.get(attribute);
			int beforeAttrValue = beforable.get(attribute);
			int changeAttrValue = afterAttrValue - beforeAttrValue;
			if(changeAttrValue != 0) {
				pushAttributes.put(attribute, changeAttrValue);
			}
		}
		
		if(pushAttributes.isEmpty()) {
			return;
		}
	
		int index = 0;
		int size = pushAttributes.size();
		AnimalInfo animalInfo = AnimalInfo.valueOf(userDomain.getUnitId(), new Object[size], new Object[size]);
		for (Entry<Integer, Integer> entry : pushAttributes.entrySet()) {
			animalInfo.getParams()[index] = entry.getKey();
			animalInfo.getValues()[index] = entry.getValue();
			index++;
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("推送给玩家:[{}] 上下装/升级属性变化信息: [{}] ", playerId, animalInfo);
		}
		
		Response response = Response.defaultResponse(Module.USER, UserCmd.PUSH_PLAYER_ATTRCHANGE, animalInfo);
		getInstance().sessionManager.write(playerId, response);
	}
}
