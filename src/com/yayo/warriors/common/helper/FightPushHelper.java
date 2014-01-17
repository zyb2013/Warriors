package com.yayo.warriors.common.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.buffer.vo.DOTInfoVO;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.cooltime.manager.CoolTimeManager;
import com.yayo.warriors.module.cooltime.model.CoolTime;
import com.yayo.warriors.module.cooltime.model.PetCoolTime;
import com.yayo.warriors.module.fight.FightReportVO;
import com.yayo.warriors.module.fight.facade.FightFutureFacade;
import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.fight.model.FightEvent;
import com.yayo.warriors.module.fight.model.FightReport;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.pet.entity.Pet;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.pet.model.PetDomain;
import com.yayo.warriors.module.user.model.AnimalInfo;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.fight.FightCmd;
import com.yayo.warriors.socket.handler.skill.SkillCmd;
import com.yayo.warriors.type.ElementType;

/**
 * 战斗推送帮助类
 * 
 * @author Hyint
 */
@Component
public class FightPushHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(FightPushHelper.class);
	private static final ObjectReference<FightPushHelper> ref = new ObjectReference<FightPushHelper>();
	
	@Autowired
	private Pusher pusher;
	@Autowired
	private PetManager petManager;
	@Autowired
	private CoolTimeManager coolTimeManager;
	@Autowired
	private FightFutureFacade fightFutureFacade;
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	/**
	 * 获得战斗推送类的实例
	 * 
	 * @return {@link FightPushHelper}
	 */
	private static FightPushHelper getInstance() {
		return ref.get();
	}
	
	/**
	 * 推送冷却时间对象给客户端
	 * 
	 * @param  playerId				角色ID
	 * @param  coolTimeIds			冷却时间ID数组
	 */
	public static void pushUserCoolTime2Client(long playerId, int...coolTimeIds) {
		UserCoolTime userCoolTime = getInstance().coolTimeManager.getUserCoolTime(playerId);
		if(userCoolTime == null) {
			return;
		}
		
		Set<CoolTime> coolTimes = new HashSet<CoolTime>();
		for (int coolTimeId : coolTimeIds) {
			CoolTime coolTime = userCoolTime.getCoolTime(coolTimeId);
			if(coolTime != null) {
				coolTimes.add(coolTime);
			}
		}
		
		if(!coolTimes.isEmpty()) {
			getInstance().pusher.pushMessage(playerId, Response.defaultResponse(Module.SKILL, SkillCmd.PUSH_MEMBER_SKILLCD_2_CLIENT, coolTimes.toArray() ) );
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("推送给玩家:[{}] CD信息:[{}] ", playerId, Arrays.toString( coolTimes.toArray(new CoolTime[coolTimes.size()]) ));
			}
		}
	}
	
	/**
	 * Buffer移除信息
	 * 
	 * @param unitId
	 * @param bufferIdArray
	 * @param playerIds
	 */
	public static void pushUnitBufferClear(UnitId unitId, Object[] bufferIdArray, Collection<Long> playerIds) {
		if(playerIds != null && !playerIds.isEmpty()) {
			Map<String, Object> resultMap = new HashMap<String, Object>(2);
			resultMap.put(ResponseKey.UNITID, unitId);
			resultMap.put(ResponseKey.BUFFERS, bufferIdArray);
			Response defaultResponse = Response.defaultResponse(Module.FIGHT, FightCmd.PUSH_BUFFER_CLEAR, resultMap);
			getInstance().pusher.pushMessage(playerIds, defaultResponse);
		}
	}

	/**
	 * 推送冷却时间对象给客户端
	 * 
	 * @param  userPetId			家将ID
	 * @param  coolTimeIds			冷却时间ID数组
	 */
	public void pushPetCoolTime2Client(long userPetId, int...coolTimeIds) {
		PetDomain petDomain = petManager.getPetDomain(userPetId);
		if(petDomain == null) {
			return;
		}
		
		Pet pet = petDomain.getPet();
		if(pet == null) {
			LOGGER.error("推送家将: [{}] CD, 家将不存在", userPetId);
			return;
		}
		
		PetCoolTime petCoolTime = coolTimeManager.getPetCoolTime(userPetId);
		if(petCoolTime == null) {
			LOGGER.error("推送家将: [{}] CD, CD对象不存在", userPetId);
			return;
		}
		
		Set<CoolTime> coolTimes = new HashSet<CoolTime>();
		for (int coolTimeId : coolTimeIds) {
			CoolTime coolTime = petCoolTime.getCoolTime(coolTimeId);
			if(coolTime != null) {
				coolTimes.add(coolTime);
			}
		}
		
		if(coolTimes.isEmpty()) {
			return;
		}
		
		long playerId = pet.getPlayerId();
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.USER_PET_ID, userPetId);
		resultMap.put(ResponseKey.COOL_TIME, coolTimes.toArray());
		pusher.pushMessage(playerId, Response.defaultResponse(Module.SKILL, SkillCmd.PUSH_PET_SKILLCD_2_CLIENT, resultMap));
		if(LOGGER.isDebugEnabled()) {
			CoolTime[] coolTimeArray = coolTimes.toArray(new CoolTime[coolTimes.size()]);
			LOGGER.debug("推送玩家:[{}] -召唤兽:[{}] CD信息:[{}] ", new Object[] { playerId, userPetId, Arrays.toString(coolTimeArray) });
		}
	}

	/**
	 * 推送战报给客户端
	 * 
	 * @param fightEvent		战斗事件对象
	 */
	public static void pushReport2Client(FightEvent fightEvent) {
		if(fightEvent != null) {
			getInstance().pushFightReport2Client(fightEvent);
			getInstance().pushFightCoolTime2Client(fightEvent);
		}
	}
	
	/**
	 * 推送战斗冷却时间信息给客户端
	 * 
	 * @param fightEvent		战斗事件对象
	 */
	private void pushFightCoolTime2Client(FightEvent fightEvent) {
		Context context = fightEvent.getContext();
		int coolTimeId = context.getCoolTimeId();
		ISpire attacker = fightEvent.getAttacker();
		if(coolTimeId > 0) {
			switch (attacker.getType()) {
				case PET:		pushPetCoolTime2Client(attacker.getId(), coolTimeId);	break;
				case PLAYER:	pushUserCoolTime2Client(attacker.getId(), coolTimeId);	break;
			}
		}
	}

	/**
	 * 推送战斗报告给客户端
	 * 
	 * @param fightEvent	战斗事件对象
	 */
	private void pushFightReport2Client(FightEvent fightEvent) {
		Collection<Long> receivePlayers = fightEvent.getViewPlayers();
		if(receivePlayers == null || receivePlayers.isEmpty()) {
			return;
		}
		
		Context context = fightEvent.getContext();
		ISpire attacker = fightEvent.getAttacker();
		ISpire targeter = fightEvent.getTargeter();
		
		try {
			int skillId = fightEvent.getSkillId();
			Point targetPoint = context.getTargetPoint();
			UnitId attackUnitId = attacker == null ? null : attacker.getUnitId();
			UnitId targetUnitId = targeter == null ? null : targeter.getUnitId();
			
			//先推成员的血量
//			Map<String, Object> resultMap = new HashMap<String, Object>(8);
//			resultMap.put(ResponseKey.SKILL_ID, skillId);
//			resultMap.put(ResponseKey.ACTIVE, attackUnitId);
//			resultMap.put(ResponseKey.TARGET, targetUnitId);
//			resultMap.put(ResponseKey.X, targetPoint.getX());
//			resultMap.put(ResponseKey.Y, targetPoint.getY());
//			resultMap.put(ResponseKey.CRITICAL, context.isCritical());
//			resultMap.put(ResponseKey.INFO, this.getUnitAttributes(context));
//			resultMap.put(ResponseKey.REPORTS, context.getFightReportArray());
			
			if(LOGGER.isDebugEnabled()) {
				List<FightReport> fightReports = context.getFightReports();
				FightReport[] fightReportArray = new FightReport[fightReports.size()];
				LOGGER.debug("接受者:[{}] 攻击战报: {}", receivePlayers, Arrays.toString(fightReports.toArray(fightReportArray)));
			}
			List<AnimalInfo> unitAttributes = this.getUnitAttributes(context);
			FightReportVO fightReportVO = FightReportVO.valueOf(attackUnitId, targetUnitId, skillId, targetPoint, context, unitAttributes );
			Response response = Response.defaultResponse(Module.FIGHT, FightCmd.PUSH_FIGHT_REPORT, fightReportVO );
			pusher.pushMessage(receivePlayers, response);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			fightFutureFacade.executeFightContext(attacker, context);
		}
	}
	
	/**
	 * 推送DOT持续伤害
	 * 
	 * @param  unitId		掉血的单位
	 * @param  receives		接收掉血信息的单位
	 * @param  dotInfoVOs	掉血详细信息数组
	 */
	public static void pushDOTDamage2Client(UnitId unitId, Collection<Long> receives, Collection<DOTInfoVO> dotInfoVOs) {
		if(unitId == null || receives == null || receives.isEmpty() || dotInfoVOs.isEmpty()) {
			return;
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("推送单位:[{}] DOT信息:[{}] 给玩家:[{}] ", new Object[] { unitId, Arrays.toString(dotInfoVOs.toArray()), receives });
		}

		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.UNITID, unitId);
		resultMap.put(ResponseKey.INFO, dotInfoVOs.toArray());
		Response response = Response.defaultResponse(Module.FIGHT, FightCmd.PUSH_DOT_DAMAGE, resultMap);
		getInstance().pusher.pushMessage(receives, response);
	}
	
	/**
	 * 推送基础属性值, BUFF给区域玩家. (HP/MP, BUFF信息)
	 * 
	 * @param  context				战斗上下文对象
	 * @return {@link AnimalInfo}	返回值战斗属性单位列表
	 */
	private List<AnimalInfo> getUnitAttributes(Context context) {
		List<AnimalInfo> animalInfos = new ArrayList<AnimalInfo>();
		Map<UnitId, Map<Integer, Integer>> changes = context.getAttributeChanges();
		for (Entry<UnitId, Map<Integer, Integer>> entry : changes.entrySet()) {
			UnitId unitId = entry.getKey();
			Map<Integer, Integer> attributes = entry.getValue();
			if(unitId == null || attributes == null || attributes.isEmpty()) {
				continue;
			}
			
			AnimalInfo animalInfo = null;
			ElementType type = unitId.getType();
			if(type == ElementType.PLAYER) {
				animalInfo = UserPushHelper.getPlayreAnimalInfo(unitId, attributes.keySet().toArray());
			} else if(type == ElementType.MONSTER) {
				animalInfo = UserPushHelper.getMonsterAnimalInfo(unitId, attributes.keySet().toArray());
			} else if(type == ElementType.PET) {
				animalInfo = UserPushHelper.getPetAnimalInfo(unitId, attributes.keySet().toArray());
			}
			
			if(animalInfo != null) {
				animalInfos.add(animalInfo);
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("推送战斗单位:[{}] 属性类型:[{}] 值:[{}]", new Object[] { unitId, attributes.keySet(), animalInfo });
				}
			}
		}
		
		return animalInfos;
		//UserPushHelper.pushAttribute2Members(playerId, playerIdList, animalInfos);
	}
}
