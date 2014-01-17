package com.yayo.warriors.module.fight.model;

import java.util.HashMap;
import java.util.Map;

import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.fight.type.FightCasting;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.module.user.model.Fightable;
import com.yayo.warriors.module.user.model.StatusElement;
import com.yayo.warriors.module.user.type.AttributeKeys;
import com.yayo.warriors.module.user.type.StatusType;


/**
 * 战斗属性对象
 * 
 * @author Hyint
 */
public class FightAttribute {

	/** 域模型属性 */
	private ISpire iSpire;

	/** 抗击飞 */
	private boolean knockFlyDefense = false;

	/** 抗击退 */
	private boolean knockBackDefense = false;
	
	/** 抗捉取 */
	private boolean garbbingDefense = false;
	
	/** 技能等级 */
	private Fightable skillable = new Fightable();
	
	/** 总属性列表, 用于计算战斗 */
	private Fightable attributes = new Fightable();

	/** 战斗单位施法模式. 是物理伤害/法术伤害*/
	private FightCasting fightCasting = FightCasting.PHYSICAL;

	/** 本身身上的BUFF. { 效果ID, Buffer对象 } */
	private Map<Integer, Buffer> buffers = new HashMap<Integer, Buffer>(0);
	
	/** 角色的状态信息 */
	private Map<StatusType, StatusElement> statusCache = new HashMap<StatusType, StatusElement>(0);
	
	public UnitId getUnitId() {
		return iSpire.getUnitId();
	}

	public Fightable getAttributes() {
		return attributes;
	}

	public Map<Integer, Buffer> getBuffers() {
		return buffers;
	}

	public Fightable getSkillable() {
		return skillable;
	}
	
	public FightCasting getFightCasting() {
		return fightCasting;
	}

	public void setFightCasting(FightCasting fightCasting) {
		this.fightCasting = fightCasting;
	}

	public ISpire getiSpire() {
		return iSpire;
	}

	/**
	 * 构建角色属性对象
	 * 
	 * @param  unitId	 			属性ID
	 * @param  casting				施法类型
	 * @return {@link IAttribute}	属性接口
	 */
	public static FightAttribute newInstance(ISpire iSpire, FightCasting casting) {
		FightAttribute attribute = new FightAttribute();
		attribute.iSpire = iSpire;
		attribute.fightCasting = casting;
		return attribute;
	}

	public void removeBuffer(int bufferId) {
		this.buffers.remove(bufferId);
	}
	
	public void addBuffer(Buffer buffer) {
		if(buffer != null) {
			this.buffers.put(buffer.getId(), buffer);
		}
	}
	
	/**
	 * 角色是否死亡
	 * 
	 * @return {@link Boolean} true-已死亡, false-未死亡
	 */
	public boolean isDead() {
		return this.getAttributes().getAttribute(AttributeKeys.HP) <= 0;
	}
	
	public Map<StatusType, StatusElement> getStatusCache() {
		return statusCache;
	}

	public void setStatusCache(Map<StatusType, StatusElement> statusCache) {
		this.statusCache = statusCache;
	}

	public boolean hasElementStatus(StatusType statusType) {
		StatusElement statusElement = this.statusCache.get(statusType);
		return statusElement != null && !statusElement.isTimeOut();
	}

	public void removeElementStatus(StatusType statusType) {
		this.statusCache.remove(statusType);
	}

	public void addElementStatus(StatusElement statusElement) {
		if(statusElement != null) {
			this.statusCache.put(statusElement.getType(), statusElement);
		}
	}
	
	public boolean isKnockFlyDefense() {
		return knockFlyDefense;
	}

	public void setKnockFlyDefense(boolean knockFlyDefense) {
		this.knockFlyDefense = knockFlyDefense;
	}

	public boolean isKnockBackDefense() {
		return knockBackDefense;
	}

	public void setKnockBackDefense(boolean knockBackDefense) {
		this.knockBackDefense = knockBackDefense;
	}

	public boolean isGarbbingDefense() {
		return garbbingDefense;
	}

	public void setGarbbingDefense(boolean garbbingDefense) {
		this.garbbingDefense = garbbingDefense;
	}

	@Override
	public String toString() {
		return "FightAttribute [iSpire=" + iSpire + ", knockFlyDefense=" + knockFlyDefense
				+ ", knockBackDefense=" + knockBackDefense + ", garbbingDefense=" + garbbingDefense
				+ ", skillable=" + skillable + ", attributes=" + attributes + ", fightCasting="
				+ fightCasting + ", buffers=" + buffers + ", statusCache=" + statusCache + "]";
	}
	
	
}
