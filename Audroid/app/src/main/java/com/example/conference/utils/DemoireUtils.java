package com.example.conference.utils;

import android.content.Context;
import android.graphics.Bitmap;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.IOException;

import static com.example.conference.utils.Utils.assetFilePath;

/**
 * Utility class for removing moiré patterns from document images using
 * the UniDemoiré ESDNet model (exported as PyTorch Mobile .ptl).
 *
 * The .ptl model has built-in auto-padding (pads to 32× multiple internally),
 * so it accepts any input resolution. Large images (>4M pixels) are
 * downscaled automatically to prevent OOM.
 */
public class DemoireUtils {

    private static Module module = null;

    // 最大像素数阈值，超过时等比缩小以防 OOM
    private static final long MAX_PIXELS = 4_000_000L;  // ≈ 2000×2000

    // UniDemoiré 数据加载器使用 ToTensor()（[0,1] 范围），不使用 ImageNet 归一化
    private static final float[] MEAN = {0.0f, 0.0f, 0.0f};
    private static final float[] STD  = {1.0f, 1.0f, 1.0f};

    /** 从 assets 加载去摩尔纹模型（如果尚未加载）。 */
    public static void loadModule(Context ctx) throws IOException {
        if (module == null) {
            module = Module.load(assetFilePath(ctx, "demoire_esdnet_fhdmi.ptl"));
        }
    }

    /** 获取模块是否已加载。 */
    public static boolean isLoaded() {
        return module != null;
    }

    /**
     * 对一张 ARGB_8888 bitmap 做去摩尔纹处理。
     * 返回新的 bitmap，尺寸与输入相同。
     *
     * @param src 输入位图（不会被修改）
     * @return 去摩尔纹后的位图
     */
    public static Bitmap removeMoire(Bitmap src) {
        int W = src.getWidth();
        int H = src.getHeight();

        // 1) 超大图等比缩放以控制内存
        if ((long) W * H > MAX_PIXELS) {
            float scale = (float) Math.sqrt((double) MAX_PIXELS / (W * H));
            W = Math.round(W * scale);
            H = Math.round(H * scale);
            src = Bitmap.createScaledBitmap(src, W, H, true);
        }

        // 2) bitmap -> float32 tensor [0,1]（模型内置自动填充到 32 倍数）
        Tensor inTensor = TensorImageUtils.bitmapToFloat32Tensor(src, MEAN, STD);

        // 3) 推理
        Tensor out = module.forward(IValue.from(inTensor)).toTensor();

        // 4) tensor (1,3,H,W) -> ARGB bitmap
        float[] data = out.getDataAsFloatArray();
        int N = W * H;
        int[] pixels = new int[N];
        for (int i = 0; i < N; i++) {
            int r = clamp255(data[i] * 255f);
            int g = clamp255(data[N + i] * 255f);
            int b = clamp255(data[2 * N + i] * 255f);
            pixels[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
        }
        Bitmap outBmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        outBmp.setPixels(pixels, 0, W, 0, 0, W, H);

        return outBmp;
    }

    /** 将浮点值四舍五入到 [0, 255] 范围内的整型。 */
    private static int clamp255(float v) {
        int x = Math.round(v);
        if (x < 0) return 0;
        if (x > 255) return 255;
        return x;
    }
}
