package com.example.conference.ui.handlefragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.example.conference.MainActivity;
import com.example.conference.R;
import com.example.conference.entity.AreaLines;
import com.example.conference.entity.CrossPoint;
import com.example.conference.entity.LineGroup;
import com.example.conference.imageExtract.FinalImgList;
import com.example.conference.imageExtract.MyBitmap;
import com.example.conference.imageExtract.Myadapter;
import com.example.conference.utils.Global;
import com.example.conference.utils.IOHelper;
import com.example.conference.utils.ImageUtil;
import com.example.conference.utils.Utils;
import com.example.conference.utils.DemoireUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import rx.functions.Action1;

import static com.example.conference.constants.Constants.PI;
import static com.example.conference.utils.Utils.assetFilePath;
import static com.example.conference.utils.Utils.getCenterPoint;
import static com.example.conference.utils.Utils.getNormPred2Mat;
import static com.example.conference.utils.Utils.getOutputSize;
import static com.example.conference.utils.Utils.getSystemTime;
import static com.example.conference.utils.Utils.imageScale;
import static com.example.conference.utils.Utils.maxAndMin;
import static com.example.conference.utils.Utils.putLines;
import static com.example.conference.utils.Utils.readStream;
import static com.example.conference.utils.Utils.showPictures;
import static com.example.conference.utils.Utils.type2Label;

public class HandleFragment extends Fragment {

    private static List<MyBitmap> finalList = new ArrayList<MyBitmap>();
    private static List<MyBitmap> emptyList = new ArrayList<MyBitmap>();
    private static boolean isFinished = false;

    private List<MyBitmap> testList = new ArrayList<MyBitmap>();
    private static Context context;
    private static GridView gridView;
    private static int successNum = 0;
    private static Mat element;
    private static Bitmap emptybp;
    private int handledNum = 0;
    private FloatingActionButton handleBtn;
    private FloatingActionButton creatPdf;
    private int CODE = 0513;
    private String name = "handle";
    private int exchangePosition;
    private static String TAG = "peralk";
    private Myadapter myadapter;
    private Bitmap exchange_bitmap;
    private EditText editText;
    private boolean isHandled = FinalImgList.isIsHandled();
    private int saveNum = 0;
    private static View pb;

    private static String SAVE_PDF_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Conference/pdf/";//保存到SD卡
    private static String SAVE_TEMP_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Conference/";
    private static String SAVE_PIC_PATH;
    private int nowPosition = 0;
    private View nowView;
    private boolean isItemOnLongtimeSelect = false;
    private String code = null;
    private boolean isTopdfSuccess = false;
    private String PDF_PATH = null;
    private static boolean isRepeat = false;
    private int PERMISSION_CODE = 0513;
    private RxPermissions rxPermissions;
    private RelativeLayout relativeLayout;
    private static Module module = null;
    private static List<Size> sizeList = new ArrayList<Size>();
    private static List<Mat> matList = new ArrayList<Mat>();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_handle, container, false);
        Log.d(TAG, "handle_onCreateView: ");
        context = inflater.getContext();
        if (finalList.size() == 0) {
            finalList = FinalImgList.getFinalImgList();
        }
        Calendar now = new GregorianCalendar();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        SAVE_PIC_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Conference/" + simpleDate.format(now.getTime());
        //Log.d(TAG, "onCreateView: " + SAVE_PIC_PATH);
        gridView = root.findViewById(R.id.gvHandle);
        showPictures(finalList, context, gridView);
        element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        handleBtn = root.findViewById(R.id.confirm);
        creatPdf = root.findViewById(R.id.toPdf);
        relativeLayout = root.findViewById(R.id.fragment_handle);
        try {
            module = Module.load(assetFilePath(context,"u2net_p1.pt"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            DemoireUtils.loadModule(context);
        } catch (IOException e) {
            Log.e(TAG, "去摩尔纹模型加载失败", e);
        }


        rxPermissions = new RxPermissions((Activity) inflater.getContext());
        pb = root.findViewById(R.id.pb);
        if (FinalImgList.getList().size() != 0 && FinalImgList.getRequestCode() == 0402) {
            FinalImgList.setRequestCode(0);
            finalList = FinalImgList.getFinalImgList();
            showPictures(finalList, context, gridView);
        }
        if ("ai".equals(Global.method))
            matList = bitmapList2IValueList(finalList);
        handleBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v){
                handleBtn.setVisibility(View.GONE);
                return false;

            }
        });
        handleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                        gotoHandlePictures();




            }



    });
        creatPdf.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v){
                creatPdf.setVisibility(View.GONE);
                return false;
            }
        });


        creatPdf.setOnClickListener(new View.OnClickListener()

    {
        @Override
        public void onClick (View view){
        if (finalList.size() > 0) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setIcon(R.drawable.pdf_pic)
                    .setTitle("确认框")
                    .setMessage("确定将此列表生成PDF吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setIcon(R.drawable.pdf_picture)
                                    .setTitle("请选择保存选择类型")
                                    .setPositiveButton("保存当前图片并生成PDF", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            editText = new EditText(context);
                                            AlertDialog.Builder editAlert = new AlertDialog.Builder(context);
                                            editAlert.setView(editText)
                                                    .setTitle("请输入您要保存的PDF文件名：")
                                                    .setIcon(R.mipmap.pdf_selected)
                                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            if (editText.getText().toString().length() == 0 ||
                                                                    editText.getText().toString().length() > 20) {
                                                                AlertDialog.Builder isNull = new AlertDialog.Builder(context);
                                                                isNull.setTitle("输入不能为空且输入字数不能大于20,请从新点击生成按钮")
                                                                        .setIcon(R.mipmap.pdf_selected)
                                                                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                                            }
                                                                        })
                                                                        .create()
                                                                        .show();
                                                            } else {
                                                                FinalImgList.setPdfNameInput(editText.getText().toString());
                                                                code = "withSavePic";
                                                                pb.setVisibility(View.VISIBLE);
                                                                Toast.makeText(context, "PDF生成中······", Toast.LENGTH_SHORT).show();
                                                                getPermission(rxPermissions, context);
                                                                //new toPdfTask().execute();
//                                                                        try {
//                                                                            saveBitmapPic("withSavePic");
//                                                                        } catch (IOException e) {
//                                                                            e.printStackTrace();
//                                                                        }

//                                                                        MainActivity mainActivity = (MainActivity) getActivity();
//                                                                        BottomNavigationView bottomNavigationView = mainActivity.getNavView();
//                                                                        bottomNavigationView.setSelectedItemId(R.id.navigation_pdfs);
                                                            }
                                                            // System.out.println(" editText: " + editText.getText().toString());

                                                        }
                                                    })
                                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                        }
                                                    })
                                                    .create()
                                                    .show();
                                            //isLegal();

                                        }
                                    })
                                    .setNegativeButton("仅生成PDF", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            editText = new EditText(context);
                                            AlertDialog.Builder editAlert = new AlertDialog.Builder(context);
                                            editAlert.setView(editText)
                                                    .setTitle("请输入您要保存的文件名：")
                                                    .setIcon(R.mipmap.pdf_selected)
                                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            if (editText.getText().toString().length() == 0 ||
                                                                    editText.getText().toString().length() > 20) {
                                                                AlertDialog.Builder isNull = new AlertDialog.Builder(context);
                                                                isNull.setTitle("输入不能为空且输入字数不能大于20,请从新点击生成按钮")
                                                                        .setIcon(R.mipmap.pdf_selected)
                                                                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                                            }
                                                                        })
                                                                        .create()
                                                                        .show();
                                                            } else {
                                                                FinalImgList.setPdfNameInput(editText.getText().toString());
                                                                code = "withoutSavePic";
                                                                //pb.setVisibility(View.VISIBLE);
                                                                Toast.makeText(context, "PDF生成中······", Toast.LENGTH_SHORT).show();
                                                                getPermission(rxPermissions, context);

                                                                //new toPdfTask().execute();

//                                                                        try {
//                                                                            saveBitmapPic("withoutSavePic");
//                                                                        } catch (IOException e) {
//                                                                            e.printStackTrace();
//                                                                        }
//                                                                        MainActivity mainActivity = (MainActivity) getActivity();
//                                                                       BottomNavigationView bottomNavigationView = mainActivity.getNavView();
//                                                                       bottomNavigationView.setSelectedItemId(R.id.navigation_pdfs);
                                                            }
                                                        }
                                                    })
                                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                        }
                                                    })
                                                    .create()
                                                    .show();

                                        }
                                    })
                                    .create()
                                    .show();


                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create()
                    .show();

        } else {
            Toast.makeText(context, "请先添加图片······", Toast.LENGTH_SHORT).show();
        }
    }
    });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()

    {
        @Override
        public boolean onItemLongClick (AdapterView < ? > adapterView, View view,final int position,
        long l){
        if (adapterView != null) {
            nowPosition = position;
            nowView = view;
            // Toast.makeText(context, "你长按的是第" + position + "个item", Toast.LENGTH_SHORT).show();
            Button delButton = view.findViewById(R.id.deleta_pic);
            delButton.setVisibility(View.VISIBLE);
            delButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //  Toast.makeText(context, "点到了del", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setIcon(R.drawable.red_deleat)
                            .setTitle("确认框")
                            .setMessage("确定删除此照片吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Utils.removeItem(position, finalList, context, gridView, name);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .create()
                            .show();

                }
            });
            Button add_pic = view.findViewById(R.id.add_pic);
            add_pic.setVisibility(View.VISIBLE);
            add_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setIcon(R.drawable.add_red)
                            .setTitle("确认框")
                            .setMessage("确定在此照片之前插入照片吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Utils.addPic(position, context, "handle");
                                    isHandled = false;
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .create()
                            .show();
                }
            });
            Button exchange_pic = view.findViewById(R.id.exchange_pic);
            exchange_pic.setVisibility(View.VISIBLE);
            exchange_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setIcon(R.drawable.exchange)
                            .setTitle("确认框")
                            .setMessage("确定替换本照片吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    exchangePosition = position;
                                    exchangePic(position);

                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .create()
                            .show();
                }
            });

        }
        isItemOnLongtimeSelect = true;

        return true;
    }
    });
        relativeLayout.setOnClickListener(new View.OnClickListener()

    {
        @Override
        public void onClick (View v){
        if (nowView != null) {
            nowView.findViewById(R.id.deleta_pic).setVisibility(View.GONE);
            nowView.findViewById(R.id.add_pic).setVisibility(View.GONE);
            nowView.findViewById(R.id.exchange_pic).setVisibility(View.GONE);
        }
    }
    });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()

    {
        @Override
        public void onItemClick (AdapterView < ? > adapterView, View view,int i, long l){
        if (nowView != view && isItemOnLongtimeSelect == true) {
            nowView.findViewById(R.id.deleta_pic).setVisibility(View.GONE);
            nowView.findViewById(R.id.add_pic).setVisibility(View.GONE);
            nowView.findViewById(R.id.exchange_pic).setVisibility(View.GONE);
        }
    }
    });


        return root;
}




    public static void setFinalList(List<MyBitmap> finalList) {
        HandleFragment.finalList = finalList;
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            Log.d(TAG, "requestCode: " + requestCode + "permission ： " + permissions + "grantResults: " + grantResults);

        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ContentResolver contentResolver = context.getContentResolver();

        if (requestCode == CODE) {
            if (data != null) {
                Log.e(TAG, "CODE requestCode:" + requestCode);
                try {
                    Uri orginalUri = data.getData();
                    //将图片内容解析成字节数组
                    byte[] mContent = Utils.readStream(contentResolver.openInputStream(Uri.parse(orginalUri.toString())));
                    //将字节数组转换为ImageView可调用的Bitmap对象
                    String getFromPic = data.getData().getPath();
                    String finalSelectPic = getFromPic.substring(getFromPic.length() - 54, getFromPic.length());
                    Log.e(TAG, "getFromPic: " + getFromPic.substring(getFromPic.length() - 54, getFromPic.length()));
                    exchange_bitmap = Utils.getPicFromBytes(mContent, null);
                    Log.e(TAG, "exchange_picUrl: " + data.getData().getPath());
                    MyBitmap exchangeBitmap = new MyBitmap(finalSelectPic, exchange_bitmap);
                    finalList.add(exchangePosition, exchangeBitmap);
                    finalList.remove(exchangePosition + 1);
                    System.out.println("exchangePosition" + exchangePosition);
                    isHandled = false;
                    for (int i = 0; i < finalList.size(); i++) {
                        if (finalList.get(i).getPath().length() < 5) {
                            continue;
                        } else {
                            finalList.get(i).setBm(IOHelper.loadBitmap(finalList.get(i).getPath(), true));
                        }

                    }


                    showPictures(finalList, context, gridView);
                    FinalImgList.setFinalImgList(finalList);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void exchangePic(int position) {

        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setType("image/*");
        startActivityForResult(intent, CODE);

    }

    public synchronized static <T> boolean Thread(List<MyBitmap> list, int nThread) {
        long startTime = System.currentTimeMillis();
        Log.d("ThreadTime", "Thread: startTime = " + startTime);
        if (list.size() == 0 || nThread <= 0) {
            return false;
        }
        int handledNum = 0;
        Semaphore semaphore = new Semaphore(nThread);//定义几个许可
        ExecutorService executorService = Executors.newFixedThreadPool(nThread);//创建一个固定的线程池
        for (int i = 0; i < list.size(); i++) {
            pb.setVisibility(View.VISIBLE);
            if (list.get(i).getPath().length() < 5) {
                handledNum++;
            }
            if (handledNum == (finalList.size() )) {
                isRepeat = true;
            }
            try {
                semaphore.acquire();
                int finalI = i;
                executorService.execute(() -> {
                    //此处可以放入待处理的业务
                    if (list.get(finalI).getPath().length() > 5) {
                        try {
//                            extract(list.get(finalI).getPath(), finalI);//处理图片
                            if ("ai".equals(Global.method)){//深度学习
                                if (matList.size() == 0){//先进入线程池处理模型
                                    matList = bitmapList2IValueList(finalList);

                                }
                                AIExtract(matList.get(finalI),finalI,list.get(finalI).getPath());
                            }
                        } catch (Exception e) {
                            System.out.println("第" + finalI + "个处理失败！");
                            e.printStackTrace();
                            LineGroup.crossPoints.clear();
                            LineGroup.lineItems.clear();
                        }
                    }
                    semaphore.release();
                });
            } catch (InterruptedException e) {

                // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);


            }

        }
        //showPictures(finalList, context, gridView);
        executorService.shutdown();
        while (true) {
            if (executorService.isTerminated()) {
                long endTime = System.currentTimeMillis();
               // showPictures(finalList, context, gridView);
                Log.d("ThreadTime", "Thread: endTime = " + endTime);
                Log.d("ThreadTime", "Thread: 一共执行了 " + (endTime - startTime) + "毫秒");
//                Toast.makeText(context, "执行了", Toast.LENGTH_SHORT).show();
//                pb.setVisibility(View.GONE);
//                Log.d("thread", "Thread: " + executorService.isShutdown());
//                FinalImgList.setFinalSrcImgList(emptyList);
//                FinalImgList.setFinalImgList(finalList);
                //Myadapter myadapter = new Myadapter(context,finalList);
               // gridView.setAdapter(myadapter);
                return true;

           /* MainActivity mainActivity = (MainActivity) getActivity();
            BottomNavigationView bottomNavigationView = mainActivity.getNavView();
            bottomNavigationView.setSelectedItemId(R.id.navigation_handle);*/

            }
        }

    }

    private void gotoHandlePictures() {

        if (finalList.size() >= 1) {
            pb.setVisibility(View.VISIBLE);
            Toast.makeText(context, "正在处理请稍后······", Toast.LENGTH_SHORT).show();
            new extractTask().execute();

        } else {
            Toast.makeText(context, "请先添加照片······", Toast.LENGTH_SHORT).show();
        }


    }

    public void saveBitmapPic(String s) throws IOException {
        //获取内部存储状态
        if (finalList.size() > 0) {


            String state = Environment.getExternalStorageState();
            //如果状态不是mounted，无法读写
            if (!state.equals(Environment.MEDIA_MOUNTED)) {
                return;
            }

            for (int i = 0; i < finalList.size(); i++) {
                Calendar now = new GregorianCalendar();
                SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                String fileName = simpleDate.format(now.getTime()) + "_" + (i + 1);
                //System.out.println("fileName" + (i + 1) + ": " + fileName);
                saveFile(finalList.get(i).getBm(), fileName, i, s);
            }

        } else {
            Toast.makeText(context, "当前列表没有图片！", Toast.LENGTH_SHORT).show();
        }
    }

    public void creatFile() {
        //获取外部存储路径
//        Calendar now = new GregorianCalendar();
//        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        //SAVE_PIC_PATH = SAVE_PIC_PATH + simpleDate.format(now.getTime()).toString();

        File tempFile = new File(SAVE_TEMP_PATH);
        Log.d("tempFile", tempFile.toString());
        if (!tempFile.exists()) {
            boolean tempMkdirs = tempFile.mkdirs();
            if (!tempMkdirs) {
                Log.d("tempMkdirs", tempFile.toString() + " 文件夹创建失败 ");
            } else {
                Log.d("tempMkdirs", tempFile.toString() + " 文件夹创建成功 ");
            }
        }


        File dirFile = new File(SAVE_PIC_PATH);
        Log.d("dirFile", "" + dirFile);
        if (!dirFile.exists()) {
            boolean mkdirs = dirFile.mkdirs();
            if (!mkdirs) {
                Log.e("TAG", "pic_文件夹创建失败");
            } else {
                Log.e("TAG", "pic_文件夹创建成功");
            }
        }

        File pdfFile = new File(SAVE_PDF_PATH);
        Log.d("pdfFile", "" + pdfFile.toString());
        if (!pdfFile.exists()) {
            boolean pdfMkdirs = pdfFile.mkdirs();
            if (!pdfMkdirs) {
                Log.e("TAG", "pdf_文件夹创建失败");
            } else {
                Log.e("TAG", "pdf_文件夹创建成功");
            }
        }

    }

    public void saveFile(Bitmap bm, String fileName, int position, String s) throws IOException {
        creatFile();
        File imagesPath = new File(SAVE_PIC_PATH + "/");
        File imageFoder = new File(SAVE_PIC_PATH + "/" + fileName + ".jpg");
        File pdfFoder = new File(SAVE_PDF_PATH + "/" + FinalImgList.getPdfNameInput() + ".pdf");

        try {
            //文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(imageFoder);
            //压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            //写入，这里会卡顿，因为图片较大
            fileOutputStream.flush();
            //记得要关闭写入流
            fileOutputStream.close();
            //成功的提示，写入成功后，请在对应目录中找保存的图片
            //Toast.makeText(context,"写入成功！目录"+Environment.getExternalStorageDirectory()+"/mfw.png",Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //失败的提示
            //Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            //失败的提示
            // Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(imageFoder);
        intent.setData(uri);
        context.sendBroadcast(intent);//这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了！，记得要传你更新的file哦
        saveNum++;
        FinalImgList.getFinalSavedList().add(position);


        if (saveNum == finalList.size()) {
            try {
                toPdf(imagesPath, pdfFoder, context, finalList, s);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void toPdf(File imageFolderPath, File pdfPath, Context context, List<MyBitmap> finalList, String s) throws DocumentException, IOException {

        try {
            String imagePath = null;
            FileOutputStream fos = new FileOutputStream(pdfPath);
            Document doc = new Document(null, 0, 0, 0, 0);
            PdfWriter.getInstance(doc, fos);
            Bitmap img = null;
            Image image = null;
            int i = 0;
            File file = new File(imageFolderPath + "/");

            File[] files = file.listFiles();


            for (File file1 : files) {
                if (file1.getName().endsWith(".png") || file1.getName().endsWith(".jpg") || file1.getName().endsWith(".jpeg")
                        || file1.getName().endsWith(".tif") || file1.getName().endsWith(".gif")) {
                    //System.out.println("file1.getName(): " + file1.getName());
                    imagePath = imageFolderPath + "/" + file1.getName();
                    img = finalList.get(i).getBm();
                    //System.out.println("file1 = " + file1);
                    //System.out.println("img = " + img);
                    doc.setPageSize(new Rectangle(img.getWidth(), img.getHeight()));
                    //doc.setPageSize(new Rectangle(img.getWidth(),img.getHeight()));
                    image = Image.getInstance(file1.toString());
                    doc.newPage();
                    doc.open();
                    doc.add(image);
                    i++;
                }

            }
            if ("withoutSavePic".equals(s)) {

                deleteDirWihtFile(file);
                //Log.d(TAG, "toPdf: withoutSavePic");
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(new File(SAVE_TEMP_PATH));
                intent.setData(uri);
                context.sendBroadcast(intent);//这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了！，记得要传你更新的file哦
            }
            doc.close();
            PDF_PATH = pdfPath.toString();
            // Toast.makeText(context, "PDF保存在" + pdfPath, Toast.LENGTH_SHORT).show();
            isTopdfSuccess = true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    public void getPermission(RxPermissions rxPermissions, Context context) {

        rxPermissions.requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Action1<Permission>() {
            @Override
            public void call(Permission permission) {
                if (permission.granted) {

                    new toPdfTask().execute();


                } else if (permission.shouldShowRequestPermissionRationale) {
                    Toast.makeText(context, "您拒绝了打开文件读写的权限，无法完成", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "您拒绝了打开文件读写的权限，无法完成", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static Mat bitmapList2Mat(Bitmap bitmap){



           //Bitmap bitmap = BitmapFactory.decodeFile(path);
//            new Thread(new DeepThread(i,bitmapList.get(i).getBm(),matList,module)).start();
            sizeList.add(new Size(bitmap.getWidth(),bitmap.getHeight()));
            bitmap = imageScale(bitmap,320,320);
            final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,TensorImageUtils.TORCHVISION_NORM_STD_RGB);
            IValue input = IValue.from(inputTensor);
            long moduleStart = System.currentTimeMillis();
            IValue[] outputs = module.forward(input).toTuple();
            long moduleEnd = System.currentTimeMillis();

            System.out.println("模型处理时间为："+ (moduleEnd - moduleStart) + "ms");


            Tensor tensor = outputs[0].toTensor();

            float[] tensorFloatArray = tensor.getDataAsFloatArray();
            float min = maxAndMin(tensorFloatArray)[0];
            float max = maxAndMin(tensorFloatArray)[1];
            System.out.println("min: " + min + "max: " + max);
            Mat dst = getNormPred2Mat(tensorFloatArray,max,min);
            dst.convertTo(dst, CvType.CV_8UC1);
            Imgproc.resize(dst,dst,new Size(bitmap.getWidth(),bitmap.getHeight()));



        return dst;
    }

    public static List<Mat> bitmapList2IValueList(List<MyBitmap> bitmapList){
        List<Mat> matList = new ArrayList<>(bitmapList.size());
        sizeList = new ArrayList<>(bitmapList.size());
        for (int i = 0; i < bitmapList.size(); i++) {
            sizeList.add(new Size(bitmapList.get(i).getBm().getWidth(),bitmapList.get(i).getBm().getHeight()));
//            new Thread(new DeepThread(i,bitmapList.get(i).getBm(),matList,module)).start();
            Bitmap bitmap = imageScale(bitmapList.get(i).getBm(),320,320);
            final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,TensorImageUtils.TORCHVISION_NORM_STD_RGB);
            IValue input = IValue.from(inputTensor);
            long moduleStart = System.currentTimeMillis();
            IValue[] outputs = module.forward(input).toTuple();
            long moduleEnd = System.currentTimeMillis();

            System.out.println("模型处理时间为："+ (moduleEnd - moduleStart) + "ms");


            Tensor tensor = outputs[0].toTensor();

            float[] tensorFloatArray = tensor.getDataAsFloatArray();
            float min = maxAndMin(tensorFloatArray)[0];
            float max = maxAndMin(tensorFloatArray)[1];
            System.out.println("min: " + min + "max: " + max);
            Mat dst = getNormPred2Mat(tensorFloatArray,max,min);
            dst.convertTo(dst, CvType.CV_8UC1);
            Imgproc.resize(dst,dst,new Size(bitmap.getWidth(),bitmap.getHeight()));

            matList.add(i,dst);
        }
        return matList;
    }

    //使用深度学习模型提取文档
    public static void AIExtract(Mat input, int i,String path) throws Exception{
        Mat srcmat = input;//320*320
        double quality = 1;
        Size srcSize = sizeList.get(i); //cols = width rows = height
        long en = System.currentTimeMillis();


        int width = (int)srcSize.width;
        int height = (int)srcSize.height;


        if(width >4000 || height > 4000){
            quality = 6;
        }else if (width > 3000 || height > 3000){
            quality = 4;
        }else if(width > 2000 || height > 2000){
            quality = 3;
        }else if (width > 1000 || height > 1000){
            quality = 2;
        }else{
            quality = 1;
        }
        long realStartTime = System.currentTimeMillis();
       ;

        //tempImg = IOHelper.loadBitmap(input, true);
        Mat original =new Mat();
        Bitmap origBmp = BitmapFactory.decodeFile(path);
        if (Global.useDemoire && DemoireUtils.isLoaded()) {
            origBmp = DemoireUtils.removeMoire(origBmp);
        }
        org.opencv.android.Utils.bitmapToMat(origBmp, original);

        Imgproc.resize(srcmat,srcmat , new Size(width/quality,height/quality));

        Mat dstmat = new Mat();
        //腐蚀去除边缘小碎片
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Imgproc.erode(srcmat,dstmat,kernel,new Point(-1,-1),2);



        long contour = System.currentTimeMillis();
        List<MatOfPoint> f_contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dstmat, f_contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        //System.out.println("findContours:  " + dstmat);
        //加粗增强所有找到的轮廓

        MatOfPoint mpoint = Utils.getMaximum(f_contours);
        f_contours.clear();
        f_contours.add(mpoint);
        //画出唯一轮廓
        dstmat.setTo(new Scalar(0));//填充为黑色
        Imgproc.drawContours(dstmat, f_contours, -1, new Scalar(255, 255, 255), 3);

        long e2 = System.currentTimeMillis();
        System.out.println(" findcontours: " + (e2 - contour));

        long lineTime = System.currentTimeMillis();
        Mat lines = new Mat();
        AreaLines areaLines = new AreaLines();
        Point centerPoint = new Point();
        double[] line = new double[] {};


        Imgproc.HoughLinesP(dstmat, lines, 1, PI/180, 80, 20, 30);//使用霍夫变换查找背景色相近的线段 PI/180, 100, 20, 30

        line = getLinesCenter(lines);
        centerPoint = new Point((line[2]+line[0])/2,(line[3]+line[1])/2);



        areaLines = putLines(centerPoint, lines, (int)(line[2]), (int)(line[3]) );
        Global.line = line.clone();
        long e3 = System.currentTimeMillis();
        System.out.println("houghtransform: " + (e3-lineTime));

        long vertexTime = System.currentTimeMillis();
        //获取每个区域的交点坐标
        Point ltp = areaLines.getLeft_top_area().getCrossPoint();
        Point rtp = areaLines.getRight_top_area().getCrossPoint();
        Point rbp = areaLines.getRight_bottom_area().getCrossPoint();
        Point lbp = areaLines.getLeft_bottom_area().getCrossPoint();

        List<CrossPoint> crossPoints = LineGroup.getCrossPoints();

        int position = 0;
        int type = 0;
        int vertex[] = new int[] {0,1,2,3};
        //System.out.println("crossPoint.size = " + crossPoints.size());
        for (int j = 0; j < crossPoints.size(); j++) {
            //System.out.println(crossPoints.get(j).toString());
            if (!crossPoints.get(j).isFlage()) {
                //flage = true;
                position = j;
                type++;
                vertex[j] = 5;
            }
        }

        System.out.println("type: " + type) ;
        //判断边界是否有误
        if(type==1) {

            List<Point> points = Utils.getCrossPointType1(crossPoints, srcmat.cols(), srcmat.rows(),position,srcmat,dstmat);

            ltp = points.get(0);
            rtp = points.get(1);
            rbp = points.get(2);
            lbp = points.get(3);
            //flage = false;
        }else if (type == 2) {
            List<Point> points = Utils.getCrossPointType2(crossPoints, srcmat.cols(), srcmat.rows());
            ltp = points.get(0);
            rtp = points.get(1);
            rbp = points.get(2);
            lbp = points.get(3);
            //flage = false;
        }else if (type == 3) {
            List<Point> points = Utils.getCrossPointType3(crossPoints, vertex, srcmat.cols(), srcmat.rows());
            ltp = points.get(0);
            rtp = points.get(1);
            rbp = points.get(2);
            lbp = points.get(3);

        }
        Global.line = new double[] {};
        long e4 = System.currentTimeMillis();
        System.out.println("vertexes :  " + (e4 - vertexTime));

        long warp = System.currentTimeMillis();
        ltp = new Point(ltp.x * quality,ltp.y * quality);
        rtp = new Point(rtp.x * quality,rtp.y * quality);
        rbp = new Point(rbp.x * quality,rbp.y * quality);
        lbp = new Point(lbp.x * quality,lbp.y * quality);

        //开始做透视变换
        Mat mat = new Mat();
        mat.push_back(new MatOfPoint2f(ltp));
        mat.push_back(new MatOfPoint2f(rtp));
        mat.push_back(new MatOfPoint2f(rbp));
        mat.push_back(new MatOfPoint2f(lbp));

        Size outputSize = getOutputSize(ltp, rtp, rbp, lbp);

        Mat size = new Mat();
        size.push_back(new MatOfPoint2f(new Point(0, 0)));
        size.push_back(new MatOfPoint2f(new Point(outputSize.width, 0)));
        size.push_back(new MatOfPoint2f(new Point(outputSize.width, outputSize.height)));
        size.push_back(new MatOfPoint2f(new Point(0, outputSize.height)));
        Mat pt = Imgproc.getPerspectiveTransform(mat, size);

        //  org.opencv.android.Utils.bitmapToMat(tempImg,srcmat);
        Imgproc.warpPerspective(original, original, pt, new Size(outputSize.width, outputSize.height));
        Imgproc.resize(original,original,new Size(720,540));
        //System.out.println("final_srcmat:  " + srcmat);
        long endTime = System.currentTimeMillis();
        System.out.println("toushi: " + (endTime - warp));
        System.out.println("total: " + (endTime- realStartTime));
        emptybp = Bitmap.createBitmap(original.width(), original.height(), Bitmap.Config.ARGB_8888);

        org.opencv.android.Utils.matToBitmap(original, emptybp);
        MyBitmap myBitmap = new MyBitmap("" + i, emptybp);
        srcmat.release();
        dstmat.release();
        original.release();
        f_contours.clear();
        hierarchy.release();
        LineGroup.crossPoints.clear();
        LineGroup.lineItems.clear();

        Collections.replaceAll(finalList, finalList.get(i), myBitmap);

        successNum++;




    }


    private static int getChannel(Mat dstmat) {
        long startTime = System.currentTimeMillis();

        Point cen = getCenterPoint(dstmat);

        int x = (int)cen.x;
        int y = (int)cen.y;
        int x_d = 1;
        int y_d = 1;
        int redTotal = 0;
        int greenTotal = 0;
        int blueTotal = 0;
        int totalColor = 0;

        for (int j = 0; j < 4; j++) {
            switch (j) {
                case 0:
                    x_d = 0;y_d = -1;break;
                case 1:
                    x_d = 0;y_d = 1;break;
                case 2:
                    x_d = -1;y_d = 0;break;
                case 3:
                    x_d = 1;y_d = 0;break;

            }

            //System.out.println("center : " + cen);
            for(int k = x + x_d*50; k < x + x_d*50+20; k++) {
                for(int m = y + y_d*50; m < y + y_d*50+20; m++) {

                    double[] rgb = dstmat.get(m,k);
                    blueTotal += rgb[0];
                    greenTotal += rgb[1];
                    redTotal += rgb[2];
                    totalColor += 1;

                    //System.out.println("("+ k + "," + m + "): rgb[0]: " + rgb[0] + " ,rgb[1]: " + rgb[1] + " ,rgb[2]: " + rgb[2] );
                }

            }

        }
        double b = blueTotal / totalColor;
        double g = greenTotal / totalColor;
        double r = redTotal / totalColor;

       // System.out.println("blueTotal: " + blueTotal/totalColor + " greenTotal: " + greenTotal/totalColor + "redTotal: " + redTotal/totalColor);

        if(b > g && b > r) {
            System.out.println("getChanneltime: " + (System.currentTimeMillis()-startTime));
            return 0;
        }
        if(g > r && g > b) {
            System.out.println("getChanneltime: " + (System.currentTimeMillis()-startTime));
            return 1;
        }
        if(r > g && r > b) {
            System.out.println("getChanneltime: " + (System.currentTimeMillis()-startTime));
            return 2;
        }





        return 1;

    }


    /**
     * 区域界点
     * @param lines
     * @return
     */
    private static double[] getLinesCenter(Mat lines) {
        Point centerPoint = new Point();
        // TODO Auto-generated method stub
        Point startPoint = new Point();
        Point endPoint = new Point();
        double maxX = 0;
        double maxY = 0;
        double minX = 11111;
        double minY = 11111;
        for (int i = 0; i < lines.rows(); i++) {
            double[] line = lines.get(i, 0);
            //System.out.println(" line[0]:"+line[0]+" line[1]:"+line[1]+" line[2]:"+line[2]+" line[3]:"+line[3]);
            if (line[0] <= minX ) {
                minX = line[0];
            }
            if (line[1] <= minY) {
                minY = line[1];
            }
            if (line[2] >= maxX  ) {
                maxX = line[2];
            }
            if ( line[3] >= maxY ) {
                maxY = line[3];
            }
        }
        //System.out.println("minX:"+minX + "  minY:" + minY + "\nmaxX:"+maxX +"  maxY:"+maxY);
        centerPoint = new Point((maxX-minX)/2,(maxY-minY)/2);
        return new double[] {minX,minY,maxX,maxY};
    }


class extractTask extends AsyncTask<Void, Void, Boolean> {
    private long startTime;
    private long endTime;
    @Override
    protected Boolean doInBackground(Void... voids) {
        int handled = 0;
        startTime = System.currentTimeMillis();
        System.out.println("doInBackground----startTime:" + startTime);
        if (Thread(finalList, finalList.size())) {
            FinalImgList.setIsHandled(true);
            isHandled = true;

        }
        return isHandled;


    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Log.d(TAG, "onPostExecute: run" + isHandled);
        endTime = System.currentTimeMillis();
        System.out.println("onPostExecute----endtime:" + endTime);
        System.out.println("thread用了：" + (endTime - startTime));
        if (isHandled) {
            Log.d(TAG, "isHandled + run" + isHandled);
            pb.setVisibility(View.GONE);


            //handledNum = i;
            FinalImgList.setFinalImgList(finalList);
            //finalList = finalImgListAtExtract2.getFinalImgList();
            // Log.i("finalImgList", "finalImgList.size:" + finalList.get(0).getPath());
            System.out.println("finalList.size() = " + finalList.size());
            showPictures(finalList, context, gridView);
            Log.d("before_emptys", "onPostExecute:  " + FinalImgList.getFinalSrcImgList().size() + "   emptyList: " + emptyList.size());
            //FinalImgList.setFinalSrcImgList(emptyList);
            Log.d("after_emptys", "onPostExecute:  " + FinalImgList.getFinalSrcImgList().size() + "   emptyList: " + emptyList.size());

            // emptyList.clear();
            //testList = finalList;
            if (isRepeat) {
                Toast.makeText(context, "请勿重复处理······", Toast.LENGTH_SHORT).show();
                isRepeat = false;
            } else {
                Toast.makeText(context, "处理完成！成功" + successNum + "个!", Toast.LENGTH_SHORT).show();

            }
            successNum = 0;
            isHandled = false;
        }
    }


}


class toPdfTask extends AsyncTask<Void, Void, Boolean> {


    @Override
    protected Boolean doInBackground(Void... voids) {

        if ("withSavePic".equals(code))
            try {
                saveBitmapPic("withSavePic");
            } catch (IOException e) {
                e.printStackTrace();
            }
        else if ("withoutSavePic".equals(code)) {
            try {
                saveBitmapPic("withoutSavePic");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return isHandled;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (isTopdfSuccess) {
            pb.setVisibility(View.GONE);
            Toast.makeText(context, "PDF保存在" + PDF_PATH + "中！", Toast.LENGTH_LONG).show();
            MainActivity mainActivity = (MainActivity) getActivity();
            BottomNavigationView bottomNavigationView = mainActivity.getNavView();
            bottomNavigationView.setSelectedItemId(R.id.navigation_pdfs);

        }
    }


}

}
