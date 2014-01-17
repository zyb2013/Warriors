package com.yayo.warriors.module.props.parser.impl;

import static com.yayo.warriors.constant.CommonConstant.*;
import static com.yayo.warriors.module.skill.constant.SkillConstant.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.basedb.model.CoolTimeConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.SkillConfig;
import com.yayo.warriors.basedb.model.SkillLearnConfig;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.constant.PropsConstant;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.parser.AbstractEffectParser;
import com.yayo.warriors.module.props.type.PropsChildType;
import com.yayo.warriors.module.props.type.PropsType;
import com.yayo.warriors.module.skill.constant.SkillConstant;
import com.yayo.warriors.module.skill.entity.UserSkill;
import com.yayo.warriors.module.skill.facade.SkillFacade;
import com.yayo.warriors.module.skill.model.SkillVO;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.skill.SkillCmd;
import com.yayo.warriors.type.IndexName;

/**
 * 其它特殊使用的道具的解析器
 * @author jonsai
 *
 */
@Component
public class SkillPropsParser extends AbstractEffectParser {
	@Autowired
	private SkillFacade skillFacade;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private Pusher pusher;
	
	
	protected int getType() {
		return PropsType.SKILL_PROPS_TYPE;
	}

	
	public int effect(UserDomain userDomain, UserCoolTime userCoolTime, CoolTimeConfig coolTime, UserProps userProps, int count) {
		if(!userProps.validBackpack(BackpackType.DEFAULT_BACKPACK)) {
			return NOT_IN_BACKPACK;
		}
		
		PropsConfig propsConfig = userProps.getPropsConfig();
		int childType = propsConfig.getChildType();
		switch (childType) {
			case PropsChildType.USER_SKILL_BOOK_TYPE: 	return learnUserSkill(userDomain, userCoolTime, coolTime, userProps, count, propsConfig);
			default:									return FAILURE;
	    }
	}
	
	/**
	 * 学习角色技能
	 * @param userDomain
	 * @param userProps
	 * @param count
	 * @param propsConfig
	 * @return	Integer			{@link PropsConstant}
	 */
	private int learnUserSkill(UserDomain userDomain, UserCoolTime userCoolTime, CoolTimeConfig coolTime, UserProps userProps, int count, PropsConfig propsConfig){
		long playerId = userDomain.getPlayerId();
		SkillLearnConfig skillLearnConfig = resourceService.getByUnique(IndexName.SKILL_LEARN_ITEMID, SkillLearnConfig.class, userProps.getBaseId());
		if(skillLearnConfig == null){
			return SKILL_TYPE_INVALID;
		}
		
		int skillId = skillLearnConfig.getSkillId();
		UserSkill userSkill = userDomain.getUserSkill();
		if(userSkill == null) {
			return SKILL_NOT_FOUND;
		}
		
		SkillConfig skillConfig = resourceService.get(skillId, SkillConfig.class);
		if(skillConfig == null){
			return SKILL_NOT_FOUND;
		} else if( userSkill.hasSkill( skillId, skillConfig.isActivity() ) ){
			return SkillConstant.PLAYER_LEARNED_SKILL;
		}
		
		ResultObject<SkillVO> resultObject = skillFacade.learnUserSkill(playerId, userProps.getId(), skillId );
		int result = resultObject.getResult();
		if(result == PropsConstant.SUCCESS) {
			Map<String, Object> resultMap = new HashMap<String, Object>(2);
			resultMap.put(ResponseKey.RESULT, result);
			SkillVO skillVO = resultObject.getValue();
			if(skillVO != null) {
				resultMap.put(ResponseKey.SKILL_VO, skillVO);
			}
			Response response = Response.defaultResponse(Module.SKILL, SkillCmd.SKILL_LEVELUP, resultMap);
			pusher.pushMessage(playerId, response);
		}
		return result;
	}
}
