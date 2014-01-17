package com.yayo.warriors.basedb.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceAdapter;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.EquipBreakConfig;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.EquipRankConfig;
import com.yayo.warriors.basedb.model.EquipRankRuleConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.SuitConfig;
import com.yayo.warriors.basedb.model.WashAttributeConfig;
import com.yayo.warriors.basedb.model.WashTypeConfig;
import com.yayo.warriors.type.IndexName;

/**
 * 装备配置接口
 * 
 * @author Hyint
 */
@Component
public class EquipService extends ResourceAdapter {
	
//	private final Map<Integer, List<WashTypeConfig>> CACHE_SORT_WASHTYPE_MAP = new HashMap<Integer, List<WashTypeConfig>>();
//	private final Map<Integer, List<EquipRankRuleConfig>> CACHE_SORT_RANKRULE_MAP = new HashMap<Integer, List<EquipRankRuleConfig>>();
//	
	
	@Override
	public void initialize() {
//		CACHE_SORT_WASHTYPE_MAP.clear();
//		CACHE_SORT_RANKRULE_MAP.clear();

		Collection<WashAttributeConfig> washAttrList = resourceService.listAll(WashAttributeConfig.class);
		for (WashAttributeConfig washAttributeConfig : washAttrList) {
			int id = washAttributeConfig.getId();
			int level = washAttributeConfig.getLevel();
			int quality = washAttributeConfig.getQuality();
			int attribute = washAttributeConfig.getAttribute();
			String washAttributeKey = toWashAttributeKey(level, attribute, quality);
			resourceService.addToIndex(washAttributeKey, id, WashAttributeConfig.class);
		}
		
		for (EquipBreakConfig equipBreakConfig : resourceService.listAll(EquipBreakConfig.class)) {
			equipBreakConfig.setPropsConfig1(resourceService.get(equipBreakConfig.getItemId1(), PropsConfig.class));
			equipBreakConfig.setPropsConfig2(resourceService.get(equipBreakConfig.getItemId2(), PropsConfig.class));
			String qualityLevelKey = toQualityLevelKey(equipBreakConfig.getQuality(), equipBreakConfig.getLevel());
			resourceService.addToIndex(qualityLevelKey, equipBreakConfig.getId(), EquipBreakConfig.class);
		}
		
		Collection<SuitConfig> equipSuitSrc = resourceService.listAll(SuitConfig.class);
		for (SuitConfig currSuit : equipSuitSrc) {
			int id = currSuit.getId();
			int suitId = currSuit.getSuitId();
			currSuit.setAttributes(new HashMap<Integer, Integer>());
			resourceService.addToIndex(toSuitConditionKey(currSuit.getCondition(), suitId), id, SuitConfig.class);
			List<SuitConfig> suits = resourceService.listByIndex(SuitConfig.SUIT_CONFIG_ID, SuitConfig.class, suitId);
			if(suits == null || suits.isEmpty()) {
				continue;
			}
			
			for (SuitConfig suit : suits) {
				if(suit.getCondition() > currSuit.getCondition()) {
					continue;
				}
				addAttribute2Map(suit.getAttribute1(), suit.getAttrValue1(), currSuit.getAttributes());
				addAttribute2Map(suit.getAttribute2(), suit.getAttrValue2(), currSuit.getAttributes());
				addAttribute2Map(suit.getAttribute3(), suit.getAttrValue3(), currSuit.getAttributes());
			}
		}

		//构建装备升阶信息列表
		Collection<EquipRankConfig> equipRanks = resourceService.listAll(EquipRankConfig.class);
		for (EquipRankConfig equipRankConfig : equipRanks) {
			int nextEquipId = equipRankConfig.getNextEquipId();
			equipRankConfig.setNextEquip(resourceService.get(nextEquipId, EquipConfig.class));
		}
		
		for (EquipConfig equipConfig : resourceService.listAll(EquipConfig.class)) {
			int equipId = equipConfig.getId();
			int level = equipConfig.getLevel();
			int quality = equipConfig.getQuality();
			resourceService.addToIndex(toLevelQuality(level, quality), equipId, EquipConfig.class);
		}
	}
	
	private String toLevelQuality(int level, int quality) {
		return new StringBuffer().append("LEVEL_").append(level).append("_QUALITY_").append(quality).toString();
	}
	
	private String toSuitConditionKey(int condition, int suitId) {
		return new StringBuilder().append("SUIT_ID_").append(suitId).append("_CONDITION_").append(condition).toString();
	}
	
	/**
	 * 列出装备基础对象列表
	 * 
	 * @param  level			装备的等级
	 * @return {@link List}		基础装备列表
	 */
	public List<EquipConfig> listEquipConfig(int level, int quality) {
		int minLevel = Math.max(0, level - 20);
		int maxLevel = Math.max(0, level + 5);
		List<EquipConfig> equipConfigSwapList = new ArrayList<EquipConfig>();
		for (int lv = minLevel; lv < maxLevel; lv++) {
			List<EquipConfig> equipList = resourceService.listByIndex(toLevelQuality(lv, quality), EquipConfig.class);
			if(equipList != null && !equipList.isEmpty()) {
				equipConfigSwapList.addAll(equipList);
			}
		}
		return equipConfigSwapList;
	}

	
	/**
	 * 叠加属性集合中的值
	 * 
	 * @param attribute
	 * @param attrValue
	 * @param attributes
	 */
	private void addAttribute2Map(int attribute, int attrValue, Map<Integer, Integer> attributes) {
		if(attribute != 0 && attrValue != 0) {
			Integer cacheValue = attributes.get(attribute);
			cacheValue = cacheValue == null ? 0 : cacheValue;
			attributes.put(attribute, cacheValue + attrValue);
		}
	}
	
	/**
	 * 构建星级Key
	 *  
	 * @param  quality			装备品质
	 * @return {@link String}	品质Key
	 */
	private String toQualityLevelKey(int quality, int starLevel) {
		return new StringBuilder().append("EQUIPID_BREAK_").append(quality).append("_LEVEL_").append(starLevel).toString();
	}
	
	/**
	 * 根据装备星级和品质获得装备分解对象
	 * 
	 * @param  starLevel					装备星级
	 * @param  quality						装备品质
	 * @return {@link EquipResolveConfig}	装备分解配置
	 */
	public EquipBreakConfig getEquipResolveByIndex(int starLevel, int quality) {
		return resourceService.getByUnique(toQualityLevelKey(quality, starLevel), EquipBreakConfig.class);
	}
	
	/**
	 * 构建洗练属性Key
	 * 
	 * @param  level				装备等级
	 * @param  attribute			属性值
	 * @return {@link String}		属性Key
	 */
	private String toWashAttributeKey(int level, int attribute, int quality) {
		return new StringBuilder().append("WASH_LEVEL_").append(level).append("_ATTRIBUTE_").append(attribute).append("_QUALITY_").append(quality).toString();
	}
	
	/**
	 * 列出附加属性洗练类型配置对象列表
	 * 
	 * @return {@link List}
	 */
	public List<WashTypeConfig> listAdditionWashTypeConfig(int addition) {
		List<WashTypeConfig> washTypeConfigs = resourceService.listByIndex(IndexName.EQUIP_WASH_ADDITION, WashTypeConfig.class, addition);
		if(washTypeConfigs != null && !washTypeConfigs.isEmpty()) {
			Collections.sort(washTypeConfigs);
		}
		return washTypeConfigs;
	}
	
	/**
	 * 列出装备升阶配置信息
	 * 
	 * @param  attribute		装备的属性
	 * @return {@link List}		装备升阶配置规则对象
	 */
	public List<EquipRankRuleConfig> listEquipRankConfig(int attributeCount) {
		List<EquipRankRuleConfig> equipRankRuleConfigs = resourceService.listByIndex(IndexName.EQUIP_RANKRULE_ATTRIBUTE, EquipRankRuleConfig.class, attributeCount);
		if(equipRankRuleConfigs != null && !equipRankRuleConfigs.isEmpty()) {
			Collections.sort(equipRankRuleConfigs);
		}
		return equipRankRuleConfigs;
	}
	
	/**
	 * 获得星级变化随机对象
	 * 
	 * @param  attributeCount		附加属性的条数/当前星级
	 * @param  byStar				是否取得星级概率计算. true-按星级, false-按附加属性
	 * @return
	 */
	public EquipRankRuleConfig getRandomEquipRankRule(int attributeCount, boolean byStar) {
		List<EquipRankRuleConfig> list = this.listEquipRankConfig(attributeCount);
		if(list == null || list.isEmpty()) {
			return null;
		}
		
		EquipRankRuleConfig rule = list.get(0);
		if(rule == null) {
			return null;
		}
		
		int totalRate = 0;
		int maxRate = rule.getMaxRate();
		int random = Tools.getRandomInteger(maxRate);
		for (EquipRankRuleConfig config : list) {
			totalRate += (byStar ? config.getStarRate() : config.getAdditionRate());
			if(random < totalRate) {
				return config;
			}
		}
		return null;
	}
	
	/**
	 * 获得洗练属性配置对象
	 * 
	 * @param  equipLevel					装备等级	
	 * @param  attribute					装备附加属性
	 * @param  quality						装备的品质
	 * @return {@link WashAttributeConfig}	洗练属性配置对象
	 */
	public WashAttributeConfig getWashAttributeConfig(int equipLevel, int attribute, int quality) {
		return resourceService.getByUnique(toWashAttributeKey(equipLevel, attribute, quality), WashAttributeConfig.class);
	}
	
	/**
	 * 获得随机附加属性类型
	 * 
	 * @param  addition					附加的属性
	 * @return {@link WashTypeConfig}	洗练属性配置对象
	 */
	public WashTypeConfig getRandomAdditionWashType(int addition) {
		List<WashTypeConfig> typeConfigList = listAdditionWashTypeConfig(addition);
		if(typeConfigList == null || typeConfigList.isEmpty()) {
			return null;
		}
		
		WashTypeConfig washTypeConfig = typeConfigList.get(0);
		if(washTypeConfig == null) {
			return null;
		}
		
		int totalRate = 0;
		int random = Tools.getRandomInteger(washTypeConfig.getMaxRate());
		for (WashTypeConfig typeConfig : typeConfigList) {
			totalRate += typeConfig.getRate();
			if(random < totalRate) {
				return typeConfig;
			}
		}
		return null;
	}
	
	public SuitConfig getSuitConfig(int suitId, int equipCount) {
		while (equipCount > 0) {
			String suitConditionKey = toSuitConditionKey(equipCount, suitId);
			SuitConfig suitConfig = resourceService.getByUnique(suitConditionKey, SuitConfig.class);
			if(suitConfig != null) {
				return suitConfig;
			}
			equipCount --;
		}
		return null;
	}
}
