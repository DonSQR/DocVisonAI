package com.example.conference.entity;

import org.opencv.core.Point;

public class CrossPoint {
    public String region;
    public Point crossPoint;
    public boolean flage;
    public double[] fullLine1;
    public double[] fullLine2;
    public double[] longLine1;
    public double[] longLine2;
    public CrossPoint(String region,Point crossPoint,boolean flage, double[] fullLine1, double[] fullLine2,double[] longLine1,double[] longLine2) {
        this.region = region;
        this.crossPoint = crossPoint;
        this.flage = flage;
        this.fullLine1 = fullLine1;
        this.fullLine2 = fullLine2;
        this.longLine1 = longLine1;
        this.longLine2 = longLine2;

    }

    public String getRegion() {
        return region;
    }
    public void setRegion(String region) {
        this.region = region;
    }
    public Point getCrossPoint() {
        return crossPoint;
    }
    public void setCrossPoint(Point crossPoint) {
        this.crossPoint = crossPoint;
    }

    public boolean isFlage() {
        return flage;
    }

    public void setFlage(boolean flage) {
        this.flage = flage;
    }


    public double[] getFullLine1() {
        return fullLine1;
    }

    public void setFullLine1(double[] fullLine1) {
        this.fullLine1 = fullLine1;
    }

    public double[] getFullLine2() {
        return fullLine2;
    }

    public void setFullLine2(double[] fullLine2) {
        this.fullLine2 = fullLine2;
    }



    public double[] getLongLine1() {
        return longLine1;
    }

    public void setLongLine1(double[] longLine1) {
        this.longLine1 = longLine1;
    }

    public double[] getLongLine2() {
        return longLine2;
    }

    public void setLongLine2(double[] longLine2) {
        this.longLine2 = longLine2;
    }

    @Override
    public String toString() {
        return "区域：" + region + "   交点:("
                +crossPoint.x + "," + crossPoint.y + ")" + "   falge: " + flage;
    }
}
