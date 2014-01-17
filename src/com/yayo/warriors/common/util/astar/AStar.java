package com.yayo.warriors.common.util.astar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.yayo.warriors.module.map.types.MaskTypes;


@Deprecated
public class AStar {
    private byte[][] map;//地图(1可经由过程 0不成经由过程)
    private List<Node> openList;//开启列表
    private Set<Node> closeList;//封闭列表
    private final int COST_STRAIGHT = 10;//垂直标的目标或程度标的目标移动的路径评分
    private final int COST_DIAGONAL = 14;//斜标的目标移动的路径评分
    private int row;//行
    private int column;//列
    
    public AStar(){
    	
    }
    
    public AStar(byte[][] map,int row,int column){
        this.map=map;
        this.row=row;
        this.column=column;
        openList=new ArrayList<Node>();
        closeList=new HashSet<Node>();
    }
    
    /**
     * 初始化地图
     * @param map
     * @param row
     * @param col
     */
    public void initMap(byte[][] map,int row,int col){
        this.map=map;
        this.row=row;
        this.column=col;
        openList=new ArrayList<Node>();
        closeList=new HashSet<Node>();
    }
    
    //查找坐标(-1：错误,0：没找到,1：找到了)
    @Deprecated
    public int search(int x1,int y1,int x2,int y2){
        if(x1<0||x1>=row||x2<0||x2>=row||y1<0||y1>=column||y2<0||y2>=column){
            return -1;
        }
        if(map[x1][y1] == MaskTypes.PATH_BARRIER || map[x2][y2] == MaskTypes.PATH_BARRIER){
            return -1;
        }
        Node sNode=new Node(x1,y1,null);
        Node eNode=new Node(x2,y2,null);
        openList.add(sNode);
        List<Node> resultList = search(sNode, eNode);
        if(resultList.size()==0){
            return 0;
        }
//      for(Node node:resultList){
//      	map[node.getX()][node.getY()]=-1;
//      }
        return 1;
    }
    
    @Deprecated
    public List<Point> searchs(int x1,int y1,int x2,int y2){
        if(x1<0||x1>=row||x2<0||x2>=row||y1<0||y1>=column||y2<0||y2>=column){
            return null;
        }
        if(map[x1][y1] == MaskTypes.PATH_BARRIER || map[x2][y2] == MaskTypes.PATH_BARRIER){
            return null;
        }
        
        List<Point> list = new ArrayList<Point>();
        
        Node sNode=new Node(x1,y1,null);
        Node eNode=new Node(x2,y2,null);
        openList.add(sNode);
        List<Node> resultList = search(sNode, eNode);
        if(resultList == null){
        	return null;
        }
        
        for(Node node : resultList){
        	list.add(new Point(node.getX(),node.getY()));
        }
        
        return list;
    }
    
    
    
    //查找核默算法
    private List<Node> search(Node sNode,Node eNode){
        List<Node> resultList=new ArrayList<Node>();
        boolean isFind=false;
        Node node=null;
//        int absX = Math.abs(eNode.getX() - sNode.getX());
//        int absY = Math.abs(eNode.getY() - sNode.getY());
//        int limit = absX * absY * 2;
        int count = 0;
        while(openList.size() > 0 ){
            //取出开启列表中最低F值,即第一个存储的值的F为最低的
            node=openList.get(0);
            //断定是否找到目标点
            if(node.getX()==eNode.getX() && node.getY()==eNode.getY() ){
                isFind=true;
                break;
            }
            if(count++ > 200 && count % 100 == 0){
            	 System.err.println(String.format("从[%d,%d]到[%d,%d],遍历了%d次", sNode.getX(), sNode.getY(), eNode.getX(), eNode.getY(), count));
            }
//            if(count > limit){
//            	break;
//            }
            int tmpX = 0;
            int tmpY = 0;
            
            //上
            tmpX = node.getX() + DirectionUtil.getDeltaX(7);
            tmpY = node.getY() + DirectionUtil.getDeltaY(7);
            if(tmpX >= 0 && tmpY >= 0){
                checkPath(tmpX,tmpY,node, eNode, COST_DIAGONAL);
            }
            
            //下
            tmpX = node.getX() + DirectionUtil.getDeltaX(3);
            tmpY = node.getY() + DirectionUtil.getDeltaY(3);
            if(tmpX < row){
                checkPath(tmpX,tmpY,node, eNode, COST_DIAGONAL);
            }
            
            
            //左
            tmpX = node.getX() + DirectionUtil.getDeltaX(1);
            tmpY = node.getY() + DirectionUtil.getDeltaY(1);
            if(tmpY >= 0 && tmpX < row){
                checkPath(tmpX,tmpY,node, eNode, COST_DIAGONAL);
            }
            
            //右
            tmpX = node.getX() + DirectionUtil.getDeltaX(9);
            tmpY = node.getY() + DirectionUtil.getDeltaY(9);
            if(tmpY < column){
                checkPath(tmpX,tmpY,node, eNode, COST_DIAGONAL);
            }
            
            //左上
            tmpX = node.getX() + DirectionUtil.getDeltaX(4);
            tmpY = node.getY() + DirectionUtil.getDeltaY(4);
            if(tmpX >=0 && tmpY >= 0){
                checkPath(tmpX,tmpY,node, eNode, COST_STRAIGHT);
            }
            
            //左下
            tmpX = node.getX() + DirectionUtil.getDeltaX(2);
            tmpY = node.getY() + DirectionUtil.getDeltaY(2);
            if(tmpX < row && tmpY >= 0){
                checkPath(tmpX,tmpY,node, eNode, COST_STRAIGHT);
            }
            
            //右上
            tmpX = node.getX() + DirectionUtil.getDeltaX(8);
            tmpY = node.getY() + DirectionUtil.getDeltaY(8);
            if(tmpX >= 0 && tmpY < column){
                checkPath(tmpX,tmpY,node, eNode, COST_STRAIGHT);
            }
            
            //右下
            tmpX = node.getX() + DirectionUtil.getDeltaX(6);
            tmpY = node.getY() + DirectionUtil.getDeltaY(6);
            if(tmpX < row && tmpY < column){
                checkPath(tmpX, tmpY, node, eNode, COST_STRAIGHT);
            }
            
            //从开启列表中删除
            //添加到封闭列表中
            closeList.add(openList.remove(0));
            //开启列表中排序,把F值最低的放到最底端
            Collections.sort(openList, new NodeFComparator());
        }
        if(isFind){
            getPath(resultList, node);
        }
        System.err.println(String.format("遍历了%d次, 找到:%s", count, isFind));
        return resultList;
    }
    
    //查询此路是否能走通
    private boolean checkPath(int x,int y,Node parentNode,Node eNode,int cost){
        Node node=new Node(x, y, parentNode);
        //查找地图中是否能经由过程
        if(map[x][y] == MaskTypes.PATH_BARRIER){
            closeList.add(node);
            return false;
        }
        //查找封闭列表中是否存在
//        if(isListContains(closeList, x, y) !=-1 ){
//        	return false;
//        }
        if(closeList.contains(node)){
        	return false;
        }
        
        //查找开启列表中是否存在
        int index=-1;
        if( (index=isListContains(openList, node)) !=-1 ){
            //G值是否更小,便是否更新G,F值
            if( (parentNode.getG()+cost) < openList.get(index).getG() ){
                node.setParentNode(parentNode);
                countG(node, eNode, cost);
                countF(node);
                openList.set(index, node);
            }
        }else{
            //添加到开启列表中
            node.setParentNode(parentNode);
            count(node, eNode, cost);
            openList.add(node);
        }
        return true;
    }
    
    //凑集中是否包含某个元素(-1：没有找到,不然返回地点的索引)
//    private int isListContains(List<Node> list,int x,int y){
//        for(int i=0; i<list.size(); i++){
//            Node node = list.get(i);
//            if( node.getX()==x && node.getY()==y ){
//                return i;
//            }
//        }
//    	return -1;
//        
//    	return list.indexOf(new Node(x, y, null));
//    }
    
    private int isListContains(List<Node> list, Node node){
    	return list.indexOf(node);
    }
    
    //从终点往返回到出发点
    private void getPath(List<Node> resultList, Node node){
        if(node.getParentNode()!=null){
            getPath(resultList, node.getParentNode());
        }
        resultList.add(node);
    }
    
    //策画G,H,F值
    private void count(Node node, Node eNode,int cost){
        countG(node, eNode, cost);
        countH(node, eNode);
        countF(eNode);
    }
    //策画G值
    private void countG(Node node,Node eNode,int cost){
        if(node.getParentNode()==null){
            node.setG(cost);
        }else{
            node.setG( node.getParentNode().getG() + cost );
        }
    }
    //策画H值
    private void countH(Node node,Node eNode){
        node.setH( Math.abs( node.getX()-eNode.getX() ) + Math.abs( node.getY()-eNode.getY() ) );
    }
    //策画F值
    private void countF(Node node){
        node.setF( node.getG() + node.getH() );
    }
    
    
  //节点类
    class Node {
        private int x;//X坐标
        private int y;//Y坐标
        private Node parentNode;//父类节点
        private int g;//当前点到出发点的移动花费
        private int h;//当前点到终点的移动花费,即曼哈顿间隔|x1-x2|+|y1-y2|(忽视障碍物)
        private int f;//f=g+h
        
        public Node(int x,int y,Node parentNode){
            this.x=x;
            this.y=y;
            this.parentNode=parentNode;
        }
        
        public int getX() {
            return x;
        }
        public void setX(int x) {
            this.x = x;
        }
        public int getY() {
            return y;
        }
        public void setY(int y) {
            this.y = y;
        }
        public Node getParentNode() {
            return parentNode;
        }
        public void setParentNode(Node parentNode) {
            this.parentNode = parentNode;
        }
        public int getG() {
            return g;
        }
        public void setG(int g) {
            this.g = g;
        }
        public int getH() {
            return h;
        }
        public void setH(int h) {
            this.h = h;
        }
        public int getF() {
            return f;
        }
        public void setF(int f) {
            this.f = f;
        }

		
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		
		public boolean equals(Object obj) {
			if (this == obj){
				return true;
			}
			if (obj == null){
				return false;
			}
			if (getClass() != obj.getClass()){
				return false;
			}
			Node other = (Node) obj;
			if (!getOuterType().equals(other.getOuterType())){
				return false;
			}
			if (x != other.x){
				return false;
			}
			if (y != other.y){
				return false;
			}
			return true;
		}

		private AStar getOuterType() {
			return AStar.this;
		}
    }
    
    //节点斗劲类
    class NodeFComparator implements Comparator<Node>{
        
        public int compare(Node o1, Node o2) {
        	if(o1.getF() < o2.getF()){
        		return -1;
        	} else if(o1.getF() > o2.getF()){
        		return 1;
        	}
            return  0;
        }        
    }
    
}