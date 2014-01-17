package com.yayo.warriors.module.horse.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Splitable;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.Flushable;

/**
 * 坐骑实体
 * @author liuyuhua
 */
@Entity
@Table(name = "userHorse")
public class Horse extends BaseModel<Long> {
	private static final long serialVersionUID = 4260944662351020016L;
	
	@Id
	@Column(name = "playerId")
	private Long id;
	
	/** 等级*/
	private int level;
	
	/** 当前经验值*/
	private int exp;
	
	/** 所拥有过的皮肤模型 格式:模型1_模型2_模型3*/
	@Lob
	private String models = "";
	
	/** 模型(外观)*/
	@Transient
	private transient volatile int model;
	
	/** 是否骑乘*/
	@Transient
	private transient volatile boolean riding = false;
	
	/** 坐骑的属性. */
	@Transient
	private transient Fightable attributes = new Fightable();
	
	/** 刷新状态 */
	@Transient
	private transient volatile int flushable = Flushable.FLUSHABLE_NORMAL;
	
	/** 坐骑模型 解析{@link Horse#models}*/
	@Transient
	private transient Set<Integer> modelList = null;
	
	/**
	 * 构造方法
	 * @param playerId   玩家的ID
	 * @param model      模型的ID
	 * @return
	 */
	public static Horse valueOf(long playerId,int model){
		Horse horse = new Horse();
		horse.id = playerId;
		horse.model = model;
		horse.exp = 0; //坐骑默认经验值
		horse.level = 1; //坐骑默认等级
		return horse;
	}
	
	/**
	 * 是否能够使用该模型
	 * @param mount 模型的ID
	 * @return true 可以使用 false 不能使用
	 */
	public boolean canUseModel(int mount){
		return this.getHorseMount().contains(mount);
	}
	
	/**
	 * 获取已拥有的坐骑模型
	 * @return 坐骑模型集合
	 */
	public Set<Integer> getHorseMount(){
		if(modelList != null){
			return modelList;
		}
		
		synchronized (this) {
			if(modelList != null){
				return modelList;
			}
			
			modelList = new HashSet<Integer>();
			if(models == null || models.isEmpty()){
				return modelList;
			}
			
			String[] mounts = models.split(Splitable.ATTRIBUTE_SPLIT);
			for(String mount : mounts){
				modelList.add(Integer.parseInt(mount));
			}
			
			return modelList;
		}
	}
	
	/**
	 * 增加坐骑外怪
	 * @param mounts 模型的ID
	 */
	public void addHorseMounts(Collection<Integer> mounts){
		if(mounts == null || mounts.isEmpty()){
			return;
		}
		Set<Integer> modelList = this.getHorseMount();
		modelList.addAll(mounts);
		this.builderModels(modelList);
	}
	
	/**
	 * 增加坐骑的外观(模型)
	 * @param model 模型的ID
	 */
	public void addHorseMount(int model){
		Set<Integer> modelList = this.getHorseMount();
		if(!modelList.contains(model)){
			modelList.add(model);
			this.builderModels(modelList);
		}
	}
	
	/**
	 * 构建存储模型
	 * @param modellist 模型类表
	 */
	private void builderModels(Collection<Integer> modellist){
		if(modellist == null || modellist.isEmpty()){
			return;
		}
		
		StringBuilder builder = new StringBuilder();
		for(Integer model : modellist){
			builder.append(model).append(Splitable.ATTRIBUTE_SPLIT);
		}
		
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		
		this.models = builder.toString();
	}
	
	
	//Getter and Setter...
	
	/**
	 * 设置属性值
	 * @param attributeKey {@link AttributeKeys}属性键值
	 * @param value 属性值
	 */
	public void setAttribute(int attributeKey,int value){
		this.attributes.put(attributeKey, value);
	}
	
	/**
	 * 获取属性
	 * @param attributeKey {@link AttributeKeys}属性键值
	 * @return {@link Integer} 属性值
	 */
	public int getAttribute(int attributeKey) {
		return this.attributes.getAttribute(attributeKey);
	}
	
	/**
	 * 是否要重算属性
	 * @return true 需要刷新 , false 不需刷新
	 */
	public boolean isFlushable(){
		return this.flushable != Flushable.FLUSHABLE_NOT;
	}
	
	/**
	 * 上马
	 */
	public void onRiding(){
		this.riding = true;
	}
	
	/**
	 * 下马
	 */
	public void offRiding(){
		this.riding = false;
	}
	
	//Getter and Setter...

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getModel() {
		return model;
	}

	public void setModel(int model) {
		this.model = model;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public boolean isRiding() {
		return riding;
	}

	public void setRiding(boolean riding) {
		this.riding = riding;
	}
	
	public int getFlushable() {
		return flushable;
	}

	public void setFlushable(int flushable) {
		this.flushable = flushable;
	}

	public Fightable getAttributes() {
		return attributes;
	}

	public void setAttributes(Fightable attributes) {
		this.attributes = attributes;
	}

	public String getModels() {
		return models;
	}

	public void setModels(String models) {
		this.models = models;
	}

	public void increaseExp(int addExp) {
		this.exp += addExp;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Horse other = (Horse) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Horse [id=" + id + ", level=" + level + ", exp=" + exp + "]";
	}
}
