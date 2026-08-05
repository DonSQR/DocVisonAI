package com.example.conference.ui.photopreview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.conference.R;

import java.util.ArrayList;
import java.util.List;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;

public class ShowImagesDialig extends Dialog {

    private View mview;
    private Context context;
    private ShowImagesViewPager mViewPager;
    private List<String> mImgUrls;
    private List<View> mViews;
    private ShowImagesAdapter mAdapter;


    public ShowImagesDialig(@NonNull Context context, List<String> imgUrls) {
        super(context, R.style.transparentBgDialog);
        this.context = context;
        this.mImgUrls = imgUrls;
        initView();
        initData();
    }

    private void initData() {
        OnPhotoTapListener listener = new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                dismiss();//点击图片后返回到原来的界面
            }
        };
        for (int i = 0; i < mImgUrls.size(); i++) {
            final PhotoView photoView = new PhotoView(context);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            photoView.setLayoutParams(layoutParams);
            photoView.setOnPhotoTapListener(listener);
            Glide.with(context)
                    .load(mImgUrls.get(i))
                    .placeholder(R.drawable.broken)
                    .error(R.drawable.broken)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            photoView.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
            mViews.add(photoView);
        }
        mAdapter = new ShowImagesAdapter(mViews);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void initView() {
        mview = View.inflate(context,R.layout.dialog_images_brower,null);
        mViewPager = (ShowImagesViewPager)mview.findViewById(R.id.vp_images);
        mViews = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mview);
        Window window = getWindow();
        WindowManager.LayoutParams w1 = window.getAttributes();
        w1.x = 0;
        w1.y = 0;
        w1.height = Config.EXACR_SCREEN_HEIGHT;
        w1.width = Config.EXACR_SCREEN_WIDTH;
        w1.gravity = Gravity.CENTER;
        window.setAttributes(w1);
    }

















}
