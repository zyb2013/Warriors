package com.yayo.warriors.basedb.model;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Index;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.warriors.type.IndexName;

/**
 * 阵营传送点
 * @author jonsai
 *
 */
@Resource
public class CampChangePoint {
	/** 阵营 */
	@Id
	private int id;
	
	@Index(name = IndexName.MAP_CAMP_CHANGE_POINT)
	private int camp;
	
	/** 传送地图 */
	private int mapId;
	
	/** X坐标 */
	private int x;
	
	/** Y坐标 */
	private int y;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCamp() {
		return camp;
	}

	public void setCamp(int camp) {
		this.camp = camp;
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
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
