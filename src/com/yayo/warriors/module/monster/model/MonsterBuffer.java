package com.yayo.warriors.module.monster.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.yayo.warriors.module.buffer.model.Buffer;

/**
 * 怪物的BUFF对象
 * 
 * @author Hyint
 */
public class MonsterBuffer {

	/** 身上的BUFF集合*/
	private Map<Integer, Buffer> bufferInfos = new ConcurrentHashMap<Integer,Buffer>(3);
	
	/** 身上的DEBUFF集合 */
	private Map<Integer, Buffer> debufferInfos = new ConcurrentHashMap<Integer,Buffer>(3);
	
	/**
	 * 移除buffer效果
	 * @param key
	 */
	public boolean removeBuffer(int key, boolean isBuffer){
		if(isBuffer) {
			return this.bufferInfos.remove(key) != null;
		} else {
			return this.debufferInfos.remove(key) != null;
		}
	}
	
	/**
	 * 获取所有buffer效果
	 * @return
	 */
	public Map<Integer,Buffer> getBufferInfoMap(){
		return this.bufferInfos;
	}
 
	/**
	 * 添加Buffer效果
	 * 
	 * @param buffer		BUFFER对象
	 * @param isBuffer		true-BUFF, false-
	 */
	public boolean addBuffer(Buffer buffer, boolean isBuffer){
		if(buffer == null) {
			return false;
		}
		
		if(isBuffer) {
			bufferInfos.put(buffer.getId(), buffer);
		} else {
			debufferInfos.put(buffer.getId(), buffer);
		}
		return true;
	}
	
	/**
	 * 清除所有buffer效果
	 */
	public void clearAllBuffer(){
		bufferInfos.clear() ;
		debufferInfos.clear() ;
	}
	
	/**
	 * 获取所有debuffer信息
	 * @return
	 */
	public Map<Integer, Buffer> getDebufferInfoMap(){
		return this.debufferInfos;
	}
	
	public List<Buffer> getAndCopyBufferInfo() {
		return new ArrayList<Buffer>(this.getBufferInfoMap().values());
	}
	
	/**
	 * 获取Buffer信息
	 * 
	 * @param  bufferId		BUFFID
	 * @param  isBuffer		true-BUFF, false-DEBUFF
	 * @return
	 */
	public Buffer getBufferInfoByKey(int bufferId, boolean isBuffer) {
		if(isBuffer) {
			return this.bufferInfos.get(bufferId);
		} else {
			return this.debufferInfos.get(bufferId);
		}
	}
	
	public boolean isAllBufferEmpty() {
		return isBufferEmpty() && this.isDeBufferEmpty();
	}
	
	public boolean isBufferEmpty() {
		return this.bufferInfos.isEmpty();
	}

	public boolean isDeBufferEmpty() {
		return this.debufferInfos.isEmpty();
	}
	
	public Set<Integer> removeAllBufferIds() {
		Set<Integer> bufferIds = new HashSet<Integer>();
		Map<Integer, Buffer> bufferInfoMap = this.getBufferInfoMap();
		Map<Integer, Buffer> debufferInfoMap = this.getDebufferInfoMap();
		bufferIds.addAll(bufferInfoMap.keySet());
		bufferIds.addAll(debufferInfoMap.keySet());
		bufferInfoMap.clear();
		debufferInfoMap.clear();
		return bufferIds;
	}
}
