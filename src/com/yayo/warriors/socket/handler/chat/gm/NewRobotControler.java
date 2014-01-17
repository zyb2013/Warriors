package com.yayo.warriors.socket.handler.chat.gm;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.utility.Tools;
import com.yayo.warriors.common.util.astar.Point;
import com.yayo.warriors.module.map.domain.GameMap;
import com.yayo.warriors.module.map.facade.MapFacade;
import com.yayo.warriors.module.map.manager.GameMapManager;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerMotion;
import com.yayo.warriors.module.user.facade.UserFacade;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Job;
import com.yayo.warriors.socket.handler.chat.domain.RobotDomain;


@Component
public class NewRobotControler {
	static Logger logger = LoggerFactory.getLogger(NewRobotControler.class);
	
	@Autowired
	private MapFacade mapFacade;
	@Autowired
	private UserFacade userFacade;
	@Autowired
	private UserManager userManager;
	@Autowired
	private GameMapManager gameMapManager ;
	private static AtomicLong atomicLong = new AtomicLong();
	private ExecutorService executorService = Executors.newFixedThreadPool(128);
	private Vector<RobotDomain> robotVector = new Vector<RobotDomain>();
	
	private volatile boolean openRobotThread = false ;
	private static List<Point> goHomeList ;
	private static List<Point> toTargetList ;
	
	void addThread(final int addCount, int branching){
		for (int i = 0; i < addCount; i++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			newCreateRobot(branching);
			System.out.println(atomicLong);
		}
		
		if(!openRobotThread){
			this.openRobotThread = true ;
			GameMap gameMap = gameMapManager.getGameMapById(101, 1);
			goHomeList = gameMap.findForAStar(128, 128, 67, 174);
			toTargetList = gameMap.findForAStar(67, 174, 128, 128);
			startRobotThread();
		}
	}
	
	private void startRobotThread() {
		Thread thread = new Thread(new RobotBossThread());
		thread.setDaemon(true);
		thread.start();
	}
	
	private static Job[] jobs = { Job.XINGXIU, Job.XIAOYAO, Job.XIAOYAO, Job.TIANSHAN };
	
	private void newCreateRobot(int branching) {
		long index = atomicLong.getAndIncrement() + 10000 ;
		String name = "T" + index;
		String password = "123456";
		
		long playerId = 0;
		List<Long> players = userManager.listPlayerIdByUserName(name);
		if(players == null || players.isEmpty()) {
			Job job = jobs[Tools.getRandomInteger(jobs.length)];
			userFacade.createPlayer(name, password, name , job.ordinal(), job.getSex()[0], 1);
			Player player = userManager.getPlayer(name);
			if(player != null) {
				playerId = player.getId();
			}
		} else {
			playerId = players.get(0);
		}

		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return;
		}
		
		Player player = userDomain.getPlayer();
		PlayerMotion motion = userDomain.getMotion();
		player.setBranching(branching);
		mapFacade.enterScreen(player.getId());
		robotVector.add( new RobotDomain(player, motion));
	}
	
	private static final long DELAY_TIME = 100;
	private static final int PEOPLE_THREAD = 20 ;
	
	private class RobotBossThread implements Runnable {
		
		public void run() {
			while(true) {
				try{
					Thread.sleep(DELAY_TIME);
					int maxSize = robotVector.size() ;
					for (int i = 0 ; i < maxSize ; i +=PEOPLE_THREAD) {
						int offSet = i + PEOPLE_THREAD >= maxSize ? maxSize :i + PEOPLE_THREAD ;
						executorService.execute(new RobotWorker(new ArrayList<RobotDomain>(robotVector.subList(i, offSet))));
					}
				} catch(Exception e) {
					e.printStackTrace() ;
				}
			}
		}
	}
	
	class RobotWorker implements Runnable{
		private List<RobotDomain> domains ;
		public RobotWorker(List<RobotDomain> list) {
			this.domains = list;
		}

		
		public void run() {
			try {
				for(RobotDomain domain : domains){
					if(!domain.hasTarget()){
						if(domain.getRote() != 1){
							domain.setRote(1);
							domain.setTarget(toTargetList);
						}else{
							domain.setTarget(goHomeList);
							domain.setRote(2);
						}
					}
					
					if(domain.delay())return ;
					if(domain.toNext()){
						Point target = domain.next() ;
						if(target == null ){
							return ;
						}
						Integer [] direction = {target.x, target.y};
						mapFacade.motionPath(domain.getPlayerId(), direction);
						mapFacade.motion(domain.getPlayerId(), target.x, target.y);	
					}else if(domain.hasTarget()){
						Point target = domain.getNextPoint() ;
						Integer [] direction = {target.x, target.y};
						mapFacade.motionPath(domain.getPlayerId(), direction);
						mapFacade.motion(domain.getPlayerId(), target.x, target.y);	
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
