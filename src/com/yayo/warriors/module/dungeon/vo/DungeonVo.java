package com.yayo.warriors.module.dungeon.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import com.yayo.warriors.module.dungeon.model.Dungeon;
import com.yayo.warriors.module.dungeon.types.DungeonState;

/**
 * 副本信息对象 
 * <per>用于客户端掉线下线后</per>
 * @author liuyuhua
 */
public class DungeonVo implements Serializable{
	private static final long serialVersionUID = -6670645577232104345L;
	
	/** 副本增量ID*/
	private long dungeonId;
	/** 副本的创建时间(单位:秒)*/
	private long createDate;
	/** 副本基础ID*/
	private int baseId;
	/** 副本类型*/
	private int type;
	/** 状态*/
	private DungeonState state;
	/** 副本统计信息*/
	private DungeonStaticesVo[] dungeonStaticesVos;
	
	/**
	 * 构造方法
	 * @param dungeon   副本进行中
	 * @return {@link DungeonVo} 
	 */
	public static DungeonVo valueOf(Dungeon dungeon){
		DungeonVo vo = new DungeonVo();
		vo.type = dungeon.getType();
		vo.state = dungeon.getState();
		vo.baseId = dungeon.getBaseId();
		vo.dungeonId = dungeon.getDungeonId();
		vo.createDate = dungeon.getCreateDate();
		List<DungeonStaticesVo> list = new ArrayList<DungeonStaticesVo>();
		for(Entry<Integer,List<Long>> entry : dungeon.getRound4Monsters().entrySet()){
			int round = entry.getKey();
			int number = entry.getValue().size();
			int exp = dungeon.getRound4Exp().get(round) == null ? 0 : dungeon.getRound4Exp().get(round);
			int total = dungeon.getRound4MaxMonster().get(round) == null ? 0 : dungeon.getRound4MaxMonster().get(round);
			list.add(DungeonStaticesVo.valueOf(round, number, total, exp));
		}
		
		if(list.isEmpty()){
			vo.dungeonStaticesVos = new DungeonStaticesVo[0];
		}else{
			vo.dungeonStaticesVos = list.toArray(new DungeonStaticesVo[list.size()]);
		}
		
		return vo;
	}
	
	public long getDungeonId() {
		return dungeonId;
	}
	public void setDungeonId(long dungeonId) {
		this.dungeonId = dungeonId;
	}
	public long getCreateDate() {
		return createDate;
	}
	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}
	public int getBaseId() {
		return baseId;
	}
	public void setBaseId(int baseId) {
		this.baseId = baseId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public DungeonState getState() {
		return state;
	}
	public void setState(DungeonState state) {
		this.state = state;
	}

	public DungeonStaticesVo[] getDungeonStaticesVos() {
		return dungeonStaticesVos;
	}

	public void setDungeonStaticesVos(DungeonStaticesVo[] dungeonStaticesVos) {
		this.dungeonStaticesVos = dungeonStaticesVos;
	}

}
