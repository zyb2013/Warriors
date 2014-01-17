package com.yayo.warriors.module.title.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.yayo.warriors.module.title.model.TitleType;

@Entity
@Table(name="playerTitle")
public class PlayerTitle extends BaseModel<Long> {
	private static final long serialVersionUID = 6812842558095060188L;
	/** 玩家编号*/
	@Id
	@Column(name="playerId")
	private long id ;
	/** 已经获得的称号*/
	private String gainTitles ;
	/** 称号参数*/
	@Lob
	private String titleParams ;
	
	/** 已获得称号 缓存*/
	@Transient
	private transient volatile Set<Integer> gainedTitlesCache;
	
	/** 参数缓存	NAME_VALUE|NAME_VALUE*/
	@Transient
	private transient volatile Map<String, String> cacheParamsMap ;
	
	
	public PlayerTitle() {}
	
	
	/**
	 * 修改值
	 * @param titleType 称号类型
	 * @param param	参数
	 */
	public void alterParam(TitleType titleType, String param) {
		Set<Integer> gainedTitles = this.getGainedTItleCache();
		if(gainedTitles.contains(titleType.getId())) {
			return;
		}
		
		Map<String, String> cacheParam = this.getCacheParam();
		cacheParam.put(titleType.name(), param);
	}

	/**
	 * 初始化称号数组
	 */
	public Set<Integer> getGainedTItleCache() {
		if(this.gainedTitlesCache != null) {
			return this.gainedTitlesCache;
		}
		
		synchronized (this) {
			if(this.gainedTitlesCache != null) {
				return this.gainedTitlesCache;
			}
			
			this.gainedTitlesCache = new HashSet<Integer>();
			if(StringUtils.isBlank(this.gainTitles)) {
				return this.gainedTitlesCache;
			}
			
			String[] gainedTitlesCacheSwap = this.gainTitles.split(Splitable.ATTRIBUTE_SPLIT);
			for (String element : gainedTitlesCacheSwap) {
				if(!StringUtils.isBlank(element)) {
					this.gainedTitlesCache.add(Integer.parseInt(element));
				}
			}
		}
		return this.gainedTitlesCache;
	}

	/**
	 * 把缓存转成字符串
	 */
	private void updateParamCacheToString() {
		StringBuffer builder = new StringBuffer() ;
		Map<String, String> cacheParam = this.getCacheParam();
		for (Entry<String, String> entry : cacheParam.entrySet()) {
			builder.append(entry.getKey()).append(Splitable.ATTRIBUTE_SPLIT);
			builder.append(entry.getValue()).append(Splitable.ELEMENT_DELIMITER);
		}
		this.titleParams = builder.toString() ;
	}

	/**
	 * 初始化缓存
	 */
	public Map<String, String> getCacheParam() {
		if(this.cacheParamsMap != null) {
			return this.cacheParamsMap;
		}
		
		synchronized (this) {
			if(this.cacheParamsMap != null) {
				return this.cacheParamsMap;
			}
			
			this.cacheParamsMap = new HashMap<String, String>(0);
			List<String[]> paramsArrays = Tools.delimiterString2Array(this.titleParams);
			if(paramsArrays == null || paramsArrays.isEmpty()) {
				return this.cacheParamsMap;
			}
			
			for(String[] keyValue : paramsArrays){
				if(!StringUtils.isBlank(keyValue[0]) && !StringUtils.isBlank(keyValue[1])) {
					this.cacheParamsMap.put(keyValue[0], keyValue[1]);
				}
			}
		}
		return this.cacheParamsMap;
	}
	
	/**
	 * 检查是否获得称号 ， 返回true 即为获得，需要保存  
	 * 1,2,3,4
	 * @param titleType
	 * @param titleConfig
	 * @return
	 */
	public boolean checkObtainTitle(TitleType titleType) {
		Set<Integer> gainedTitles = this.getGainedTItleCache();
		if(gainedTitles.contains(titleType.getId())) {
			return false;
		}

		Map<String, String> cacheParam = this.getCacheParam();
		String value = cacheParam.get(titleType.name());
		if(value == null) {
			return false ;
		}
		
		if(titleType.checkObtainTitle(value)) {
			gainedTitles.add(titleType.getId());
			this.updateGainTitleCache();
			this.updateParamCacheToString();
			return true ;
		}
		cacheParam.remove(titleType.name());
		return false;
	}
	
	/**
	 * 更新称号缓存
	 */
	public void updateGainTitleCache() {
		StringBuffer buffer = new StringBuffer();
		Set<Integer> gainedTitles = this.getGainedTItleCache();
		for (Integer title : gainedTitles) {
			buffer.append(title).append(Splitable.ATTRIBUTE_SPLIT);
		}
		this.gainTitles = buffer.toString();
	}

	public String getGainTitles() {
		return gainTitles;
	}

	public void setGainTitles(String gainTitles) {
		this.gainedTitlesCache = null;
		this.gainTitles = gainTitles;
	}

	public String getTitleParams() {
		return titleParams;
	}

	public void setTitleParams(String titleParams) {
		this.cacheParamsMap = null;
		this.titleParams = titleParams;
	}
	
	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id ;
	}
	
	/**
	 * 构建称号实体
	 * 
	 * @param  playerId				角色ID
	 * @return {@link PlayerTitle}	称号实体
	 */
	public static PlayerTitle valueOf(long playerId) {
		PlayerTitle entity = new PlayerTitle();
		entity.id = playerId;
		return entity;
	}
}
