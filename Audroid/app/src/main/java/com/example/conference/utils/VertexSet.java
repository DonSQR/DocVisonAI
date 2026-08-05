package com.example.conference.utils;

import org.opencv.core.Point;

public class VertexSet {
    private Point LT = new Point();//左上角顶点
    private Point RT = new Point();//右上角顶点
    private Point LB = new Point();//左下角顶点
    private Point RB = new Point();//右下角顶点




    public VertexSet(Point lT, Point rT, Point lB, Point rB) {
        LT = lT;
        RT = rT;
        LB = lB;
        RB = rB;
    }

    public Point getLT() {
        return LT;
    }
    public void setLT(Point lT) {
        LT = lT;
    }
    public Point getRT() {
        return RT;
    }
    public void setRT(Point rT) {
        RT = rT;
    }
    public Point getLB() {
        return LB;
    }
    public void setLB(Point lB) {
        LB = lB;
    }
    public Point getRB() {
        return RB;
    }
    public void setRB(Point rB) {
        RB = rB;
    }


    @Override
    public String toString() {
        return "VertexSet [LT=" + LT + ", RT=" + RT + ", LB=" + LB + ", RB=" + RB + "]";
    }

}
