package com.yayo.warriors.module.active.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;


import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.Splitable;
import com.yayo.common.utility.Tools;

/**
 * 玩家在线活动领取记录
 * @author liuyuhua
 */
@Entity
@Table(name = "userActive")
public class PlayerActive extends BaseModel<Long>{
	private static final long serialVersionUID = -4225570331911768913L;
	
	@Id
	@Column(name = "playerId")
	private Long id;
	
	/** 排名活动(只能领取一次)
	 * <per>格式: 活动ID_活动ID_活动ID</per>
	 * */
	@Lob
	private String rankActive = "";
	
	/** 冲级活动(可以多领)
	 * <per>格式:活动ID_要求等级|活动ID_要求等级</per>
	 * */
	@Lob
	private String levelActive = "";
	
	/** 兑换活动
	 * <per>格式:活动ID_领取次数|活动ID_领取次数</per>
	 */
	@Lob
	private String exChangeActive = "";
	
	
	/** 排名活动集合 解析{@link PlayerActive#rankActive}*/
	@Transient
	private transient List<Integer> rankActiveList = null;
	
	/** 等级活动集合 解析{@link PlayerActive#levelActive}*/
	@Transient
	private transient List<String> levelActiveList = null;
	
	/** 兑换活动集合 解析{@link PlayerActive#exChangeActive}*/
	@Transient
	private transient Map<Integer,Integer> exChangeMap = null;
	
	
	/**
	 * 构造方法
	 * @param playerId              玩家的ID
	 * @return {@link PlayerActive} 玩家活动对象
	 */
	public static PlayerActive valueOf(long playerId){
		PlayerActive active = new PlayerActive();
		active.id = playerId;
		return active;
	}
	
	/**
	 * 添加兑换次数
	 * @param activeId              活动ID
	 */
	public void addExChange(int activeId){
		Map<Integer,Integer> times = this.getExChange();
		Integer currentTimes = times.get(activeId);
		if(currentTimes == null){
			currentTimes = new Integer(1);
		}else{
			currentTimes += 1;
		}
		times.put(activeId, currentTimes);
		
		//格式化...
		StringBuilder builder = new StringBuilder();
		for(Entry<Integer,Integer> entry : times.entrySet()){
			int id = entry.getKey();
			int count = entry.getValue();
			builder.append(id).append(Splitable.ATTRIBUTE_SPLIT).append(count).append(Splitable.ELEMENT_DELIMITER);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		
		this.exChangeActive = builder.toString();
	}
	
	
	/**
	 * 获取兑换奖励集合
	 * @return {@link Map} 兑换奖励集合
	 */
	public Map<Integer,Integer> getExChange(){
		if(exChangeMap != null){
			return exChangeMap;
		}
		
		synchronized (this) {
			if(exChangeMap != null){
				return exChangeMap;
			}
			
			exChangeMap = new HashMap<Integer, Integer>();
			if(exChangeActive == null || exChangeActive.isEmpty()){
				return exChangeMap;
			}
			
			List<String[]> delimiters = Tools.delimiterString2Array(exChangeActive);
			if(delimiters != null && !delimiters.isEmpty()){
				for(String[] delimiter : delimiters){
					if(delimiter.length < 2){
						continue;
					}
					
					int activeId = Integer.parseInt(delimiter[0]);
					int times = Integer.parseInt(delimiter[1]);
					exChangeMap.put(activeId, times);
				}
			}
			return exChangeMap;
		}
	}
	
	
	/**
	 * 获取等级活动集合
	 * @return {@link List} 等级活动集合
	 */
	public List<String> getLevelActives(){
		if(levelActiveList != null){
			return levelActiveList;
		}
		
		synchronized (this) {
			if(levelActiveList != null){
				return levelActiveList;
			}
			
			levelActiveList = new ArrayList<String>();
			if(levelActive == null || levelActive.isEmpty()){
				return levelActiveList;
			}
			
			String[] splits = levelActive.split(Splitable.ELEMENT_SPLIT);
			for(String split : splits){
				levelActiveList.add(split);
			}
			
			return levelActiveList;
		}
	}
	
	/**
	 * 添加已领取的等级活动
	 * @param levelId     等级活动ID 格式:{活动ID_要求等级}
	 */
	public void addLevelActives(String levelId){
		List<String> levelList = this.getLevelActives();
		levelList.add(levelId);
		
		//格式化...
		StringBuilder builder = new StringBuilder();
		for(String level : levelList){
			builder.append(level).append(Splitable.ELEMENT_DELIMITER);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		
		this.levelActive = builder.toString();
	}
	
	/**
	 * 获取排名活动集合 
	 * @return {@link List} 排名活动集合
	 */
	public List<Integer> getRankActives(){
		if(rankActiveList != null){
			return rankActiveList;
		}
		
		synchronized (this) {
			if(rankActiveList != null){
				return rankActiveList;
			}
			
			rankActiveList = new ArrayList<Integer>();
			if(rankActive == null || rankActive.isEmpty()){
				return rankActiveList;
			}
			String[] splits = rankActive.split(Splitable.ATTRIBUTE_SPLIT);
			for(String split : splits){
				rankActiveList.add(Integer.parseInt(split));
			}
			return rankActiveList;
		}
	}
	
	/**
	 * 添加已经领取的排行活动
	 * @param rankId    排行活动的ID
	 */
	public void addRankActives(int rankId){
		List<Integer> rankList = getRankActives();
		rankList.add(rankId);
		
		//格式化...
		StringBuilder builder = new StringBuilder();
		for(Integer rank : rankList){
			builder.append(rank).append(Splitable.ATTRIBUTE_SPLIT);
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		this.rankActive = builder.toString();
	}
	
	
	public String getRankActive() {
		return rankActive;
	}

	public void setRankActive(String rankActive) {
		this.rankActive = rankActive;
	}

	public String getLevelActive() {
		return levelActive;
	}

	public void setLevelActive(String levelActive) {
		this.levelActive = levelActive;
	}

	public String getExChangeActive() {
		return exChangeActive;
	}

	public void setExChangeActive(String exChangeActive) {
		this.exChangeActive = exChangeActive;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
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
		PlayerActive other = (PlayerActive) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
