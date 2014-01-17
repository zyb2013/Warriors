package com.yayo.warriors.module.props.manager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.EquipRankConfig;
import com.yayo.warriors.basedb.model.EquipStarConfig;
import com.yayo.warriors.basedb.model.PropsArtificeConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.PropsSynthConfig;
import com.yayo.warriors.basedb.model.WashRuleConfig;
import com.yayo.warriors.module.pack.type.BackpackType;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.model.CreateResult;
import com.yayo.warriors.module.user.entity.Player;

/**
 * 用户道具Manager
 * 
 * @author Hyint
 */
public interface PropsManager  {
	
	/**
	 * 创建角色背包(只是缓存上处理,只在创建角色时调用)
	 * @param player
	 */
	void createPlayerPack(Player player);
	
	/**
	 * 查询基础道具对象
	 * 
	 * @param  baseId					基础道具ID
	 * @return {@link PropsConfig}		基础道具对象
	 */
	PropsConfig getPropsConfig(int baseId);
	
	/**
	 * 查询用户道具对象
	 * 
	 * @param  userPropsId				用户道具ID
	 * @return {@link UserProps}		用户道具对象
	 */
	UserProps getUserProps(long userPropsId);
	
	/**
	 * 用户装备对象
	 * 
	 * @param  userEquipId			用户装备ID
	 * @return {@link UserEquip}	用户装备对象
	 */
	UserEquip getUserEquip(long userEquipId);
	
	/**
	 * 查询基础装备对象
	 * 
	 * @param  equipId				基础装备ID
	 * @return {@link EquipConfig}	基础装备对象
	 */
	EquipConfig getEquipConfig(int equipId);
	
	/**
	 * 获得装备升星配置
	 * 
	 * @param 	star					装备的星级
	 * @return {@link EquipStarConfig}	装备星级配置
	 */
	EquipStarConfig getEquipStarConfig(int star);

	/**
	 * 获得装备升阶配置信息
	 * 
	 * @param  equipId					装备ID
	 * @return {@link EquipRankConfig}	装备升阶配置信息
	 */
	EquipRankConfig getEquipRankConfig(int equipId);
	
	/**
	 * 获得道具炼化配置对象
	 * 
	 * @param  propsId						道具ID
	 * @return {@link PropsArtificeConfig}	道具炼化对象
	 */
	PropsArtificeConfig getPropsArtifice(int propsId);
	
	/**
	 * 查询附加属性规则类
	 * 
	 * @param  additionNum					附加属性条数
	 * @return {@link WashRuleConfig}		附加属性规则类
	 */
	WashRuleConfig getWashRuleConfig(int additionNum);
	
	/**
	 * 获得道具合成配置信息
	 * 
	 * @param  propsId						基础道具ID
	 * @return {@link PropsSynthConfig}		道具合成配置信息
	 */
	PropsSynthConfig getPropsSynthConfig(int propsId);
	
	/** 
	 * 查询背包的大小 
	 * 
	 * @param  playerId					角色ID
	 * @param  backpack					背包号
	 * @return {@link Integer}			背包号
	 */
	int getBackpackSize(long playerId, int backpack);
	
	/**
	 * 增加用户道具到列表缓存
	 * 
	 * @param playerId					角色ID
	 * @param backpacks					背包号数组
	 */
	void put2UserPropsIdsList(long playerId, int backpack, Collection<UserProps> userPropsList);
	
	/**
	 * 增加用户道具到列表缓存
	 * 
	 * @param playerId					角色ID
	 * @param backpacks					背包号数组
	 */
	void put2UserPropsIdsList(long playerId, int backpack, UserProps... userPropsList);
	
	/**
	 * 道具变背包
	 * @param playerId
	 * @param sourceBackpack
	 * @param targetBackpack
	 * @param userPropsList
	 */
	void changeUserPropsBackpack(long playerId, int sourceBackpack,  int targetBackpack, Collection<UserProps> userPropsList);
	
	/**
	 * 道具变背包
	 * @param playerId
	 * @param sourceBackpack
	 * @param targetBackpack
	 * @param userPropsList
	 */
	void changeUserPropsBackpack(long playerId, int sourceBackpack,  int targetBackpack, UserProps... userPropsList);
	
	/**
	 * 移除道具
	 * @param playerId
	 * @param backpack
	 * @param userPropsList
	 */
	void removeFromUserPropsIdsList(long playerId, int backpack, Collection<UserProps> userPropsList);
	
	/**
	 * 移除道具道具道具数量是否<=0
	 * @param playerId
	 * @param backpack
	 * @param userPropsList
	 */
	void removeUserPropsIfCountNotEnough(long playerId, int backpack, Collection<UserProps> userPropsList);
	
	/**
	 * 移除道具道具道具数量是否<=0
	 * @param playerId
	 * @param backpack
	 * @param userPropsList
	 */
	void removeUserPropsIfCountNotEnough(long playerId, int backpack, UserProps... userPropsList);
	
	/**
	 * 移除道具
	 * @param playerId
	 * @param backpack
	 * @param userPropsList
	 */
	void removeFromUserPropsIdsList(long playerId, int backpack, UserProps... userProps);

	/**
	 * 移除道具道具道具数量是否<=0
	 * @param playerId
	 * @param backpack
	 * @param userPropsList
	 */
	void removeUserPropsIfCountNotEnough(Collection<UserProps> userPropsList);
	
	/**
	 * 移除道具道具道具数量是否<=0
	 * @param playerId
	 * @param backpack
	 * @param userPropsList
	 */
	void removeUserPropsIfCountNotEnough(UserProps... userPropsList);

	
	/**
	 * 获得通过用户道具ID列表查询用户道具列表
	 * 
	 * @param  userItemIdList			用户道具ID列表
	 * @return {@link Collection}		用户道具列表
	 */
	List<UserProps> getUserPropsByIdList(Collection<Long> userItemIdList);

	/**
	 * 查询角色所属背包道具列表
	 * 
	 * @param  playerId					角色ID
	 * @param  backpack					用户背包对象
	 * @return {@link List}				用户道具列表
	 */
	List<UserProps> listUserProps(long playerId, int backpack);
	
	/**
	 * 查询用户装备ID列表 
	 * 
	 * @param  playerId			角色ID
	 * @param  backpack			背包号
	 * @return {@link List}		用户装备ID列表
	 */
	Set<Long> getUserEquipIdList(long playerId, int backpack);
	
	/**
	 * 装备移背包
	 * @param playerId
	 * @param backpacks
	 */
	void changeUserEquipBackpack(long playerId, int sourceBackpack,  int targetBackpack, Collection<UserEquip> userEquipList);
	
	/**
	 * 装备移背包
	 * @param playerId
	 * @param backpacks
	 */
	void changeUserEquipBackpack(long playerId, int sourceBackpack,  int targetBackpack, UserEquip... userEquipList);
	
	/**
	 * 添加用户装备ID列表
	 * 
	 * @param playerId				角色ID
	 * @param backpacks				背包号
	 */
	void put2UserEquipIdsList(long playerId, int backpack, Collection<UserEquip> userEquipList);
	
	/**
	 * 添加用户装备ID列表
	 * 
	 * @param playerId				角色ID
	 * @param backpacks				背包号
	 */
	void put2UserEquipIdsList(long playerId, int backpack, UserEquip... userEquipList);
	
	/**
	 * 移除用户装备ID列表
	 * @param playerId
	 * @param backpack
	 * @param userEquipList
	 */
	void removeFromEquipIdsList(long playerId, int backpack, Collection<UserEquip> userEquipList);
	
	/**
	 * 移除用户装备ID列表
	 * @param playerId
	 * @param backpack
	 * @param userEquips
	 */
	void removeFromEquipIdsList(long playerId, int backpack, UserEquip... userEquips);
	
	/**
	 * 查询用户道具ID列表 
	 * 
	 * @param  playerId			角色ID
	 * @param  backpack			背包号
	 * @return {@link List}		用户道具ID列表
	 */
	Set<Long> getUserPropsIdList(long playerId, int backpack);
	
	/**
	 * 查询用户装备列表
	 * 
	 * @param  playerId				角色ID
	 * @param  backpack				背包号				
	 * @return {@link List}			用户背包ID列表
	 */
	List<UserEquip> listUserEquip(long playerId, int backpack);

	/**
	 * 通过用户装备ID列表查询用户装备对象
	 * 
	 * @param  userEquipIdList		用户装备ID列表
	 * @return {@link List}			用户装备列表
	 */
	List<UserEquip> getUserEquipByIdList(Collection<Long> userEquipIdList);
	
	/**
	 * 查询角色所属背包, 指定基础ID的道具列表
	 * 
	 * @param  playerId					角色ID
	 * @param  propsId					基础道具ID
	 * @param  backpack					用户背包对象
	 * @return {@link List}				用户道具列表
	 */
	List<UserProps> listUserPropByBaseId(long playerId, int propsId, int backpack);
	
	/**
	 * 根据基础ID查询装备列表
	 * 
	 * @param  playerId					角色ID
	 * @param  equipId					装备ID
	 * @param  backpack					背包号
	 * @return {@link List}				用户装备列表
	 */
	List<UserEquip> listUserEquipByBaseId(long playerId, int equipId, int backpack);

	/**
	 * 创建用户道具
	 * 
	 * @param userProps
	 */
	UserProps createUserProps(UserProps userProps);
	
	/**
	 * 创建用户装备信息
	 * 
	 * @param userProps					用户道具数组
	 */
	List<UserProps> createUserProps(Collection<UserProps> userPropsList);
	
	/**
	 * 创建用户装备数组
	 * 
	 * @param userEquips				创建用户装备
	 */
	List<UserEquip> createUserEquip(UserEquip...userEquips);

	/**
	 * 创建用户装备
	 * 
	 * @param userEquips				创建用户装备列表
	 */
	List<UserEquip> createUserEquip(Collection<UserEquip> userEquips);
	
	/**
	 * 拆分用户道具
	 * 
	 * @param  createProps				新创建的用户道具
	 * @param  updateProps				更新的用户道具
	 */
	List<UserProps> spliteUserProps(UserProps createProps, UserProps updateProps);
	 
	/**
	 * 扣用户道具
	 * @param costUserItems				需要扣的道具 {@link Map<Long, Integer>}			
	 * @return							返回扣完后道具的剩余列表
	 */
	List<UserProps> costUserPropsList(Map<Long, Integer> costUserItems);

	/**
	 * 更新用户道具数量
	 * @param updateUserItems			需要更新的道具 {@link Map<Long, Integer>}
	 * @return							返回扣完后道具的剩余列表 {@link List<UserProps>}
	 */
	List<UserProps> updateUserPropsList(Map<Long, Integer> updateUserItems);

	/**
	 * 创建用户道具或装备(同时)					
	 * @param  userPropsList			用户道具列表
	 * @param  userEquips				用户装备列表
	 * @return {@link CreateResult}		返回值信息
	 */
	CreateResult<UserProps,UserEquip> createUserEquipAndUserProps(Collection<UserProps> userPropsList, Collection<UserEquip> userEquips);
	
	/**
	 * 变换用户道具或装备的背包类型(只支持抽奖仓库向默认背包中称物品)
	 * 
	 * @param playerId					角色id
	 * @param userPropsList				要变换背包的用户道具
	 * @param userEquipList				要变换背包的用户道装备集合
	 * @param sourcePackType			源背包类型	{@link BackpackType}
	 * @param targetPackType			目标背包类型 {@link BackpackType}
	 * @return
	 */
	int transferPackage(long playerId, List<UserProps> userPropsList,  List<UserEquip> userEquipList, int sourcePackType, int targetPackType);
	
	
	
}
