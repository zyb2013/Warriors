package com.yayo.warriors.module.fight.helper;

import static com.yayo.warriors.module.buffer.rule.BufferRule.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yayo.warriors.basedb.adapter.SkillService;
import com.yayo.warriors.bo.ObjectReference;
import com.yayo.warriors.module.buffer.entity.UserBuffer;
import com.yayo.warriors.module.buffer.model.Buffer;
import com.yayo.warriors.module.monster.model.MonsterBuffer;

/**
 * Buffer帮助类
 * 
 * @author Hyint
 */
@Component
public class BufferHelper {
	
	private static final ObjectReference<BufferHelper> ref = new ObjectReference<BufferHelper>();
	
	@Autowired
	private SkillService skillService;
	
	@PostConstruct
	protected void init() {
		ref.set(this);
	}

	/**
	 * 获得 {@link BufferHelper} Singleton实例 
	 * 
	 * @return {@link BufferHelper}
	 */
	private static BufferHelper getInstance() {
		return ref.get();
	}

	/**
	 * 是否有指定类型的BUFF对象
	 * 
	 * @param  buffers			Buffer列表
	 * @param  bufferTypes		BUFF类型
	 * @return {@link Boolean}	true-有该BUFF, false-没有该BUFF
	 */
	private boolean hasBufferInBuffers(Map<Integer, Buffer> buffers, int...bufferTypes) {
		if(buffers == null || buffers.isEmpty() || bufferTypes.length <= 0) {
			return false;
		}
		
		Set<Integer> bufferIds = new HashSet<Integer>(buffers.keySet());
		Set<Integer> effectIds = skillService.getEffectIdByBufferType(bufferTypes);
		effectIds.retainAll(bufferIds);
		for (Integer effectId : effectIds) {
			Buffer buffer = buffers.get(effectId);
			if(buffer != null && !buffer.isTimeOut()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 角色是否在定身状态.
	 * 
	 * @param	buffers			角色的BUFFER列表
	 * @return {@link Boolean}	角色
	 */
	public static boolean isUnitInImmobilize(Map<Integer, Buffer> buffers) {
		return getInstance().hasBufferInBuffers(buffers, IMMOBILIZE_BUFFER_TYPE);
	}
	
	/**
	 * 角色是否在定身中
	 * 
	 * @param  userBuffer		用户的BUFF效果
	 * @return {@link Boolean}	true-定身中, false-自由状态
	 */
	public static boolean isPlayerInImmobilize(UserBuffer userBuffer) {
		boolean hasInImmobilize = false;
		if(userBuffer != null) {
			hasInImmobilize = isUnitInImmobilize(userBuffer.getDeBufferInfos());
		} 
		return hasInImmobilize;
	}

	/**
	 * 怪物是否在定身中
	 * 
	 * @param  monsterBuffer	怪物BUFF效果集合
	 * @return {@link Boolean}	true-定身中, false-自由状态
	 */
	public static boolean isMonsterInImmobilize(MonsterBuffer monsterBuffer) {
		boolean hasInImmobilize = false;
		if(monsterBuffer != null) {
			hasInImmobilize = isUnitInImmobilize(monsterBuffer.getDebufferInfoMap());
		} 
		return hasInImmobilize;
	}
	
	/**
	 * 获得战斗单位的 Buffer对象
	 * 
	 * @param  buffers			战斗单位的Buffer列表
	 * @param  bufferTypes		buffer类型数组
	 * @return {@link Buffer}	Buffer效果对象
	 */
	public static Buffer getUnitBuffer(Map<Integer, Buffer> buffers, int...bufferTypes) {
		if(buffers == null || buffers.isEmpty()) {
			return null;
		}
		
		Set<Integer> bufferIds = new HashSet<Integer>(buffers.keySet());
		Set<Integer> effectIds = getInstance().skillService.getEffectIdByBufferType(bufferTypes);
		effectIds.retainAll(bufferIds);
		for (Integer effectId : effectIds) {
			Buffer current = buffers.get(effectId);
			if(current != null && !current.isTimeOut()) {
				return current;
			}
		}
		return null;
	}

	/**
	 * 是否可以附加Buffer到目标者身上
	 * 
	 * @param  buffer			{@link Buffer}
	 * @param  skillLevel		技能等级
	 * @return {@link Boolean}	是否可以附加到战斗单位身上
	 */
	public static boolean canAddBuffer2Unit(Buffer buffer, int skillLevel) {
		return buffer == null || buffer.isTimeOut() || buffer.getLevel() < skillLevel;
	}
}
