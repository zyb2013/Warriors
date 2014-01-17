package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/**
 * 乱舞战场出生点
 * @author jonsai
 *
 */
@Resource
public class BattlePointConfig {
	/** 序号 */
	@Id
	private int id;
	
	/** 类型 */
	@Index(name = IndexName.BATTLE_FIELD_TYPE_CAMP, order = 0)
	private int type;
	
	/** 阵营 */	
	@Index(name = IndexName.BATTLE_FIELD_TYPE_CAMP, order = 1)
	private int camp;
	
	/** 坐标X */
	private int x;
	
	/** 坐标Y */
	private int y;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCamp() {
		return camp;
	}

	public void setCamp(int camp) {
		this.camp = camp;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	
}
