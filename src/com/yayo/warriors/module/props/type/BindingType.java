package com.yayo.warriors.module.props.type;

/**
 * 绑定类型
 * 
 * <pre>
 *  0-不绑定
 *  1-拾取绑定
 *  2-打造绑定
 *  3-穿着绑定
 * </pre>
 * @author Hyint
 */
public enum BindingType {
	
	/** 0-不绑定 */
	UNBINDING,
	
	/** 1-拾取绑定 */
	PICK_BINDING,
	
	/** 2-打造绑定*/
	FORGE_BINDING,

	/** 3-穿着绑定  */
	DRESS_BINDING;
	
}
