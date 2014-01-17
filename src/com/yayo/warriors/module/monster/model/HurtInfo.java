package com.yayo.warriors.module.monster.model;

import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.type.ElementType;

public class HurtInfo implements Comparable<HurtInfo> {

	/** 玩家编号 */
	private ISpire spire;
	/** 最近一次伤害 */
	private int lastHurt;
	/** 总伤害值 */
	private int totalhurt;
	/** 仇恨值 */
	private int threat;
	/** 最近一次使用的技能 */
	private int skillId;

	public HurtInfo(ISpire spire, int totalhurt) {
		super();
		this.spire = spire;
		this.totalhurt = totalhurt;
	}
	
	public ElementType getElementType(){
		return this.spire.getType() ;
	}
	
	public ISpire getSpire() {
		return spire;
	}

	public int getLastHurt() {
		return lastHurt;
	}

	public int getTotalhurt() {
		return totalhurt;
	}

	public int getThreat() {
		return threat;
	}

	public int getSkillId() {
		return skillId;
	}
	
	
	public int compareTo(HurtInfo o) {
		int totalhurt2 = o.getTotalhurt();
		if(this.totalhurt > totalhurt2){
			return -1;
			
		} else if(this.totalhurt < totalhurt2){
			return 1;
			
		}
		
		return 0;
	}

	public void addInfo(int hurt, int skillId) {
		synchronized (this) {
			this.lastHurt = hurt;
			this.totalhurt += hurt;
			this.skillId = skillId;
		}
	}

	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((spire == null) ? 0 : spire.hashCode());
		return result;
	}

	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HurtInfo other = (HurtInfo) obj;
		if (spire == null) {
			if (other.spire != null)
				return false;
		} else if (!spire.equals(other.spire))
			return false;
		return true;
	}
	
}
