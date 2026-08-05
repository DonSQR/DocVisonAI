package com.example.conference.ui.photopreview;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShowImagesAdapter  extends PagerAdapter {
    private List<View> mViews;

    public ShowImagesAdapter(List<View> mViews) {
        this.mViews = mViews;
    }




    @Override
    public int getCount() {
        return mViews.size();
    }

    @Override
    public boolean isViewFromObject( View view,  Object object) {
        return view == object;
    }

    @Override
    public void destroyItem( ViewGroup container, int position,  Object object) {
        ((ViewPager) container).removeView(mViews.get(position));

    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ((ViewPager) container).addView(mViews.get(position));

        return mViews.get(position);
    }
}
