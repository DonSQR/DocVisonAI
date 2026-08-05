package com.example.conference.utils;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class imgSave {
    private Context context;
    public imgSave(){
        super();
    }
    public imgSave(Context context){
        this.context = context;
    }
    /*
     **创建文件夹
     * filepath文件夹名字
     * filename文件名字
     */
    public void SaveImgToFile(String filepath,String filename,Bitmap bitmap){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //创建SDcard文件夹
            String filedir = Environment.getExternalStorageDirectory()+"/"
                    +context.getPackageName()+"/"+filepath;
            File file = new File(filedir);
            if (!file.exists()){
                file.mkdirs();
            }
            File file_name = new File(filedir,filename);
            SaveImg(file_name,bitmap);
            Log.e("SDcard",filedir);
        }else {
            //创建本地文件夹
            File file_name = new File(context.getDir(filepath,context.MODE_PRIVATE),filename);
            Log.e("localSD",context.getDir(filepath,context.MODE_PRIVATE).getPath());
            SaveImg(file_name,bitmap);

        }
    }
    //获取保存的图片
    public Bitmap GetImgFromFile(String filepath,String filename){
        Bitmap bitmap = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            String filedir = Environment.getExternalStorageDirectory()+"/"
                    +context.getPackageName()+"/"+filepath;
            File file_name = new File(filedir,filename);
            bitmap = ReadImg(file_name);
        }else {
            File file_name = new File(context.getDir(filepath,context.MODE_PRIVATE),filename);
            bitmap = ReadImg(file_name);
        }
        return bitmap;
    }

    //保存图片
    private void SaveImg(File file_name,Bitmap bitmap){
        try {
            FileOutputStream fos = new FileOutputStream(file_name);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Bitmap ReadImg(File file_name ){
        Bitmap bitmap = null;
        try {
            FileInputStream fis = new FileInputStream(file_name);
            bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}

