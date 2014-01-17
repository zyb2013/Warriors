package com.yayo.warriors.module.onhook.model;

/**
 * 闭关领取奖励类型
 * 
 * @author huachaoping
 */
public enum ReceiveType {
	
	NONE(-1),
	
	/** 单倍 */
	SINGLE(1),
	
	/** 双倍 */
	DOUBLE(2),
	
	/** 三倍 */
	TRIPLE(3),
	
	;
	
	/** 领取倍数 */
	int multiple;
	
	private ReceiveType(int multiple) {
		this.multiple = multiple;
	}

	public int getMultiple() {
		return multiple;
	}

	public void setMultiple(int multiple) {
		this.multiple = multiple;
	}
	

}
