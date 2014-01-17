package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;

/**
 * 护送任务品质所对应的移动速度 
 * @author liuyuhua
 */
@Resource
public class EscortGharryConfig {

	/** 品质*/
	@Id
	private int quality;
	
	/** 模型*/
	private int model;
	
	/** 品质镖车所对应的速度*/
	private int speed;

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}
	
	public int getModel() {
		return model;
	}

	public void setModel(int model) {
		this.model = model;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	@Override
	public String toString() {
		return "EscortGharryConfig [quality=" + quality + ", model=" + model
				+ ", speed=" + speed + "]";
	}
}
