package com.yayo.warriors.module.alliance.types;

/**
 * 帮派设置验证状态 
 * @author liuyuhua
 */
public interface VilidaState {
	
	/** 允许任何人加入帮派(默认)*/
	int NORMAL = 0;
	
	/** 需要验证才能够加入帮派(需要申请)*/
	int VILIDATE = 1;
	
	/** 不允许任何人加入帮派(关闭申请和加入)*/
	int CLOSED = 2;
}
