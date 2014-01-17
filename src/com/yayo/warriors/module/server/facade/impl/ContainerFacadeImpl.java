package com.yayo.warriors.module.server.facade.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.delay.DataRemoveElement;
import com.yayo.common.socket.delay.MessageInfo;
import com.yayo.common.thread.NamedThreadFactory;
import com.yayo.warriors.module.logger.log.LoginLogger;
import com.yayo.warriors.module.server.facade.ContainerFacade;
import com.yayo.warriors.module.server.listener.DataRemoveListener;
import com.yayo.warriors.module.server.listener.Listener;
import com.yayo.warriors.module.server.listener.LoginListener;
import com.yayo.warriors.module.server.listener.LogoutListener;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.manager.UserManager;
import com.yayo.warriors.module.user.model.UserDomain;

@Component
public class ContainerFacadeImpl implements ApplicationListener<ContextRefreshedEvent>, ContainerFacade {
	@Autowired
	private UserManager userManager;
	@Autowired
	private ApplicationContext applicationContext;
	
	
	private static final Map<Class<?>, List<?>> LISTENER_MAP = new HashMap<Class<?>, List<?>>(1);
	@SuppressWarnings("unchecked")
	private static final Class<? extends Listener>[] LISTENERS = new Class[]{ LoginListener.class, LogoutListener.class, DataRemoveListener.class };
	
	
	public void onApplicationEvent(ContextRefreshedEvent event) {
		for (Class<? extends Listener> clazz : LISTENERS) {
			Map<String, ? extends Listener> listenerMap = applicationContext.getBeansOfType(clazz);
			if(listenerMap != null && !listenerMap.isEmpty()) {
				LISTENER_MAP.put(clazz, new ArrayList<Listener>(listenerMap.values()));
			}
		}
	}

	public void onLogoutUpdateListener(long playerId, String remoteIp) {
		UserDomain userDomain = userManager.getUserDomain(playerId);
		if(userDomain == null) {
			return;
		}
		
		Player player = userDomain.getPlayer();
		int branching = player.getBranching();
		PlayerBattle battle = userDomain.getBattle();
		List<LogoutListener> logoutListeners = getListener(LogoutListener.class); 
		if(logoutListeners != null && !logoutListeners.isEmpty()) {
			for (LogoutListener listener : logoutListeners) {
				try {
					listener.onLogoutEvent(userDomain);
				} catch (Exception e) {
				}
			}
		}
		
		LoginLogger.logout(player, battle, remoteIp, branching);
		QUEUES.add(new DataRemoveElement(MessageInfo.valueOf(playerId), new Date(), REMOVE_TIME));
	}
	
	
	public void onLoginListener(UserDomain userDomain, int branching) {
		long playerId = userDomain.getPlayerId();
		List<LoginListener> loginListeners = getListener(LoginListener.class); 
		if(loginListeners == null || loginListeners.isEmpty()) {
			return;
		}
		
		for (LoginListener listener : loginListeners) {
			try {
				listener.onLoginEvent(userDomain, branching);
			} catch (Exception e) {
			}
		}
	}



	private static final long REMOVE_TIME = 300;
	private static final String NAME = "数据移除线程";
	private static final DelayQueue<DataRemoveElement> QUEUES = new DelayQueue<DataRemoveElement>();
	private static final NamedThreadFactory THREAD_FACTORY = new NamedThreadFactory(new ThreadGroup(NAME), "");
	private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(2, 5, 1800, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), THREAD_FACTORY); 
	@PostConstruct
	protected void initialize() {
		Thread thread = new Thread(new Runnable() {
			
			public void run() {
				while(true) {
					try {
						DataRemoveElement take = QUEUES.take();
						Runnable runnable = getRunnable(take);
						if(runnable != null) {
							EXECUTOR.submit(runnable);
						}
					} catch (Exception e) {
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
	
	@SuppressWarnings("unchecked")
	private <T> List<T> getListener(Class<T> clazz) {
		return (List<T>)LISTENER_MAP.get(clazz);
	}
	
	private Runnable getRunnable(final DataRemoveElement dataRemoveElement) {
		if(dataRemoveElement == null) {
			return null;
		}
		
		final MessageInfo messageInfo = dataRemoveElement.getMessageInfo();
		if(messageInfo == null) {
			return null;
		}
		
		if(userManager.isOnline(messageInfo.getPlayerId())){
			return null;
		}
		
		final List<DataRemoveListener> dataRemoveListeners = getListener(DataRemoveListener.class);
		if(dataRemoveListeners == null || dataRemoveListeners.isEmpty()) {
			return null;
		}
		
		return new Runnable() {
			
			public void run() {
				if(userManager.isOnline(messageInfo.getPlayerId())){
					return;
				}
				
				for (DataRemoveListener listener : dataRemoveListeners) {
					try {
						listener.onDataRemoveEvent(messageInfo);
					} catch (Exception e) {
					}
				}
			}
		};
	}
	public void cancelDataRemoveScheduleTask(long playerId) {
		DataRemoveElement dataRemove = new DataRemoveElement(MessageInfo.valueOf(playerId));
		if(QUEUES.contains(dataRemove)) {
			QUEUES.remove(dataRemove);
		}
	}
	
	
}
