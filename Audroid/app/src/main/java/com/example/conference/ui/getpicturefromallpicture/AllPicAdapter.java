package com.example.conference.ui.getpicturefromallpicture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.conference.R;

import java.util.ArrayList;
import java.util.List;

public class AllPicAdapter extends BaseAdapter {
    private List<AllPicBitmap> selectList = new ArrayList<AllPicBitmap>();
    private LayoutInflater inflater;
    private Context context;
    public AllPicAdapter(Context context, List<AllPicBitmap> selectList){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.selectList = selectList;

    }

    private class Holder {


        public ImageView item_all_img,item_select_pic;

        public ImageView getItem_all_img() {
            return item_all_img;
        }

        public ImageView getItem_select_pic() {
            return item_select_pic;
        }

        public void setItem_all_img(ImageView item_all_img) {
            this.item_all_img = item_all_img;
        }

        public void setItem_select_pic(ImageView item_select_pic) {
            this.item_select_pic = item_select_pic;
        }
    }


    @Override
    public int getCount() {
        return selectList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        AllPicAdapter.Holder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.item_all_pic, null);

            holder = new AllPicAdapter.Holder();
            holder.item_all_img = view.findViewById(R.id.item_pic);
            holder.item_select_pic = view.findViewById(R.id.all_image_btn);
            view.setTag(holder);
        } else {
            holder = (AllPicAdapter.Holder) view.getTag();
        }

        AllPicBitmap myBitmap = selectList.get(i);

        holder.item_all_img.setImageBitmap(myBitmap.getBitmap());

        return view;
    }
}
