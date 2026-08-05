package com.example.conference.imageExtract;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.example.conference.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 丁瑞 on 2017/4/13.
 */
public class Myadapter extends BaseAdapter {

    private Context context = null;
    private List<MyBitmap> list = new ArrayList<MyBitmap>();
    private int imgId[] = null;
    private LayoutInflater inflater;

    public Myadapter(Context cotext, List<MyBitmap> list) {
        this.context = cotext;
        inflater = LayoutInflater.from(cotext);
        this.list = list;
    }

    private class Holder {


        public ImageView item_img;
        public Button item_deleat, add_item, exchange_item;

        public Button getAdd_item() {
            return add_item;
        }

        public Button getExchange_item() {
            return exchange_item;
        }

        public void setAdd_item(Button add_item) {
            this.add_item = add_item;
        }

        public void setExchange_item(Button exchange_item) {
            this.exchange_item = exchange_item;
        }

        public ImageView getItem_img() {
            return item_img;
        }

        public Button getItem_deleat() {
            return item_deleat;
        }

        public void setItem_img(ImageView item_img) {
            this.item_img = item_img;
        }

        public void setItem_deleat(Button item_deleat) {
            this.item_deleat = item_deleat;
        }
    }


    @Override
    public int getCount() {


        return list.size();

    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.item_picture, null);

            holder = new Holder();
            holder.item_img = (ImageView) view.findViewById(R.id.item_jpg);
            holder.item_deleat = (Button) view.findViewById(R.id.deleta_pic);
            holder.add_item = (Button) view.findViewById(R.id.add_pic);
            holder.exchange_item = view.findViewById(R.id.exchange_pic);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        MyBitmap myBitmap = list.get(i);

        holder.item_img.setImageBitmap(myBitmap.getBm());

        return view;
    }


}

