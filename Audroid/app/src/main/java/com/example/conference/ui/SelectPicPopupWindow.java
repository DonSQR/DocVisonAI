package com.example.conference.ui;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.example.conference.R;

public class SelectPicPopupWindow extends PopupWindow {
    private Button select_from_camera,
                    select_from_photos,
                    cancel_select;
    private View menuview;

    public SelectPicPopupWindow(Activity context, View.OnClickListener itemsOnclick){
        super(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        menuview = inflater.inflate(R.layout.item_popupwindows,null);
        select_from_camera = (Button)menuview.findViewById(R.id.select_from_camera);
        select_from_photos = (Button)menuview.findViewById(R.id.select_from_photos);
        cancel_select = (Button)menuview.findViewById(R.id.cancel_select);
        /**
         * 取消按钮销毁事件
         */
        cancel_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                dismiss();
            }
        });
        select_from_camera.setOnClickListener(itemsOnclick);
        select_from_photos.setOnClickListener(itemsOnclick);
        //设置SelectPicPopupWindow的View
        this.setContentView(menuview);
        this.setWidth(ViewGroup.LayoutParams.FILL_PARENT);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setFocusable(true);
        ColorDrawable drawable = new ColorDrawable(0xb000000);
        this.setBackgroundDrawable(drawable);

        menuview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event){
                int height = menuview.findViewById(R.id.popup).getTop();
                int y = (int)event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP){
                    if (y<height){
                        dismiss();
                    }
                }
                return true;
            }
        });









    }
}
