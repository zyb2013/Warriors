package com.yayo.warriors.module.dungeon.rule;

import org.springframework.stereotype.Component;
import com.yayo.warriors.module.dungeon.model.Dungeon;
import com.yayo.warriors.module.dungeon.types.DungeonState;
import com.yayo.warriors.module.dungeon.types.DungeonType;

/**
 * 闯关类型副本
 * @author liuyuhua
 */
@Component
public class BargedDungeonRule extends BaseDungeonRule{
	
	@Override
	public int getDungeonType() {
		return DungeonType.BARGED;
	}
	
	@Override
	public void action(Dungeon dungeon) {
		if(dungeon == null){
			logger.error("副本对象为空");
			return;
		}
		
		if(this.validation(dungeon)){
			return;
		}
		
		if(dungeon.getState() == DungeonState.SUCCESS){
			return;
		}
		
		/** 当怪物全部被杀完,即副本已经完成*/
		this.doSuccess(dungeon); //副本完成
		
	}

}
