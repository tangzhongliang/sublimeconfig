/*
 *  Copyright (C) 2015 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.cheetahutil.preview;

import android.graphics.Bitmap;

/**
 * 画像関係のユーティリティ。
 */
public class PreviewImageUtils {
    /** 平均画素法 */
    public static final int SCALE_AREA_AVARAGING = 1;

    /**
     * 画像のサイズ変更
     * @param src
     * @param dstWidth
     * @param dstHeight
     * @param rect
     * @param flag
     * @return サイズ変更後のビットマップ
     */
    public static Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, int flag) {
        if (src.getWidth() == dstWidth && src.getHeight() == dstHeight) {
            return src;
        }
        Bitmap bitmap = Bitmap.createBitmap(dstWidth, dstHeight, src.getConfig());
        copyScaledBitmapAreaAveraging(bitmap, src);
        return bitmap;
    }

    /** 平均画素法による画像縮小 */
    private static void copyScaledBitmapAreaAveraging(Bitmap dst, Bitmap src) {
        final int dstWidth = dst.getWidth();
        final int dstHeight = dst.getHeight();
        final int srcWidth = src.getWidth();
        final int srcHeight = src.getHeight();
        final int[] dstPixels = new int[dstWidth];
        final int[] srcPixels = new int[srcWidth * (srcHeight / dstHeight + 1)];

        int sy = 0;                                             // 平均化するソース画像の左上座標
        for (int dy = 0; dy < dstHeight; dy++) {
            final int ny = (dy + 1) * srcHeight / dstHeight;    // 平均化するソース画像の右下座標 + 1
            final int sh = ny - sy;                             // 平均化するソース画像の高さ
            src.getPixels(srcPixels, 0, srcWidth, 0, sy, srcWidth, sh);

            int sx = 0;                                         // 平均化するソース画像の左上座標
            for (int dx = 0; dx < dstWidth; dx++) {
                final int nx = (dx + 1) * srcWidth / dstWidth;  // 平均化するソース画像の右下座標 + 1
                final int sw = nx - sx;                         // 平均化するソース画像の幅

                int r = 0;
                int g = 0;
                int b = 0;
                int offset = 0;
                for (int y = sy; y < ny; y++) {                 // 平均化する画像の幅高さでループ
                    for (int x = sx + offset; x < nx + offset; x++) {
                        r += (srcPixels[x] & 0xFF0000);
                        g += (srcPixels[x] & 0xFF00);
                        b += (srcPixels[x] & 0xFF);
                    }
                    offset += srcWidth;
                }
                
                r = (r >> 16) / (sw * sh);
                g = (g >> 8) / (sw * sh);
                b = b  / (sw * sh);
                
                dstPixels[dx] = (0xFF << 24) | (r << 16) | (g << 8) | b;
                sx = nx;
            }
            dst.setPixels(dstPixels, 0, dstWidth, 0, dy, dstWidth, 1);
            sy = ny;
        }
    }
}
