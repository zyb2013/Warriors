package com.yayo.warriors.module.mortal.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Tools;

/**
 * 用户肉身实体
 * @author huachaoping
 *
 */
@Entity
@Table(name="userMortalBody")
public class UserMortalBody extends BaseModel<Long>{

	private static final long serialVersionUID = -6268063175560086686L;
	
	/** 主键id */
	@Id
	@Column(name="playerId")
	private Long id;
	
	/**
	 * 已提升的肉身等级
	 * 
	 * 格式: 肉身类型_等级|肉身类型_等级|...
	 */
	@Lob
	private String mortalBody = "";
	
	/** 总肉身等级, 参考各种肉身类型等级之和，主要用于肉身等级排行榜 */
	private Integer mortalLevel = 0;
	
	/** 已提升的肉身集合 */
	@Transient
	private transient volatile Map<Integer, Integer> mortalBodyMap = null;
	
	/**
	 * 取得肉身列表
	 * 
	 * @return {@link ConcurrentHashMap}
	 */
	public Map<Integer, Integer> getMortalBodyMap() {
		if (this.mortalBodyMap != null) {
			return mortalBodyMap;
		}
		
		mortalBodyMap = new ConcurrentHashMap<Integer, Integer>(0);
		if (StringUtils.isBlank(mortalBody)) {
			return mortalBodyMap;
		}
		
		int totalLevel = 0;
		List<String[]> arrays = Tools.delimiterString2Array(mortalBody);
		for (String[] array : arrays) {
			Integer mortalBodyType = Integer.valueOf(array[0]);
			Integer level = Integer.valueOf(array[1]);
			totalLevel += level;
			this.mortalBodyMap.put(mortalBodyType, level);
		}
		this.mortalLevel = totalLevel;
		return mortalBodyMap;
	}
	
	public int getMortalLevel(int type) {
		Map<Integer, Integer> map = this.getMortalBodyMap();
		Integer level = map.get(type);
		return level == null ? 0 : level;
	}
	

	public void putMortalLevel(int type, int level) {
		this.getMortalBodyMap().put(type, level);
	}
	
	
	public void resetMortalMap() {
		this.mortalBodyMap = null;
	}
	
	
	/** 
	 * 更新肉身列表
	 */
	public void updateMortalBodyMap() {
		int totalLevel = 0;
		Map<Integer, Integer> map = this.getMortalBodyMap();
		List<String[]> subArrayList = new ArrayList<String[]>();
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			String[] element = new String[] {String.valueOf(entry.getKey()), String.valueOf(entry.getValue())};
			subArrayList.add(element);
			totalLevel += entry.getValue();
		}
		this.mortalLevel = totalLevel;
		this.mortalBody = Tools.listArray2DelimiterString(subArrayList);	
	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getMortalBody() {
		return mortalBody;
	}

	public void setMortalBody(String mortalBody) {
		this.mortalBody = mortalBody;
	}
	
	public int getMortalLevel() {
		return mortalLevel;
	}

	public void setMortalLevel(int mortalLevel) {
		this.mortalLevel = mortalLevel;
	}

	/**
	 * 构造函数
	 * @param playerId          玩家id
	 * @return
	 */
	public static UserMortalBody valueOf(long playerId) {
		UserMortalBody userMortalBody = new UserMortalBody();
		userMortalBody.id = playerId;
		return userMortalBody;
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
		if (!(obj instanceof UserMortalBody))
			return false;
		UserMortalBody other = (UserMortalBody) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	/**
	 * 获得肉身的最大等级
	 * 
	 * @return {@link Integer}	肉身的最大等级
	 */
	public int getMaxLevel() {
		int maxLevel = 0;
		Map<Integer, Integer> maps = this.getMortalBodyMap();
		if(maps != null && !maps.isEmpty()) {
			for (Integer level : maps.values()) {
				maxLevel = Math.max(maxLevel, level);
			}
		}
		return maxLevel;
	}
	
	public String toString() {
		return "UserMortalBody [id=" + id + "mortalBody=" + mortalBody + "]";	
	}
}
