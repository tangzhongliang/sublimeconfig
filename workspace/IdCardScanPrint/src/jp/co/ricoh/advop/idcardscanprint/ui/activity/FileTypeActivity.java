
package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.os.Bundle;
import android.view.View;

import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnItemClickListener;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.model.SettingItemData;
import jp.co.ricoh.advop.idcardscanprint.scan.ScanSettingDataHolder;
import jp.co.ricoh.advop.idcardscanprint.util.SettingItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class FileTypeActivity extends BaseActivity {

    private String TAG = FileTypeActivity.class.getSimpleName();
    private TextView headTx;
    private RelativeLayout okBtn;
    private List<SettingItemData> ftList;
    private ListView ftListView;

    private SettingItemAdapter settingItemAdapter;
    
    private ScanSettingDataHolder scanSettingDataHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.view_scan_setting_item);
        init();
        setActionListener();
    }

    private void init() {
        
        setEnableBack();
        
        scanSettingDataHolder = CHolder.instance().getScanManager().getScanSettingDataHolder();
        ftList = new ArrayList<SettingItemData>();

        ftList = scanSettingDataHolder.getFileSettingLabelList();

        headTx = (TextView) findViewById(R.id.tx_cm_header);
        headTx.setText(getString(R.string.btn_init_setting_section_scan_file_type));
        okBtn = (RelativeLayout) findViewById(R.id.btn_cm_ok);

        settingItemAdapter = new SettingItemAdapter(FileTypeActivity.this, ftList);
        settingItemAdapter.setSelect(getSelect());
        ftListView = (ListView) findViewById(R.id.smtp_authentication_group_id);
        ftListView.setAdapter(settingItemAdapter);
    }

    private void setActionListener() {

        okBtn.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {
                
                
                
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });

        ftListView.setOnItemClickListener(new BaseOnItemClickListener() {

            @Override
            public void onWork(AdapterView<?> arg0, View view, int position, long arg3) {
                
                
                
                scanSettingDataHolder.setSelectedFileSetting(ftList.get(position).getTextId());
                settingItemAdapter.setSelect(position);
                settingItemAdapter.notifyDataSetChanged();
                
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });
    }

    private int getSelect() {

        for (int i = 0; i < ftList.size(); i++) {
            if (getString(ftList.get(i).getTextId())
                    .equals(getIntent().getStringExtra("select"))) {
                return i;
            }
        }
        return 0;
    }
}
