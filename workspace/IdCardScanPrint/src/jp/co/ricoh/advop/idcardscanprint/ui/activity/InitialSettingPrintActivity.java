
package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.widget.MultiButtonDialog;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.model.PrintDataSetting;
import jp.co.ricoh.advop.idcardscanprint.model.SettingItemData;
import jp.co.ricoh.advop.idcardscanprint.print.PrintSettingDataHolder;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.attribute.standard.PrintColor;
import jp.co.ricoh.advop.idcardscanprint.ssdk.function.print.supported.MaxMinSupported;
import jp.co.ricoh.advop.idcardscanprint.ui.view.HeaderOkCancel;

public class InitialSettingPrintActivity extends BaseActivity {

    private final String TAG = InitialSettingPrintActivity.class.getSimpleName();
    private HeaderOkCancel headerOkCancel;
    private RelativeLayout resetBtn;

    private RelativeLayout printColor;
    private RelativeLayout printCount;

    private TextView pColorTx01;
    private TextView pCountTx02;

    /**
     * show color mode text
     */
    private TextView pColorTx;

    private ImageView pColorIm;

    /**
     * show file type text
     */
    private EditText pCountTx;
    private static String pCountStr;

    private PreferencesUtil preferences;

    private PrintSettingDataHolder printSettingDataHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.initial_setting_print);
        init();
        setActionListener();
    }

    private void init() {
        
        preferences = CHolder.instance().getApplication().getPreferences();
        printSettingDataHolder =
                CHolder.instance().getPrintManager().getPrintSettingDataHolder();

        headerOkCancel = (HeaderOkCancel) findViewById(R.id.init_setting_print_header);
        headerOkCancel.setHeaderTitle(getString(R.string.tx_init_setting_print));
        headerOkCancel.setOkBtnText(getString(R.string.bt_ok));
        headerOkCancel.setCancelBtnText(getString(R.string.bt_cancel));

        resetBtn = (RelativeLayout) findViewById(R.id.init_setting_print_reset_btn);

        printColor = (RelativeLayout) findViewById(R.id.init_setting_print_color_mode);
        printCount = (RelativeLayout) findViewById(R.id.init_setting_print_file_type);

        pColorTx01 = (TextView) findViewById(R.id.tx_init_setting_print_first_itemone);
        pColorTx01.setText(getString(R.string.btn_init_setting_section_print_color));
        pCountTx02 = (TextView) findViewById(R.id.tx_init_setting_print_second_itemone);
        pCountTx02.setText(getString(R.string.btn_init_setting_section_print_count));

        pColorTx = (TextView) findViewById(R.id.tx_init_setting_print_cm);
        pColorIm = (ImageView) findViewById(R.id.image_init_setting_print_cm);
        pCountTx = (EditText) findViewById(R.id.tx_init_setting_print_ft);
        pCountTx.setInputType(EditorInfo.TYPE_CLASS_PHONE);

        SettingItemData cmData =
                printSettingDataHolder.getSelectedPrintColorValue();

        if (cmData != null) {
            pColorTx.setText(getString(cmData.getTextId()));
            pColorIm.setImageDrawable(getResources().getDrawable(cmData.getImageId()));
        }
        pCountTx.setText(printSettingDataHolder.getSelectedPrintPage() + "");

        printSettingDataHolder.setTempSelectedPrintColor((printSettingDataHolder
                .getSelectedPrintColor()));
        printSettingDataHolder.setTempSelectedPrintPage(printSettingDataHolder
                .getSelectedPrintPage());

        setupKeyboardHide(printCount);
        printCount.requestFocus();
    }

    private void setActionListener() {
        /**
         * set cancel button action listener
         */
        headerOkCancel.setCancelBtnActionListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {
                
                
                
                printSettingDataHolder.setSelectedPrintColor(printSettingDataHolder
                        .getTempSelectedPrintColor());
                printSettingDataHolder.setSelectedPrintPage(printSettingDataHolder
                        .getTempSelectedPrintPage());
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });

        /**
         * set ok button action listener
         */
        headerOkCancel.setOkBtnActionListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {

                
                
                MaxMinSupported mmSupported = printSettingDataHolder.getMmSupported();
                int minValue = 0;
                int maxValue = 999;
                if (mmSupported != null) {
                    minValue = Integer.parseInt(mmSupported.getMinValue());
                    maxValue = Integer.parseInt(mmSupported.getMaxValue());
                }

                try {
                    Integer printCount = Integer.parseInt(pCountTx.getText().toString());

                    if (printCount >= minValue && printCount <= maxValue) {
                        printSettingDataHolder.setSelectedPrintPage(Integer.parseInt(pCountTx
                                .getText()
                                .toString()));
                        PrintDataSetting printDataSetting = new PrintDataSetting(
                                (PrintColor) printSettingDataHolder.getSelectedPrintColorValue()
                                        .getItemValue(), printCount);
                        preferences.setPrintDataSetting(printDataSetting);

                        finish();
                        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                    } else {
//                        String msg = String.format(
//                                getString(R.string.dialog_print_count_not_in_range),
//                                pCountTx02.getText().toString(), minValue, maxValue);
                        String msg = String.format(
                                getString(R.string.dialog_print_count_not_in_range), minValue, maxValue);
                        MultiButtonDialog msgDialog = MultiButtonDialog.createInputCheckDialog(
                                InitialSettingPrintActivity.this, msg, pCountTx);
                        msgDialog.show();
                    }
                } catch (Exception e) {
//                    String msg =
//                            String.format(getString(R.string.dialog_print_count_not_in_range),
//                                    pCountTx02.getText().toString(), minValue, maxValue
//                                    );
                    String msg = String.format(getString(R.string.dialog_print_count_not_in_range), minValue, maxValue);
                    MultiButtonDialog msgDialog =
                            MultiButtonDialog.createInputCheckDialog(
                                    InitialSettingPrintActivity.this, msg
                                    , pCountTx);
                    msgDialog.show();
                }

            }
        });

        resetBtn.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {

                
                
                printSettingDataHolder
                        .setSelectedPrintColor(printSettingDataHolder.getDefaultSupportedColor());
                printSettingDataHolder.setSelectedPrintPage(1);

                SettingItemData pColorData = printSettingDataHolder.getSelectedPrintColorValue();
                int pCount = printSettingDataHolder.getSelectedPrintPage();
                if(pColorData != null) {
                    pColorTx.setText(pColorData.getTextId());
                    pColorIm.setImageDrawable(getResources().getDrawable(pColorData.getImageId()));
                }
                pCountTx.setText(pCount + "");
            }
        });

        printColor.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {

                
                
                pCountStr = pCountTx.getText().toString();

                Intent intent = new Intent(InitialSettingPrintActivity.this,
                        PrintColorActivity.class);
                intent.putExtra("select", pColorTx.getText().toString());
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

    }

    @Override
    public void onRestart() {
        
        printCount.requestFocus();
        
        super.onRestart();

        SettingItemData pColorData = printSettingDataHolder.getSelectedPrintColorValue();
        if (pColorData != null) {
            pColorTx.setText(getString(pColorData.getTextId()));
            pColorIm.setImageDrawable(getResources().getDrawable(pColorData.getImageId()));
        }
        pCountTx.setText(pCountStr);
    }
}
