package com.yayo.warriors.common.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.chat.model.ChatResponse;
import com.yayo.warriors.module.props.entity.BackpackEntry;
import com.yayo.warriors.module.props.model.GoodsVO;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.chat.ChatCmd;
import com.yayo.warriors.socket.handler.props.PropsCmd;

/**
 * 消息推送类
 * 
 * @author Hyint
 */
@Component
public class MessagePushHelper {
	
	private static final ObjectReference<MessagePushHelper> ref = new ObjectReference<MessagePushHelper>();
	
	@Autowired
	private VOFactory voFactory;
	@Autowired
	private SessionManager sessionManager;
	private static final Logger LOGGER = LoggerFactory.getLogger(MessagePushHelper.class);
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}
	
	private static MessagePushHelper getInstance() {
		return ref.get();
	}
	
	/**
	 * 推送聊天公告信息
	 * 
	 * @param channel 	显示频道
	 * @param chatInfo 	信息
	 */
	public static void pushChat2Client(Collection<Long> idList, ChatResponse chatResponse) {
		if(idList != null && !idList.isEmpty() && chatResponse != null) {
			Response response = Response.defaultResponse(Module.CHAT, ChatCmd.CHAT_PUSH, chatResponse);
			getInstance().sessionManager.write(idList, response);
		}
	}

	/**
	 * 推送用户道具到客户端
	 * 
	 * @param playerIdList			角色ID列表
	 * @param backpackAdjust		是否背包整理
	 * @param backpackEntries		背包实体对象
	 */
	public static void pushUserProps2Client(long playerId, int backpackType, boolean backpackAdjust, BackpackEntry...backpackEntries) {
		if( backpackEntries.length <= 0) {
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(4);
		List<BackpackEntry> entryArrays = Arrays.asList(backpackEntries);
		long[] coolTimeArray = getInstance().voFactory.getCoolTime(playerId, entryArrays);
		resultMap.put(ResponseKey.TYPE, backpackAdjust);
		resultMap.put(ResponseKey.ENTRY, backpackEntries);
		resultMap.put(ResponseKey.COOL_TIME, coolTimeArray);
		resultMap.put(ResponseKey.PACKAGE_TYPE, backpackType);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("CoolTime: {}, Entry:[{}]", Arrays.toString(coolTimeArray), Arrays.toString(backpackEntries));
		}
		
		Response response = Response.defaultResponse(Module.PROPS, PropsCmd.CHECK_ROLE_PACKINFO, resultMap);
		getInstance().sessionManager.write(playerId, response);
	}
	
	/**
	 * 推送用户物品数量改变
	 * @param playerId				角色id
	 * @param goodsVO				物品变化VO
	 */
	public static void pushGoodsCountChange2Client(long playerId, GoodsVO...goodsVO){
		Response response = Response.defaultResponse(Module.PROPS, PropsCmd.PUSH_GOODS_CHANGE, goodsVO);
		getInstance().sessionManager.write(playerId, response);
	}
	
	public static void pushGoodsCountChange2Client(long playerId, Collection<GoodsVO> goodsVOs){
		if(goodsVOs != null && !goodsVOs.isEmpty()) {
			Response response = Response.defaultResponse(Module.PROPS, PropsCmd.PUSH_GOODS_CHANGE, goodsVOs.toArray() );
			getInstance().sessionManager.write(playerId, response);
		}
	}

	/**
	 * 推送用户道具到客户端
	 * 
	 * @param playerIdList			角色ID列表
	 * @param backpackAdjust		是否背包整理
	 * @param backpackEntries		背包实体对象
	 */
	public static <T extends BackpackEntry> void pushUserProps2Client(long playerId, int backpackType, boolean backpackAdjust, Collection<T> backpackEntries) {
		if( backpackEntries == null || backpackEntries.isEmpty()) {
			return;
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>(4);
		List<T> list = new ArrayList<T>(backpackEntries);
		long[] coolTimeArray = getInstance().voFactory.getCoolTime(playerId, list);
		resultMap.put(ResponseKey.TYPE, backpackAdjust);
		resultMap.put(ResponseKey.COOL_TIME, coolTimeArray);
		resultMap.put(ResponseKey.PACKAGE_TYPE, backpackType);
		resultMap.put(ResponseKey.ENTRY, backpackEntries.toArray());
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("CoolTime: {}, Entry:[{}]", Arrays.toString(coolTimeArray), Arrays.toString(backpackEntries.toArray()));
		}
		
		Response response = Response.defaultResponse(Module.PROPS, PropsCmd.CHECK_ROLE_PACKINFO, resultMap);
		getInstance().sessionManager.write(playerId, response);
	}
	
//	/**
//	 * 推送修改的背包实体.(只包括增加, 移除)
//	 * 
//	 * @param playerId
//	 * @param modifyEntries
//	 */
//	public static void pushModifyEntry2Client(long playerId, ModifyEntry...modifyEntries) {
//		if(modifyEntries.length > 0) {
//			Response response = Response.defaultResponse(Module.PROPS, PropsCmd.PUSH_UPDATE_ENTRY, modifyEntries);
//			getInstance().sessionManager.write(playerId, response);
//		}
//	}
}
