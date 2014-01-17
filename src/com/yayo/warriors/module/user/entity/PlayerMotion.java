package com.yayo.warriors.module.user.entity;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yayo.common.db.model.BaseModel;
import com.yayo.warriors.module.map.rule.MapRule;
import com.yayo.warriors.util.GameConfig;

/**
 * 角色行走
 * @author liuyuhua
 */
@Entity
@Table(name = "playerMotion")
public class PlayerMotion extends BaseModel<Long> {
	private static final long serialVersionUID = -6179786273976469473L;

	@Id
	@Column(name = "playerId")
	private Long id;

	/** 当前的X坐标点 */
	private int x = 0;

	/** 当前的Y坐标点 */
	private int y = 0;

	/** 当前角色所在的地图ID */
	private int mapId;
	
	/**存储人物行走方向*/
	@Transient
	private transient List<Integer> path = new LinkedList<Integer>();
	
	/**存储人物行走方向*/
	@Transient
	private byte face;
	
	@Transient
	private int steps = 0;
	
	// Getter and Setter
	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
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


	/**
	 * 角色移动的对象
	 * 
	 * @param playerId
	 * @param sourceX
	 * @param sourceY
	 * @return
	 */
	public static PlayerMotion valueOf(long playerId, int sourceX, int sourceY) {
		PlayerMotion playerMotion = new PlayerMotion();
		playerMotion.x = sourceX;
		playerMotion.y = sourceY;
		return playerMotion;
	}

	/**玩家行走信息*/
	public List<Integer> getPath() {
		return path;
	}
	
	public Object[] currentPointToArrays() {
		synchronized (this.path) {
			return this.path.toArray();
		}
	}

	public void clearPath(){
		synchronized (this.path) {
			this.path.clear();
		}
	}

	/**
	 * 超出验证范围
	 * @param tmpX
	 * @param tmpY
	 * @param maxPointSize
	 * @return
	 */
	public boolean beyondTheVerificationScope(int tmpX, int tmpY, int maxPointSize) {
		if (Math.abs(this.x - tmpX) > MapRule.MAX_POINT_SIZE || Math.abs(this.y - tmpY) > MapRule.MAX_POINT_SIZE) {
			return true ;
		}
		return false;
	}
	
	public int[] currentPath() {
		return new int[] { this.x, this.y };
	}
	
	/**
	 * 每走动一步,删除一个方向
	 * @param y2 
	 * @param x2 
	 */
	public void walk(int toX, int toY){
		synchronized (this.path) {
			this.x = toX ;
			this.y = toY ;
		}
	}
	/**
	 * 每走动一步,删除一个方向
	 * @param y2 
	 * @param x2 
	 */
	public void removePoint(){
		synchronized (this.path) {
			if(this.path.size() >= 2){
				this.path.remove(0);
				this.path.remove(0);
			}
		}
	}

	public boolean removePath(){
		synchronized (path) {
			if(path.size() <= 2){
				this.path.clear() ;
				return  true;
			}
			return false;
		}
	}
	
	public void addPath(Object[] direction){
		synchronized (path) {
			this.path.clear() ;
			for(Object p : direction){
				this.path.add((Integer) p);
			}
		}
	}

	public void changeMap(int id, int x, int y) {
		this.mapId = id ;
		this.x = x ; 
		this.y = y ;
	}

	public byte getFace() {
		return face;
	}

	public void setFace(byte face) {
		this.face = face;
	}
	
}
