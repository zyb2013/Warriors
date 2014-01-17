package com.yayo.warriors.module.vip.rule;

import java.util.HashMap;
import java.util.Map;

import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.basedb.model.VipConfig;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeID;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.user.entity.Player;


public class VipRule {
	
	public final static int HORSE_PROPS_ID = 70001;
	public final static int TASK_PROPS_ID = 90001;
	
	

	public static void pushVipNotice(Player player, VipConfig vipConfig) {
		BulletinConfig config = NoticePushHelper.getConfig(NoticeID.OBTAIN_VIP, BulletinConfig.class);
		if (config != null) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put(NoticeRule.vipLevel, vipConfig.getVipName());
			params.put(NoticeRule.playerId, player.getId());
			params.put(NoticeRule.playerName, player.getName());
			NoticePushHelper.pushNotice(config.getId(), NoticeType.HONOR, params, config.getPriority());
		}
	}
	
}
