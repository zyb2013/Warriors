package com.yayo.warriors.basedb.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.MallActiveConfig;
import com.yayo.warriors.basedb.model.MallSpecialConfig;
import com.yayo.warriors.basedb.model.NpcConfig;
import com.yayo.warriors.basedb.model.ShopConfig;
import com.yayo.warriors.type.IndexName;

/**
 * 商店/商城配置对象
 * 
 * @author Hyint
 */
@Component
public class ShopService extends ResourceAdapter {
	// 限购物品对象
//	private volatile List<Integer> LIMIT_MALLS = new ArrayList<Integer>();
	
	// 缓存的商店NPCID{ 物品类型, {NPCID, 物品ID }}
	private volatile Map<Integer, Map<Integer, Set<Integer>>> CACHE_SHOP_NPC_NPCIDS = new HashMap<Integer, Map<Integer, Set<Integer>>>(5);
	
//	 商城物品列表
//	private volatile Map<Integer, MallProps[]> CACHE_MALL_MAPS = new HashMap<Integer, MallProps[]>();
//	 商店列表. { NPCID_JOB, ShopProps[] } , { NPCID, {JOB, ShopProps[]} }
//	private volatile Map<Integer, Map<Integer, ShopProps[]>> CACHE_SHOP_MAPS = new HashMap<Integer, Map<Integer, ShopProps[]>>();

	@Override
	public void initialize() {
		this.constMallConfig();
		this.constShopConfig();
	}

	// 初始化商城的出售物品
	private void constMallConfig() {
//		//限购物品
//		Set<Integer> limits = new HashSet<Integer>();
//
//		Collection<MallSpecialConfig> specialList = resourceService.listAll(MallSpecialConfig.class);
//		
//		if (specialList != null && !specialList.isEmpty()) {
//			for (MallSpecialConfig config : specialList) {
//				limits.add(config.getId());
//			}
//		}
//		LIMIT_MALLS.clear();
//		LIMIT_MALLS.addAll(limits);
	}

	
	/**
	 * 构建商店配置信息
	 */
	private void constShopConfig() {
		// 刷新列表
		Map<Integer, Map<Integer, Set<Integer>>> shopNpcIds = new HashMap<Integer, Map<Integer, Set<Integer>>>(5);
		// 获得所有Npc商店基础数据
		Collection<ShopConfig> shopConfigList = resourceService.listAll(ShopConfig.class);
		if (shopConfigList != null && !shopConfigList.isEmpty()) {
			for (ShopConfig shopConfig : shopConfigList) {
				refreshShopPropsTypeInfo(shopNpcIds, shopConfig);
			}
		}

		CACHE_SHOP_NPC_NPCIDS.clear();
		CACHE_SHOP_NPC_NPCIDS.putAll(shopNpcIds);
	}
	

	
//	/**
//	 * 查询商城特价商品列表
//	 * 
//	 * @return {@link Collection}
//	 */
//	public List<Integer> getSpecialMallList() {
//		return this.LIMIT_MALLS;
//	}
	
	/**
	 * 刷新物品类型NPCID信息
	 * 
	 * @param  shopNpcIds	物品的NPCID列表信息
	 * @param  shopConfig	物品对象
	 */
	private void refreshShopPropsTypeInfo(Map<Integer, Map<Integer, Set<Integer>>> shopNpcIds, ShopConfig shopConfig) {
		int goodsType = shopConfig.getGoodsType();
		List<Integer> npcIdList = shopConfig.getNpcIdList();
		if(npcIdList == null || npcIdList.isEmpty()) {
			return;
		}
		
		Map<Integer, Set<Integer>> map = shopNpcIds.get(goodsType);
		if(map == null) {
			map = new HashMap<Integer, Set<Integer>>(5);
			shopNpcIds.put(goodsType, map);
		}
		
		int propsId = shopConfig.getPropsId();
		Set<Integer> set = map.get(propsId);
		if(set == null) {
			set = new HashSet<Integer>(5);
			map.put(propsId, set);
		}
		set.addAll(npcIdList);
	}

	/**
	 * 随机查询出售某装备/道具的NPCID
	 * 
	 * @param  goodsType		物品类型
	 * @param  propsId			物品ID
	 * @return {@link Long}		NPCID
	 */
	public long findNpcByEquipOrProps(int goodsType, int propsId, int screenType) {
		long npcId = 0L;
		Map<Integer, Set<Integer>> map = CACHE_SHOP_NPC_NPCIDS.get(goodsType);
		if(map == null) {
			return npcId;
		}
		
		Set<Integer> set = map.get(propsId);
		if(set == null || set.isEmpty()) {
			return npcId;
		}
		
		List<Integer> arrayList = new ArrayList<Integer>(set);
		do {
			if(arrayList.isEmpty()) {
				return npcId;
			}
			int randomIndex = Tools.getRandomInteger(arrayList.size());
			Integer randomNpcId = arrayList.remove(randomIndex);
			NpcConfig npcConfig = resourceService.get(randomNpcId, NpcConfig.class);
			if(npcConfig != null && npcConfig.getScreenType() == screenType) {
				npcId = randomNpcId;
				break;
			}
		} while (npcId <= 0L);
		
		return npcId;
	}
	
//	/**
//	 * 刷新职业道具信息
//	 * 
//	 * @param propsMap
//	 * @param shopConfig
//	 */
//	private void refreshJobPropsInfo(Map<Integer, Map<Integer, List<ShopProps>>> propsMap, ShopConfig shopConfig) {
//		int roleJob = shopConfig.getRoleJob();
//		List<Integer> npcIdList = shopConfig.getNpcIdList();
//		for (Integer npcId : npcIdList) {
//			Map<Integer, List<ShopProps>> shopCacheMap = propsMap.get(npcId);
//			if (shopCacheMap == null) {
//				shopCacheMap = new HashMap<Integer, List<ShopProps>>();
//				propsMap.put(npcId, shopCacheMap);
//			}
//
//			List<ShopProps> propsList = shopCacheMap.get(roleJob);
//			if (propsList == null) {
//				propsList = new ArrayList<ShopProps>();
//				shopCacheMap.put(roleJob, propsList);
//			}
//
//			ShopProps shopProps = ShopProps.valueOf(shopConfig);
//			if (shopProps != null && !propsList.contains(shopProps)) {
//				propsList.add(shopProps);
//			}
//		}
//	}

	/**
	 * 根据id获取指定类的实体
	 * 
	 * @param <T>
	 * @param id 					主键
	 * @param clazz 				实体类对象
	 * @return
	 */
	public <T> T get(Object id, Class<T> clazz) {
		return resourceService.get(id, clazz);
	}

	/**
	 * 获得有效活动配置
	 * 
	 * @param  open
	 * @return {@link List}
	 */
	public List<MallActiveConfig> listMallActives(int open) {
		return resourceService.listByIndex(IndexName.MALL_ACTIVE_OPEN, MallActiveConfig.class, open);
	}
	
	
//	public List<Integer> getEffectActiveIds() {
//		return resourceService.listIdByIndex(IndexName.MALL_ACTIVE_OPEN, MallActiveConfig.class, Integer.class, 1);
//	}
	
	
	/**
	 * 获得商城活动物品
	 * 
	 * @param activeId
	 * @return
	 */
	public List<MallSpecialConfig> listMallSpecial(int activeId) {
		return resourceService.listByIndex(IndexName.MALL_ACTIVE_PROPS, MallSpecialConfig.class, activeId);
	}
	
//	/**
//	 * 查询NPC出售的物品列表
//	 * 
//	 * @param  npcId 				NPC的ID
//	 * @param  roleJod 				物品所属的职业
//	 * @return {@link ShopProps[]} 	物品数组信息
//	 */
//	public ShopProps[] listShopProps(int npcId, int roleJod) {
//		Map<Integer, ShopProps[]> map = CACHE_SHOP_MAPS.get(npcId);
//		return map == null ? null : map.get(roleJod);
//	}

//	/**
//	 * 查询商城物品列表
//	 * 
//	 * @param mallType
//	 *            商城的类型. 详细见: {@link MallType}
//	 * @return {@link MallProps[]} 商城物品数组
//	 */
//	public MallProps[] listMallProps(int mallType) {
//		return CACHE_MALL_MAPS.get(mallType);
//	}

}
