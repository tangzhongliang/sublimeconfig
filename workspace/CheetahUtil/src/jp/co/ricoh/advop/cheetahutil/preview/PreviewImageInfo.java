/*
 *  Copyright (C) 2014 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.cheetahutil.preview;

import android.graphics.Bitmap;

/**
 * プレビューのイメージを表す。<BR>
 */
public class PreviewImageInfo{
    /** プレビューのビットマップ */
    public Bitmap bitmapPreview;
    /** プレビューイメージのスケール */
    public int scale;   //元画像を 1/Nしたことを示すパラメータ。例：２のとき、縦横1/2に縮めたものをbitmap_previewに格納したことを示している
    /** コンテントの種類 */
    public String contentType;
    /** プレビューイメージの回転角度 */
    public int rotate;
}
