
package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.props.type.EquipType;

/**
 * 基础装备对象
 * 
 * @author Hyint
 */
@Resource
public class EquipConfig extends GoodsParent {

	/** 基础装备ID */
	@Id
	private int id;
	
	/** 穿着装备的性别. -1-无性别区分, 0-男性, 1-女性 */
	private int sex;
	
	/** 指定的套装ID */
	private int suitId = 0;
	
	/** 装备的模型ID */
	private int modelId = 0;

	/** 装备的最大孔数 */
	private int maxHole = 0;
	
	/** 装备基础属性1 */
	private int attribute1 = 0;
	
	/** 装备基础属性值1 */
	private int attrValue1 = 0;
	
	/** 装备基础属性2 */
	private int attribute2 = 0;
	
	/** 装备基础属性值2 */
	private int attrValue2 = 0;
	
	/** 装备基础属性3 */
	private int attribute3 = 0;
	
	/** 装备基础属性值3 */
	private int attrValue3 = 0;
	
	/** 装备基础属性4 */
	private int attribute4 = 0;
	
	/** 装备基础属性值4 */
	private int attrValue4 = 0;

	/** 装备的最大耐久度 */
	private int maxEndurance = 0;
	
	/** 可以附加的随机属性. 属性_属性_... */
	private String additions = "";

	/** 附加属性条数下限 */
	private int minAddition = 0;

	/** 附加属性条数上限 */
	private int maxAddition = 0;
	
	/** 可以镶嵌的道具子类型. 格式: 子类型1_子类型1_子类型1_子类型1 */
	private String embedProps = "";

	/** 可以附加的随机属性 */
	@JsonIgnore
	private List<Integer> additionList = null;

	/** 可以镶嵌的道具ID列表 */
	@JsonIgnore
	private List<Integer> embedPropsList = null;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSuitId() {
		return suitId;
	}

	public void setSuitId(int suitId) {
		this.suitId = suitId;
	}

	public int getMaxHole() {
		return maxHole;
	}

	public void setMaxHole(int maxHole) {
		this.maxHole = maxHole;
	}

	public int getMaxEndurance() {
		return maxEndurance;
	}

	public void setMaxEndurance(int maxEndurance) {
		this.maxEndurance = maxEndurance;
	}

	public String getEmbedProps() {
		return embedProps;
	}

	public void setEmbedProps(String embedProps) {
		this.embedProps = embedProps;
	}

	public int getModelId() {
		return modelId;
	}

	public void setModelId(int modelId) {
		this.modelId = modelId;
	}

	public String getAdditions() {
		return additions;
	}

	public void setAdditions(String additions) {
		this.additions = additions;
	}

	public int getAttribute1() {
		return attribute1;
	}

	public void setAttribute1(int attribute1) {
		this.attribute1 = attribute1;
	}

	public int getAttrValue1() {
		return attrValue1;
	}

	public void setAttrValue1(int attrValue1) {
		this.attrValue1 = attrValue1;
	}

	public int getAttribute2() {
		return attribute2;
	}

	public void setAttribute2(int attribute2) {
		this.attribute2 = attribute2;
	}

	public int getAttrValue2() {
		return attrValue2;
	}

	public void setAttrValue2(int attrValue2) {
		this.attrValue2 = attrValue2;
	}

	public int getAttribute3() {
		return attribute3;
	}

	public void setAttribute3(int attribute3) {
		this.attribute3 = attribute3;
	}

	public int getAttrValue3() {
		return attrValue3;
	}

	public void setAttrValue3(int attrValue3) {
		this.attrValue3 = attrValue3;
	}

	public int getAttribute4() {
		return attribute4;
	}

	public void setAttribute4(int attribute4) {
		this.attribute4 = attribute4;
	}

	public int getAttrValue4() {
		return attrValue4;
	}

	public void setAttrValue4(int attrValue4) {
		this.attrValue4 = attrValue4;
	}

	public int getMinAddition() {
		return minAddition;
	}

	public void setMinAddition(int minAddition) {
		this.minAddition = minAddition;
	}

	public int getMaxAddition() {
		return maxAddition;
	}

	public void setMaxAddition(int maxAddition) {
		this.maxAddition = maxAddition;
	}

	public int getRandomAdditionCount() {
		return minAddition + Tools.getRandomInteger((maxAddition - minAddition) + 1);
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		EquipConfig other = (EquipConfig) obj;
		return id == other.id;
	}

	/**
	 * 获得装备可以镶嵌的道具列表
	 * 
	 * @return {@link List}
	 */
	public List<Integer> getAdditionList() {
		if(this.additionList != null ) {
			return this.additionList;
		}
		
		synchronized (this) {
			if(this.additionList != null) {
				return this.additionList;
			}
			
			additionList = new ArrayList<Integer>();
			if(StringUtils.isBlank(this.additions)) {
				return this.additionList;
			}
			
			String[] itemIdArray = this.additions.split(Splitable.ATTRIBUTE_SPLIT);
			for (String itemIdStr : itemIdArray) {
				if(!StringUtils.isBlank(itemIdStr)) {
					this.additionList.add(Integer.valueOf(itemIdStr.trim()));
				}
			}
		}
		return additionList;
	}
	
	/**
	 * 获得装备可以镶嵌的道具列表
	 * 
	 * @return {@link List}
	 */
	public List<Integer> getCanEmbadeList() {
		if(this.embedPropsList != null ) {
			return this.embedPropsList;
		}
		
		synchronized (this) {
			if(this.embedPropsList != null) {
				return this.embedPropsList;
			}
			
			embedPropsList = new ArrayList<Integer>();
			if(StringUtils.isBlank(this.embedProps)) {
				return this.embedPropsList;
			}
			
			String[] itemIdArray = this.embedProps.split(Splitable.ATTRIBUTE_SPLIT);
			for (String itemIdStr : itemIdArray) {
				if(!StringUtils.isBlank(itemIdStr)) {
					this.embedPropsList.add(Integer.valueOf(itemIdStr.trim()));
				}
			}
		}
		return embedPropsList;
	}
	
	public boolean isFaction() {
		return getPropsType() == EquipType.FASHION_TYPE;
	}
	
	public boolean isSexFit(int...sexs) {
		return this.sex < 0 ? true : ArrayUtils.contains(sexs, this.sex);
	}
	
	/** 属性集合 */
	private volatile int[][] ATTRIBUTE_ARR = null;
	public int[][] getAttributeArray() {
		if(ATTRIBUTE_ARR == null) {
			ATTRIBUTE_ARR = new int[][] { { this.attribute1, this.attrValue1 }, { this.attribute2, this.attrValue2 }, 
										  { this.attribute3, this.attrValue3 }, { this.attribute4, this.attrValue4 }};
		}
		return ATTRIBUTE_ARR;
	}

	@Override
	public String toString() {
		return "EquipConfig [id=" + id + ", suitId=" + suitId + ", modelId=" + modelId
				+ ", maxHole=" + maxHole + ", attribute1=" + attribute1 + ", attrValue1="
				+ attrValue1 + ", attribute2=" + attribute2 + ", attrValue2=" + attrValue2
				+ ", attribute3=" + attribute3 + ", attrValue3=" + attrValue3 + ", attribute4="
				+ attribute4 + ", attrValue4=" + attrValue4 + ", maxEndurance=" + maxEndurance
				+ ", additions=" + additions + ", minAddition=" + minAddition + ", maxAddition="
				+ maxAddition + ", embedProps=" + embedProps + "]";
	}
	
	/**
	 * 是否可以强化
	 * 
	 * @return {@link Boolean}
	 */
	public boolean canForge() {
		return this.getPropsType() != EquipType.FASHION_TYPE; 
	}
	
}
