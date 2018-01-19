
package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;


import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.model.RuleFileNameType;
import jp.co.ricoh.advop.idcardscanprint.ui.view.HeaderOkCancel;
import jp.co.ricoh.advop.idcardscanprint.util.Const;
import jp.co.ricoh.advop.idcardscanprint.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitialSettingRuleFileNameActivity extends BaseActivity {

    private final String TAG = InitialSettingRuleFileNameActivity.class.getSimpleName();
    private jp.co.ricoh.advop.idcardscanprint.ui.view.HeaderOkCancel headerOkCancel;

    private TextView txFileName;
    private TextView ruleFileName;
    private RelativeLayout resetBtn;

    private RelativeLayout stratum1Layout;
    private RelativeLayout stratum2Layout;
    private RelativeLayout stratum3Layout;
    private RelativeLayout stratum4Layout;
    private RelativeLayout stratum5Layout;
    private RelativeLayout stratum6Layout;

    private TextView stratum1Tx;
    private TextView stratum2Tx;
    private TextView stratum3Tx;
    private TextView stratum4Tx;
    private TextView stratum5Tx;
    private TextView stratum6Tx;
    private List<TextView> txList;

    private Button separator1;
    private Button separator2;
    private Button separator3;

    private int[] idStratum;
    private List<Map<String, Object>> fileNameArray;
    private String fileName;
    private String separatorC;

    private final int[] time2 = Util.getTime();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.initial_setting_rule_file_name);
        init();
        setActionListener();
    }

    private void init() {

        // get file name from local;
        fileNameArray = PreferencesUtil.getInstance().getRuleFileName();
        if (fileNameArray == null) {
            fileNameArray = Const.DefaultFileNameList;
        }

        headerOkCancel = (HeaderOkCancel) findViewById(R.id.init_filename_rule_header);
        headerOkCancel.setHeaderTitle(getString(Const.FILENAMESETTING));
        headerOkCancel.setOkBtnText(getString(R.string.bt_ok));
        headerOkCancel.setCancelBtnText(getString(R.string.bt_cancel));

        txFileName = (TextView) findViewById(R.id.tx_file_name);
        ruleFileName = (TextView) findViewById(R.id.tx_rule_file_name);
        separatorC = PreferencesUtil.getInstance().getSeparatorChar();

        resetBtn = (RelativeLayout) findViewById(R.id.init_filename_rule_reset_btn);

        stratum1Layout = (RelativeLayout) findViewById(R.id.stratum01_layout);
        stratum2Layout = (RelativeLayout) findViewById(R.id.stratum02_layout);
        stratum3Layout = (RelativeLayout) findViewById(R.id.stratum03_layout);
        stratum4Layout = (RelativeLayout) findViewById(R.id.stratum04_layout);
        stratum5Layout = (RelativeLayout) findViewById(R.id.stratum05_layout);
        stratum6Layout = (RelativeLayout) findViewById(R.id.stratum06_layout);

        idStratum = new int[] {
                R.id.stratum01_tx, R.id.stratum02_tx, R.id.stratum03_tx, R.id.stratum04_tx,
                R.id.stratum05_tx, R.id.stratum06_tx
        };
        stratum1Tx = (TextView) findViewById(idStratum[0]);
        stratum2Tx = (TextView) findViewById(idStratum[1]);
        stratum3Tx = (TextView) findViewById(idStratum[2]);
        stratum4Tx = (TextView) findViewById(idStratum[3]);
        stratum5Tx = (TextView) findViewById(idStratum[4]);
        stratum6Tx = (TextView) findViewById(idStratum[5]);
        txList = new ArrayList<TextView>();
        txList.add(stratum1Tx);
        txList.add(stratum2Tx);
        txList.add(stratum3Tx);
        txList.add(stratum4Tx);
        txList.add(stratum5Tx);
        txList.add(stratum6Tx);

        for (int i = 0; i < idStratum.length; i++) {
            TextView tx = txList.get(i);
            Map<String, Object> map = fileNameArray.get(i);
            String type = null;
            if (map.get(Const.KEY_TYPE) != null) {
                type = map.get(Const.KEY_TYPE).toString();
            }
            if (!CUtil.isStringEmpty(type)) {
                tx.setText(Util.gainRuleFileName(type));
            } else {
                tx.setText(getString(Const.SETTING_NOTHING));
            }
        }

        separator1 = (Button) findViewById(R.id.Separator1);
        separator2 = (Button) findViewById(R.id.Separator2);
        separator3 = (Button) findViewById(R.id.Separator3);

        if (".".equals(separatorC)) {
            replaceFileName(separatorC);
            separator1.setSelected(true);
        } else if ("_".equals(separatorC)) {
            replaceFileName(separatorC);
            separator2.setSelected(true);
        } else if ("-".equals(separatorC)) {
            replaceFileName(separatorC);
            separator3.setSelected(true);
        } else {
            separatorC = ".";
            replaceFileName(".");
            separator1.setSelected(true);
        }
    }

    private void setActionListener() {

        headerOkCancel.setCancelBtnActionListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {

                

                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }

        });

        headerOkCancel.setOkBtnActionListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {

                

                Util.setFileName(fileName);
                if (PreferencesUtil.getInstance() != null) {
                    PreferencesUtil.getInstance().setRuleFileName(fileNameArray);
                }
                PreferencesUtil.getInstance().setSeparatorChar(separatorC);
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });

        resetBtn.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {

                

                Map<String, Object> map = new HashMap<String, Object>();
                map.put(Const.KEY_TYPE, null);
                map.put(Const.KEY_VALUE, null);

                fileNameArray = new ArrayList<Map<String, Object>>();
                for (int i = 0; i < 6; i++) {
                    fileNameArray.add(map);
                }

                for (int i = 0; i < idStratum.length; i++) {
                    Map<String, Object> map2 = fileNameArray.get(i);
                    if (map2.get(Const.KEY_TYPE) != null) {
                        txList.get(i).setText(
                                Util.gainRuleFileName(map2.get(Const.KEY_TYPE).toString()));
                    } else {
                        txList.get(i).setText(
                                Util.gainRuleFileName(RuleFileNameType.SETTING_NIL.toString()));
                    }
                }

                separatorC = ".";
                cancelSelect();
                separator1.setSelected(true);
                clearLabel();
            }
        });

        stratum1Layout.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {

                

                Intent intent = new Intent(InitialSettingRuleFileNameActivity.this,
                        StratumContentActivity.class);
                String title = getString(Const.FILENAMESETTING) + getString(Const.STRATUM1);
                intent.putExtra("title", title);
                intent.putExtra("which", "1");
                Map<String, Object> map = fileNameArray.get(0);
                String type = null;
                if (map.get(Const.KEY_TYPE) != null) {
                    type = map.get(Const.KEY_TYPE).toString();
                }
                String value = (String) map.get(Const.KEY_VALUE);
                intent.putExtra(Const.KEY_TYPE, type);
                intent.putExtra(Const.KEY_VALUE, value);
                startActivityForResult(intent, 1);
            }
        });

        stratum2Layout.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {

                

                Intent intent = new Intent(InitialSettingRuleFileNameActivity.this,
                        StratumContentActivity.class);
                String title = getString(Const.FILENAMESETTING) + getString(Const.STRATUM2);
                intent.putExtra("title", title);
                intent.putExtra("which", "2");
                Map<String, Object> map = fileNameArray.get(1);
                String type = null;
                if (map.get(Const.KEY_TYPE) != null) {
                    type = map.get(Const.KEY_TYPE).toString();
                }
                String value = (String) map.get(Const.KEY_VALUE);
                intent.putExtra(Const.KEY_TYPE, type);
                intent.putExtra(Const.KEY_VALUE, value);
                startActivityForResult(intent, 1);
            }
        });

        stratum3Layout.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {

                

                Intent intent = new Intent(InitialSettingRuleFileNameActivity.this,
                        StratumContentActivity.class);
                String title = getString(Const.FILENAMESETTING) + getString(Const.STRATUM3);
                intent.putExtra("title", title);
                intent.putExtra("which", "3");
                Map<String, Object> map = fileNameArray.get(2);
                String type = null;
                if (map.get(Const.KEY_TYPE) != null) {
                    type = map.get(Const.KEY_TYPE).toString();
                }
                String value = (String) map.get(Const.KEY_VALUE);
                intent.putExtra(Const.KEY_TYPE, type);
                intent.putExtra(Const.KEY_VALUE, value);
                startActivityForResult(intent, 1);
            }
        });

        stratum4Layout.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {

                

                Intent intent = new Intent(InitialSettingRuleFileNameActivity.this,
                        StratumContentActivity.class);
                String title = getString(Const.FILENAMESETTING) + getString(Const.STRATUM4);
                intent.putExtra("title", title);
                intent.putExtra("which", "4");
                Map<String, Object> map = fileNameArray.get(3);
                String type = null;
                if (map.get(Const.KEY_TYPE) != null) {
                    type = map.get(Const.KEY_TYPE).toString();
                }
                String value = (String) map.get(Const.KEY_VALUE);
                intent.putExtra(Const.KEY_TYPE, type);
                intent.putExtra(Const.KEY_VALUE, value);
                startActivityForResult(intent, 1);
            }
        });

        stratum5Layout.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {

                

                Intent intent = new Intent(InitialSettingRuleFileNameActivity.this,
                        StratumContentActivity.class);
                String title = getString(Const.FILENAMESETTING) + getString(Const.STRATUM5);
                intent.putExtra("title", title);
                intent.putExtra("which", "5");
                Map<String, Object> map = fileNameArray.get(4);
                String type = null;
                if (map.get(Const.KEY_TYPE) != null) {
                    type = map.get(Const.KEY_TYPE).toString();
                }
                String value = (String) map.get(Const.KEY_VALUE);
                intent.putExtra(Const.KEY_TYPE, type);
                intent.putExtra(Const.KEY_VALUE, value);
                startActivityForResult(intent, 1);
            }
        });

        stratum6Layout.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {

                

                Intent intent = new Intent(InitialSettingRuleFileNameActivity.this,
                        StratumContentActivity.class);
                String title = getString(Const.FILENAMESETTING) + getString(Const.STRATUM6);
                intent.putExtra("title", title);
                intent.putExtra("which", "6");
                Map<String, Object> map = fileNameArray.get(5);
                String type = null;
                if (map.get(Const.KEY_TYPE) != null) {
                    type = map.get(Const.KEY_TYPE).toString();
                }
                String value = (String) map.get(Const.KEY_VALUE);
                intent.putExtra(Const.KEY_TYPE, type);
                intent.putExtra(Const.KEY_VALUE, value);
                startActivityForResult(intent, 1);
            }
        });

        separator1.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {

                

                separatorC = ".";
                cancelSelect();
                separator1.setSelected(true);
                replaceFileName(separatorC);
            }
        });

        separator2.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {

                

                separatorC = "_";
                cancelSelect();
                separator2.setSelected(true);
                replaceFileName(separatorC);
            }
        });

        separator3.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {

                

                separatorC = "-";
                cancelSelect();
                separator3.setSelected(true);
                replaceFileName(separatorC);
            }
        });
    }

    private void cancelSelect() {
        separator1.setSelected(false);
        separator2.setSelected(false);
        separator3.setSelected(false);
    }

    private void clearLabel() {
        txFileName.setText("");
        ruleFileName.setText("");
        //replaceFileName(".");
        replaceFileName(separatorC);
    }

    private void replaceFileName(String c) {
        String fnStr = "";
        String rfnStr = "";
        for (int i = 0; i < fileNameArray.size(); i++) {
            Map<String, Object> map = fileNameArray.get(i);
            String type = null;
            if (map.get(Const.KEY_TYPE) != null) {
                type = map.get(Const.KEY_TYPE).toString();
            }
            String value = (String) map.get(Const.KEY_VALUE);
            String s = Util.gainFileName(type, value, time2);
            if (s != null) {
                fnStr = fnStr + c + s;
            }

            String str = Util.gainRuleFileName(type);
            if (s != null && !s.equals(getString(Const.SETTING_NOTHING))) {
                rfnStr = rfnStr + c + "[" + str + "]";
            }
        }
        if (fnStr.length() > 0) {
            fnStr = fnStr.substring(1, fnStr.length());
            fileName = fnStr;
            txFileName.setText(fnStr);
        }

        if (rfnStr.length() > 0) {
            rfnStr = rfnStr.substring(1, rfnStr.length());
            ruleFileName.setText(rfnStr);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == 1) {
                String which = data.getStringExtra("which");
                String type = data.getStringExtra(Const.KEY_TYPE);
                String value = data.getStringExtra(Const.KEY_VALUE);

                Map<String, Object> map = new HashMap<String, Object>();
                map.put(Const.KEY_TYPE, type);
                map.put(Const.KEY_VALUE, value);
                int index = Integer.parseInt(which) - 1;
                fileNameArray.remove(index);
                fileNameArray.add(index, map);

                value = Util.gainRuleFileName(type);
                txList.get(index).setText(value);

                clearLabel();
            }
        }

    }

}
