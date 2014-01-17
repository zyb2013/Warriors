package com.yayo.warriors.module.chat.parser;

import static com.yayo.warriors.constant.CommonConstant.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.common.lock.ChainLock;
import com.yayo.common.lock.LockUtils;
import com.yayo.common.socket.message.Response;
import com.yayo.common.socket.push.Pusher;
import com.yayo.warriors.basedb.model.HorseConfig;
import com.yayo.warriors.module.chat.parser.context.AbstractGMCommandParser;
import com.yayo.warriors.module.chat.type.GmType;
import com.yayo.warriors.module.horse.entity.Horse;
import com.yayo.warriors.module.horse.manager.HorseManager;
import com.yayo.warriors.module.horse.vo.HorseVo;
import com.yayo.warriors.module.user.entity.PlayerBattle;
import com.yayo.warriors.module.user.model.UserDomain;
import com.yayo.warriors.module.user.type.Flushable;
import com.yayo.warriors.socket.Module;
import com.yayo.warriors.socket.ResponseKey;
import com.yayo.warriors.socket.handler.horse.HorseCmd;

/**
 * 修改座骑等级命令解析器
 * 
 * @author Hyint
 */
@Component
public class HorseParser extends AbstractGMCommandParser {
	@Autowired
	private HorseManager horseManager;
	@Autowired
	private Pusher pusher;
	
	
	protected String getCommand() {
		return GmType.HORSE;
	}

	
	public boolean execute(UserDomain userDomain, String[] elements) {
		PlayerBattle battle = userDomain.getBattle();
		Horse horse = horseManager.getHorse(battle);
		if(horse == null){
			return false;	
		}
		
		long playerId = userDomain.getPlayerId();
		int newLevel = Integer.valueOf(elements[2]);
		HorseConfig nextConfig = this.horseManager.getHorseConfig(newLevel);
		if (nextConfig == null) {
			LOGGER.error("玩家[{}],坐骑直接升级,坐骑配置[{}]不存在", playerId, newLevel);
			return false;
		}
		
		ChainLock lock = LockUtils.getLock(horse);
		try {
			lock.lock();
			if(battle.isDead()) {
				return false;
			}

			horse.setLevel(newLevel);
			horse.addHorseMount(nextConfig.getModel());
			battle.setFlushable(Flushable.FLUSHABLE_NORMAL);
			horse.setExp(0);
			
			dbService.submitUpdate2Queue(horse);
		} finally {
			lock.unlock();
		}
		
		horse = horseManager.getHorse(battle);
		Response response = Response.defaultResponse(Module.HORSE, HorseCmd.DEFIND_PROPS_FANCY);
		Map<String,Object> map = new HashMap<String,Object>(2);
		map.put(ResponseKey.RESULT, SUCCESS);
		map.put(ResponseKey.HORSE, HorseVo.valueOf(horse) );
		response.setValue(map);
		pusher.pushMessage(playerId, response);
		
		return true;
	}

}
