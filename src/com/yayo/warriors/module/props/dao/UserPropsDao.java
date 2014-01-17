package com.yayo.warriors.module.props.dao;

import java.util.Collection;
import java.util.List;

import com.yayo.common.db.dao.CommonDao;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;

/**
 * 用户道具DAO对象
 * 
 * @author Hyint
 */
public interface UserPropsDao extends CommonDao {

	/**
	 * 获得道具ID列表
	 * 
	 * @param  playerId		角色ID
	 * @param  backpack		背包号
	 * @return {@link List}	用户装备ID列表
	 */
	List<Long> getUserPropsIdList(long playerId, int backpack);
	
	/**
	 * 创建用户道具
	 * 
	 * @param userProps			用户装备
	 */
	void createUserProps(UserProps userProps);
	
	/**
	 * 创建用户道具列表
	 * 
	 * @param userProps			用户装备列表
	 */
	void createUserProps(Collection<UserProps> userProps);
	
	/**
	 * 创建用户装备
	 * 
	 * @param userEquips		用户装备数组
	 */
	void createUserEquip(UserEquip...userEquips);

	/**
	 * 创建用户装备列表
	 * 
	 * @param userEquips		用户装备列表
	 */
	void createUserEquip(Collection<UserEquip> userEquips);

	/**
	 * 拆分用户道具
	 * 
	 * @param createProps		新创建的用户道具
	 * @param updateProps		更新的用户道具
	 */
	void spliteUserProps(UserProps createProps, UserProps updateProps);
	
	/**
	 * 创建用户装备和道具
	 * 
	 * @param  userEquips		用户装备对象列表
	 * @param  propsList		用户道具对象列表
	 */
	void createUserEquipAndProps(Collection<UserEquip> userEquips, Collection<UserProps> propsList);
	
	/**
	 * 更新用户道具信息
	 * 
	 * @param newUserPropList	新创建的用户道具列表
	 * @param updateUserProps	更新的用户道具列表
	 */
	void updateUserProps(Collection<UserProps> newUserPropList, Collection<UserProps> updateUserProps);


}
