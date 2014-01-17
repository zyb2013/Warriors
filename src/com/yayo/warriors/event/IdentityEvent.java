package com.yayo.warriors.event;

/**
 * 事件唯一标识
 * 
 * @author hyint
 */
public interface IdentityEvent {
	
	/**
	 * 获取对应的事件名
	 * 
	 * @return {@link String}
	 */
	String getName();

	/**
	 * 获取发生事件的用户身份标识
	 * 
	 * @return {@link Long}	属性ID
	 */
	long getOwnerId();
}
