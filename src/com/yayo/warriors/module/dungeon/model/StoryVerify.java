package com.yayo.warriors.module.dungeon.model;

/**
 * 剧情验证 模型
 * @author liuyuhua
 */
public class StoryVerify {

	/** 类型 {@link StoryVerify}*/
	private int type;
	
	/** 参数1(更具类型标识用处)*/
	private int param1;
	
	/** 参数2(更具类型标识用处)*/
	private int param2;
	
	/**
	 * 构造方法
	 * @param type        类型
	 * @param param1            参数1
	 * @param param2            参数2
	 * @return {@link StoryVerify} 剧情验证 模型对象
	 */
	public static StoryVerify valueOf(int type,int param1,int param2){
		StoryVerify verify = new StoryVerify();
		verify.type   = type;
		verify.param1 = param1;
		verify.param2 = param2;
		return verify;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getParam1() {
		return param1;
	}

	public void setParam1(int param1) {
		this.param1 = param1;
	}

	public int getParam2() {
		return param2;
	}

	public void setParam2(int param2) {
		this.param2 = param2;
	}

	@Override
	public String toString() {
		return "StoryVerify [type=" + type + ", param1=" + param1 + ", param2="
				+ param2 + "]";
	}
}
