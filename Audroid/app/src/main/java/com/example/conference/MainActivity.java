package com.example.conference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.conference.imageExtract.FinalImgList;
import com.example.conference.imageExtract.MyBitmap;
import com.example.conference.ui.handlefragment.HandleFragment;
import com.example.conference.ui.pdfsfragment.PdfFragment;
import com.example.conference.ui.selectfragment.SelectFragment;
import com.example.conference.utils.Global;
import com.example.conference.utils.IOHelper;
import com.example.conference.utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.donkingliang.imageselector.utils.ImageSelector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = "peralk";
    private BottomNavigationView navView;

    private BaseLoaderCallback mbaseLoderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            Log.i(TAG, "OpenCV load status: " + status);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_select, R.id.navigation_handle, R.id.navigation_pdfs).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        navView.setItemIconTintList(null);
        NavigationUI.setupWithNavController(navView, navController);
        staticLoadCVLibraries();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,

                WindowManager.LayoutParams.FLAG_FULLSCREEN);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.ai:
                Global.method = "ai";
                Toast.makeText(this,"已切换成深度学习方法",Toast.LENGTH_SHORT).show();
                break;
            case R.id.reset:
                Utils.resetAll();

                BottomNavigationView bottomNavigationView = getNavView();
                bottomNavigationView.setSelectedItemId(R.id.navigation_select);
                Toast.makeText(this,"已重置页面",Toast.LENGTH_SHORT).show();
                break;
            case R.id.demoire:
                Global.useDemoire = !Global.useDemoire;
                Toast.makeText(this,
                        Global.useDemoire ? "已开启去摩尔纹" : "已关闭去摩尔纹",
                        Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void staticLoadCVLibraries() {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mbaseLoderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mbaseLoderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public BottomNavigationView getNavView() {
        return navView;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       // Toast.makeText(this, "requestCode = " + requestCode, Toast.LENGTH_SHORT).show();
        if (requestCode == 520) {
            List<MyBitmap> myList = new ArrayList<MyBitmap>();
            if (FinalImgList.getFinalSrcImgList().size() != 0) {
                myList = FinalImgList.getFinalSrcImgList();
            }
            List<String> list = new ArrayList<String>();
            list = data.getStringArrayListExtra(ImageSelector.SELECT_RESULT);
            FinalImgList.setList(list, 520);
            for (int i = 0; i < list.size(); i++) {
                MyBitmap myBitmap = new MyBitmap(list.get(i), IOHelper.loadBitmap(list.get(i),true));
                myList.add(myBitmap);

            }
            FinalImgList.setFinalSrcImgList(myList);

            BottomNavigationView bottomNavigationView = getNavView();
            bottomNavigationView.setSelectedItemId(R.id.navigation_select);
        }

        if (requestCode == 0402) {

            List<MyBitmap> myList = new ArrayList<MyBitmap>();
            List<MyBitmap> emptyList = new ArrayList<MyBitmap>();
            if (FinalImgList.getFinalSrcImgList().size() != 0 && "select".equals(FinalImgList.getString())) {
                myList = FinalImgList.getFinalSrcImgList();
            } else if (FinalImgList.getFinalImgList().size() != 0 && "handle".equals(FinalImgList.getString())) {
                myList = FinalImgList.getFinalImgList();
            }
            List<String> list = new ArrayList<String>();
            list = data.getStringArrayListExtra(ImageSelector.SELECT_RESULT);
            FinalImgList.setList(list, 0402);
            int position = FinalImgList.getPosition();
            //Log.d("myList", "onActivityResult: " + myList.size());
            for (int i = 0; i < list.size(); i++) {
                MyBitmap myBitmap = new MyBitmap(list.get(i), IOHelper.loadBitmap(list.get(i),true));
                myList.add(position++, myBitmap);
                emptyList.add(i,myBitmap);
            }

            BottomNavigationView bottomNavigationView = getNavView();
            if ("select".equals(FinalImgList.getString())) {
                FinalImgList.setFinalSrcImgList(myList);
                bottomNavigationView.setSelectedItemId(R.id.navigation_select);
            } else if ("handle".equals(FinalImgList.getString())) {
                FinalImgList.setFinalImgList(myList);
                int j = FinalImgList.getPosition();
                for (int i =0 ; i <emptyList.size();i++){
                    FinalImgList.getFinalSrcImgList().add(j,emptyList.get(i));

                }

                bottomNavigationView.setSelectedItemId(R.id.navigation_handle);
            }

        }

    }
}