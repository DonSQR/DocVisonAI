package com.example.conference.ui.selectfragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;


import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.conference.MainActivity;
import com.example.conference.R;
import com.example.conference.imageExtract.FinalImgList;
import com.example.conference.imageExtract.MyBitmap;
import com.example.conference.imageExtract.Myadapter;
import com.example.conference.ui.SelectPicPopupWindow;
import com.example.conference.ui.getpicturefromallpicture.AllPicBitmap;
import com.example.conference.ui.getpicturefromallpicture.GetPicFromAll;
import com.example.conference.ui.handlefragment.HandleFragment;
import com.example.conference.ui.photopreview.Config;
import com.example.conference.ui.photopreview.ShowImagesDialig;
import com.example.conference.utils.Global;
import com.example.conference.utils.IOHelper;
import com.example.conference.utils.ToFragmentListener;
import com.example.conference.utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.donkingliang.imageselector.utils.ImageSelector;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.functions.Action1;

import static android.app.Activity.RESULT_OK;
import static com.example.conference.utils.Utils.exchangePic;
import static com.example.conference.utils.Utils.getAngle;
import static com.example.conference.utils.Utils.showPictures;

public class SelectFragment extends Fragment {
    private FloatingActionButton selectBtn;
    private FloatingActionButton confirmBtn;
    private SelectPicPopupWindow menuWindow;
    private Context MainContext;
    private RxPermissions rxPermissions;
    private long systemTime1;
    private long systemTime2;
    private int REQUEST_SMALL = 111;
    private String TAG = "peralk";
    private int CODE = 1314;
    private String name = "select";
    private static List<MyBitmap> finalList = new ArrayList<MyBitmap>();
    private Bitmap exchange_bitmap;
    private int exchangePosition;
    private int MaxSize = 30;
    private GridView gridView;
    private boolean isSrcEmpty = true;
    private static boolean isLode = false;
    private int nowPosition = 0;
    private View nowView;
    private int times = 0;
    private List<AllPicBitmap> emptyAllList = new ArrayList<AllPicBitmap>();
    private View root;
    private int reqCode = 520;
    private boolean isItemOnLongtimeSelect = false;
    private RelativeLayout relativeLayout;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        refreshImages();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshImages();
    }

    private void refreshImages() {
        if (MainContext == null || gridView == null) return;
        if (FinalImgList.getFinalSrcImgList().size() > 0) {
            finalList = FinalImgList.getFinalSrcImgList();
            for (int i = 0; i < finalList.size(); i++) {
                if (finalList.get(i).getBm() == null) {
                    finalList.get(i).setBm(IOHelper.loadBitmap(finalList.get(i).getPath(), true));
                }
            }
            showPictures(finalList, MainContext, gridView);
        }
    }

    public static void setFinalList(List<MyBitmap> finalList) {
        SelectFragment.finalList = finalList;
    }

    public static void setIsLode(boolean isLode){
        SelectFragment.isLode = isLode;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_select, container, false);
        selectBtn = root.findViewById(R.id.select_imgs);
        confirmBtn = root.findViewById(R.id.confirm);
        MainContext = inflater.getContext();
        rxPermissions = new RxPermissions((Activity) inflater.getContext());
        gridView = root.findViewById(R.id.gvMain);
        relativeLayout = root.findViewById(R.id.fragment_select);


            if (finalList.size() != 0 && !isLode) {
                isLode = true;
                // Log.d("if_first", "firstIf");
                for (int i = 0; i < finalList.size(); i++) {
                    finalList.get(i).setBm(IOHelper.loadBitmap(finalList.get(i).getPath(), true));
                }
                Myadapter myadapter = new Myadapter(MainContext, finalList);
                gridView.setAdapter(myadapter);
            }
            if (FinalImgList.getList().size() != 0 && FinalImgList.getRequestCode() == 520) {
                //Log.d("if_second", "secondIf ");
                isLode = true;
                finalList = FinalImgList.getFinalSrcImgList();
                for (int i = 0; i < finalList.size(); i++) {
                    finalList.get(i).setBm(IOHelper.loadBitmap(finalList.get(i).getPath(), true));
                }
                showPictures(finalList, MainContext, gridView);
                FinalImgList.setRequestCode(0);
            }
            if (FinalImgList.getList().size() != 0 && FinalImgList.getRequestCode() == 0402) {
                //Log.d("if_third", "thirdIf");
                isLode = true;
                finalList = FinalImgList.getFinalSrcImgList();
                for (int i = 0; i < finalList.size(); i++) {
                    finalList.get(i).setBm(IOHelper.loadBitmap(finalList.get(i).getPath(), true));
                }
                showPictures(finalList, MainContext, gridView);
                FinalImgList.setRequestCode(0);
            }


            Bundle b = getArguments();
            if (null != b) {
                emptyAllList = b.<AllPicBitmap>getParcelableArrayList("key");
                if (emptyAllList.size() != 0) {
                    // System.out.println("emptyAllList.size = " + emptyAllList.size());
                    //System.out.println("emptyAllList.get(0) = " + emptyAllList.get(0).getPath());

                }

            }

            if (FinalImgList.getFinalSrcImgList().size() != 0 && FinalImgList.getRequestCode() == 0 && !isLode) {
                //Log.d("if_forth", "forthIf");
                finalList = FinalImgList.getFinalSrcImgList();
                for (int i = 0; i < finalList.size(); i++) {
                    finalList.get(i).setBm(IOHelper.loadBitmap(finalList.get(i).getPath(), true));
                }
                //System.out.println("finalList = " + FinalImgList.getFinalSrcImgList().get(0).getPath());
                showPictures(finalList, MainContext, gridView);
            }
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nowView != null) {
                    nowView.findViewById(R.id.deleta_pic).setVisibility(View.GONE);
                    nowView.findViewById(R.id.add_pic).setVisibility(View.GONE);
                    nowView.findViewById(R.id.exchange_pic).setVisibility(View.GONE);
                }
            }
        });
        selectBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v){
                selectBtn.setVisibility(View.GONE);
                return false;
            }
        });
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPopupWindow();
            }
        });
        confirmBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v){
                confirmBtn.setVisibility(View.GONE);
                return false;
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Toast.makeText(inflater.getContext(), "你点击了confirm", Toast.LENGTH_SHORT).show();

//                Intent intent = new Intent();
//                intent.setClass(getActivity(), HandleFragment.class);
//                intent.putExtra("finalList",(Serializable)finalList);
//                getActivity().startActivity(intent);
                if (finalList.size() == 0) {
                    Toast.makeText(MainContext, "请先选择图片······", Toast.LENGTH_SHORT).show();
                } else {


                    AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(MainContext);
                    myAlertDialog.setTitle("确认框")
                            .setMessage("确认将此列表生成为预处理列表吗?点击确定将跳转到处理页面并将已处理图片列表删除！")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(MainContext, "您取消了生成处理列表", Toast.LENGTH_SHORT).show();

                                }
                            })
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity mainActivity = (MainActivity) getActivity();
                                    BottomNavigationView bottomNavigationView = mainActivity.getNavView();
                                    bottomNavigationView.setSelectedItemId(R.id.navigation_handle);
                                    HandleFragment.setFinalList(finalList);
                                    FinalImgList.setFinalImgList(finalList);
                                }
                            })
                            .create()
                            .show();


                    //  FinalImgList.setFinalSrcImgList(finalList);

                }
            }
        });


        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int position, long l) {
                //  exchangePosition =  Utils.changePics(adapterView,MainContext,gridView,finalList,position,CODE,view,getActivity());
                if (adapterView != null) {
                    nowPosition = position;
                    nowView = view;
                    // Toast.makeText(MainContext, "你长按的是第" + position + "个item", Toast.LENGTH_SHORT).show();
                    Button delButton = view.findViewById(R.id.deleta_pic);
                    delButton.setVisibility(View.VISIBLE);
                    delButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Toast.makeText(MainContext, "点到了del", Toast.LENGTH_SHORT).show();
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainContext);
                            builder.setIcon(R.drawable.red_deleat)
                                    .setTitle("确认框")
                                    .setMessage("确定删除此照片吗？")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Utils.removeItem(position, finalList, MainContext, gridView, name);
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainContext);
                            builder.setIcon(R.drawable.add_red)
                                    .setTitle("确认框")
                                    .setMessage("确定在此照片之前插入照片吗？")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Utils.addPic(position, MainContext, "select");

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
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainContext);
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
                    isItemOnLongtimeSelect = true;
                }
                return true;
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final List<String> urls = new ArrayList<>();
                getDeviceDensity();
                if (nowPosition != i && isItemOnLongtimeSelect) {
                    nowView.findViewById(R.id.deleta_pic).setVisibility(View.GONE);
                    nowView.findViewById(R.id.add_pic).setVisibility(View.GONE);
                    nowView.findViewById(R.id.exchange_pic).setVisibility(View.GONE);
                    isItemOnLongtimeSelect = false;
                    return;
                }
                if (!isItemOnLongtimeSelect) {
                    for (int j = 0; j < finalList.size(); j++) {
                        urls.add(finalList.get(j).getPath());
                    }
                    new ShowImagesDialig(MainContext, urls).show();
                }


            }
        });

        return root;
    }

    private void getDeviceDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Config.EXACR_SCREEN_HEIGHT = metrics.heightPixels;
        Config.EXACR_SCREEN_WIDTH = metrics.widthPixels;

    }

    private void isSrcEmpty(List<MyBitmap> finalList) {
        for (int i = 0; i < finalList.size(); i++) {
            if (finalList.get(i).getPath().length() < 5 || finalList == null) {
                //System.out.println("finalList.get(i).getPath() = " + finalList.get(i).getPath());
                isSrcEmpty = true;
                return;
            } else {
                //System.out.println("finalList.get(i).getPath() = " + finalList.get(i).getPath());
                continue;
            }


        }

        isSrcEmpty = false;


    }


    private void initPopupWindow() {
        menuWindow = new SelectPicPopupWindow((Activity) MainContext, itemsOnClick);
        menuWindow.showAtLocation(((Activity) MainContext).findViewById(R.id.fragment_select), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                case R.id.select_from_camera:
                    Utils.getCameraPermission(rxPermissions, MainContext, new Runnable() {
                        @Override
                        public void run() {
                            takeOnCamera();
                        }
                    });
                    break;
                case R.id.select_from_photos:
                    // 先检查存储权限是否已给
                    if (android.content.pm.PackageManager.PERMISSION_GRANTED
                            == androidx.core.content.ContextCompat.checkSelfPermission(MainContext, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // 已授权，直接打开选择器
                        ImageSelector.builder()
                                .setMaxSelectCount(30)
                                .setSingle(false)
                                .useCamera(true)
                                .start((Activity) MainContext, reqCode);
                    } else {
                        // 未授权，请求权限
                        rxPermissions.requestEach(Manifest.permission.READ_EXTERNAL_STORAGE)
                                .subscribe(new rx.functions.Action1<com.tbruyelle.rxpermissions.Permission>() {
                                    @Override
                                    public void call(com.tbruyelle.rxpermissions.Permission permission) {
                                        if (permission.granted) {
                                            ImageSelector.builder()
                                                    .setMaxSelectCount(30)
                                                    .setSingle(false)
                                                    .useCamera(true)
                                                    .start((Activity) MainContext, reqCode);
                                        } else {
                                            Toast.makeText(MainContext, "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }, new rx.functions.Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        Toast.makeText(MainContext, "权限请求失败", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    break;
                default:
                    break;
            }
        }
    };


    //打开相机
    public void takeOnCamera() {
        //打开相机之前记录时间
        systemTime1 = getSystemTime();
        Intent intent = new Intent();
        try {
            intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
            startActivityForResult(intent, REQUEST_SMALL);

        } catch (Exception e) {
            try {
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
                startActivityForResult(intent, REQUEST_SMALL);
            } catch (Exception e1) {
                try {
                    intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
                    startActivityForResult(intent, REQUEST_SMALL);
                } catch (Exception e2) {
                    Toast.makeText(MainContext, "请从相册选择", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //获取当前时间
    public Long getSystemTime() {
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

    @SuppressWarnings("all")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("requestCode", "requestCode: " + requestCode);
        systemTime2 = getSystemTime();
        ContentResolver contentResolver = MainContext.getContentResolver();
        if (requestCode == REQUEST_SMALL) {
            Log.e(TAG, "cameraRequestCode:" + requestCode);
            Utils.getContactList(rxPermissions, gridView,
                    MainContext, finalList, MaxSize, systemTime1, systemTime2);

            //finalImgList.clear();
            FinalImgList.setFinalSrcImgList(finalList);

        }
        if (requestCode == CODE) {
            if (data != null) {
                Log.e(TAG, "CODE requestCode:" + requestCode);
                try {
                    Uri orginalUri = data.getData();
                    //将图片内容解析成字节数组
                    byte[] mContent = Utils.readStream(contentResolver.openInputStream(Uri.parse(orginalUri.toString())));
                    //将字节数组转换为ImageView可调用的Bitmap对象
                    String getFromPic = data.getData().getPath();
                    Log.e(TAG, "getFromPic: " + getFromPic);
                    String finalSelectPic = getFromPic.substring(5, getFromPic.length());
                    Log.e(TAG, "finalSelectPic: " + finalSelectPic);
                    Log.e(TAG, "getFromPic: " + getFromPic.substring(5, getFromPic.length()));
                    exchange_bitmap = Utils.getPicFromBytes(mContent, null);
                    Log.e(TAG, "exchange_picUrl: " + data.getData().getPath());
                    MyBitmap exchangeBitmap = new MyBitmap(finalSelectPic, exchange_bitmap);
                    finalList.add(exchangePosition, exchangeBitmap);
                    finalList.remove(exchangePosition + 1);
                    System.out.println("exchangePosition" + exchangePosition);

//                    for (int i = 0; i < finalList.size(); i++) {
//                        finalList.get(i).setBm(IOHelper.loadBitmap(finalList.get(i).getPath(), true));
//                    }

                    showPictures(finalList,MainContext,gridView);

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            FinalImgList.setFinalSrcImgList(finalList);
        }


        if (requestCode == reqCode && resultCode == Activity.RESULT_OK) {

            // Toast.makeText(MainContext, "reqCode = " + requestCode, Toast.LENGTH_SHORT).show();

            List<String> list = data.getStringArrayListExtra(ImageSelector.SELECT_RESULT);
            for (int i = 0; i < list.size(); i++) {
                MyBitmap myBitmap = new MyBitmap(list.get(i), BitmapFactory.decodeFile(list.get(i)));
                finalList.add(myBitmap);
                showPictures(finalList,MainContext,gridView);
            }
            FinalImgList.setFinalSrcImgList(finalList);

        }


        super.onActivityResult(requestCode, resultCode, data);
    }



    public void exchangePic(int position) {

        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setType("image/*");
        startActivityForResult(intent, CODE);

    }


}
