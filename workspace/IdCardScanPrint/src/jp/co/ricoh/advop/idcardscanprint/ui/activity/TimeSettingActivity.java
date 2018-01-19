
package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.model.TimeFormat;
import jp.co.ricoh.advop.idcardscanprint.ui.view.HeaderOkCancel;

import java.util.ArrayList;
import java.util.List;

public class TimeSettingActivity extends BaseActivity {

    private jp.co.ricoh.advop.idcardscanprint.ui.view.HeaderOkCancel headerOkCancel;

    private RelativeLayout StratumContent1;
    private RelativeLayout StratumContent2;
    private RelativeLayout StratumContent3;

    private List<ImageView> ivList;
    private ImageView iv1;
    private ImageView iv2;
    private ImageView iv3;

    private String selecteTX;
    private String tempSelect;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_time_setting);
        init();
        setActionLIstener();
    }

    private void init() {
        
        headerOkCancel = (HeaderOkCancel) findViewById(R.id.init_setting_scan_header);
        headerOkCancel
                .setHeaderTitle(getString(R.string.tx_init_setting_section_print_file_name_rule_time));
        headerOkCancel.setOkBtnText(getString(R.string.bt_ok));
        headerOkCancel.setCancelBtnText(getString(R.string.bt_cancel));

        StratumContent1 = (RelativeLayout) findViewById(R.id.stratum01_layout);
        StratumContent2 = (RelativeLayout) findViewById(R.id.stratum02_layout);
        StratumContent3 = (RelativeLayout) findViewById(R.id.stratum03_layout);

        iv1 = (ImageView) findViewById(R.id.iv_cm_item_lasticon1);
        iv2 = (ImageView) findViewById(R.id.iv_cm_item_lasticon2);
        iv3 = (ImageView) findViewById(R.id.iv_cm_item_lasticon3);
        ivList = new ArrayList<ImageView>();
        ivList.add(iv1);
        ivList.add(iv2);
        ivList.add(iv3);

        setSelect(getIntent().getStringExtra("select"));
    }

    private void setSelect(String selectStr) {

        if (TimeFormat.YYYY_MM_DD_HH_MM_SS.toString().equals(selectStr)) {
            tempSelect = TimeFormat.YYYY_MM_DD_HH_MM_SS.toString();
            iv1.setSelected(true);
            StratumContent1.setSelected(true);
        } else if (TimeFormat.DD_MM_YYYY_HH_MM_SS.toString().equals(selectStr)) {
            tempSelect = TimeFormat.DD_MM_YYYY_HH_MM_SS.toString();
            iv2.setSelected(true);
            StratumContent2.setSelected(true);
        } else if (TimeFormat.MM_DD_YYYY_HH_MM_SS.toString().equals(selectStr)) {
            tempSelect = TimeFormat.MM_DD_YYYY_HH_MM_SS.toString();
            iv3.setSelected(true);
            StratumContent3.setSelected(true);
        } else {
            tempSelect = TimeFormat.YYYY_MM_DD_HH_MM_SS.toString();
            iv1.setSelected(true);
            StratumContent1.setSelected(true);
        }
    }

    private void setActionLIstener() {

        headerOkCancel.setCancelBtnActionListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                
                

                Intent intent = new Intent();
                intent.putExtra("time", tempSelect);
                setResult(2, intent);
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });

        headerOkCancel.setOkBtnActionListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {

                
                
                if (selecteTX == null || selecteTX.trim().length() == 0) {
                    selecteTX = tempSelect;
                }
                
                Intent intent = new Intent();
                intent.putExtra("time", selecteTX);
                setResult(2, intent);
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });

        StratumContent1.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {
                
                
                
                cancelSelect();
                iv1.setSelected(true);
                StratumContent1.setSelected(true);
                selecteTX = TimeFormat.YYYY_MM_DD_HH_MM_SS.toString();
            }
        });

        StratumContent2.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {
                
                
                
                cancelSelect();
                iv2.setSelected(true);
                StratumContent2.setSelected(true);
                selecteTX = TimeFormat.DD_MM_YYYY_HH_MM_SS.toString();
            }
        });

        StratumContent3.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {
                
                
                
                cancelSelect();
                iv3.setSelected(true);
                StratumContent3.setSelected(true);
                selecteTX = TimeFormat.MM_DD_YYYY_HH_MM_SS.toString();
            }
        });
    }

    private void cancelSelect() {
        
        StratumContent1.setSelected(false);
        StratumContent2.setSelected(false);
        StratumContent3.setSelected(false);
        
        for (int i = 0; i < ivList.size(); i++) {
            ivList.get(i).setSelected(false);
        }
    }
}
