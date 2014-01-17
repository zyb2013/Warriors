package com.yayo.warriors.module.props.parser;

import com.yayo.warriors.basedb.model.CoolTimeConfig;
import com.yayo.warriors.module.cooltime.entity.UserCoolTime;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.user.model.UserDomain;
 

/**
 * 道具效果处理器
 * 
 * @author Hyint
 */
public interface PropsParser {

	/**
	 * 道具效果作用
	 * 
	 * @param  userDomain 				道具作用的角色(主角)
	 * @param  userCoolTime				用户CD对象
	 * @param  coolTime					冷却时间基础对象
	 * @param  userProps 				使用的用户道具
	 * @param  count 					使用的用户道具数量
	 * @return {@link Integer}			返回值对象
	 */
	int effect(UserDomain userDomain, UserCoolTime userCoolTime, CoolTimeConfig coolTime, UserProps userProps, int count);
}
