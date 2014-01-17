package com.yayo.warriors.module.user.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 角色的战斗属性
 * 
 * @author Hyint
 */
public class Fightable implements Map<Object, Integer>, Serializable {
	private static final long serialVersionUID = 6241460604743996037L;
	
	/** 角色的属性 */
	private Map<Object, Integer> attributes = null;
	
	public Fightable() {
		attributes = new HashMap<Object, Integer>();
	}

	public Fightable(int initSize) {
		attributes = new HashMap<Object, Integer>(initSize);
	}
	
	
	/**
	 * 清除所有属性
	 */
	public void clear() {
		attributes.clear();
	}

	/**
	 * 是否包含属性
	 * 
	 * @param key 	属性KEY
	 */
	public boolean containsKey(Object key) {
		return attributes.containsKey(key);
	}

	/**
	 * 获取属性键值对 [属性KEY -> 属性值]
	 */
	public Set<Entry<Object, Integer>> entrySet() {
		return attributes.entrySet();
	}

	/**
	 * 获取属性值
	 * 
	 * @param key 	属性KEY
	 */
	public Integer getAttribute(Object key) {
		if (key == null) {
			return 0;
		}
		Integer value = attributes.get(key);
		return value == null ? 0 : value;
	}
	
	
	/**
	 * 属性值是否为空
	 */
	public boolean isEmpty() {
		return attributes.isEmpty();
	}

	/**
	 * 获取所有键值KEY
	 */
	public Set<Object> keySet() {
		return attributes.keySet();
	}

	public Integer put(Object key, Integer value) {
		Integer result = attributes.put(key, value);
		return result == null ? 0 : result;
	}

	/**
	 * 设置属性值
	 * 
	 * @param key 		属性KEY
	 * @param value 	属性值
	 */
	public int set(Object key, int value) {
		return this.put(key, value);
	}

	/**
	 * 增加属性值
	 * 
	 * @param key 		属性KEY
	 * @param value 	属性值
	 */
	public int add(Object attKey, int attValue) {
		int total = this.getAttribute(attKey);
		total += attValue;
		return set(attKey, total);
	}
	
	/**
	 * 增加属性值
	 * 
	 * @param attach 			[属性KEY -> 属性值]集合	
	 * @param addition			属性加成比率
	 */
	public void addAll(Fightable attach, double addition) {
		for(Entry<Object, Integer> e : attach.entrySet()) {
			Object key = e.getKey();
			Integer value = e.getValue();
			if(key != null && value != null && value > 0) {
				this.put(key, (int)(value * addition));
			}
		}
	}

	/**
	 * 增加属性值
	 * 
	 * @param attach 			[属性KEY -> 属性值]集合	
	 * @param addition			属性加成比率
	 */
	public void addAll(Fightable attach) {
		for(Entry<Object, Integer> e : attach.entrySet()) {
			Object key = e.getKey();
			Integer value = e.getValue();
			if(key != null && value != null && value > 0) {
				Integer oldValue = this.getAttribute(key);
				if(oldValue == null || oldValue == 0) {
					this.put(key, value);
				} else {
					this.put(key, value + oldValue);
				}
			}
		}
	}

	/**
	 * 增加属性值
	 * 
	 * @param attach 			[属性KEY -> 属性值]集合	
	 * @param addition			属性加成比率
	 */
	public void addAll(ConcurrentFightable attach) {
		for(Entry<Object, Integer> e : attach.entrySet()) {
			Object key = e.getKey();
			Integer value = e.getValue();
			if(key != null && value != null && value > 0) {
				Integer oldValue = this.getAttribute(key);
				if(oldValue == null || oldValue == 0) {
					this.put(key, value);
				} else {
					this.put(key, value + oldValue);
				}
			}
		}
	}

	/**
	 * 设置属性值
	 * 
	 * @param map
	 *            [属性KEY -> 属性值]集合
	 */
	public void putAll(Map<? extends Object, ? extends Integer> map) {
		attributes.putAll(map);
	}
	
	/**
	 * 移除属性
	 * 
	 * @param 属性KEY
	 */
	public Integer remove(Object key) {
		return attributes.remove(key);
	}

	/**
	 * 属性集合大小
	 */
	public int size() {
		return attributes.size();
	}

	
	public String toString() {
		return this.attributes.toString();
	}
	
	public Map<Object, Integer> getAttributes() {
		return this.attributes;
	}
	
	
	public boolean containsValue(Object value) {
		return this.attributes.containsValue(value);
	}

	
	public Integer get(Object key) {
		return this.getAttribute(key);
	}

	
	public Collection<Integer> values() {
		return this.attributes.values();
	}

}
