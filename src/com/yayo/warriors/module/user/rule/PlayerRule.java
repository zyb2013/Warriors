package com.yayo.warriors.module.user.rule;

import static com.yayo.warriors.module.props.type.PropsChildType.*;
import static com.yayo.warriors.module.skill.type.SkillEffectType.*;
import static com.yayo.warriors.module.user.type.AttributeKeys.*;
import static com.yayo.warriors.type.FormulaKey.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.config.ServerConfig;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.adapter.SkillService;
import com.yayo.warriors.basedb.model.SkillConfig;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.common.helper.FormulaHelper;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.dungeon.entity.PlayerDungeon;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.meridian.entity.Meridian;
import com.yayo.warriors.module.mortal.entity.UserMortalBody;
import com.yayo.warriors.module.pet.entity.PetBattle;
import com.yayo.warriors.module.skill.entity.UserSkill;
import com.yayo.warriors.module.skill.type.Classify;
import com.yayo.warriors.module.task.entity.TaskComplete;
import com.yayo.warriors.module.title.entity.PlayerTitle;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.InitCreateInfo;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.module.user.type.PortableType;
import com.yayo.warriors.module.user.type.Sex;
import com.yayo.warriors.module.vip.entity.PlayerVip;
import com.yayo.warriors.type.FormulaKey;

/**
 * 角色规则对象
 * 
 * @author Hyint
 */
@Component
public class PlayerRule {
	/** 新建角色的等级 */
	public static final int INIT_DEFAULT_LEVEL = 1;
	/** 角色切换战斗模式等级限制 */
	public static final int CHANGE_MODE_MINLEVEL = 20;
	/** 角色固定的移动速度 */
	public static final int PERSON_FIXED_SPEED = 5000;
	/** 新建角色所在的地图ID */
	public static final int INIT_MAP_ID = 101;
	/** 新建角色所在X坐标 */
	public static final int INIT_POSITION_X = 62;
	/** 新建角色所在Y坐标 */
	public static final int INIT_POSITION_Y = 177;
	
	/** 初始化的银币 */
	public static final int INIT_SILVER = 0;
	/** 初始的角色HP */
	public static final int INIT_HP = 10000;
	/** 初始化的真气值 */
	public static final int INIT_GAS = 1000;
	/** 初始的角色MP */
	public static final int INIT_MP = 10000;
	/** 初始化的真气最大*/
	public static final int INIT_GAS_MAX = 10000;
	/** 一个账号可以创建的最大角色数 */
	public static final int MAX_CREATE_LIMIT = 1;
	
	/** 最大的背包页数 */
	public static final int MAX_BACKPACK_PAGE = 5;
	/** 一页的背包格子数 */
	public static final int PAGE_BACKPACK_SIZE = 30;
	/** 星级套装需要计算属性的件数 */
	public static final int STAR_SUIT_COUNT_LIMIT = 13;
	/** 奖励VIP道具的角色等级 */
	public static final int REWARD_VIP_PROPS_LEVEL = 16;
	/** 最大的背包的格子数 */
	public static final int MAX_BACKPACK_SIZE = PAGE_BACKPACK_SIZE * MAX_BACKPACK_PAGE;

	
	/** 初始化的家将,存放个数*/
	public static final int INIT_PET_SLOT_SZIE = 2;
	
	/** 初始化的家将历练值*/
	public static final int INIT_PET_EXPERIENCE = 10000;
	
	/** 最大的家将,存放个数*/
	public static final int MAX_PET_SLOT_SIZE = 8;
	
	/** 大于该等级使用原地复活,不需要消耗复活丹*/
	public static final int REVIVE_NEED_PROPS_LEVEL = 15;
	
	/** 角色原地复活时,需要消耗的复活丹数量*/
	public static final int REVIVE_USE_PROPS_COUNT = 1;
	
	/** 角色HP便携包cdId  单位:秒 */
	public static final int PLAYER_HP_PORTABLEBAG_CD_ID  = 107;
	
	/** 角色MP便携包cdId  单位:秒 */
	public static final int PLAYER_MP_PORTABLEBAG_CD_ID  = 108;
	
	/** 家将HP便携包cdId  单位:秒 */
	public static final int PET_HP_PORTABLEBAG_CD_ID  = 109;
	
	private static final ObjectReference<PlayerRule> REF = new ObjectReference<PlayerRule>();
	
	@Autowired
	private GameMapManager mapManager;
	
	/** 创建角色奖励的金币数量 */
	@Autowired(required=false)
	@Qualifier("create.reward.golden")
	private Integer createRewardGolden = 0;
	
	@Autowired
	private SkillService skillService;
	
	@PostConstruct
	protected void init() {
		REF.set(this);
	}
	
	public static PlayerRule getInstance() {
		return REF.get();
	}
	
	/**
	 * 角色的最大等级
	 * 
	 * @return {@link Integer}	返回角色的最大等级
	 */
	public static int getMaxPlayerLevel() {
		return FormulaHelper.invoke(FormulaKey.MAX_PLAYER_LEVEL_LIMIT).intValue();
	}
	
	/**
	 * 创建角色对象
	 * 
	 * @param  userName				帐号名
	 * @param  playerName			角色名
	 * @param  camp					角色阵营
	 * @param  sex					角色的性别
	 * @param  icon					角色的头像
	 * @return {@link Player}		角色对象
	 */
	private static Player createPlayer(String userName, String password, String playerName, Sex sex, int icon){
		Player player = Player.valueOf(userName, playerName, sex, icon);
		player.setPassword(password);
		player.setSilver(INIT_SILVER);
		player.setMaxBackSize(PAGE_BACKPACK_SIZE);          // 创建角色默认一页背包, 超平修改...
		player.setMaxStoreSize(PAGE_BACKPACK_SIZE);
		player.setMaxPetSlotSize(INIT_PET_SLOT_SZIE);
		player.setServerId(ServerConfig.getServerId());
		player.setPetexperience(INIT_PET_EXPERIENCE);
		player.setGolden(getInstance().createRewardGolden);
		return player;
	}
	
	/**
	 * 构建创建信息
	 * 
	 * @param  userName					用户名
	 * @param  password					密码
	 * @param  playerName				角色名
	 * @param  sex						性别
	 * @param  icon						头像
	 * @param  playerJob				角色职业
	 * @return {@link InitCreateInfo}	初始化信息
	 */
	public static InitCreateInfo initCreateInfo(String userName, String password, String playerName, Sex sex, int icon, Job playerJob) {
		InitCreateInfo createInfo = new InitCreateInfo();
		createInfo.setMeridian(new Meridian());
		createInfo.setUserSkill(newUserSkill(playerJob));
		createInfo.setPlayerVip(new PlayerVip());
		createInfo.setUserBuffer(new UserBuffer());
		createInfo.setCoolTime(new UserCoolTime());
		createInfo.setPlayerTitle(new PlayerTitle());
		createInfo.setTaskComplete(new TaskComplete());
		createInfo.setPlayerMotion(createPlayerMotion());
		createInfo.setPlayerDungeon(new PlayerDungeon());
		createInfo.setUserMortalBody(new UserMortalBody());
		createInfo.setBattle(createPlayerBattle(playerJob));
		createInfo.setPlayer(createPlayer(userName, password, playerName, sex, icon));
		return createInfo;
	}
	
	/**
	 * 获得用户技能
	 * 
	 * @return {@link UserSkill}	用户技能对象
	 */
	private static UserSkill newUserSkill(Job job) {
		UserSkill userSkill = new UserSkill();
		refreshInnateSkillInfo(job, userSkill);
		return userSkill;
	}
	
	/**
	 * 刷新角色的先天技能
	 * 
	 * @param  playerJob		角色职业
	 * @param  userSkill		用户技能对象
	 * @return {@link Boolean}	是否需要更新到数据库中
	 */
	public static boolean refreshInnateSkillInfo(Job playerJob, UserSkill userSkill) {
		if(userSkill == null) {
			return false;
		}
		
		int classify = Classify.NORMAL_SKILL.ordinal();
		List<SkillConfig> skillConfigs = getInstance().skillService.getSkillByClassify(classify);
		if(skillConfigs == null || skillConfigs.isEmpty()) {
			return false;
		}
		
		for (SkillConfig skill : skillConfigs) {
			if(skill.getSkillLearns().isEmpty()) {
				continue;
			}
			
			int job = skill.getJob();
			if(job != Job.COMMON.ordinal() && job != playerJob.ordinal()) {
				continue;
			}
			
			int skillId = skill.getId();
			boolean isActive = skill.isCanLearn();
			if(!userSkill.hasSkill(skillId, isActive)) {
				userSkill.addSkill(skillId, 1, isActive);
			}
		}
		
		userSkill.updateUserSkillInfos(true);
		userSkill.updateUserSkillInfos(false);
		return true;
	}
	/**
	 * 创建角色移动对象
	 * 
	 * @param  player				角色对象
	 * @return {@link PlayerMotion}	角色移动对象
	 */
	private static PlayerMotion createPlayerMotion() {
		PlayerMotion playerMotion = new PlayerMotion();
		playerMotion.setMapId(INIT_MAP_ID);
		GameMap gameMap = getInstance().mapManager.getGameMapById(INIT_MAP_ID, 0);
		Point point = null;
		if(gameMap != null){
			point = gameMap.getRandomCanStandPoint(INIT_POSITION_X, INIT_POSITION_Y, 3);
		}
		
		if(point != null){
			playerMotion.setX(point.x);
			playerMotion.setY(point.y);
		}else{
			playerMotion.setX(INIT_POSITION_X);
			playerMotion.setX(INIT_POSITION_Y);
		}
		
		return playerMotion;
	}
	
	/**
	 * 创建角色战斗对象
	 * 
	 * @param  playerJob			角色职业
	 * @return {@link PlayerBattle}	角色移动对象
	 */
	private static PlayerBattle createPlayerBattle(Job playerJob) {
		PlayerBattle playerBattle = PlayerBattle.valueOf();
		playerBattle.setJob(playerJob);
		playerBattle.setLevel(INIT_DEFAULT_LEVEL);
		playerBattle.setAttribute(AttributeKeys.GAS_MAX, INIT_GAS_MAX);
		playerBattle.setAttribute(AttributeKeys.HP_MAX, INIT_HP);
		playerBattle.setAttribute(AttributeKeys.MP_MAX, INIT_MP);
		playerBattle.setAttribute(AttributeKeys.HP, INIT_HP);
		playerBattle.setAttribute(AttributeKeys.MP, INIT_MP);
		playerBattle.setAttribute(AttributeKeys.GAS, INIT_GAS);
		return playerBattle;
	}
	
	/**
	 * 镶嵌的道具信息
	 * 
	 * @param childType
	 * @param attrValue
	 * @param attacher
	 */
	public static void addHoleItem2Attach(int childType, int attrValue, Fightable attacher) {
		switch (childType) {
			case HIT_ITEM_TYPE: 				attacher.add(HIT, attrValue);				break;
			case DODGE_ITEM_TYPE: 				attacher.add(DODGE, attrValue);				break;
			case HPMAX_ITEM_TYPE: 				attacher.add(HP_MAX, attrValue);			break;
			case MPMAX_ITEM_TYPE: 				attacher.add(MP_MAX, attrValue);			break;
			case THEURGY_ATTACK_ITEM_TYPE:	 	attacher.add(THEURGY_ATTACK, attrValue);	break;
			case PHYSICAL_ATTACK_ITEM_TYPE: 	attacher.add(PHYSICAL_ATTACK, attrValue);	break;
			case THEURGY_DEFENSE_ITEM_TYPE: 	attacher.add(THEURGY_DEFENSE, attrValue);	break;
			case PHYSICAL_DEFENSE_ITEM_TYPE: 	attacher.add(PHYSICAL_DEFENSE, attrValue);	break;
			case THEURGY_CRITICAL_ITEM_TYPE: 	attacher.add(THEURGY_CRITICAL, attrValue);	break;
			case PHYSICAL_CRITICAL_ITEM_TYPE: 	attacher.add(PHYSICAL_CRITICAL, attrValue);	break;
			
//			case HIT_ITEM_TYPE: 				attacher.add(HIT_RATIO, attrValue);					break;
//			case DODGE_ITEM_TYPE: 				attacher.add(DODGE_RATIO, attrValue);				break;
//			case HPMAX_ITEM_TYPE: 				attacher.add(HPMAX_RATIO, attrValue);				break;
//			case MPMAX_ITEM_TYPE: 				attacher.add(MPMAX_RATIO, attrValue);				break;
//			case THEURGY_ATTACK_ITEM_TYPE:	 	attacher.add(THEURGY_ATTACK_RATIO, attrValue);		break;
//			case PHYSICAL_ATTACK_ITEM_TYPE: 	attacher.add(PHYSICAL_ATTACK_RATIO, attrValue);		break;
//			case THEURGY_DEFENSE_ITEM_TYPE: 	attacher.add(THEURGY_DEFENSE_RATIO, attrValue);		break;
//			case PHYSICAL_DEFENSE_ITEM_TYPE: 	attacher.add(PHYSICAL_DEFENSE_RATIO, attrValue);	break;
//			case THEURGY_CRITICAL_ITEM_TYPE: 	attacher.add(THEURGY_CRITICAL_RATIO, attrValue);	break;
//			case PHYSICAL_CRITICAL_ITEM_TYPE: 	attacher.add(PHYSICAL_CRITICAL_RATIO, attrValue);	break;
		}
	}
	
	/**
	 * 计算BUFF在所有处理结束后的属性计算(虚弱BUFF处理)
	 * 
	 * @param fightable		战斗属性
	 * @param attacher		附加属性
	 */
	public static void processAfterBufferAttach(Fightable fightable, Fightable attacher) {
		Set<Entry<Object, Integer>> entrySet = fightable.entrySet();
		for (Iterator<Entry<Object, Integer>> it = entrySet.iterator(); it.hasNext();) {
			Entry<Object, Integer> entry = it.next();
			int effectType = (Integer) entry.getKey();
			int effectValue = (Integer) entry.getValue();
			if(effectType == WEAKNESS_EFFECT.getCode()) {
				Integer moveSpeed = attacher.getAttribute(MOVE_SPEED);
				Integer theurgyDefense = attacher.getAttribute(THEURGY_DEFENSE);
				Integer physicalDefense = attacher.getAttribute(PHYSICAL_DEFENSE);
				double rate = Tools.divideAndRoundDown(effectValue, AttributeKeys.RATE_BASE, 3);
				attacher.set(MOVE_SPEED, (int) Math.max(0, moveSpeed - (moveSpeed * rate)));
				attacher.set(THEURGY_DEFENSE, (int) Math.max(0, theurgyDefense - (theurgyDefense * rate)));
				attacher.set(PHYSICAL_DEFENSE, (int) Math.max(0, physicalDefense - (physicalDefense * rate)));
			}
		}
	}

	/**
	 * 计算角色装备镶嵌宝石属性值
	 * 
	 * @param effectType		属性类型
	 * @param attrValue			属性值
	 * @param attacher			属性集合
	 */
	public static void processEquipHolesAttach(Fightable fightable, Fightable attacher) {
		for (Entry<Object, Integer> entry : fightable.entrySet()) {
			int attribute = (Integer) entry.getKey();
			int attrValue = (Integer) entry.getValue();
//			switch (attribute) {
//				case HIT_RATIO:					attachAttribute = HIT;				break;
//				case DODGE_RATIO:				attachAttribute = DODGE;			break;
//				case HPMAX_RATIO:				attachAttribute = HP_MAX;			break;
//				case MPMAX_RATIO:				attachAttribute = MP_MAX;			break;
//				case THEURGY_ATTACK_RATIO:		attachAttribute = THEURGY_ATTACK;	break;
//				case PHYSICAL_ATTACK_RATIO:		attachAttribute = PHYSICAL_ATTACK;	break;
//				case THEURGY_DEFENSE_RATIO:		attachAttribute = THEURGY_DEFENSE;	break;
//				case PHYSICAL_DEFENSE_RATIO:	attachAttribute = PHYSICAL_DEFENSE;	break;
//				case PHYSICAL_CRITICAL_RATIO:	attachAttribute = PHYSICAL_CRITICAL;break;
//				case THEURGY_CRITICAL_RATIO:	attachAttribute = THEURGY_CRITICAL;	break;
//			}
			
			if(attribute != 0 && attrValue != 0) {
				attacher.add(attribute, attrValue);
			}
		}			
	}
	
	/**
	 * 获得
	 * @param playerJob
	 * @param isPhysical
	 * @return
	 */
	public static int getDefenseFormulaId(Job playerJob, boolean isPhysical) {
		switch (playerJob) {
			case XINGXIU: 	return isPhysical ? CUIYAN_PHYSICAL_DEFENSE : CUIYAN_THEURGY_DEFENSE;
			case TIANSHAN: 	return isPhysical ? TANGMEN_PHYSICAL_DEFENSE : TANGMEN_THEURGY_DEFENSE;
			case XIAOYAO: 	return isPhysical ? GAIBANG_PHYSICAL_DEFENSE : GAIBANG_THEURGY_DEFENSE;
			case TIANLONG: 	return isPhysical ? MINGJIAO_PHYSICAL_DEFENSE : MINGJIAO_THEURGY_DEFENSE;
		}
		return -1;
	}
	
	/**
	 * 判断是否使用便携包
	 * @param battle
	 * @param petBattle
	 * @return
	 */
	public static List<PortableType> usePortableBag(PlayerBattle battle, PetBattle petBattle){
		List<PortableType> typs = new ArrayList<PortableType>();
		if(battle != null){
			int hp = battle.getAttribute(AttributeKeys.HP);
			int hpMax = battle.getAttribute(AttributeKeys.HP_MAX);
			double ratio = hp / (double)hpMax;
			if(ratio > 0 && ratio < 0.5 ){
				typs.add(PortableType.HPBAG);
			}
			
			int mp = battle.getAttribute(AttributeKeys.MP);
			int mpMax = battle.getAttribute(AttributeKeys.MP_MAX);
			ratio = mp / (double)mpMax;
			if(ratio > 0 && ratio < 0.5 ){
				typs.add(PortableType.MPBAG);
			}
		}
		
		if(petBattle != null){
			int hp = petBattle.getAttribute(AttributeKeys.HP);
			int hpMax = petBattle.getAttribute(AttributeKeys.HP_MAX);
			double ratio = hp / (double)hpMax;
			if(ratio > 0 && ratio < 0.5 ){
				typs.add(PortableType.PET_HPBAG);
			}
		}
		
		return typs;
	}
}
