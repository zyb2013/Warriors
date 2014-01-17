package com.yayo.warriors.module.logger.type;

/**
 * 收支情况对象
 * 
 * @author Hyint
 */
public enum Orient {

	/** 0 - 收入 */
	INCOME("income", 0),

	/** 1 - 支出 */
	OUTCOME("outcome", 1),
	
	/** 0 - 货币收入 */
	MONEY_INCOME("moneyOrient", 0),

	/** 1 - 货币支出 */
	MONEY_OUTCOME("moneyOrient", 1);
	
	private String name;
	
	private int code = -1;
	
	Orient(String name, int code) {
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public int getCode() {
		return code;
	}
	
	
}
