package com.example.conference.entity;

import com.example.conference.utils.LineItem;
import com.example.conference.utils.Utils;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class LineGroup {

	private List<double[]> lines = new ArrayList<double[]>();//当前区域内的所有线段

	public static List<CrossPoint> crossPoints = new ArrayList<CrossPoint>();//保存已获取和获取失败的交点

	public static List<LineItem> lineItems = new ArrayList<LineItem>();//保存线段和未获取线段的区域
	
	private int imgWidth;//原始图像宽度
	
	private int imgHeight;//原始图像高度
	
	private int type;
	
	public static final int LEFT_TOP = 1;
	
	public static final int RIGHT_TOP = 2;
	
	public static final int LEFT_BOTTOM = 3;
	
	public static final int RIGHT_BOTTOM = 4;
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public LineGroup(int imgWidth, int imgHeight, int type) {
		// TODO Auto-generated constructor stub
		this.imgWidth = imgWidth;
		this.imgHeight = imgHeight;
		this.type = type;
	}

	public static List<CrossPoint> getCrossPoints() {
		return crossPoints;
	}

	public static List<LineItem> getLineItems() {
		return lineItems;
	}



	public List<double[]> getLines() {
		return lines;
	}

	/**
	 * 获取两条线段的交点
	 * @return
	 */
	public Point getCrossPoint() throws Exception{
		List<double[]> group1 = new ArrayList<double[]>();
		List<double[]> group2 = new ArrayList<double[]>();
		//计算出每条线段水平方向的角度，按角度对线段进行分组
		double angle = 0;
		//计算出每条线段水平方向的角度，按角度对线段进行分组
		List<Item> lineList = getLinesAngle();

		if(lineList.isEmpty()){
			lineItems.add(new LineItem(Utils.type2Label(type),false));
			angle = 0;
		}else {
			lineItems.add(new LineItem(Utils.type2Label(type),true));
			angle = lineList.get(0).getAngle();
		}
		for(Item item : lineList){
			double _angle = item.getAngle();
			//角度相差30以内的认为是一组
			if(Math.abs(angle - _angle) <= 30){
				group1.add(item.getLine());
			}else{
				group2.add(item.getLine());
			}
		}
		double[] longLine1 = getLine(group1);
		double[] longLine2 = getLine(group2);
		
		double[] fullLine1 = getExtendedLine(longLine1);
		double[] fullLine2 = getExtendedLine(longLine2);
		Point crossPoint = Utils.getCrossPoint(fullLine1, fullLine2);


		if(crossPoint == null){
			crossPoints.add(new CrossPoint(Utils.type2Label(type), new Point(0,0),false,fullLine1,fullLine2,longLine1,longLine2));
			//throw new Exception(Utils.type2Label(type) + "未找到交点");

//				double[] fullLine3 = new double[]{imgWidth, 0, imgWidth-1, imgHeight};
//				crossPoint = Utils.getCrossPoint(fullLine1, fullLine3);
//				System.out.println(imgWidth+ ","+imgHeight);
			//System.out.println(Utils.type2Label(type) + "未找到交点");
		}else {
			crossPoints.add(new CrossPoint(Utils.type2Label(type), crossPoint,true,fullLine1,fullLine2,longLine1,longLine2));
		}
		return crossPoint;
	}
	
	/**
	 * 获取线段延长线
	 * @param line
	 * @return
	 */
	private double[] getExtendedLine(double[] line){
		//获取线段水平方向角度
		double angle = Utils.getAngle(line);
		if(angle == 0){//如果是水平横线
			return new double[]{0, line[1], imgWidth, line[3]};
		}else if(angle == 90){//如果是垂直竖线
			return new double[]{line[0], 0, line[2], imgHeight};
		}else{
			//将线段延伸到图像最两端
			Point startPoint = Utils.calExtendedLine(new Point(line[0], line[1]), new Point(line[2], line[3]), 0);
			Point endPoint = Utils.calExtendedLine(new Point(line[0], line[1]), new Point(line[2], line[3]), imgWidth);
			return new double[]{startPoint.x, startPoint.y, endPoint.x, endPoint.y};
		}
	}
	
	/**
	 * 将多条短线段合并为一条长线段
	 * @param group
	 * @return
	 */
	private double[] getLine(List<double[]> group){
		int[] index = null; 
		//判断是否是
		if(Utils.isVerticalLine(group)){
			index = new int[]{1, 3};
		}else{
			index = new int[]{0, 2};
		}
		//获取多个短线段中的起点与终点
		Point startPoint = Utils.getMinimumPoint(group, index);
		Point endPoint = Utils.getMaximumPoint(group, index);
		return new double[]{startPoint.x, startPoint.y, endPoint.x, endPoint.y};
	}
	
	/**
	 * 获取每条线段水平方向的角度
	 * @return
	 */
	private List<Item> getLinesAngle(){
		List<Item> lineList = new ArrayList<Item>();
		for(double[] line : lines){
			double angle = Utils.getAngle(line);
			lineList.add(new Item(angle, line));
		}
		return lineList;
	}
	
	class Item{
		private double angle;
		private double[] line;
		public Item(double angle, double[] line) {
			// TODO Auto-generated constructor stub
			this.angle = angle;
			this.line = line;
		}
		public double getAngle() {
			return angle;
		}
		public void setAngle(double angle) {
			this.angle = angle;
		}
		public double[] getLine() {
			return line;
		}
		public void setLine(double[] line) {
			this.line = line;
		}
	}
	
}
