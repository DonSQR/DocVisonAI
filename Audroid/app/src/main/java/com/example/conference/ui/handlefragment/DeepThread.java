package com.example.conference.ui.handlefragment;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.util.List;

import static com.example.conference.utils.Utils.getNormPred2Mat;
import static com.example.conference.utils.Utils.imageScale;
import static com.example.conference.utils.Utils.maxAndMin;

public class DeepThread implements Runnable {
    private int position = 0;
    private Bitmap bp = null;
    public List<Mat> matList = null;
    public Module module = null;


    public DeepThread(int position, Bitmap bp, List<Mat> matList, Module module ){
        this.position = position;
        this.bp = bp;
        this.matList = matList;
        this.module = module;

    }

    @Override
    public void run(){

        Bitmap bitmap = imageScale(bp,320,320);
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        IValue input = IValue.from(inputTensor);
        long moduleStart = System.currentTimeMillis();
        IValue[] outputs = module.forward(input).toTuple();
        long moduleEnd = System.currentTimeMillis();

        System.out.println("模型处理时间为："+ (moduleEnd - moduleStart) + "ms");


        Tensor tensor = outputs[0].toTensor();

        float[] tensorFloatArray = tensor.getDataAsFloatArray();
        float min = maxAndMin(tensorFloatArray)[0];
        float max = maxAndMin(tensorFloatArray)[1];
        System.out.println("min: " + min + "max: " + max);
        Mat dst = getNormPred2Mat(tensorFloatArray,max,min);
        dst.convertTo(dst, CvType.CV_8UC1);
        Imgproc.resize(dst,dst,new Size(bitmap.getWidth(),bitmap.getHeight()));

        matList.add(position,dst);
    }
}
