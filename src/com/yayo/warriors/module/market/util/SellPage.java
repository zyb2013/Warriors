package com.yayo.warriors.module.market.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

import org.springframework.stereotype.Component;



/**
 * 销售分页
 * @author liuyuhua
 */
@Component
public class SellPage {
	
	/** 放入比较器中的集合 (坑爹的add跟remove方法)*/
	private static final ConcurrentSkipListSet<SellPage.Node> NODE_SET = new ConcurrentSkipListSet<SellPage.Node>(); 
	
	/**
	 * 加入节点
	 * @param playerId   玩家的ID
	 * @param level      玩家的等级
	 */
	public boolean addNode(long playerId, int level) {
		if (isExist(playerId)) {
			return false;
		}
		return createNode(playerId,level);
	}
	
	/**
	 * 更新玩家等级
	 * @param playerId  玩家的ID
	 * @param level     玩家的等级
	 * @return
	 */
	public boolean update(long playerId, int level) {
		if (remove(playerId)) {
			createNode(playerId, level);
		}
		return false;
	}
	
	/**
	 * 删除节点元素
	 * @param playerId   玩家的ID
	 * @return
	 */
	public boolean remove(long playerId) {
		Node node = null;
		for (Node element : NODE_SET) {
			if (element.getPlayerId() == playerId) {
				node = element;
			}
		}
		if (node != null) {
			NODE_SET.remove(node);
			return true;
		}
		return false;
	}
	
	/**
	 * 根据等级的升序获取玩家列表
	 * @return
	 */
	public Collection<Long> getPlayerId4Sort() {
		Collection<Long> result = new ArrayList<Long>();
		for (Node node : NODE_SET) {
			result.add(node.getPlayerId());
		}
		return result;
	}
	
	/**
	 * 构建Node元素
	 * @param playerId   玩家的ID
	 * @param level      玩家的等级
	 * @return
	 */
	private boolean createNode(long playerId,int level) {
		try{
			SellPage.Node node = new SellPage.Node(playerId,level);
			NODE_SET.add(node);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 判断玩家是否存在
	 * (还可以用作锁的粒度开关)
	 * @param playerId    玩家的ID
	 * @return  存在  true 不存在 false
	 */
	private boolean isExist(long playerId) {
		for (Node node : NODE_SET) {            
			if (node.playerId == playerId) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 元素内部类
	 * 
	 * @author liuyuhua
	 */
	class Node implements Comparable<Node> {

		/** 玩家的ID*/
		private long playerId;
		
		/** 玩家的等级*/
		private int level;
		
		/**
		 * 构造函数
		 * @param playerId   玩家的ID
		 * @param level      玩家的等级
		 * @return
		 */
		public Node(long playerId, int level) {
			this.playerId = playerId;
			this.level    = level;
		}

		public long getPlayerId() {
			return playerId;
		}

		public void setPlayerId(long playerId) {
			this.playerId = playerId;
		}

		public int getLevel() {
			return level;
		}

		public void setLevel(int level) {
			this.level = level;
		}

		
		
		public int compareTo(Node o) {
			if (o.getLevel() < this.getLevel()) {
				return -1;
			} else if (o.playerId == this.playerId && o.level == this.level){
				return 0;
			} else {
				return 1;
			}
		}

		
		
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (int) (playerId ^ (playerId >>> 32));
			return result;
		}

		
		public String toString() {
			return "Node [playerId=" + playerId + ", level=" + level + "]";
		}

		
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (playerId != other.playerId)
				return false;
			return true;
		}

		private SellPage getOuterType() {
			return SellPage.this;
		}
		
	} 
}
