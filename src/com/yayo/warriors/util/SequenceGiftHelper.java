package com.yayo.warriors.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.yayo.common.utility.CryptUtil;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.bo.ResultObject;
import com.yayo.warriors.module.gift.constant.GiftConstant;
import com.yayo.warriors.module.user.entity.Player;

/**
 * 序号礼包服客户端连接
 * 
 * @author Hyint
 */
@Component
public class SequenceGiftHelper {
	/** 0- 获取并锁定序列号 */
	public static final int QUERY = 1;
	/** 1- 撤销序列号锁定 */
	public static final int CANCEL = 2;
	/** 2- 确认序列号消耗 */
	public static final int CONFIRM = 3;
	
	@Autowired(required=true)
	@Qualifier("sequence.param.md5")
	private String md5Key;

	@Autowired(required=true)
	@Qualifier("sequence.url")
	private String sequenceUrl;

	
	@Autowired(required=true)
	@Qualifier("server.agent")
	private String serverAgent;
	
	@Autowired(required=true)
	@Qualifier("server.server")
	private String serverNo;
	
	@Autowired(required=true)
	@Qualifier("server.game")
	private String gameName;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SequenceGiftHelper.class);
	private static final ThreadLocal<HttpClient> THREAD_LOCAL = new ThreadLocal<HttpClient>();
	private static final ObjectReference<SequenceGiftHelper> REF = new ObjectReference<SequenceGiftHelper>();
	
	/**
	 * 初始化对象
	 */
	@PostConstruct
	protected void initialize() {
		REF.set(this);
	}
	
	@PreDestroy
	void close() {
		pool.shutdownNow();
	}
	
	/** 执行队列 */
	private ExecutorService pool = Executors.newSingleThreadExecutor(new ThreadFactory() {
		public Thread newThread(Runnable r) {
			return new Thread(r, "序列号处理线程");
		}
	});
	
	/**
	 * 提交请求任务
	 * 
	 * @param  task				提交的任务事件
	 * @return {@link Future}	提交的任务句柄
	 */
	public static <T> Future<T> submit(Callable<T> task) {
		return REF.get().pool.submit(task);
	}
	
	/**
	 * 取得HttpClient实例
	 * 
	 * @return {@link HttpClient}
	 */
	private static HttpClient getHttpClient() {
		HttpClient httpClient = THREAD_LOCAL.get();
		if(httpClient == null) {
			httpClient = new HttpClient();
			httpClient.getHttpConnectionManager().getParams().setSoTimeout(10000);
			httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
			THREAD_LOCAL.set(httpClient);
		}
		return httpClient;
	}
	
	/**
	 * 队列任务
	 * 
	 * @author Hyint
	 */
	private class SequenceCallable implements Callable<Integer> {
		private int action;
		private String userName;
		private String sequence;

		SequenceCallable(String userName, int action, String sequence) {
			this.userName = userName;
			this.action = action;
			this.sequence = sequence;
		}

		
		public Integer call() throws Exception {
			String md5 = "";
			String time = String.valueOf(System.currentTimeMillis());
			try {
				md5 = CryptUtil.md5(new StringBuffer().append(sequence).append(action).append(userName).append(time).append(md5Key).toString());
			} catch (Exception e) {
				LOGGER.error("Parse MD5 Exception:{}", e);
				md5 = "";
				userName = "";
			}

			PostMethod postMethod = new PostMethod(sequenceUrl);
			postMethod.setParameter("sign", md5);
			postMethod.setParameter("time", time);
			postMethod.setParameter("game", gameName);
			postMethod.setParameter("server", serverNo);
			postMethod.setParameter("agent", serverAgent);
			postMethod.setParameter("sequence", sequence);
			postMethod.setParameter("username", userName);
			postMethod.setParameter("action", String.valueOf(action));
			postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=\"utf-8\"");
			try {
				getHttpClient().executeMethod(postMethod);
				String response = postMethod.getResponseBodyAsString();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("连接序列号服务器:\t {}", response);
				}

				Integer giftId = Integer.valueOf(response);
				return giftId == null ? -1 : giftId.intValue();
			} catch (Exception e) {
				LOGGER.error("序列号请求异常", e);
			} finally {
				if (postMethod != null) {
					try {
						postMethod.releaseConnection();
					} catch (Exception e) {
						LOGGER.error("释放HTTP连接", e);
					}
				}
			}
			return -1;
		}
	}
	
	/**
	 * 根据序号获得礼包ID
	 * 
	 * @param  player				角色对象
	 * @param  action				操作类型
	 * @param  sequence 			礼包序列号
	 * @return {@link ResultObject}	返回值对象
	 */
	public ResultObject<Integer> processSequence(Player player, int action, String sequence) {
		Future<Integer> future = submit(new SequenceCallable(player.getUserName(), action, sequence));
		Integer result = GiftConstant.FAILURE;
		try {
			result = future.get(10, TimeUnit.SECONDS);
			if(result != null && result > 0) {
				return ResultObject.SUCCESS(result);
			}
		} catch (Exception ex) {
			LOGGER.error("连接序列号服务器", ex);
		}
		return ResultObject.ERROR(result);
	}
}

