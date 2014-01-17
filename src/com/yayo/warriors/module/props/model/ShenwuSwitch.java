package com.yayo.warriors.module.props.model;

import com.yayo.common.utility.Splitable;

/**
 * 神武开关
 * 
 * @author Hyint
 */
public class ShenwuSwitch {

	/** 神武ID */
	private int shenwuId;
	
	/** 是否已经进阶 */
	private boolean tempo;
	
	/** 喂养的次数 */
	private int shenwuCount;

	public int getShenwuId() {
		return shenwuId;
	}

	public void setShenwuId(int shenwuId) {
		this.shenwuId = shenwuId;
	}

	public boolean isTempo() {
		return tempo;
	}

	public void setTempo(boolean tempo) {
		this.tempo = tempo;
	}

	public int getShenwuCount() {
		return shenwuCount;
	}

	public void setShenwuCount(int shenwuCount) {
		this.shenwuCount = shenwuCount;
	}

	/**
	 * 神武开关对象
	 * 
	 * @param  shenwuId					神武ID
	 * @param  tempo					神武开关
	 * @param  shenwuCount				神武喂养次数	
	 * @return {@link ShenwuSwitch}		神武开关对象
	 */
	public static ShenwuSwitch valueOf(int shenwuId, boolean tempo, int shenwuCount) {
		ShenwuSwitch shenwuSwitch = new ShenwuSwitch();
		shenwuSwitch.tempo = tempo;
		shenwuSwitch.shenwuId = shenwuId;
		shenwuSwitch.shenwuCount = shenwuCount;
		return shenwuSwitch;
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append(shenwuId).append(Splitable.ATTRIBUTE_SPLIT).append(tempo ? 1 : 0).append(Splitable.ATTRIBUTE_SPLIT).append(shenwuCount).toString();
	}
	
	
}
