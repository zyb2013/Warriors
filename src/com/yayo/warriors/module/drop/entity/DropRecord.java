package com.yayo.warriors.module.drop.entity;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;

/**
 * 掉落记录信息
 * 
 * @author Hyint
 */
@Entity
@Table(name="dropRecord")
public class DropRecord extends BaseModel<Long> {
	private static final long serialVersionUID = -349593340518846549L;
	public static final long GLOBAL_DROP_ID = 0L;
	
	/**
	 * 奖励记录信息.
	 */
	@Id
	private Long id;
	
	/** 
	 * 掉落记录日期.
	 * 
	 *  {@link Calendar.DAY_OF_YEAR}
	 */
	private int day;
	
	/**
	 * 掉落信息.格式: 掉落ID(Integer)_已掉落个数(Integer)|掉落ID(Integer)|...
	 */
	@Lob
	private String dropInfo = "";

	/** 掉落信息对象 */
	@Transient
	private transient volatile Map<Integer, Element> dropElements = null;
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getDropInfo() {
		return dropInfo;
	}

	public void setDropInfo(String dropInfo) {
		this.dropElements = null;
		this.dropInfo = dropInfo;
	}
	
	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	/**
	 * 掉落记录元素
	 */
	public static class Element {
		private int dropCount = 0;
		private int rewardId = -1;
		
		public int getDropCount() {
			return dropCount;
		}

		public void setDropCount(int dropCount) {
			this.dropCount = dropCount;
		}

		public int getRewardId() {
			return rewardId;
		}

		public void setRewardId(int rewardId) {
			this.rewardId = rewardId;
		}

		public static Element valueOf(String[] arrays) {
			try {
				Element entity = new Element();
				entity.rewardId = Integer.parseInt(arrays[0]);
				entity.dropCount = Integer.parseInt(arrays[1]);
				return entity;
			} catch (Exception ex) {
				return null;
			}
		}

		public static Element valueOf(int rewardId) {
			try {
				Element entity = new Element();
				entity.rewardId = rewardId;
				return entity;
			} catch (Exception ex) {
				return null;
			}
		}

		@Override
		public String toString() {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(this.rewardId);
			stringBuilder.append(Splitable.ATTRIBUTE_SPLIT);
			stringBuilder.append(this.dropCount);
			return stringBuilder.toString();
		}
	}
	
	// -------------------- Getter / Setter --------------------

	/**
	 * 已记录的掉落集合
	 */
	private Map<Integer, Element> getDrops() {
		if (this.dropElements != null) {
			return this.dropElements;
		}

		synchronized (this) {
			if(this.dropElements != null) {
				return this.dropElements;
			}
			
			this.dropElements = new ConcurrentHashMap<Integer, Element>(5);
			List<String[]> arrays = Tools.delimiterString2Array(this.dropInfo);
			if(arrays != null && !arrays.isEmpty()) {
				for (String[] array : arrays) {
					Element element = Element.valueOf(array);
					if(element != null) {
						this.dropElements.put(element.rewardId, element);
					}
				}
			}
		}
		return this.dropElements;
	}
	
	/**
	 * 获取已奖励数量
	 * 
	 * @return {@link Collection}	已奖励ID列表
	 */
	public synchronized Collection<Integer> getDropRecords() {
		return this.getDrops().keySet();
	}

	/**
	 * 获取已奖励数量
	 * 
	 * @param  rewardId 	 	奖励ID
	 * @return {@link Integer}	获得已掉落的数量
	 */
	public synchronized int getDropCount(int rewardId) {
		Element element = this.getDrops().get(rewardId);
		return element == null ? 0 : element.getDropCount();
	}

	/**
	 * 增加奖励数量
	 * 
	 * @param rewardId 		奖励ID
	 * @param addCount 		已奖励数量
	 */
	public void addDropCount(int rewardId, int addCount) {
		Element element = this.getDrops().get(rewardId);
		if(element == null) {
			element = Element.valueOf(rewardId);
			this.getDrops().put(rewardId, element);
		}
		element.setDropCount(element.getDropCount() + addCount);
	}

	/**
	 * 刷新INFO
	 */
	public void updateInfo(){
		StringBuilder builder = new StringBuilder();
		Map<Integer, Element> drops = this.getDrops();
		for(Entry<Integer, Element> entry : drops.entrySet()){
			Integer rewardId = entry.getKey();
			Element element = entry.getValue();
			if(rewardId != null && element != null) {
				builder.append(element).append(Splitable.ELEMENT_DELIMITER);
			}
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		this.dropInfo = builder.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dropInfo == null) ? 0 : dropInfo.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public static DropRecord valueOf(long playerId) {
		DropRecord record = new DropRecord();
		record.id = playerId;
		record.dropInfo = "";
		return record;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!super.equals(obj)) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		}
		
		DropRecord other = (DropRecord) obj;
		return id != null && other.id != null && this.id.equals(other.id);
	}
}
