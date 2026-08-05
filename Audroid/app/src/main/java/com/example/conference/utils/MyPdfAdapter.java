package com.example.conference.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.conference.R;
import com.example.conference.imageExtract.MyBitmap;
import com.example.conference.imageExtract.Myadapter;

import java.util.ArrayList;
import java.util.List;

public class MyPdfAdapter extends BaseAdapter {
    private List<MyPdfBitmap> pdfList = new ArrayList<MyPdfBitmap>();
    private Context context;
    private LayoutInflater inflater;

    public MyPdfAdapter(Context context, List<MyPdfBitmap> pdfList) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.pdfList = pdfList;
    }

    public static class Holder {
        private  ImageView imageView;
        private  TextView textView;
        private  Button itemDeleatBtn;

        public void setImageView(ImageView imageView) {
            this.imageView = imageView;
        }

        public void setTextView(TextView textView) {
            this.textView = textView;
        }

        public  ImageView getImageView() {
            return imageView;
        }

        public  TextView getTextView() {
            return textView;
        }

        public void setItemDeleatBtn(Button itemDeleatBtn) {
            this.itemDeleatBtn = itemDeleatBtn;
        }

        public Button getItemDeleatBtn() {
            return itemDeleatBtn;
        }
    }


    @Override
    public int getCount() {
        return pdfList.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.item_pdf, null);

            holder = new MyPdfAdapter.Holder();
            holder.imageView = (ImageView) view.findViewById(R.id.pdf_image);
            holder.textView = (TextView) view.findViewById(R.id.pdf_name);
            holder.itemDeleatBtn = (Button) view.findViewById(R.id.item_deleta_btn);
            //holder.textView.setTextSize(20);



            view.setTag(holder);
        } else {
            holder = (MyPdfAdapter.Holder) view.getTag();
        }
        MyPdfBitmap myPdfBitmap = pdfList.get(i);
        int width = PdfNameLength.getPdfNameLength();
        System.out.println("width" + width);
        if (myPdfBitmap.getPdfName().length()>10){
            System.out.println("pdfNameLength : " + myPdfBitmap.getPdfName().length());
            holder.textView.setTextSize((float) (myPdfBitmap.getPdfName().length()*(-9.0/8)+35.5));
            //holder.textView.setTextSize((float));

          //  holder.getImageView()
        }
        holder.textView.setText(myPdfBitmap.getPdfName() );
        return view;
    }
}
