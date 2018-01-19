package jp.co.ricoh.advop.idcardscanprint.ui.view;

import android.R.bool;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.application.resource.common.ImageResource;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.model.SettingItemData;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry;



public class SettingShowView extends RelativeLayout {
    private FrameLayout mBtnStart, mBtnSetting, mBtnShow;
    /** スタートキーのふわふわエフェクト画像 */
    private ImageView mImageStartKeyEffect;
    /** スタートキーのふわふわアニメーション */
    private Animation mStartKeyAnim;
    private Context ctx;
    private ImageView imgColorValue, imgFileTypeValue;
    private TextView txtColor, txtFileType, txtDestNum;
    private ImageView[] imgDest = new ImageView[3];
    private TextView[] txtDest = new TextView[3];
    private FrameLayout[] fyDest = new FrameLayout[3];
    
    public SettingShowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_scan_setting_float, this, true);
        this.ctx = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        imgColorValue = (ImageView) findViewById(R.id.icon_value_color);
        txtColor = (TextView) findViewById(R.id.txt_fb_set_color);
        
        imgFileTypeValue = (ImageView) findViewById(R.id.icon_value_filetype);
        txtFileType = (TextView)findViewById(R.id.txt_fb_set_filetype);
        
        // スタートボタン
        ImageView start = (ImageView)findViewById(R.id.img_start);
        start.setImageDrawable(ImageResource.createSelector(start, getResources(), R.raw.bt_start_n, R.raw.bt_start_w));
        
        mBtnStart = (FrameLayout)findViewById(R.id.set_btn_start);
        
        txtDestNum = (TextView)findViewById(R.id.float_txt_dest_totalnum);
        
        imgDest[0] = (ImageView)findViewById(R.id.icon_dest1_type);
        imgDest[1] = (ImageView)findViewById(R.id.icon_dest2_type);
        imgDest[2] = (ImageView)findViewById(R.id.icon_dest3_type);
        
        txtDest[0] = (TextView)findViewById(R.id.txt_dest1_name);
        txtDest[1] = (TextView)findViewById(R.id.txt_dest2_name);
        txtDest[2] = (TextView)findViewById(R.id.txt_dest3_name);
        
        fyDest[0] = (FrameLayout)findViewById(R.id.btn_dest1);
        fyDest[0].setVisibility(View.INVISIBLE);
        
        fyDest[1] = (FrameLayout)findViewById(R.id.btn_dest2);
        fyDest[1].setVisibility(View.INVISIBLE);
        
        fyDest[2] = (FrameLayout)findViewById(R.id.btn_dest3);
        fyDest[2].setVisibility(View.INVISIBLE);
        //スタートキーふわふわアニメーション
        mImageStartKeyEffect = (ImageView) findViewById(R.id.img_startkey_effect);
        mImageStartKeyEffect.setImageDrawable(ImageResource.getImageDrawable(mImageStartKeyEffect, getResources(), R.raw.bt_start_e));
        mStartKeyAnim = AnimationUtils.loadAnimation(ctx, R.anim.startkey_animation);
        mImageStartKeyEffect.startAnimation(mStartKeyAnim);
              
        setScanSetting();
        
        // スキャン条件設定画面へ遷移
        mBtnSetting = (FrameLayout)findViewById(R.id.scan_propery_set);
        mBtnShow = (FrameLayout)findViewById(R.id.scan_property_show);
//        mBtnSetting.setOnClickListener(new BaseOnClickListener() {
//            @Override
//            public void onWork(View v) {
//                //連打の抑制対応
////                if (getUIManager() == null || !getUIManager().isTouchable()) {
////                    return;
////                }
////                getUIManager().setTouchable(false);
////
////                // 入力確認音
////                Buzzer.play(Buzzer.BUZZER_ACK);
////                ParamSet param = new ParamSet();
////                param.put(UIEventId.PARAM_FRAGMENT, ScanSetTopFragment.newInstance(null));
////                param.put(UIEventId.PARAM_FRAGMENT_ANIMATION, FragmentAnimation.ANIMATION_SLIDE_IN);
////                getUIManager().sendUIEvent(new UIEvent(getActivity(), UIEventId.UIEVENT_SHOW_BASE_SETTING, param));
//            }
//        });
    }
    
    /**
     * 現在のスキャン条件を取得し、画面に表示する。<BR>
     */
    public void setScanSetting() {
//        // 本体アプリ情報が取得済み以外の場合、画面表示処理を行わない
//        if ( getUIManager().getAppService() == null || getUIManager().getAppService().getApp().getAppInfoState() != AppInfoState.LOADED) {
//            return;
//        }

        SettingItemData colorValue = PreferencesUtil.getInstance().getSelectedColorValue();
        txtColor.setText(colorValue.getTextId());
        
        imgColorValue.setImageResource(colorValue.getImageId());
        
       
        SettingItemData fileType = PreferencesUtil.getInstance().getSelectedFileFormatValue();
        txtFileType.setText(fileType.getTextId());
       
        imgFileTypeValue.setImageResource(fileType.getImageId());
        
        if(CHolder.instance().getSelects() != null) {
            txtDestNum.setText(CHolder.instance().getSelects().length + "");
            
            Entry[] entryList = CHolder.instance().getSelects();
            
            if(entryList != null && entryList.length != 0) {
              
                for (int i = 0; i < entryList.length && i < 3; i++) {
                    
                    fyDest[i].setVisibility(View.VISIBLE);     
                    String name = entryList[i].getName();
                    if(name != null) {
                        txtDest[i].setText(name);
                    } 
                    if(entryList[i] != null && entryList[i].getMailData() != null && entryList[i].getMailData().getMailAddress() != null) {
                        imgDest[i].setImageResource(R.drawable.icon_service_mail);
                        if(name == null) {
                            txtDest[i].setText(entryList[i].getMailData().getMailAddress());
                         }
                       
                    } else if(entryList[i] != null && entryList[i].getFolderData().getProtocolType() != null) {
                        imgDest[i].setImageResource(R.drawable.icon_service_folder);
                       
                        if(name == null) {
                            txtDest[i].setText(entryList[i].getFolderData().getPath());
                        } 
                    }
                }
            }
        } else {
            txtDestNum.setText("0");
        }
        
        
    }
    

   public void setStartBtnListener(BaseOnClickListener startListener, BaseOnClickListener settingListener) {
       if(startListener != null) {
           mBtnStart.setOnClickListener(startListener);
       }
       
       if(settingListener != null) {
           mBtnSetting.setOnClickListener(settingListener);
           mBtnShow.setOnClickListener(settingListener);
       }
   }
   
   public void setSettingItemDisable() {
       mBtnSetting.setClickable(false);      
       mBtnShow.setClickable(false);
       mBtnShow.setBackground(null);
       mBtnSetting.setVisibility(View.INVISIBLE);
       
   }
   
   public void setPropertySelect(boolean isSelect){
       if(isSelect && !mBtnSetting.isSelected()) {
           mBtnSetting.setSelected(isSelect);
           return;
       } 
       mBtnSetting.setSelected(isSelect);
   }
    
}
