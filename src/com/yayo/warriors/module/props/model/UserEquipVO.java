package com.yayo.warriors.module.props.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Lob;

/**
 * 用户装备VO对象, 给到客户端使用的
 * 
 * @author Hyint
 */
public class UserEquipVO implements Serializable {
	private static final long serialVersionUID = -3143355923816336472L;

	/** 物品ID,自增 */
	private Long id;
	
	/** 基础ID */
	private int baseId;
	
	/** 装备/道具的数量 */
	private int count;
	
	/** 物品在背包的位置 */
	private int index = -1;

	/** 物品类型. 道具/装备 */
	private int goodsType = -1;
	
	/** 装备所在的背包 */
	private int backpack;

	/** 角色ID */
	private long playerId;

	/** 装备的星级 */
	private int starLevel;
	
	/** 装备当前耐久度 */
	private int currentEndure;
	
	/** 装备当前最大耐久度 */
	private int currentMaxEndure;
	
	/** 装备的基础属性. 属性下标1_属性编号1_属性值1|属性下标2_属性编号2_属性值2|... */
	private String attributes;

	/** 装备的孔属性. 属性下标1_道具ID_属性编号1_属性值1|属性下标2_道具ID_属性编号2_属性值2|... */
	private String holeAttributes;

	/** 装备的附加属性. 属性下标1_属性编号1_属性值1|属性下标2_属性编号2_属性值2|... */
	private String additionAttributes;
	
	/** 是否已绑定 */
	private boolean binding = false;
	
	/** 失效时间, 为null则为永久不失效 */
	private Date expiration = null;
	
	/** 当前的神武突破进度 */
	private int shenwuTempo = 0;
	
	/** 
	 * 神武阶开关. 有记录的数据, 则表示已开启. 已开启的神武孔才可以进行神武操作.
	 * 
	 * 格式解析: 神武阶1_是否已突破(0-未突破, 1-已突破)|神武阶2_是否已突破(0-未突破, 1-已突破)|...
	 * 
	 * 格式: 1_1|2_0|....,
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
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(int goodsType) {
		this.goodsType = goodsType;
	}

	public int getBackpack() {
		return backpack;
	}

	public void setBackpack(int backpack) {
		this.backpack = backpack;
	}

	public boolean isBinding() {
		return binding;
	}

	public void setBinding(boolean binding) {
		this.binding = binding;
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

	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public int getStarLevel() {
		return starLevel;
	}

	public void setStarLevel(int starLevel) {
		this.starLevel = starLevel;
	}

	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

	public String getHoleAttributes() {
		return holeAttributes;
	}

	public void setHoleAttributes(String holeAttributes) {
		this.holeAttributes = holeAttributes;
	}

	public String getAdditionAttributes() {
		return additionAttributes;
	}

	public void setAdditionAttributes(String additionAttributes) {
		this.additionAttributes = additionAttributes;
	}

	public int getShenwuTempo() {
		return shenwuTempo;
	}

	public void setShenwuTempo(int shenwuTempo) {
		this.shenwuTempo = shenwuTempo;
	}

	public String getShenwuSwitch() {
		return shenwuSwitch;
	}

	public void setShenwuSwitch(String shenwuSwitch) {
		this.shenwuSwitch = shenwuSwitch;
	}

	public String getShenwuAttributes() {
		return shenwuAttributes;
	}

	public void setShenwuAttributes(String shenwuAttributes) {
		this.shenwuAttributes = shenwuAttributes;
	}

	@Override
	public String toString() {
		return "UserEquipVO [id=" + id + ", baseId=" + baseId + ", count=" + count + ", index="
				+ index + ", goodsType=" + goodsType + ", backpack=" + backpack + ", playerId="
				+ playerId + ", starLevel=" + starLevel + ", currentEndure=" + currentEndure
				+ ", currentMaxEndure=" + currentMaxEndure + ", attributes=" + attributes
				+ ", holeAttributes=" + holeAttributes + ", additionAttributes="
				+ additionAttributes + ", binding=" + binding + ", expiration=" + expiration
				+ ", shenwuTempo=" + shenwuTempo + ", shenwuSwitch=" + shenwuSwitch
				+ ", shenwuAttributes=" + shenwuAttributes + "]";
	}
}