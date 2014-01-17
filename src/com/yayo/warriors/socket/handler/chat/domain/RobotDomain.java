package com.yayo.warriors.socket.handler.chat.domain;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerMotion;

public class RobotDomain {

	private Player player ;
	private PlayerMotion playerMotion ;
	private Point nextPoint ;
	private Queue<Point> pointQueue = new LinkedBlockingQueue<Point>();
	private int rote;
	private long delay ;
	
	public RobotDomain(Player player , PlayerMotion playerMotion) {
		this.player = player ;
		this.playerMotion = playerMotion ;
	}

	public Long getPlayerId() {
		return player.getId();
	}

	public boolean toNext() {
		if(pointQueue == null)return false ;
		if(nextPoint == null){
			next();
		}
		return nextPoint.x == playerMotion.getX() && nextPoint.y == playerMotion.getY();
	}
	
	public Point next(){
		nextPoint = pointQueue.poll() ;
		delay = System.currentTimeMillis() + 250 * 3 ;
		return nextPoint ;
	}

	public void setTarget(List<Point> toTargetList) {
		for(int i = 0 ; i < toTargetList.size() ; i ++){
			if(i%5== 0 || (i+1) == toTargetList.size()){
				pointQueue.add(toTargetList.get(i));
			}
		}
	}
	public boolean inBorn() {
		return (playerMotion.getX() == 67 && playerMotion.getY() == 174);
	}

	public boolean hasTarget() {
		return pointQueue.size() > 0;
	}
	
	public Point getNextPoint(){
		return this.nextPoint ;
	}

	public int getRote() {
		return this.rote ;
	}

	public void setRote(int i) {
		this.rote = i ;
	}

	public boolean delay() {
		return this.delay - System.currentTimeMillis() > 0;
	}
}
