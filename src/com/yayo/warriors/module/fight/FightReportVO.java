package com.yayo.warriors.module.fight;

import java.io.Serializable;
import java.util.List;

import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.fight.model.Context;
import com.yayo.warriors.module.fight.model.FightReport;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.user.model.AnimalInfo;

/**
 * 战报VO
 *
 */
public class FightReportVO implements Serializable {
	private static final long serialVersionUID = 8479415475251701512L;

	/** 技能ID */
	private int skillId;
	
	/** 发起技能的角色/怪物/召唤兽 */
	private UnitId active;			
	
	/** 发起技能的角色/怪物/召唤兽 */
	private UnitId target;
	
	/** 技能的目标点 */
	private int x;
	
	/** 技能的目标点 */
	private int y;
	
	/** 是否暴击. true-暴击, false-未暴击 */
	private boolean critical;
	
	/** 怪物的属性信息数组 {@link AnimalInfo} */
	private Object[] info;
	
	/** 战报数组.(战报有可能为空, 因为玩家可能攻击的据点上没有任何怪/人, 所以直接打空据点) {@link AnimalInfo}  */
	private Object[] reports;

	//--------------------
	public static FightReportVO valueOf(UnitId attackUnitId, UnitId targetUnitId, int skillId, Point targetPoint, Context context, List<AnimalInfo> info ){
		FightReportVO vo = new FightReportVO();
		vo.skillId = skillId;
		vo.active = attackUnitId;
		vo.target = targetUnitId;
		vo.x = targetPoint.x;
		vo.y = targetPoint.y;
		vo.critical = context.isCritical();
		vo.info = info.toArray();
		vo.reports = context.getFightReports().toArray();
		return vo;
	}
	
	public int getSkillId() {
		return skillId;
	}

	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}

	public UnitId getActive() {
		return active;
	}

	public void setActive(UnitId active) {
		this.active = active;
	}

	public UnitId getTarget() {
		return target;
	}

	public void setTarget(UnitId target) {
		this.target = target;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public boolean isCritical() {
		return critical;
	}

	public void setCritical(boolean critical) {
		this.critical = critical;
	}

	public Object[] getInfo() {
		return info;
	}

	public void setInfo(Object[] info) {
		this.info = info;
	}

	public Object[] getReports() {
		return reports;
	}

	public void setReports(Object[] reports) {
		this.reports = reports;
	}
	
}
