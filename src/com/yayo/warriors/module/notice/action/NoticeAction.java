package com.yayo.warriors.module.notice.action;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.notice.vo.NoticeVo;


@Component
public class NoticeAction {
	
	
	private final ConcurrentSkipListSet<NoticeVo> POOL = new ConcurrentSkipListSet<NoticeVo>();
	
	private final BlockingQueue<NoticeVo> PUSH_QUEUE = new LinkedBlockingQueue<NoticeVo>();
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	private int TEMP = 100;
	
	@PostConstruct
	protected void init() {
		Thread thread = new Thread(NOTICE_PUSH_TASK);
		thread.setDaemon(true);
		thread.start();
	}
	
	
	public void addNotice(int noticeID, NoticeType noticeType, Map<String, Object> paramsMap) {
	}
	
	
	public void processNoticeScheduling() {
		for (int i = 1; i <= TEMP; i++) {
			if (POOL.isEmpty()) break;
			NoticeVo noticeVo = POOL.pollFirst();
			PUSH_QUEUE.add(noticeVo);
		}
	}
	
	
	private final Runnable NOTICE_PUSH_TASK = new Runnable() {
		
		public void run() {
			while (true) {
				try {
					NoticeVo noticeVo = PUSH_QUEUE.take();
					NoticePushHelper.pushNotice(noticeVo);
				} catch (Exception e) {
					LOGGER.error("公告ERROR: [{}]" , e);
				}
			}
		}
	};
	
	
}
