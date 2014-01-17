package com.yayo.warriors.module.user.type;

import com.yayo.warriors.module.fight.type.FightCasting;

/**
 * 角色的职业信息
 * 
 * <pre>
 * 0-通用
 * 1-明教
 * 2-唐门
 * 3-翠烟
 * 4-丐帮
 * </pre>
 * @author liuyuhua
 */
public enum Job {
	
	/** 0 - 通用的 */
	COMMON(0, null),

	/** 1 - 天龙 */
	TIANLONG(104001, FightCasting.PHYSICAL, Sex.MALE.ordinal()),

	/** 2 - 天山 */
	TIANSHAN(204001, FightCasting.PHYSICAL, Sex.FEMALE.ordinal()),
	
	/** 3 - 星宿 */
	XINGXIU(304001, FightCasting.THEURGY, Sex.FEMALE.ordinal()),
	
	/** 4 - 逍遥 */
	XIAOYAO(404001, FightCasting.THEURGY, Sex.MALE.ordinal());
	
	/** 衣服外观 */
	private int closing;

	/** 性别外观 */
	private int[] sex = null;
	
	/** 施法模式 */
	private FightCasting casting;
	
	Job(int closing, FightCasting casting, int...sexes) {
		this.sex = sexes;
		this.closing = closing;
		this.casting = casting;
	}

	public int[] getSex() {
		return sex;
	}
	
	public void setSex(int[] sex) {
		this.sex = sex;
	}

	public FightCasting getCasting() {
		return casting;
	}

	public void setCasting(FightCasting casting) {
		this.casting = casting;
	}

	public int getClosing() {
		return closing;
	}
}
