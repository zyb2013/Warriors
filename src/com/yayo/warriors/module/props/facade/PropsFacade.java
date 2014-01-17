
package com.yayo.warriors.module.props.facade;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.entity.UserProps;
import com.yayo.warriors.module.props.model.ShenwuResult;
import com.yayo.warriors.module.props.model.SynthStoneResult;
import com.yayo.warriors.module.props.model.WashAttributeVO;
import com.yayo.warriors.module.user.type.PortableType;

/**
 * 道具接口
 * 
 * @author Hyint
 */
public interface PropsFacade {

	/**
	 * 查询用户道具信息
	 * 
	 * @param  userPropsIds			用户道具ID数组
	 * @return {@link List}			用户道具列表
	 */
	List<UserProps> queryUserProps(Object[] userPropsIds);
	
	/**
	 * 列出背包中的装备实体信息
	 * 
	 * @param  playerId				角色ID
	 * @param  backpack				背包号
	 * @return {@link List}			背包实体信息
	 */
	List<BackpackEntry> listEquipBackpackEntry(long playerId, int backpack);
	
	/**
	 * 列出背包中的道具实体信息
	 * 
	 * @param  playerId				角色ID
	 * @param  backpack				背包号
	 * @return {@link List}			背包实体信息
	 */
	List<BackpackEntry> listPropsBackpackEntry(long playerId, int backpack);
	
	/**
	 * 丢弃用户装备
	 * 
	 * @param  playerId				角色ID
	 * @param  userEquipId			用户道具ID
	 * @return {@link ResultObject}	操作返回值
	 */
	ResultObject<BackpackEntry> dropUserEquip(long playerId, long userEquipId);
	
	/**
	 * 出售用户装备
	 * 
	 * @param  playerId				角色ID
	 * @param  userEquipId			用户装备ID
	 * @return {@link Integer}		用户装备ID
	 */
	ResultObject<BackpackEntry> sellUserEquip(long playerId, long userEquipId);
	
	/**
	 * 角色上装.
	 * 
	 * @param  playerId				角色ID
	 * @param  userEquipId			用户装备ID
	 * @param  targetEquipId 		被替换下的装备ID
	 * @return {@link ResultObject}	穿着装备的返回值. [原来在身上的装备, 原来在背包中的装备]
	 */
	ResultObject<BackpackEntry[]> dressUserEquip(long playerId, long userEquipId, long targetEquipId);
	
	/**
	 * 角色脱下装备
	 * 
	 * @param  playerId				角色ID
	 * @param  userEquipId			用户装备ID
	 * @param  index				背包的索引号
	 * @return {@link ResultObject}	穿着装备的返回值. [原来在身上的装备, 原来在背包中的装备]
	 */
	ResultObject<BackpackEntry[]> undressUserEquip(long playerId, long userEquipId, int index);
	
	/**
	 * 损坏角色身上装备的耐久值
	 * 
	 * @param playerId				角色ID
	 * @param damageEndure			损坏装备的耐久值
	 */
	void damageUserEquipEndure(long playerId, int damageEndure);
	
	
	//-------------------------------以下是道具模块----------------------------------------------------

	
	/**
	 * 使用用户道具
	 * 
	 * @param  playerId					角色ID
	 * @param  userPropsId				用户道具ID
	 * @param  count					数量
	 * @return {@link Integer}			用户道具模块返回值
	 */
	int useProps(long playerId, long userPropsId, int count);
	
	/** 
	 * 丢弃用户道具
	 * 
	 * @param  playerId					用户ID
	 * @param  userPropsId				用户道具ID
	 * @return {@link ResultObject}		用户道具模块返回值
	 */
	ResultObject<BackpackEntry> dropUserProps(long playerId, long userPropsId);
	
	/**
	 * 出售用户道具
	 * 
	 * @param  playerId					用户ID
	 * @param  userPropsId				用户道具ID
	 * @return {@link Integer}			用户道具模块返回值
	 */
	ResultObject<BackpackEntry> sellUserProps(long playerId, long userPropsId);
	
	/** 
	 * 使用HP/MP/SP便携包
	 * 
	 * @param  playerId					角色ID
	 * @param  type						补血类型. 详细见: {@link PortableType}
	 * @param  needPost					是否需要推送
	 * @return {@link Integer}			是否使用成功
	 */
	int usePortableBag(long playerId, boolean needPost, PortableType...type);
	
	/**
	 * 批量购买用户道具和使用(目前只支持批量购买HPBag和MPBag道具)
	 * @param playerId					角色ID			
	 * @param propsId					基础道具id
	 * @param count						道具数量
	 * @param isBuy						是否购买道具
	 * @param isUse						是否使用道具
	 * @return	{@link Integer}			用户道具模块返回值
	 */
	int batchBuyOrUseProps(long playerId, int propsId, int count, boolean isBuy, boolean isUse);
	
	/**
	 * 更新背包位置
	 * 
	 * @param playerId					角色ID
	 * @param info						背包位置信息.格式:[[物品ID, 物品类型, 物品位置], [物品ID, 物品类型, 物品位置]...]
	 */
	void updateBackpackEntryPosition(long playerId, Object[] info);
	
	/** 
	 * 整理背包位置
	 * 
	 * @param  playerId					角色ID
	 * @param  backpack					背包号
	 * @return {@link Integer}			道具模块返回值
	 */
	int settleBackpackPos(long playerId, int backpack);
	
	/**
	 * 合并用户道具
	 * 
	 * @param  playerId					角色ID
	 * @param  addPropsId				数量会增加的道具ID
	 * @param  costPropsId				数量会减少的道具ID
	 * @return {@link Integer}			返回值
	 */
	int mergeUserProps(long playerId, long addPropsId, long costPropsId);
	
	/**
	 * 拆分用户道具
	 * 
	 * @param  playerId					角色ID
	 * @param  userPropsId				需要拆分的用户道具ID
	 * @param  count					需要拆分的用户道具数量
	 * @return {@link Integer}			返回值对象信息	
	 */
	int spliteUserProps(long playerId, long userPropsId, int count);

	//-----------------特殊业务
	/**
	 * 合成宝石道具
	 * 
	 * @param  playerId					角色ID
	 * @param  bindUserItems			绑定的用户道具
	 * @param  unBindUserItems			未绑定的用户道具
	 * @return {@link SynthStoneResult}	返回值信息对象		
	 */
	SynthStoneResult synthStoneItem(long playerId, String bindUserItems, String unBindUserItems);
	 
	/**
	 * 合成舍利子
	 * 
	 * @param playerId					角色ID
	 * @param userPropsId				用户道具ID(舍利子, 主材料)
	 * @param targetPropsId				用户道具ID(舍利子, 辅材料)
	 * @param userItems					聚灵珠用户道具. 格式: 用户道具ID_数量|...
	 * @param luckItems					幸运石用户道具. 格式: 用户道具ID_数量|...
	 * @param autoBuyCount				自动购买聚灵珠的数量
	 * @return
	 */
	ResultObject<Collection<UserProps>> synthSharipuDiverse(long playerId, long userPropsId, 
			long targetPropsId,	String userItems, String luckItems, int autoBuyCount);

	/**
	 * 合成舍利子, 相同堆的舍利子合成
	 * 
	 * @param playerId					角色ID
	 * @param userPropsId				用户道具ID(舍利子, 主材料)
	 * @param userItems					聚灵珠用户道具. 格式: 用户道具ID_数量|...
	 * @param luckItems					幸运石用户道具. 格式: 用户道具ID_数量|...
	 * @param autoBuyCount				自动购买聚灵珠的数量
	 * @return
	 */
	ResultObject<Collection<UserProps>> synthSharipuSameItem(long playerId, long userPropsId, 
										String userItems, String luckItems, int autoBuyCount);
	
	/**
	 * 装备升星
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  userProps				金刚石用户道具.. 格式: 用户道具ID_数量|...
	 * @param  luckyUserItems			幸运石用户道具.. 格式: 用户道具ID_数量|...
	 * @param  userPropsId				用户保护符道具ID
	 * @param  autoBuyCount				自动购买金刚石的数量
	 * @return {@link ResultObject}		返回值信息
	 */
	ResultObject<Collection<BackpackEntry>> ascentEquipStar(long playerId, long userEquipId, 
				String userProps, String luckyUserItems, long userPropsId, int autoBuyCount);

	/**
	 * 装备升阶. 
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  userProps				升阶石信息. 	    格式: 用户道具ID_用户道具数量|..
	 * @param  safeUserProps			镇星符数量. 	    格式: 用户道具ID_用户道具数量|..
	 * @param  lockyUserProps			幸运晶道具信息. 格式: 用户道具ID_用户道具数量|..
	 * @param  autoBuyCount				自动购买升阶石的数量
	 * @return {@link ResultObject}		返回值信息
	 */
	ResultObject<Collection<BackpackEntry>> ascentEquipRank(long playerId, long userEquipId, 
					String userProps, String safeUserProps, String lockyUserProps, int autoBuyCount);
	
	/**
	 * 装备镶嵌
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  enchangeInfos			镶嵌下标1_镶嵌的用户道具ID1|镶嵌下标2_镶嵌的用户道具ID2|
	 * @return {@link ResultObject}		返回值信息
	 */
	ResultObject<Collection<BackpackEntry>> enchangeEquip(long playerId, long userEquipId, String enchangeInfos);
	
	/**
	 * 移除装备镶嵌的宝石
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户道具ID
	 * @param  index					移除的下标
	 * @return {@link ResultObject}		返回值信息
	 */
	ResultObject<Collection<BackpackEntry>> removeEquipEnchange(long playerId, long userEquipId, int index);
	
	/**
	 * 洗练角色装备的属性值
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  userProps				精炼石道具ID信息.(格式: 用户道具ID_使用数量|...)
	 * @param  autoBuyCount				自动购买的数量
	 * @param  safeIndex				保护的下标集合(保护下标_保护下标...)
	 * @param  lockProps				洗练锁道具信息(格式: 用户道具ID1_使用数量1|...)
	 * @return {@link ResultObject}		返回值对象
	 */
	ResultObject<Collection<BackpackEntry>> polishedEquipAdditions(long playerId, long userEquipId, 
							String userProps, int autoBuyCount, String safeIndex, String lockProps);
	
	/**
	 * 选择洗练装备的附加属性
	 * 
	 * @param  playerId					角色ID
	 * @param  select					是否选择属性. true-选择, false-删除
	 * @return {@link ResultObject}		返回值对象			
	 */
	int selectPolishedEquipAddition(long playerId, boolean select);
	
	/**
	 * 移除洗练属性VO对象
	 * 
	 * @param  playerId					角色ID
	 * @return {@link Boolean}			是否移除洗练属性VO
	 */
	WashAttributeVO removeWashAttributeVO(long playerId);
	
	/**
	 * 获得洗练属性VO对象
	 * 
	 * @param  playerId					角色ID
	 * @return {@link WashAttributeVO}	洗练属性VO对象
	 */
	WashAttributeVO getWashAttributeVO(long playerId);
	
	/**
	 * 增删装备附加属性
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  userProps				属性增删宝石    .格式: 用户道具ID_数量|..
	 * @param  luckyProps				幸运石道具信息.格式: 用户道具ID_数量|..
	 * @param  autoBuyCount				自动购买增删宝石的数量
	 * @return {@link ResultObject}		用户道具模块返回值
	 */
	ResultObject<Collection<BackpackEntry>> emendationEquipAttribute(long playerId, long userEquipId, 
												String userProps, String luckyProps, int autoBuyCount);
	
	/**
	 * 提升装备的附加属性
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  userProps				玄晶石...	格式: 用户道具ID_数量|..
	 * @param  luckyProps				幸运石信息.	格式: 用户道具ID_数量|..
	 * @param  autoBuyCount				自动购买玄晶石的数量
	 * @param  upgradeParams			需要洗点的信息. 
	 * @return {@link ResultObject}		用户道具模块返回值
	 */
	ResultObject<Collection<BackpackEntry>> recastEquipAttribute(long playerId, long userEquipId, 
					String userProps, String luckyProps, int autoBuyCount, Set<Integer> upgradeParams);
	
	/**
	 * 精炼用户装备对象
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  userProps				用户道具信息. 格式: 用户道具ID_数量|...
	 * @return {@link ResultObject}		精炼用户装备返回值
	 */
	ResultObject<Collection<BackpackEntry>> refineEquipAttribute(long playerId, long userEquipId, String userProps);
	
	/**
	 * 用户道具炼化
	 * 
	 * @param  playerId					角色ID
	 * @param  propsId					道具ID
	 * @param  count					道具数量
	 * @param  userProps				用户道具信息. 格式: 用户道具ID1_道具数量1|用户道具ID2_道具数量2|...
	 * @return {@link ResultObject}		用户道具模块返回值
	 */
	ResultObject<Collection<BackpackEntry>> aritificeProps(long playerId, int propsId, int count, String userProps);
	
	/**
	 * 用户装备分解
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquips				用户装备信息. 格式: 用户装备ID_用户装备ID_...
	 * @return {@link ResultObject}		用户道具模块返回值
	 */
	ResultObject<Collection<BackpackEntry>> resolveUserEquips(long playerId, String userEquips);
	
	/**
	 * 把物品放入仓库中
	 * 
	 * @param  playerId					角色ID
	 * @param  goodsId					移动的用户道具/用户装备ID
	 * @param  goodsType				移动的物品类型
	 * @param  userPropsId				需要堆叠的物品ID.
	 * @param  index					物品的下标
	 * @return {@link ResultObject}		返回值封装对象
	 */
	ResultObject<Collection<BackpackEntry>> put2Storage(long playerId, long goodsId, int goodsType, long userPropsId, int index);
	
	/**
	 * 从仓库中取出物品
	 * 
	 * @param  playerId					角色ID
	 * @param  goodsId					移动的用户道具/用户装备ID
	 * @param  goodsType				移动的物品类型
	 * @param  userPropsId				需要堆叠的物品ID.
	 * @param  amount					需要移动的数量
	 * @param  index					物品的下标
	 * @return {@link ResultObject}		返回值封装对象
	 */
	ResultObject<Collection<BackpackEntry>> checkoutFromStorage(long playerId, long goodsId, int goodsType, int amount, long userPropsId, int index);
	
	/**
	 * 交换物品的背包号
	 * 
	 * @param  playerId					角色ID
	 * @param  goodsId					发起交换的物品ID
	 * @param  goodsType				发起交换的物品类型
	 * @param  targetId					被交换的物品ID
	 * @param  targetType				被交换的物品ID
	 * @return {@link ResultObject}		返回值封装对象
	 */
	ResultObject<Collection<BackpackEntry>> swapBackpack(long playerId, long goodsId, int goodsType, long targetId, int targetType);
	
	/**
	 * 扩展背包
	 * 
	 * @param playerId                  角色ID
	 * @param userItems                 扩展符用户道具信息, 格式: 用户道具ID_数量|...
	 * @param autoBuyCount              自动购买道具数量
	 * @param backpack                  背包类型
	 * @return {@link ResultObject}     返回值封装对象
	 */
	ResultObject<Collection<BackpackEntry>> expandBackpack(long playerId, String userItems, int autoBuyCount, int backpack);

	/**
	 * 计算修理装备需要的费用
	 * 
	 * @param  playerId					角色ID
	 * @param  backpackInfos			背包信息. 格式: 背包1_背包2_...
	 * @return {@link Integer}			需要扣除的货币信息
	 */
	int calcRepairCostSilver(long playerId, String backpackInfos);
	
	/**
	 * 修理角色的装备
	 * 
	 * @param  playerId					角色ID
	 * @param  backpackInfos			背包信息
	 * @return {@link Integer}			返回值信息
	 */
	int repairUserEquips(long playerId, String backpackInfos);

	/**
	 * 从抽奖仓库中取出物品
	 * 
	 * @param  playerId					角色ID
	 * @param  goodsId					移动的用户道具/用户装备ID
	 * @param  goodsType				移动的物品类型
	 * @param  userPropsId				需要堆叠的物品ID.
	 * @param  amount					需要移动的数量
	 * @param  index					物品的下标
	 * @return {@link ResultObject}		返回值封装对象
	 */
	ResultObject<Collection<BackpackEntry>> checkoutFromLotteryStorage(long playerId, 
			long goodsId, int goodsType, int amount, long userPropsId, int index);
	
	/**
	 * 神武进度突破
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  rank						当前装备的神武阶
	 * @param  userProps				消耗的用户道具. 用户道具ID1_使用数量1|用户道具ID2_使用数量2|...
	 * @return {@link ShenwuResult}		返回值封装对象
	 */
	ShenwuResult doEquipShenwuTempo(long playerId, long userEquipId, int rank, String userProps);
	
	/**
	 * 神武属性打造
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				用户装备ID
	 * @param  rank						当前装备的神武阶
	 * @param  userProps				消耗的用户道具. 用户道具ID1_使用数量1|用户道具ID2_使用数量2|...
	 * @return {@link ShenwuResult}		返回值封装对象
	 */
	ShenwuResult doShenwuAttributeForge(long playerId, long userEquipId, int shenwuId, String userProps);
	
	/**
	 * 装备星级继承
	 * 
	 * @param  playerId					角色ID
	 * @param  userEquipId				原始用户装备ID
	 * @param  targetEquipId			目标装备ID
	 * @param  userPropsId				继承石保护道具
	 * @return {@link ResultObject}		返回值封装对象
	 */
	ResultObject<Collection<BackpackEntry>> equipExtendStar(long playerId, long userEquipId, long targetEquipId, long userPropsId);

}
