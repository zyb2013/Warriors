package com.yayo.warriors.module.map.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.BigMapConfig;
import com.yayo.warriors.basedb.model.MapConfig;
import com.yayo.warriors.common.util.astar.AStar2;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.fight.model.UnitId;
import com.yayo.warriors.module.map.model.ChangePoint;
import com.yayo.warriors.module.map.types.MaskTypes;
import com.yayo.warriors.module.map.types.ScreenType;
import com.yayo.warriors.module.map.util.MapUtils;
import com.yayo.warriors.type.ElementType;


public class GameMap{
	private long id ;
	private int GIRD_SIZE = 7 ;	
	public static final int CACHE_DISTANCE = 1 ;
	private int mapId;
	private List<ChangePoint> changePoints;
	private GameScreen[][] gameScreens ;
	private MapConfig mapConfig;
	private BigMapConfig bigMapConfig;
	private int height ;
	private int width ;
	private int gridWidth ;
	private int gridHeight ;
	private int branching ;
	private int screenType;
	
	private int levelLimit;
	
	private AtomicInteger playerNums = new AtomicInteger(0);
	
	public GameMap(int mapId ,long id , MapConfig config ,BigMapConfig binMapConfig,int branching ,long createTime ) {
		this.mapConfig = config;
		this.bigMapConfig = binMapConfig;
		this.mapId = mapId ;
		this.id = id ;
		this.changePoints = config.getPointlist();
		this.initMap();
		this.gameScreens = new GameScreen[gridWidth + 1][gridHeight + 1] ;
		this.screenType = config.getScreenType() ;
		this.branching = branching ;
		this.levelLimit = config.getLevelLimit();
		if(this.screenType == ScreenType.CAMP.ordinal() ){
			this.GIRD_SIZE = 10;
		}
		else if(this.screenType == ScreenType.BATTLE_FIELD.ordinal()){
			this.GIRD_SIZE = 10;
		}
		else if(this.screenType == ScreenType.DUNGEON.ordinal()){
			this.GIRD_SIZE = 10;
		}
		else if(this.screenType == ScreenType.NEUTRAL.ordinal()){
			this.GIRD_SIZE = 10;
		}
	}
	
	public int getMapId(){
		return this.mapId ;
	}

	public MapConfig getMapConfig() {
		return mapConfig;
	}
	
	
	public ChangePoint getChangePoint(int x, int y){
		int dist = 0;
		ChangePoint target = null;
		for(ChangePoint changePoint : this.changePoints){
			if(target != null){
				int dist2 = MapUtils.calcDistance(x, y, changePoint.getVerfiX(), changePoint.getVerfiY());
				if( dist2 < dist){
					target = changePoint;
					dist = dist2;
				}
				
			} else {
				target = changePoint;
				dist = MapUtils.calcDistance(x, y, target.getVerfiX(), target.getVerfiY());
			}
		}
		return target;
	}

	
	
	public ChangePoint getNearestChangePoint(int x, int y){
		int dist = 0;
		ChangePoint target = null;
		for(ChangePoint changePoint : this.changePoints){
			if(target != null){
				int dist2 = MapUtils.calcDistance(x, y, changePoint.getX(), changePoint.getY());
				if( dist2 < dist){
					target = changePoint;
					dist = dist2;
				}
				
			} else {
				target = changePoint;
				dist = MapUtils.calcDistance(x, y, target.getX(), target.getY());
			}
		}
		return target;
	}

	private void initMap() {
		byte[][] mapMask = mapConfig.getMapMask();
		this.height =  mapMask[0].length ;
		this.width =  mapMask.length ;
		this.gridHeight = this.height / GIRD_SIZE ;
		this.gridWidth = this.width / GIRD_SIZE ;
	}
	
	
	public List<Point> findForAStar(int x1, int y1, int x2, int y2){
		AStar2 aStar = new AStar2(mapConfig.getMapMask(), 2000);
		return aStar.find(x1, y1, x2, y2);
	}
	
	public List<Point> findForAStar(int x1, int y1, int x2, int y2, int timeout){
		AStar2 aStar = new AStar2(mapConfig.getMapMask(), timeout);
		return aStar.find(x1, y1, x2, y2);
	}
	

	public boolean isPathPass(int x, int y) {
		try{
			if(x < 0 || y < 0 || x >= width || y >= height){
				return false ;
			}
			return this.mapConfig.getMapMask()[x][y] != MaskTypes.PATH_BARRIER;
			
		} catch(Exception e){
			return false;
		}
	}
	
	
	


	
	public boolean inThisMap(ISpire spire) {
		GameScreen gameScreen = this.getGameScreen(spire);
		return gameScreen != null && gameScreen.hasThisSpire(spire.getType(), spire);
	}

	
	public GameScreen getGameScreen(ISpire spire) {
		return this.getGameScreen(spire.getX() , spire.getY());
	}

	
	public GameScreen getGameScreen(int x, int y) {
		try{
			if(x < 0 || y < 0 || x > width || y > height){
				return null ;
			}
			GameScreen[][] gameScreens = this.gameScreens;
			if(gameScreens == null){
				return null;
			}
			int xIndex = x/GIRD_SIZE;
			int yIndex = y/GIRD_SIZE;
			GameScreen gameScreen = gameScreens[xIndex][yIndex] ;
			if(gameScreen == null){
				synchronized (this) {
					if(gameScreen == null){
						gameScreen = new GameScreen(this,xIndex,yIndex);
						gameScreens[xIndex][yIndex] = gameScreen ;
					}
				}
			}
			return gameScreen ;
		} catch(Exception e){
			return null ;
		}
	}
	

	public GameScreen getGameScreenByGrid(int gx, int gy) {
		if(gx < 0 || gy < 0 || gx > gridWidth || gy > gridHeight){
			return null ;
		}
		GameScreen[][] gameScreens = this.gameScreens;
		if(gameScreens == null){
			return null;
		}
		GameScreen gameScreen = gameScreens[gx][gy] ;
		if(gameScreen == null){
			gameScreen = new GameScreen(this,gx,gy);
			gameScreens[gx][gy] = gameScreen ;
		}
		return gameScreen ;
	}


	public void enterMap(ISpire spire) {
		if(spire != null){
			synchronized (spire) {
				GameScreen gameScreen = this.getGameScreen(spire);
				gameScreen.enterScreen(spire);
				spire.recordScreen(gameScreen);
			}
		}
	}


	public Set<Long> getCanViewsSpireIdCollection(ISpire spire,ElementType type, Long...ignore) {
		Set<Long> spireIdSet = new HashSet<Long>();
		if(spire == null || type == null){
			return spireIdSet;
		}
		
		int x = spire.getX();
		int y = spire.getY();
		Set<GameScreen> gameScreenList = this.calcViewScreen(x , y);
		for(GameScreen gameScreen : gameScreenList){
			spireIdSet.addAll(gameScreen.getSpireIdCollection(type));
		}
		for (Long ignoreId : ignore) {
			spireIdSet.remove(ignoreId);
		}
		return spireIdSet;
	}
	

	public Set<ISpire> getCanViewsSpireCollection(ISpire spire,ElementType type) {
		Set<ISpire> spireSet = new HashSet<ISpire>();
		if(spire != null && type != null){
			Set<GameScreen> gameScreenList = this.calcViewScreen(spire.getX() , spire.getY());
			for(GameScreen gameScreen : gameScreenList){
				spireSet.addAll(gameScreen.getSpireCollection(type));
			}
		}
		return spireSet;
	}


	public Set<ISpire> getCanViewsSpireCollection(int x,int y,ElementType... types) {
		Set<ISpire> spireSet = new HashSet<ISpire>();
		if(types != null){
			Set<GameScreen> gameScreenList = this.calcViewScreen(x, y);
			for(GameScreen gameScreen : gameScreenList){
				spireSet.addAll(gameScreen.getSpireCollection(types));
			}
		}
		return spireSet;
	}


	public Set<GameScreen> calcViewScreen(int x , int y) {
		Set<GameScreen> screenList = new HashSet<GameScreen>(9);
		int xIndex = x/GIRD_SIZE;
		int yIndex = y/GIRD_SIZE;
		final int offset = CACHE_DISTANCE + 1;
		for(int xOffset = -offset ; xOffset <= offset ; xOffset ++){
			for(int yOffset = -offset; yOffset <= offset ; yOffset ++){
				if(xOffset + yOffset != 0 || Math.abs(xOffset) <= CACHE_DISTANCE){
					GameScreen gameScreen = this.getGameScreenByGrid(xIndex + xOffset , yIndex + yOffset);
					if(gameScreen != null){
						screenList.add(gameScreen);
					}
				}
			}
		}
		
		return screenList;
	}
	

	public Set<GameScreen> calcViewScreen(ISpire spire) {
		if(spire == null){
			return new HashSet<GameScreen>(1);
		}
		return this.calcViewScreen(spire.getX(), spire.getY());
	}
	

	public boolean checkInViewScreen(ISpire spire, ISpire targetSpire){
		if(spire == null){
			return false;
		}
		int xIndex = spire.getX()/GIRD_SIZE;
		int yIndex = spire.getY()/GIRD_SIZE;
		GameScreen currentScreen = targetSpire.getCurrentScreen();
		if(currentScreen == null){
			return false;
		}
		for(int xOffset = -CACHE_DISTANCE ; xOffset <= CACHE_DISTANCE ; xOffset ++){
			for(int yOffset = -CACHE_DISTANCE ; yOffset <= CACHE_DISTANCE ; yOffset ++){
				GameScreen gameScreen = this.getGameScreenByGrid(xIndex + xOffset , yIndex + yOffset);
				if(gameScreen == currentScreen){
					return true;
				}
			}
		}
		return false;
	}
	

	public Set<GameScreen> calcViewScreenByScreen(ISpire spire) {
		GameScreen gameScreen = spire.getCurrentScreen();
		if(gameScreen == null){
			return new HashSet<GameScreen>(1);
		}
		return this.calcViewScreen(gameScreen.getPoint().x * GIRD_SIZE, gameScreen.getPoint().y * GIRD_SIZE);
	}


	public List<GameScreen> calcOffSetGameScreen(GameScreen lastScreen,GameScreen toGameScreen) {
		List<GameScreen> offSetGameScreen = new LinkedList<GameScreen>();
		if(lastScreen == null || toGameScreen == null){
			return offSetGameScreen;
		}
		int offsetX = toGameScreen.getPoint().x - lastScreen.getPoint().x ; 
		int offsetY = toGameScreen.getPoint().y - lastScreen.getPoint().y ; 
		
		int ox = lastScreen.getPoint().x - offsetX ;
		int oy = lastScreen.getPoint().y - offsetY ;

		for(int offset = -(CACHE_DISTANCE) ; offset <= (CACHE_DISTANCE)  ; offset ++){
			if(offsetY != 0){
				int xSwap = toGameScreen.getPoint().x - offset ;
				if(!(xSwap < 0 || oy < 0 || xSwap > width/GIRD_SIZE || oy > height/GIRD_SIZE)){
					GameScreen gameScreenSwapX = this.getGameScreenByGrid(xSwap, oy) ;
					if(gameScreenSwapX != null){
						offSetGameScreen.add(gameScreenSwapX);
					}
				}
			}
			if(offsetX != 0){
				int ySwap = toGameScreen.getPoint().y - offset ;
				if(!(ox < 0 || ySwap < 0 || ox > width/GIRD_SIZE || ySwap > height/GIRD_SIZE)){
					GameScreen gameScreenSwapY = this.getGameScreenByGrid(ox, ySwap) ;
					if(gameScreenSwapY != null){
						offSetGameScreen.add(gameScreenSwapY);
					}
				}
			}
		}
		return offSetGameScreen;
	}
	

	public static Set<ISpire> getSpires(Collection<GameScreen> gameScreenList , ElementType... types){
		Set<ISpire> spires = new HashSet<ISpire>();
		if(gameScreenList != null){
			for(GameScreen gameScreen : gameScreenList){
				spires.addAll(gameScreen.getSpireCollection(types));
			}
		}
		return spires ;
	}

	
	public int getScreenType() {
		return this.screenType;
	}


	public Set<Long> getAllSpireIdCollection(ElementType type) {
		Set<Long> playerIdCollection = new HashSet<Long>();
		GameScreen[][] gameScreens = this.gameScreens;
		if(gameScreens != null){
			for(GameScreen[] screens : gameScreens){
				for(GameScreen screen : screens){
					if(screen == null) continue ;
					playerIdCollection.addAll(screen.getSpireIdCollection(type));
				}
			}
		}
		return playerIdCollection;
	}
	
	
	public Set<ISpire> getAllSpireCollection(ElementType type) {
		Set<ISpire> spireCollection = new HashSet<ISpire>();
		if(gameScreens != null){
			for(GameScreen[] screens : gameScreens){
				for(GameScreen screen : screens){
					if(screen == null) continue ;
					spireCollection.addAll(screen.getSpireCollection(type));
				}
			}
		}
		return spireCollection;
	}


	public void enterMap(UnitId unitId , int bornX, int bornY) {
		GameScreen screen = this.getGameScreen(bornX, bornY);
		ISpire motion = new DefaultMotion(unitId, bornX, bornY) ;
		screen.enterScreen(motion);
		motion.recordScreen(screen);
	}


	public Set<Long> getSpireIdCollectionByGird(int targetX, int targetY, int grid , ElementType type) {
		Set<ISpire> spireCollection = this.getCanViewsSpireCollection(targetX,targetY, type);
		Set<Long> hitMotionList = new HashSet<Long>();
		for(ISpire spire : spireCollection ){
			if(MapUtils.checkPosScopeInfloat(spire.getX(), spire.getY(), targetX, targetY, grid)){
				hitMotionList.add(spire.getId());
			}
		}
		return hitMotionList;
	}
	

	public void leaveMap(ISpire spire){
		GameScreen gameScreen = this.getGameScreen(spire);
		if(gameScreen != null){
			gameScreen.leaveScreen(spire);
		}
	}

	public int getBranching(){
		return this.branching ;
	}
	

	public long getId(){
		return this.id ;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + branching;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + mapId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
	

	public Point getRandomCanStandPoint(int x , int y , int patrolRange){
		if(patrolRange > 0){
			int xSwap = x + getRandomRange(patrolRange);
			int ySwap = y + getRandomRange(patrolRange);
			if(this.isPathPass(xSwap, ySwap)){
				return new Point(xSwap,ySwap);
			}
		}
		return new Point(x,y);
	}
	

	public int getRandomRange(int patrolRange){
		if(patrolRange <= 0){
			return 0;
		}
		int xx = 0;
		while(true){
			xx = Tools.getRandomInteger(patrolRange * 2 + 1) - patrolRange;
			if(Math.abs(xx) > 1){
				break;
			}
		}
		return xx;
	}


	public void recoverMap() {
		this.clear() ;
	}
	

	public void clear(){
		synchronized (this) {
			for(GameScreen[] gameScreenArray : gameScreens){
				if(gameScreenArray == null){
					continue ;
				}
				for(GameScreen gameScreen : gameScreenArray){
					if(gameScreen == null){
						continue ;
					}
					gameScreen.clear();
					gameScreen = null ;
				}
			}
			this.gameScreens = null ;
		}
	}
	
	public boolean isCleared(){
		return this.gameScreens == null;
	}

	
	public int getPlayerNums() {
		return playerNums.get();
	}
	

	protected void increasePlayerNum(int i){
		playerNums.addAndGet(i);
	}

	public int getLevelLimit() {
		return levelLimit;
	}

	public void setLevelLimit(int levelLimit) {
		this.levelLimit = levelLimit;
	}

	public BigMapConfig getBigMapConfig() {
		return bigMapConfig;
	}

	public void setBigMapConfig(BigMapConfig bigMapConfig) {
		this.bigMapConfig = bigMapConfig;
	}
	
}
