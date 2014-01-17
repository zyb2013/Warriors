package com.yayo.warriors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import com.yayo.common.socket.SocketServer;
import com.yayo.common.socket.codec.CommonCodecFactory;
import com.yayo.common.socket.codec.RequestDecoder;
import com.yayo.common.socket.codec.ResponseEncoder;
import com.yayo.common.socket.context.ApplicationContext;
import com.yayo.common.socket.firewall.ByteAttackFilter;
import com.yayo.common.socket.firewall.CmdAttackFilter;
import com.yayo.warriors.socket.WarriorsServerHandler;


/**
 * 亚游刀剑无双后台服务器主进程入口
 * 
 * @author Hyint
 */
public class YayoDaemon {
	private static final Log log = LogFactory.getLog(YayoDaemon.class);
	
	public static void main(String[] args) {
		ApplicationContext context = new ApplicationContext();
		ProtocolEncoder encoder = (ProtocolEncoder) context.getBean(ResponseEncoder.class);
		ProtocolDecoder decoder = (ProtocolDecoder) context.getBean(RequestDecoder.class);
		
		ProtocolCodecFactory protocolCodecFactory = new CommonCodecFactory(encoder, decoder);
		IoHandler ioHandler = (IoHandler) context.getBean(WarriorsServerHandler.class);
		
		IoFilter cmdFilter = (IoFilter) context.getBean(CmdAttackFilter.class);
		IoFilter byteFilter = (IoFilter) context.getBean(ByteAttackFilter.class);
		
		final SocketServer socketServer = new SocketServer(protocolCodecFactory, ioHandler, byteFilter, cmdFilter);
		try {
			socketServer.start();
			log.error("NO ERROR, Yayo Server Start....");
		} catch (Exception ex) {
			log.error("SOCKET", ex);
		} finally {
			// add shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				public void run() {
					log.info("Stopping server...");
					socketServer.stop();
				}
			}));
		}
	}
}
