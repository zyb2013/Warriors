package com.yayo.warriors.module.dungeon.storyverify;

import com.yayo.warriors.module.dungeon.model.StoryVerify;
import com.yayo.warriors.module.dungeon.types.StoryVerifType;
import com.yayo.warriors.module.user.model.UserDomain;

/**
 * 剧情副本验证处理类
 * @author liuyuhua
 */
public interface VerifyHandler {

	/**
	 * 获取类型
	 * @return {@link StoryVerifType}
	 */
	public int getType();
	
	/**
	 * 验证 剧情副本是否开放状态
	 * @param userDomain        玩家的域对象
	 * @param storyVerify       验证实体
	 * @return {@link Boolean} true 通过  false 反之
	 */
	public boolean verify(UserDomain userDomain,StoryVerify storyVerify);
	
	/**
	 * 注册验证器
	 */
	public void registerVerify();
}
