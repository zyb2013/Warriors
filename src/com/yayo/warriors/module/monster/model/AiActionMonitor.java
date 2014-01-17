package com.yayo.warriors.module.monster.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yayo.common.utility.Splitable;

/**
 * 
 * @author haiming
 *怪物技能控制者
 */
public class AiActionMonitor {

	/** 疲劳值记录*/
	private Map<String,Long> tiredMap = new ConcurrentHashMap<String,Long>(5);
	/** 技能控制*/
	private List<SkillMonitor> skillMonitorList = new ArrayList<SkillMonitor>(3);
	
	/**
	 * 构造器
	 * @param skillStr
	 */
	public AiActionMonitor(String skillStr){
		if(skillStr == null){
			return ;
		}
		String[] skillInfos = skillStr.split(Splitable.ELEMENT_SPLIT);
		for(String skillInfo : skillInfos){
			skillMonitorList.add(new SkillMonitor(skillInfo));
		}
	}
	
	public AiActionMonitor(){}
	
	/**
	 * 取得技能
	 * @param currentHpPercent
	 * @return
	 */
	public SkillMonitor getSkillByIdPercent(int currentHpPercent) {
		for(SkillMonitor skillMonitor : skillMonitorList){
			if(this.isOverTired( String.valueOf(skillMonitor.getSkillId()) ) && skillMonitor.hitContidion(currentHpPercent)){
				return skillMonitor;
			}
		}
		return null;
	}
	
	/**
	 * 添加疲劳值
	 * @param clazz
	 * @param ss 毫秒
	 * @return	true:有返回值
	 */
	public boolean addTired(String key , long ss) {
		return tiredMap.put(key, ss + System.currentTimeMillis()) == null;
	}
	
	/**
	 * 疲劳值是否已过期
	 * @param clazz
	 * @return
	 */
	public boolean isOverTired(String key){
		Long tired = tiredMap.get(key);
		if(tired == null){
			return true ;
		}else if(System.currentTimeMillis() >= tired){
//			tiredMap.remove(key);
			return  true;
		}
		return false;
	}
	
	public long getTiredTime(String key){
		Long tired = tiredMap.get(key);
		return tired == null ? 0 : tired;
	}

	/**
	 * 是否疲劳
	 * @param key
	 * @return
	 */
	public boolean isHasTired(String key) {
		return tiredMap.containsKey(key);
	}

	/**
	 * 移除怪物指定疲劳记录
	 * @param key
	 */
	public boolean removeTired(String key){
		return tiredMap.remove(key) != null;
	}
	
	/**
	 * 清空怪物的疲劳记录
	 */
	public void removeAllTired(){
		tiredMap.clear() ;
	}
}
