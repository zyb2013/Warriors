package com.yayo.warriors.module.buffer.vo;

import java.io.Serializable;

import com.yayo.warriors.module.buffer.model.Buffer;

/**
 * BufferVO对象
 * 
 * @author Hyint
 */
public class BufferVO implements Serializable {
	private static final long serialVersionUID = 5358996449215814497L;
	
	/** 效果ID */
	private int id;
	
	/** 效果结束时间 */
	private long endTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return "BufferVO [id=" + id + ", endTime=" + endTime + "]";
	}
	
	public static BufferVO valueOf(int bufferId, long endTime) {
		BufferVO bufferVO = new BufferVO();
		bufferVO.id = bufferId;
		bufferVO.endTime = endTime;
		return bufferVO;
	}
	
	public static BufferVO valueOf(Buffer buffer) {
		BufferVO bufferVO = new BufferVO();
		bufferVO.id = buffer.getId();
		bufferVO.endTime = buffer.getEndTime();
		return bufferVO;
	}
	
}
