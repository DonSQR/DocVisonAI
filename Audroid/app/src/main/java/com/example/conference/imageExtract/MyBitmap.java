package com.example.conference.imageExtract;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 */

public class MyBitmap implements Parcelable {
    String path;
    Bitmap bm;

    public MyBitmap(Bitmap bm) {
        this.bm = bm;
    }

    public MyBitmap(String path, Bitmap bm) {
        this.path = path;
        this.bm = bm;


    }

    protected MyBitmap(Parcel in) {
        path = in.readString();
        bm = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<MyBitmap> CREATOR = new Creator<MyBitmap>() {
        @Override
        public MyBitmap createFromParcel(Parcel in) {
            return new MyBitmap(in);
        }

        @Override
        public MyBitmap[] newArray(int size) {
            return new MyBitmap[size];
        }
    };

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Bitmap getBm() {
        return bm;
    }

    public void setBm(Bitmap bm) {
        this.bm = bm;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(path);
        parcel.writeParcelable(bm, i);
    }
}
