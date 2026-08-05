package com.example.conference.imageExtract;

import com.example.conference.ui.getpicturefromallpicture.AllPicBitmap;

import java.util.ArrayList;
import java.util.List;

public class FinalImgList {
    private static List<MyBitmap> finalImgList = new ArrayList<MyBitmap>();
    private static List<MyBitmap> finalSrcImgList = new ArrayList<MyBitmap>();
    private static String pdfNameInput;
    private static List finalSavedList = new ArrayList<>();
    private static List<AllPicBitmap> allSelectedPicList = new ArrayList<AllPicBitmap>();
    private static List<String> list = new ArrayList<String>();
    private static int position = 0;
    private static int requestCode = 0;
    private static String string = "";
    public static boolean isHandled = false;
    public FinalImgList() {
    }

    public static int getPosition() {
        return position;
    }

    public static void setPosition(int position) {
        FinalImgList.position = position;
    }

    public static List<MyBitmap> getFinalImgList() {
        return finalImgList;
    }

    public static void setFinalImgList(List<MyBitmap> finalList) {
        finalImgList = finalList;
    }

    public void addMember(MyBitmap mbitmap) {
        finalImgList.add(mbitmap);
    }

    public void delMember(int position) {
        finalImgList.remove(position);
    }

    public static String getString() {
        return string;
    }

    public static void setString(String string) {
        FinalImgList.string = string;
    }

    public static void setPdfNameInput(String pdfNameInput) {
        FinalImgList.pdfNameInput = pdfNameInput;
    }

    public static void setIsHandled(boolean isHandled) {
        FinalImgList.isHandled = isHandled;
    }

    public static boolean isIsHandled() {
        return isHandled;
    }

    public static void setFinalSrcImgList(List<MyBitmap> finalSrcImgList) {

        FinalImgList.finalSrcImgList = finalSrcImgList;
    }

    public static List<MyBitmap> getFinalSrcImgList() {

        return finalSrcImgList;
    }

    public static List<String> getList() {
        return list;
    }

    public static void setList(List<String> list, int requsetCode) {
        FinalImgList.list = list;
        FinalImgList.requestCode = requsetCode;
    }

    public static void setRequestCode(int requestCode) {
        FinalImgList.requestCode = requestCode;
    }

    public static int getRequestCode() {
        return requestCode;
    }

    public static String getPdfNameInput() {
        return pdfNameInput;
    }

    public static List getFinalSavedList() {
        return finalSavedList;
    }

    public static void setFinalSavedList(List finalSavedList) {
        FinalImgList.finalSavedList = finalSavedList;
    }

    public static void setAllSelectedPicList(List<AllPicBitmap> allSelectedPicList) {
        FinalImgList.allSelectedPicList = allSelectedPicList;
    }

    public static List<AllPicBitmap> getAllSelectedPicList() {
        return allSelectedPicList;
    }

    @Override
    public String toString() {
        return "FinalImgList{" +
                "finalImgList=" + finalImgList +
                '}';
    }
}
