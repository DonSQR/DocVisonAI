package com.example.conference.utils;

import android.graphics.Bitmap;

public class MyPdfBitmap {
    private String pdfName;
    private Bitmap bp;
    private String filePath;
    public MyPdfBitmap() {
    }

    public void setPdfName(String pdfName) {
        this.pdfName = pdfName;
    }

    public String getPdfName() {
        return pdfName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "MyPdfBitmap{" +
                "pdfName='" + pdfName + '\'' +
                ", bp=" + bp +
                '}';
    }
}
