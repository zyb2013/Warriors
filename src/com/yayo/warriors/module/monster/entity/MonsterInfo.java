package com.yayo.warriors.module.monster.entity;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.yayo.common.db.model.BaseModel;
import com.yayo.common.utility.NumberUtil;
import com.yayo.common.utility.Splitable;

/**
 * 怪物信息
 * @author jonsai
 *
 */
@Entity
@Table(name="monsterInfo")
public class MonsterInfo extends BaseModel<Integer> {
	private static final long serialVersionUID = 2543066370584711966L;
	
	/** 分线 */
	@Id
	private Integer branch;

	/** 野外BOSS复活信息， 格式:monsterConfigId_终结者_复活时间|... */
	@Lob
	private String resurrection;
	
	@Transient
	private ConcurrentMap<Integer, DeathInfo>  resurrectionMap = null;
	
	@Override
	public Integer getId() {
		return branch;
	}

	@Override
	public void setId(Integer id) {
		this.branch = id;
	}

	public String getResurrection() {
		return resurrection;
	}

	public void setResurrection(String resurrection) {
		this.resurrection = resurrection;
	}
	
	public void recordResurrection(int monsterConfigId, long monsterKiller, long reviveTime){
		getResurrectionMap().put(monsterConfigId, DeathInfo.valueOf(monsterKiller, reviveTime) );
		refreshToInfo();
	}

	private void refreshToInfo() {
		synchronized (this) {
			StringBuilder str = new StringBuilder(); 
			for(Entry<Integer, DeathInfo> entry : resurrectionMap.entrySet()){
				DeathInfo deathInfo = entry.getValue();
				str.append(Splitable.ELEMENT_DELIMITER).append(entry.getKey()).append(Splitable.ATTRIBUTE_SPLIT).append(deathInfo.monsterKiller).append(Splitable.ATTRIBUTE_SPLIT).append(deathInfo.reviveTime);
			}
			
			if(str.length() > 0){
				str.deleteCharAt(0);
			}
			this.resurrection = str.toString();
		}
	}
	
	public void removeResurrection(int monsterConfigId){
		getResurrectionMap().remove(monsterConfigId);
		refreshToInfo();
	}
	
	public ConcurrentMap<Integer, DeathInfo> getResurrectionMap() {
		if(resurrectionMap == null){
			synchronized (this) {
				if(resurrectionMap == null){
					resurrectionMap = new ConcurrentHashMap<Integer, DeathInfo>(8);
				}
				
				if(StringUtils.isNotBlank(this.resurrection)){
					String[] infos = this.resurrection.split(Splitable.ELEMENT_SPLIT);
					for(String info : infos){
						String[] values = info.split(Splitable.ATTRIBUTE_SPLIT);
						if(values != null && values.length >= 3){
							resurrectionMap.put(Integer.valueOf(values[0]), DeathInfo.valueOf( Long.valueOf(values[1]), Long.valueOf(values[2]) ) );
						}
					}
				}
			}
		}
		return resurrectionMap;
	}
	
	public static class DeathInfo{
		/** 终结者 */
		public long monsterKiller;
		
		/** 复活时间 */
		public long reviveTime;
		
		public static DeathInfo valueOf(long playerId, long reviveTime){
			DeathInfo deathInfo = new DeathInfo();
			deathInfo.monsterKiller = playerId;
			deathInfo.reviveTime = reviveTime;
			return deathInfo;
		}
	}
}
