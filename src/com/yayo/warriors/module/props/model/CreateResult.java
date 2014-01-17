package com.yayo.warriors.module.props.model;

import java.util.List;

/**
 * 创建返回值
 * 
 * @author Hyint
 *
 * @param <T>
 * @param <E>
 */
public class CreateResult <T, E> {

	private List<T> collections1;
	
	private List<E> collections2;

	public List<T> getCollections1() {
		return collections1;
	}

	public List<E> getCollections2() {
		return collections2;
	}

	public static <T, E> CreateResult<T, E> valueCollection1(List<T> collection1) {
		CreateResult<T, E> createResult = new CreateResult<T, E>();
		createResult.collections1 = collection1;
		return createResult;
	}
	
	public static <T, E> CreateResult<T, E> valueCollection2(List<E> collection2) {
		CreateResult<T, E> createResult = new CreateResult<T, E>();
		createResult.collections2 = collection2;
		return createResult;
	}

	public static <T, E> CreateResult<T, E> valueOf(List<T> collection1, List<E> collection2) {
		CreateResult<T, E> createResult = new CreateResult<T, E>();
		createResult.collections1 = collection1;
		createResult.collections2 = collection2;
		return createResult;
	}
}
