package com.yayo.warriors.module.title.manager;

import java.util.Collection;

import com.yayo.warriors.basedb.model.TitleDictionary;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.title.entity.PlayerTitle;

/**
 * 称号Manager接口
 * 
 * @author Hyint
 */
public interface TitleManager extends DataRemoveListener {

	/**
	 * 称号基础数据
	 * 
	 * @param  titleId				称号ID
	 * @return {@link TitleDictionary}	称号基础对象
	 */
	TitleDictionary getTitleConfig(int titleId);
	
	/**
	 * 列出称号列表
	 * 
	 * @return {@link Collection}	称号配置列表
	 */
	Collection<TitleDictionary> listAllTitleConfig();
	
	/**
	 * 获得用户的实体
	 * 
	 * @param  playerId				角色ID
	 * @return {@link PlayerTitle}	称号实体
	 */
	PlayerTitle getUserTitle(long playerId);
}
