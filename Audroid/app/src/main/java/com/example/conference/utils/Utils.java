package com.example.conference.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.example.conference.R;
import com.example.conference.constants.Constants;
import com.example.conference.entity.AreaLines;
import com.example.conference.entity.CrossPoint;
import com.example.conference.entity.LineGroup;
import com.example.conference.imageExtract.FinalImgList;
import com.example.conference.imageExtract.MyBitmap;
import com.example.conference.imageExtract.Myadapter;
import com.example.conference.ui.getpicturefromallpicture.AllPicBitmap;
import com.example.conference.ui.handlefragment.HandleFragment;
import com.example.conference.ui.selectfragment.SelectFragment;
import com.donkingliang.imageselector.utils.ImageSelector;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import rx.functions.Action1;


public class Utils {

    public static void output(String path, Mat img) {
        if (Constants.DEBUG) {
            Imgcodecs.imwrite(path, img);
        }
    }

    /**
     * 获取两条线段的夹角
     *
     * @param o 夹角point
     * @param s
     * @param e
     * @return
     */
    public static double calAngle(Point o, Point s, Point e) {
        double cosfi = 0, fi = 0, norm = 0;
        double dsx = s.x - o.x;
        double dsy = s.y - o.y;
        double dex = e.x - o.x;
        double dey = e.y - o.y;

        cosfi = dsx * dex + dsy * dey;
        norm = (dsx * dsx + dsy * dsy) * (dex * dex + dey * dey);
        cosfi /= Math.sqrt(norm);

        if (cosfi >= 1.0) return 0;
        if (cosfi <= -1.0) return Math.PI;
        fi = Math.acos(cosfi);
        if (180 * fi / Math.PI < 180) {
            return 180 * fi / Math.PI;
        } else {
            return 360 - 180 * fi / Math.PI;
        }
    }

    /**
     * 判断线段组成的直线是竖线还是横线
     *
     * @param lines
     * @return
     */
    public static boolean isVerticalLine(List<double[]> lines) {
        double spacingX = 0;
        double spacingY = 0;
        for (double[] line : lines) {
            double x1 = line[0];
            double y1 = line[1];
            double x2 = line[2];
            double y2 = line[3];
            spacingX += Math.abs(x1 - x2);
            spacingY += Math.abs(y1 - y2);
        }
        //y值增量比x值增量大认为是更倾向于竖线
        if (spacingY / lines.size() > spacingX / lines.size()) {
            return true;
        }
        return false;
    }

    /**
     * 获取线段中最大的点
     *
     * @param lines
     * @param indexs
     * @return
     */
    public static Point getMaximumPoint(List<double[]> lines, int... indexs) {
        double max = 0;
        double[] point = new double[2];
        for (double[] line : lines) {
            for (int index : indexs) {
                if (line[index] > max) {
                    max = line[index];
                    if (index == 0 || index == 2) {
                        point[0] = max;
                        point[1] = line[index + 1];
                    } else {
                        point[0] = line[index - 1];
                        point[1] = max;
                    }
                }
            }
        }
        return new Point(point[0], point[1]);
    }

    /**
     * 获取线段中最小的点
     *
     * @param lines
     * @param indexs
     * @return
     */
    public static Point getMinimumPoint(List<double[]> lines, int... indexs) {
        double min = Double.MAX_VALUE;
        double[] point = new double[2];
        for (double[] line : lines) {
            for (int index : indexs) {
                if (line[index] < min) {
                    min = line[index];
                    if (index == 0 || index == 2) {
                        point[0] = min;
                        point[1] = line[index + 1];
                    } else {
                        point[0] = line[index - 1];
                        point[1] = min;
                    }
                }
            }
        }
        return new Point(point[0], point[1]);
    }

    /**
     * 获取线段角度
     *
     * @param line
     * @return
     */
    public static double getAngle(double[] line) {
        Point p1 = new Point(line[0], line[1]);
        Point p2 = new Point(line[2], line[3]);
        Point p3 = null;
        if (p2.y < p1.y) {
            p3 = p2;
            p2 = p1;
            p1 = p3;
        }
        p3 = new Point(0, p2.y);
        return Utils.calAngle(p2, p1, p3);
    }

    /**
     * 计算延长线坐标
     *
     * @param a
     * @param b
     * @param x
     * @return
     */
    public static Point calExtendedLine(Point a, Point b, int x) {
        double k0 = (b.y - a.y) / (b.x - a.x);
        double e = (b.y - k0 * b.x);
        double y = k0 * x + e;
        return new Point(x, y);
    }

    /**
     * 计算交点
     *
     * @param lsegA
     * @param lsegB
     * @return
     */
    public static Point getCrossPoint(double[] lsegA, double[] lsegB) {
        double x;
        double y;
        double x1 = lsegA[0];
        double y1 = lsegA[1];
        double x2 = lsegA[2];
        double y2 = lsegA[3];
        double x3 = lsegB[0];
        double y3 = lsegB[1];
        double x4 = lsegB[2];
        double y4 = lsegB[3];
        double k1 = Double.MAX_VALUE;
        double k2 = Double.MAX_VALUE;
        boolean flag1 = false;
        boolean flag2 = false;

        if ((x1 - x2) == 0)
            flag1 = true;
        if ((x3 - x4) == 0)
            flag2 = true;

        if (!flag1)
            k1 = (y1 - y2) / (x1 - x2);
        if (!flag2)
            k2 = (y3 - y4) / (x3 - x4);

        if (k1 == k2)
            return null;

        if (flag1) {
            if (flag2)
                return null;
            x = x1;
            if (k2 == 0) {
                y = y3;
            } else {
                y = k2 * (x - x4) + y4;
            }
        } else if (flag2) {
            x = x3;
            if (k1 == 0) {
                y = y1;
            } else {
                y = k1 * (x - x2) + y2;
            }
        } else {
            if (k1 == 0) {
                y = y1;
                x = (y - y4) / k2 + x4;
            } else if (k2 == 0) {
                y = y3;
                x = (y - y2) / k1 + x2;
            } else {
                x = (k1 * x2 - k2 * x4 + y4 - y2) / (k1 - k2);
                y = k1 * (x - x2) + y2;
            }
        }
        if (between(x1, x2, x) && between(y1, y2, y) && between(x3, x4, x) && between(y3, y4, y)) {
            Point point = new Point(x, y);
            if (point.equals(new Point(lsegA[0], lsegA[1])) || point.equals(new Point(lsegA[2], lsegA[3])))
                return null;
            return point;
        } else {
            if (Double.isNaN(x) || Double.isNaN(y)) {
                return null;
            }else {
                return new Point(x, y);
            }
        }
    }

    public static boolean between(double a, double b, double target) {
        if (target >= a - 0.01 && target <= b + 0.01 || target <= a + 0.01 && target >= b - 0.01)
            return true;
        else
            return false;
    }

    public static String type2Label(int type) {
        String label = "";
        switch (type) {
            case LineGroup.LEFT_TOP:
                label = "左上区域";
                break;
            case LineGroup.LEFT_BOTTOM:
                label = "左下区域";
                break;
            case LineGroup.RIGHT_TOP:
                label = "右上区域";
                break;
            case LineGroup.RIGHT_BOTTOM:
                label = "右下区域";
                break;
            default:
                label = "区域类型错误";
        }
        return label;
    }

    /**
     * 获取轮廓最大的
     *
     * @param f_contours
     * @return
     */
    public static MatOfPoint getMaximum(List<MatOfPoint> f_contours) {
        MatOfPoint mpoint = null;
        double maxArea = 0;


        ArrayList<MatOfPoint> approxCurves = new ArrayList<MatOfPoint>();//存取拟合多边形的点集
        for(MatOfPoint p : f_contours) {
            if(p.toArray().length > 500) {
                approxCurves.add(p);
            }
        }
        for (MatOfPoint p : approxCurves){
            MatOfPoint2f point2f = new MatOfPoint2f(p.toArray());
            RotatedRect rect = Imgproc.minAreaRect(point2f);
            double currentArea = rect.size.height * rect.size.width;
            if (currentArea > maxArea)
            {
                mpoint = p;
                maxArea = currentArea;
            }

        }

        return mpoint;
    }
    //获取当前时间
    public static Long getSystemTime() {
        //yyyy年MM月dd日 HH时MM分ss秒
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Long times = System.currentTimeMillis();
        Date date = new Date(times);
        String time = sdf.format(date).toString();
        long timeint = 0;
        try {
            timeint = Long.valueOf(time).longValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("timeint", "timeint" + timeint);
        return timeint;
    }

    public static Size getOutputSize(Point ltp, Point rtp, Point rbp, Point lbp) {
        double h1 = Math.sqrt(Math.pow(ltp.x - lbp.x, 2) + Math.pow(ltp.y - lbp.y, 2));
        double h2 = Math.sqrt(Math.pow(rtp.x - rbp.x, 2) + Math.pow(rtp.y - rbp.y, 2));

        double w1 = Math.sqrt(Math.pow(ltp.x - rtp.x, 2) + Math.pow(ltp.y - rtp.y, 2));
        double w2 = Math.sqrt(Math.pow(lbp.x - rbp.x, 2) + Math.pow(lbp.y - rbp.y, 2));
        return new Size(Math.max(w1, w2), Math.max(h1, h2));
    }

    /**
     * 将线段放置到对应区域的对象中
     *
     * @param centerPoint
     * @param imgWidth
     * @param imgHeight
     * @param lines
     */
    public static AreaLines putLines(Point centerPoint, Mat lines, int imgWidth, int imgHeight) {
        AreaLines areaLines = new AreaLines(imgWidth, imgHeight);
        for (int i = 0; i < lines.rows(); i++) {
            double[] line = lines.get(i, 0);
            Point p1 = new Point(line[0], line[1]);
            Point p2 = new Point(line[2], line[3]);

            putLines(centerPoint, p1, areaLines, line);
            putLines(centerPoint, p2, areaLines, line);
        }
        return areaLines;
    }

    /**
     * 将线段放置到对应区域的对象中
     *
     * @param centerPoint
     * @param point
     * @param areaLines
     * @param line
     */
    private static void putLines(Point centerPoint, Point point, AreaLines areaLines, double[] line) {
        if (point.x <= centerPoint.x && point.y <= centerPoint.y) {//左上区域
            if (!areaLines.getLeft_top_area().getLines().contains(line)) {
                areaLines.getLeft_top_area().getLines().add(line);
            }
        } else if (point.x > centerPoint.x && point.y <= centerPoint.y) {//右上区域
            if (!areaLines.getRight_top_area().getLines().contains(line)) {
                areaLines.getRight_top_area().getLines().add(line);
            }
        } else if (point.x <= centerPoint.x && point.y > centerPoint.y) {//左下区域
            if (!areaLines.getLeft_bottom_area().getLines().contains(line)) {
                areaLines.getLeft_bottom_area().getLines().add(line);
            }
        } else {
            if (!areaLines.getRight_bottom_area().getLines().contains(line)) {//右下区域
                areaLines.getRight_bottom_area().getLines().add(line);
            }
        }
    }

    /**
     * 获取图像的中心点
     *
     * @param img
     * @return
     */
    public static Point getCenterPoint(Mat img) {
        int row = img.rows();
        int col = img.cols();
        int centerX = col / 2 - 1;
        int centerY = row / 2 - 1;
        return new Point(centerX, centerY);
    }

    public static void getContactList(RxPermissions rxPermissions, GridView gridView,
                                      Context context, List<MyBitmap> finalList, int MaxSize, Long systemTime1, Long systemTime2) {
        rxPermissions.requestEach(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Action1<Permission>() {
                    @Override
                    public void call(Permission permission) {
                        if (permission.granted) {
                            //将合适的照片放在list里面
                            final String[] projection = {
                                    MediaStore.Images.Media._ID,
                                    MediaStore.Images.Media.DISPLAY_NAME,
                                    MediaStore.Images.Media.DATA
                            };
                            final String orderBy = MediaStore.Images.Media.DISPLAY_NAME;
                            final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    List<MyBitmap> list2 = getContentProvider(context, uri, projection, orderBy, systemTime1, systemTime2);
                                    Log.e("list", "call" + list2.toString() + ".size" + list2.size());
                                    if (list2 != null) {
                                        if (list2.size() > MaxSize) {
                                            list2 = list2.subList(list2.size() - MaxSize, list2.size());
                                        }
                                        finalList.addAll(list2);
                                        gridView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                final Myadapter myadapter = new Myadapter(context, finalList);
                                                boolean isLegal = true;
                                                //Log.e("测试", "finalList: " + finalList.get(0).getPath());
//                                                for (int i = 0; i<finalList.size();i++){
//                                                    if (finalList.get(i).getPath().length()<5){
//                                                        isLegal = false;
//                                                        break;
//                                                    }
//                                                }
//                                                if (isLegal){
//
//                                                }
                                                FinalImgList.setFinalSrcImgList(finalList);


                                                gridView.setAdapter(myadapter);
                                                if (finalList.size() == 0) {
                                                    // confirm.setVisibility(View.GONE);
                                                } else {
                                                    //selectPic.setVisibility(View.GONE);
                                                }
                                            }
                                        });

                                    }
                                }
                            }).start();
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            Toast.makeText(context, "您拒绝了读取照片的权限", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(context, "您拒绝了读取照片的权限", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    //获取打开相机后拍摄的照片
    public static List<MyBitmap> getContentProvider(Context context, Uri uri, String[] projection, String orderBy, Long systemTime1, Long systemTime2) {
        List<MyBitmap> lists = new ArrayList<MyBitmap>();
        HashSet<String> set = new HashSet<String>();
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, orderBy);
        if (null == cursor) {
            return null;
        }
        while (cursor.moveToNext()) {
            Log.e("lengthpro", "getContentProvider: " + projection.length);
            for (int i = 0; i < projection.length; i++) {
                String string = cursor.getString(i);
                Log.e("String", "cursor.getString(i) : " + cursor.getString(i));
                if (string != null) {
                    int length = string.length();
                    String ss = null;
                    if (length >= 30) {
                        Log.e("getInto_length >= 30", "进入length >= 30  length =" + length);
                        ss = string.substring(length - 23, length);
                        String substring = ss.substring(0, 4);
                        String hen = ss.substring(12, 13);
                        if (substring.equals("IMG_") && hen.equals("_")) {
                            String laststring = ss.substring(4, 19).replace("_", "");
                            try {
                                long time = Long.valueOf(laststring).longValue();
                                Log.e("time", "time: " + time + "  systemTime1: " + systemTime1 + "  systemTime2: " + systemTime2);
                                if (time > systemTime1 && time <= systemTime2) {
                                    Log.e("gotoAddString", "addStringSuccess");
                                    set.add(string);

                                }
                            } catch (Exception e) {
                                Log.e("exception", "getContentProvider: " + e.toString());
                            }
                        }
                    }
                }
            }
        }
        for (String strings : set) {

            try {
                Log.e("setsize", "getContentProvider: " + strings);
                Bitmap bitmap = convertToBitmap(strings, 300, 300);
                MyBitmap myBitmap = new MyBitmap(strings, bitmap);
                lists.add(myBitmap);
            } catch (Exception e) {
                Log.e("exceptionee", "getSystemTime: " + e.toString());
            }
        }
        Log.e("lists", "listsAtgetContentProvider(): " + lists.toString());

        return lists;
    }


    private static Bitmap convertToBitmap(String filePath, int destWidth, int destHeight) {
        //第一次采样
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        int sampleSize = 1;
        while ((outWidth / sampleSize > destWidth) || (outHeight / sampleSize > destHeight)) {
            sampleSize *= 2;
        }
        //第二次采样
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static byte[] readStream(InputStream in) throws Exception {
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        while ((len = in.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        in.close();
        return data;
    }

    public static Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
        if (bytes != null)
            if (opts != null)
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
            else
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return null;
    }

    public static void getCameraPermission(RxPermissions rxPermissions, Context context, Runnable onGranted) {

        rxPermissions.requestEach(Manifest.permission.CAMERA).subscribe(new Action1<Permission>() {
            @Override
            public void call(Permission permission) {
                if (permission.granted) {
                    if (onGranted != null) {
                        onGranted.run();
                    }
                } else if (permission.shouldShowRequestPermissionRationale) {
                    Toast.makeText(context, "您拒绝了打开相机的权限，无法完成", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "您拒绝了打开相机的权限，无法完成", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void removeItem(int position, List<MyBitmap> finalList, Context context, GridView gridView, String name) {

        if (finalList.get(position).getPath().length() < 5 && "handle".equals(name)) {
            finalList.remove(position);
            Myadapter myadapter = new Myadapter(context, finalList);
            gridView.setAdapter(myadapter);
            FinalImgList.setFinalImgList(finalList);
        }else if (finalList.get(position).getPath().length() > 5 && "handle".equals(name)) {
            finalList.remove(position);
            Myadapter myadapter = new Myadapter(context, finalList);
            gridView.setAdapter(myadapter);
            FinalImgList.setFinalImgList(finalList);
            FinalImgList.getFinalSrcImgList().remove(position);
        }

        if ("select".equals(name)) {
            finalList.remove(position);

            for (int i = 0; i < finalList.size(); i++) {
                finalList.get(i).setBm(IOHelper.loadBitmap(finalList.get(i).getPath(),true));
            }

            Myadapter myadapter = new Myadapter(context, finalList);
            //FinalImgList.getFinalSrcImgList().remove(position);
            gridView.setAdapter(myadapter);

        }


        //FinalImgList.setFinalSrcImgList(finalList);
        if (finalList.size() != 0) {
            Log.e("测试removeItem", "finalList: " + finalList.get(0).getPath());
        }

    }

    public static void exchangePic(int position, int CODE, Activity activity) {

        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setType("image/*");
        activity.startActivityForResult(intent, CODE);

    }
    public static void resetAll(){
        List<MyBitmap> fin = new ArrayList<MyBitmap>();
        SelectFragment.setIsLode(false);
        Global.isReset = true;
        SelectFragment.setFinalList(fin);
        HandleFragment.setFinalList(fin);
        FinalImgList.setFinalImgList(fin);
    }

    public static void addPic(int position, Context context, String s) {
        int reqCode = 0402;
        ImageSelector.builder()
                .setMaxSelectCount(30)
                .setSingle(false)
                .useCamera(true)
                .start((Activity) context, reqCode);
        FinalImgList.setPosition(position);
        FinalImgList.setString(s);

    }
    public static float[] maxAndMin(float[] tensorFloatArray){
        float max = tensorFloatArray[0];
        float min = tensorFloatArray[0];

        for (int i = 0; i < tensorFloatArray.length; i++) {
            if (max < tensorFloatArray[i]){
                max = tensorFloatArray[i];
            }
            if (min > tensorFloatArray[i]){
                min = tensorFloatArray[i];
            }
        }
        return new float[]{Math.round(min),Math.round(max)};
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String assetFilePath(Context context, String assetName) throws IOException{
        File file = new File(context.getFilesDir(),assetName);
        if (file.exists() && file.length() > 0){
            return file.getAbsolutePath();
        }

        try(InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)){
                byte[] buffer = new byte[ 4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1){
                    os.write(buffer,0,read);
                }
                os.flush();
            }

            return file.getAbsolutePath();
        }


    }
    public static Bitmap imageScale(Bitmap bitmap, int dst_w, int dst_h) {
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = ((float) dst_w) / src_w;
        float scale_h = ((float) dst_h) / src_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,
                true);
        return dstbmp;
    }

    public static Mat getNormPred2Mat(float[] tensorFloatArray, float max, float min){
        int row = 0,col = 0;//行和列
        Mat dst = new Mat(320,320, CvType.CV_32S);
        for (int i = 0; i < tensorFloatArray.length; i++) {
            float value = tensorFloatArray[i];
            int normValue = Math.round((value - min)/(max - min) *255);
            if (i%320 != 0 || i == 0){
                dst.put(row,col++,new int[]{normValue});
            }else{
                col = 0;
                row ++;
                dst.put(row,col++,new int[]{normValue});
            }
        }
        return dst;
    }


    public static void getAllPic(String ALL_PIC_PATH, List<AllPicBitmap> allPicList) {
        File imagesPath = new File(ALL_PIC_PATH);
        System.out.println("imagesPath = " + imagesPath);
        File file = new File(imagesPath.toString());
        System.out.println("file = " + file);
        File[] imgFiles = file.listFiles();

        if (imgFiles != null) {
            System.out.println("这个目录，他的的名字是：" + file.getName());
            if (imgFiles.length > 0) {
                for (File f : imgFiles) {
                    System.out.println("他不是空目录，他目录中的文件名字是" + f.getName());

                }
            }
        } else {
            System.out.println("这个file是个文件，他的文件名是;" + file.getName());
        }


        if (imgFiles.length != 0) {
//            for (File file1 : imgFiles
//            ) {
//                if (file1.getName().endsWith(".jpg") || file1.getName().endsWith(".png") || file1.getName().endsWith("jpeg")) {
//                    AllPicBitmap allPicBitmap = new AllPicBitmap();
//                    allPicBitmap.setPath(file1.getPath());
//                    allPicBitmap.setBitmap(BitmapFactory.decodeFile(file1.getPath()));
//
//                    allPicList.add(allPicBitmap);
//
//                }
//
//            }
            for (int i = 0; i < imgFiles.length; i++) {
                if (imgFiles[i].getName().endsWith(".jpg") || imgFiles[i].getName().endsWith(".png") || imgFiles[i].getName().endsWith(".jpeg")) {
                    AllPicBitmap allPicBitmap = new AllPicBitmap();
                    allPicBitmap.setPath(imgFiles[i].getPath());
                    allPicBitmap.setBitmap(BitmapFactory.decodeFile(imgFiles[i].getPath()));
                    allPicList.add(allPicBitmap);
                }
                if (i > 100) {
                    break;
                }
            }
        }


    }

    public static void openFile(Context context, File url) throws ActivityNotFoundException,
            IOException {
        // Create URI
        //Uri uri = Uri.fromFile(url);

        if (url.exists()) {
            //TODO you want to use this method then create file provider in androidmanifest.xml with fileprovider name

            Uri uri = FileProvider.getUriForFile(context,  "com.example.conference.fileprovider", url);

            String urlString = url.toString().toLowerCase();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            /**
             * Security
             */
            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            // Check what kind of file you are trying to open, by comparing the url with extensions.
            // When the if condition is matched, plugin sets the correct intent (mime) type,
            // so Android knew what application to use to open the file
            if (urlString.toLowerCase().toLowerCase().contains(".doc")
                    || urlString.toLowerCase().contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword");
            } else if (urlString.toLowerCase().contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf");
            } else if (urlString.toLowerCase().contains(".ppt")
                    || urlString.toLowerCase().contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else if (urlString.toLowerCase().contains(".xls")
                    || urlString.toLowerCase().contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (urlString.toLowerCase().contains(".zip")
                    || urlString.toLowerCase().contains(".rar")) {
                // ZIP file
                intent.setDataAndType(uri, "application/trap");
            } else if (urlString.toLowerCase().contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf");
            } else if (urlString.toLowerCase().contains(".wav")
                    || urlString.toLowerCase().contains(".mp3")) {
                // WAV/MP3 audio file
                intent.setDataAndType(uri, "audio/*");
            } else if (urlString.toLowerCase().contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif");
            } else if (urlString.toLowerCase().contains(".jpg")
                    || urlString.toLowerCase().contains(".jpeg")
                    || urlString.toLowerCase().contains(".png")) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg");
            } else if (urlString.toLowerCase().contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain");
            } else if (urlString.toLowerCase().contains(".3gp")
                    || urlString.toLowerCase().contains(".mpg")
                    || urlString.toLowerCase().contains(".mpeg")
                    || urlString.toLowerCase().contains(".mpe")
                    || urlString.toLowerCase().contains(".mp4")
                    || urlString.toLowerCase().contains(".avi")) {
                // Video files
                intent.setDataAndType(uri, "video/*");
            } else {
                // if you want you can also define the intent type for any other file

                // additionally use else clause below, to manage other unknown extensions
                // in this case, Android will show all applications installed on the device
                // so you can choose which application to use
                intent.setDataAndType(uri, "*/*");
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "File doesn't exists", Toast.LENGTH_SHORT).show();
        }
    }

    public static void showPictures(List<MyBitmap> finalList,Context context,GridView gridView){
        if (finalList.size() != 0 ){
        //yasuo(finalList);
        }

        Myadapter myadapter = new Myadapter(context,finalList);
        gridView.setAdapter(myadapter);


    }

    public static void yasuo(List<MyBitmap> finalList){
        for (int i = 0; i < finalList.size(); i++) {
            if ( finalList.get(i).getPath().length() > 5){


            finalList.get(i).setBm(suofang(BitmapFactory.decodeFile(finalList.get(i).getPath())));
            Log.i("yasuo", "压缩前图片的大小" + (finalList.get(i).getBm().getByteCount() / 1024 / 1024)
                    + "M，宽度为" + finalList.get(i).getBm().getWidth() + "，高度为" + finalList.get(i).getBm().getHeight());
            }
        }

    }

    private static Bitmap suofang(Bitmap select){
        Bitmap tmp = null;
        Matrix matrix = new Matrix();
        matrix.setScale(0.2f, 0.2f);
        tmp = Bitmap.createBitmap(select, 0, 0, select.getWidth(),
                select.getHeight(), matrix, true);


        Log.i("suofang", "压缩前图片的大小" + (select.getByteCount() / 1024 / 1024)
                + "M，宽度为" + select.getWidth() + "，高度为" + select.getHeight());

        Log.i("suofang", "压缩后图片的大小" + (tmp.getByteCount() / 1024 / 1024)
                + "M,宽度为" + tmp.getWidth() + ",高度为" + tmp.getHeight());

        return tmp;
    }


    public static List<Point> getCrossPointType1(List<CrossPoint> crossPoints, int imageWidth, int imageHeight, int position, Mat src, Mat dst){
        List<Point> points = new ArrayList<Point>();

        double[] longLine1,longLine2;
        Point ltPoint = crossPoints.get(0).getCrossPoint();
        Point rtPoint = crossPoints.get(1).getCrossPoint();
        Point rbPoint = crossPoints.get(2).getCrossPoint();
        Point lbPoint = crossPoints.get(3).getCrossPoint();
        switch (position) {
            case 0://左上

                longLine1 = crossPoints.get(1).getLongLine1();

                if(Math.abs(longLine1[0]-longLine1[2]) < Math.abs(longLine1[1]-longLine1[3])) {
                    longLine1 = crossPoints.get(1).getLongLine2();
                }

                longLine2 = crossPoints.get(3).getLongLine1();

                if(Math.abs(longLine2[0]-longLine2[2]) > Math.abs(longLine2[1]-longLine2[3])) {
                    longLine2 = crossPoints.get(3).getLongLine2();
                }

                ltPoint = getCrossPoint1(longLine1, longLine2);
                if(ltPoint.x>imageWidth+100) {
                    ltPoint.x = imageWidth;
                }
                if(ltPoint.y>imageHeight+100) {
                    ltPoint.y = imageHeight;
                }


                points.add(ltPoint);
                points.add(rtPoint);
                points.add(rbPoint);
                points.add(lbPoint);

                break;
            case 1://右上

                longLine1 = crossPoints.get(0).getLongLine2();

                if(Math.abs(longLine1[0]-longLine1[2]) < Math.abs(longLine1[1]-longLine1[3])) {
                    longLine1 = crossPoints.get(0).getLongLine1();
                }

                longLine2 = crossPoints.get(2).getLongLine1();


                if(Math.abs(longLine2[0]-longLine2[2]) > Math.abs(longLine2[1]-longLine2[3])) {
                    longLine2 = crossPoints.get(2).getLongLine2();
                }

                rtPoint = getCrossPoint1(longLine1, longLine2);

                if(rtPoint.x>imageWidth+100) {
                    rtPoint.x = imageWidth;
                }
                if(rtPoint.y>imageHeight+100) {
                    rtPoint.y = imageHeight;
                }

                System.out.println("rtPoint: " + rtPoint);
                points.add(ltPoint);
                points.add(rtPoint);
                points.add(rbPoint);
                points.add(lbPoint);


                break;
            case 2://右下
                longLine1 = crossPoints.get(1).getLongLine2();

                if(Math.abs(longLine1[0]-longLine1[2]) > Math.abs(longLine1[1]-longLine1[3])) {
                    longLine1 = crossPoints.get(1).getLongLine1();
                }

                longLine2 = crossPoints.get(3).getLongLine2();

                if(Math.abs(longLine2[0]-longLine2[2]) < Math.abs(longLine2[1]-longLine2[3])) {
                    longLine2 = crossPoints.get(3).getLongLine1();
                }

                //System.out.println( "——longLine1-起点坐标：(" +longLine1[0] + "," + longLine1[1] + ") 终点坐标：("+longLine1[2] + "," + longLine1[3] + ")");
                //	System.out.println("——longLine2-起点坐标：(" +longLine2[0] + "," + longLine2[1] + ") 终点坐标：("+longLine2[2] + "," + longLine2[3] + ")");
//

                rbPoint = getCrossPoint1(longLine1, longLine2);

                if(rbPoint.x>imageWidth+100) {
                    rbPoint.x = imageWidth;
                }
                if(rbPoint.y>imageHeight+100) {
                    rbPoint.y = imageHeight;
                }
                //System.out.println( "rbpoint: " + rbPoint);

                points.add(ltPoint);
                points.add(rtPoint);
                points.add(rbPoint);
                points.add(lbPoint);

                break;
            case 3://左下


                longLine1 = crossPoints.get(0).getLongLine2();

                if(Math.abs(longLine1[0]-longLine1[2]) > Math.abs(longLine1[1]-longLine1[3])) {
                    longLine1 = crossPoints.get(0).getLongLine1();
                }

                longLine2 = crossPoints.get(2).getLongLine2();

                if(Math.abs(longLine2[0]-longLine2[2]) < Math.abs(longLine2[1]-longLine2[3])) {
                    longLine2 = crossPoints.get(2).getLongLine1();
                }

                lbPoint = getCrossPoint1(longLine1, longLine2);

                if(lbPoint.x>imageWidth +100) {
                    lbPoint.x = imageWidth;
                }
                if(lbPoint.y>imageHeight +100 ) {
                    lbPoint.y = imageHeight;
                }

                points.add(ltPoint);
                points.add(rtPoint);
                points.add(rbPoint);
                points.add(lbPoint);
                break;

            default:
                break;
        }




        return points;
    }

    private static Point getCrossPoint1(double[] lsegA, double[] lsegB) {
        double x;
        double y;
        double x1 = lsegA[0];
        double y1 = lsegA[1];
        double x2 = lsegA[2];
        double y2 = lsegA[3];
        double x3 = lsegB[0];
        double y3 = lsegB[1];
        double x4 = lsegB[2];
        double y4 = lsegB[3];
        double k1 = Double.MAX_VALUE;
        double k2 = Double.MAX_VALUE;
        boolean flag1 = false;
        boolean flag2 = false;

        if ((x1 - x2) == 0)
            flag1 = true;
        if ((x3 - x4) == 0)
            flag2 = true;

        if (!flag1)
            k1 = (y1 - y2) / (x1 - x2);
        if (!flag2)
            k2 = (y3 - y4) / (x3 - x4);

        if (k1 == k2) {

            return null;}

        if (flag1) {
            if (flag2) {

                return null;
            }

            x = x1;
            if (k2 == 0) {
                y = y3;
            } else {
                y = k2 * (x - x4) + y4;
            }
        } else if (flag2) {
            x = x3;
            if (k1 == 0) {
                y = y1;
            } else {
                y = k1 * (x - x2) + y2;
            }
        } else {
            if (k1 == 0) {
                y = y1;
                x = (y - y4) / k2 + x4;
            } else if (k2 == 0) {
                y = y3;
                x = (y - y2) / k1 + x2;
            } else {
                x = (k1 * x2 - k2 * x4 + y4 - y2) / (k1 - k2);
                y = k1 * (x - x2) + y2;
            }
        }
        return new Point(x, y);
    }


    private static Point getAnotherPoint(Point ltPoint, Point rtPoint, Point lbPoint, Point point) {
        double x = rtPoint.x - ltPoint.x;
        double y = lbPoint.y - ltPoint.y;

        double lackX = x - point.x;
        double lackY = y - point.y;

        return new Point(point.x + lackX,point.y - lackY);
    }



    private static Point getAntherPoint(Point A, Point B, Point C, double x2) {
        double k1 = (A.y-B.y)/(A.x-B.x);
        double k2 = (B.y-C.y)/(B.x-C.x);
        double b1 = C.y-k1*C.x;
        double b2 = A.y-k2*A.x;
        double x = (b2-b1)/(k1-k2);
        double y = k1*x+b1;



        return new Point(x2,y);
    }

    private static double getPianyi(double sita, Point Min, Point Max, Point tempY, int imageWidth,int imageHeight) {
        double duibianX = Math.abs( Max.y - Min.y);

        int type = 0;
        if (sita > 90) {
            sita = 180 - sita;
            type = 1;
        }else {
            type = -1;
        }
        double tanX = Math.tan(Math.toRadians(sita));
        double linbianX = duibianX/tanX;

        Max.x -= (linbianX*type);

        if (Max.x > imageWidth) {
            Max.x = imageWidth;
        }
        if (Max.x < 0 ) {
            Max.x = 0;
        }

        double duibianY = Math.abs(tempY.x - Max.x);
        double linbianY = duibianY/tanX;
        Max.y -=(linbianY*type);
        if (Max.y > imageHeight) {
            Max.y = imageHeight;
        }
        if (Max.y < 0) {
            Max.y = 0;
        }



        return Max.x;
    }

    public static List<Point> getCrossPointType3(List<CrossPoint> crossPoints, int[] vertex, int imageWidth, int imageHeight) {
        // TODO Auto-generated method stub
        List<Point> points = new ArrayList<Point>();
        int state = 4;
        int count = 0;
        for (int i = 0; i < vertex.length; i++) {
            if (vertex[i] == i) {
                state = i;
            }
        }
        double[] longLine1;
        double[] longLine2;
        double[] fullLine1;
        double[] fullLine2;
        Point LT = new Point();
        Point RT = new Point();
        Point LB = new Point();
        Point RB = new Point();
        switch (state) {
            case 0://左上
                LT = new Point(crossPoints.get(0).longLine1[0], crossPoints.get(0).longLine1[1]);
                RT = new Point(crossPoints.get(1).longLine1[2], crossPoints.get(1).longLine1[3]);
                LB = new Point(crossPoints.get(3).longLine1[2], crossPoints.get(3).longLine1[3]);
                RB = getPoint(LT, RT, LB);

                points.add(LT);
                points.add(RT);
                points.add(RB);
                points.add(LB);
                break;
            case 1://右上

                LT = new Point(crossPoints.get(0).longLine2[0], crossPoints.get(0).longLine2[1]);
                RT = new Point(crossPoints.get(1).longLine1[0], crossPoints.get(1).longLine1[1]);
                RB = new Point(crossPoints.get(2).longLine1[2], crossPoints.get(3).longLine1[3]);
                LB = getPoint(RT, LT, RB);

                points.add(LT);
                points.add(RT);
                points.add(RB);
                points.add(LB);
                break;
            case 2://右下


                RT = new Point(crossPoints.get(1).longLine1[0], crossPoints.get(1).longLine1[1]);
                RB = new Point(crossPoints.get(2).longLine1[2], crossPoints.get(3).longLine1[3]);
                LB = new Point(crossPoints.get(3).longLine2[0], crossPoints.get(0).longLine2[1]);
                LT = getPoint(RB, LB, RT);

                points.add(LT);
                points.add(RT);
                points.add(RB);
                points.add(LB);
                break;
            case 3://左下
                LT = new Point(crossPoints.get(0).longLine1[0], crossPoints.get(0).longLine1[1]);
                LB = crossPoints.get(3).getCrossPoint();
                RB = new Point(crossPoints.get(2).longLine1[2], crossPoints.get(2).longLine1[3]);
                RT = getPoint(LB, RB, LT);



                points.add(LT);
                points.add(RT);
                points.add(RB);
                points.add(LB);
                break;

            default:
                break;
        }
        return points;
    }

    private static Point getPoint(Point LT, Point RT, Point LB) {
        // TODO Auto-generated method stub
        double x= 0,y = 0,X1 = 0,X2 = 0;
        double k = (RT.y-LT.y)/(RT.x-LT.x);
        double b1 = LB.y-k*LB.x;
        double distance = Math.pow(RT.y-LT.y, 2)+ Math.pow(RT.x-LT.x, 2);
        double a = k*k+1;
        double b = -2*LB.x*a;
        double c = a*LB.x*LB.x-distance;
        if (b*b-4*a*c < 0) {
            System.out.println("无解");
        }else {
            X1=(-b + Math.sqrt(b * b-4 * a * c)) * 1/(2 * a);
            X2=(-b - Math.sqrt(b * b-4 * a * c)) * 1/(2 * a);
            if (X1 < 0 && X2 >= 0) {
                x = X2;
            }
            if (X1 >= 0 && X2 < 0) {
                x = X1;
            }
        }
        y = k * x + b1;







        return new Point(x,y);
    }
    public static List<Point> getCrossPointType2(List<CrossPoint> crossPoints, int imageWidth,int imageHeight){
        List<Point> points = new ArrayList<Point>();
        int state = 4 ;
        int count = 0;
        if (!crossPoints.get(0).isFlage()) {
            if (!crossPoints.get(1).isFlage()) {
                state = 0;
            }
            if (crossPoints.get(2).isFlage()&&crossPoints.get(3).isFlage()) {
                state = 0;
            }
            if (!crossPoints.get(3).isFlage()) {
                state = 2;
            }
            if (crossPoints.get(1).isFlage()&&crossPoints.get(2).isFlage()) {
                state = 2;
            }
        }

        if (!crossPoints.get(2).isFlage()) {
            if (!crossPoints.get(3).isFlage()) {
                state = 1;
            }
            if (crossPoints.get(0).isFlage()&&crossPoints.get(1).isFlage()) {
                state = 1;
            }
        }

        if (!crossPoints.get(1).isFlage()) {
            if (!crossPoints.get(2).isFlage()) {
                state = 3;
            }
            if (crossPoints.get(0).isFlage()&&crossPoints.get(3).isFlage()) {
                state = 3;
            }
        }
        if (!crossPoints.get(3).isFlage()) {
            if (!crossPoints.get(2).isFlage()) {
                state = 1;
            }
            if (crossPoints.get(0).isFlage()&&crossPoints.get(1).isFlage()) {

            }
        }
        for (int i = 0; i < crossPoints.size(); i++) {
            if (!crossPoints.get(i).isFlage()) {
                count++;
            }
        }
        if (count==4) {
            state = 4;
        }
        //System.out.println(state);
        double[] fullLine1,fullLine2;
        switch (state) {
            case 0://上
                if (Global.line.length == 0) {
                    //System.out.println("global = null");
                    fullLine1 = new double[] {0,0,imageWidth-1,0};
                }else {
                    //System.out.println("global not null");
                    double[] line = Global.line;
                    fullLine1 = new double[] {0,line[1],line[2],line[1]};
                }
                points.add(getCrossPoint(crossPoints.get(0).getFullLine1(), fullLine1));
                points.add(getCrossPoint(crossPoints.get(1).getFullLine1(), fullLine1));
                points.add(crossPoints.get(2).getCrossPoint());
                points.add(crossPoints.get(3).getCrossPoint());
                break;
            case 1://下

                if (Global.line.length != 0) {

                    fullLine1 = crossPoints.get(3).getLongLine1();
                    fullLine2 = crossPoints.get(2).getLongLine1();
                    Point LB = new Point(fullLine1[2],fullLine1[3]);
                    Point RB = new Point(fullLine2[2],fullLine2[3]);

                    if (Math.abs(LB.y - imageHeight) >= 20 || Math.abs(RB.y - imageHeight) >= 20){

                        points.add(crossPoints.get(0).getCrossPoint());
                        points.add(crossPoints.get(1).getCrossPoint());
                        points.add(RB);
                        points.add(LB);
                        break;
                    }else{
                        fullLine1 = new double[] {0,imageHeight,imageWidth-1,imageHeight+1};
                        points.add(crossPoints.get(0).getCrossPoint());
                        points.add(crossPoints.get(1).getCrossPoint());
                        points.add(getCrossPoint(crossPoints.get(2).getFullLine1(), fullLine1));
                        points.add(getCrossPoint(crossPoints.get(3).getFullLine1(), fullLine1));
                        break;
                    }
                    //
                }else {

                    double[] line = Global.line;

                    fullLine1 = new double[] {0,line[3],line[2],line[3]};
                    points.add(crossPoints.get(0).getCrossPoint());
                    points.add(crossPoints.get(1).getCrossPoint());
                    points.add(getCrossPoint(crossPoints.get(2).getFullLine1(), fullLine1));
                    points.add(getCrossPoint(crossPoints.get(3).getFullLine1(), fullLine1));
                }


                break;
            case 2://左

                if (Global.line.length == 0) {
                    //System.out.println("global = null");
                    fullLine1 = new double[] {0,0,1,imageHeight};
                }else {
                    //System.out.println("global not null");
                    double[] line = Global.line;
                    fullLine1 = new double[] {line[0],0,line[0],line[3]};
                }
                points.add(getCrossPoint(crossPoints.get(0).getFullLine1(), fullLine1));
                points.add(crossPoints.get(1).getCrossPoint());
                points.add(crossPoints.get(2).getCrossPoint());
                points.add(getCrossPoint(crossPoints.get(3).getFullLine1(), fullLine1));
                break;
            case 3://右
                if (Global.line.length == 0) {
                    fullLine1 = crossPoints.get(1).getLongLine1();
                    fullLine2 = crossPoints.get(2).getLongLine1();
                    Point RT = new Point(fullLine1[2],fullLine1[3]);
                    Point RB = new Point(fullLine2[2],fullLine2[3]);

                    Point LT = crossPoints.get(0).getCrossPoint();
                    Point LB = crossPoints.get(3).getCrossPoint();
                    double x2 = getPianyi(Utils.calAngle(LT, RT, LB),RT,RB,LB,imageWidth,imageHeight);

                    Point anotherPoint2 = new Point();
                    if(x2 == 0) {
                        anotherPoint2 = getPoint(LT,RT,LB);
                    }else {
                        anotherPoint2 = getAntherPoint(LB,LT,RT,x2);
                    }

                    RB = anotherPoint2;



                    if (Math.abs(RT.x - imageWidth) >= 20 && Math.abs(RB.x - imageWidth) >= 20){
                        points.add(crossPoints.get(0).getCrossPoint());



                        points.add(RT);
                        points.add(RB);


                        points.add(crossPoints.get(3).getCrossPoint());
                        break;
                    }else{
                        fullLine1 = new double[] {imageWidth-1,0,imageWidth-1,imageHeight+1};
                        points.add(crossPoints.get(0).getCrossPoint());
                        points.add(crossPoints.get(1).getCrossPoint());
                        points.add(getCrossPoint(crossPoints.get(2).getFullLine1(), fullLine1));
                        points.add(getCrossPoint(crossPoints.get(3).getFullLine1(), fullLine1));
                        break;
                    }

                }else {
                    //System.out.println("global not null");
                    double[] line = Global.line;
                    fullLine1 = new double[] {line[2],0,line[2],line[3]};
                }
                points.add(crossPoints.get(0).getCrossPoint());
                points.add(getCrossPoint(crossPoints.get(1).getFullLine1(), fullLine1));
                points.add(getCrossPoint(crossPoints.get(2).getFullLine1(), fullLine1));
                points.add(crossPoints.get(3).getCrossPoint());
                break;

            default:
                break;
        }
        return points;
    }



    public static boolean isLineExist(List<LineItem> lineItems) {

        for (int i = 0; i < lineItems.size(); i++) {
            if (!lineItems.get(i).isExist()) {
                return false;
            }
        }


        return true;
    }

    public static boolean isPointInRect(VertexSet vertexSet , Point point) {//点是否在四边形内
        Point A = vertexSet.getLB();
        Point B = vertexSet.getLT();
        Point C = vertexSet.getRT();
        Point D = vertexSet.getRB();
        double a = (B.x - A.x)*(point.y - A.y) - (B.y - A.y)*(point.x - A.x);
        double b = (C.x - B.x)*(point.y - B.y) - (C.y - B.y)*(point.x - B.x);
        double c = (D.x - C.x)*(point.y - C.y) - (D.y - C.y)*(point.x - C.x);
        double d = (A.x - D.x)*(point.y - D.y) - (A.y - D.y)*(point.x - D.x);
        if((a > 0 && b > 0 && c > 0 && d > 0) || (a < 0 && b < 0 && c < 0 && d < 0)) {
            return true;
        }

//     AB X AP = (b.x - a.x, b.y - a.y) x (p.x - a.x, p.y - a.y) = (b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x);
//     BC X BP = (c.x - b.x, c.y - b.y) x (p.x - b.x, p.y - b.y) = (c.x - b.x) * (p.y - b.y) - (c.y - b.y) * (p.x - b.x);
        return false;
    }

}
