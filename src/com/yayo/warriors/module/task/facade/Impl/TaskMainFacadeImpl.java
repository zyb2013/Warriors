package com.yayo.warriors.module.task.facade.Impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.basedb.model.MonsterFightConfig;
import com.yayo.warriors.basedb.model.NpcConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.battlefield.facade.BattleFieldFacade;
import com.yayo.warriors.module.battlefield.rule.BattleFieldRule;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.npc.facade.NpcFacade;
import com.yayo.warriors.module.npc.model.Npc;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.task.facade.AllianceTaskFacade;
import com.yayo.warriors.module.task.facade.CampTaskFacade;
import com.yayo.warriors.module.task.facade.LoopTaskFacade;
import com.yayo.warriors.module.task.facade.MapTaskFacade;
import com.yayo.warriors.module.task.facade.PracticeTaskFacade;
import com.yayo.warriors.module.task.facade.TaskFacade;
import com.yayo.warriors.module.task.facade.TaskMainFacade;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.type.ElementType;

@Component
public class TaskMainFacadeImpl implements TaskMainFacade {
	@Autowired
	private NpcFacade npcFacade;
	@Autowired
	private TaskFacade taskFacade;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private MapTaskFacade mapTaskFacade;
	@Autowired
	private LoopTaskFacade loopTaskFacade;
	@Autowired
	private PracticeTaskFacade practiceTaskFacade;
	@Autowired
	private CampTaskFacade campTaskFacade;
	@Autowired
	private AllianceTaskFacade allianceTaskFacade;
	@Autowired
	private BattleFieldFacade battleFieldFacade;
	
	
	public void updateEquipPolishTask(long playerId) {
		try {
			taskFacade.updateEquipPolishTask(playerId);
		} catch (Exception e) {
		}
		try {
			mapTaskFacade.updateEquipPolishTask(playerId);
		} catch (Exception e) {
		}
	}

	
	public void updateFightMonsterTask(long playerId, MonsterFightConfig monsterFight) {
		if(monsterFight == null) {
			return;
		}
		try {
			taskFacade.updateFightMonsterTask(playerId, monsterFight.getBaseId());
		} catch (Exception e) {
		}

		try {
			mapTaskFacade.updateFightMonsterTask(playerId, monsterFight.getBaseId());
		} catch (Exception e) {
		}
		
		try {
			taskFacade.updateFightCollectTask(playerId, monsterFight);
		} catch (Exception e) {
		}
		
		try {
			mapTaskFacade.updateFightCollectTask(playerId, monsterFight);
		} catch (Exception e) {
		}

		try {
			loopTaskFacade.updateFightLootTask(playerId, monsterFight.getBaseId());
		} catch (Exception e) {
		}

		try {
			practiceTaskFacade.updateFightPracticeTask(playerId, monsterFight.getBaseId());
		} catch (Exception e) {
		}
	}

	
	public void updateEquipAscentStarTask(long playerId, int starLevel) {
		try {
			taskFacade.updateEquipStarTask(playerId, starLevel);
		} catch (Exception e) {
		}
		try {
			mapTaskFacade.updateEquipStarTask(playerId, starLevel);
		} catch (Exception e) {
		}
	}
	
	public void updateCompleteInstanceTask(long playerId, int instanceId) {
		try {
			taskFacade.updateInstanceTask(playerId, instanceId);
		} catch (Exception e) {
		}
		try {
			mapTaskFacade.updateInstanceTask(playerId, instanceId);
		} catch (Exception e) {
		}
	}
	
	
	public ResultObject<int[]> collect(long playerId, int npcId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		if(player == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle battle = userDomain.getBattle();
		if(battle == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} else if(battle.isDead()) {
			return ResultObject.ERROR(PLAYER_DEADED);
		}
		
		PlayerMotion motion = userDomain.getMotion();
		Npc npc = npcFacade.getNpc(npcId);
		if(npc == null) {
			return ResultObject.ERROR(FAILURE);
		} else if(npc.getElementType() != ElementType.NPC_GATHER) {
			return ResultObject.ERROR(TYPE_INVALID);
		} else if(npc.getMapId() != motion.getMapId()) {
			return ResultObject.ERROR(POSITION_INVALID);
		}
		
		NpcConfig npcConfig = npc.getNpcConfig();
		if(npcConfig == null) {
			return ResultObject.ERROR(FAILURE);
		}
		
		int baseId = npcConfig.getRandomCollect();
		PropsConfig propsConfig = propsManager.getPropsConfig(baseId);
		if(propsConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}
		
		GameMap gameMap = userDomain.getGameMap();
		if(!npc.isCanView(gameMap)){
			return ResultObject.ERROR(NPC_GATHER_NOT_EXIST);
		} else {
			npcFacade.handleCollect(gameMap, npc);
		}
		
		try {
			if(gameMap.getMapId() == BattleFieldRule.BATTLE_FIELD_MAPID){
				int result = battleFieldFacade.processCollect(userDomain, npcId, baseId);
				return ResultObject.SUCCESS(new int[] { baseId, result >= SUCCESS ? 1 : 0 });
			} 
				
		} catch (Exception e) {
		}
		
		try {
			int result = taskFacade.collectProps(playerId, baseId);
			if(result >= SUCCESS){
				return ResultObject.SUCCESS(new int[] { baseId, 1 });
			}
		} catch (Exception e) {
		}
		
		try {
			int result = mapTaskFacade.collectProps(playerId, baseId);
			if(result >= SUCCESS){
				return ResultObject.SUCCESS(new int[] { baseId, 1 });
			}
		} catch (Exception e) {
		}
		
		try {
			int result = campTaskFacade.collectProps(playerId, baseId);
			if(result >= SUCCESS){
				return ResultObject.SUCCESS(new int[] { baseId, 1 });
			}
		} catch (Exception e) {
		}
		
		try {
			int result = allianceTaskFacade.collectProps(playerId, baseId);
			if(result >= SUCCESS){
				return ResultObject.SUCCESS(new int[] { baseId, 1 });
			}
		} catch (Exception e) {
		}
		
		return ResultObject.SUCCESS(new int[] { baseId, 0 });
	}

	
	public void updateSelectCampTask(long playerId) {
		try {
			taskFacade.updateSelectCampTask(playerId);
		} catch (Exception e) {
		}
		try {
			mapTaskFacade.updateSelectCampTask(playerId);
		} catch (Exception e) {
		}
	}
	
	
}
