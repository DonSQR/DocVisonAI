package com.example.conference.utils;

public class LineItem {

    public String area;
    public boolean isExist;


    public LineItem(String area,boolean isExist) {
        this.area = area;
        this.isExist = isExist;

        // TODO Auto-generated constructor stub
    }
    public String getArea() {
        return area;
    }
    public void setArea(String area) {
        this.area = area;
    }
    public boolean isExist() {
        return isExist;
    }
    public void setExist(boolean isExist) {
        this.isExist = isExist;
    }
    @Override
    public String toString() {
        return "LineItem [area=" + area + ", isExist=" + isExist + "]";
    }

}
