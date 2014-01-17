package com.yayo.warriors.module.animal.facade.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.module.animal.facade.AnimalFacade;
import com.yayo.warriors.module.campbattle.facade.CampBattleFacade;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.monster.domain.MonsterDomain;
import com.yayo.warriors.module.monster.facade.MonsterFacade;
import com.yayo.warriors.module.monster.model.Monster;
import com.yayo.warriors.module.monster.model.MonsterBattle;
import com.yayo.warriors.module.npc.facade.NpcFacade;
import com.yayo.warriors.module.npc.model.Npc;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.type.ElementType;

@Component
public class AnimalFacadeImpl implements AnimalFacade{

	@Autowired
	private UserManager userManager;
	
	@Autowired
	private MonsterFacade monsterFacade;
	
	@Autowired
	private NpcFacade npcFacade;
	
	@Autowired
	private CampBattleFacade campBattleFacade;
	
	@Autowired
	private PetManager petManager;
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	
	public Map<String, Object> getAnimal(ISpire spire, ElementType type) {
		if(type == ElementType.PLAYER){
			return this.gatPlayer(spire);
		} else if(type == ElementType.MONSTER){
			return this.getMonster(spire.getId());
		} else if(type == ElementType.NPC){
			return this.getNpc((int)spire.getId());
		}
		return null;
	}
	
	
	/**
	 * 拼装角色属性信息
	 * @param spire         角色的ID  
	 * @return
	 */
	private Map<String, Object> gatPlayer(ISpire spire){
		UserDomain userDomain = (UserDomain)spire;
		Map<String,Object> result = new HashMap<String,Object>(10);
		result.put(ResponseKey.X, userDomain.getX());
		result.put(ResponseKey.Y, userDomain.getY());
		byte face = userDomain.getMotion().getFace();
		if(face > 0){
			result.put(ResponseKey.FACE, face);
		}
		result.put(ResponseKey.UNITID, spire.getUnitId());
		result.put(ResponseKey.PARAMS, AttributeRule.PLAYER_PARAMS);
		result.put(ResponseKey.VALUES, userManager.getPlayerAttributes(spire, AttributeRule.PLAYER_PARAMS));
		
		PetDomain petDomain = petManager.getFightingPet(spire.getId());
		if(petDomain != null){
			Object[] pet_params = AttributeRule.PET_PARAMS;
			Object[] pet_values = petManager.getPetAttributes(spire.getId(), petDomain.getPetId(), pet_params);
			result.put(ResponseKey.PET_PARAMS, pet_params);
			result.put(ResponseKey.PET_VALUES, pet_values);
			result.put(ResponseKey.PET_ID, petDomain.getPetId());
			result.put(ResponseKey.PET_X, petDomain.getMotion().getX());
			result.put(ResponseKey.PET_Y, petDomain.getMotion().getY());
		}
		
		return result;
	}
	
	/**
	 * 拼装怪物信息 
	 * @param id     怪物的ID
	 * @return
	 */
	private Map<String, Object> getMonster(Long id){
		MonsterDomain monsterDomain = monsterFacade.getMonsterDomain(id);
		if(monsterDomain == null) {
			LOGGER.warn("怪物[{}]找不到！", id);
			return null;
		}
		
		Monster monster = monsterDomain.getMonster();
		MonsterBattle battle = monsterDomain.getMonsterBattle();
//		MonsterMotion motion = monsterDomain.getMonsterMotion();
//		MonsterAiDomain monsterAiDomain = (MonsterAiDomain)monsterDomain;
//		if(monsterAiDomain.isPushView()){
			Map<String,Object> result = new HashMap<String,Object>(5);
			result.put(ResponseKey.X, monsterDomain.getX());
			result.put(ResponseKey.Y, monsterDomain.getY());
			result.put(ResponseKey.UNITID, monsterDomain.getUnitId());
			result.put(ResponseKey.PARAMS, AttributeRule.MONSTER_PARAMS);
			result.put(ResponseKey.VALUES, this.getMonsterValue(monsterDomain, monster, battle, AttributeRule.MONSTER_PARAMS));
			return result;
//		} 
//		return null;
		
	}
	

	
	
	/**
	 * 拼装元素
	 * @param id    NPC配置ID
	 * @return
	 */
	private Map<String, Object> getNpc(int id){
		Npc npc = npcFacade.getNpc(id);
		if(npc == null){
			LOGGER.error("npc[{}]找不到！", id);
			return null;
		}
		Map<String,Object> result =  this.getNpc(npc);
		return result;
	}
	
	/**
	 * 获取NPC信息
	 * @param npc
	 * @return
	 */
	private Map<String, Object> getNpc(Npc npc){
		Map<String,Object> result = new HashMap<String,Object>(5);
		//NPC的ID需要发baseId给客户端,便于客户端定位NPC
		Object[] values = getNpcValue();
		result.put(ResponseKey.UNITID, npc.getUnitId() );
		result.put(ResponseKey.PARAMS, AttributeRule.NPC_PARAMS);
		result.put(ResponseKey.VALUES, values);
		result.put(ResponseKey.X, npc.getBornX());
		result.put(ResponseKey.Y, npc.getBornY());
		return result;
	}
	
	/**
	 * 怪物属性
	 * @param monsterDomain
	 * @param monster
	 * @param battle
	 * @return
	 */
	private Object[] getMonsterValue(MonsterDomain monsterDomain, Monster monster, MonsterBattle battle, Object...attributes) {
		Object[] values = new Object[attributes.length];
		Map<Integer, Object> attrMap = campBattleFacade.getAttributesOfCampBattleMonster(monsterDomain);
		for (int i = 0; i < attributes.length; i++) {
			try {
				Integer attribute = (Integer) attributes[i];
				if(attribute == AttributeKeys.NAME) {
					values[i] = monster.getName();
				} else if(attribute == AttributeKeys.ICON) {
					values[i] = monster.getIcon();
				} else if(attribute == AttributeKeys.MONSTER_CONFIG_ID) {
					values[i] = monsterDomain.getMonsterConfig().getId();
				}  else if(attribute == AttributeKeys.BASE_ID) {
					values[i] = monster.getBaseId();
				} else if(attribute == AttributeKeys.CLOTHING) {
					values[i] = attrMap != null && attrMap.containsKey(attribute) ? (Integer)attrMap.get(attribute) : monster.getModel();
				} else if(attribute == AttributeKeys.CAMP) {
					values[i] = monsterDomain.getMonsterCamp();
				} else {
					values[i] = battle.getAttribute(attribute);
				}
			} catch (Exception e) {
				LOGGER.error("取得怪物属性异常: {}", e);
				LOGGER.error("{}", e);
			}
		}
		return values;
	}
	
	/**
	 * 获取NPC属性
	 * @param npc
	 * @return
	 */
	private Object[] getNpcValue() {
		//TODO NPC 不需要有HP MP SP 所以只需要写死
		Object[] result = new Object[]{100, 100 , 100, 100};
//		if(LOGGER.isDebugEnabled()){
//			LOGGER.debug("NPC 属性数据:[{}]",result);
//		}
		return result;
	}

}
