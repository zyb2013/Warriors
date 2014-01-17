
package com.yayo.warriors.module.buffer.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.DatePattern;
import com.yayo.common.utility.DateUtil;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.buffer.model.Buffer;


@Entity
@Table(name="userBuffer")
public class UserBuffer extends BaseModel<Long> {
	private static final long serialVersionUID = -3150583066351797820L;
	
	@Id
	@Column(name="playerId")
	private Long id;
	
	@Lob
	private String skillBuffers = "";
	
	@Lob
	private String skillDebuffers = "";
	
	@Lob
	private String itemBuffers = "";
	
	@Transient
	private transient volatile Map<Integer, Buffer> bufferInfos = null;

	@Transient
	private transient volatile Map<Integer, Buffer> debufferInfos = null;
	
	@Transient
	private transient volatile Map<Integer, Buffer> itemBufferInfos = null;
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getSkillBuffers() {
		return skillBuffers;
	}

	public void setSkillBuffers(String skillBuffers) {
		this.skillBuffers = skillBuffers;
	}
	
	public String getSkillDebuffers() {
		return skillDebuffers;
	}

	public void setSkillDebuffers(String skillDebuffers) {
		this.skillDebuffers = skillDebuffers;
	}

	/**
	 * 获得 Buffer 信息
	 * 
	 * @return {@link Map}	
	 */
	public Map<Integer, Buffer> getBufferInfos() {
		if(this.bufferInfos == null) {
			synchronized (this) {
				if(this.bufferInfos == null) {
					this.bufferInfos = buffer2Map(this.skillBuffers);
				}
			}
		}
		return this.bufferInfos;
	}
	
	/**
	 * 检查集合是否有Buffer超时
	 * 
	 * @param  buffers			{@link Buffer} 集合
	 * @return {@link Boolean}	true-有超时, false-没有超时的
	 */
	public boolean hasTimeOut(Map<Integer, Buffer> buffers) {
		if(buffers == null || buffers.isEmpty()) {
			return false;
		}
		
		Set<Entry<Integer, Buffer>> entrySet = buffers.entrySet();
		for (Entry<Integer, Buffer> entry : entrySet) {
			Buffer value = entry.getValue();
			if(value != null && value.isTimeOut()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isBufferEmpty() {
		return this.getBufferInfos().isEmpty();
	}
	
	public boolean isDeBufferEmpty() {
		return this.getDeBufferInfos().isEmpty();
	}

	public boolean isItemBufferEmpty() {
		return this.getItemBufferInfos().isEmpty();
	}
	
	public Map<Integer, Buffer> getItemBufferInfos() {
		if(this.itemBufferInfos == null) {
			synchronized (this) {
				if(this.itemBufferInfos == null) {
					itemBufferInfos = buffer2Map(this.itemBuffers);
				}
			}
		}
		return itemBufferInfos;
	}
	
	/**
	 * 字符串转换Buffer Map集合
	 * 
	 * @param  infos			字符串信息. 
	 * @return {@link Map}		集合对象
	 */
	private Map<Integer, Buffer> buffer2Map(String infos) {
		Map<Integer, Buffer> maps = new ConcurrentHashMap<Integer, Buffer>(1);
		List<String[]> arrays = Tools.delimiterString2Array(infos);
		if(arrays == null || arrays.isEmpty()) {
			return maps;
		}
		
		for (String[] element : arrays) {
			if(element == null || element.length < 5) {
				continue;
			}
			
			//效果ID1_技能等级1_伤害值1_结束时间1(单位:年月日时分秒)
			Integer effectId = Integer.valueOf(element[0]);
			Integer skillLevel = Integer.valueOf(element[1]);
			Integer damageValue = Integer.valueOf(element[2]);
			Integer bufferCycle = Integer.valueOf(element[3]);
			String pattern = DatePattern.PATTERN_YYYYMMDDHHMMSS;
			Date endTime = DateUtil.string2Date(element[4], pattern);
			long castId = element.length >= 6 ? Long.valueOf(element[5]) : -1L;
			int unitType = element.length >= 6 ? Integer.valueOf(element[6]) : -1;
			if(endTime == null || endTime.getTime() <= System.currentTimeMillis()) {
				continue;
			}
			
			Buffer buffer = Buffer.valueOf(effectId, skillLevel, damageValue, bufferCycle, endTime.getTime(), castId, unitType);
			if(!buffer.isTimeOut()) {
				maps.put(effectId, buffer);
			}
			
		}
		return maps;
	}
	
	/**
	 * 获得 Buffer 信息
	 * 
	 * @return {@link Map}	
	 */
	public Map<Integer, Buffer> getDeBufferInfos() {
		if(this.debufferInfos == null) {
			synchronized (this) {
				if(this.debufferInfos == null) {
					debufferInfos = buffer2Map(this.skillDebuffers);
				}
			}
		}
		return this.debufferInfos;
	}
	
	public String getItemBuffers() {
		return itemBuffers;
	}

	public void setItemBuffers(String itemBuffers) {
		this.itemBuffers = itemBuffers;
	}

	/**
	 * 查询并拷贝BUFF集合
	 *  
	 * @return {@link Map}
	 */
	public Map<Integer, Buffer> getAndCopyBufferMap() {
		Map<Integer, Buffer> bufferMaps = new HashMap<Integer, Buffer>(5);
		bufferMaps.putAll(this.getBufferInfos());
		bufferMaps.putAll(this.getDeBufferInfos());
		bufferMaps.putAll(this.getItemBufferInfos());
		return bufferMaps;
	}

	/**
	 * 重置BUFFER信息
	 */
	public void resetBufferInfos(boolean resetAll) {
		if(resetAll) {
			this.bufferInfos = null;
			this.debufferInfos = null;
		}
	}
	
	/** 更新BUFF列表 */
	public void updateBufferInfos(boolean resetMap) {
		this.skillBuffers = buffer2String(this.getBufferInfos());
		if(resetMap) {
			this.bufferInfos = null;
		}
	}

	/** 更新道具BUFF列表 */
	public void updateItemBufferInfos(boolean resetMap) {
		this.itemBuffers = buffer2String(this.getItemBufferInfos());
		if(resetMap) {
			this.itemBufferInfos = null;
		}
	}
	
	/**
	 * 构建Buffer字符串
	 * 
	 * @param  maps
	 * @return {@link String}	
	 */
	private String buffer2String(Map<Integer, Buffer> maps) {
		StringBuilder builder = new StringBuilder();
		if(maps == null || maps.isEmpty()) {
			return builder.toString();
		}
		
		for (Iterator<Entry<Integer, Buffer>> it = maps.entrySet().iterator(); it.hasNext();) {
			Entry<Integer, Buffer> entry = it.next();
			Integer effectId = entry.getKey();
			Buffer buffer = entry.getValue();
			if(effectId != null && buffer != null && !buffer.isTimeOut()) {
				builder.append(buffer.toString()).append(Splitable.ELEMENT_DELIMITER);
			} else {
				it.remove();
			}
		}
		return builder.toString();
	}

	public void removeUserBuffer(int bufferId, boolean isBuffer) {
		if(isBuffer) {
			Map<Integer, Buffer> infos = this.getBufferInfos();
			if(infos != null && !infos.isEmpty()) {
				infos.remove(bufferId);
			}
		} else {
			Map<Integer, Buffer> infos = this.getDeBufferInfos();
			if(infos != null && !infos.isEmpty()) {
				infos.remove(bufferId);
			}
		}
	}
	
	/**
	 * 增加BUFF对象
	 * 
	 * @param isBuffer		true-Buffer, false-Debuffer
	 * @param buffer		{@link Buffer}对象
	 */
	public void addBuffer(boolean isBuffer, Buffer buffer) {
		if(buffer != null) {
			int bufferId = buffer.getId();
			if(isBuffer) {
				this.getBufferInfos().put(bufferId, buffer);
			} else {
				this.getDeBufferInfos().put(bufferId, buffer);
			}
		}
	}
	
	/** 更新BUFF列表 */
	public void updateDeBufferInfos(boolean resetMap) {
		this.skillDebuffers = buffer2String(this.getDeBufferInfos());
		if(resetMap) {
			this.debufferInfos = null;
		}
	}
	
	@Override
	public String toString() {
		return "UserBuffer [id=" + id + ", skillBuffers=" + skillBuffers + ", skillDebuffers=" + skillDebuffers + "]";
	}

	/**
	 * 构建用户Buffer对象
	 * 
	 * @param  playerId				角色ID
	 * @return {@link UserBuffer}	用户Buffer对象
	 */
	public static UserBuffer valueOf(long playerId) {
		UserBuffer userBuffer = new UserBuffer();
		userBuffer.id = playerId;
		return userBuffer;
	}
	
	public boolean isAllBufferEmpty() {
		return getBufferInfos().isEmpty() && getDeBufferInfos().isEmpty() && this.getItemBufferInfos().isEmpty();
	}
}
