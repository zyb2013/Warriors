package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;

/**
 * 公式对象
 * 
 * @author Hyint
 */
@Resource
public class FormulaConfig {

	/** 公式ID */
	@Id
	private int id;
	
	/** 公式字符串 */
	private String expression;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public String toString() {
		return "Formula [id=" + id + ", expression=" + expression + "]";
	}
	
}
