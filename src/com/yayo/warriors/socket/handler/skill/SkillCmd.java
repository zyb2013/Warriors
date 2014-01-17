package com.yayo.warriors.socket.handler.skill;

import java.util.Map;

import com.yayo.warriors.module.skill.constant.SkillConstant;
import com.yayo.warriors.module.skill.model.SkillVO;



public interface SkillCmd {
	
	int QUERY_SKILL = 1;
	int SKILL_LEVELUP = 2;
	int PUSH_MEMBER_SKILLCD_2_CLIENT = 100;
	int PUSH_PET_SKILLCD_2_CLIENT = 101;
}
