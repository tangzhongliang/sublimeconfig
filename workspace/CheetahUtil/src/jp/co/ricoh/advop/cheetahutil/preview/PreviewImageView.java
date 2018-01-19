/*
 *  Copyright (C) 2014 RICOH Co.,LTD.
 *  All rights reserved.
 */
package jp.co.ricoh.advop.cheetahutil.preview;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import jp.co.ricoh.advop.cheetahutil.preview.PreviewImageUtils;
import jp.co.ricoh.advop.cheetahutil.util.LogC;

/**
 * プレビュー画面のImageViewを提供する。<BR>
 * 本クラスはProgramActivityから呼び出される。<BR>
 * このクラスは以下の機能を提供する。<BR>
 * ・プレビューの開始<BR>
 * ・プレビューの終了<BR>
 * ・プレビュー画像のズームイン<BR>
 * ・プレビュー画像ズームアウト<BR>
 * ・プレビュー画像の位置変更<BR>
 * ・フリックによるページの切り替え<BR>
 */
public class PreviewImageView extends ImageView {
    /** ログ出力用タグ情報 */
    private static final String TAG = "SS:WG:PIV";

    // ===============================================================
    // 定数定義
    // ===============================================================
    /** プレビューモードの列挙型を表す。 */
    private enum Mode {
        /** サムネイルモード */
        THUMBNAIL,
        /** プレビューモード アイドル状態 */
        PREVIEW_IDLE,
        /** プレビュードラッグモード */
        PREVIEW_DGAG,
        /** プレビューズームモード */
        PREVIEW_ZOOM,
    }

    /** Matrixオブジェクトが保有しているデータの数 */
    private static final int MATRIX_VALUES_NUM = 9;
    /** 外部(ボタン等)から拡大指示されたときの倍率 */
    private static final float SCALEFACTOR_ZOOMIN = 1.25f;
    /** 外部(ボタン等)から縮小指示されたときの倍率 */
    private static final float SCALEFACTOR_ZOOMOUT = (1/SCALEFACTOR_ZOOMIN);

    // ===============================================================
    // メンバー変数
    // ===============================================================
    /** シングルタッチイベントのDetector */
    private GestureDetector mGestureDetector;
    /** シングルタッチジェスチャー操作のリスナー */
    private SingleGestureListener mSingleGestureListener;
    /** マルチタッチイベントのDetector */
    private ScaleGestureDetector mScaleGestureDetector;
    /** マルチタッチジェスチャー操作のリスナー */
    private MultiGestureListener mMultiGestureListener;

    /** 動作モード */
    private Mode mMode = Mode.THUMBNAIL;

    /** 変倍前のBitmap */
    private Bitmap mBitmap;
    /** 表示用に変倍済みのBitmap */
    private Bitmap mScaledBitmap;
    /** 表示中のBitmapの高さ */
    private int mBitmapHeight;
    /** 表示中のBitmapの幅   */
    private int mBitmapWidth;

    /** ビットマップに対する初期変倍率 */
    private float mDefaultScale = 0.0f;

    /** 下限変倍率 */
    private float mMinScale = 0.0f;

    /** 上限変倍率 */
    private float mMaxScale = 0.0f;

    /** グラフィックマトリクス */
    private Matrix mImageMatrix = new Matrix();

    /** スレッド処理用ハンドラ  */
    private Handler mHandler = new Handler();

    /** オーバースクロール(左右にフリック・ドラッグしたときに、領域以上にスクロールできる幅) */
    private static final int OVERSCROLL_OFFSET_X = 78;
    /** オーバースクロール(上下にフリック・ドラッグしたときに、領域以上にスクロールできる高さ) */
    private int OVERSCROLL_OFFSET_Y = 58; // オーバースクロール(上下にフリック・ドラッグしたときに、領域以上にスクロールできる高さ)
    /** ビュー幅 */
    private static final int VIEW_WIDTH = 1024;
    /** ビュー高さ */
    private static int VIEW_HEIGHT = 548;//465;
    /** プレート部の幅 */
    private static final int PLATE_WIDTH = 261;
    /** シャドウ幅 */
    private static final int SHADOW_WH = 7;
    
    //慣性による移動スピード
    /** 慣性動作を有効にするかどうか */
    private static final boolean mIsInertialMoveAvailable = true;
    /**  慣性動作アニメーションのインターバル(msec)。あまり細かくしすぎると負荷が大きい*/
    private static final int INERTIAL_INTERVAL = 20;
    /** インターバルごとの慣性動作スピードの減衰率。1に近づけるほどつるつるになる。最大1(無重力状態) */
    private static final float INERTIAL_SPEED_DEC_RATIO = (float) 0.90;
    /** 現在の慣性動作スピード */
    private PointF mInertialSpeed = new PointF();
    /** 表示位置を調整するための左余白 */
    private int mLeftMargin;
    /** 表示位置を調整するための上余白 */
    private int mTopMargin;
    /** オリジナル拡大率 */
    private int mOriginalScale;

    /** スレッド実行フラグ */
    private boolean mRunning = false;

    private boolean mIdCardScan = false;
    // ===============================================================
    // Constructor
    // ===============================================================
    /**
     * プレビュー画面のImageViewを構築する。<BR>
     * @param context コンテキスト
     */
    public PreviewImageView(Context context) {
        super(context);
        init(context);
    }

    /**
     * プレビュー画面のImageViewを構築する。<BR>
     * @param context コンテキスト
     * @param attrs アトリビュート
     */
    public PreviewImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * コンストラクタ<BR>
     * @param context Context
     * @param attrs AttributeSet
     * @param defStyle スタイル
     */
    public PreviewImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * プレビュー画面のImageViewを初期化するメソッド。<BR>
     * @param context コンテキスト
     */
    private void init(Context context) {
        mSingleGestureListener = new SingleGestureListener();
        mGestureDetector = new GestureDetector(getContext(), mSingleGestureListener);
        mGestureDetector.setIsLongpressEnabled(false);
        mMultiGestureListener = new MultiGestureListener();
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), mMultiGestureListener);
        mIdCardScan = false;
        OVERSCROLL_OFFSET_Y = 58;
    }


    // ===============================================================
    // Previewの開始、終了
    // ===============================================================
    /**
     * プレビューを開始する。<BR>
     * @param original_scale オリジナル拡大率
     * @param bitmap ビットマップ画像
     */
    public synchronized void startPreview(final int original_scale, final Bitmap bitmap){
        if(mMode == Mode.THUMBNAIL || mBitmap == null){
            mBitmapHeight = bitmap.getHeight();
            mBitmapWidth = bitmap.getWidth();
            actInitPreviewMode(original_scale);

            setImageBitmap(bitmap);
            actSetMode(Mode.PREVIEW_IDLE);  //setImageBitmapよりも後

            // 描画処理
            actUpdateImage(true);

            if(mIsInertialMoveAvailable){
                if(false == mRunning){
                    mRunning = true;
                    new Thread(new Runnable(){
                        @Override
                        public void run(){
                            InertialMoveProc();
                        }
                    }).start();
                }
            }
        }
    }

    /**
     * プレビューモードの初期化を行なう。
     * @param original_scale オリジナル拡大率
     */
    private void actInitPreviewMode(final int original_scale) {
        // -------------------------------------
        // 縮小率を算出する
        // -------------------------------------
        // 最小表示領域
        final float minWidth = 606.0f;
        final float minHeight = 430.0f;

        // 小さい画像の場合
        if (mBitmapWidth < minWidth && mBitmapHeight < minHeight) {
            mDefaultScale = 1.0f;
        }
        // 縦長画像の場合
        else if (mBitmapHeight > mBitmapWidth) {
            mDefaultScale = minHeight / mBitmapHeight;
        }
        // 横長画像の場合
        else {
            mDefaultScale = Math.min(minWidth / mBitmapWidth, minHeight / mBitmapHeight);
        }

        // 下限変倍率(デフォルトのサイズよりも小さくはさせない)
        mMinScale = mDefaultScale;

        // 最大拡大率(デフォルトのサイズよりも小さくはさせない)
        mMaxScale = Math.max(mMinScale, 1.0f * original_scale);

       // Logger.i(TAG, "actInitPreviewMode begin scale:" + original_scale + " min:" + mMinScale + " max:" + mMaxScale);
        mOriginalScale = original_scale;

        // -------------------------------------
        // サムネイル画像の表示操作
        // -------------------------------------
        // matrix初期化
        mImageMatrix.reset();

        // 最初は画像を初期表示用に縮小する
        mImageMatrix.postScale(mDefaultScale, mDefaultScale);

        actMoveCenter(true,true,true);

        mInertialSpeed.x = 0;
        mInertialSpeed.y = 0;
    }

    /**
     * プレビューを終了する。
     */
    public synchronized void stopPreview(){
        if(mMode != Mode.THUMBNAIL){
            actSetMode(Mode.THUMBNAIL);
        }
    }

    /**
     * Bitmapを設定する。<BR>
     * @param bm 設定するBitmap
     */
    @Override
    public void setImageBitmap(Bitmap bm) {
        //Log.i(TAG, "setImageBitmap ");
        super.setImageBitmap(bm);
        mBitmap = bm;
        mScaledBitmap = null;
        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();
    }

    /**
     * Drawableを設定する。<BR>
     * @param drawable 設定するDrawable
     */
    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable == null) {
            //Log.i(TAG, "setImageDrawable null");
            mBitmap = null;
            mScaledBitmap = null;
            // BitmapのrecycleはActivityで行うのでここでは行わない
        }
    }

    // ===============================================================
    // タッチイベントの処理
    // ===============================================================
    /**
     * タッチイベントが通知された時に呼ばれる。<BR>
     * @param motionevent タッチ時のイベント
     * @return 結果(常にtrue)
     */
    @Override
    public boolean onTouchEvent(MotionEvent originalMotionevent) {
        boolean gesRet = false;
        MotionEvent motionevent = MotionEvent.obtain(originalMotionevent);
        motionevent.offsetLocation(this.getX(), this.getY());
        final int action = motionevent.getAction() & MotionEvent.ACTION_MASK;
        //マルチタッチイベントを処理する
        final boolean isInProgress = mScaleGestureDetector.isInProgress();
        //Log.i(TAG, "onTouch begin mMode:" + mMode + " action:" + action + " pointernum:" + motionevent.getPointerCount());
        if(isInProgress && mMode == Mode.PREVIEW_ZOOM && motionevent.getPointerCount() <= 1){
            motionevent.setAction(MotionEvent.ACTION_CANCEL);
            mScaleGestureDetector.onTouchEvent(motionevent);
            return true;
        }
        mScaleGestureDetector.onTouchEvent(motionevent);
        if (isInProgress || mScaleGestureDetector.isInProgress()) {
            //Log.i(TAG, "onTouch isInProgresse:" + isInProgress + " mScaleGestureDetector.isInProgress():" + mScaleGestureDetector.isInProgress());
            return true;
        }
        if(motionevent.getPointerCount() == 1){
            //シングルタッチイベントを処理する
            gesRet = mGestureDetector.onTouchEvent(motionevent);
        }
        if (action ==  MotionEvent.ACTION_UP) {
            if(mMode == Mode.PREVIEW_DGAG){
                actSetMode(Mode.PREVIEW_IDLE);
            }
        }
        motionevent.recycle();
        return gesRet;
    }

    /**
     * プレビュータッチイベントを提供する。<BR>
     */
    private class SingleGestureListener extends SimpleOnGestureListener {
        /** 次のページへのフリックが可能かどうか */
        boolean mCanFlingPageChangeNext = false;
        /** 前のページへの不リックが可能かどうか */
        boolean mCanFlingPageChangePrev = false;
        /** x軸の最後にタッチした位置 */
        float lastX;
        /** y軸の最後にタッチした位置 */
        float lastY;

        /**
         * タップされた時に呼ばれる。<BR>
         * 他のMotionEventよりも先行して呼び出される。<BR>
         * @param e タップ動作のイベント
         * @return 結果(常にtrue)
         */
        @Override
        public boolean onDown(MotionEvent e) {
            // 画像エリアをタッチしたタイミングで、それまでの慣性動作をリセット(強制停止)
            mInertialSpeed.set(0, 0);
            if(mMode != Mode.THUMBNAIL){
                //最初にキーダウンした時の画像位置で、フリックによるページめくりが可能かどうかが変わる
                mCanFlingPageChangeNext = actCanFlingAsPageChange(true);
                mCanFlingPageChangePrev = actCanFlingAsPageChange(false);
            }
            //絵が表示されたところ以外(グレーのエリア)に対するキータッチは無効
            lastX = e.getRawX();
            lastY = e.getRawY();
            return actIsPointerOnImage(e.getRawX(), e.getRawY());
        }

        /**
         * スクロールイベントが通知された時に呼ばれる。<BR>
         * @param e1 スクロール開始した初回のMotionEvent
         * @param e2 カレントのスクロールを引き起こしたMotionEvent
         * @param distanceX x軸に沿った毎秒ピクセルの中で測定された速度
         * @param distanceY y軸に沿った毎秒ピクセルの中で測定された速度
         * @return 必ずtrueが返る
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e1.getPointerId(0) == e2.getPointerId(0) && (mMode == Mode.PREVIEW_IDLE || mMode == Mode.PREVIEW_DGAG)) {
                //Log.i(TAG, "onScroll begin x:"+ -distanceX + " y:" + -distanceY);
                actSetMode(Mode.PREVIEW_DGAG);
                float x = e2.getRawX();
                float y = e2.getRawY();
                mImageMatrix.postTranslate(x - lastX, y - lastY);
                lastX = x;
                lastY = y;
                actUpdateImage(false);
            }
            return true;
        }

        /**
         * フリックイベントが通知された時に呼ばれる。<BR>
         * 計算された速度は、毎秒ピクセル中のxおよびy軸に沿って提供される。<BR>
         * @param e1 フリック開始した初回のMotionEvent
         * @param e2 カレントのフリックを引き起こしたMotionEvent
         * @param velocityX x軸に沿った毎秒ピクセルの中で測定された速度
         * @param velocityY y軸に沿った毎秒ピクセルの中で測定された速度
         * @return 必ずtrueが返る
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Fling方向
            boolean is_direction_for_nextpage = (velocityX < 0) ? true : false;
            if (e1.getPointerId(0) == e2.getPointerId(0) && (mMode == Mode.PREVIEW_IDLE || mMode == Mode.PREVIEW_DGAG)) {
                boolean canFlingPageChange = (is_direction_for_nextpage) ? mCanFlingPageChangeNext : mCanFlingPageChangePrev;
                if (canFlingPageChange && actCanFlingAsPageChange(is_direction_for_nextpage) && Math.abs(velocityX) > Math.abs(velocityY)) {
                    if(mPreviewImageListener != null){
                        // フリックを次ページ遷移に作用させたいのでリスナに通知する
                        mPreviewImageListener.notifyFlingOccurredAsPageChange(is_direction_for_nextpage);
                    }
                } else {
                    mInertialSpeed.set(velocityX / 20, velocityY / 20); // velocityの値が大きすぎて、そのままだとフリックしたときの慣性動作がまるでワープなので、適当に間引く
                }
            } else if (mMode == Mode.THUMBNAIL) {
                if(Math.abs(velocityX) > Math.abs(velocityY) ){
                    // サムネイルモードの場合は基本的に無条件で前後ページに遷移する
                    mPreviewImageListener.notifyFlingOccurredAsPageChange(is_direction_for_nextpage);
                }
            }
            return true;
        }

        /**
         * ダブルタップされた時に呼ばれる。<BR>
         * @param e ダブルタップ動作のイベント
         * @return 必ずtrueが返る
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mMode == Mode.PREVIEW_IDLE) {
                float cur_scale = actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix);
                if (actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix) <= mMinScale) {
                    float new_scale = mMaxScale / cur_scale;
                    mImageMatrix.postScale(new_scale, new_scale, e.getX(), e.getY());
                    cur_scale = actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix);
                    if(mMaxScale / cur_scale < 1.01 && mMaxScale / cur_scale > 1){
                        actSetValueToMatrix(Matrix.MSCALE_X, mMaxScale, mImageMatrix);
                        actSetValueToMatrix(Matrix.MSCALE_Y, mMaxScale, mImageMatrix);
                    }
                    actMoveCenter(true,true,true);
                    actUpdateImage(false);
                } else {
                    actSetValueToMatrix(Matrix.MSCALE_X, mMinScale, mImageMatrix);
                    actSetValueToMatrix(Matrix.MSCALE_Y, mMinScale, mImageMatrix);
                    actMoveCenter(true,true,true);
                    actUpdateImage(false);
                }
            }
            return true;
        }

        /**
         * ダブルタップイベントが通知された時に呼ばれる。<BR>
         * 特に処理は行わない。<BR>
         * @param e ダブルタップ動作のイベント
         * @return 必ずtrueが返る
         */
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }

        /**
         * 長押しされた時に呼ばれる。<BR>
         * 特に処理は行わない。<BR>
         * @param e 長押し動作のイベント
         */
        @Override
        public void onLongPress(MotionEvent e) {
        }

        /**
         * 押下時に呼ばれる。<BR>
         * 特に処理は行わない。<BR>
         * @param e 押下動作のイベント
         */
        @Override
        public void onShowPress(MotionEvent e) {
        }

        /**
         * シングルタップ時に呼ばれる。<BR>
         * ダブルタップ時は呼ばれない。<BR>
         * 特に処理は行わない。<BR>
         * @param e シングルタップ動作のイベント
         * @return 必ずtrueが返る
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return true;
        }

        /**
         * シングルタップ時に呼ばれる。<BR>
         * ダブルタップ時も呼ばれる。<BR>
         * 特に処理は行わない。<BR>
         * @param e シングルタップ時のイベント
         * @return 必ずtrueが返る
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

    }

    /**
     * プレビュータッチイベントを提供する。<BR>
     */
    private class MultiGestureListener extends SimpleOnScaleGestureListener {

        /**
         * マルチタッチの開始を検知する。<BR>
         * @param detector モーションジェスチャー
         * @return 必ずtrueが返る
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            //Log.i(TAG, "onScaleBegin begin");
            if(mMode != Mode.THUMBNAIL){
                actSetMode(Mode.PREVIEW_ZOOM);
            }
            return true;
        }

        /**
         * ピンチ操作を検知する。<BR>
         * @param detector モーションジェスチャー
         * @return 必ずtrueが返る
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            //Log.i(TAG, "onScale begin");
            if(mMode == Mode.PREVIEW_ZOOM){

                float cur_scale = actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix);
                float scale_factor = detector.getScaleFactor();
                float new_scale = scale_factor;
                //拡大
                if(scale_factor > 1.0f){
                    new_scale = Math.min(scale_factor, mMaxScale / cur_scale);
                }
                //縮小
                else if(scale_factor < 1.0f){
                    new_scale = Math.max(scale_factor, mMinScale / cur_scale);
                }
                //Log.i(TAG, "onScale begin cur_scale:" + cur_scale + " scale_factor:" + scale_factor + " new_scale:" + new_scale);
                mImageMatrix.postScale(new_scale, new_scale, detector.getFocusX(), detector.getFocusY());
                actUpdateImage(false);
            }
            return true;
        }

        /**
         * マルチタッチの終了を検知する。<BR>
         * @param detector モーションジェスチャー
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            //Log.i(TAG, "onScaleEnd begin");
            if(mMode == Mode.PREVIEW_ZOOM){
                actSetMode(Mode.PREVIEW_IDLE);
                actUpdateImage(false);
                super.onScaleEnd(detector);
            }
        }

    }

    /**
     * プレビューイメージを更新する。<BR>
     * @param initFlag true:初回表示時、false:初回以外
     */
    private void actUpdateImage(final boolean initFlag) {
        float cur_scale = actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix);
        if(Float.isNaN(cur_scale) || Float.isInfinite(cur_scale)){
            //Logger.w(TAG, "actUpdateImage invalid float value!!!! cur_scale:" + cur_scale);
            //何らかの手違い・計算ミス等により、拡大縮小率のfloatが不正値になることがある。
            //その場合、画像そのものが表示されなくなってしまい、画像の下に表示していたぐるぐるが表示され、
            //ユーザにとって、あたかも処理中のまま止まってしまったかのようになってしまう。

            //そこで、拡大縮小率が不正の場合は、初期画像を表示することにする

            // matrix初期化
            mImageMatrix.reset();

            // 最初は画像を初期表示用に縮小する
            mImageMatrix.postScale(mDefaultScale, mDefaultScale);

            mInertialSpeed.x = 0;
            mInertialSpeed.y = 0;
        }

        if (mMode != Mode.THUMBNAIL) {
            //画像センタリング(必要なら)
            actMoveCenter(true,true,false);
            //画像の見切れ調整(必要なら)
            actFixOverScroll(true, true);

            int scaledWidth = mBitmapWidth;
            int scaledHeight = mBitmapHeight;
            if (mScaledBitmap != null) {
                scaledWidth = mScaledBitmap.getWidth();
                scaledHeight = mScaledBitmap.getHeight();
            }
            if (mMode == Mode.PREVIEW_ZOOM || null == mBitmap || mOriginalScale != 1) {
                // ピンチイン、アウト中、ページめくり中、元サイズを縮小した場合はMatrixによる変倍で表示
                float targetWidth = mBitmapWidth
                        * actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix);
                float targetHeight = mBitmapHeight
                        * actGetValueFromMatrix(Matrix.MSCALE_Y, mImageMatrix);

                // Scale ImageView
                Matrix imageMatrix = new Matrix();
                imageMatrix.postScale(targetWidth / scaledWidth, targetHeight / scaledHeight);
                actSetValueToMatrix(Matrix.MTRANS_X, SHADOW_WH, imageMatrix);
                actSetValueToMatrix(Matrix.MTRANS_Y, SHADOW_WH, imageMatrix);
                this.setImageMatrix(imageMatrix);
            } else {
                // ImageViewのmatrixでは、影の幅高さ分オフセットさせるのみ
                Matrix imageMatrix = new Matrix();
                actSetValueToMatrix(Matrix.MTRANS_X, SHADOW_WH, imageMatrix);
                actSetValueToMatrix(Matrix.MTRANS_Y, SHADOW_WH, imageMatrix);
                this.setImageMatrix(imageMatrix);

                // 変倍後の画像サイズを算出
                float values[] = new float[MATRIX_VALUES_NUM];
                mImageMatrix.getValues(values);

                final int width = (int) ((float) mBitmapWidth * values[Matrix.MSCALE_X]);
                final int height = (int) ((float) mBitmapHeight * values[Matrix.MSCALE_Y]);

                // サイズが異なっていれば画像を変倍
                if (scaledWidth != width || scaledHeight != height) {
                    mScaledBitmap = PreviewImageUtils.createScaledBitmap(mBitmap, width, height,
                            PreviewImageUtils.SCALE_AREA_AVARAGING);
                    super.setImageBitmap(mScaledBitmap);
                }
            }

            // Translate ImageView
            RelativeLayout.LayoutParams vlp = (RelativeLayout.LayoutParams)this.getLayoutParams();
            vlp.leftMargin = (int)actGetValueFromMatrix(Matrix.MTRANS_X, mImageMatrix);
            vlp.topMargin = (int)actGetValueFromMatrix(Matrix.MTRANS_Y, mImageMatrix);
            // 初回表示位置の調整
            if (initFlag) {
                if (vlp.leftMargin < 87) {
                    mLeftMargin = 87 - vlp.leftMargin;
                }
//                if (vlp.topMargin < 99) {
//                    mTopMargin = 99 - vlp.topMargin;
//                }
            }
            vlp.leftMargin += mLeftMargin;
            vlp.topMargin += mTopMargin;
            vlp.width = (int)(mBitmapWidth * actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix)) + SHADOW_WH * 2;
            LogC.d("mIdCardScan " + mIdCardScan);
            if(mIdCardScan) {
            	vlp.height = (int)(mBitmapHeight * actGetValueFromMatrix(Matrix.MSCALE_Y, mImageMatrix));// + SHADOW_WH * 2;
            } else {
            	vlp.height = (int)(mBitmapHeight * actGetValueFromMatrix(Matrix.MSCALE_Y, mImageMatrix)) + SHADOW_WH * 2;
            }
            this.setLayoutParams(vlp);

            // Redraw
            this.invalidate();
        }
        //拡大縮小可否をリスナーに通知する
        actUpdateZoomInOutAvailability();
        actUpdatePrevNextAvailability();
    }

    /**
     * 親アクティビティに対して、フリックによるImageViewの切替を許可するかどうかを判断する。<BR>
     * @param isDirectionForNextpage true:次ページへのフリックあり / false:次ページへのフリックなし
     * @return true:許可 / false:不許可
     */
    private boolean actCanFlingAsPageChange(boolean isDirectionForNextpage) {
        boolean ret = false;
        float viewWidth = VIEW_WIDTH;
        float imageWidth = (float) mBitmapWidth;
        imageWidth *= actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix);
        float currentX = actGetValueFromMatrix(Matrix.MTRANS_X, mImageMatrix);

        float scale =  actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix);

        if (isDirectionForNextpage) {
            //画像全景表示時の表示領域右端までスクロールした状態ならフリック可能 (3pxの余裕を持たせる)
            if (scale <= mDefaultScale || (imageWidth + currentX + OVERSCROLL_OFFSET_X + PLATE_WIDTH) <= viewWidth + 3) {
                ret = true;
            }
        } else {
            // 画像全景表示時の表示領域左端までスクロールした状態ならフリック可能(3pxの余裕を持たせる)
            if (scale <= mDefaultScale || currentX + 3 >= OVERSCROLL_OFFSET_X) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * 画像の見切れ調整を行なう。<BR>
     * @param fixX true:X方向調整する / false:X方向調整しない
     * @param fixY true:Y方向調整する / false:Y方向調整しない
     */
    private void actFixOverScroll(boolean fixX, boolean fixY) {
        if(fixX){
            float viewWidth = VIEW_WIDTH - PLATE_WIDTH;
            float imageWidth = (float) mBitmapWidth;
            imageWidth *= actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix);
            float currentX = actGetValueFromMatrix(Matrix.MTRANS_X, mImageMatrix);

            // 表示中の画像サイズがViewのサイズより大きい場合のみ考慮する。(小さい場合はセンタリングするので。)
            if(imageWidth > viewWidth - (2 * OVERSCROLL_OFFSET_X)){
                if (currentX > OVERSCROLL_OFFSET_X) {
                    // 画面左に余白あり
                    actSetValueToMatrix(Matrix.MTRANS_X, (float) OVERSCROLL_OFFSET_X, mImageMatrix);
                } else if ((imageWidth + currentX + OVERSCROLL_OFFSET_X) < viewWidth) {
                    // 画面右に余白あり
                    float cal = viewWidth - imageWidth - (float) OVERSCROLL_OFFSET_X;
                    actSetValueToMatrix(Matrix.MTRANS_X, cal, mImageMatrix);
                }
            }
        }
        if(fixY){
            float viewHeight = VIEW_HEIGHT; // Viewのサイズ
            float imageHeight = (float) mBitmapHeight;
            imageHeight *= actGetValueFromMatrix(Matrix.MSCALE_Y, mImageMatrix);
            float currentY = actGetValueFromMatrix(Matrix.MTRANS_Y, mImageMatrix);

            // 表示中の画像サイズがViewのサイズより大きい場合のみ考慮する。(小さい場合はセンタリングするので。)
            LogC.d("fitY OVERSCROLL_OFFSET_Y" + OVERSCROLL_OFFSET_Y);
            if(imageHeight > viewHeight - (2 * OVERSCROLL_OFFSET_Y)){
                if (currentY > OVERSCROLL_OFFSET_Y) {
                    // 画面上に余白あり
                    actSetValueToMatrix(Matrix.MTRANS_Y, (float) OVERSCROLL_OFFSET_Y, mImageMatrix);
                } else if ((imageHeight + currentY + OVERSCROLL_OFFSET_Y) < viewHeight) {
                    // 画面下に余白あり
                    float cal = viewHeight - imageHeight - (float) OVERSCROLL_OFFSET_Y;
                    actSetValueToMatrix(Matrix.MTRANS_Y, cal, mImageMatrix);
                }
            }
        }

    }

    /**
     * 画像をセンタリングする。<BR>
     * @param centerX true:X方向センタリングする / false:X方向センタリングしない
     * @param centerY true:Y方向センタリングする/ false:Y方向センタリングしない
     * @param forceCentering true:強制センタリングする / false:強制センタリングしない
     */
    private void actMoveCenter(boolean centerX, boolean centerY, boolean forceCentering) {
        if(centerX){
            final float overscroll_offset = (forceCentering) ? 0 : OVERSCROLL_OFFSET_X;
            float viewWidth = VIEW_WIDTH - PLATE_WIDTH;
            float imageWidth = (float) mBitmapWidth;
            imageWidth *= actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix);
            // 画像サイズがViewのサイズより小さい場合のみセンタリング
            float cal = viewWidth - imageWidth;
            if (cal > (2 * overscroll_offset)) {
                cal /= 2.0f;
                actSetValueToMatrix(Matrix.MTRANS_X, cal, mImageMatrix);
            }
        }
        if (centerY) {
        	LogC.d("centerY OVERSCROLL_OFFSET_Y:" + OVERSCROLL_OFFSET_Y);
            final float overscroll_offset = (forceCentering) ? 0 : OVERSCROLL_OFFSET_Y;
            float viewHeight = VIEW_HEIGHT; // Viewのサイズ
            float imageHeight = (float) mBitmapHeight;
            imageHeight *= actGetValueFromMatrix(Matrix.MSCALE_Y, mImageMatrix);
            // 画像サイズがViewのサイズより小さい場合のみセンタリング
            float cal = viewHeight - imageHeight;
            if (cal > (2 * overscroll_offset)) {
                cal /= 2.0f;
                actSetValueToMatrix(Matrix.MTRANS_Y, cal, mImageMatrix);
            }
        }
    }

    /**
     * 指定座標がイメージ上かどうかを判断する。<BR>
     * @param x X座標
     * @param y Y座標
     * @return true:イメージ上 / イメージ外
     */
    private boolean actIsPointerOnImage(float x, float y){
        boolean ret = false;

        float imageWidth = (float) mBitmapWidth;
        imageWidth *= actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix);
        float currentX = actGetValueFromMatrix(Matrix.MTRANS_X, mImageMatrix);
        float imageHeight = (float) mBitmapHeight;
        imageHeight *= actGetValueFromMatrix(Matrix.MSCALE_Y, mImageMatrix);
        float currentY = actGetValueFromMatrix(Matrix.MTRANS_Y, mImageMatrix);

        ret = (x >= currentX && x <= currentX + imageWidth && y >= currentY && y <= currentY + imageHeight);

        return ret;
    }

    /**
     * Matrixオブジェクトに値を設定する。<BR>
     * @param index インデックス
     * @param value 設定値
     * @param dst Matrix
     */
    private void actSetValueToMatrix(int index, float value, Matrix dst) {

        float[] values = new float[MATRIX_VALUES_NUM];
        dst.getValues(values);

        values[index] = value;
        dst.setValues(values);
    }

    /**
     * Matrixオブジェクトから値を取得する。<BR>
     * @param index インデックス
     * @param dst Matrix
     */
    private float actGetValueFromMatrix(int index, Matrix dst) {

        float[] values = new float[MATRIX_VALUES_NUM];
        dst.getValues(values);

        return values[index];
    }

    /**
     * モードを設定する。<BR>
     * @param mode モード
     */
    private void actSetMode(Mode mode){
        if(mMode != mode){
            //Log.i(TAG, "actSetMode() " + mMode + " > " + mode);
        }
        this.mMode = mode;
        //拡大縮小可否をリスナーに通知する
        actUpdateZoomInOutAvailability();
        actUpdatePrevNextAvailability();
    }

    // -------------------------------------
    // 拡大縮小キー用
    // -------------------------------------
    /**
     * ズームインを行う。<BR>
     */
    public void zoomIn(){
        float cur_scale = actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix);
        float new_scale = Math.min(SCALEFACTOR_ZOOMIN, mMaxScale / cur_scale);

        //Log.i(TAG, "zoomIn() cur_scale:" + cur_scale + " new_scale:" + new_scale * cur_scale);

        mImageMatrix.postScale(new_scale, new_scale, ((float) super.getWidth()) / 2, ((float) super.getHeight()) / 2);

        //拡大した結果、最大スケールとの差異が1%未満になった場合、最大スケールとする
        //floatの乗除を行うため、例えばX * 5/4 = Yとした後に、Y * 4/5 = X'としたときに、XとX'が厳密に==とならないため。
        cur_scale = actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix);
        if(mMaxScale / cur_scale < 1.01 && mMaxScale / cur_scale > 1){
            actSetValueToMatrix(Matrix.MSCALE_X, mMaxScale, mImageMatrix);
            actSetValueToMatrix(Matrix.MSCALE_Y, mMaxScale, mImageMatrix);
        }
        actMoveCenter(true,true,true);
        actUpdateImage(false);
    }

    /**
     * ズームアウトを行う。<BR>
     */
    public void zoomOut(){
        float cur_scale = actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix);
        float new_scale = Math.max(SCALEFACTOR_ZOOMOUT, mMinScale / cur_scale);

        //Log.i(TAG, "zoomOut() cur_scale:" + cur_scale + " new_scale:" + new_scale * cur_scale);

        mImageMatrix.postScale(new_scale, new_scale, ((float) super.getWidth()) / 2, ((float) super.getHeight()) / 2);

        //縮小した結果、最小スケールとの差異が1%未満になった場合、最小スケールとする
        //floatの乗除を行うため、例えばX * 5/4 = Yとした後に、Y * 4/5 = X'としたときに、XとX'が厳密に==とならないため。
        cur_scale = actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix);
        if(cur_scale / mMinScale < 1.01 && cur_scale / mMinScale > 1){
            actSetValueToMatrix(Matrix.MSCALE_X, mMinScale, mImageMatrix);
            actSetValueToMatrix(Matrix.MSCALE_Y, mMinScale, mImageMatrix);
        }
        actMoveCenter(true,true,true);
        actUpdateImage(false);
    }

    /**
     * 拡大縮小可否をリスナーに通知する。<BR>
     */
    private void actUpdateZoomInOutAvailability(){
        if(mPreviewImageListener == null){
            return;
        }

        switch(mMode){
        case PREVIEW_IDLE:{
            boolean zoomin = false, zoomout = false;
            float scale =  actGetValueFromMatrix(Matrix.MSCALE_X, mImageMatrix);
            if(scale < mMaxScale){
                zoomin = true;
            }
            if(scale > mMinScale){
                zoomout = true;
            }
            mPreviewImageListener.notifyZoomInOutAvailability(this, zoomin, zoomout);
        }
            break;
        default:
        case THUMBNAIL:     //サムネイルモード中の画像は外部からのズームインアウトはさせない
            mPreviewImageListener.notifyZoomInOutAvailability(this, false, false);
            break;
        }
    }

    /**
     * ページ移動可否をリスナーに通知する。<BR>
     */
    private void actUpdatePrevNextAvailability(){
        if(mPreviewImageListener == null){
            return;
        }

        switch(mMode){
        case PREVIEW_IDLE:
        case PREVIEW_DGAG:
        case PREVIEW_ZOOM:
        {
            boolean prev = false, next = false;
            prev = actCanFlingAsPageChange(false);
            next = actCanFlingAsPageChange(true);
            mPreviewImageListener.notifyPrevNextAvailability(this, prev, next);
        }
            break;
        default:
        case THUMBNAIL:     //サムネイルモード中は常時矢印表示
            mPreviewImageListener.notifyPrevNextAvailability(this, true, true);
            break;
        }
    }

    /**
     * リスナー(フリック発生などを通知したいリスナがいるときに用いる)。<BR>
     */
    private PreviewImageListener mPreviewImageListener = null;

    /**
     * プレビュー画面の操作を受け取るときに、実装して使用する。<BR>
     */
    public interface PreviewImageListener {
        /**
         * ズームイン・ズームアウトキーの有効化/無効化を通知する。<BR>
         * @param obj プレビュー画面のImageView
         * @param isZoomInAvailable ズームインキーを表示するかどうか(true:表示する、false:表示しない)
         * @param isZoomOutAvailable ズームアウトキーを表示するかどうか(true:表示する、false:表示しない)
         */
        public void notifyZoomInOutAvailability(PreviewImageView obj, boolean isZoomInAvailable, boolean isZoomOutAvailable);
        /**
         * ページめくりキーの有効化/無効化を通知する。<BR>
         * @param obj プレビュー画面のImageView
         * @param isPrevAvailable ページめくり(前)キーを表示するかどうか(true:表示する、false:表示しない)
         * @param isNextAvailable ページめくり(後)キーを表示するかどうか(true:表示する、false:表示しない)
         */
        public void notifyPrevNextAvailability(PreviewImageView obj, boolean isPrevAvailable, boolean isNextAvailable);
        /**
         * フリングによるページ切り替えを通知する。<BR>
         * @param isDirectionForNextpage 次のページへ遷移するかどうか(true:遷移する、false:遷移しない)
         */
        public void notifyFlingOccurredAsPageChange(boolean isDirectionForNextpage);
    }

    /**
     * リスナーを設定する。<BR>
     *
     * @param listener PreviewImageListener
     */
    public void setPreviewImageListener(PreviewImageListener listener) {
        this.mPreviewImageListener = listener;
    }


    /**
     * 慣性動作スレッドを停止させる。<BR>
     */
    public void stopRunning(){
        mRunning = false;
    }

    /**
     * 慣性動作を実施する。<BR>
     */
    private synchronized void InertialMoveProc(){
        //Logger.d(TAG, "InertialMoveProc");
        while (PreviewImageView.this.isShown() && mRunning && mIsInertialMoveAvailable) {
            actUpdateInertialMovement();
            try {
                wait(INERTIAL_INTERVAL);
                continue;
            } catch (InterruptedException e) {
                //Logger.w(TAG, "" + e);
            }
        }
        mRunning = false;
    }

    /**
     * 慣性動作を更新する。
     */
    private void actUpdateInertialMovement() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMode == Mode.PREVIEW_IDLE) {
                    if(mInertialSpeed.x != 0 || mInertialSpeed.y != 0){
                        // 移動、回転の慣性反映
                        mImageMatrix.postTranslate(mInertialSpeed.x, mInertialSpeed.y);

                        // 以下、次回の慣性のための移動スピード、回転スピード軽減処理

                        // 移動による慣性
                        mInertialSpeed.set(mInertialSpeed.x * INERTIAL_SPEED_DEC_RATIO, mInertialSpeed.y * INERTIAL_SPEED_DEC_RATIO);
                        // 慣性の移動量が十分に小さくなったときは停止
                        if (Math.abs(mInertialSpeed.x) < 1) {
                            mInertialSpeed.x = 0;
                        }
                        if (Math.abs(mInertialSpeed.y) < 1) {
                            mInertialSpeed.y = 0;
                        }
                        actUpdateImage(false);
                    }
                }
            }
        });
    }
    
    public void setOverScrollOffsetY(int offset) {
    	OVERSCROLL_OFFSET_Y = offset;
    }
    
    public void enableIdCardScan() {
    	mIdCardScan = true;
    	VIEW_HEIGHT = 465;
    }
}
