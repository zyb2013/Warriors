package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/**
 * 阵营战出生复活点配置
 * @author jonsai
 *
 */
@Resource
public class CampPointConfig {
	
	/** id */
	@Id
	private int id;
	
	/** 类型 */
	@Index(name = IndexName.CAMP_TYPE_POINT, order = 0)
	private int type;
	
	/** 据点编号(怪物配置编号) */
	@Index(name = IndexName.CAMP_TYPE_POINT, order = 1)
	private int baseId;
	
	/** 阵营 */
	@Index(name = IndexName.CAMP_TYPE_POINT, order = 2)
	private int camp;
	
	/** x坐标 */
	private int x;
	
	/** y坐标 */
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

	public int getBaseId() {
		return baseId;
	}

	public void setBaseId(int baseId) {
		this.baseId = baseId;
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
