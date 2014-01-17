package com.yayo.warriors.common.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.basedb.ResourceService;
import com.yayo.common.socket.SessionManager;
import com.yayo.common.socket.message.Response;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.chat.facade.ChannelFacade;
import com.yayo.warriors.module.chat.model.Channel;
import com.yayo.warriors.module.chat.type.ChatChannel;
import com.yayo.warriors.module.mortal.entity.UserMortalBody;
import com.yayo.warriors.module.mortal.manager.MortalManager;
import com.yayo.warriors.module.mortal.rule.MortalRule;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.notice.vo.NoticeVo;
import com.yayo.warriors.module.user.entity.Player;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.type.Camp;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.handler.notice.NoticeCmd;

@Component
public class NoticePushHelper {
	@Autowired
	private ChannelFacade channelFacade;
	@Autowired
	private MortalManager mortalManager;
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private ResourceService resourceService;

	private static ObjectReference<NoticePushHelper> ref = new ObjectReference<NoticePushHelper>();
	
	@SuppressWarnings("unused")
	@PostConstruct
	private void init(){
		ref.set(this);
	}
	
	
	/**
	 * 推送阵营公告
	 * 
	 * @param camp
	 * @param noticeID
	 * @param noticeType
	 * @param paramsMap
	 */
	public static void pushCampNotice(Camp camp, int noticeID, NoticeType noticeType, Map<String, Object> paramsMap, int priority) {
		NoticeVo noticeVo = NoticeVo.valueOf(noticeID, noticeType, paramsMap, priority);
		Channel channels = Channel.valueOf(ChatChannel.CAMP_CHANNEL.ordinal(), camp.ordinal());
		Collection<Long> players = ref.get().channelFacade.getChannelPlayers(channels);
		pushNotice(players, noticeVo);
	}
	
	/**
	 * 向某些人推送公告
	 * @param playerIds
	 * @param noticeVO
	 */
	public static void pushNotice(Collection<Long> playerIds, NoticeVo... noticeVOs){
		Response response = Response.defaultResponse(Module.NOTICE, NoticeCmd.NOTICE_PUSHER, noticeVOs );
		ref.get().sessionManager.write(playerIds, response);
	}
	
	/**
	 * 推送世界公告
	 * @param noticeVo
	 */
	public static void pushNotice(NoticeVo noticeVo){
		if(noticeVo == null){
			return ;
		}
		Response response = Response.defaultResponse(Module.NOTICE, NoticeCmd.NOTICE_PUSHER, new NoticeVo[] {noticeVo});
		if(noticeVo.getPlayerId() <= 0L){
			ref.get().sessionManager.writeAllOnline(response);
		} else {
			ref.get().sessionManager.write(noticeVo.getPlayerId(), response);
		}
	}
	
	/**
	 * 推送公告
	 * @param noticeID
	 * @param paramsMap
	 */
	public static void pushNotice(int noticeID, NoticeType noticeType, Map<String, Object> paramsMap, int priority){
		pushNotice(NoticeVo.valueOf(noticeID, noticeType, paramsMap, priority));
	}

	public static NoticePushHelper getInstance() {
		return ref.get();
	}

	/**
	 * 取得基础数据
	 * @param <T>
	 * @param mapId
	 * @return
	 */
	public static <T> T getConfig(int id, Class<T> clazz){
		return (T) getInstance().resourceService.get(id, clazz);
	}
	
	
	/**
	 * 推送客户端公告
	 * @param player
	 * @param mortal
	 * @param type
	 * @param level
	 */
	public static void pushNoticeMessage2Client(Player player, PlayerBattle battle, UserMortalBody mortal, int level) {
		BulletinConfig config = NoticePushHelper.getConfig(NoticeID.MORTAL_LAYER_UP, BulletinConfig.class);
		if(config == null) {
			return;
		}
		
		long playerId = player.getId();
		String playerName = player.getName();
		int minLevel = getInstance().mortalManager.getMortalMinLevel(battle);
		int addLevel = MortalRule.getMortalAddedLevel(minLevel);
		if (level == addLevel) {
			HashMap<String, Object> paramsMap = new HashMap<String, Object>(3); 
			paramsMap.put(NoticeRule.playerId, playerId);
			paramsMap.put(NoticeRule.mortalLevel, minLevel);
			paramsMap.put(NoticeRule.playerName, playerName);
			NoticePushHelper.pushNotice(NoticeID.MORTAL_LAYER_UP, NoticeType.HONOR, paramsMap, config.getPriority());
		}
	}
	
	
//	/**
//	 * 推送客户端公告
//	 * @param player
//	 * @param mortal
//	 */
//	public static void pushMortalLevelUpNotice(Player player, UserMortalBody mortal) {
//		BulletinConfig config = NoticePushHelper.getConfig(NoticeID.MORTAL_LEVEL_UP, BulletinConfig.class);
//		if (config != null) {
//			HashMap<String, Object> paramsMap = new HashMap<String, Object>(3); 
//			paramsMap.put(NoticeRule.playerId, player.getId());
//			paramsMap.put(NoticeRule.level, mortal.getMortalLevel());
//			paramsMap.put(NoticeRule.playerName, player.getName());
//			NoticePushHelper.pushNotice(NoticeID.MORTAL_LEVEL_UP, NoticeType.HONOR, paramsMap, config.getPriority());
//		}
//	}

	
}
