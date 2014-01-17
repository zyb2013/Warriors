package com.yayo.warriors.module.props.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.adapter.EquipService;
import com.yayo.warriors.basedb.adapter.ShenwuService;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.EquipRankConfig;
import com.yayo.warriors.basedb.model.EquipRankRuleConfig;
import com.yayo.warriors.basedb.model.EquipStarConfig;
import com.yayo.warriors.basedb.model.PropsConfig;
import com.yayo.warriors.basedb.model.ShenwuAttributeConfig;
import com.yayo.warriors.basedb.model.ShenwuConfig;
import com.yayo.warriors.basedb.model.WashAttributeConfig;
import com.yayo.warriors.basedb.model.WashTypeConfig;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.props.entity.UserEquip;
import com.yayo.warriors.module.props.manager.PropsManager;
import com.yayo.warriors.module.props.model.AttributeVO;
import com.yayo.warriors.module.props.model.HoleInfo;
import com.yayo.warriors.module.props.model.ShenwuSwitch;
import com.yayo.warriors.module.props.type.Quality;

/**
 * 装备帮助类
 * 
 * @author Hyint
 */
@Service
public class EquipHelper {
	private static final ObjectReference<EquipHelper> ref = new ObjectReference<EquipHelper>();
	
	@Autowired
	private PropsManager propsManager;
	@Autowired
	private ShenwuService shenwuService;
	@Autowired
	private EquipService equipConfigService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EquipHelper.class);

	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	public static EquipConfig getEquipConfig(int equipId) {
		return getInstance().propsManager.getEquipConfig(equipId);
	}
	
	public static ShenwuAttributeConfig getShenwuAttributes(int equipType, int shenwuId, int job, int attribute) {
		return getInstance().shenwuService.getShenwuAttribute(shenwuId, equipType, job, attribute);
	}

	public static PropsConfig getPropsConfig(int propsId) {
		return getInstance().propsManager.getPropsConfig(propsId);
	}
	
	public static EquipStarConfig getEquipStarConfig(int star) {
		return getInstance().propsManager.getEquipStarConfig(star);
	}
	/**
	 * 获得装备帮助类的实例
	 * 
	 * @return {@link EquipHelper}		装备帮助类
	 */
	private static EquipHelper getInstance() {
		return ref.get();
	}
	
	public static void rebuildIndex(Map<Integer, AttributeVO> additionAttributes){
		int index = 1;
		Set<AttributeVO> treeSet = new TreeSet<AttributeVO>(additionAttributes.values());
		additionAttributes.clear();
		for (AttributeVO attributeVO : treeSet) {
			attributeVO.setId(index);
			additionAttributes.put(attributeVO.getId(), attributeVO);
			index++;
		}
	}

	
	/**
	 * 创建一件装备(默认在背包中)
	 * 
	 * @param playerId		角色ID
	 * @param baseId		基础装备ID
	 * @param binding		创建出来后的绑定状态
	 * @return
	 */
	public static UserEquip newUserEquip(long playerId, int backpack, int baseId, boolean binding) {
		UserEquip userEquip = null;
		EquipConfig equipConfig = getInstance().propsManager.getEquipConfig(baseId);
		if(equipConfig != null) {
			userEquip = newUserEquip(playerId, equipConfig, backpack, binding);
		}
		return userEquip; 
	}
	
	/**
	 * 新创建用户装备列表
	 * 
	 * @param  playerId		角色ID
	 * @param  baseId		基础ID
	 * @param  binding		绑定类型
	 * @param  count		创建数量
	 * @return {@link List}	用户装备列表
	 */
	public static List<UserEquip> newUserEquips(long playerId, int backpack, int baseId, boolean binding, int count) {
		List<UserEquip> userEquips = new ArrayList<UserEquip>(count);
		for (int i = 0; i < count; i++) {
			UserEquip userEquip = newUserEquip(playerId, backpack, baseId, binding);
			if(userEquip != null) {
				userEquips.add(userEquip);
			}
		}
		return userEquips;
	}
	
	/**
	 * 新创建指定星级的装备
	 * 
	 * @param  playerId				角色ID
	 * @param  backpack				背包号
	 * @param  baseId				基础ID
	 * @param  binding				绑定状态
	 * @param  starLevel			指定的星级
	 * @return {@link UserEquip}	用户装备对象
	 */
	public static UserEquip newUserEquip2Star(long playerId, int backpack, int baseId, boolean binding, int starLevel) {
		UserEquip userEquip = newUserEquip(playerId, backpack, baseId, binding);
		userEquip.setStarLevel(starLevel);
		return refreshEquipStarAttributes(userEquip);
	}
	
	/**
	 * 创建一件装备(默认在背包中)
	 * 
	 * @param  playerId				角色ID
	 * @param  baseId				基础装备ID
	 * @param  binding				创建出来后的绑定状态
	 * @return {@link UserEquip}	基础装备对象
	 */
	public static UserEquip newUserEquip(long playerId, EquipConfig equipConfig, int backpack, boolean binding) {
		int equipId = equipConfig.getId();
		int maxHole = equipConfig.getMaxHole();
		Quality quality = equipConfig.getQualityEnum();
		int maxEndurance = equipConfig.getMaxEndurance();

		UserEquip userEquip = new UserEquip();
		userEquip.setCount(1);
		userEquip.setIndex(-1);
		userEquip.setBaseId(equipId);
		userEquip.setBinding(binding);
		userEquip.setQuality(quality);
		userEquip.setBackpack(backpack);
		userEquip.setPlayerId(playerId);
		userEquip.setCurrentEndure(maxEndurance);
		userEquip.setCurrentMaxEndure(maxEndurance);
		fillShenwuAttributeToEquip(userEquip, equipConfig);
		getInstance().fillHoleAttributeToEquip(userEquip, maxHole);
		userEquip.setExpiration(equipConfig.getExpirateDate(false));
		getInstance().fillBaseAttributeToEquip(userEquip, equipConfig);
		//策划需要新创建的装备, 是不能附加属性的
		//fillAdditionAttributeToEquip(userEquip, equipConfig);
		return userEquip;
	}
	
	/**
	 * 为装备填充神武属性
	 * 
	 * @param userEquip
	 * @param equipConfig
	 */
	public static boolean fillShenwuAttributeToEquip(UserEquip userEquip, EquipConfig equipConfig) {
		if(userEquip == null || equipConfig == null) {
			return false;
		}
		
		boolean result = false;
		String shenwuSwitch = userEquip.getShenwuSwitch();
		if(StringUtils.isBlank(shenwuSwitch)) { //神武开关为空, 则需要校验是否需要开启一阶神武属性
			result = getInstance().updateEquipShenwuAttribute(PropsRule.DEFAULT_SHENWU_ID, userEquip, equipConfig);
		} else { //需要重新计算所有神武属性
			int maxShenwuId = 0;
			Map<Integer, Map<Integer, AttributeVO>> shenwuAttributeMap = userEquip.getShenwuAttributeMap();
			for (Integer shenwuId : new HashSet<Integer>(shenwuAttributeMap.keySet())) {
				maxShenwuId = Math.max(maxShenwuId, shenwuId);
				if(getInstance().updateEquipShenwuAttribute(shenwuId, userEquip, equipConfig)) {
					result = true;
				};
			}
			
			int nextShenwuId = maxShenwuId + 1;
			Map<Integer, ShenwuSwitch> shenwuSwitches = userEquip.getShenwuSwitches();
			ShenwuSwitch maxShenwuSwitch = shenwuSwitches.get(maxShenwuId);
			if(maxShenwuSwitch != null && maxShenwuSwitch.isTempo() && userEquip.validTempoAttributes(maxShenwuId)) { //验证最大的阶是否属性已满
				if(getInstance().updateEquipShenwuAttribute(nextShenwuId, userEquip, equipConfig)) {
					result = true;
				};
			}
		}
		return result;
	}
	
	/**
	 * 更新装备神武属性
	 * 
	 * @param  shenwuId			神武ID
	 * @param  userEquip		用户装备对象
	 * @param  equipConfig		用户装备基础对象
	 * @return {@link Boolean}	
	 */
	private boolean updateEquipShenwuAttribute(int shenwuId, UserEquip userEquip, EquipConfig equipConfig) {
		int job = equipConfig.getJob();
		int level = equipConfig.getLevel();
		int propsType = equipConfig.getPropsType();
		ShenwuConfig shenwuConfig = getInstance().shenwuService.getShenwuConfig(shenwuId, propsType);
		List<ShenwuAttributeConfig> listShenwuAttribute = getInstance().shenwuService.listShenwuAttribute(shenwuId, propsType, job);
		if(shenwuConfig == null || level < shenwuConfig.getLevel()) {
			return false;
		}
		
		Map<Integer, Map<Integer, AttributeVO>> shenwuAttributeMap = userEquip.getShenwuAttributeMap();
		Map<Integer, AttributeVO> shenwuAttributes = shenwuAttributeMap.get(shenwuId);
		if(shenwuAttributes == null || shenwuAttributes.isEmpty()) { //属性不存在
			userEquip.setShenwuTempo(0);
			userEquip.updateTempoShenwuState(shenwuId, false);
			userEquip.updateShenwuSwitches();
		}

		userEquip.newShenwuAttributeVO(shenwuId, listShenwuAttribute);
		userEquip.updateShenwuAttributeMap();
		return true;
	}
	/**
	 * 填充装备附加属性
	 * 
	 * @param userEquip
	 * @param equipConfig
	 */
	protected static void fillAdditionAttributeToEquip(UserEquip userEquip, EquipConfig equipConfig) {
		int level = equipConfig.getLevel();
		List<Integer> additions = equipConfig.getAdditionList();
		int additionCount = equipConfig.getRandomAdditionCount();
		getInstance().fillAdditionAttributeCount2Equip(userEquip, level, additionCount, additions);
	}
	
	/**
	 * 填充附加属性的条数
	 * 
	 * @param userEquip		用户装备对象
	 * @param equipLevel	装备等级
	 * @param addCount		附加属性条数
	 * @param additions		附加属性列表
	 */
	private void fillAdditionAttributeCount2Equip(UserEquip userEquip, int equipLevel, int addCount, List<Integer> additions) {
		if(additions == null || additions.isEmpty()) {
			return;
		}
		
		int index = 1;
		int quality = userEquip.getQuality().ordinal();
		Map<Integer, AttributeVO> attributeMap = userEquip.getAdditionAttributeMap();
		for (int i = 0; i < addCount; i++) {
			WashAttributeConfig washAttribute = getRandomWashAttribute(equipLevel, quality, additions);
			if(washAttribute == null) {
				continue;
			}
			
			int attribute = washAttribute.getAttribute();
			int addAttrValue = washAttribute.getRandomAddValue();
			if(addAttrValue <= 0) {
				continue;
			}
			attributeMap.put(index, AttributeVO.valueOf(index, attribute, addAttrValue));
			index++;
		}
		
		userEquip.updateAdditionAttributeMap();
	}
	
	/**
	 * 获得随机洗练属性对象
	 *  
	 * @param  equipLevel					装备等级	
	 * @param  additions					附加属性列表
	 * @return {@link WashAttributeConfig}	洗练属性配置对象
	 */
	public static WashAttributeConfig getRandomWashAttribute(int equipLevel, int quality, List<Integer> additions) {
		int addition = additions.get(Tools.getRandomInteger(additions.size()));
		WashTypeConfig washType = getInstance().equipConfigService.getRandomAdditionWashType(addition);
		if(washType == null) {
			return null;
		}
		return getInstance().equipConfigService.getWashAttributeConfig(equipLevel, washType.getAttribute(), quality);
	}
	
	/**
	 * 填充基础属性值
	 * 
	 * @param userEquip
	 * @param equipAttribute
	 */
	public void fillBaseAttributeToEquip(UserEquip userEquip, EquipConfig equipConfig) {
		int index = 1;
		Map<Integer, AttributeVO> baseAttrs = userEquip.getBaseAttributeMap();
		for (int[] elements : equipConfig.getAttributeArray()) {
			if(elements[0] > 0 && elements[1] > 0) {
				baseAttrs.put(index, AttributeVO.valueOf(index, elements[0], elements[1]));
				index ++;
			}
		}
		userEquip.updateBaseAttributeMap();
	}
	
	/**
	 * 填充孔属性值
	 * 
	 * @param userEquip		用户装备对象
	 * @param maxHole		最大孔数
	 */
	private void fillHoleAttributeToEquip(UserEquip userEquip, int maxHole) {
		for (int index = 1; index <= maxHole; index++) {
			userEquip.getHoleInfos().put(index, HoleInfo.valueOf(index, -1));
		}
		userEquip.updateHoleAttributeMap();
	}
	
	/**
	 * 刷新装备的附加的星级属性
	 * 
	 * @param userEquip			用户装备对象	
	 * @param equipConfig		基础装备对象
	 * @param equipStarConfig	装备升星属性对象	
	 */
	public static UserEquip refreshEquipStarAttributes(UserEquip userEquip) {
		return getInstance().reclaculateBaseAttributes(userEquip);
	}

	/**
	 * 重新计算装备的星级属性
	 * 
	 * @param userEquip
	 */
	private UserEquip reclaculateBaseAttributes(UserEquip userEquip) {
		int index = 1;
		userEquip.getBaseAttributeMap().clear();
		int starLevel = userEquip.getStarLevel() - 1;
		EquipConfig equipConfig = userEquip.getEquipConfig();
		if(equipConfig == null) {
			return userEquip;
		}
		
		EquipStarConfig equipStar = getInstance().propsManager.getEquipStarConfig(starLevel);
		for (int[] elements : equipConfig.getAttributeArray()) {
			int attribute = elements[0];
			int attrValue = elements[1];
			if(attribute > 0 && attrValue > 0) {
				if(equipStar != null) {
					attrValue = equipStar.getStarAttrValue(attrValue);
				}
				AttributeVO attributeVO = AttributeVO.valueOf(index, attribute, attrValue);
				userEquip.getBaseAttributeMap().put(attributeVO.getId(), attributeVO);
				index++;
			}
		}
		userEquip.updateBaseAttributeMap();
		return userEquip;
	}
	
	/**
	 * 刷新装备的升阶属性
	 * 
	 * @param userEquip				用户装备对象
	 * @param equipConfig			用户装备基础对象
	 * @param isAscentSuccess		是否升星成功
	 * @param hasSafeItem			是否有保护符
	 * @param luckyCount			使用幸运晶的数量
	 */
	public static boolean refreshEquipRankAttributes(UserEquip userEquip, EquipRankConfig equipRank, boolean hasSafeItem, int luckyCount) {
		if(equipRank.isAscentRankSuccess(luckyCount)) { //升阶成功
			userEquip.resetShenwuAttribute();
			userEquip.setBaseId(equipRank.getNextEquipId());
			EquipConfig nextRankEquip = getInstance().propsManager.getEquipConfig(equipRank.getNextEquipId());
			userEquip.setCurrentEndure(nextRankEquip.getMaxEndurance());
			userEquip.setCurrentMaxEndure(nextRankEquip.getMaxEndurance());
			getInstance().reclaculateBaseAttributes(userEquip);
			EquipHelper.fillShenwuAttributeToEquip(userEquip, nextRankEquip);
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("装备:[{}] 升阶成功", userEquip.getId());
			}
			return true;
		} else { //升阶失败
			if(!hasSafeItem) {
				getInstance().recalculateEquipStar(userEquip);
				getInstance().reclaculateBaseAttributes(userEquip);
				getInstance().recalculateEquipAddition(userEquip);
				getInstance().refreshAdditionAttributes(userEquip);
			}
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("装备:[{}] 升阶失败", userEquip.getId());
			}
			return false;
		}
	}

	/**
	 * 重新计算装备的星级
	 * 
	 * @param userEquip		用户装备对象
	 */
	private void recalculateEquipAddition(UserEquip userEquip) {
		Map<Integer, AttributeVO> additions = userEquip.getAdditionAttributeMap();
		EquipRankRuleConfig equipRankRule = equipConfigService.getRandomEquipRankRule(additions.size(), false);
		if(equipRankRule == null) {
			LOGGER.error("当前星级:[{}] 装备升阶规则对象不存在", additions.size());
			return;
		}
		
		//移除装备的附加属性条数
		int decreaseAddition = equipRankRule.getAddition();	//装备剩余的属性条数.
		List<Integer> indexList = new ArrayList<Integer>(additions.keySet());
		while(decreaseAddition > 0 && indexList.size() > decreaseAddition) {
			int ranIndex = Tools.getRandomInteger(indexList.size());
			Integer removeAttributeIndex = indexList.remove(ranIndex);
			if(removeAttributeIndex != null) {
				additions.remove(removeAttributeIndex);
			}
		}
	}

	/**
	 * 重新计算装备的星级
	 * 
	 * @param userEquip		用户装备对象
	 */
	private void recalculateEquipStar(UserEquip userEquip) {
		int currentStarLevel = userEquip.getStarLevel();
		EquipRankRuleConfig equipRankRule = equipConfigService.getRandomEquipRankRule(currentStarLevel, true);
		if(equipRankRule != null) {
			int decreaseStarLevel = equipRankRule.getStar();
			userEquip.setStarLevel(Math.max(0, currentStarLevel - decreaseStarLevel));
		}
	}
	
	/**
	 * 刷新装备的升阶属性
	 * 
	 * @param userEquip				用户装备对象
	 * @param equipConfig			用户装备基础对象
	 */
	public static void refreshAmuletEquipRankAttributes(UserEquip userEquip, EquipConfig nextRankEquip) {
		int maxEndurance = nextRankEquip.getMaxEndurance();
		userEquip.setCurrentEndure(maxEndurance);
		userEquip.setBaseId(nextRankEquip.getId());
		userEquip.setCurrentMaxEndure(maxEndurance);
		getInstance().reclaculateBaseAttributes(userEquip);
	}
	
	/**
	 * 刷新装备的附加属性
	 * 
	 * @param userEquip		用户装备对象
	 */
	private void refreshAdditionAttributes(UserEquip userEquip) {
		int index = 1;
		Map<Integer, AttributeVO> additionAttributeMap = new HashMap<Integer, AttributeVO>(userEquip.getAdditionAttributeMap());
		userEquip.getAdditionAttributeMap().clear();
		for (AttributeVO attributeVO : additionAttributeMap.values()) {
			attributeVO.setId(index);
			userEquip.getAdditionAttributeMap().put(index, attributeVO);
			index++;
		}
		userEquip.updateAdditionAttributeMap();
	}

	/**
	 * 刷新装备精炼的附加属性
	 * 
	 * @param  userEquip	用户装备对象
	 */
	public static void refreshRefineEquipAddition(UserEquip userEquip, EquipConfig equipConfig) {
		int count = 0;
		int equipLevel = equipConfig.getLevel();
		int quality = userEquip.getQuality().ordinal();
		List<Integer> additions = equipConfig.getAdditionList();
		Map<Integer, AttributeVO> attributeMap = userEquip.getAdditionAttributeMap();
		while(++count < 20 && attributeMap.size() < 8) {
			WashAttributeConfig washAttribute = getRandomWashAttribute(equipLevel, quality, additions);
			if(washAttribute != null) {
				int index = attributeMap.size() + 1;
				int attribute = washAttribute.getAttribute();
				int addAttrValue = washAttribute.getRandomAddValue();
				AttributeVO attributeVO = AttributeVO.valueOf(index, attribute, addAttrValue);
				attributeMap.put(attributeVO.getId(), attributeVO);
			}
		}
		getInstance().refreshAdditionAttributes(userEquip);
	}
	
	/**
	 * 刷新装备的附加属性
	 * 
	 * @param userEquip		用户装备对象
	 * @param maxHole		最大的孔数量
	 */
	public static void refreshHoleIndex(UserEquip userEquip, int maxHole) {
		int currIndex = 1;
		Map<Integer, HoleInfo> holeAttributeMap = userEquip.getHoleInfos();
		Map<Integer, HoleInfo> holeAttributeCache = new HashMap<Integer, HoleInfo>(holeAttributeMap);
		holeAttributeMap.clear();
		//先计算有属性的
		for (HoleInfo attributeVO : holeAttributeCache.values()) {
			if(attributeVO.getItemId() > 0) {
				attributeVO.setIndex(currIndex);
				holeAttributeMap.put(currIndex, attributeVO);
				currIndex++;
			}
		}
		 
		//再计算没有属性的
		for (HoleInfo attributeVO : holeAttributeCache.values()) {
			if(attributeVO.getItemId() <= 0) {
				attributeVO.setIndex(currIndex);
				holeAttributeMap.put(currIndex, attributeVO);
				currIndex++;
			}
		}
		userEquip.updateHoleAttributeMap();
	}
}
