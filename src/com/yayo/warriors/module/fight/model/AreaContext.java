package com.yayo.warriors.module.fight.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.yayo.warriors.module.map.domain.ISpire;

/**
 * 区域列表上下文
 * 
 * @author Hyint
 */
public class AreaContext {

	/** 本次攻击可以看到战斗信息的角色列表 */
	private Collection<ISpire> viewPlayerSpire = new HashSet<ISpire>();
	
	/**
	 * {技能特效ID, 战斗单位ID列表}
	 */
	private Map<Integer, List<ISpire>> fightUnits = new HashMap<Integer, List<ISpire>>();
	
	public void addISpireFightUnits(int skillEffectId, ISpire iSpire) {
		if(iSpire != null) {
			List<ISpire> list = fightUnits.get(skillEffectId);
			if(list == null) {
				list = new java.util.ArrayList<ISpire>();
				fightUnits.put(skillEffectId, list);
			}
			list.add(iSpire);
			addViewPlayerISpire(iSpire);
		}
	}

	public void addISpireFightUnits(int skillEffectId, Collection<ISpire> iSpires) {
		if(iSpires != null && !iSpires.isEmpty()) {
			List<ISpire> list = fightUnits.get(skillEffectId);
			if(list == null) {
				list = new java.util.ArrayList<ISpire>();
				fightUnits.put(skillEffectId, list);
			}
			list.addAll(iSpires);
			viewPlayerSpire.addAll(iSpires);
		}
	}
	
	public void addViewPlayerISpire(ISpire iSpire) {
		if(iSpire != null) {
			viewPlayerSpire.add(iSpire);
		}
	}
	
	public void addViewPlayerISpire(Collection<ISpire> iSpires) {
		if(iSpires != null && !iSpires.isEmpty()) {
			for (ISpire spire : iSpires) {
				addViewPlayerISpire(spire);
			}
		}
	}

	public Collection<ISpire> getViewPlayerSpire() {
		return viewPlayerSpire;
	}

	public Map<Integer, List<ISpire>> getFightUnits() {
		return fightUnits;
	}
}
