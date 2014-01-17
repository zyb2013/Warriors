package com.yayo.warriors.module.user.helper;

import static com.yayo.warriors.module.pack.type.BackpackType.*;
import static com.yayo.warriors.module.user.type.AttributeKeys.*;
import static com.yayo.warriors.type.FormulaKey.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.event.EventBus;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.adapter.EquipService;
import com.yayo.warriors.basedb.adapter.RoleUpgradeService;
import com.yayo.warriors.basedb.adapter.ShenwuService;
import com.yayo.warriors.basedb.adapter.SkillService;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.MortalAddedConfig;
import com.yayo.warriors.basedb.model.MortalBodyConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.RoleUpgradeConfig;
import com.yayo.warriors.basedb.model.ShenwuAttributeConfig;
import com.yayo.warriors.basedb.model.SkillConfig;
import com.yayo.warriors.basedb.model.SkillEffectConfig;
import com.yayo.warriors.basedb.model.StarSuitConfig;
import com.yayo.warriors.basedb.model.SuitConfig;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.common.helper.EquipPushHelper;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.event.LevelUpEvent;
import com.yayo.warriors.module.alliance.entity.PlayerAlliance;
import com.yayo.warriors.module.alliance.manager.AllianceManager;
import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.friends.manager.FriendManager;
import com.yayo.warriors.module.horse.entity.Horse;
import com.yayo.warriors.module.horse.manager.HorseManager;
import com.yayo.warriors.module.logger.log.PlayerLevelLogger;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.meridian.entity.Meridian;
import com.yayo.warriors.module.meridian.manager.MeridianManager;
import com.yayo.warriors.module.mortal.entity.UserMortalBody;
import com.yayo.warriors.module.mortal.manager.MortalManager;
import com.yayo.warriors.module.mortal.rule.MortalRule;
import com.yayo.warriors.module.pet.manager.PetManager;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.AttributeVO;
import com.yayo.warriors.module.props.model.HoleInfo;
import com.yayo.warriors.module.props.model.ShenwuSwitch;
import com.yayo.warriors.module.props.type.EquipType;
import com.yayo.warriors.module.skill.entity.UserSkill;
import com.yayo.warriors.module.skill.model.SkillVO;
import com.yayo.warriors.module.skill.type.SkillEffectType;
import com.yayo.warriors.module.task.manager.EscortTaskManager;
import com.yayo.warriors.module.team.manager.TeamManager;
import com.yayo.warriors.module.team.model.Team;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.UserAttach;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.rule.PlayerRule;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.type.ElementType;
import com.yayo.warriors.type.FormulaKey;

/**
 * 用户帮助类
 * 
 * @author Hyint
 */
@Component
public class UserHelper {
	@Autowired
	private EventBus eventBus;
	@Autowired
	private DbService dbService;
	@Autowired
	private PetManager petManager;
	@Autowired
	private TeamManager teamManager;
	@Autowired
	private SkillService skillService;
	@Autowired
	private HorseManager horseManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private EquipService equipService;
	@Autowired
	private ShenwuService shenwuService;
	@Autowired
	private MortalManager mortalManager;
	@Autowired
	private FriendManager friendManager;
	@Autowired
	private EscortTaskManager escrotManager;
	@Autowired
	private AllianceManager allianceManager;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private MeridianManager meridianManager;
	@Autowired
	private RoleUpgradeService roleUpgradeService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserHelper.class);
	private static final ObjectReference<UserHelper> ref = new ObjectReference<UserHelper>();
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	/**
	 * 获得用户实例
	 * 
	 * @return {@link UserHelper}	用户帮助类
	 */
	private static UserHelper getInstance() {
		return ref.get();
	}
	
	/**
	 * 查询升级配置
	 * 
	 * @param  job							角色的职业	
	 * @param  level						角色的等级
	 * @return {@link RoleUpgradeConfig}	角色升级配置对象
	 */
	public static long getUpgradeExp(int job, int level) {
		RoleUpgradeConfig upgrade = getInstance().roleUpgradeService.getRoleUpgradeConfig(job, level);
		return upgrade == null ? 0L : upgrade.getExp();
	}

	/**
	 * 查询升级配置
	 * 
	 * @param  job							角色的职业	
	 * @param  level						角色的等级
	 * @return {@link RoleUpgradeConfig}	角色升级配置对象
	 */
	public static RoleUpgradeConfig getUpgradeConfig(int job, int level) {
		return getInstance().roleUpgradeService.getRoleUpgradeConfig(job, level);
	}
	
	/**
	 * 获得用户属性附加对象
	 * 
	 * @param  playerId				角色ID
	 * @return {@link UserAttach}	用户附加对象
	 */
	public static UserAttach getUserAttach(UserDomain userDomain, PlayerBattle playerBattle) {
		Job job = playerBattle.getJob();
		long playerId = playerBattle.getId();
		Player player = userDomain.getPlayer();
		UserAttach userAttach = new UserAttach();
		UserSkill userSkill = userDomain.getUserSkill();
		UserBuffer userBuffer = userDomain.getUserBuffer();
		getInstance().getUserSkillAttach(userSkill, userAttach);				// 获得用户技能附加属性
		getInstance().getUserHorseAttach(playerBattle, userAttach);    			// 计算角色的坐骑附加
		getInstance().getUserEquipAttach(player, playerBattle, userAttach);		// 获得装备附加
		getInstance().getUserBufferAttach(userBuffer, userAttach);				// 获得BUFF附加
		getInstance().getUserMeridianAttach(playerId, userAttach);				// 计算角色的经脉附加
		getInstance().getUserMortalAttach(playerBattle, job.ordinal(), userAttach);	// 计算用户肉身系统
		getInstance().getUserMergedAttach(playerBattle, userAttach);            //家将真传(附身)
		getInstance().getUserAllianceSkillAttach(playerBattle, userAttach);     //帮派技能属性
		getInstance().getFriendlyAttach(playerId, userAttach);                  //计算好友组队加成
		return userAttach;
	}
	
	/**
	 * 获得用户BUFF附加属性
	 * 
	 * @param  playerId			角色ID
	 * @param  userAttach		用户附加属性对象
	 */
	private void getUserBufferAttach(UserBuffer userBuffer, UserAttach userAttach) {
		userAttach.getBeforeBufferable().set(AttributeKeys.FIGHT_EXP_RATE, 1000);
		userAttach.getBeforeBufferable().set(AttributeKeys.TRAIN_EXP_RATE, 1000);
		userAttach.getBeforeBufferable().set(AttributeKeys.TRAIN_GAS_RATE, 1000);
		Map<Integer, Buffer> copyBuffers = userBuffer.getAndCopyBufferMap();
		if(copyBuffers == null || copyBuffers.isEmpty()) {
			return;
		}
		
		for (Buffer buffer : copyBuffers.values()) {
			if(buffer == null || buffer.isTimeOut()) {
				continue;
			}
			
			int effectId = buffer.getId();
			SkillEffectConfig skillEffect = skillService.getSkillEffectConfig(effectId);
			if(skillEffect == null) {
				continue;
			}
			
			/* 增加力量, 增加敏捷, 增加体质, 增加精神, 增加智力. 策划会在BUFF上避免这几个属性值的BUFF */
			int damageValue = buffer.getDamage();
			int effectType = skillEffect.getEffectType();
			SkillEffectType skillEffectType = SkillEffectType.getSkillEffectType(effectType);
			if(skillEffectType == null) {
				continue;
			}
			
			int attribute = skillEffectType.getAttribute();
			if(attribute >= 0) { //直接附加属性的
				if(skillEffectType.isAdd()) {
					userAttach.getBeforeBufferable().add(attribute, damageValue);
				} else {
					userAttach.getBeforeBufferable().set(attribute, damageValue);
				}
			} else {
				userAttach.getAfterBufferable().add(effectType, damageValue);
			}
		}
	}
	
	/**
	 * 附加装备星级套装属性
	 * 
	 * @param  attach		角色属性集合
	 * @param  starInfos	星级属性集合
	 */
	private void calculateStarSuitAttach(Fightable attach, Map<Integer, Integer> starInfos) {
		for (Entry<Integer, Integer> entry : starInfos.entrySet()) {
			Integer star = entry.getKey();
			Integer count = entry.getValue();
			if(star == null || count == null || count < PlayerRule.STAR_SUIT_COUNT_LIMIT) {
				continue;
			}
			
			StarSuitConfig starSuitConfig = resourceService.get(star, StarSuitConfig.class);
			if(starSuitConfig == null) {
				continue;
			}
			
			Map<Integer, Integer> attributes = starSuitConfig.getAttributes();
			if(attributes == null || attributes.isEmpty()) {
				continue;
			}
			
			for (Entry<Integer, Integer> attributeEntry : attributes.entrySet()) {
				Integer attribute = attributeEntry.getKey();
				Integer attrValue = attributeEntry.getValue();
				if(attribute != null && attrValue != null) {
					attach.add(attribute, attrValue);
				}
			}
		}
	}

	/**
	 * 附加装备套装属性
	 * 
	 * @param  attach		角色属性集合
	 * @param  suitInfos	套装属性集合
	 */
	private void calculateEquipSuitAttach(Fightable attach, Map<Integer, Integer> suitInfos) {
		for (Entry<Integer, Integer> entry : suitInfos.entrySet()) {
			Integer suitId = entry.getKey();
			Integer suitCount = entry.getValue();
			suitId = 1;
			if(suitId == null || suitCount == null || suitId <= 0) {
				continue;
			}
			
			SuitConfig suit = equipService.getSuitConfig(suitId, suitCount);
			if(suit == null) {
				continue;
			}
			
			Map<Integer, Integer> attributes = suit.getAttributes();
			if(attributes == null || attributes.isEmpty()) {
				continue;
			}
			
			for (Entry<Integer, Integer> attributeEntry : attributes.entrySet()) {
				Integer attribute = attributeEntry.getKey();
				Integer attrValue = attributeEntry.getValue();
				if(attribute != null && attrValue != null) {
					attach.add(attribute, attrValue);
				}
			}
		}
	}
	
	
	/**
	 * 计算技能附加的属性值
	 * 
	 * @param  playerId		角色ID
	 * @param  attach		附加的属性值集合
	 */
	private void getUserSkillAttach(UserSkill userSkill, UserAttach attach) {
		if(userSkill == null) {
			return;
		}
		
		Map<Integer, SkillVO> passiveSkillMap = userSkill.getPassiveSkillMap();
		if(passiveSkillMap == null || passiveSkillMap.isEmpty()) {
			return;
		}
			
		for (SkillVO skillVO : passiveSkillMap.values()) {
			int skillId = skillVO.getId();
			SkillConfig skillConfig = resourceService.get(skillId, SkillConfig.class);
			if(skillConfig == null) {
				continue;
			}
			
			List<SkillEffectConfig> skillEffects = skillConfig.getSkillEffects();
			if(skillEffects == null || skillEffects.isEmpty()) {
				continue;
			}
			
			int skillLevel = skillVO.getLevel();
			for (SkillEffectConfig skillEffect : skillEffects) {
				int effectType = skillEffect.getEffectType();
				int addValue = skillEffect.calcSkillEffect(1, skillLevel).intValue();
				SkillEffectType skillEffectType = SkillEffectType.getSkillEffectType(effectType);
				if(skillEffectType != null && skillEffectType.getAttribute() > 0) {
					attach.getSkillable().add(skillEffectType.getAttribute(), addValue);
				}
			}
		}
	}
	
	
	/**
	 * 把属性值加入集合中		
	 * 
	 * @param attributes	属性值集合
	 * @param attribute		属性
	 * @param addValue		附加属性值
	 */
	private void addAttribute2Map(Map<Integer, Integer> attributes, int attribute, int addValue) {
		for (int level = 1; level <= attribute; level+=2) {
			Integer cacheCount = attributes.get(level);
			cacheCount = cacheCount == null ? 0 : cacheCount;
			attributes.put(level, cacheCount + 1);
		}
	}
	
	/**
	 * 获得装备附加属性
	 * 
	 * @param playerId			角色ID
	 * @param userAttach		用户附加属性
	 */
	private void getUserEquipAttach(Player player, PlayerBattle battle, UserAttach userAttach) {
		Job job = battle.getJob();
		long playerId = battle.getId();
		userAttach.getEquipable().set(EquipType.CLOTHES_TYPE, job.getClosing());	//默认的装备模型
		List<UserEquip> dressEquips = propsManager.listUserEquip(playerId, DRESSED_BACKPACK);
		if(dressEquips == null || dressEquips.isEmpty()) {
			return;
		}
		
		Map<Integer, Integer> suitInfos = new HashMap<Integer, Integer>();
		Map<Integer, Integer> starInfos = new HashMap<Integer, Integer>();
		for (UserEquip userEquip : dressEquips) {
			EquipConfig equipConfig = userEquip.getEquipConfig();
			if(equipConfig == null) {
				continue;
			}
			
			if(userEquip.getCurrentEndure() <= 0 && !equipConfig.isFaction()) { //时装没有耐久度, 所以没有耐久都可以穿
				continue;
			} 

			if(!userEquip.isOutOfExpiration()) { //未超时
				this.addEquipAttribute2Attach(userEquip, userAttach);
				this.addEquipShenwuAttribute2Attach(userEquip, userAttach);
				this.addAttribute2Map(starInfos, userEquip.getStarLevel(), 1);
				this.addAttribute2Map(suitInfos, equipConfig.getSuitId(), 1);
				this.addEquipHoleAttribute2Attach(userEquip.getHoleInfos().values(), userAttach.getEquipHoles());
			} else {
				if(equipConfig.isFaction()) {
					userAttach.setFactionOutOfExpiration(true);
				}
			}
		}
		this.calculateEquipSuitAttach(userAttach.getEquipSuits(), suitInfos);
		this.calculateStarSuitAttach(userAttach.getEquipStarSuits(), starInfos);
	}
	
	/**
	 * 增加装备的神武属性
	 * 
	 * @param  userEquip		装备对象
	 * @param  userAttach		装备的附加对象
	 */
	private void addEquipShenwuAttribute2Attach(UserEquip userEquip, UserAttach userAttach) {
		EquipConfig equipConfig = userEquip.getEquipConfig();
		int job = equipConfig.getJob();
		int propsType = equipConfig.getPropsType();
		Map<Integer, ShenwuSwitch> shenwuSwitches = userEquip.getShenwuSwitches();
		for (Entry<Integer, ShenwuSwitch> entry : shenwuSwitches.entrySet()) {
			Integer shenwuId = entry.getKey();
			ShenwuSwitch shenwuSwitch = entry.getValue();
			if(shenwuId == null || shenwuSwitch == null || !shenwuSwitch.isTempo()) {
				continue;
			}
			
			List<ShenwuAttributeConfig> shenwuAttributes = shenwuService.listShenwuAttribute(shenwuId, propsType, job);
			if(shenwuAttributes != null && !shenwuAttributes.isEmpty()) {
				for (ShenwuAttributeConfig shenwuAttribute : shenwuAttributes) {
					int attribute = shenwuAttribute.getAttribute();
					int maxTempoValue = shenwuAttribute.getMaxTempoValue();
					userAttach.getShenwuable().add(attribute, maxTempoValue);
				}
			}
		}
		
		Map<Integer, Map<Integer, AttributeVO>> shenwuAttributes = userEquip.getShenwuAttributeMap();
		for (Map<Integer, AttributeVO> attributeVOMap : shenwuAttributes.values()) {
			for (AttributeVO attributeVO : attributeVOMap.values()) {
				int attribute = attributeVO.getAttribute();
				int attrValue = attributeVO.getAttrValue();
				if(attribute > 0 && attrValue > 0) {
					userAttach.getShenwuable().add(attribute, attrValue);
				}
			}
		}
	}

	/**
	 * 处理角色装备过期
	 * 
	 * @param userDomain
	 * @param isFationTimeout
	 */
	public static void processFationOutExpiration(UserDomain userDomain, boolean isFationTimeout, boolean flushable) {
		if(!isFationTimeout) { //没有装备过期
			return;
		} 
		
		Player player = userDomain.getPlayer();
		if(!player.isFashionShow()) {
			return;
		} 

		ChainLock lock = LockUtils.getLock(player);
		try {
			lock.lock();
			if(!player.isFashionShow()) { //没勾选展示
				return;
			}
			player.setFashionShow(false);
		} finally {
			lock.unlock();
		}
		
		if(flushable) {
			userDomain.updateFlushable(true, Flushable.FLUSHABLE_NORMAL);
		}
		EquipPushHelper.pushDressAttributeChanges(userDomain, AttributeRule.CHANGE_FATION_VIEW);
	}
	
	/**
	 * 附加装备随机和基础属性
	 * 
	 * @param  attributes		装备附加属性列表
	 * @param  userAttach		用户装备属性
	 */
	private void addEquipAttribute2Attach(UserEquip userEquip, UserAttach userAttach) {
		EquipConfig equip = userEquip.getEquipConfig();
		for (AttributeVO attributeVO : userEquip.getBaseAttributeMap().values()) {
			int attribute = attributeVO.getAttribute();
			int attrValue = attributeVO.getAttrValue();
			userAttach.getEquipable().add(attribute, attrValue);
			if(attribute == equip.getAttribute1()) {
				userAttach.getEquipEnhancable().add(attribute, Math.max(0, attrValue - equip.getAttrValue1()));
			} else if(attribute == equip.getAttribute2()) {
				userAttach.getEquipEnhancable().add(attribute, Math.max(0, attrValue - equip.getAttrValue2()));
			} else if(attribute == equip.getAttribute3()) {
				userAttach.getEquipEnhancable().add(attribute, Math.max(0, attrValue - equip.getAttrValue3()));
			} else if(attribute == equip.getAttribute4()) {
				userAttach.getEquipEnhancable().add(attribute, Math.max(0, attrValue - equip.getAttrValue4()));
			}
		}
			
			for (AttributeVO attributeVO : userEquip.getAdditionAttributeMap().values()) {
				try {
					int attribute = attributeVO.getAttribute();
					int attrValue = attributeVO.getAttrValue();
					userAttach.getEquipAdditions().add(attribute, attrValue);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	}

	/**
	 * 附加装备随机和基础属性
	 * 
	 * @param  attributes		装备附加属性列表
	 * @param  attacher			战斗属性对象
	 */
	private void addEquipHoleAttribute2Attach(Collection<HoleInfo> attributes, Fightable attacher) {
		if(attributes != null && !attributes.isEmpty()) {
			for (HoleInfo holeInfo : attributes) {
				PropsConfig propsConfig = propsManager.getPropsConfig(holeInfo.getItemId());
				if(propsConfig != null) {
					int childType = propsConfig.getChildType();
					int attrValue = (int)Math.round(propsConfig.getAttrValue());
					PlayerRule.addHoleItem2Attach(childType, attrValue, attacher);
				}
				
			}
		}
	}
	
	/**
	 * 计算用户肉身附加
	 * 
	 * @param  playerId		角色ID
	 * @param  attach		附加属性
	 */
	private void getUserMortalAttach(PlayerBattle playerBattle, int job, UserAttach attach) {
		UserMortalBody mortal = mortalManager.getUserMortalBody( playerBattle );
		if(mortal == null) {
			return;
		} 

		Map<Integer, Integer> mortalBodyMap = mortal.getMortalBodyMap();
		if(mortalBodyMap == null || mortalBodyMap.isEmpty()) {
			return;
		}
		
		Set<Entry<Integer, Integer>> entrySet = mortalBodyMap.entrySet();
		for (Iterator<Entry<Integer, Integer>> it = entrySet.iterator(); it.hasNext();) {
			Entry<Integer, Integer> entry = it.next();
			Integer type = entry.getKey();
			Integer level = entry.getValue();
			if(type == null || level == null) {
				continue;
			}
			
			MortalBodyConfig config = mortalManager.getMorbodyConfig(job, type, level);
			if(config == null) {
				continue;
			}
			
			int[] attributes = config.getAttributes();
			for (int i = 0; i <= attributes.length - 1; i++) {
				attach.getMortalable().add(attributes[i], config.getValues()[i]);
			}
		}
		
		// 计算肉身附加属性 ------- 超平
		int minLevel = mortalManager.getMortalMinLevel(playerBattle);
		int level = MortalRule.getMortalAddedLevel(minLevel);		
		MortalAddedConfig config = mortalManager.getMortalAddedConfig(job, level);
		if (config != null) {
			int [] attributes = config.getAttributes();
			for (int i = 0; i <= attributes.length - 1; i++) {
				attach.getMortalable().add(attributes[i], config.getValues()[i]);
			}
		}
		
	}
 
	/**
	 * 计算经脉点.
	 * 
	 * @param  playerId		角色ID
	 * @param  attach		战斗属性
	 */
	private void getUserMeridianAttach(long playerId, UserAttach attach) {
		Meridian meridian = meridianManager.getMeridian(playerId);
		if(meridian != null && !meridian.isAttributeEmpty()) {
			attach.getMeridianable().putAll(meridian.getMeridianAttributes());
		}
	}
	
	/**
	 * 刷新角色战斗属性
	 * 
	 * @param battle		角色战斗属性
	 */
	public static PlayerBattle refreshBattleAttribute(PlayerBattle battle, UserDomain userDomain) {
		Fightable beforable = null;
		List<Integer> levels = getInstance().checkPlayerLevelUp(battle, userDomain.getPlayer(), userDomain.getMotion() );
		if(levels != null && !levels.isEmpty()) {
			beforable = battle.getAndCopyAttributes();
		}
		
		getInstance().refreshPlayerBattle(battle, userDomain);
		if(levels != null && !levels.isEmpty()) {
			getInstance().dbService.submitUpdate2Queue(battle);
			getInstance().eventBus.post(LevelUpEvent.valueOf(beforable, userDomain));
		}
		return battle;
	}
	
	/**
	 * 检测角色是否升级
	 * 
	 * @param  battle				角色战斗属性
	 * @param  player				角色对象
	 * @return {@link Boolean}		true-升级, false-不升级
	 */
	private List<Integer> checkPlayerLevelUp(PlayerBattle battle, Player player, PlayerMotion motion) {
		List<Integer> levels = null;
		if(battle.getExp() <= 0) {
			return levels;
		}

		int level = battle.getLevel();
		if(level >= PlayerRule.getMaxPlayerLevel()) {
			return levels;
		}
		
		int job = battle.getJob().ordinal();
		if(battle.getExp() < getUpgradeExp(job, level)){
			return levels;
		}
		
		ChainLock lock = LockUtils.getLock(battle);
		try {
			lock.lock();
			if(battle.getExp() < getUpgradeExp(job, level)) { // 已到达升级边界
				return levels;
			}
			levels = this.validPlayerUpgrade(battle, player, motion);
		} finally {
			lock.unlock();
		}
		return levels;
	}
	
	/**
	 * 计算战斗属性
	 * 
	 * @param  battle				角色战斗对象
	 * @param  roleUpgrade			角色升级对象
	 * @param  userAttach			用户继承属性
	 * @return {@link Fightable}	角色战斗属性集合
	 */
	public Fightable calcFightAttribute(PlayerBattle battle, RoleUpgradeConfig roleUpgrade, UserAttach userAttach) {
		Job job = battle.getJob();
		int level = battle.getLevel();
		Fightable attach = new Fightable();
		int baseStrength = roleUpgrade.getStrength() + battle.getStrength();
		int baseDexerity = roleUpgrade.getDexerity() + battle.getDexerity();
		int baseIntellect = roleUpgrade.getIntellect() + battle.getIntellect();
		int baseConstitution = roleUpgrade.getConstitution() + battle.getConstitution();
		int baseSpirituality = roleUpgrade.getSpirituality() + battle.getSpirituality();
		
		//开始进行属性计算
		attach.set(LEVEL, level);
		attach.set(STRENGTH, baseStrength);
		attach.set(DEXERITY, baseDexerity);
		attach.set(INTELLECT, baseIntellect);
		attach.set(CONSTITUTION, baseConstitution);
		attach.set(SPIRITUALITY, baseSpirituality);
		
		//计算家将, 坐骑属性, BUFFER属性
		attach.addAll(userAttach.getUserPetMerged());           // 计算角色的家将附身附加
		attach.addAll(userAttach.getHorsesable());				// 计算角色的坐骑附加
		attach.addAll(userAttach.getBeforeBufferable());		// 计算用户BUFF信息和返回延迟附加属性

		//装备附加
		attach.addAll(userAttach.getEquipable());				// 计算角色的装备基础属性
		attach.addAll(userAttach.getEquipSuits());				// 计算角色的装备套装附加
		attach.addAll(userAttach.getEquipAdditions());			// 计算角色的装备附加复兴
		attach.addAll(userAttach.getEquipStarSuits());			// 计算角色的装备星级附加
		attach.addAll(userAttach.getMortalable());				// 计算用户肉身系统
		attach.addAll(userAttach.getSkillable());				// 计算角色的技能附加
		attach.addAll(userAttach.getMeridianable());			// 计算角色的经脉附加
		attach.addAll(userAttach.getShenwuable());				// 计算角色的神武属性附加
		attach.addAll(userAttach.getFriendlyAdded());           // 计算好友组队附加

		//这里是真实计算属性值
		attach.add(HP, battle.getHp());
		attach.add(MP, battle.getMp());
		attach.add(GAS, battle.getGas());
		attach.add(GAS_MAX, PlayerRule.INIT_GAS_MAX);
		int newStrength = attach.getAttribute(STRENGTH);
		int newDexerity = attach.getAttribute(DEXERITY);
		int newIntellect = attach.getAttribute(INTELLECT);
		int newConstitution = attach.getAttribute(CONSTITUTION);
		int newSpirituality = attach.getAttribute(SPIRITUALITY);
		attach.add(MOVE_SPEED, PlayerRule.PERSON_FIXED_SPEED + battle.getMoveSpeed());
		
		//角色自身成长物理防御和法术防御计算
		int theurgyFormulaId = PlayerRule.getDefenseFormulaId(job, false);
		int physicalFormulaId = PlayerRule.getDefenseFormulaId(job, true);
		int theurgyDefense = FormulaHelper.invoke(theurgyFormulaId, level).intValue();
		int physicalDefense = FormulaHelper.invoke(physicalFormulaId, level).intValue();
		int dodge = FormulaHelper.invoke(DODGE_FORMULA, Integer.class, level, newDexerity);
		int calcMpMax = FormulaHelper.invoke(MPMAX_FORMULA, Integer.class, level, newIntellect);
		int calcHpMax = FormulaHelper.invoke(HPMAX_FORMULA, Integer.class, level, newConstitution);
		int physicalAttack = FormulaHelper.invoke(PHYSICAL_ATTACK_FORMULA, Integer.class, level, newStrength);
		int theurgyAttack = FormulaHelper.invoke(THEURGY_ATTACK_FORMULA, Integer.class, level, newSpirituality);
		int theurgyCritical = FormulaHelper.invoke(THEURGY_CRITICAL_FORMULA, Integer.class, level, newIntellect);
		int physicalCritical = FormulaHelper.invoke(PHYSICAL_CRITICAL_FORMULA, Integer.class, level, newDexerity);
		
		attach.add(HIT, battle.getHit());
		attach.add(DODGE, dodge + battle.getDodge());
		attach.add(MP_MAX, calcMpMax + battle.getAddMpMax());
		attach.add(HP_MAX, calcHpMax + battle.getAddHpMax());
		attach.add(THEURGY_ATTACK, theurgyAttack + battle.getTheurgyAttack());
		attach.add(THEURGY_DEFENSE, theurgyDefense + battle.getTheurgyDefense());
		attach.add(PHYSICAL_ATTACK, physicalAttack + battle.getPhysicalAttack());
		attach.add(PHYSICAL_DEFENSE, physicalDefense + battle.getPhysicalDefense());
		attach.add(THEURGY_CRITICAL, theurgyCritical + battle.getTheurgyCritical());
		attach.add(PHYSICAL_CRITICAL, physicalCritical + battle.getPhysicalCritical());
		
		//角色基础属性战斗力计算
		Fightable baseFightable = new Fightable(10);
		baseFightable.set(HIT, 0);
		baseFightable.set(THEURGY_DEFENSE, theurgyDefense);
		baseFightable.set(PHYSICAL_DEFENSE, physicalDefense);
		baseFightable.set(DODGE, FormulaHelper.invoke(DODGE_FORMULA, Integer.class, level, baseDexerity));
		baseFightable.set(MP_MAX, FormulaHelper.invoke(MPMAX_FORMULA, Integer.class, level, baseIntellect));
		baseFightable.set(HP_MAX, FormulaHelper.invoke(HPMAX_FORMULA, Integer.class, level, baseConstitution));
		baseFightable.set(THEURGY_ATTACK, FormulaHelper.invoke(THEURGY_ATTACK_FORMULA, Integer.class, level, baseSpirituality));
		baseFightable.set(PHYSICAL_ATTACK, FormulaHelper.invoke(PHYSICAL_ATTACK_FORMULA, Integer.class, level, baseStrength));
		baseFightable.set(THEURGY_CRITICAL, FormulaHelper.invoke(THEURGY_CRITICAL_FORMULA, Integer.class, level, baseIntellect));
		baseFightable.set(PHYSICAL_CRITICAL, FormulaHelper.invoke(PHYSICAL_CRITICAL_FORMULA, Integer.class, level, baseDexerity));

		// 计算装备镶嵌宝石属性值
		PlayerRule.processEquipHolesAttach(userAttach.getEquipHoles(), attach);			
		
		//------------------------- 以上已经全部附加所有二级属性 ------------------------------
		
		
		//------------------------- 百分比运算 --------------------------------------------
		double hitRatio = 1 + Tools.divideAndRoundDown(attach.get(HIT_RATIO), RATE_BASE, 3);
		double dodgeRatio = 1 + Tools.divideAndRoundDown(attach.get(DODGE_RATIO), RATE_BASE, 3);
		double hpMaxRatio = 1 + Tools.divideAndRoundDown(attach.get(HPMAX_RATIO), RATE_BASE, 3);
		double mpMaxRatio = 1 + Tools.divideAndRoundDown(attach.get(MPMAX_RATIO), RATE_BASE, 3);
		double theurgeAttackRatio = 1 + Tools.divideAndRoundDown(attach.get(THEURGY_ATTACK_RATIO), RATE_BASE, 3);
		double physicalAttackRatio = 1 + Tools.divideAndRoundDown(attach.get(PHYSICAL_ATTACK_RATIO), RATE_BASE, 3);
		double theurgeDefenseRatio = 1 + Tools.divideAndRoundDown(attach.get(THEURGY_DEFENSE_RATIO), RATE_BASE, 3);
		double physicalDefenseRatio = 1 + Tools.divideAndRoundDown(attach.get(PHYSICAL_DEFENSE_RATIO), RATE_BASE, 3);
		double theurgyCriticalRatio = 1 + Tools.divideAndRoundDown(attach.get(THEURGY_CRITICAL_RATIO), RATE_BASE, 3);
		double physicalCriticalRatio = 1 + Tools.divideAndRoundDown(attach.get(PHYSICAL_CRITICAL_RATIO), RATE_BASE, 3);
		
		attach.set(AttributeKeys.HIT, (int) (attach.get(AttributeKeys.HIT) * hitRatio));
		attach.set(AttributeKeys.DODGE, (int) (attach.get(AttributeKeys.DODGE) * dodgeRatio));
		attach.set(AttributeKeys.HP_MAX, (int) (attach.get(AttributeKeys.HP_MAX) * hpMaxRatio));
		attach.set(AttributeKeys.MP_MAX, (int) (attach.get(AttributeKeys.MP_MAX) * mpMaxRatio));
		attach.set(AttributeKeys.THEURGY_ATTACK, (int) (attach.get(AttributeKeys.THEURGY_ATTACK) * theurgeAttackRatio));
		attach.set(AttributeKeys.PHYSICAL_ATTACK, (int) (attach.get(AttributeKeys.PHYSICAL_ATTACK) * physicalAttackRatio));
		attach.set(AttributeKeys.THEURGY_DEFENSE, (int) (attach.get(AttributeKeys.THEURGY_DEFENSE) * theurgeDefenseRatio));
		attach.set(AttributeKeys.PHYSICAL_DEFENSE, (int) (attach.get(AttributeKeys.PHYSICAL_DEFENSE) * physicalDefenseRatio));
		attach.set(AttributeKeys.THEURGY_CRITICAL, (int) (attach.get(AttributeKeys.THEURGY_CRITICAL) * theurgyCriticalRatio));
		attach.set(AttributeKeys.PHYSICAL_CRITICAL, (int) (attach.get(AttributeKeys.PHYSICAL_CRITICAL) * physicalCriticalRatio));
		PlayerRule.processAfterBufferAttach(userAttach.getAfterBufferable(), attach);	// 所有属性计算完, 再计算角色的BUFF信息
		this.calculatePlayerFightCapacity(attach, userAttach, battle, baseFightable);	// 计算角色的战斗属性
		return attach;
	}
	
	/**
	 * 计算角色的战斗属性
	 * 
	 * @param battle		角色的战斗信息
	 * @param userAttach	用户属性集合
	 */
	private void refreshFightableProperties(PlayerBattle battle, UserAttach userAttach) {
		Job job = battle.getJob();
		int level = battle.getLevel();
		RoleUpgradeConfig upgradeConfig = getUpgradeConfig(job.ordinal(), level);
		if(upgradeConfig == null) {
			return;
		}
		 
		long baseExpMax = upgradeConfig.getExp();
		Fightable attach = this.calcFightAttribute(battle, upgradeConfig, userAttach);
		if(!userAttach.isRiding()) { //非乘骑状态, 需要计算出乘骑状态的属性
			userAttach.getHorsesable().clear();
			userAttach.getHorsesable().addAll(userAttach.getRidHorsesable());
			Fightable ridAttach = calcFightAttribute(battle, upgradeConfig, userAttach);
			int playerTotalCapacity = ridAttach.get(AttributeKeys.FIGHT_TOTAL_CAPACITY);
			attach.set(AttributeKeys.PLAYER_FIGHT_TOTAL_CAPACITY, playerTotalCapacity);
		} else { //当前乘骑状态, 直接取当前的角色战斗力属性
			int playerTotalCapacity = attach.get(AttributeKeys.FIGHT_TOTAL_CAPACITY);
			attach.set(AttributeKeys.PLAYER_FIGHT_TOTAL_CAPACITY, playerTotalCapacity);
		}
		
		battle.putAttributes(attach, true);
		battle.setExpMax(baseExpMax);
		battle.setAttribute(HP_MAX, attach.getAttribute(HP_MAX));
		battle.setAttribute(MP_MAX, attach.getAttribute(MP_MAX));
		battle.setAttribute(GAS_MAX, attach.getAttribute(GAS_MAX));
		battle.setAttribute(HP, attach.getAttribute(HP));
		battle.setAttribute(MP, attach.getAttribute(MP));
		battle.setAttribute(GAS, attach.getAttribute(GAS));
		battle.setLevel(battle.getAttribute(LEVEL));
	}
	
	/**
	 * 计算角色的战斗力属性
	 * 
	 * @param  playerAttach		角色的附加对象
	 * @param  userAttach		用户Attach对象
	 * @param  battle			角色的战斗对象
	 * @param  upgrade			升级属性对象
	 * @param  baseFightable	基础战斗力属性对象
	 */
	private void calculatePlayerFightCapacity(Fightable playerAttach, UserAttach userAttach, PlayerBattle battle, Fightable baseFightable) {
		Job playerJob = battle.getJob();
		int baseCapacity = calcCommonAttributeCapacity(baseFightable, playerJob);							//角色基础属性
		int dungeonRewardCapacity = calcDungeonAttributeCapacity(battle, playerJob);						//剧情副本战斗力属性
		int horseCapacity = calcHorseCapacity(userAttach.getHorsesable(), playerJob);						//坐骑战斗力属性属性
		int mortalCapacity = calcCommonRatioCapacity(userAttach.getMortalable(), playerJob);				//肉身战斗力加成属性
		int meridianCapacity = calcCommonRatioCapacity(userAttach.getMeridianable(), playerJob);			//经脉战斗力加成属性
		int playerCapacity = baseCapacity + meridianCapacity + mortalCapacity + dungeonRewardCapacity;		//角色的个人属性战斗力
		
		int equipStarCapacity = calcEquipStarCapacity(userAttach, playerJob);								//装备星级战斗力属性
		int equipBaseCapacity = calcEquipBaseCapacity(userAttach.getEquipable(), playerJob);				//装备基础战斗力属性
		int equipSuitCapacity = calcCommonAttributeCapacity(userAttach.getEquipSuits(), playerJob);			//装备附加属性战斗力属性
		int equipEnchangeCapacity = calcEquipEnchangeCapacity(userAttach.getEquipHoles(), playerJob);		//装备的镶嵌属性战斗力
		int equipAdditionCapacity = calcEquipAdditionCapacity(userAttach.getEquipAdditions(), playerJob);	//装备星级战斗力属性
		int equipTotalCapacity = equipStarCapacity + equipBaseCapacity  + equipSuitCapacity 
							   + equipAdditionCapacity + equipEnchangeCapacity;
		
		playerAttach.set(AttributeKeys.FIGHT_HORSE_CAPACITY, horseCapacity);			//坐骑战斗力
		playerAttach.set(AttributeKeys.PLAYER_BASIC_CAPACITY, baseCapacity);			//角色基础属性战斗力
		playerAttach.set(AttributeKeys.PLAYER_FIGHT_CAPACITY, playerCapacity);			//角色的战斗力
		playerAttach.set(AttributeKeys.PLAYER_MORTAL_CAPACITY, mortalCapacity);			//肉身属性战斗力
		playerAttach.set(AttributeKeys.FIGHT_HORSE_STAR_CAPACITY, horseCapacity);		//坐骑星级战斗力
		playerAttach.set(AttributeKeys.PLAYER_MERIDIAN_CAPACITY, meridianCapacity);		//经脉属性战斗力
		playerAttach.set(AttributeKeys.EQUIP_ENCHANGE_CAPACITY, equipEnchangeCapacity);	//宝石加成战斗力
		playerAttach.set(AttributeKeys.DUNGEON_REWARD_CAPACITY, dungeonRewardCapacity);	//角色的副本战斗力奖励

		int playerTotalCapacity = playerCapacity + equipTotalCapacity + horseCapacity;	//角色综合战斗力
		playerAttach.set(AttributeKeys.EQUIP_BASE_CAPACITY, equipBaseCapacity);			//装备的基础战斗力
		playerAttach.set(AttributeKeys.EQUIP_STAR_CAPACITY, equipStarCapacity);			//装备的星际战斗力
		playerAttach.set(AttributeKeys.EQUIP_SUIT_CAPACITY, equipSuitCapacity);			//装备的套装战斗力
		playerAttach.set(AttributeKeys.EQUIP_TOTAL_CAPACITY, equipTotalCapacity);		//装备总战斗力
		playerAttach.set(AttributeKeys.FIGHT_TOTAL_CAPACITY, playerTotalCapacity);		//综合属性战斗力
		playerAttach.set(AttributeKeys.EQUIP_ADDITION_CAPACITY, equipAdditionCapacity);	//装备的附加属性战斗力
	}
	
	/**
	 * 计算装备镶嵌战斗力
	 * 
	 * @param  attach			属性对象
	 * @param  playerJob		角色职业
	 * @return {@link Integer}	战斗属性
	 */
	public int calcEquipEnchangeCapacity(Fightable attach, Job playerJob) {
		switch (playerJob) {
			case TIANLONG:	
				return FormulaHelper.invoke(EQUIP_ENCHANGE_TILONG_CAPACITY, 
						attach.getAttribute(PHYSICAL_ATTACK), 	attach.getAttribute(PHYSICAL_DEFENSE),
						attach.getAttribute(PHYSICAL_CRITICAL), attach.getAttribute(HIT),
						attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
						attach.getAttribute(THEURGY_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();	
			case TIANSHAN:	
				return FormulaHelper.invoke(EQUIP_ENCHANGE_TIANSHAN_CAPACITY, 
						attach.getAttribute(PHYSICAL_ATTACK), 	attach.getAttribute(PHYSICAL_DEFENSE),
						attach.getAttribute(PHYSICAL_CRITICAL), attach.getAttribute(HIT),
						attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
						attach.getAttribute(THEURGY_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();
			case XINGXIU:	
				return FormulaHelper.invoke(EQUIP_ENCHANGE_XINGXIU_CAPACITY, 
						attach.getAttribute(THEURGY_ATTACK), 	attach.getAttribute(THEURGY_DEFENSE),
						attach.getAttribute(THEURGY_CRITICAL), attach.getAttribute(HIT),
						attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
						attach.getAttribute(PHYSICAL_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();
			case XIAOYAO:	
				return FormulaHelper.invoke(EQUIP_ENCHANGE_XIAOYAO_CAPACITY, 
						attach.getAttribute(THEURGY_ATTACK), 	attach.getAttribute(THEURGY_DEFENSE),
						attach.getAttribute(THEURGY_CRITICAL), attach.getAttribute(HIT),
						attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
						attach.getAttribute(PHYSICAL_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();
		}
		return 0;
	}
	
	/**
	 * 计算装备基础属性战斗力
	 * 
	 * @param fightable				战斗力数据集合	
	 * @param playerJob				角色的职业
	 * @return {@link Integer}		战斗力总和
	 */
	private int calcEquipBaseCapacity(Fightable fightable, Job playerJob) {
		int totalOne = fightable.get(AttributeKeys.STRENGTH);
		totalOne += fightable.get(AttributeKeys.DEXERITY);
		totalOne += fightable.get(AttributeKeys.INTELLECT);
		totalOne += fightable.get(AttributeKeys.CONSTITUTION);
		totalOne += fightable.get(AttributeKeys.SPIRITUALITY);
		int totalTwo = calcCommonAttributeCapacity(fightable, playerJob);
		return FormulaHelper.invoke(FormulaKey.EQUIP_BASE_CAPACITY, totalOne, totalTwo).intValue();
	}
	
	/**
	 * 计算装备星级战斗力
	 * 
	 * @param  userAttach			角色的附加对象
	 * @param  playerJob			角色的职业
	 * @return {@link Integer}		战斗力总和
	 */
	private int calcEquipStarCapacity(UserAttach userAttach, Job playerJob) {
		Fightable equipStarSuits = userAttach.getEquipStarSuits();
		Fightable equipEnhancable = userAttach.getEquipEnhancable();
		int totalOne = equipEnhancable.get(AttributeKeys.STRENGTH);
		totalOne += equipEnhancable.get(AttributeKeys.DEXERITY);
		totalOne += equipEnhancable.get(AttributeKeys.INTELLECT);
		totalOne += equipEnhancable.get(AttributeKeys.CONSTITUTION);
		totalOne += equipEnhancable.get(AttributeKeys.SPIRITUALITY);
		int totalTwo = calcCommonAttributeCapacity(equipEnhancable, playerJob);
		int totalStarSuits = calcCommonAttributeCapacity(equipStarSuits, playerJob);
		return FormulaHelper.invoke(FormulaKey.EQUIP_STAR_CAPACITY, totalOne, totalTwo, totalStarSuits).intValue();
	}
	
	/**
	 * 计算角色升级后的战斗属性
	 * 
	 * @param battle	角色的战斗属性对象
	 */
	private List<Integer> validPlayerUpgrade(PlayerBattle battle, Player player, PlayerMotion motion) {
		int job = battle.getJob().ordinal();
		RoleUpgradeConfig config = getUpgradeConfig(job, battle.getLevel());
		if(config == null) {
			LOGGER.error("职业:[{}] 等级:[{}] 升级配置对象不存在", job, battle.getLevel());
			return Collections.emptyList();
		}
		
		long nextExp = config.getExp();
		if(battle.getExp() < nextExp) {
			return Collections.emptyList();
		}
		
		List<Integer> levels = new ArrayList<Integer>(0);	// 升级的等级列表
		try {
			int addLevel = 0;								// 升级的等级
			long newExp = battle.getExp(); 					// 当前经验;
			int newLevel = battle.getLevel(); 				// 当前等级
			nextExp = getUpgradeExp(job, newLevel);
			while(newExp >= nextExp) {
				if(newLevel >= PlayerRule.getMaxPlayerLevel()) {
					newExp = 0;	
					break;
				}
				newExp -= nextExp; 							// 经验减少
				newLevel ++; 								// 等级增加
				addLevel ++;								// 当前升级等级+1
				levels.add(newLevel);						// 新升级的列表
				nextExp = getUpgradeExp(job, newLevel);
			}
			
			// 升级总消耗经验
			battle.setExp(newExp); 			
			if(addLevel > 0) {
				int beforeLevel = battle.getLevel();
				battle.increaseLevel(addLevel);
				config = getUpgradeConfig(job, battle.getLevel());
				battle.setFlushable(Flushable.FLUSHABLE_LEVEL_UP);
				if(config != null) {
					battle.setExpMax(config.getExp());
				}
				PlayerLevelLogger.level(battle.getId(), Source.PLAYER_LEVEL_UP_EXP, beforeLevel, battle.getLevel(), new Date(), player, motion);
			}
		} catch (Exception e) {
			LOGGER.error("{}", e);
		}
		return levels;
	}
	
	/**
	 * 刷新角色的战斗属性
	 * 
	 * @param battle
	 */
	private void refreshPlayerBattle(PlayerBattle battle, UserDomain userDomain) {
		if(!battle.isFlushable()) {
			return;
		}
		
		boolean isFullHpMp = false;
		ChainLock chainLock = LockUtils.getLock(battle);
		UserAttach userAttach = getUserAttach(userDomain, battle);
		try {
			chainLock.lock();
			isFullHpMp = battle.isFullHPMPFlushable();
			if(!battle.isFlushable()) {
				return;
			}
			battle.setFlushable(Flushable.FLUSHABLE_NOT);
			this.refreshFightableProperties(battle, userAttach);		// HP / MP / SP / GAS
			if(isFullHpMp) {// 角色升级, HP/MP满
				battle.setHp(battle.getAttribute(HP_MAX));
				battle.setMp(battle.getAttribute(MP_MAX));
			}
		} finally {
			chainLock.unlock();
		}
		
		processFationOutExpiration(userDomain, userAttach.isFactionOutOfExpiration(), false);
	}

	/**
	 * 推送Buffer过期角色属性变化
	 * 
	 * @param userDomain		角色域模型
	 */
	public static void pushBufferAttributeChange2Area(UserDomain userDomain) {
		GameMap gameMap = userDomain.getGameMap();
		if(gameMap != null) {
			long playerId = userDomain.getPlayerId();
			List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
			Collection<Long> playerIdList = gameMap.getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER);
			UserPushHelper.pushAttribute2AreaMember(playerId, playerIdList, unitIds, AttributeRule.PLAYER_BUFFER_TIMEOUT_PARAMS);
		}
	}
	
	/**
	 * 计算帮派技能属性
	 * @param battle     角色战斗对象
	 * @param attach     战斗属性
	 */
	private void getUserAllianceSkillAttach(PlayerBattle battle,UserAttach attach){
		PlayerAlliance playerAlliance = allianceManager.getPlayerAlliance(battle);
		if(playerAlliance == null){
			return;
		}
		Fightable fightable = playerAlliance.getAttributes();
		if(fightable.isEmpty()){
			return;
		}
		Set<Entry<Object, Integer>> entrySet = fightable.entrySet();
		for (Iterator<Entry<Object, Integer>> it = entrySet.iterator(); it.hasNext();) {
			Entry<Object, Integer> entry = it.next();
			Object attribute = entry.getKey();
			Integer attrValue = entry.getValue();
			if(attribute == null || attrValue == null) {
				continue;
			}
			attach.getUserPetMerged().add(attribute, attrValue);
		}
	}
	
	
	/**
	 * 计算家将附身加成属性
	 * 
	 * @param battle     角色的ID
	 * @param attach     战斗属性
	 */
	private void getUserMergedAttach(PlayerBattle battle, UserAttach attach){
		Fightable fightable = this.petManager.getMergedAttribute(battle);
		if(fightable == null || fightable.isEmpty()){
			return;
		}
		
		Map<Object, Integer> attributes = fightable.getAttributes();
		Set<Entry<Object, Integer>> entrySet = attributes.entrySet();
		for (Iterator<Entry<Object, Integer>> it = entrySet.iterator(); it.hasNext();) {
			Entry<Object, Integer> entry = it.next();
			Object attribute = entry.getKey();
			Integer attrValue = entry.getValue();
			if(attribute == null || attrValue == null) {
				continue;
			}
			attach.getUserPetMerged().add(attribute, attrValue);
		}
	}
	
	
	/**
	 * 计算坐骑加成属性
	 * @param playerId    角色的ID
	 * @param attach      战斗属性
	 */
	private void getUserHorseAttach(PlayerBattle battle, UserAttach attach) {
		Horse horse = this.horseManager.getHorse(battle);
		if(horse == null) {
			return;
		}
		
		Map<Object, Integer> attributes = horse.getAttributes().getAttributes();
		if(attributes == null || attributes.isEmpty()) {
			return;
		}
		
		attach.setRiding(horse.isRiding());
		for (Entry<Object, Integer> entry : new HashMap<Object, Integer>(attributes).entrySet()) {
			Object attribute = entry.getKey();
			Integer attrValue = entry.getValue();
			if(attribute == null || attrValue == null) {
				continue;
			}

			int attr = Integer.valueOf(attribute.toString());
			if(attach.isRiding()) { //乘骑状态
				if(attr == AttributeKeys.MOVE_SPEED) {//如果是骑乘状态时且不在押镖状态时才会增加速度
					if(!escrotManager.isEscortStatus(battle)){
						attach.getHorsesable().add(attribute, attrValue);
						attach.getRidHorsesable().add(attribute, attrValue);
					}
				} else {
					attach.getHorsesable().add(attribute, attrValue);
					attach.getRidHorsesable().add(attribute, attrValue);
				}
			} else { //非乘骑状态. 只加给乘骑状态数值
				attach.getRidHorsesable().add(attribute, attrValue);
			}
		}
	}
	 
	/**
	 * 计算坐骑的战斗力
	 * 
	 * @param  fightable		附加属性
	 * @param  playerJob		角色的职业
	 * @return {@link Integer}	角色的战斗力
	 */
	public static int calcHorseCapacity(Fightable attach, Job playerJob) {
		switch (playerJob) {
			case TIANLONG:	
				return FormulaHelper.invoke(TIANLONG_HORSE_CAPACITY, 
						attach.getAttribute(PHYSICAL_ATTACK), 	attach.getAttribute(PHYSICAL_DEFENSE),
						attach.getAttribute(PHYSICAL_CRITICAL), attach.getAttribute(HIT),
						attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
						attach.getAttribute(THEURGY_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();	
			case TIANSHAN:	
				return FormulaHelper.invoke(TIANSHAN_HORSE_CAPACITY,  
						attach.getAttribute(PHYSICAL_ATTACK), 	attach.getAttribute(PHYSICAL_DEFENSE),
						attach.getAttribute(PHYSICAL_CRITICAL), attach.getAttribute(HIT),
						attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
						attach.getAttribute(THEURGY_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();
			case XINGXIU:	
				return FormulaHelper.invoke(XINGXIU_HORSE_CAPACITY,  
						attach.getAttribute(THEURGY_ATTACK), 	attach.getAttribute(THEURGY_DEFENSE),
						attach.getAttribute(THEURGY_CRITICAL), 	attach.getAttribute(HIT),
						attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
						attach.getAttribute(PHYSICAL_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();
			case XIAOYAO:	
				return FormulaHelper.invoke(XIAOYAO_HORSE_CAPACITY,  
						attach.getAttribute(THEURGY_ATTACK), 	attach.getAttribute(THEURGY_DEFENSE),
						attach.getAttribute(THEURGY_CRITICAL), 	attach.getAttribute(HIT),
						attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
						attach.getAttribute(PHYSICAL_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();
		}
		return 0;
	}
	
	/**
	 * 计算装备附加属性战斗力.
	 * 
	 * <pre>
	 * n1+n2+n3+n4+n5	天龙   n1:力量   n2：体质  n3：敏捷  n4：智力 n5:装备附加属性所获得的二级属性按照公式（701）计算出的结果	
	 * n1+n2+n3+n4+n5	天山   n1:力量   n2：体质  n3：敏捷  n4：智力 n5:装备附加属性所获得的二级属性按照公式（702）计算出的结果	
	 * n1+n2+n3+n4+n5	星宿   n1:精神   n2：体质  n3：敏捷  n4：智力 n5:装备附加属性所获得的二级属性按照公式（703）计算出的结果	
	 * n1+n2+n3+n4+n5	逍遥   n1:精神   n2：体质  n3：敏捷  n4：智力 n5:装备附加属性所获得的二级属性按照公式（704）计算出的结果
	 * </pre>
	 * 
	 * @param additions
	 *            附加属性集合
	 * @param playerJob
	 *            角色职业
	 * @return
	 */
	private static int calcEquipAdditionCapacity(Fightable additions, Job playerJob) {
		int strength = additions.get(AttributeKeys.STRENGTH);
		int dexerity = additions.get(AttributeKeys.DEXERITY);
		int intellect = additions.get(AttributeKeys.INTELLECT);
		int spirituality = additions.get(AttributeKeys.SPIRITUALITY);
		int constitution = additions.get(AttributeKeys.CONSTITUTION);
		int secondAttribute = calcCommonAttributeCapacity(additions, playerJob);
		switch (playerJob) {
			case TIANLONG:	
				return FormulaHelper.invoke(TIANLONG_EQUIP_ADDITION_CAPACITY, strength, constitution, dexerity, intellect, secondAttribute).intValue(); 
			case TIANSHAN:			
				return FormulaHelper.invoke(TIANSHAN_EQUIP_ADDITION_CAPACITY, strength, constitution, dexerity, intellect, secondAttribute).intValue(); 
			case XINGXIU:			
				return FormulaHelper.invoke(XINGXIU_EQUIP_ADDITION_CAPACITY, spirituality, constitution, dexerity, intellect, secondAttribute).intValue(); 
			case XIAOYAO:			
				return FormulaHelper.invoke(XIAOYAO_EQUIP_ADDITION_CAPACITY, spirituality, constitution, dexerity, intellect, secondAttribute).intValue(); 
		}
		return 0;
	}
	
	/**
	 * 计算角色的战斗力
	 * 
	 * @param  attach			附加属性
	 * @param  playerJob		角色的职业
	 * @return {@link Integer}	角色的战斗力
	 */
	public static int calcCommonAttributeCapacity(Fightable attach, Job playerJob) {
		switch (playerJob) {
			case TIANLONG:	
				return FormulaHelper.invoke(TIANLONG_FIGHT_CAPACITY, 
						attach.getAttribute(PHYSICAL_ATTACK), 	attach.getAttribute(PHYSICAL_DEFENSE),
						attach.getAttribute(PHYSICAL_CRITICAL), attach.getAttribute(HIT),
						attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
						attach.getAttribute(THEURGY_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();	
			case TIANSHAN:	
				return FormulaHelper.invoke(TIANSHAN_FIGHT_CAPACITY, 
						attach.getAttribute(PHYSICAL_ATTACK), 	attach.getAttribute(PHYSICAL_DEFENSE),
						attach.getAttribute(PHYSICAL_CRITICAL), attach.getAttribute(HIT),
						attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
						attach.getAttribute(THEURGY_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();
			case XINGXIU:	
				return FormulaHelper.invoke(XINGXIU_FIGHT_CAPACITY, 
						attach.getAttribute(THEURGY_ATTACK), 	attach.getAttribute(THEURGY_DEFENSE),
						attach.getAttribute(THEURGY_CRITICAL), attach.getAttribute(HIT),
						attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
						attach.getAttribute(PHYSICAL_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();
			case XIAOYAO:	
				return FormulaHelper.invoke(XIAOYAO_FIGHT_CAPACITY, 
						attach.getAttribute(THEURGY_ATTACK), 	attach.getAttribute(THEURGY_DEFENSE),
						attach.getAttribute(THEURGY_CRITICAL), attach.getAttribute(HIT),
						attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
						attach.getAttribute(PHYSICAL_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();
		}
		return 0;
	}

	/**
	 * 计算角色的战斗力
	 * 
	 * @param  attach			附加属性
	 * @param  playerJob		角色的职业
	 * @return {@link Integer}	角色的战斗力
	 */
	public static int calcCommonRatioCapacity(Fightable attach, Job playerJob) {
		switch (playerJob) {
		case TIANLONG:	
			return FormulaHelper.invoke(TIANLONG_RATIO_FIGHT_CAPACITY, 
					attach.getAttribute(PHYSICAL_ATTACK), 	attach.getAttribute(PHYSICAL_DEFENSE),
					attach.getAttribute(PHYSICAL_CRITICAL), attach.getAttribute(HIT),
					attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
					attach.getAttribute(THEURGY_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();	
		case TIANSHAN:	
			return FormulaHelper.invoke(TIANSHAN_RATIO_FIGHT_CAPACITY, 
					attach.getAttribute(PHYSICAL_ATTACK), 	attach.getAttribute(PHYSICAL_DEFENSE),
					attach.getAttribute(PHYSICAL_CRITICAL), attach.getAttribute(HIT),
					attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
					attach.getAttribute(THEURGY_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();
		case XINGXIU:	
			return FormulaHelper.invoke(XINGXIU_RATIO_FIGHT_CAPACITY, 
					attach.getAttribute(THEURGY_ATTACK), 	attach.getAttribute(THEURGY_DEFENSE),
					attach.getAttribute(THEURGY_CRITICAL), attach.getAttribute(HIT),
					attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
					attach.getAttribute(PHYSICAL_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();
		case XIAOYAO:	
			return FormulaHelper.invoke(XIAOYAO_RATIO_FIGHT_CAPACITY, 
					attach.getAttribute(THEURGY_ATTACK), 	attach.getAttribute(THEURGY_DEFENSE),
					attach.getAttribute(THEURGY_CRITICAL), attach.getAttribute(HIT),
					attach.getAttribute(DODGE),				attach.getAttribute(HP_MAX),
					attach.getAttribute(PHYSICAL_DEFENSE),	attach.getAttribute(MP_MAX)).intValue();
		}
		return 0;
	}
	
	/**
	 * 计算角色的战斗力
	 * 
	 * @param  battle			角色的战斗对象
	 * @param  playerJob		角色的职业
	 * @return {@link Integer}	角色的战斗力
	 */
	public static int calcDungeonAttributeCapacity(PlayerBattle battle, Job playerJob) {
		Fightable attach = new Fightable();
		attach.set(HIT, battle.getHit());
		attach.set(DODGE, battle.getDodge());
		attach.set(HP_MAX, battle.getAddHpMax());
		attach.set(MP_MAX, battle.getAddHpMax());	
		attach.set(THEURGY_ATTACK, battle.getTheurgyAttack());
		attach.set(THEURGY_DEFENSE, battle.getTheurgyDefense());
		attach.set(THEURGY_CRITICAL, battle.getTheurgyCritical());
		attach.set(PHYSICAL_ATTACK, battle.getPhysicalAttack());
		attach.set(PHYSICAL_DEFENSE, battle.getPhysicalDefense());
		attach.set(PHYSICAL_CRITICAL, battle.getPhysicalCritical());
		return calcCommonRatioCapacity(attach, playerJob);
	}
	
	
	
	/**
	 * 计算好友组队加成
	 * 
	 * @param playerId          角色Id
	 * @param attach            战斗属性
	 */
	private void getFriendlyAttach(long playerId, UserAttach attach) {
		Team team = teamManager.getPlayerTeam(playerId);
		if (team == null) {
			return;
		}
		
		int friendlyValue = 0;
		for (long memberId : team.getMembers()) {
			if (playerId == memberId) {
				continue;
			}
			
			int value = friendManager.getFriendlyValue(playerId, memberId);
			if (value > friendlyValue) {
				friendlyValue = value;
			}
		}
		
		if (friendlyValue <= 0) {
			return;
		}
		
		Fightable fightable = friendManager.getFriendAddedValue(friendlyValue);
		if (fightable.getAttributes().isEmpty()) {
			return;
		}
		attach.getFriendlyAdded().putAll(fightable.getAttributes());
	}
	
	
}
