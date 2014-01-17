package com.yayo.warriors.module.fight.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.domain.ISpire;
import com.yayo.warriors.type.ElementType;

/**
 * 战斗上下文信息
 * 
 * @author Hyint
 */
public class Context {

	/** 角色释放技能的CDID */
	private int coolTimeId;

	/** 释放者的坐标 */
	private Point casterPoint;

	/** 目标的坐标 */
	private Point targetPoint;
	
	/** 地图对象 */
	private GameMap gameMap;

	/** 是否暴击. true-暴击, false-未暴击 */
	private boolean critical;
	
	/** 对于成员ID */
	private Set<Long> teamMemberIds = new HashSet<Long>(0);
	
	/** 死亡的战斗单位列表 */
	private Set<ISpire> fightDeadUnits = new HashSet<ISpire>(0);
	
	/** 角色战斗单位 */
	private Set<ISpire> playerFightings = new HashSet<ISpire>(0);
	
	/** 伤害量统计. 仅仅计算伤害值. { 战斗单位对象, 扣除的血 } */
	private Map<ISpire, Integer> hurtInfo = new HashMap<ISpire, Integer>(0);

	/** 战斗报告列表 */
	private List<FightReport> fightReports = new ArrayList<FightReport>(0);
	

	/** 命中目标 */
	private Map<UnitId, Boolean> hitTargets = new HashMap<UnitId, Boolean>(0);
	
	/** 改变坐标的单位ID(坐标改变主要包括: 冲锋打击, 擒拿手, 闪现) */
	private Map<ISpire, Point> changePointUnits = new HashMap<ISpire, Point>();
	
	/** 战斗单元增加的属性 */
	private Map<UnitId, List<ChangeBuffer>> changeBuffers = new HashMap<UnitId, List<ChangeBuffer>>(0);
	
	/** 攻击者属性发生变化 */
	private Map<UnitId, Map<Integer, Integer>> attributeChanges = new HashMap<UnitId, Map<Integer, Integer>>(0);

	public Point getCasterPoint() {
		return casterPoint;
	}

	public void setCasterPoint(Point casterPoint) {
		this.casterPoint = casterPoint;
	}

	public void setTargetPoint(Point targetPoint) {
		this.targetPoint = targetPoint;
	}

	public Point getTargetPoint() {
		return targetPoint;
	}

	public List<FightReport> getFightReports() {
		return fightReports;
	}
	
	public void addFightReport(FightReport fightReport) {
		this.fightReports.add(fightReport);
	}
	
	public void addMissFightReport(UnitId unitId, int skillId) {
		this.hitTargets.put(unitId, false);
		this.fightReports.add(FightReport.miss(unitId, skillId));
	}

	public void addImmuneFightReport(UnitId unitId, int skillId) {
		this.hitTargets.put(unitId, false);
		this.fightReports.add(FightReport.immune(unitId, skillId));
	}
	
	public void addFightDeadUnit(ISpire...unitIds) {
		for (ISpire unitId : unitIds) {
			this.fightDeadUnits.add(unitId);
		}
	}

	public Set<ISpire> getFightDeadUnits() {
		return fightDeadUnits;
	}
	
	public Map<UnitId, List<ChangeBuffer>> getChangeBuffers() {
		return changeBuffers;
	}

	public Map<UnitId, Map<Integer, Integer>> getAttributeChanges() {
		return attributeChanges;
	}

	public Map<Integer, Integer> getAttributeChanges(UnitId unitId) {
		return attributeChanges.get(unitId);
	}
	
	public void addPlayerFightings(ISpire...unitIds) {
		for (ISpire unitId : unitIds) {
			ElementType type = unitId.getType();
			if(type == ElementType.PLAYER || type == ElementType.PET) {
				this.playerFightings.add(unitId);
			}
		}
	}
	
	public Set<ISpire> getPlayerFightings() {
		return playerFightings;
	}

	public void addAttributeChanges(UnitId unitId, int attribute, int attrValue) {
		Map<Integer, Integer> attrValueMap = this.attributeChanges.get(unitId);
		if (attrValueMap == null) {
			this.attributeChanges.put(unitId, new HashMap<Integer, Integer>(0));
			attrValueMap = this.attributeChanges.get(unitId);
		}

		Integer cacheAttrValue = attrValueMap.get(attribute);
		cacheAttrValue = cacheAttrValue == null ? 0 : cacheAttrValue;
		attrValueMap.put(attribute, cacheAttrValue + attrValue);
	}

	public void addChangeBuffers(UnitId unitId, ChangeBuffer...changeBuffers) {
		List<ChangeBuffer> list = this.changeBuffers.get(unitId);
		if (list == null) {
			this.changeBuffers.put(unitId, new ArrayList<ChangeBuffer>());
			list = this.changeBuffers.get(unitId);
		}

		for (ChangeBuffer changeBuffer : changeBuffers) {
			list.add(changeBuffer);
		}
	}

	public int getCoolTimeId() {
		return coolTimeId;
	}

	public void setCoolTimeId(int coolTimeId) {
		this.coolTimeId = coolTimeId;
	}

	public Boolean getHitTargets(UnitId unitId) {
		return hitTargets.get(unitId);
	}

	public Map<UnitId, Boolean> getHitTargets() {
		return this.hitTargets;
	}
	
	public void updateHitTargets(UnitId unitId, boolean isHitTarget) {
		this.hitTargets.put(unitId, isHitTarget);
	}

	public Set<Long> getTeamMemberIds() {
		return teamMemberIds;
	}

	public void retainAll(Set<Long> viewPlayerIds) {
		if(viewPlayerIds != null && !viewPlayerIds.isEmpty()) {
			this.teamMemberIds.retainAll(viewPlayerIds);
		}
	}
	
	public boolean hasTeamAddition() {
		return this.teamMemberIds.size() >= 2;
	}
	
	public void addTeamMemberIds(Collection<Long> memberIds) {
		if(memberIds != null && !memberIds.isEmpty()) {
			this.teamMemberIds.addAll(memberIds);
		}
	}

	public void addTeamMemberIds(long memberId) {
		this.teamMemberIds.add(memberId);
	}
	
	public boolean isCritical() {
		return critical;
	}

	public void updateCritical(boolean critical) {
		this.critical = critical ? critical : this.critical;
	}

	public GameMap getGameMap() {
		return gameMap;
	}

	public void updateFightUnitChangePoints(ISpire unitId, Point point, boolean updateTargetPoint) {
		if(unitId != null && point != null) {
			this.changePointUnits.put(unitId, point);
			if(updateTargetPoint) {
				this.targetPoint = point;
			}
		}
	}
	
	public Map<ISpire, Point> getChangePointUnits() {
		return changePointUnits;
	}

	/**
	 * 构建战斗上下文对象
	 * 
	 * @param  positionX		X坐标点
	 * @param  positionY		Y坐标点
	 * @param  coolTimeId		冷却时间ID
	 * @return {@link Context}	上下文对象
	 */
	public static Context valueOf(Point castPoint, Point targetPoint, int coolTimeId, GameMap gameMap) {
		Context context = new Context();
		context.gameMap = gameMap;
		context.casterPoint = castPoint;
		context.coolTimeId = coolTimeId;
		context.targetPoint = targetPoint;
		return context;
	}
	
	/**
	 * 构建战斗上下文对象
	 * 
	 * @return {@link Context}	上下文对象
	 */
	public static Context defaultContext(Context source) {
		Context context = new Context();
		context.gameMap = source.getGameMap();
		context.casterPoint = source.casterPoint;
		context.targetPoint = source.targetPoint;
		context.coolTimeId = source.getCoolTimeId();
		context.hitTargets.putAll(source.getHitTargets());
		return context;
	}
 
	public void addUnitHurtValue(ISpire iSpire, int hurtValue) {
		if(hurtValue > 0) {
			Integer hpArr = this.hurtInfo.get(iSpire);
			hpArr = hpArr == null ? 0 : hpArr;
			this.hurtInfo.put(iSpire, hpArr + hurtValue);
		}
	}
	
	/** 伤害量统计. 仅仅计算伤害值. { 战斗单位对象, 扣除的血 } */
	public Map<ISpire, Integer> getHurtInfo() {
		return this.hurtInfo;
	}
}
