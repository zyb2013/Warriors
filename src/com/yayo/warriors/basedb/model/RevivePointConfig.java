package com.yayo.warriors.basedb.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.yayo.common.basedb.annotation.Id;
import com.yayo.common.basedb.annotation.Resource;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.module.map.model.RevivePosition;

/**
 * 复活点配置
 * <per>
 * 当配置中存在该记录
 * 在玩家死亡之后点击回城复活,将优先选择该配置中的复活点
 * </per>
 * @author liuyuhua
 */
@Resource
public class RevivePointConfig {

	/** 地图ID*/
	@Id
	private int id;
	
	/** 复活点
	 * {阵营ID_地图ID_坐标X_坐标Y|阵营ID_地图ID_坐标X_坐标Y}
	 * */
	private String point;
	
	@JsonIgnore
	private transient volatile List<RevivePosition> revivePositions = null;
	
	/**
	 * 获取复活点
	 * @return {@link List} 复活点集合
	 */
	public List<RevivePosition> getRevivePositions(){
		if(revivePositions != null){
			return revivePositions;
		}
		
		synchronized (this) {
			if(revivePositions != null){
				return revivePositions;
			}
			
			revivePositions = new ArrayList<RevivePosition>();
			if(point == null || point.isEmpty()){
				return revivePositions;
			}
			
			List<String[]> positions = Tools.delimiterString2Array(point);
			for(String[] position : positions){
				if(position.length < 4){
					continue;
				}
				int camp = Integer.parseInt(position[0]);
				int mapId = Integer.parseInt(position[1]);
				int x    = Integer.parseInt(position[2]);
				int y    = Integer.parseInt(position[3]);
				revivePositions.add(RevivePosition.valueOf(camp, mapId, x, y));
			}
			
			return revivePositions;
		}
		
	}
	

	//Getter and Setter....
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPoint() {
		return point;
	}

	public void setPoint(String point) {
		this.point = point;
	}

	@Override
	public String toString() {
		return "RevivePointConfig [id=" + id + ", point=" + point + "]";
	}
}
