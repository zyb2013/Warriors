package com.yayo.warriors.module.skill.facade.impl;
import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.skill.constant.SkillConstant.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.db.executor.DbService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.SkillConfig;
import com.yayo.warriors.basedb.model.SkillLearnConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.common.helper.MessagePushHelper;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.common.helper.VOFactory;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.logger.log.GoodsLogger;
import com.yayo.warriors.module.logger.log.SilverLogger;
import com.yayo.warriors.module.logger.model.LoggerGoods;
import com.yayo.warriors.module.logger.type.Source;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.skill.constant.SkillConstant;
import com.yayo.warriors.module.skill.entity.UserSkill;
import com.yayo.warriors.module.skill.facade.SkillFacade;
import com.yayo.warriors.module.skill.model.SkillVO;
import com.yayo.warriors.module.skill.rule.SkillRule;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.PlayerStatus;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.type.ElementType;

/**
 * 用户技能等级
 * 
 * @author Hyint
 */
@Component
public class SkillFacadeImpl implements SkillFacade {
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private DbService dbService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private ResourceService resourceService;
	
	/**
	 * 列出用户技能VO列表
	 * 
	 * @param  playerId						角色ID
	 * @return {@link Collection<SkillVO>}	技能VO列表	
	 */
	
	public List<SkillVO> listUserSkillVO(long playerId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return Collections.emptyList();
		}
		
		UserSkill userSkill = userDomain.getUserSkill();
		if(userSkill == null) {
			return Collections.emptyList();
		}

		List<SkillVO> skillVOList = new ArrayList<SkillVO>(0);
		if(userSkill != null) {
			skillVOList.addAll(userSkill.getSkillVOList(null));
		}
		return skillVOList;
	}

	/**
	 * 学习或者升级技能等级
	 * 
	 * @param  playerId						角色ID
	 * @param  userItemId					用户道具ID
	 * @param  skillId						技能ID
	 * @return {@link SkillConstant}		用户技能返回值
	 */
	
	public ResultObject<SkillVO> learnUserSkill(long playerId, long userItemId, int skillId) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		Player player = userDomain.getPlayer();
		if(player == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		}
		
		PlayerBattle playerBattle = userDomain.getBattle();
		if(playerBattle == null) {
			return ResultObject.ERROR(PLAYER_NOT_FOUND);
		} 

		PlayerStatus playerStatus = playerBattle.getPlayerStatus();
		if(playerStatus == null) {
			return ResultObject.ERROR(PLAYER_OFF_LINE);
		}
		
		UserSkill userSkill = userDomain.getUserSkill();
		if(userSkill == null) {
			return ResultObject.ERROR(SKILL_NOT_FOUND);
		} 
		
		Job job = playerBattle.getJob();
		SkillConfig skillConfig = resourceService.get(skillId, SkillConfig.class);
		if(skillConfig == null) {
			return ResultObject.ERROR(SKILL_NOT_FOUND);
		} else if(!skillConfig.canLearnSkill(job)) {
			return ResultObject.ERROR(SKILL_TYPE_INVALID);
		} else if(!skillConfig.isCanLearn()) {
			return ResultObject.ERROR(SKILL_TYPE_INVALID);
		}
		
		boolean activity = skillConfig.isActivity();
		int skillLevel = userSkill.getSkillLevel(skillId, activity);
		SkillLearnConfig skillLearn = skillConfig.getLearnSkill(skillLevel + 1);
		if(skillLearn == null) {
			return ResultObject.ERROR(SKILL_TYPE_INVALID);
		}
		
		if(!skillLearn.isNeedItem()) {
			return skillLevelup(userDomain, userSkill, skillConfig, skillLearn);
		} else {
			return learnNewSkill(userDomain, userSkill, skillConfig, skillLearn, userItemId);
		}
	}

	/**
	 * 学习新的技能
	 * 
	 * @param  player					角色对象
	 * @param  battle					角色战斗对象
	 * @param  userSkill				用户技能
	 * @param  skillConfig				基础技能
	 * @param  skillLearn				技能学习对象
	 * @param  userItemId				使用的用户道具ID
	 * @return {@link ResultObject}		用户技能模块返回值
	 */
	@SuppressWarnings("unchecked")
	private ResultObject<SkillVO> learnNewSkill(UserDomain userDomain, UserSkill userSkill, SkillConfig skillConfig, SkillLearnConfig skillLearn, long userItemId) {
		UserProps userProps = propsManager.getUserProps(userItemId);
		if(userProps == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} 

		int skillId = skillConfig.getId();
		int baseId = userProps.getBaseId();
		long playerId = userDomain.getPlayerId();
		int backpack = BackpackType.DEFAULT_BACKPACK;
		if(userProps.getPlayerId() != playerId) {
			return ResultObject.ERROR(BELONGS_INVALID);
		} else if(!userProps.validBackpack(backpack)) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(userProps.isOutOfExpiration()) {
			return ResultObject.ERROR(OUT_OF_EXPIRATION);
		} else if(userProps.isTrading()) {
			return ResultObject.ERROR(ITEM_CANNOT_USE);
		}
		
		PropsConfig propsConfig = userProps.getPropsConfig();
		if(propsConfig == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		}

		int itemId = skillLearn.getItemId();
		PropsConfig needProps = propsManager.getPropsConfig(itemId);
		if(needProps == null) {
			return ResultObject.ERROR(ITEM_NOT_FOUND);
		} else if(baseId != itemId ) {
			return ResultObject.ERROR(TYPE_INVALID);
		}
		
		int skillClassify = skillConfig.getClassify();
		if(!ArrayUtils.contains(SkillRule.SKILL_CLASSIFY_ARRAY, skillClassify)) {
			return ResultObject.ERROR(SKILL_TYPE_INVALID);
		} else if(!skillConfig.isCanLearn()) {
			return ResultObject.ERROR(SKILL_TYPE_INVALID);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if(skillLearn.getRestrict() > battle.getLevel()) {
			return ResultObject.ERROR(LEVEL_INVALID);
		}
		
		int addLevel = 1;
		int itemCount = 1;
		int costGas = skillLearn.getGas();
		int costSilver = skillLearn.getSilver();
		boolean isActivity = skillConfig.isActivity();
		ChainLock lock = LockUtils.getLock(player, battle, userSkill, player.getPackLock());
		try {
			lock.lock();
			if(userProps.getCount() <= 0) {
				return ResultObject.ERROR(ITEM_NOT_FOUND);
			} else if(userSkill.hasSkill(skillId, isActivity)) {
				return ResultObject.ERROR(PLAYER_LEARNED_SKILL);
			}
			
			if(player.getSilver() < costSilver) {
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			} else if(battle.getGas() < costGas) {
				return ResultObject.ERROR(GAS_NOT_ENOUGH);
			}
			
			battle.decreaseGas(costGas);
			player.decreaseSilver(costSilver);
			userProps.decreaseItemCount(itemCount);
			userSkill.addSkill(skillId, addLevel, isActivity);
			userSkill.updateUserSkillInfos(isActivity);
			battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			propsManager.removeUserPropsIfCountNotEnough(playerId, backpack, userProps);
		} finally {
			lock.unlock();
		}
		
		dbService.updateEntityIntime(userSkill);
		dbService.submitUpdate2Queue(player, userProps, userDomain.getBattle());
		
		LoggerGoods outcomeProps = LoggerGoods.outcomeProps(userItemId, baseId, 1);
		GoodsLogger.goodsLogger(player, Source.PLAYER_LEARN_SKILL, outcomeProps);
		if(costSilver != 0) {
			SilverLogger.outCome(Source.PLAYER_LEARN_SKILL, costSilver, player, outcomeProps);
		}
		this.pushLearnSkillResult2Client(userDomain, userProps, AttributeRule.LEARN_SKILL_ARRAY);
		return ResultObject.SUCCESS(userSkill.getSkillVO(skillId, isActivity));
	}
	
	/**
	 * 推送学习技能结果到客户端
	 * 
	 * @param  playerId		角色ID
	 * @param  userProps	用户道具对象
	 */
	private void pushLearnSkillResult2Client(UserDomain userDomain, UserProps userProps, Object...attributes) {
		Collection<Long> playerIds = new HashSet<Long>(2);
		playerIds.add(userDomain.getPlayerId());
		
		List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(userDomain.getPlayerId(), playerIds, unitIds, attributes);
		if(userProps != null) {
			MessagePushHelper.pushUserProps2Client(userDomain.getPlayerId(), BackpackType.DEFAULT_BACKPACK, false, userProps);
		}
		
		//推送给周围玩家属性变化
		if(userDomain.getGameMap() != null) {
			playerIds.addAll(userDomain.getGameMap().getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER));
			UserPushHelper.pushAttribute2AreaMember(userDomain.getPlayerId(), playerIds, unitIds, AttributeRule.AREA_MEMBER_VIEWS_PARAMS);
		}
	}
	
	/**
	 * 升级用户技能等级
	 * 
	 * @param  userDomain				用户域模型对象
	 * @param  userSkill				用户技能对象
	 * @param  skillConfig				基础技能对象
	 * @param  skillLearn				用户学习技能对象
	 * @return {@link ResultObject}		用户技能道具返回值
	 */
	@SuppressWarnings("unchecked")
	private ResultObject<SkillVO> skillLevelup(UserDomain userDomain, UserSkill userSkill, SkillConfig skillConfig, SkillLearnConfig skillLearn) {
		int skillId = skillLearn.getSkillId();
		boolean isActivity = skillConfig.isActivity();
		if(!userSkill.hasSkill(skillId, isActivity)) {
			return ResultObject.ERROR(MUST_ACTIVATE_SKILL);
		}

		int maxLevel = skillConfig.getMaxLevel();
		int skillLevel = userSkill.getSkillLevel(skillId, isActivity);
		if(skillLevel <= 0) {
			return ResultObject.ERROR(MUST_ACTIVATE_SKILL);
		} else if(skillLevel >= maxLevel) {
			return ResultObject.ERROR(MAX_SKILLLEVEL_INVALID);
		}
		
		Player player = userDomain.getPlayer();
		PlayerBattle battle = userDomain.getBattle();
		if(skillLearn.getRestrict() > battle.getLevel()) {
			return ResultObject.ERROR(LEVEL_INVALID);
		}
		
		int costGas = 0;
		int costSilver = 0;
		ChainLock lock = LockUtils.getLock(player, battle, userSkill);
		try {
			lock.lock();
			if(!userSkill.hasSkill(skillId, isActivity)) {
				return ResultObject.ERROR(MUST_ACTIVATE_SKILL);
			}
			
			skillLevel = userSkill.getSkillLevel(skillId, isActivity);
			if(skillLevel <= 0) {
				return ResultObject.ERROR(MUST_ACTIVATE_SKILL);
			} else if(skillLevel >= maxLevel) {
				return ResultObject.ERROR(MAX_SKILLLEVEL_INVALID);
			}
			
			skillLearn = skillConfig.getLearnSkill(skillLevel + 1);
			if(skillLearn == null) {
				return ResultObject.ERROR(SKILL_NOT_FOUND);
			}
			
			if(skillLearn.getRestrict() > battle.getLevel()) {
				return ResultObject.ERROR(LEVEL_INVALID);
			}
			
			costGas = skillLearn.getGas();
			costSilver = skillLearn.getSilver();
			if(player.getSilver() < costSilver) {
				return ResultObject.ERROR(SILVER_NOT_ENOUGH);
			} else if(battle.getGas() < costGas) {
				return ResultObject.ERROR(GAS_NOT_ENOUGH);
			}
			battle.decreaseGas(costGas);
			player.decreaseSilver(costSilver);
			userSkill.addSkill(skillId, 1, isActivity);
			userSkill.updateUserSkillInfos(isActivity);
			battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
		} finally {
			lock.unlock();
		}
		
		GoodsLogger.goodsLogger(player, Source.PLAYER_LEARN_SKILL);
		if(costSilver != 0) {
			SilverLogger.outCome(Source.PLAYER_LEARN_SKILL, costSilver, player);
		}
		dbService.submitUpdate2Queue(player, userSkill, userDomain.getBattle());
		this.pushLearnSkillResult2Client(userDomain, null, AttributeRule.LEARN_SKILL_ARRAY);
		return ResultObject.SUCCESS(userSkill.getSkillVO(skillId, isActivity));
	}
}
