package com.yayo.warriors.bo;

import com.yayo.warriors.constant.CommonConstant;

/**
 * 返回值对象
 * 
 * @author Hyint
 * 
 * @param <T>
 */
public class ResultObject<T extends Object> {

	/** 返回值 */
	private int result;

	/** 返回的对象 */
	private T value;

	/** 构造器私有化 */
	private ResultObject() { }
	
	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	/**
	 * 是否成功
	 * @return
	 */
	public boolean isOK(){
		return result == CommonConstant.SUCCESS;
	}

	/**
	 * 成功的返回值 
	 * 
	 * @param <T>
	 * @return {@link ResultObject}		返回值对象
	 */
	public static <T extends Object> ResultObject<T> SUCCESS() {
		ResultObject<T> resultObject = new ResultObject<T>();
		resultObject.result = CommonConstant.SUCCESS;
		return resultObject;
	}

	/**
	 * 成功的返回值 
	 * 
	 * @param <T>
	 * @param  value					返回所带的对象
	 * @return {@link ResultObject}		返回值对象
	 */
	public static <T extends Object> ResultObject<T> SUCCESS(T value) {
		ResultObject<T> resultObject = new ResultObject<T>();
		resultObject.value = value;
		resultObject.result = CommonConstant.SUCCESS;
		return resultObject;
	}
 
	/**
	 * 错误返回
	 * 
	 * @param <T>
	 * @param  result					返回值的值
	 * @return {@link ResultObject}		返回值对象
	 */
	public static <T extends Object> ResultObject<T> ERROR(int result) {
		ResultObject<T> resultObject = new ResultObject<T>();
		resultObject.result = result;
		return resultObject;
	}

	/**
	 * 错误返回
	 * 
	 * @param <T>
	 * @param  result					返回值的值
	 * @param  value					返回所带的对象
	 * @return {@link ResultObject}		返回值对象
	 */
	public static <T extends Object> ResultObject<T> valueOf(int result, T value) {
		ResultObject<T> resultObject = new ResultObject<T>();
		resultObject.result = result;
		resultObject.value = value;
		return resultObject;
	}
}
