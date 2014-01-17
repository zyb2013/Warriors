package com.yayo.warriors.module.map.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.type.ElementType;


public class GameScreen {

	private GameMap gameMap = null;
	
	private Point point = null;
	
	private Set<ISpire>[] spireContainers = null;
	
	@SuppressWarnings("unchecked")
	public GameScreen(GameMap gameMap, int x, int y){
		if(spireContainers == null){
			synchronized (this) {
				if(spireContainers == null){
					ElementType[] types = ElementType.values();
					spireContainers = new Set[ types.length ];
					for(ElementType type : types){
						spireContainers[type.ordinal()] = Collections.synchronizedSet( new HashSet<ISpire>() );
					}
				}
			}
		}
		
		this.gameMap = gameMap ;
		this.point = new Point(x,y);
	}
	

	public Collection<Long> getSpireIdCollection(ElementType type){
		List<Long> list = new LinkedList<Long>();
		Set<ISpire> spires = spireContainers[ type.ordinal() ];
		synchronized (spires) {
			for(ISpire spire : spires){
				list.add( spire.getId() );
			}
		}
		return list;
	}
	

	public Collection<ISpire> getSpireCollection(ElementType...types){
		List<ISpire> list = new LinkedList<ISpire>();
		for(ElementType type : types){
			Set<ISpire> spires = spireContainers[ type.ordinal() ];
			synchronized (spires) {
				list.addAll( spires );
			}
		}
		return list;
	}
	

	public boolean hasThisSpire(ElementType type , ISpire spire){
		synchronized (spireContainers[type.ordinal()]) {
			return spireContainers[type.ordinal()].contains(spire);
		}
	}
	

	public void enterScreen(ISpire spire ){
		Set<ISpire> spireContainer = spireContainers[spire.getType().ordinal()];
		synchronized (spireContainer) {
			if( spireContainer.add(spire) ) {
				if(spire.getType() == ElementType.PLAYER){
					gameMap.increasePlayerNum(1);
				}
			}
		}
	}

	public void leaveScreen(ISpire spire ){
		if(spireContainers == null){
			return ;
		}
		Set<ISpire> spireSet = spireContainers[ spire.getType().ordinal() ];
		if(spireSet != null){
			synchronized (spireSet) {
				if(spireSet.remove(spire)){
					if(spire.getType() == ElementType.PLAYER){
						gameMap.increasePlayerNum(-1);
					}
				}
			}
		}
	}
	

	public int getMapId() {
		return gameMap.getMapId();
	}


	public Point getPoint() {
		return point;
	}
	

	public int totalSpireNum(){
		int num = 0 ;
		if(spireContainers != null){
			ElementType[] values = ElementType.values();
			for(ElementType type : values){
				synchronized (spireContainers[ type.ordinal() ]) {
					num += spireContainers[ type.ordinal() ].size();
				}
			}
		}
		return num ;
	}
	
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gameMap == null) ? 0 : gameMap.hashCode());
		result = prime * result + ((point == null) ? 0 : point.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(obj == null){
			return false;
		}else if(obj == this){
			return true ;
		}else if(obj instanceof GameScreen){
			GameScreen gameScreen = (GameScreen)obj ;
			GameMap gameMap2 = gameScreen.getGameMap();
			return gameMap == gameMap2 && this.point.equals(gameScreen.getPoint());
		}
		return false ;
	}

	@Override
	public String toString(){
		return "mapId:"+this.getMapId() +", point:" + point.toString() + ", spireNum:" + this.totalSpireNum();
	}
	

	public GameMap getGameMap(){
		return this.gameMap ;
	}


	public boolean checkInThisGameScreen(ISpire spire) {
		GameScreen gameScreen = gameMap.getGameScreen(spire);
		if(gameScreen == this){
			return true ;
		}
		
		return false;
	}

	public void clear() {
		if(spireContainers != null){
			for(Set<ISpire> spires : spireContainers ){
				synchronized (spires) {
					spires.clear();
				}
			}
		}
	}

	public boolean hasSpireInThisPoint(int x , int y , ElementType... types){
		Collection<ISpire> spireCollection = getSpireCollection(types);
		for(ISpire spire : spireCollection){
			if(spire.getX() == x && spire.getY() == y){
				return true ;
			}
		}
		return false ;
	}
}
