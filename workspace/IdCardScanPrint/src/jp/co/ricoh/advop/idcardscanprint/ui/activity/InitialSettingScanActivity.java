
package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.model.ScanDataSetting;
import jp.co.ricoh.advop.idcardscanprint.model.SettingItemData;
import jp.co.ricoh.advop.idcardscanprint.scan.ScanSettingDataHolder;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.scan.attribute.standard.ScanColor;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.BaseActivity;
import jp.co.ricoh.advop.idcardscanprint.ui.view.HeaderOkCancel;

public class InitialSettingScanActivity extends BaseActivity {

    private String TAG = InitialSettingScanActivity.class.getSimpleName();
    private jp.co.ricoh.advop.idcardscanprint.ui.view.HeaderOkCancel headerOkCancel;
    private RelativeLayout resetBtn;

    private RelativeLayout colorMode;
    private RelativeLayout fileType;

    private TextView cmTx01;
    private TextView cmTx02;

    /**
     * show color mode text
     */
    private TextView cmTx;

    private ImageView cmIm;

    /**
     * show file type text
     */
    private TextView ftTx;

    private ImageView ftIm;

    private ScanSettingDataHolder scanSettingDataHolder;

    private PreferencesUtil preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.initial_setting_scan);
        init();
        setActionListener();
    }

    private void init() {
        
        preferences = CHolder.instance().getApplication().getPreferences();
        scanSettingDataHolder = CHolder.instance().getScanManager().getScanSettingDataHolder();

        headerOkCancel = (HeaderOkCancel) findViewById(R.id.init_setting_scan_header);
        headerOkCancel.setHeaderTitle(getString(R.string.tx_init_setting_scan));
        headerOkCancel.setOkBtnText(getString(R.string.bt_ok));
        headerOkCancel.setCancelBtnText(getString(R.string.bt_cancel));

        cmTx01 = (TextView) findViewById(R.id.tx_init_setting_scan_first_itemone);
        cmTx01.setText(getString(R.string.btn_init_setting_section_scan_color_mode));
        cmTx02 = (TextView) findViewById(R.id.tx_init_setting_scan_second_itemone);
        cmTx02.setText(getString(R.string.btn_init_setting_section_scan_file_type));

        resetBtn = (RelativeLayout) findViewById(R.id.init_setting_scan_reset_btn);
        colorMode = (RelativeLayout) findViewById(R.id.init_setting_scan_color_mode);
        fileType = (RelativeLayout) findViewById(R.id.init_setting_scan_file_type);
        cmTx = (TextView) findViewById(R.id.tx_init_setting_scan_cm);
        cmIm = (ImageView) findViewById(R.id.image_init_setting_scan_cm);
        ftTx = (TextView) findViewById(R.id.tx_init_setting_scan_ft);
        ftIm = (ImageView) findViewById(R.id.image_init_setting_scan_ft);

        SettingItemData cmData = scanSettingDataHolder.getSelectedColorValue();
        SettingItemData ftData = scanSettingDataHolder.getSelectedFileFormatValue();
        if (cmData != null) {
            cmTx.setText(getString(cmData.getTextId()));
            cmIm.setImageDrawable(getResources().getDrawable(cmData.getImageId()));
        }
        if (ftData != null) {
            ftTx.setText(getString(ftData.getTextId()));
            ftIm.setImageDrawable(getResources().getDrawable(ftData.getImageId()));
        }

        scanSettingDataHolder.setTempSelectedColorLabel(scanSettingDataHolder
                .getSelectedColorLabel());
        scanSettingDataHolder.setTempSelectedFileSettingLabel(scanSettingDataHolder
                .getSelectedFileSettingLabel());
    }

    private void setActionListener() {

        headerOkCancel.setCancelBtnActionListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                
                
                
                scanSettingDataHolder.setSelectedColor(scanSettingDataHolder
                        .getTempSelectedColorLabel());
                scanSettingDataHolder.setSelectedFileSetting(scanSettingDataHolder
                        .getTempSelectedFileSettingLabel());
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }

        });

        headerOkCancel.setOkBtnActionListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {

                
                
                ScanColor scanColor = (ScanColor) scanSettingDataHolder.getSelectedColorValue()
                        .getItemValue();
                String fileFormat = (String) scanSettingDataHolder
                        .getSelectedFileFormatValue()
                        .getItemValue();

                ScanDataSetting scanDataSetting = new ScanDataSetting(
                        scanColor, fileFormat)
                ;
                preferences.setScanDataSetting(scanDataSetting);
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });

        resetBtn.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {
                
                
                
                scanSettingDataHolder.setSelectedColor(scanSettingDataHolder
                        .getDefaultSupportedColor());
                scanSettingDataHolder.setSelectedFileSetting(scanSettingDataHolder
                        .getDefaultSupportedFileTyp());
                // scanSettingDataHolder.setSelectedColor(R.string.txid_scan_b_top_auto_color_select);
                // scanSettingDataHolder.setSelectedFileSetting(R.string.txid_scan_b_top_file_pdf);

                SettingItemData cmData = scanSettingDataHolder.getSelectedColorValue();
                SettingItemData ftData = scanSettingDataHolder.getSelectedFileFormatValue();
                if (cmData != null) {
                    cmTx.setText(getString(cmData.getTextId()));
                    cmIm.setImageDrawable(getResources().getDrawable(cmData.getImageId()));
                }
                if (ftData != null) {
                    ftTx.setText(getString(ftData.getTextId()));
                    ftIm.setImageDrawable(getResources().getDrawable(ftData.getImageId()));
                }

            }
        });
        colorMode.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {
                
                
                
                Intent intent = new Intent(InitialSettingScanActivity.this, ColorModeActivity.class);
                intent.putExtra("select", cmTx.getText().toString());
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        fileType.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {
                
                
                
                Intent intent = new Intent(InitialSettingScanActivity.this, FileTypeActivity.class);
                intent.putExtra("select", ftTx.getText().toString());
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
    }

    @Override
    public void onRestart() {

        super.onRestart();
        SettingItemData cmData = scanSettingDataHolder.getSelectedColorValue();
        SettingItemData ftData = scanSettingDataHolder.getSelectedFileFormatValue();
        if(cmData != null) {
            cmTx.setText(getString(cmData.getTextId()));
            cmIm.setImageDrawable(getResources().getDrawable(cmData.getImageId()));
        }
        if(ftData != null) {
            ftTx.setText(getString(ftData.getTextId()));
            ftIm.setImageDrawable(getResources().getDrawable(ftData.getImageId()));
        }
    }
}
