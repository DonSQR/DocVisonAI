package com.example.conference.ui.pdfsfragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.conference.MainActivity;
import com.example.conference.R;
import com.example.conference.imageExtract.FinalImgList;
import com.example.conference.utils.MyPdfAdapter;
import com.example.conference.utils.MyPdfBitmap;
import com.example.conference.utils.PdfNameLength;
import com.example.conference.utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.itextpdf.text.pdf.PdfReader;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PdfFragment extends Fragment {
    private View root;
    private Context context;
    private GridView gridView;
    private TextView textView;
    private ImageView imageView;
    private List<MyPdfBitmap> pdfList = new ArrayList<MyPdfBitmap>();
    private MyPdfAdapter myPdfAdapter;
    private MyPdfBitmap myPdfBitmap;
    private String TAG = "peralk";
    private String pdfNameInput;
    private File[] pdfFiles;
    private File file;
    private static final String SAVE_PIC_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Conference/pdf/";
    private View nowView;
    private int nowPosition;
    private boolean isItemOnLongtimeSelect = false;
    private RelativeLayout relativeLayout;
    private RelativeLayout itemRelativeLayout;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_pdfs, container, false);
        context = inflater.getContext();
        gridView = root.findViewById(R.id.gv_pdfs);
        textView = root.findViewById(R.id.pdf_name);
        imageView = root.findViewById(R.id.pdf_image);
        pdfNameInput = FinalImgList.getPdfNameInput();
        relativeLayout = root.findViewById(R.id.fragment_pdfs);

        File imagesPath = new File(SAVE_PIC_PATH);
        file = new File(imagesPath.toString());
        pdfFiles = file.listFiles();

        if (pdfFiles != null) {
            for (File file1 : pdfFiles
            ) {
                if (file1.getName().endsWith(".pdf")) {
                    myPdfBitmap = new MyPdfBitmap();
                    myPdfBitmap.setPdfName(file1.getName());
                    myPdfBitmap.setFilePath(file1.getPath());
                    System.out.println("file1.getName = " + file1.getName());
                    System.out.println("file1.getPath = " + file1.getPath());
                    pdfList.add(myPdfBitmap);

                }

            }
        }
        myPdfAdapter = new MyPdfAdapter(context, pdfList);
        gridView.setAdapter(myPdfAdapter);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nowView!=null&&nowView.findViewById(R.id.item_deleta_btn).getVisibility() == View.VISIBLE)
                nowView.findViewById(R.id.item_deleta_btn).setVisibility(View.GONE);
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                if (nowPosition != i && isItemOnLongtimeSelect ) {
                    nowView.findViewById(R.id.item_deleta_btn).setVisibility(View.GONE);
                    isItemOnLongtimeSelect = false;
                    return;
                }else {
                    openPdf(root, pdfList.get(i).getFilePath());
                }


            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {


                if (nowPosition != position && isItemOnLongtimeSelect ) {
                    nowView.findViewById(R.id.item_deleta_btn).setVisibility(View.GONE);
                    isItemOnLongtimeSelect = false;

                }

                Button btn = view.findViewById(R.id.item_deleta_btn);
                btn.setVisibility(View.VISIBLE);
                nowView = view;
                nowPosition = position;
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        alertDialog.setTitle("确认框")
                                .setMessage("确认删除当前文件吗？")
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(context, "您取消了删除操作~", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pdfFiles[position].delete();
                                        MainActivity mainActivity = (MainActivity)getActivity();
                                        BottomNavigationView bottomNavigationView = mainActivity.getNavView();
                                        bottomNavigationView.setSelectedItemId(R.id.navigation_pdfs);

                                        Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .create()
                                .show();
                    }
                });

                isItemOnLongtimeSelect = true;
                return false;
            }
        });
        return root;
    }


    public void openPdf(View view, String path) {

        File file = new File(path);
//        Intent target = new Intent(Intent.ACTION_VIEW);
//        target.setDataAndType(Uri.fromFile(file), "application/pdf");
//        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//
//        Intent intent = Intent.createChooser(target, "Open File");
//        try {
//            startActivity(intent);
//        } catch (ActivityNotFoundException e) {
//            Toast.makeText(context, "打开失败", Toast.LENGTH_LONG).show();
        try {
//            InputStream inputStream = null;
//            PdfReader reader = null;
//            AssetManager assetManager = context.getAssets();
//
//            inputStream = (InputStream) assetManager.open(path);
//            reader = new PdfReader(inputStream);


            Utils.openFile(context, new File(path));

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
