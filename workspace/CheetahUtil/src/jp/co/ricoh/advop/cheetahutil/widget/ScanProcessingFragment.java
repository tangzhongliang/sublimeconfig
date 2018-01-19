
package jp.co.ricoh.advop.cheetahutil.widget;

import jp.co.ricoh.advop.cheetahutil.R;
import jp.co.ricoh.advop.cheetahutil.util.Buzzer;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class ScanProcessingFragment extends DialogFragment {
    /** ログ出力用タグ情報 */
    private static final String TAG = "SS:MN:ScnPrcssng";
    private TextView mProcessTitle;
    private TextView mSendCountText;
    private TextView mAddrCountText;
    private TextView mMemoryText;
    private TextView mMemoryLabelText;
    private TextView mRuningTxt;
    private FrameLayout mScanningAnimationContainer;
    private ViewFlipper mScanningAnimationFlipper = null;
    private Context context;
    private boolean isCancled = false;
    public static final int TYPE_SCAN_AND_SEND = 0;
    /** 実行中アニメーション種類：読み取りのみ */
    public static final int TYPE_SCAN_ONLY = 1;
    /** 実行中アニメーション種類：送信のみ */
    public static final int TYPE_SEND_ONLY = 2;
    /** 実行中アニメーション種類：印刷 */
    public static final int TYPE_PRINT = 3;
    /** 実行中アニメーション種類：WebDav, upload to share point server */
    public static final int TYPE_WEBDAV_UPLOAD = 4;
    // 実行中のアニメーションタイプ
    int mScanningAnimation = TYPE_SCAN_AND_SEND;
    int mNextScanningAnimation = TYPE_SCAN_AND_SEND;

    /** メモリ量 */
    private int mMemory = 0;
    /** 送信枚数 */
    private int mSendCount = 0;
    private boolean mIsClearParam = false;
    private Button stop;
    private AlertDialog alertDialog;
    private Activity mActivity;
	private ImageView totalImage;
	private TextView totalText;
	private TextView inputTotal;

    /*
     * @Override public View onCreateView(LayoutInflater inflater, ViewGroup
     * container, Bundle savedInstanceState) {
     * getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE); View view =
     * inflater.inflate(R.layout.view_running, container, false); mSendCountText
     * = (TextView) view.findViewById(R.id.text_cur_original_num); // 宛先件数
     * mAddrCountText = (TextView)view.findViewById(R.id.text_total_dest_num);
     * // 残メモリ mMemoryText =
     * (TextView)view.findViewById(R.id.text_rest_memory_num); mMemoryLabelText
     * = (TextView)view.findViewById(R.id.txt_runnning_send_memory); //
     * アニメーション表示領域 mScanningAnimationContainer =
     * (FrameLayout)view.findViewById(R.id.container_anime);
     * setScanningAnimation(mNextScanningAnimation); return view; }
     */
    /*
     * @Override public void onCreate(Bundle savedInstanceState) {
     * super.onCreate(savedInstanceState); setStyle(DialogFragment.STYLE_NORMAL,
     * android.R.style.Theme_Black_NoTitleBar_Fullscreen); }
     */

  
    @Override
    public void onCreate(Bundle savedInstanceState) { 
    	LogC.d("onCreate");
        super.onCreate(savedInstanceState);
//        Bundle arguments = getArguments();
//        this.mNextScanningAnimation =arguments.getInt("type", 0);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
    	
    	//super.show(manager, tag);
    	 android.app.FragmentTransaction ft = manager.beginTransaction();
         ft.add(this, tag);
         ft.commitAllowingStateLoss();    	    	
    }
    
	public ScanProcessingFragment(Activity act, int style, int scanPages, int memory, int addrCount) {
    	LogC.d("ScanProcessingFragment");
    	mActivity = act;
    	this.mNextScanningAnimation = style;
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(act);
    	
    	LayoutInflater inflater = act.getLayoutInflater();
        View view = inflater.inflate(R.layout.view_running, null);
        alertDialog = builder.create();
        alertDialog.show();
       
        alertDialog.setTitle(null);
        alertDialog.setContentView(view);
        
        alertDialog.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				 if(keyCode == KeyEvent.KEYCODE_BACK  && event.getAction() == KeyEvent.ACTION_DOWN)
                 {
                    Buzzer.onBuzzer(Buzzer.BUZZER_NACK);
                    return true;
                 }                

				return false;
			}
		});
        alertDialog.setCancelable(false);

        mRuningTxt = (TextView) view.findViewById(R.id.txt_running_send);
        // builder.setView(view);
        mSendCountText = (TextView) view.findViewById(R.id.text_cur_original_num);
        
        // 宛先件数
        mAddrCountText = (TextView) view.findViewById(R.id.text_total_dest_num);
        // 残メモリ     
        mMemoryText = (TextView) view.findViewById(R.id.text_rest_memory_num);        
        mMemoryLabelText = (TextView) view.findViewById(R.id.txt_runnning_send_memory);
        stop = (Button) view.findViewById(R.id.vr_bt_stop);
        totalImage = (ImageView) view.findViewById(R.id.vr_total);
        totalText = (TextView) view.findViewById(R.id.txt_total_of_address);
        inputTotal = (TextView) view.findViewById(R.id.text_total_dest_num);
        
        if (style == TYPE_SCAN_ONLY || style == TYPE_SCAN_AND_SEND) {
        	mMemoryText.setText(memory + "%");
        	mAddrCountText.setText(addrCount + "");
        	mSendCountText.setText(scanPages + "");
        } else if (style == TYPE_WEBDAV_UPLOAD) { // For webdav
        	// Title
        	mRuningTxt.setText(R.string.txid_sending);
        	// 原稿枚数
        	ImageView imgVw1 = (ImageView) view.findViewById(R.id.imgvw_org_num);
        	imgVw1.setVisibility(View.INVISIBLE);
        	TextView tv1 = (TextView) view.findViewById(R.id.txt_cmn_g_001);
        	tv1.setVisibility(View.INVISIBLE);
        	mSendCountText.setVisibility(View.INVISIBLE);
        	
        	// 全宛先数
        	ImageView imgVw2 = (ImageView) view.findViewById(R.id.vr_total);
        	imgVw2.setVisibility(View.INVISIBLE);
        	
        	TextView tv2 = (TextView) view.findViewById(R.id.txt_total_of_address);
        	tv2.setVisibility(View.INVISIBLE);
        	
        	mAddrCountText.setVisibility(View.INVISIBLE);
        	
        	// 残メモリー
        	mMemoryText.setText(memory + "%");
        }
        
        isCancled = false;
        // アニメーション表示領域
        mScanningAnimationContainer = (FrameLayout) view.findViewById(R.id.container_anime);
        setScanningAnimation(mNextScanningAnimation);
        LogC.d("ScanProcessingFragment");
    }	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	LogC.d("onCreateDialog");
        return alertDialog;
    }

    @Override
    public void onStart() {
    	LogC.d("onStart");
         super.onStart();

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        
        if(getDialog() == null) {
        	LogC.w(TAG, "onStart(), getDialog() fail.");        	
        } else {        
	        if(getDialog().getWindow() == null) {
	        	LogC.w(TAG, "onStart(), getWindow() fail.");	        	
	        } else {
	        	getDialog().getWindow().setLayout(dm.widthPixels, dm.heightPixels);
	        }
        }
        
        if(this.getDialog() == null) {
        	LogC.w(TAG, "onStart(), this.getDialog() fail.");        	
        } else {
        	this.getDialog().setCancelable(false);
        }
    }
    
   /* public void dismissPart(){
    	mAddrCountText.setVisibility(View.INVISIBLE);
    	totalImage.setVisibility(View.INVISIBLE);
    	totalText.setVisibility(View.INVISIBLE);
    }*/

    private void setScanningAnimation(int type) {
        LogC.i(TAG, "setScanningAnimation():" + type);
        if (mScanningAnimationFlipper != null) {
            mScanningAnimationFlipper.stopFlipping();
            mScanningAnimationContainer.removeView(mScanningAnimationFlipper);
        }

        LayoutInflater inflater = LayoutInflater.from(mActivity);

        if (type == TYPE_SCAN_ONLY) {
            // 読取のみタイプ
            mScanningAnimationFlipper = (ViewFlipper) inflater.inflate(R.layout.flipper_anim_scan,
                    null, false);
        } else if (type == TYPE_SEND_ONLY) {
            // 送信のみタイプ
            mScanningAnimationFlipper = (ViewFlipper) inflater.inflate(R.layout.flipper_anim_send,  
                    null, false);
        } else if (type == TYPE_PRINT) {
        	// 印刷タイプ
        	 mScanningAnimationFlipper = (ViewFlipper) inflater.inflate(R.layout.flipper_anim_print,
                     null, false);
        } else if (type == TYPE_WEBDAV_UPLOAD) {
        	// Upload to share point server
        	 mScanningAnimationFlipper = (ViewFlipper) inflater.inflate(R.layout.flipper_anim_send,  
                     null, false);
        } else {			
		   // 読取+送信タイプ
            mScanningAnimationFlipper = (ViewFlipper) inflater.inflate(
                    R.layout.flipper_anim_scan_and_send, null, false);
        }
        mScanningAnimationContainer.addView(mScanningAnimationFlipper);
        mScanningAnimationFlipper.setFlipInterval(80);
        mScanningAnimationFlipper.startFlipping();

        mScanningAnimation = type;

    }


//    public void setStopCallback(updateOnClick stopCallback){
//    	stop.setOnClickListener(stopCallback);
//    }
    public void setStopCallback(OnClickListener stopCallback){
    	stop.setOnClickListener(stopCallback);
  	} 
    
    public void setOrignalText(String text) {
       mSendCountText.setText(text);
    }
    
    public void setAddrCountText(String text) {
    	mAddrCountText.setText(text);
    }
    
    public void setMemoryText(String text) {
    	mMemoryText.setText(text + "%");
    }
    public void setStopButtonEnable(boolean isEnable) {
    	if(!isCancled) {
    		stop.setEnabled(isEnable);
    	}
    }
   
    public void setRunningTitle(int resId) {
    	if(mRuningTxt != null) {
    		mRuningTxt.setText(resId);
    	}
    }
    
    public void dismissPart(){
    	totalImage.setVisibility(View.INVISIBLE);
    	totalText.setVisibility(View.INVISIBLE);
    	inputTotal.setVisibility(View.INVISIBLE);
    }
    
    public void setCanceled(){
    	isCancled = true;
    }
    
    public void setDisCanceled(){
    	isCancled = false;
    }
    
    public boolean isCanceled(){
    	return isCancled;
    }
}
