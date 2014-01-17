package com.yayo.warriors.module.chat.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.basedb.model.SkillConfig;
import com.yayo.warriors.common.helper.UserPushHelper;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.skill.entity.UserSkill;
import com.yayo.warriors.module.skill.model.SkillVO;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.rule.AttributeRule;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.skill.SkillCmd;
import com.yayo.warriors.type.ElementType;

/**
 * 增加用户技能命令解析器
 * 
 * @author Hyint
 */
@Component
public class UserSkillParser extends AbstractGMCommandParser {
	
	@Autowired
	private Pusher pusher;
	@Autowired
	private ResourceService resourceService;
	
	
	protected String getCommand() {
		return GmType.SKILL;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		Integer skillId = Integer.valueOf(elements[2]);
		Integer skillLevel = Integer.valueOf(elements[3]);
		SkillConfig skillConfig = resourceService.get(skillId, SkillConfig.class);
		if(skillConfig == null || skillLevel == null || skillLevel <= 0) {
			return false;
		}
		
		int skillJob = skillConfig.getJob();
		PlayerBattle battle = userDomain.getBattle();
		if(skillJob != Job.COMMON.ordinal() && skillJob != battle.getJob().ordinal()) {
			return false;
		}
		
		if(skillConfig.getSkillLearns().isEmpty()) {
			return false;
		}
		
		skillLevel = Math.abs(skillLevel);
		int maxLevel = skillConfig.getMaxLevel();
		boolean isActive = skillConfig.isActivity();
		UserSkill userSkill = userDomain.getUserSkill();
		ChainLock lock = LockUtils.getLock(userSkill);
		try {
			lock.lock();
			int addLevel = Math.max(0, maxLevel - skillLevel);
			if(addLevel <= 0) {
				return false;
			}
			
			userSkill.addSkill(skillId, addLevel, isActive);
			userSkill.updateUserSkillInfos(isActive);
			dbService.submitUpdate2Queue(userSkill);
		} finally {
			lock.unlock();
		}
		
		this.pushSkillVO2Client(userSkill);
		this.pushLearnSkillResult2Client(userDomain, AttributeRule.LEARN_SKILL_ARRAY);
		return true;
	}

	/**
	 * 推送学习技能结果到客户端
	 * 
	 * @param  playerId		角色ID
	 * @param  userProps	用户道具对象
	 */
	private void pushLearnSkillResult2Client(UserDomain userDomain, Object...attributes) {
		Collection<Long> playerIds = new HashSet<Long>(2);
		playerIds.add(userDomain.getPlayerId());
		List<UnitId> unitIds = Arrays.asList(userDomain.getUnitId());
		UserPushHelper.pushAttribute2AreaMember(userDomain.getPlayerId(), playerIds, unitIds, attributes);
		if(userDomain.getGameMap() != null) {	//推送给周围玩家属性变化
			playerIds.addAll(userDomain.getGameMap().getCanViewsSpireIdCollection(userDomain, ElementType.PLAYER));
			UserPushHelper.pushAttribute2AreaMember(userDomain.getPlayerId(), playerIds, unitIds, AttributeRule.AREA_MEMBER_VIEWS_PARAMS);
		}
	}

	
	/**
	 * 推送技能到客户端
	 * 
	 * @param userSkill 用户技能对象
	 */
	private void pushSkillVO2Client(UserSkill userSkill) {
		long playerId = userSkill.getId();
		List<SkillVO> skillVOS = userSkill.getSkillVOList(null);
		List<SkillVO> skillVOList = new ArrayList<SkillVO>(skillVOS);
		long[] coolTimeArray = voFactory.getCdTime(playerId, skillVOList);
		Map<String, Object> resultMap = new HashMap<String, Object>(2);
		resultMap.put(ResponseKey.COOL_TIME, coolTimeArray);
		resultMap.put(ResponseKey.SKILL_VO, skillVOList.toArray());
		pusher.pushMessage(playerId, Response.defaultResponse(Module.SKILL, SkillCmd.QUERY_SKILL, resultMap));
		
	}
}
