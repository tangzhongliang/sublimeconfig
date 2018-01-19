
package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;


import jp.co.ricoh.advop.cheetahutil.util.Buzzer;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.model.GlobalDataManager.FragmentState;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.model.PrintDataSetting;
import jp.co.ricoh.advop.idcardscanprint.model.ScanDataSetting;
import jp.co.ricoh.advop.idcardscanprint.print.PrintSettingDataHolder;
import jp.co.ricoh.advop.idcardscanprint.scan.ScanManager.ScanJobContinueCB;
import jp.co.ricoh.advop.idcardscanprint.scan.ScanSettingDataHolder;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrintColor;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScanColor;
import jp.co.ricoh.advop.idcardscanprint.ui.fragment.PreviewFragment;
import jp.co.ricoh.advop.idcardscanprint.ui.fragment.ScanBackFragment;
import jp.co.ricoh.advop.idcardscanprint.ui.fragment.ScanFrontFragment;
import jp.co.ricoh.advop.idcardscanprint.ui.fragment.SenderSelectFragment;
import jp.co.ricoh.advop.idcardscanprint.util.Const;
import jp.co.ricoh.advop.idcardscanprint.util.Util;

public class IdCardScanPrintActivity extends BaseActivity {
    private ImageView[] imageButtons;
    private TextView[] textButtons;

    //private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    private Button btBack, btNext;
    private TextView tvBack, tvNext;
    private SenderSelectFragment senderSelFrag;// = new SenderSelectFragment();
    // private ScanFrontFragment scanFrontFrag = new ScanFrontFragment();
    // private ScanBackFragment scanBackFrag = new ScanBackFragment();
    // private PreviewFragment previewImageFrag= null;//new PreviewFragment();
    private BaseOnClickListener backListener, nextListener;

    private PrintSettingDataHolder printSettingDataHolder;
    private ScanSettingDataHolder scanSettingDataHolder;

	private final int[] time = Util.getTime();    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.act_main);

        initView();
        setDefaultFragment();
        scanManager.getScanAuthInfo(this, new ScanJobContinueCB() {
            
            @Override
            public void scanJobContinue() {
                LogC.d("auth scan user code is success");
            }
        });
        setEnableBack();
    }

    private void initView() {

        // fragments.add(senderSelFrag);
        // fragments.add(scanFrontFrag);
        // fragments.add(scanBackFrag);
        // fragments.add(previewImageFrag);

        imageButtons = new ImageView[4];
        imageButtons[0] = (ImageView) findViewById(R.id.tle_img_step1);
        imageButtons[1] = (ImageView) findViewById(R.id.tle_img_step2);
        imageButtons[2] = (ImageView) findViewById(R.id.tle_img_step3);
        imageButtons[3] = (ImageView) findViewById(R.id.tle_img_step4);
        // imageButtons[0].setSelected(true);

        textButtons = new TextView[4];
        textButtons[0] = (TextView) findViewById(R.id.tle_txt_step1);
        textButtons[1] = (TextView) findViewById(R.id.tle_txt_step2);
        textButtons[2] = (TextView) findViewById(R.id.tle_txt_step3);
        textButtons[3] = (TextView) findViewById(R.id.tle_txt_step4);
        // textButtons[0].setSelected(true);

        // getSupportFragmentManager().beginTransaction()
        // transaction.add(R.id.fragment_container, senderSelFrag)
        // .add(R.id.fragment_container, scanFrontFrag)
        // .add(R.id.fragment_container, scanBackFrag)
        // //.add(R.id.fragment_container, previewImageFrag)
        // .hide(scanFrontFrag).hide(scanBackFrag)//.hide(previewImageFrag)
        // .show(senderSelFrag).commit();

        btBack = (Button) findViewById(R.id.main_btn_back);
        tvBack = (TextView) findViewById(R.id.main_txt_back);
        btNext = (Button) findViewById(R.id.main_btn_next);
        tvNext = (TextView) findViewById(R.id.main_txt_next);

        btBack.setVisibility(View.GONE);
        tvBack.setVisibility(View.GONE);
        btNext.setVisibility(View.GONE);
        tvNext.setVisibility(View.GONE);

        initListener();

        btBack.setOnClickListener(backListener);
        tvBack.setOnClickListener(backListener);
        btNext.setOnClickListener(nextListener);
        tvNext.setOnClickListener(nextListener);

        imageButtons[0].setImageResource(R.drawable.icon_wizardstep_01_w);
        imageButtons[1].setImageResource(R.drawable.icon_wizardstep_02_n);
        imageButtons[2].setImageResource(R.drawable.icon_wizardstep_03_n);
        imageButtons[3].setImageResource(R.drawable.icon_wizardstep_04_n);

        textButtons[0].setVisibility(View.VISIBLE);
        textButtons[1].setVisibility(View.GONE);
        textButtons[2].setVisibility(View.GONE);
        textButtons[3].setVisibility(View.GONE);

        CHolder.instance().getGlobalDataManager().setState(FragmentState.SenderSelect);

        initPrintDefaultValue();
        initScanDefaultValue();
        initRuleFileName();
    }

    private void initPrintDefaultValue() {
        // setting print default value
        printSettingDataHolder = CHolder.instance().getPrintManager().getPrintSettingDataHolder();
        PrintDataSetting printDataSetting = PreferencesUtil.getInstance().getPrintDataSetting();
        if (printDataSetting != null) {
            PrintColor printColor = printDataSetting.getPrintColor();
            int printCount = printDataSetting.getPrintCount();
            printSettingDataHolder.setSelectedPrintColor(printSettingDataHolder
                    .getColorFromEnum(printColor));
            printSettingDataHolder.setSelectedPrintPage(printCount);
        } else {
            printSettingDataHolder.setSelectedPrintColor(printSettingDataHolder
                    .getDefaultSupportedColor());
            printSettingDataHolder.setSelectedPrintPage(1);
        }
    }

    private void initScanDefaultValue() {
        // setting scan default value
        scanSettingDataHolder = CHolder.instance().getScanManager().getScanSettingDataHolder();
        ScanDataSetting scanDataSetting = PreferencesUtil.getInstance().getScanDataSetting();
        if (scanDataSetting != null) {
            ScanColor scanColor = scanDataSetting.getScanColor();
            String scanFileFormat = scanDataSetting.getScanFileType();
            scanSettingDataHolder.setSelectedColor(scanSettingDataHolder
                    .getColorFromEnum(scanColor));
            scanSettingDataHolder.setSelectedFileSetting(scanSettingDataHolder
                    .getFileFormatFromString(scanFileFormat));
        } else {
            scanSettingDataHolder
                    .setSelectedColor(scanSettingDataHolder.getDefaultSupportedColor());
            scanSettingDataHolder.setSelectedFileSetting(scanSettingDataHolder
                    .getDefaultSupportedFileTyp());
        }
    }

    private void initRuleFileName() {
        String separator = PreferencesUtil.getInstance().getSeparatorChar();
        List<Map<String, Object>> fileNameArray = PreferencesUtil.getInstance().getRuleFileName();
        if (fileNameArray == null) {
            fileNameArray = Const.DefaultFileNameList;
        }

        String fileName = Util.replaceFileName(separator, fileNameArray, time);
        Util.setFileName(fileName);
    }

    private void initListener() {
        backListener = new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                
                backFragment();
            }
        };

        nextListener = new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                
                FragmentState current = CHolder.instance().getGlobalDataManager().getState();
                FragmentState nextState = current;
                switch (current) {
                    case SenderSelect:
                        nextState = FragmentState.ScanSide1;
                        imageButtons[0].setImageResource(R.drawable.icon_wizardstep_01_n);
                        imageButtons[1].setImageResource(R.drawable.icon_wizardstep_02_w);
                        getFragmentManager()
                                .beginTransaction()
                                .setCustomAnimations(R.animator.fragment_slide_left_enter,
                                        R.animator.fragment_slide_left_exit)
                                .replace(R.id.fragment_container, new ScanFrontFragment()).commitAllowingStateLoss();
                        break;
                    case ScanSide1:

                        break;
                    case ScanSide2:

                        break;
                    case ImagePreview:

                        break;
                    default:
                        break;
                }
                updateScreen(current, nextState);

            }
        };
    }

    public boolean backFragment() {
        FragmentState current = CHolder.instance().getGlobalDataManager().getState();
        FragmentState nextState = current;
        boolean isSenderSelect = false;
        switch (current) {
            case SenderSelect:
                isSenderSelect = true;
                break;
            case ScanSide1:
                nextState = FragmentState.SenderSelect;
                imageButtons[current.ordinal()].setImageResource(R.drawable.icon_wizardstep_02_n);
                imageButtons[nextState.ordinal()].setImageResource(R.drawable.icon_wizardstep_01_w);
                getFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit)
                        .replace(R.id.fragment_container, new SenderSelectFragment()).commitAllowingStateLoss();
                break;
            case ScanSide2:
                nextState = FragmentState.ScanSide1;
                imageButtons[current.ordinal()].setImageResource(R.drawable.icon_wizardstep_03_n);
                imageButtons[nextState.ordinal()].setImageResource(R.drawable.icon_wizardstep_02_w);
                getFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit)
                        .replace(R.id.fragment_container, new ScanFrontFragment()).commitAllowingStateLoss();
                break;
            case ImagePreview:
                nextState = FragmentState.ScanSide2;
                imageButtons[current.ordinal()].setImageResource(R.drawable.icon_wizardstep_04_n);
                imageButtons[nextState.ordinal()].setImageResource(R.drawable.icon_wizardstep_03_w);
                getFragmentManager().beginTransaction().setCustomAnimations(R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit)
                        .replace(R.id.fragment_container, new ScanBackFragment()).commitAllowingStateLoss();
                // fragments.remove(previewImageFrag);
                // transaction = fm.beginTransaction();
                //
                // transaction.remove(previewImageFrag).commit();
                // previewImageFrag = null;
                break;
            default:
                break;
        }
        updateScreen(current, nextState);

        return isSenderSelect;
    }

    private void setDefaultFragment()
    {
        senderSelFrag = new SenderSelectFragment();
        setNextEnable(false, (float)0.3);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, senderSelFrag).commitAllowingStateLoss();

    }

    private void updateScreen(FragmentState current, FragmentState nextState) {
        if (current != nextState) {
            CHolder.instance().getGlobalDataManager().setState(nextState);
            textButtons[current.ordinal()].setVisibility(View.GONE);
            textButtons[nextState.ordinal()].setVisibility(View.VISIBLE);

            switch (nextState) {
                case SenderSelect:
                    btBack.setVisibility(View.GONE);
                    tvBack.setVisibility(View.GONE);
                    if(CHolder.instance().getApplication().getSystemStateMonitor().isMachineAdmin()) {
                        setNextVisble(View.INVISIBLE);            
                    } else {
                        setNextVisble(View.VISIBLE);  
                    }
//                    btNext.setVisibility(View.VISIBLE);
//                    tvNext.setVisibility(View.VISIBLE);

                    break;
                case ScanSide1:
                    btBack.setVisibility(View.VISIBLE);
                    tvBack.setVisibility(View.VISIBLE);
                    btNext.setVisibility(View.GONE);
                    tvNext.setVisibility(View.GONE);
                    break;
                case ScanSide2:
                    btBack.setVisibility(View.VISIBLE);
                    tvBack.setVisibility(View.VISIBLE);
                    btNext.setVisibility(View.GONE);
                    tvNext.setVisibility(View.GONE);
                    break;
                case ImagePreview:
                    btBack.setVisibility(View.GONE);
                    tvBack.setVisibility(View.GONE);
                    btNext.setVisibility(View.GONE);
                    tvNext.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }

    }

    public void previewToFirstScan() {
        imageButtons[1].setImageResource(R.drawable.icon_wizardstep_02_w);
        imageButtons[3].setImageResource(R.drawable.icon_wizardstep_04_n);
        updateScreen(FragmentState.ImagePreview, FragmentState.ScanSide1);
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit)
                .replace(R.id.fragment_container, new ScanFrontFragment()).commitAllowingStateLoss();
    }

    public void goToNextScan() {
        imageButtons[1].setImageResource(R.drawable.icon_wizardstep_02_n);
        imageButtons[2].setImageResource(R.drawable.icon_wizardstep_03_w);
        updateScreen(FragmentState.ScanSide1, FragmentState.ScanSide2);
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit)
                .replace(R.id.fragment_container, new ScanBackFragment()).commitAllowingStateLoss();
    }

    public void goToPreview() {
        imageButtons[2].setImageResource(R.drawable.icon_wizardstep_03_n);
        imageButtons[3].setImageResource(R.drawable.icon_wizardstep_04_w);
        updateScreen(FragmentState.ScanSide2, FragmentState.ImagePreview);
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit)
                .replace(R.id.fragment_container, new PreviewFragment()).commitAllowingStateLoss();
    }
    
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            
            if (backFragment()) {
//                SplashActivity act = CHolder.instance().getGlobalDataManager().getsActivity();
//                if (act != null) {
//                    CHolder.instance().getGlobalDataManager().getsActivity().finish();
//                    IdCardScanPrintActivity.this.finish();
//                }
                Buzzer.onBuzzer(Buzzer.BUZZER_NACK);
            } else {
                Buzzer.play();
            }
 //           CHolder.instance().setEntries(null);
//            CHolder.instance().setLinkedSelects(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void setNextEnable(boolean enable, float value){
        btNext.setEnabled(enable);
        tvNext.setEnabled(enable);
        btNext.setAlpha(value);
        tvNext.setAlpha(value);
    }
    
    public void setNextVisble(int isVisble){
        btNext.setVisibility(isVisble);
        tvNext.setVisibility(isVisble);       
    }
    
 
    @Override
    protected void onDestroy() {        
        super.onDestroy();
        LogC.d("onDestroy");
        CHolder.instance().setEntries(null);
        CHolder.instance().setLinkedSelects(null);
    }
}
