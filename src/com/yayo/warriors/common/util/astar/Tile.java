package com.yayo.warriors.common.util.astar;

	/** 地图模型类 （考虑把A*多余不可以走的格子删除）*/
	public class Tile
	{

		private static byte[] DELTA_X = {0,  0,  1, 1, -1, 0, 1, -1, -1, 0, 0};
		private static byte[] DELTA_Y = {0, -1, -1, 0, -1, 0, 1,  0,  1, 1, 0};
		
//		public var data : Array; 							// 地图数据
		
		/**
		 * 是否为障碍
		 * @param startX	始点X坐标
		 * @param startY	始点Y坐标
		 * @param endX	终点X坐标
		 * @param endY	终点Y坐标
		 * @return 0为通路 1为障碍 2 为半透明 3 为摆摊位
		 */
//		public int isBlock(int startX, int startY, int endX, int endY)
//		{
//			int mapWidth   = data.length;
//			int mapHeight  = data[0].length;
//			
//			if (endX < 0 || endX >= mapWidth || endY < 0 || endY >= mapHeight)
//			{
//				return PATH_UNKNOWN;
//			}
//			return data[endX][endY];
//		}

		/** 获取目标格数据  */
//		public int getTargetTile(int x, int y)
//		{
//			return this.isBlock(0, 0, x, y);
//		}

		/** 判断A*地图从标是否可以通过的路 true 为可以通过 */
//		public function isPass(checkX : int, checkY : int) : Boolean
//		{
//			var mapWidth  : int = data.length;
//			var mapHeight : int = data[0].length;
//			
//			if (checkX < 0 || checkX >= mapWidth || checkY < 0 || checkY >= mapHeight)
//			{
//				return false;
//			}
//			return data[checkX][checkY] != PATH_BARRIER ? true : false;
//		}

		/** 判断改点是否是地图上可以行走的点 */
//		public function isPassPoint(checkPoint : Point) : Boolean
//		{
//			return isPass(checkPoint.x, checkPoint.y);
//		}

		/**
		 * 判断a点到b点 2点之间的线上是否有障碍物
		 * @param startX		始点X坐标
		 * @param startY		始点Y坐标
		 * @param endX			终点X坐标
		 * @param endY			终点Y坐标
		 * @param checkDistance 检查点距离
		 * @return true为通过 false为不可通过
		 */
//		public boolean isPassAToB(int startX, int startY, int endX, int endY, double checkDistance)
//		{
//			Point a               = new Point(startX, startY);
//			Point b               = new Point(endX, endY);
//			double distanceAToB  = Point.distance(a, b);
//			Point moveIncrement  = Vector2Extension.moveIncrement(a, b, checkDistance);
//			double moveDistance  = 0;
//			while (moveDistance < distanceAToB)
//			{
//				a 			  = a.add(moveIncrement);
//				moveDistance += checkDistance;
//				var checkP : Point = getTileStageToPoint(a.x, a.y);
//				if (this.isPass(checkP.x, checkP.y) == false)
//				{
//					return false;
//				}
//			}
//	    	
//			return true;
//		}

//		/** 获取指定点方法下一点A*格坐标  */
//		public static Point getNextPos(int x, int y, int dir)
//		{
//			Point point = new Point();
//			point.x = x + DELTA_X[dir];
//			point.y = y + DELTA_Y[dir];
//			return point;
//		}

		/**
		 * 获取45度A*单元格矩阵坐标
		 * @param px    		目标点X坐标
		 * @param py    		目标点Y坐标
		 * @param tileWidth     单元格宽
		 * @param tileHeight    单元格高
		 * @return              矩阵点坐标
		 */
//		public static function getTileStageToPoint(stageX : int, stageY : int) : Point
//		{
//			//界面坐标 计算以屏幕左上为原点的世界坐标
//			var dataTempy : int = stageX - stageY * 2;
//			if (dataTempy < 0)
//			{
//				dataTempy -= TILE_WIDTH;
//			}
//			var dataTempx : int  = stageY * 2 + stageX;
//
//			var dataTempx1 : int = (dataTempx + TITE_HALF_WIDTH) / TILE_WIDTH;
//			var dataTempy1 : int = (dataTempy + TITE_HALF_WIDTH) / TILE_WIDTH;
//			
//			//加上偏移
//			return new Point(OFFSET_TAB_X + dataTempx1, OFFSET_TAB_Y + dataTempy1);
//        }

		/**
		 * 获取45度A*单元格矩阵坐标转舞台从标（获得的是格子的中心点坐标）
		 * @param stageX    		舞台X坐标
		 * @param stageY    		舞台Y坐标
		 * @param tileWidth     单元格宽
		 * @param tileHeight    单元格高
		 * @return              矩阵点坐标
		 */
//		public static function getTilePointToStage(px : int, py : int) : Point
//		{
//        	var viewMouse : Point = new Point();
//			
//			var nOffX : int = px - OFFSET_TAB_X;		
//			var nOffY : int = py - OFFSET_TAB_Y;
//			viewMouse.x     = nOffX * TITE_HALF_WIDTH  + nOffY * TITE_HALF_WIDTH/*  - TITE_HREF_WIDTH*/;    // 斜坐标 x每加1   竖坐标x+1/2  y+1/2
//			viewMouse.y     = nOffX * TITE_HALF_HEIGHT - nOffY * TITE_HALF_HEIGHT/* - TITE_HREF_HEIGHT*/;   // 斜坐标 y每加1   竖坐标x+1/2  y-1/2
//			return viewMouse;
//        }

//		public static function getTileStageToPointByOffset(stageX : int, stageY : int, offsetX : int, offsetY : int) : Point
//		{
//			//界面坐标 计算以屏幕左上为原点的世界坐标
//			var dataTempy : int = stageX - stageY * 2;
//			if (dataTempy < 0)
//			{
//				dataTempy -= TILE_WIDTH;
//			}
//			var dataTempx : int  = stageY * 2 + stageX;
//
//			var dataTempx1 : int = (dataTempx + TITE_HALF_WIDTH) / TILE_WIDTH;
//			var dataTempy1 : int = (dataTempy + TITE_HALF_WIDTH) / TILE_WIDTH;
//
//			//加上偏移
//			return new Point(offsetX + dataTempx1, offsetY + dataTempy1);
//		}

//		public static function getTilePointToStageByOffset(px : int, py : int, offsetX : int, offsetY : int) : Point
//		{
//			var viewMouse : Point = new Point();
//
//			var nOffX : int       = px - offsetX;
//			var nOffY : int       = py - offsetY;
//			viewMouse.x = nOffX * TITE_HALF_WIDTH + nOffY * TITE_HALF_WIDTH /*  - TITE_HREF_WIDTH*/; // 斜坐标 x每加1   竖坐标x+1/2  y+1/2
//			viewMouse.y = nOffX * TITE_HALF_HEIGHT - nOffY * TITE_HALF_HEIGHT /* - TITE_HREF_HEIGHT*/; // 斜坐标 y每加1   竖坐标x+1/2  y-1/2
//			return viewMouse;
//		}

		/**
		 * 生成地图数据
		 * @param mapWidth    	地图宽
		 * @param mapHeight    	地图高
		 * @param tileWidth     单元格宽
		 * @param tileHeight    单元格高
		 * @return              地图二维数组
		 */
//		public static function createMapData(mapWidth : Number, mapHeight : Number, tileWidth : int, tileHeight : int) : Array
//		{
//			var arr : Array = new Array();
//			var w : int     = tileWidth / 2;
//			var h : int     = tileHeight / 2;
//			var col : int   = mapWidth  % tileWidth  == 0 ? mapWidth  / tileWidth  : mapWidth  / tileWidth  + 1;
//			var row : int   = mapHeight % tileHeight == 0 ? mapHeight / tileHeight : mapHeight / tileHeight + 1;
//
//			for (var i : uint = 0; i < col; i++)
//			{
//				arr[i] = new Array();
//				for (var j : uint = 0; j < row; j++)
//				{
//					arr[i][j * 2] = 0;
//					arr[i][(j * 2) + 1] = 0;
//				}
//			}
//			return arr;
//		}

		/** 获取A*格两点的格子数距离  */
//		public static function distance(startX : int, startY : int, endX : int, endY : int) : int
//		{
//			var dx : int = Math.abs(startX - endX);
//			var dy : int = Math.abs(startY - endY);
//			return Math.max(dx, dy);
//		}

		/** 通过A*格获取移动方法  */
//		public static function direction(startX : int, startY : int, endX : int, endY : int) : int
//		{
//			if (startX < endX)
//			{
//				if (startY < endY)
//				{
//					return 6;
//				}
//				else if (startY > endY)
//				{
//					return 2;
//				}
//				else
//				{
//					return 3;
//				}
//			}
//			else if (startX > endX)
//			{
//				if (startY < endY)
//				{
//					return 8;
//				}
//				else if (startY > endY)
//				{
//					return 4;
//				}
//				else
//				{
//					return 7;
//				}
//			}
//			else
//			{
//				if (startY < endY)
//				{
//					return 9;
//				}
//				else if (startY > endY)
//				{
//					return 1;
//				}
//			}
//			return 0;
//		}

//		/** 寻找最近的点
//		 * @startPoint         开始的点
//		 * @movePoint          结束的点
//		 * @isAttack           是否攻击
//		 * */
//		public function getNearPoint(startPoint : Point, movePoint : Point, isAttack : Boolean = false) : Point
//		{
//			var endPoint : Point;
//			//判断是否是可以走的点
//			if (isPass(movePoint.x, movePoint.y))
//			{
//				endPoint = movePoint;
//			}
//			else
//			{
//				//判断是否可以攻击
//				if (isAttack)
//				{
//					if ((startPoint.x - movePoint.x) > 0)
//					{
//						if (isPassPoint(accountPoint(movePoint, 1)))
//						{
//							endPoint = accountPoint(movePoint, 1);
//						}
//						else if (isPassPoint(accountPoint(movePoint, 4)))
//						{
//							endPoint = accountPoint(movePoint, 4);
//						}
//						else if (isPassPoint(accountPoint(movePoint, 7)))
//						{
//							endPoint = accountPoint(movePoint, 7);
//						}
//						else if (isPassPoint(accountPoint(movePoint, 8)))
//						{
//							endPoint = accountPoint(movePoint, 8);
//						}
//						else if (isPassPoint(accountPoint(movePoint, 9)))
//						{
//							endPoint = accountPoint(movePoint, 9);
//						}
//						else if (isPassPoint(accountPoint(movePoint, 6)))
//						{
//							endPoint = accountPoint(movePoint, 6);
//						}
//						else if (isPassPoint(accountPoint(movePoint, 3)))
//						{
//							endPoint = accountPoint(movePoint, 3);
//						}
//						else if (isPassPoint(accountPoint(movePoint, 2)))
//						{
//							endPoint = accountPoint(movePoint, 2);
//						}
//
//					}
//					else
//					{
//						if (isPassPoint(accountPoint(movePoint, 3)))
//						{
//							endPoint = accountPoint(movePoint, 3);
//						}
//						else if (isPassPoint(accountPoint(movePoint, 6)))
//						{
//							endPoint = accountPoint(movePoint, 6);
//						}
//						else if (isPassPoint(accountPoint(movePoint, 9)))
//						{
//							endPoint = accountPoint(movePoint, 9);
//						}
//						else if (isPassPoint(accountPoint(movePoint, 8)))
//						{
//							endPoint = accountPoint(movePoint, 8);
//						}
//						else if (isPassPoint(accountPoint(movePoint, 7)))
//						{
//							endPoint = accountPoint(movePoint, 7);
//						}
//						else if (isPassPoint(accountPoint(movePoint, 4)))
//						{
//							endPoint = accountPoint(movePoint, 4);
//						}
//						else if (isPassPoint(accountPoint(movePoint, 1)))
//						{
//							endPoint = accountPoint(movePoint, 1);
//						}
//						else if (isPassPoint(accountPoint(movePoint, 2)))
//						{
//							endPoint = accountPoint(movePoint, 2);
//						}
//					}
//				}
//				else
//				{
//					var pathFinder : AStar = new AStar(this);
//					pathFinder.isBalk = true;
//					var path : Array = pathFinder.find(startPoint.x, startPoint.y, movePoint.x, movePoint.y);
//
//					//切割最近的点
//					if (path != null && path.length > 2)
//					{
//						for (var m : int = path.length - 1; m > 0; m--)
//						{
//							if (isPass(path[m][0], path[m][1]))
//							{
//								endPoint = new Point(path[m][0], path[m][1]);
//								break;
//							}
//						}
//					}
//				}
//			}
//
//			return endPoint;
//		}
//
//		/** 移动到的目标的点**/
//		public static function accountPoint(movePoint : Point, dir : int) : Point
//		{
//			//要移动到的点
//			var endPoint : Point = movePoint.clone();
//
//			switch (dir)
//			{
//				case 1:
//					endPoint.x -= 1;
//					endPoint.y += 1;
//					break;
//				case 2:
//					endPoint.y += 1;
//					break;
//				case 3:
//					endPoint.x += 1;
//					endPoint.y += 1;
//					break;
//				case 4:
//					endPoint.x -= 1;
//					break;
//				case 6:
//					endPoint.x += 1;
//					break;
//				case 7:
//					endPoint.x -= 1;
//					endPoint.y -= 1;
//					break;
//				case 8:
//					endPoint.y -= 1;
//					break;
//				case 9:
//					endPoint.x += 1;
//					endPoint.y -= 1;
//					break;
//			}
//
//			return endPoint;
//		}
//	}
}