package com.yayo.warriors.common.util.astar;

/**
 * A*算法，在一个网格中寻找最近路径 用一个String数组表示网格，.代表可以通过，X代表障碍，S是起点，G是目标 F = G + H,
 * 三个字母在程序中有出现 启发函数用曼哈顿距离，每两个格子之间耗费都是1，每次只能走一步，不能斜着走 数据结构大多数用的数组，可能不太好
 * 
 * @author Rigel
 * 
 */
public class AStar_backup {

//	String[] grid;
//	int w;
//	int h;
//	boolean found;
//	boolean[][] openList;
//	boolean[][] closeList;
//	double[][] F;
//	double[][] G;
//	double[][] H;
//	Pair[][] parent;
//
//	int[] dx = new int[] { 1, 0, -1, 0 };
//	int[] dy = new int[] { 0, 1, 0, -1 };
//
//	int startX = -1, startY = -1, goalX = -1, goalY = -1;
//
//	static class Pair {
//		int x;
//		int y;
//
//		public Pair() {
//		}
//
//		public Pair(int x, int y) {
//			this.x = x;
//			this.y = y;
//		}
//	}
//
//	public AStar_backup(String[] s) {
//		if (s.length < 2){
//			throw new RuntimeException("the grid is at least 2X2");
//		}
//		grid = s;
//		h = s.length;
//		w = s[0].length();
//		openList = new boolean[h][w];
//		closeList = new boolean[h][w];
//		F = new double[h][w];
//		G = new double[h][w];
//		H = new double[h][w];
//		parent = new Pair[h][w];
//
//		for (int i = 0; i < h; i++)
//			for (int j = 0; j < w; j++) {
//				if (grid[i].charAt(j) == 'S') {
//					startX = i;
//					startY = j;
//				}
//				if (grid[i].charAt(j) == 'G') {
//					goalX = i;
//					goalY = j;
//				}
//			}
//		if (startX == -1 || startY == -1 || goalX == -1 || goalY == -1)
//			throw new RuntimeException("there's no start or goal point");
//
//		openList[startX][startY] = true;
//		aStar(startX, startY);
//	}
//
//	public void printPath() {
//		if (!found) {
//			System.out.println("the path does not exist");
//			return;
//		}
//		Pair p = parent[goalX][goalY];
//		System.out.print("[" + goalX + ", " + goalY + "](G) <--");
//		while (p.x != startX || p.y != startY) {
//			System.out.print("[" + p.x + ", " + p.y + "] <--");
//			p = parent[p.x][p.y];
//		}
//		System.out.println("[" + p.x + ", " + p.y + "](S)");
//	}
//
//	private void aStar(int x, int y) {
//		if (x == goalX && y == goalY) {
//			found = true;
//			return;
//		}
//		boolean isOpenEmpty = true;
//		for (int i = 0; i < h; i++)
//			for (int j = 0; j < w; j++)
//				if (openList[i][j]) {
//					isOpenEmpty = false;
//					break;
//				}
//		if (isOpenEmpty) {
//			found = false;
//			return;
//		}
//
//		openList[x][y] = false;
//		closeList[x][y] = true;
//
//		for (int i = 0; i < 4; i++) {
//			int nx = x + dx[i];
//			int ny = y + dy[i];
//			if (passed(nx, ny)) {
//				if (!openList[nx][ny] || G[x][y] + 1 < G[nx][ny]) {
//					openList[nx][ny] = true;
//					parent[nx][ny] = new Pair(x, y);
//					H[nx][ny] = manDis(nx, ny, x, y);
//					G[nx][ny] = G[x][y] + 1;
//					F[nx][ny] = H[nx][ny] + G[nx][ny];
//				}
//			}
//		}
//
//		double minF = Double.MAX_VALUE;
//		int minX = -1, minY = -1;
//		for (int i = 0; i < h; i++)
//			for (int j = 0; j < w; j++)
//				if (openList[i][j]) {
//					if (F[i][j] < minF) {
//						minF = F[i][j];
//						minX = i;
//						minY = j;
//					}
//				}
//		aStar(minX, minY);
//	}
//
//	private boolean passed(int x, int y) {
//		if (x >= 0 && x < h && y >= 0 && y < w && grid[x].charAt(y) != 'X'
//				&& !closeList[x][y])
//			return true;
//		return false;
//	}
//
//	private int manDis(int x1, int y1, int x2, int y2) {
//		return Math.abs(x1 - x2) + Math.abs(y1 - y2);
//	}
//
//	public static void main(String[] args) {
//		AStar_backup a = new AStar_backup(new String[] { "S.X", "X.G" });
//		a.printPath();
//
//	}
}