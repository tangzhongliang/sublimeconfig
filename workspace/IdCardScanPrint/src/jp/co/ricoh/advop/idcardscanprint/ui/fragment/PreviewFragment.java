package jp.co.ricoh.advop.idcardscanprint.ui.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import jp.co.ricoh.advop.cheetahutil.preview.PreviewImageInfo;
import jp.co.ricoh.advop.cheetahutil.preview.PreviewImageView;
import jp.co.ricoh.advop.cheetahutil.preview.PreviewImageView.PreviewImageListener;

import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.util.HDDUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.widget.MultiButtonDialog;
import jp.co.ricoh.advop.cheetahutil.widget.ScanProcessingFragment;
import jp.co.ricoh.advop.cheetahutil.widget.TitleButtonDialog;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.application.resource.common.ImageResource;
import jp.co.ricoh.advop.idcardscanprint.application.resource.common.KeyAvailability;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.EXE_RESULT;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.ExeCallback;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.model.SettingItemData;
import jp.co.ricoh.advop.idcardscanprint.print.PrintManager.PrintJobContinueCB;
import jp.co.ricoh.advop.idcardscanprint.scan.ScanStateMachine.ScanEvent;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrintColor;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.IdCardScanPrintActivity;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.InitialSettingPrintActivity;
import jp.co.ricoh.advop.idcardscanprint.util.Const;
import jp.co.ricoh.advop.idcardscanprint.util.Util;
import jp.co.ricoh.advop.cheetahutil.util.CUtil;

public class PreviewFragment extends Fragment {
    /** ログ出力用タグ情報 */
    private static final String TAG = "SS:PV:Rrvw";
    /** 画像保持枚数 */
    private static final int IMAGE_MEMORY_AMOUNT = 0;
    /** ジョブIDのインデックス */
   // private static final String ARGS_JOBID = "JOBID";
    /** スキャンページ数のインデックス */
   //private static final String ARGS_SCAN_PAGES = "SCANPAGES";

    /** スタートキーのイメージ */
    private ImageView mStartKeyImage;
    /** スタートキーのふわふわエフェクト画像 */
    private ImageView mImageStartKeyEffect;
    /** スタートキーのふわふわアニメーション */
    private Animation mStartKeyAnim;
    /** キャンセルキー */
   // private FrameLayout mCancelKey;
    /** 現ページ数用View */
   // private TextView mCurPageView;
    /** Totalページ数用View */
    //private TextView mTotalPageView;
    /** ズームインキー */
    private FrameLayout mZoomInKey;
    /** ズームアウトキー */
    private FrameLayout mZoomOutKey;
    /** ページめくり（前）キー */
    private FrameLayout mPrevKey;
    /** ページめくり（次）キー */
    private FrameLayout mNextKey;

    /** プレビューページのリスト */
    List<PageData> mPageList;
    private SettingItemData colorData;
    
    private TextView txtPrintColor;
    private ImageView imgPrintColor, imgPrintCheck;
    private TextView txtPrintCopies, txtSend;
    private FrameLayout mPrintSet, mPrintCheck, mPrintSetValue;
    
    private TextView txtBack;
    private Button btnBack;
    
    private Boolean isPrint = false;
    ScanProcessingFragment mSendDialog;
    /**
     * プレビューページを表す。<BR>
     */
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_preview, container, false); 
//    }

   
    private class PageData {
        /** ビューのレイアウト */
        RelativeLayout viewContainer;
        /** ビューの画像 */
        PreviewImageView viewImage;
        /** ビューのプログレス情報 */
        ProgressBar viewProgress;
        /** プレビュー画像のページ番号 */
        int pageNo;
        /** 画像そのものの情報（サイズやrotateなど) */
        PreviewImageInfo imageInfo = new PreviewImageInfo();    //画像そのものの情報(サイズやrotateなど。(※rotateは画像取得時に既に画像に反映済なので、特にここでは気にしない))
    }

    /** 拡大キーの輝度半輝度判定用 */
    boolean mIsZoomInFuncOk = false;    //拡大可能か
    /** 縮小キーの輝度半輝度判定用 */
    boolean mIsZoomOutFuncOk = false;   //縮小可能か
    /** ページめくりキー(前のページへ)の表示非表示判定用 */
    boolean mIsPrevFuncOk = false;  //前のページへ
    /** ページめくりキー(次のページへ)の表示非表示判定用 */
    boolean mIsNextFuncOk = false;  //次のページへ
    /** ジェスチャーイベントのリスナー */
    private SingleGestureListener mSingleGestureListener;
    /** ジェスチャーを検知するためのDetector */
    // プレビュー中はジェスチャーはすべてPreviewImageViewで処理するが、ぐるぐる中などImageViewが無いときでもページめくり遷移するために検知する。
    private GestureDetector mGestureDetector;

    /** 読取りページ数 */
    private int mScanPages;
    /** サムネイル操作用クラス */
    //private ScanThumbnail mThumbnail;

    /** スレッド処理受付ハンドラ */
    protected Handler handler = new Handler();

    // 表示上のページ数文字列以外はすべて0オリジンで取り扱う。 1page目は0。
    /** 現在フォーカスしている画像のページ番号(0オリジン) */
    int mCurrentFocusedPage;

    /** PreviewImageViewからの拡大率やスクロール状態を受け取るリスナー */
    MyPreviewImageListener mPreviewImageListener;

    /** ページめくりのためのViewFlipper */
    private ViewFlipper mImageFlipper;

    /** 画像取得スレッド実行フラグ */
    private boolean mImageGetRunning = false;

//    /** フラグメントのインスタンス */
//    private static PreviewImageFragment mFragment;
//
//    /**
//     * インスタンスを作成する。
//     * @return プレビューフラグメントのインスタンス
//     */
//    public static Fragment newInstance(String jobId, int validPages) {
//        mFragment = new PreviewImageFragment();
//        Bundle args = new Bundle();
//        args.putString(ARGS_JOBID, jobId);
//        args.putInt(ARGS_SCAN_PAGES, validPages);
//        mFragment.setArguments(args);
//        return mFragment;
//    }

    /**
     * FragmentのonCreate<BR>
     * @param savedInstanceState ActivityのBundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogC.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        // フェールセーフでマルチタップを無効に戻す
        //SingleTouchFrameLayout.setMultiTouchable(false);
        super.onDestroy();
    }

    /**
     * FragmentのonCreateView<BR>
     * ビューの構築を行う。<BR>
     * ビューの構築後にプレビュー情報の初期化処理を呼び出す。<BR>
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogC.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_preview, container, false);
      
        //スタートキー
        mStartKeyImage = (ImageView) rootView.findViewById(R.id.preview_img_start);
        mStartKeyImage.setImageDrawable(ImageResource.createSelector(mStartKeyImage, getResources(), R.raw.bt_start_n, R.raw.bt_start_w) );
        mStartKeyImage.setOnClickListener(new BaseOnClickListener() {
            @Override
            public void onWork(View v) {
                
                CHolder.instance().getJobData().setJobCancel(false);
                CHolder.instance().getGlobalDataManager().setCloseDlg(false);
                int memory = CHolder.instance().getScanManager().getRemainMemory();
                mSendDialog = new ScanProcessingFragment(getActivity(), ScanProcessingFragment.TYPE_WEBDAV_UPLOAD, 0, memory, 0); 
                
                mSendDialog.setRunningTitle(R.string.txid_sending);
                if(!LogC.isMT) {
                    mSendDialog.show(CHolder.instance().getActivity().getFragmentManager(), "sending");
                }
                
                mSendDialog.setStopCallback(new BaseOnClickListener() {
                    
                    @Override
                    public void onWork(View v) {
                        
                        CHolder.instance().getJobData().setJobCancel(true);
                        mSendDialog.setStopButtonEnable(false);
                        mSendDialog.setCanceled();                        
                        if(CHolder.instance().getGlobalDataManager().isCloseDlg()) {
                            mSendDialog.dismissAllowingStateLoss();
                        }
                    }
                });

//                mSendDialog.setStopCallback(new BaseOnClickListener() {
//                    @Override
//                    public void onWork(View v) {
//                        
//                        CHolder.instance().getJobData().setJobCancel(true);
//                        mSendDialog.setStopButtonEnable(false);
//                        if(CHolder.instance().getGlobalDataManager().isCloseDlg()) {
//                            mSendDialog.dismiss();
//                        }                  
//                    }
//                });
                LogC.d("start sending!");
                CHolder.instance().getJobManager().startSendFile(new ExeCallback() {
                    
                    @Override
                    public void onComplete(EXE_RESULT ret) {
                        if(mSendDialog != null && CHolder.instance().getJobData().isJobCancel() && mSendDialog.getDialog() != null && mSendDialog.getDialog().isShowing()) {
                            mSendDialog.dismissAllowingStateLoss();
                            return;
                        }
                        mSendDialog.dismissAllowingStateLoss();
                        if (ret == EXE_RESULT.SUCCESSED) {
                            LogC.d("send file success! ");  
                            if(getActivity() != null) {
                                showSendDoneDlg();
                            }
                        } else if(ret == EXE_RESULT.FAILED) {   
                            if(getActivity() != null) {
                                MultiButtonDialog dlg = MultiButtonDialog.createMsgDialog(getActivity(), R.string.sender_fail);                            
                                dlg.show();
                            }
                        }
                        
                    }
                });
            }
        });
        mStartKeyImage.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return true;    // ignore
            }
        });

        FrameLayout startKeyArea = (FrameLayout)rootView.findViewById(R.id.preview_btn_start_area);
        startKeyArea.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return mStartKeyImage.onTouchEvent(event);
            }
        });

        //スタートキーふわふわアニメーション
        mImageStartKeyEffect = (ImageView) rootView.findViewById(R.id.preview_img_startkey_effect);
        mImageStartKeyEffect.setImageDrawable(ImageResource.getImageDrawable(mImageStartKeyEffect, getResources(), R.raw.bt_start_e));
        mImageStartKeyEffect.setVisibility(View.VISIBLE);
        mStartKeyAnim = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.startkey_animation);
        mImageStartKeyEffect.startAnimation(mStartKeyAnim);

        //キャンセルキー
        //mCancelKey = (FrameLayout) rootView.findViewById(R.id.btn_cancel);
//        mCancelKey.setOnClickListener(new BaseOnClickListener() {
//            @Override
//            public void onWork(View v) {
//                // 前面アプリ通知を実施してない場合は、要求を投げない
////                SmartAppService service = getUIManager().getAppService();
////                if (service != null) {
////                    SmartAppOwner owner = service.getAppOwner();
////                    if (owner.getOwnerState() == OwnerState.FOREGROUND) {
////                        
////                        
////                        // キャンセル通知
////                        getUIManager().sendUIEvent(new UIEvent(getActivity(), ScanUIEventId.UIEVENT_CLICK_CANCELBUTTON_PREVIEW, null));
////                        // フラグメント終了する。
//                        finishPreviewFragment();
////                    }
////                }
//            }
//        });

        //ページ数
        //mCurPageView = (TextView) rootView.findViewById(R.id.text_cur_page);
       // mTotalPageView = (TextView) rootView.findViewById(R.id.text_total_page);

        // ズームインアウト
        mZoomInKey = (FrameLayout) rootView.findViewById(R.id.btn_zoom_in);
        KeyAvailability.setKeyAvailability(mZoomInKey, false);
        mZoomInKey.setOnClickListener(new BaseOnClickListener() {
            @Override
            public void onWork(View view) {
                
                

                if(mPageList.size() > mCurrentFocusedPage){
                    PageData data = mPageList.get(mCurrentFocusedPage);
                    data.viewImage.zoomIn();
                }
            }
        });

        mZoomOutKey = (FrameLayout) rootView.findViewById(R.id.btn_zoom_out);
        KeyAvailability.setKeyAvailability(mZoomOutKey, false);
        mZoomOutKey.setOnClickListener(new BaseOnClickListener() {
            @Override
            public void onWork(View view) {
                
                

                if (mPageList.size() > mCurrentFocusedPage) {
                    PageData data = mPageList.get(mCurrentFocusedPage);
                    data.viewImage.zoomOut();
                }
            }
        });

        mPrevKey = (FrameLayout) rootView.findViewById(R.id.btn_page_prev);
        mPrevKey.setVisibility(View.INVISIBLE);
//        mPrevKey.setOnClickListener(new BaseOnClickListener() {
//            @Override
//            public void onWork(View view) {
//                
//                
//
//                if (mCurrentFocusedPage > 0) {
//                    actChangeFocusPage(false);
//                }
//            }
//        });

        mNextKey = (FrameLayout) rootView.findViewById(R.id.btn_page_next);
        mNextKey.setVisibility(View.INVISIBLE);
//        mNextKey.setOnClickListener(new BaseOnClickListener() {
//            @Override
//            public void onWork(View view) {
//                
//                
//
//                if (mPageList.size() > mCurrentFocusedPage + 1) {
//                    actChangeFocusPage(true);
//                }
//            }
//        });


        //ImageViewからの拡大率やスクロール状態を受け取るリスナー
        mPreviewImageListener = new MyPreviewImageListener();

        mImageFlipper = (ViewFlipper) rootView.findViewById(R.id.flipper_preview);


        mSingleGestureListener = new SingleGestureListener();
        mGestureDetector = new GestureDetector(getActivity().getApplicationContext(), mSingleGestureListener);

        //Bundle args = getArguments();
       //if (args != null) {
       
            mScanPages = 1;//args.getInt(ARGS_SCAN_PAGES);
            //initPreviewInfo(args.getString(ARGS_JOBID));
            initPreviewInfo(null);
            //initPreviewInfo(scanManager.getScanJob().getCurrentJobId());
       // }

        // ピンチイン・ピンチアウト対応のため、マルチタップを有効にする
        //SingleTouchFrameLayout.setMultiTouchable(true);
        txtPrintColor = (TextView) rootView.findViewById(R.id.preview_txt_print_color);
        imgPrintColor = (ImageView)rootView.findViewById(R.id.preview_img_print_color);
        txtPrintCopies = (TextView)rootView.findViewById(R.id.preview_txt_print_copies_value);
        mPrintSet = (FrameLayout) rootView.findViewById(R.id.fy_print_set);
        mPrintCheck = (FrameLayout) rootView.findViewById(R.id.preview_btn_print);
        imgPrintCheck = (ImageView)rootView.findViewById(R.id.preview_title_check02);
        mPrintSetValue = (FrameLayout)rootView.findViewById(R.id.preview_set_print);
        txtSend = (TextView)rootView.findViewById(R.id.preview_txt_start);
        
        BaseOnClickListener backListener = new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                finishPreviewFragment();       
            }
        };
        txtBack = (TextView)rootView.findViewById(R.id.preview_txt_back);
        btnBack = (Button)rootView.findViewById(R.id.preview_btn_back);
        txtBack.setOnClickListener(backListener);
        btnBack.setOnClickListener(backListener);
        
        
        initPrintSetting();
        return rootView;
    }

    /**
     * FragmentのonStart<BR>
     * 画像取得キューへのセットを行う。<BR>
     */
    @Override
    public void onStart() {
        super.onStart();
        LogC.d(TAG, "onStart");
        PageData data = mPageList.get(mCurrentFocusedPage);
        mGetImageQueue.offer(data);
    }

    /**
     * FragmentのonResume<BR>
     * プレビュー開始を行う。<BR>
     */
    @Override
    public void onResume() {
        super.onResume();
        LogC.d("preview fragment---onResume");
        if(!mImageGetRunning){
            startGetImageThread();
        }
        String copies = PreferencesUtil.getInstance().getSelectedPrintPageValue().toString();//CHolder.instance().getPrintManager().getPrintSettingDataHolder().getSelectedPrintPage();    
        txtPrintCopies.setText(copies);
        
        colorData = PreferencesUtil.getInstance().getSelectedPrintColorValue();
        txtPrintColor.setText(colorData.getTextId());
        imgPrintColor.setImageResource(colorData.getImageId());
        mPrintSet.setSelected(false);
    }

    /**
     * FragmentのonPause<BR>
     * 画像取得処理をキャンセルする。<BR>
     */
    @Override
    public void onPause() {
        LogC.i(TAG, "onPause()");
        super.onPause();
        stopRunning();
        mGetImageQueue.clear();
    }

    /**
     * FragmentのonStop<BR>
     * プレビュー画像を初期化する。<BR>
     */
    @Override
    public void onStop() {
        LogC.i(TAG, "onStop");
        super.onStop();
        for(PageData data : mPageList){
            data.viewImage.setImageDrawable(null);
            data.viewProgress.setVisibility(View.VISIBLE);
            if(data.imageInfo.bitmapPreview != null){
                data.imageInfo.bitmapPreview.recycle();
                data.imageInfo.bitmapPreview  = null;
            }
        }
    }

    /**
     * プレビュー画面の初期化処理を行う。<BR>
     */
    private void initPreviewInfo(String jobId) {
        LogC.d(TAG, "initPreviewInfo");
        //mThumbnail = new ScanThumbnail(jobId);
        mPageList = new CopyOnWriteArrayList<PageData>();
        actCreatePage();
        //画面構築時は1ページ目を取得
        mCurrentFocusedPage = 0;
        //mCurPageView.setText("1");
        //mTotalPageView.setText(String.valueOf(mScanPages));
    }


    // +++++++++++++++++++++++
    // 画像表示関連
    // +++++++++++++++++++++++
    /**
     * プレビュー画像を表示するページViewを作成する。<BR>
     * 読取りページ数分のViewを作成する。<BR>
     */
    private void actCreatePage() {
        for (int i = 0; i < mScanPages; i++ ) {
            PageData data = new PageData();
            data.viewContainer = (RelativeLayout) LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.preview_image, null, false);
            data.viewProgress = (ProgressBar) data.viewContainer.findViewById(R.id.progress);
            data.viewProgress.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    // シングルタッチイベントを処理する
                    mGestureDetector.onTouchEvent(event);
                    return true;
                }
            });
            data.viewImage = (PreviewImageView) data.viewContainer.findViewById(R.id.preview_image);
            data.viewImage.setOverScrollOffsetY(10);
            data.viewImage.enableIdCardScan();
            data.viewImage.setPreviewImageListener(mPreviewImageListener);
            data.pageNo = i + 1;
            mImageFlipper.addView(data.viewContainer);
            mPageList.add(data);
        }
    }

//    /**
//     * プレビュー画面上のカレントページ数の表示を更新する。<BR>
//     */
//    private void actUpdateCurrentPageInfo(){
//        int cur_page = mImageFlipper.indexOfChild(mImageFlipper.getCurrentView());
//        if(cur_page >= 0){
//            if(mCurrentFocusedPage != cur_page){
//                LogC.i(TAG, "actUCP page_no: " + mCurrentFocusedPage +  " >> " + cur_page);
//            }
//            mCurrentFocusedPage = cur_page;
//            //ページ数文字列を更新する
//            //mCurPageView.setText(Integer.toString(mCurrentFocusedPage + 1));
//        }
//    }

//    /**
//     * カレントフォーカスのページのプレビュー画像の表示を行う。<BR>
//     */
//    private void actUpdateCurrentPageData(){
//        actUpdatePage(mCurrentFocusedPage);
//    }

    /**
     * 指定したページのプレビュー画像の表示を行う。<BR>
     * 指定したページのプレビュー画像データの取得が完了していない場合は表示を行わない。<BR>
     * @param pageNo プレビュー画像の表示を行うページ番号
     */
    private void actUpdatePage(int pageNo){
        LogC.d(TAG, "actUpdatePage");
        if(pageNo >= mPageList.size() || pageNo < 0){
            return;
        }
        PageData data = mPageList.get(pageNo);

        //カレントページ画像
        if(pageNo == mCurrentFocusedPage){
            //まだ画像イメージが存在しなければタスクに積んでおく
            if(data.imageInfo.bitmapPreview != null){
                data.viewImage.startPreview(data.imageInfo.scale, data.imageInfo.bitmapPreview);
                data.viewProgress.setVisibility(View.INVISIBLE);
            }
            //カレントの更新後はちょい見せ等の更新も行う
            for(PageData other_data : mPageList){
                int other_page_num = mPageList.indexOf(other_data);
                if(other_page_num != mCurrentFocusedPage){
                    actUpdatePage(other_page_num);
                }
            }
        }
        //それ以外
        else {
            data.viewImage.stopPreview();
        }
    }

    /**
     * 拡大縮小キーの表示/非表示の更新を行う。<BR>
     */
    private void actUpdateZoomInOutKey(){
        boolean isPreviewExist = (mPageList.get(mCurrentFocusedPage).imageInfo.bitmapPreview != null);

        KeyAvailability.setKeyAvailability(mZoomInKey, (isPreviewExist && mIsZoomInFuncOk));
        KeyAvailability.setKeyAvailability(mZoomOutKey, (isPreviewExist && mIsZoomOutFuncOk));
    }

    /**
     * ページめくりキーの表示/非表示の更新を行う。<BR>
     */
    private void actUpdatePrevNextKey(){
        boolean isPreviewExist = (mPageList.get(mCurrentFocusedPage).imageInfo.bitmapPreview != null);
        boolean isPrevAvailable = (mImageFlipper.indexOfChild(mImageFlipper.getCurrentView()) != 0);
        boolean isNextAvailable = (mImageFlipper.getChildCount() > mImageFlipper.indexOfChild(mImageFlipper.getCurrentView()) + 1);

        if(isPreviewExist){
            isPrevAvailable = (isPrevAvailable && mIsPrevFuncOk) ;
            isNextAvailable = (isNextAvailable && mIsNextFuncOk) ;
        }
        mPrevKey.setVisibility(isPrevAvailable ? View.VISIBLE : View.INVISIBLE);
        mNextKey.setVisibility(isNextAvailable ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 表示していないプレビュー画像データを開放する。<BR>
     * プレビューページのリスト全体をチェックして、カレントフォーカスされていないページの画像データをbitmapからrecycleする。<BR>
     */
    private void actReleaseUnusedImage(){
        LogC.d(TAG, "actReleaseUnusedImage");
        for(PageData data : mPageList){
            int page_no = mPageList.indexOf(data);
            if(page_no != mCurrentFocusedPage){
                data.viewImage.setImageDrawable(null);
                data.viewProgress.setVisibility(View.VISIBLE);
                if(data.imageInfo.bitmapPreview != null){
                    LogC.i(TAG, String.format("actRUI bmp_prev DELETED!"));
                    data.imageInfo.bitmapPreview.recycle();
                    data.imageInfo.bitmapPreview  = null;
                }
            }
            if(page_no > mCurrentFocusedPage + IMAGE_MEMORY_AMOUNT || page_no < mCurrentFocusedPage - IMAGE_MEMORY_AMOUNT){
                data.viewImage.setImageDrawable(null);
                data.viewProgress.setVisibility(View.VISIBLE);
            }
        }

        System.gc();
    }

    // +++++++++++++++++++++++
    // 画像の取得・表示
    // +++++++++++++++++++++++
    /*
     * 指定したURLの画像を並行処理で取得するためのスレッド。
     * 一度に一枚ずつ画像取得するため、FIFOキューで管理する。
     */
    /** プレビュー画像取得処理のキュー */
    ConcurrentLinkedQueue<PageData> mGetImageQueue = new ConcurrentLinkedQueue<PageData>();


    // +++++++++++++++++++++++
    // スクロール,拡大縮小キー関連
    // +++++++++++++++++++++++
    /**
     * プレビュー画面のページめくり、拡大縮小の有効可否を受け取る通知を表す。<BR>
     * ページめくり、拡大縮小の有効可否に応じてキーの表示/非表示を行う機能を提供する。<BR>
     */
    private class MyPreviewImageListener implements PreviewImageListener {

        /**
         * 拡大縮小の有効可否を受け取る。<BR>
         * @param obj イベントが発生したページのImageViewオブジェクト
         * @param isZoomInAvailable 拡大の有効可否(true:有効、 false:無効)
         * @param isZoomOutAvailable 縮小の有効可否(true:有効、 false:無効)
         */
        @Override
        public void notifyZoomInOutAvailability(PreviewImageView obj, boolean isZoomInAvailable, boolean isZoomOutAvailable) {
            //現在表示中の画像からのイベントにのみ反応する
            PageData data = mPageList.get(mCurrentFocusedPage);
            if (data.viewImage.equals(obj)) {
                mIsZoomInFuncOk = isZoomInAvailable;
                mIsZoomOutFuncOk = isZoomOutAvailable;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        actUpdateZoomInOutKey();
                    }
                });
            }
        }

        /**
         * ページめくりの有効可否を受け取る。<BR>
         * @param obj イベントが発生したページのImageViewオブジェクト
         * @param isPrevAvailable 前ページの有効可否(true:有効、 false:無効)
         * @param isNextAvailable 次ページの有効可否(true:有効、 false:無効)
         */
        @Override
        public void notifyPrevNextAvailability(PreviewImageView obj, boolean isPrevAvailable, boolean isNextAvailable) {
            //現在表示中の画像からのイベントにのみ反応する
            PageData data = mPageList.get(mCurrentFocusedPage);
            if (data.viewImage.equals(obj)) {
                mIsPrevFuncOk = isPrevAvailable;
                mIsNextFuncOk = isNextAvailable;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        actUpdatePrevNextKey();
                    }
                });
            }
        }

        /**
         * フリックによるページめくりの通知を受け取る。<BR>
         * @param isDirectionForNextpage ページめくり方向(true:次ページ方向、 false:前ページ方向)
         */
        @Override
        public void notifyFlingOccurredAsPageChange(final boolean isDirectionForNextpage) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //actChangeFocusPage(isDirectionForNextpage);
                }
            });
        }

    }


    /**
     * フリック操作によるページめくり機能を提供する。<BR>
     */
    private class SingleGestureListener extends SimpleOnGestureListener {
        /* (非 Javadoc)
         * @see android.view.GestureDetector.SimpleOnGestureListener#onDown(android.view.MotionEvent)
         */
        /**
         * タップされた時に呼ばれる。<BR>
         * 他のMotionEventよりも先行して呼び出される。<BR>
         * @param e タップ動作のイベント
         * @return 結果(常にtrue)
         */
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        /**
         * フリックイベントが通知された時に呼ばれる。<BR>
         * 計算された速度は、毎秒ピクセル中のxおよびy軸に沿って提供される。<BR>
         * @param e1 フリック開始した初回のMotionEvent
         * @param e2 カレントのフリックを引き起こしたMotionEvent
         * @param velocityX x軸に沿った毎秒ピクセルの中で測定された速度
         * @param velocityY y軸に沿った毎秒ピクセルの中で測定された速度
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //ここを通過するのは、画像が表示されていないとき(ぐるぐるが表示されているとき)にフリックをした時
            LogC.i(TAG, "onFling begin x:"+ velocityX + " y:" + velocityY);
            final boolean is_direction_for_nextpage = (velocityX < 0) ? true : false;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //actChangeFocusPage(is_direction_for_nextpage);
                }
            });
            return true;
        }
    }

    /**
     * フォーカスページの切り替えを行う。<BR>
     * ページ切り替え時はアニメーションと、ページ番号の表示の更新を行う。<BR>
     * @param isDirectionForNextpage ページめくり方向(true:次ページ方向、 false:前ページ方向)
     */
//    private void actChangeFocusPage(final boolean isDirectionForNextpage){
//        if(isDirectionForNextpage){
//            if(mPageList.size() > mImageFlipper.indexOfChild(mImageFlipper.getCurrentView()) + 1){
//                mImageFlipper.setInAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.animator.slide_open_enter_1000));
//                mImageFlipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.animator.slide_open_exit_1000));
//                mImageFlipper.showNext();
//            }
//        } else {
//            if(mImageFlipper.indexOfChild(mImageFlipper.getCurrentView()) > 0){
//                mImageFlipper.setInAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.animator.slide_close_enter_1000));
//                mImageFlipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.animator.slide_close_exit_1000));
//                mImageFlipper.showPrevious();
//            }
//        }
//        if(mImageFlipper.getInAnimation() != null){
//            mImageFlipper.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//                    //アニメーションが始まったタイミングでページ番号等更新する
//                    //actUpdateCurrentPageInfo();
//                    PageData data = mPageList.get(mCurrentFocusedPage);
//                    if(!mGetImageQueue.contains(data)){
//                        mGetImageQueue.offer(data);
//                    }
//
//                    KeyAvailability.setKeyAvailability(mZoomInKey, false);
//                    KeyAvailability.setKeyAvailability(mZoomOutKey, false);
//
//                    boolean isPrevAvailable = (mImageFlipper.indexOfChild(mImageFlipper.getCurrentView()) != 0);
//                    boolean isNextAvailable = (mImageFlipper.getChildCount() > mImageFlipper.indexOfChild(mImageFlipper.getCurrentView()) + 1);
//                    mPrevKey.setVisibility(isPrevAvailable ? View.VISIBLE : View.INVISIBLE);
//                    mNextKey.setVisibility(isNextAvailable ? View.VISIBLE : View.INVISIBLE);
//                }
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//                    //使わない
//                }
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    //アニメーションが終わったら画像を取得する
//                    actUpdateCurrentPageData();
//                }
//            });
//        }
//    }

    /**
     * プレビュー画像の取得と表示用ビットマップ生成を行う。<BR>
     * SmartSDKからプレビュー画像の取得を行い、ビットマップ生成して引数で指定されたプレビューページの情報オブジェクトへ登録する。<BR>
     * @param data プレビューページの情報オブジェクト
     */
//    private void setThumbnailToView(final PageData data) {
//        LogC.d(TAG, "setThumbnailToView");
//        // InputStream対応版
//        InputStream input = mThumbnail.getThumbnailInputStream(data.pageNo);
//        if(null == input){
//            if (!mImageGetRunning) {
//                return;
//            }
//            // プレビュー画像が取得出来なかった場合のメッセージ表示
////            ParamSet param = new ParamSet();
////            param.put(ScanUIEventId.ORIGINAL_MSG_KEY, MessageId.MSG_SCN_CANNOT_DISP_PREVIEW.toString());
////            getUIManager().sendUIEvent(new UIEvent(getActivity(), ScanUIEventId.UIEVENT_ORIGINAL_MESSAGE, param));
////            handler.post(new Runnable() {
////                @Override
////                public void run() {
////                    data.viewProgress.setVisibility(View.GONE);
////                }
////            });
//            return;
//        }
//        BitmapFactory.Options opt = new BitmapFactory.Options();
//        opt.inPurgeable = true;
//        opt.inPreferredConfig = Bitmap.Config.RGB_565;
//
//        opt.inJustDecodeBounds = true;
//        //BitmapFactory.decodeStream(input, null, opt) ;
//        final int wBmp = opt.outWidth;
//        final int hBmp = opt.outHeight;
//
//        //int rotate = mThumbnail.getRotate();
//        final int max_size = 1754 * 1240 * 10;
//        for (int i = 1;; i++) {
//            if (wBmp * hBmp <= max_size * i * i) {
//                opt.inSampleSize = i;
//                data.imageInfo.scale = i;
//                break;
//            }
//        }
//        opt.inJustDecodeBounds = false;
//
//        // InputStreamは一度readしないと、次回読み込みでnullが返ってくる
//        input = mThumbnail.getRetryInputStream();
//
//        Bitmap bmp = BitmapFactory.decodeStream(input, null, opt) ;
//        Matrix matrix = new Matrix();
//        int rotate = mThumbnail.getRotate();
//        if (rotate == 0) {
//            data.imageInfo.bitmapPreview = bmp;
//        } else {
//            matrix.postRotate(rotate); // 回転させる角度を指定
//            Bitmap out = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
//            bmp.recycle();
//            bmp = null;
//            data.imageInfo.bitmapPreview = out;
//        }
//    }

    private void setImageToView(final PageData data) {
      LogC.d(TAG, "setImageToView");
      // InputStream対応版
      
      if(CHolder.instance().getJobData().getPathCombinedJPG() == null) {
          return;
      }
          
      File f = new File(CHolder.instance().getJobData().getPathCombinedJPG());   
      LogC.d("get file path:" + CHolder.instance().getJobData().getPathCombinedJPG());
      try {
        InputStream input = new FileInputStream(f);
        InputStream inputTmp = input;
        
        
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPurgeable = true;
        opt.inPreferredConfig = Bitmap.Config.RGB_565;

        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputTmp, null, opt) ;
        final int wBmp = opt.outWidth;
        final int hBmp = opt.outHeight;

        //int rotate = mThumbnail.getRotate();
       // final int max_size = 1754 * 1240 * 10;
        final int max_size = 1754 * 1240 * 3;
        for (int i = 1;; i++) {
            if (wBmp * hBmp <= max_size * i * i) {
                LogC.d("wBmp * hBmp:" + wBmp * hBmp);
                opt.inSampleSize = i;
                LogC.d("insampleSize :" + i);
                data.imageInfo.scale = i;
                break;
            }
        }
        opt.inJustDecodeBounds = false;
        input = new FileInputStream(f);
        Bitmap bmp = BitmapFactory.decodeStream(input, null, opt) ;
        if(bmp == null) {
            LogC.e("can not create bitmap");
            return;
        }
        data.imageInfo.bitmapPreview = bmp;
        
     } catch (FileNotFoundException e) {
         LogC.e("FileNotFoundException", e);
            if (!mImageGetRunning) {
                return;
            }
            // プレビュー画像が取得出来なかった場合のメッセージ表示
            //TODO dialog show
//            ParamSet param = new ParamSet();
//            param.put(ScanUIEventId.ORIGINAL_MSG_KEY, MessageId.MSG_SCN_CANNOT_DISP_PREVIEW.toString());
//            getUIManager().sendUIEvent(new UIEvent(getActivity(), ScanUIEventId.UIEVENT_ORIGINAL_MESSAGE, param));
            handler.post(new Runnable() {
                @Override
                public void run() {
                    data.viewProgress.setVisibility(View.GONE);
                }
            });
            return;
        
    }   
     
      //InputStream input = mThumbnail.getThumbnailInputStream(data.pageNo);
     
     
//      } else {
//          matrix.postRotate(rotate); // 回転させる角度を指定
//          Bitmap out = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
//          bmp.recycle();
//          bmp = null;
//          data.imageInfo.bitmapPreview = out;
//      }
  }


    /**
     * 画像取得スレッドを停止させる。<BR>
     */
    public void stopRunning(){
        mImageGetRunning = false;
    }

    /**
     * 画像取得処理を実行するスレッドを起動する。<BR>
     */
    private void startGetImageThread(){
        LogC.d(TAG, "startGetImageThread");
        if(!mImageGetRunning){
            mImageGetRunning = true;
            new Thread(new Runnable(){
                @Override
                public void run(){
                    getImageProc();
                }
            }).start();
        }
    }

    /**
     * キューに積まれたプレビュー画像取得要求を非同期で実行する。<BR>
     * キューが空の場合は、非同期タスクがキャンセルされるまでキューをポーリングする。<BR>
     */
    private void getImageProc(){
        LogC.d(TAG, "getImageProc");
        while (mImageGetRunning) {
            final PageData data = mGetImageQueue.poll();
            if (data == null) {
                try {
                    Thread.sleep(200);
                    continue;
                } catch (InterruptedException e) {
                    LogC.w(TAG, "InterruptedException poll");
                }
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        actReleaseUnusedImage();
                    }
                });
                setImageToView(data);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if ( (data != null) && mImageGetRunning) {
                            actUpdatePage(mPageList.indexOf(data));
                        }
                    }
                });
            }
        }
        LogC.d(TAG, "getImageProc: end");
    }

//    @Override
//    protected GenericId[] getActions() {
//        return new GenericId[] {
//                ScanUIEventId.UIEVENT_CLICK_RESETBUTTON,
//                ScanUIEventId.UIEVENT_CLICK_CANCEL,
//                ScanUIEventId.UIEVENT_CLOSE_PREVIEW,
//        };
//    }

//    @Override
//    protected UIEventReceiver getActionReceiver(GenericId action) {
//        return mScanUIEventMap.get(action);
//    }

//    private Map<ScanUIEventId, UIEventReceiver> mScanUIEventMap = new EnumMap<ScanUIEventId, UIEventReceiver>(ScanUIEventId.class) {
//        {
//            put(ScanUIEventId.UIEVENT_CLICK_RESETBUTTON, new UIEventReceiver() {
//                @Override
//                public void onReceiveUIEvent(UIEvent event) {
//                    finishPreviewFragment();
//                }
//            });
//            put(ScanUIEventId.UIEVENT_CLICK_CANCEL, new UIEventReceiver() {
//                @Override
//                public void onReceiveUIEvent(UIEvent event) {
//                    finishPreviewFragment();
//                }
//            });
//            put(ScanUIEventId.UIEVENT_CLOSE_PREVIEW, new UIEventReceiver() {
//                @Override
//                public void onReceiveUIEvent(UIEvent event) {
//                    finishPreviewFragment();
//                }
//            });
//        }
//    };

    /**
     * プレビューフラグメントを終了する。<BR>
     */
    private void finishPreviewFragment() {
//        ParamSet param = new ParamSet();
//        param.put(UIEventId.PARAM_FRAGMENT, this);
//        param.put(UIEventId.PARAM_FRAGMENT_ANIMATION, FragmentAnimation.ANIMATION_POPUP_CLOSE);
//        getUIManager().sendUIEvent(new UIEvent(getActivity(), UIEventId.UIEVENT_CLOSE_POPUPWINDOW, param));

        // 画像取得スレッドの停止
        stopRunning();
        mGetImageQueue.clear();
        IdCardScanPrintActivity parActivity = (IdCardScanPrintActivity) getActivity();
        if(parActivity != null) {
            parActivity.backFragment();
        }
       
        // ここでマルチタップを無効に戻す
       // SingleTouchFrameLayout.setMultiTouchable(false);
    }


    private void initPrintSetting() {
        isPrint = false;
        mPrintSetValue.setOnClickListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
               mPrintSet.setSelected(true);
               LogC.d("print set onclick!");
               
               if(isPrint) {
                   Intent intent = new Intent(getActivity(), InitialSettingPrintActivity.class);
                   getActivity().startActivity(intent);                   
               }
                
            }
        });
        
        mPrintSet.setOnClickListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
               LogC.d("print set onclick!");
               
               if(isPrint) {
                   Intent intent = new Intent(getActivity(), InitialSettingPrintActivity.class);
                   getActivity().startActivity(intent);
                   //getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
               }
                
            }
        });
        mPrintCheck.setOnClickListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                txtSend.setText(R.string.txid_scmn_b_012);
                LogC.d("printcheck onclik!");
                if(imgPrintCheck.isSelected()) {
                    
                    isPrint = false;
                    mPrintCheck.setSelected(false);
                    imgPrintCheck.setSelected(false);
                    mPrintSet.setVisibility(View.GONE);
                    mPrintSetValue.setVisibility(View.GONE);
                } else {
                    isPrint = true;
                    mPrintCheck.setSelected(true);
                    imgPrintCheck.setSelected(true);
                    mPrintSet.setVisibility(View.VISIBLE);
                    mPrintSetValue.setVisibility(View.VISIBLE);
                }
            }
        });
        
        
    }
 
    private void showSendDoneDlg(){        
        final TitleButtonDialog dialog = new TitleButtonDialog(getActivity());
        dialog.setLeftButtonVisible(View.VISIBLE);
        dialog.setButtonLeft(getActivity().getString(R.string.button_continue));
        dialog.setRightButtonVisible(View.VISIBLE);
        dialog.setTxtTitle(getString(R.string.sender_complete));
        ArrayList<String> msgList = CHolder.instance().getGlobalDataManager().getSendLogList();
             
        BaseOnClickListener completeCallBack = new BaseOnClickListener() {
                
            @Override
            public void onWork(View v) {
                    
                HDDUtil.deleteFile(Const.SCAN_FILE_PATH);
                dialog.dismiss();
                getActivity().finish();
                CHolder.instance().getGlobalDataManager().getsActivity().finish();                
            }
        };
            
        BaseOnClickListener continueCallBack = new BaseOnClickListener() {
                
            @Override
            public void onWork(View v) {
                
                HDDUtil.deleteFile(Const.SCAN_FILE_PATH);
                dialog.dismiss();
                ((IdCardScanPrintActivity) getActivity()).previewToFirstScan();
            }
        };
        
        BaseOnClickListener printCallBack = new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                CHolder.instance().getPrintManager().getPrintAuthInfo(getActivity(), (PrintColor)colorData.getItemValue(), new PrintJobContinueCB() {
                    
                    @Override
                    public void printJobContinue() {
                        LogC.d("start print");
                        dialog.dismiss();
                        CHolder.instance().getJobManager().startPrintAndDelete(new ExeCallback() {
                            
                            @Override
                            public void onComplete(EXE_RESULT ret) {
                                //printProcessingFragment.dismiss();
                                
                                if (ret == EXE_RESULT.SUCCESSED) {
                                    LogC.d("ret is success");
                                    HDDUtil.deleteFile(Const.SCAN_FILE_PATH);
                                    if(getActivity() != null) {
                                        final TitleButtonDialog printCompleteDlg = new TitleButtonDialog(getActivity());
                                        printCompleteDlg.setLeftButtonVisible(View.VISIBLE);
                                        printCompleteDlg.setButtonLeft(getActivity().getString(R.string.button_continue));
                                        printCompleteDlg.setRightButtonVisible(View.VISIBLE);
                                        printCompleteDlg.setCenterButtonVisible(View.GONE);
                                        printCompleteDlg.setButtonRight(getActivity().getString(R.string.button_complete));
                                        printCompleteDlg.setTxtTitle(getString(R.string.print_complete));
                                        printCompleteDlg.setLeftButtonClick(new BaseOnClickListener() {
                                            
                                            @Override
                                            public void onWork(View v) {
                                                
                                                printCompleteDlg.dismiss();
                                                ((IdCardScanPrintActivity) getActivity()).previewToFirstScan();                                
                                                
                                            }
                                        });
                                        printCompleteDlg.setRightButtonClick(new BaseOnClickListener() {
                                            
                                            @Override
                                            public void onWork(View v) {
                                                
                                                printCompleteDlg.dismiss();
                                                getActivity().finish();
                                                CHolder.instance().getGlobalDataManager().getsActivity().finish();      
                                            }
                                        });
                                        printCompleteDlg.setTxtMsg(getString(R.string.sender_dlg_message_no_print));
                                        printCompleteDlg.show();
                                    }
                                } else if (ret == EXE_RESULT.FAILED) {                             
                                    LogC.d("ret is fail");
                                    if(getActivity() != null) {
                                        MultiButtonDialog msgDlg = MultiButtonDialog.createMsgDialog(getActivity(), R.string.print_fail);
                                        msgDlg.show();
                                    }
                                }
                            }
                        });
                    }
                });
            }
        };
        String logString = "";
        if(isPrint) {
            dialog.setCenterButtonVisible(View.VISIBLE);           
            dialog.setButtonCenter(getActivity().getString(R.string.button_complete));
            dialog.setButtonRight(getActivity().getString(R.string.button_print));
            dialog.getRightButton().setBackground(getActivity().getResources().getDrawable(R.drawable.print_bt_selector));
            dialog.setLeftButtonClick(continueCallBack);
            dialog.setCenterButtonClick(completeCallBack);
            dialog.setRightButtonClick(printCallBack);
            logString = getString(R.string.sender_dlg_message_print);
        } else {
            HDDUtil.deleteFile(Const.SCAN_FILE_PATH);
            dialog.setCenterButtonVisible(View.GONE);
            dialog.setButtonRight(getActivity().getString(R.string.button_complete));
            dialog.setLeftButtonClick(continueCallBack);
            dialog.setRightButtonClick(completeCallBack);
            logString = getString(R.string.sender_dlg_message_no_print);
           
            
        }
        
        for (int i = 0; i < msgList.size(); i++) {
            logString += "\n" + msgList.get(i);
        }
        
        dialog.setTxtMsg(logString);
        dialog.show();
    }
    
}