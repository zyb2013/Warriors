package com.yayo.warriors.module.task.rule;

import static com.yayo.warriors.module.task.type.EventType.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import com.yayo.common.utility.Tools;
import com.yayo.warriors.basedb.model.BulletinConfig;
import com.yayo.warriors.common.helper.NoticePushHelper;
import com.yayo.warriors.module.notice.rule.NoticeRule;
import com.yayo.warriors.module.notice.type.NoticeType;
import com.yayo.warriors.module.props.type.Quality;
import com.yayo.warriors.module.user.entity.Player;

public class TaskRule {
	
	public static final int FAST_COMPLETE_GOLD = 0;
	public static final int MIN_PRACTICE_LEVEL = 35;
	public static final int MIN_LOOP_TASK_LEVEL = 30;
	public static final int MAX_LOOP_COMPLETE_COUNT = 10;
	public static final int MAX_PRACTICE_COMPLETE_COUNT = 10;
	public static final int FAST_COMPLETE_LOOP_ITEMID = 90002;
	public static final int FAST_COMPLETE_PRACTICE_ITEMID = 120040;
	public static final int REFRESH_QUALITY_ITEMID = 90001;
	public static final int REFRESH_PRACTICE_ITEMID = 120040;
	public static boolean isLoopPermitted(int level) {
		return level >= 25;
	}
	
	public static void pushTaskQualityNotice(BulletinConfig config, int quality, Player player) {
		if(quality == Quality.ORANGE.ordinal()){
			Map<String, Object> paramsMap = new HashMap<String, Object>(3);
			paramsMap.put(NoticeRule.playerId, player.getId());
			paramsMap.put(NoticeRule.playerName, player.getName());
			paramsMap.put(NoticeRule.campName, player.getCamp().getName());
			NoticePushHelper.pushNotice(config.getId(), NoticeType.HONOR, paramsMap, config.getPriority());
		}
	}
	
	private static final int[] EVENT_TYPES = { COLLECT, MESSENGE, PET_LEVEL, KILL_COLLECT, PLAYER_LEVEL, 
											   ENTER_CAMP_TASK, BUY_EQUIP_COUNT, BUY_PROPS_COUNT };
	
	private static final int[] LOOP_TASK_KILL_COUNTS = { 10, 20, 50 };

	private static final int[] PRACTICE_TASK_KILL_COUNTS = { 100, 150, 180, 200 };
	
	public static int getRandomLoopKillCount() {
		return LOOP_TASK_KILL_COUNTS[Tools.getRandomInteger(LOOP_TASK_KILL_COUNTS.length)];
	}

	public static int getRandomPracticeKillCount() {
		return PRACTICE_TASK_KILL_COUNTS[Tools.getRandomInteger(PRACTICE_TASK_KILL_COUNTS.length)];
	}
	
	public static boolean validateEventType(int eventType) {
		return ArrayUtils.contains(EVENT_TYPES, eventType);
	}
	
	public static int getRandomPropsLevel(int playerLevel) {
		return Math.min((playerLevel / 10) * 10, 40);
	}
	
	public static int getRamdonPropsCount() {
		return Tools.getRandomInteger(10) + 1;
	}
}
