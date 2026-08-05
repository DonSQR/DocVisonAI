package com.example.conference.ui.getpicturefromallpicture;


import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.conference.R;
import com.example.conference.imageExtract.FinalImgList;
import com.example.conference.ui.selectfragment.SelectFragment;
import com.example.conference.utils.ToFragmentListener;
import com.example.conference.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class GetPicFromAll extends AppCompatActivity  {
    private View root;
    private GridView gridView;
    private AllPicAdapter allPicAdapter;
    private AllPicBitmap allPicBitmap;
    private boolean isSelect = false;
    private List<AllPicBitmap> allPicList = new ArrayList<AllPicBitmap>();
    private List<AllPicBitmap> blankAllPicList = new ArrayList<AllPicBitmap>();
    private ToFragmentListener toFragmentListener;
    private SelectFragment selectFragment;
    private String ALL_PIC_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera/";
    private Context context;
    private FloatingActionButton conformBtn;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_allpic);
        gridView = findViewById(R.id.gv_allpic);
        Utils.getAllPic(ALL_PIC_PATH, allPicList);

        allPicAdapter = new AllPicAdapter(GetPicFromAll.this, allPicList);
        gridView.setAdapter(allPicAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                allPicList.get(i).setSelect(!isSelect);
                isSelect = !isSelect;
                if (allPicList.get(i).isSelect() == true){
                    view.findViewById(R.id.all_image_btn).setBackgroundResource(R.drawable.all_selected);
                }else {
                    view.findViewById(R.id.all_image_btn).setBackgroundResource(R.drawable.all_selectable);

                }
            }
        });

        conformBtn = findViewById(R.id.select_confirm);
        conformBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int total = 0;
                System.out.println("allPicList.size = " + blankAllPicList.size());
                for (int i = 0;i < allPicList.size();i++){
                    if (allPicList.get(i).isSelect()){
                        blankAllPicList.add(total++,allPicList.get(i));
                    }
                }
                System.out.println("allPicList.size = " + blankAllPicList.size());
               FinalImgList.setAllSelectedPicList(blankAllPicList);
            }
        });
        selectFragment = new SelectFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("key", (ArrayList<? extends Parcelable>) blankAllPicList);
        selectFragment.setArguments(bundle);



    }



//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        root = inflater.inflate(R.layout.fragment_allpic, container, false);
//        context = inflater.getContext();
//        gridView = root.findViewById(R.id.gv_allpic);
//        Utils.getAllPic(ALL_PIC_PATH, allPicBitmap, allPicList);
//        allPicAdapter = new AllPicAdapter(context, allPicList);
//        gridView.setAdapter(allPicAdapter);
//
//        FinalImgList.setAllSelectedPicList(allPicList);
//        return root;
//    }
}
