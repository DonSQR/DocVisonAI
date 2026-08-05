package com.example.conference.ui.getpicturefromallpicture;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class AllPicBitmap implements Parcelable {
    private String path;
    private boolean isSelect = false;
    private Bitmap bitmap;

    public String getPath() {
        return path;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public String toString() {
        return "AllPicBitmap{" +
                "isSeleect=" + isSelect +
                '}';
    }

    public AllPicBitmap() {
    }

    protected AllPicBitmap(Parcel in) {
        path = in.readString();
        isSelect = in.readByte() != 0;
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(path);
        parcel.writeByte((byte) (isSelect ? 1 : 0));
        parcel.writeParcelable(bitmap, i);
    }

    public static final Creator<AllPicBitmap> CREATOR = new Creator<AllPicBitmap>() {
        @Override
        public AllPicBitmap createFromParcel(Parcel in) {
            return new AllPicBitmap(in);
        }

        @Override
        public AllPicBitmap[] newArray(int size) {
            return new AllPicBitmap[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
