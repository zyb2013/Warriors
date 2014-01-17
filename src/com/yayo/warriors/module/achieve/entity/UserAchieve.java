package com.yayo.warriors.module.achieve.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;

@Entity
@Table(name="userAchieve")
public class UserAchieve extends BaseModel<Long> {

	private static final long serialVersionUID = 887692247877956005L;

	@Id
	@Column(name="playerId")
	private Long id;
	
	private String achieves = "";
	
	@Lob
	private String achieved = "";
	
	@Transient
	private transient Set<Integer> achieveIds = null;
	
	@Transient
	private transient Map<Integer, Integer> achieveMap = null;
	
	
	
	public static UserAchieve valueOf(long playerId) {
		UserAchieve achieve = new UserAchieve();
		achieve.id = playerId;
		return achieve;
	}
	
	
	public Set<Integer> getAchieveIds() {
		if (this.achieveIds != null) {
			return this.achieveIds;
		}
		
		synchronized (this) {
			if (this.achieveIds != null) {
				return this.achieveIds;
			}
			this.achieveIds = new HashSet<Integer>();
			if (StringUtils.isBlank(this.achieves)) {
				return this.achieveIds;
			}

			String[] array = this.achieves.split(Splitable.ATTRIBUTE_SPLIT);
			for (String element : array) {
				this.achieveIds.add(Integer.valueOf(element));
			}
		}
		return this.achieveIds;
	}
	
	
	public Map<Integer, Integer> getAchieveMap() {
		if (this.achieveMap != null) {
			return this.achieveMap;
		}
		
		synchronized (this) {
			if (this.achieveMap != null) {
				return this.achieveMap;
			}
			
			this.achieveMap = new HashMap<Integer, Integer>();
			if (StringUtils.isBlank(this.achieved)) {
				return this.achieveMap;
			}
			List<String[]> arrays = Tools.delimiterString2Array(this.achieved);
			for (String[] element : arrays) {
				this.achieveMap.put(Integer.valueOf(element[0]), Integer.valueOf(element[1]));
			}
		}
		return this.achieveMap;
	}
	
	
	public void updateAchieveIds(int achieveId) {
		this.getAchieveIds().add(achieveId);
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.achieves).append(achieveId).append("_");
		this.achieves = buffer.toString();
	}
	
	
	public void put2AchieveMap(int key, int value) {
		Map<Integer, Integer> map = this.getAchieveMap();
		Integer cacheValue = map.get(key);
		if (cacheValue == null) {
			cacheValue = Integer.valueOf(0);
		}
		map.put(key, cacheValue + value);
	}
	
	
	public void updateAchieved() {
		Map<Integer, Integer> map = this.getAchieveMap();
		List<String[]> subArray = new ArrayList<String[]>();
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			String key = String.valueOf(entry.getKey());
			String value = String.valueOf(entry.getValue());
			subArray.add(new String[] {key, value});
		}
		this.achieved = Tools.listArray2DelimiterString(subArray);
	}
	
	
	public int getAchieveParams(int achieveType) {
		Map<Integer, Integer> map = this.getAchieveMap();
		Integer param = map.get(achieveType);
		return param == null ? 0 : param;
	}
	
	
	public boolean isReceived(int achieveId) {
		int key = achieveId * 100;               // id key值
		return this.getAchieveParams(key) > 0;
	}
	
	
//	public void add2RecentAchieve(int achieveId) {
//		if (this.recentAchieve != null) {
//			if (this.recentAchieve.size() == 10) {        // 历史记录10条
//				this.recentAchieve.removeLast();
//			}
//			this.recentAchieve.offerFirst(achieveId);
//			return;
//		} 
//		
//		this.recentAchieve = new LinkedList<Integer>();
//		this.recentAchieve.offerFirst(achieveId);
//	}
//	
//	
//	public void clearRecentList() {
//		this.recentAchieve.clear();
//	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getAchieves() {
		return achieves;
	}

	public void setAchieves(String achieves) {
		this.achieves = achieves;
	}

	public String getAchieved() {
		return achieved;
	}

	public void setAchieved(String achieved) {
		this.achieved = achieved;
	}

//	public LinkedList<Integer> getRecentAchieve() {
//		return recentAchieve;
//	}
	
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
		if (!(obj instanceof UserAchieve))
			return false;
		UserAchieve other = (UserAchieve) obj;
		
		return id != null && other.id != null && id.equals(other.id);
	}



}
