package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.common.helper.FormulaHelper;

/**
 * 基础冷却时间对象
 * 
 * @author Hyint
 */
@Resource
public class CoolTimeConfig {
	
	/** 冷却时间ID */
	@Id
	private int id;
	
	/** 冷却时间 */
	private String coolTimeExpr;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCoolTimeExpr() {
		return coolTimeExpr;
	}

	public void setCoolTimeExpr(String coolTimeExpr) {
		this.coolTimeExpr = coolTimeExpr;
	}

	@Override
	public String toString() {
		return "CoolTimeConfig [id=" + id + ", coolTimeExpr=" + coolTimeExpr + "]";
	}
	
	public int getCoolTime() {
		return FormulaHelper.invoke(this.coolTimeExpr).intValue();
	}
}
