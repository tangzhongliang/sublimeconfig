package jp.co.ricoh.advop.idcardscanprint.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ViewFlipper;
import jp.co.ricoh.advop.cheetahutil.R;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.util.Buzzer;


/**
 * addViewによるポップアップ画面表示を表す。<BR>
 */
public class PrintProcessingFragment extends DialogFragment {
	//private ViewBehavior mBehavior;
	private Activity mActivity;
	private AlertDialog alertDialog;
	private FrameLayout mPrinttingAnimationContainer;
	private ViewFlipper mPrinttingAnimationFlipper = null;
	private Button stop;
	// ライブラリ化
	public boolean mIsNoShown;
	private boolean isCanceled = false;

	/**
	 * スライド画面のアニメーションを設定する。<BR>
	 * behavior 画面の振舞い
	 * parent 親ビュー
	 * view ビュー
	*/
	public PrintProcessingFragment(Activity act) {
		mActivity = act;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
    	
    	LayoutInflater inflater = act.getLayoutInflater();
        View view = inflater.inflate(R.layout.view_print_running, null);
        
        alertDialog = builder.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();
        alertDialog.setTitle(null);
        alertDialog.setContentView(view);  
        
        alertDialog.setOnKeyListener(new OnKeyListener() {
            
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                 if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
                 {
                    Buzzer.onBuzzer(Buzzer.BUZZER_NACK);
                    return true;
                 }                

                return false;
            }
        });
        alertDialog.setCancelable(false);
        
        isCanceled = false;
        // builder.setView(view);
//        mSendCountText = (TextView) view.findViewById(R.id.text_cur_original_num);
//        // 宛先件数
//        mAddrCountText = (TextView) view.findViewById(R.id.text_total_dest_num);
//        // 残メモリ
//        mMemoryText = (TextView) view.findViewById(R.id.text_rest_memory_num);
//        mMemoryLabelText = (TextView) view.findViewById(R.id.txt_runnning_send_memory);
        stop = (Button) view.findViewById(R.id.print_bt_stop);		
		mPrinttingAnimationContainer = (FrameLayout) view.findViewById(R.id.print_container_anime);
		setPrinttingAnimation();
	}
	
    
    @Override
    public void show(FragmentManager manager, String tag) {
        
        //super.show(manager, tag);
         android.app.FragmentTransaction ft = manager.beginTransaction();
         ft.add(this, tag);
         ft.commitAllowingStateLoss();              
    }
    
    private void setPrinttingAnimation() {
       
        if (mPrinttingAnimationFlipper != null) {
        	mPrinttingAnimationFlipper.stopFlipping();
        	mPrinttingAnimationContainer.removeView(mPrinttingAnimationFlipper);
        }

        LayoutInflater inflater = LayoutInflater.from(mActivity);

        // 印刷タイプ
        mPrinttingAnimationFlipper = (ViewFlipper) inflater.inflate(R.layout.flipper_anim_print, null, false);
       
        mPrinttingAnimationContainer.addView(mPrinttingAnimationFlipper);
        mPrinttingAnimationFlipper.setFlipInterval(80);
        mPrinttingAnimationFlipper.startFlipping();

       // mScanningAnimation = type;

    }
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	
        return alertDialog;
    }
    
    
    
    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        getDialog().getWindow().setLayout(dm.widthPixels, dm.heightPixels);
        this.getDialog().setCancelable(false);
    }
    
    public void setPrintStopCallback(BaseOnClickListener stopCallback){
    	stop.setOnClickListener(stopCallback);
    }
    
    public void setStopButtonEnable(boolean isEnable) {
        if(!isCanceled) {
            stop.setEnabled(isEnable);
        }
    }
    
    public void setStopCanceled(){
        isCanceled = true;
    }

}
