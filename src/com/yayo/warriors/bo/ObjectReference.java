package com.yayo.warriors.bo;

/**
 * 软引用对象
 * 
 * @author Hyint
 *
 * @param <T>
 */
public class ObjectReference <T> {

	/** 软引用对象 */
	private T content;

	public T get() {
		return content;
	}

	public void set(T content) {
		this.content = content;
	}

	
}
