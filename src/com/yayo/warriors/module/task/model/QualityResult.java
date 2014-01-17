package com.yayo.warriors.module.task.model;

import com.yayo.warriors.constant.CommonConstant;

public class QualityResult<T> {

	private int result;
	
	private int useGold;
	
	private int useBooks;
	
	private T entityVO;

	public int getResult() {
		return result;
	}

	public int getUseGold() {
		return useGold;
	}

	public int getUseBooks() {
		return useBooks;
	}

	public T getEntityVO() {
		return entityVO;
	}

	public static <T> QualityResult<T> ERROR(int result) {
		return valueOf(CommonConstant.SUCCESS, null);
	}

	public static <T> QualityResult<T>  SUCCESS(T taskVO) {
		return valueOf(CommonConstant.SUCCESS, taskVO);
	}

	public static <T> QualityResult<T> valueOf(int result, T taskVO) {
		QualityResult<T> qualityResult = new QualityResult<T>();
		qualityResult.result = result;
		qualityResult.entityVO = taskVO;
		return qualityResult;
	}

	public static <T> QualityResult<T> valueOf(int result, int useGold, int useBooks, T taskVO) {
		QualityResult<T>  qualityResult = new QualityResult<T>();
		qualityResult.result = result;
		qualityResult.useGold = useGold;
		qualityResult.entityVO = taskVO;
		qualityResult.useBooks = useBooks;
		return qualityResult;
	}
}
