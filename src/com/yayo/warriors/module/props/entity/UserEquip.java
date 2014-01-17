package com.yayo.warriors.module.props.entity;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.EquipConfig;
import com.yayo.warriors.basedb.model.ShenwuAttributeConfig;
import com.yayo.warriors.module.props.model.AttributeVO;
import com.yayo.warriors.module.props.model.HoleInfo;
import com.yayo.warriors.module.props.model.ShenwuSwitch;
import com.yayo.warriors.module.props.rule.EquipHelper;
import com.yayo.warriors.type.GoodsType;

/**
 * 用户装备
 * 
 * @author Hyint
 */
@Entity
@Table(name="userEquip")
public class UserEquip extends BackpackEntry {
	private static final long serialVersionUID = -5738344145296637690L;
	
	/** 角色ID */
	private long playerId;

	/** 装备的星级 */
	private int starLevel;
	
	/** 装备当前耐久度 */
	private int currentEndure;
	
	/** 装备当前最大耐久度 */
	private int currentMaxEndure;
	
	//所有的属性下标都是从 1 开始
	/** 装备的基础属性. 属性下标1_属性编号1_属性值1|属性下标2_属性编号2_属性值2|... */
	private String attributes;

	/** 装备的孔属性. 属性下标1_道具ID1|属性下标2_道具ID2|属性下标2_道具ID2|... */
	private String holeAttributes;

	/** 装备的附加属性. 属性下标1_属性编号1_属性值1|属性下标2_属性编号2_属性值2|... */
	private String additionAttributes;
	
	/** 丢弃或者出售的时间 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date discardTime;

	/** 失效时间, 为null则为永久不失效 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date expiration = null;
	
	/** 当前的神武突破进度 */
	private int shenwuTempo = 0;
	
	/** 
	 * 神武阶开关. 有记录的数据, 则表示已开启. 已开启的神武孔才可以进行神武操作.
	 * 
	 * 格式解析: 神武阶1_是否已突破(0-未突破, 1-已突破)_喂养次数|神武阶2_是否已突破(0-未突破, 1-已突破)_喂养次数|...
	 * 
	 * 格式: 1_0_1|2_0_0|....,
	 */
	private String shenwuSwitch = null;
	
	/**
	 * 神武附加属性. 
	 * 
	 * 格式: 神武阶1_附加属性类型1_附加属性值1|神武阶1_附加属性类型2_附加属性值2
	 * 	   |神武阶2_附加属性类型1_附加属性值1|神武阶2_附加属性类型2_附加属性值2...
	 */
	@Lob
	private String shenwuAttributes = null;
	
	/** 装备属性 */
	@Transient
	private transient EquipConfig equipConfig = null;
	
	/** 装备的孔属性集合 */
	@Transient
	private transient Map<Integer, HoleInfo> holeInfos;

	/** 装备的基础属性集合 */
	@Transient
	private transient Map<Integer, AttributeVO> baseAttributeMap;

	/** 装备的附加属性.  */
	@Transient
	private transient Map<Integer, AttributeVO> additionAttributeMap;

	/** 神武开关数组 */
	@Transient
	private transient Map<Integer, ShenwuSwitch> shenwuSwitches = null;
	
	/** 神武属性集合, {神武阶, {神武属性对象, 神武属性对象, 神武属性对象}} */
	@Transient
	private transient Map<Integer, Map<Integer, AttributeVO>> shenwuAttributeMap;
	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public int getCurrentEndure() {
		return currentEndure;
	}

	public void setCurrentEndure(int currentEndure) {
		this.currentEndure = currentEndure;
	}

	public int getCurrentMaxEndure() {
		return currentMaxEndure;
	}

	public void setCurrentMaxEndure(int currentMaxEndure) {
		this.currentMaxEndure = currentMaxEndure;
	}

	public void updateCurrentEndure2Max(){
		this.currentEndure = this.currentMaxEndure;
	}
	
	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.baseAttributeMap = null;
		this.attributes = attributes;
	}

	public String getHoleAttributes() {
		return holeAttributes;
	}

	public void setHoleAttributes(String holeAttributes) {
		this.holeInfos = null;
		this.holeAttributes = holeAttributes;
	}

	@Override
	public void setBaseId(int baseId) {
		this.equipConfig = null;
		super.setBaseId(baseId);
	}

	public Date getDiscardTime() {
		return discardTime;
	}

	public void setDiscardTime(Date discardTime) {
		this.discardTime = discardTime;
	}

	public Date getExpiration() {
		return expiration;
	}

	public void updateExpiration() {
		if(this.expiration != null) {
			return;
		}
	
		EquipConfig equip = this.getEquipConfig();
		if(equip != null) {
			this.expiration = equip.getExpirateDate(true);
		}
		
	}
	
	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	public int getShenwuTempo() {
		return shenwuTempo;
	}

	public void setShenwuTempo(int shenwuTempo) {
		this.shenwuTempo = shenwuTempo;
	}

	/**
	 * 查询基础属性集合
	 * 
	 * @return {@link Map}		基础属性集合
	 */
	public Map<Integer, AttributeVO> getBaseAttributeMap() {
		if(this.baseAttributeMap != null) {
			return this.baseAttributeMap;
		}
		
		synchronized (this) {
			if(this.baseAttributeMap != null) {
				return this.baseAttributeMap;
			}
			
			this.baseAttributeMap = new ConcurrentHashMap<Integer, AttributeVO>(0);
			List<String[]> arrays = Tools.delimiterString2Array(this.attributes);
			if(arrays == null || arrays.isEmpty()) {
				return this.baseAttributeMap;
			}
			
			for (String[] element : arrays) {
				if(element == null || element.length < 3) {
					continue;
				}
				
				int index = Integer.valueOf(element[0]);
				int attribute = Integer.valueOf(element[1]);
				int attrValue = Integer.valueOf(element[2]);
				this.baseAttributeMap.put(index, AttributeVO.valueOf(index, attribute, attrValue));
			}
		}
		return this.baseAttributeMap;
	}

	public EquipConfig getEquipConfig() {
		if(this.equipConfig == null) {
			this.equipConfig = EquipHelper.getEquipConfig(this.getBaseId());
		}
		return this.equipConfig;
	}

	/**
	 * 更新基础属性值
	 */
	public void updateBaseAttributeMap() {
		StringBuilder builder = new StringBuilder();
		Map<Integer, AttributeVO> maps = this.getBaseAttributeMap();
		Collection<AttributeVO> attributeVOList = maps.values();
		for (AttributeVO attributeVO : attributeVOList) {
			builder.append(attributeVO).append(Splitable.ELEMENT_DELIMITER);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		this.attributes = builder.toString();
	}

	/**
	 * 查询装备的附加属性集合
	 * 
	 * @return {@link Map}		附加属性集合
	 */
	public Map<Integer, AttributeVO> getAdditionAttributeMap() {
		if(this.additionAttributeMap != null) {
			return this.additionAttributeMap;
		}
		
		synchronized (this) {
			if(this.additionAttributeMap != null) {
				return this.additionAttributeMap;
			}
			
			this.additionAttributeMap = new ConcurrentHashMap<Integer, AttributeVO>(0);
			List<String[]> arrays = Tools.delimiterString2Array(this.additionAttributes);
			if(arrays == null || arrays.isEmpty()) {
				return this.additionAttributeMap;
			}
			
			for (String[] element : arrays) {
				if(element == null || element.length < 3) {
					continue;
				}
				
				int index = Integer.valueOf(element[0]);
				int attribute = Integer.valueOf(element[1]);
				int attrValue = Integer.valueOf(element[2]);
				this.additionAttributeMap.put(index, AttributeVO.valueOf(index, attribute, attrValue));
			}
		}
		return this.additionAttributeMap;
	}
	
	/**
	 * 更新附加属性值
	 */
	public void updateAdditionAttributeMap() {
		StringBuilder builder = new StringBuilder();
		Map<Integer, AttributeVO> maps = this.getAdditionAttributeMap();
		Collection<AttributeVO> attributeVOList = maps.values();
		for (AttributeVO attributeVO : attributeVOList) {
			builder.append(attributeVO).append(Splitable.ELEMENT_DELIMITER);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		this.additionAttributes = builder.toString();
	}

	/**
	 * 更新基础属性值
	 */
	public void updateHoleAttributeMap() {
		StringBuilder builder = new StringBuilder();
		Map<Integer, HoleInfo> maps = this.getHoleInfos();
		for (HoleInfo attributeVO : maps.values()) {
			builder.append(attributeVO).append(Splitable.ELEMENT_DELIMITER);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		this.holeAttributes = builder.toString();
	}
	
	/**
	 * 查询装备的属性集合
	 * 
	 * @return {@link Map}		基础属性集合
	 */
	public Map<Integer, HoleInfo> getHoleInfos() {
		if(this.holeInfos != null) {
			return this.holeInfos;
		}
		
		synchronized (this) {
			if(this.holeInfos != null) {
				return this.holeInfos;
			}
			
			this.holeInfos = new ConcurrentHashMap<Integer, HoleInfo>(0);
			List<String[]> arrays = Tools.delimiterString2Array(this.holeAttributes);
			if(arrays == null || arrays.isEmpty()) {
				return this.holeInfos;
			}
			
			for (String[] element : arrays) {
				if(element == null || element.length < 2) {
					continue;
				}
				
				int index = Integer.valueOf(element[0]);
				int itemId = Integer.valueOf(element[1]);
				this.holeInfos.put(index, HoleInfo.valueOf(index, itemId));
			}
		}
		return this.holeInfos;
	}
	
	public int getEnchangeCount() {
		int holeCount = 0;
		for (HoleInfo holeInfo : this.getHoleInfos().values()) {
			holeCount += holeInfo.getItemId() > 0 ? 1 : 0;
		}
		return holeCount;
	}
	
	public int getStarLevel() {
		return starLevel;
	}
	
	public void setStarLevel(int starLevel) {
		this.starLevel = starLevel;
	}

	@Override
	public int getGoodsType() {
		return GoodsType.EQUIP;
	}

	public String getAdditionAttributes() {
		return additionAttributes;
	}

	public void setAdditionAttributes(String additionAttributes) {
		this.additionAttributes = additionAttributes;
	}

	public String getShenwuSwitch() {
		return shenwuSwitch;
	}

	public void setShenwuSwitch(String shenwuSwitch) {
		this.shenwuSwitches = null;
		this.shenwuSwitch = shenwuSwitch;
	}

	public String getShenwuAttributes() {
		return shenwuAttributes;
	}

	public void setShenwuAttributes(String shenwuAttributes) {
		this.shenwuAttributeMap = null;
		this.shenwuAttributes = shenwuAttributes;
	}
	
	public void resetShenwuAttribute() {
		this.shenwuTempo = 0;
		this.shenwuSwitch = "";
		this.shenwuAttributes = "";
		this.shenwuSwitches = null;
		this.shenwuAttributeMap = null;
	}
	
	/**
	 * 更新神武开关信息
	 */
	public void updateShenwuSwitches() {
		StringBuffer buffer = new StringBuffer();
		Map<Integer, ShenwuSwitch> switchs = this.getShenwuSwitches();
		for (ShenwuSwitch shenwuSwitch : switchs.values()) {
			buffer.append(shenwuSwitch).append(Splitable.ELEMENT_DELIMITER);
		}
		this.shenwuSwitch = buffer.toString();
	}

	/**
	 * 更新神武开关信息
	 */
	public void updateShenwuAttributeMap() {
		StringBuffer buffer = new StringBuffer();
		Map<Integer, Map<Integer, AttributeVO>> attributeMaps = this.getShenwuAttributeMap();
		for (Map<Integer, AttributeVO> attributeMap : attributeMaps.values()) {
			if(attributeMap == null || attributeMap.isEmpty()) {
				continue;
			}
		
			for (AttributeVO attributeVO : attributeMap.values()) {
				buffer.append(attributeVO).append(Splitable.ELEMENT_DELIMITER);
			}
		}
		this.shenwuAttributes = buffer.toString();
	}
	
	/**
	 * 获得神武开关
	 * 
	 * @return {@link Map}
	 */
	public Map<Integer, ShenwuSwitch> getShenwuSwitches() {
		if(this.shenwuSwitches != null) {
			return this.shenwuSwitches;
		}
		
		synchronized (this) {
			if(this.shenwuSwitches != null) {
				return this.shenwuSwitches;
			}
			
			this.shenwuSwitches = new HashMap<Integer, ShenwuSwitch>();
			List<String[]> arrays = Tools.delimiterString2Array(shenwuSwitch);
			if(arrays == null || arrays.isEmpty()) {
				return this.shenwuSwitches;
			}
			
			for (String[] elements : arrays) {
				if(elements == null) {
					continue;
				} 

				if(elements.length >= 3) {
					Integer shenwuId = Integer.valueOf(elements[0]);
					Integer switchFlag = Integer.valueOf(elements[1]);
					Integer shenwuCount = Integer.valueOf(elements[2]);
					this.shenwuSwitches.put(shenwuId, ShenwuSwitch.valueOf(shenwuId, switchFlag >= 1, shenwuCount));
				}
			}
		}
		return this.shenwuSwitches;
	}
	
	public void updateTempoShenwuState(int shenwuId, boolean status) {
		Map<Integer, ShenwuSwitch> switchs = this.getShenwuSwitches();
		ShenwuSwitch switchObject = switchs.get(shenwuId);
		if(switchObject == null) {
			switchObject = ShenwuSwitch.valueOf(shenwuId, false, 0);
			switchs.put(shenwuId, switchObject);
		}
		switchObject.setTempo(status);
	}

	public void addTempoShenwuState(int shenwuId, int count) {
		Map<Integer, ShenwuSwitch> switchs = this.getShenwuSwitches();
		ShenwuSwitch switchObject = switchs.get(shenwuId);
		switchObject.setShenwuCount(switchObject.getShenwuCount() + count);
	}
	
	/**
	 * 更新神武属性
	 * 
	 * @param shenwuId
	 * @param shenwuAttributes
	 */
	public void updateShenwuAttributes(int shenwuId, List<ShenwuAttributeConfig> shenwuAttributes) {
		Map<Integer, AttributeVO> attributeVOS = new HashMap<Integer, AttributeVO>();
		ShenwuSwitch shenwuSwitchObject = this.getShenwuSwitches().get(shenwuId);
		int shenwuFeedCount = shenwuSwitchObject != null ? shenwuSwitchObject.getShenwuCount() : 0;
		Map<Integer, Map<Integer, AttributeVO>> attributeMap = this.getShenwuAttributeMap();
		if(shenwuAttributes != null && !shenwuAttributes.isEmpty()) {
			Map<Integer, AttributeVO> attributeVOMap = attributeMap.get(shenwuId);
			for (ShenwuAttributeConfig shenwuAttribute : shenwuAttributes) {
				if(shenwuAttribute.getShenwuId() != shenwuId) {
					continue;
				};
				
				AttributeVO attributeVO = null;
				int attribute = shenwuAttribute.getAttribute();
				if(attributeVOMap != null) {
					attributeVO = attributeVOMap.get(attribute);
				}
				
				int currAttrValue = shenwuAttribute.getAttrValue();
				int maxAttrValue = shenwuAttribute.getMaxAttrValue();
				if(attributeVO == null) { //可能更换了新属性
					int feedValue = Math.min(maxAttrValue, currAttrValue * shenwuFeedCount);
					attributeVO = AttributeVO.valueOf(shenwuId, attribute, feedValue);
				}
				attributeVO.setAttrValue(Math.min(maxAttrValue,  attributeVO.getAttrValue() + currAttrValue));
				attributeVOS.put(attribute, attributeVO);
			}
		}
		attributeMap.put(shenwuId, attributeVOS);
	}
	
	public void newShenwuAttributeVO(int shenwuId, List<ShenwuAttributeConfig> shenwuAttributes) {
		Map<Integer, AttributeVO> attributeVOS = new HashMap<Integer, AttributeVO>();
		ShenwuSwitch shenwuSwitchObject = this.getShenwuSwitches().get(shenwuId);
		int shenwuFeedCount = shenwuSwitchObject != null ? shenwuSwitchObject.getShenwuCount() : 0;
		Map<Integer, Map<Integer, AttributeVO>> attributeMap = this.getShenwuAttributeMap();
		if(shenwuAttributes != null && !shenwuAttributes.isEmpty()) {
			for (ShenwuAttributeConfig shenwuAttribute : shenwuAttributes) {
				if(shenwuAttribute.getShenwuId() != shenwuId) {
					continue;
				}
				
				int attribute = shenwuAttribute.getAttribute();
				int currAttrValue = shenwuAttribute.getAttrValue();
				int maxAttrValue = shenwuAttribute.getMaxAttrValue();
				int feedValue = Math.min(maxAttrValue, currAttrValue * shenwuFeedCount);
				attributeVOS.put(attribute, AttributeVO.valueOf(shenwuId, attribute, feedValue));
			}
		}
		attributeMap.put(shenwuId, attributeVOS);
	}
	/**
	 * 是否可以进阶
	 * 
	 * @param  shenwuId				神武ID
	 * @return {@link Boolean}		true-可以进阶, false-不可以进阶
	 */
	public boolean canTempoShenwu(int shenwuId) {
		Map<Integer, ShenwuSwitch> switches = getShenwuSwitches();
		ShenwuSwitch flag = switches.get(shenwuId);
		return flag != null && !flag.isTempo(); //是否可以进阶
	}

	/**
	 * 验证属性是否已满
	 * 
	 * @param  shenwuAttributes
	 * @return
	 */
	public boolean validTempoAttributes(int shenwuId) {
		Map<Integer, AttributeVO> map = this.getShenwuAttributeMap().get(shenwuId);
		if(map == null || map.isEmpty()) {
			return false;
		}
		
		EquipConfig equipConfig = this.getEquipConfig();
		int equipJob = equipConfig.getJob();
		int equipType = equipConfig.getPropsType();
		for (Entry<Integer, AttributeVO> entry : map.entrySet()) {
			Integer attribute = entry.getKey();
			AttributeVO attributeVO = entry.getValue();
			if(attribute == null || attributeVO == null) {
				return false;
			}
			
			ShenwuAttributeConfig attributeConfig = EquipHelper.getShenwuAttributes(equipType, shenwuId, equipJob, attribute);
			if(attributeConfig == null) {
				return false;
			}
			
			if(attributeVO.getAttrValue() < attributeConfig.getMaxAttrValue()) {
				return false;
			}
			
		}
		return true;
	}
	/**
	 * 获得神武属性集合
	 * 
	 * @return {@link Map}
	 */
	public Map<Integer, Map<Integer, AttributeVO>> getShenwuAttributeMap() {
		if(this.shenwuAttributeMap != null) {
			return this.shenwuAttributeMap;
		}
		
		synchronized (this) {
			if(this.shenwuAttributeMap != null) {
				return this.shenwuAttributeMap;
			}
			
			this.shenwuAttributeMap = new HashMap<Integer, Map<Integer, AttributeVO>>();
			List<String[]> arrays = Tools.delimiterString2Array(this.shenwuAttributes);
			if(arrays == null || arrays.isEmpty()) {
				return this.shenwuAttributeMap;
			}
			
			for (String[] elementArray : arrays) {
				if(elementArray != null && elementArray.length >= 3) {
					Integer shenwuId = Integer.valueOf(elementArray[0]);
					Integer attribute = Integer.valueOf(elementArray[1]);
					Integer attrValue = Integer.valueOf(elementArray[2]);
					Map<Integer, AttributeVO> attributes = this.shenwuAttributeMap.get(shenwuId);
					if(attributes == null) {
						attributes = new HashMap<Integer, AttributeVO>(0);
						this.shenwuAttributeMap.put(shenwuId, attributes);
					}
					attributes.put(attribute, AttributeVO.valueOf(shenwuId, attribute, attrValue));
				}
			}
		}
		return this.shenwuAttributeMap;
	}

	/**
	 * 验证有效时间
	 * 
	 * @return {@link Boolean}	true-可以使用, false-超过时效不可使用
	 */
	public boolean isOutOfExpiration() {
		return this.expiration != null && expiration.getTime() < System.currentTimeMillis();
	}

	public void increaseStarLevel(int addLevel) {
		this.starLevel += addLevel;
	}
}
