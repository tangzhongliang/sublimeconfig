
package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.widget.MultiButtonDialog;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.model.RuleFileNameType;
import jp.co.ricoh.advop.idcardscanprint.ui.view.HeaderOkCancel;
import jp.co.ricoh.advop.idcardscanprint.util.Const;
import jp.co.ricoh.advop.idcardscanprint.util.Util;

import java.util.ArrayList;
import java.util.List;

public class StratumContentActivity extends BaseActivity {

    private final String TAG = StratumContentActivity.class.getSimpleName();
    private jp.co.ricoh.advop.idcardscanprint.ui.view.HeaderOkCancel headerOkCancel;

    private Boolean userInput;
    private RelativeLayout StratumContent;
    private RelativeLayout StratumContent1;
    private RelativeLayout StratumContent2;
    private RelativeLayout StratumContent3;
    private RelativeLayout StratumContent4;

    private List<ImageView> ivList;
    private ImageView iv1;
    private ImageView iv2;
    private ImageView iv3;
    private ImageView iv4;

    private TextView nilTx;
    private TextView userNTx;
    private TextView timeTx;
    private EditText inputEt;

    private String type;
    private String tempType;
    private String value;
    private String temValue;
    private String whichActivity;
    //private final String invaidPattern = "[" + "\\/:*?\"<>|" + "]+";
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_stratum_content);
        init();
        setActionListener();
    }

    private void init() {

        headerOkCancel = (HeaderOkCancel) findViewById(R.id.init_setting_scan_header);
        String title = getIntent().getStringExtra("title");
        headerOkCancel.setHeaderTitle(title);
        headerOkCancel.setOkBtnText(getString(R.string.bt_ok));
        headerOkCancel.setCancelBtnText(getString(R.string.bt_cancel));

        StratumContent = (RelativeLayout) findViewById(R.id.layout_stratum_content);
        StratumContent1 = (RelativeLayout) findViewById(R.id.stratum01_layout);
        StratumContent2 = (RelativeLayout) findViewById(R.id.stratum02_layout);
        StratumContent3 = (RelativeLayout) findViewById(R.id.stratum03_layout);
        StratumContent4 = (RelativeLayout) findViewById(R.id.stratum04_layout);

        iv1 = (ImageView) findViewById(R.id.iv_cm_item_lasticon1);
        iv2 = (ImageView) findViewById(R.id.iv_cm_item_lasticon2);
        iv3 = (ImageView) findViewById(R.id.iv_cm_item_lasticon3);

        iv4 = (ImageView) findViewById(R.id.iv_cm_item_lasticon4);

        ivList = new ArrayList<ImageView>();
        ivList.add(iv1);
        ivList.add(iv2);
        ivList.add(iv3);
        ivList.add(iv4);

        nilTx = (TextView) findViewById(R.id.stratum01_tx);
        nilTx.setText(getString(R.string.tx_init_setting_section_print_file_name_rule01));
        userNTx = (TextView) findViewById(R.id.stratum02_tx);
        userNTx.setText(getString(R.string.tx_init_setting_section_print_file_name_rule02));
        timeTx = (TextView) findViewById(R.id.stratum03_time);
       // inputEt = (EditText) findViewById(R.id.stratum04_et);
        
        inputEt = (EditText) findViewById(R.id.stratum04_et);
       
        
        
        userInput = false;

        Intent intent = getIntent();
        whichActivity = intent.getStringExtra("which");
        type = intent.getStringExtra(Const.KEY_TYPE);
        value = intent.getStringExtra(Const.KEY_VALUE);
        tempType = type;
        temValue = value;
        setSelect(type, value);

        StratumContent.requestFocus();
        setupKeyboardHide(StratumContent4);
    }

    private void setSelect(String type, String value) {

        if (RuleFileNameType.USER_NAME.toString().equals(type)) {
            iv2.setSelected(true);
            StratumContent2.setSelected(true);
        } else if (RuleFileNameType.TIME.toString().equals(type)) {
            iv3.setSelected(true);
            StratumContent3.setSelected(true);
            timeTx.setText(value);
        } else if (RuleFileNameType.USER_INPUT.toString().equals(type)) {
            iv4.setSelected(true);
            StratumContent4.setSelected(true);
            inputEt.setText(value);
        } else {
            iv1.setSelected(true);
            StratumContent1.setSelected(true);
            this.type = RuleFileNameType.SETTING_NIL.toString();
            this.value = getString(Const.SETTING_NOTHING);
        }
    }

    private void setActionListener() {
        headerOkCancel.setCancelBtnActionListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {

                

                Intent intent = new Intent();
                intent.putExtra("which", whichActivity);
                intent.putExtra(Const.KEY_TYPE, tempType);
                intent.putExtra(Const.KEY_VALUE, temValue);
                setResult(1, intent);
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });

        headerOkCancel.setOkBtnActionListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {

                

                if (iv4.isSelected()) {
                    String inputStr = inputEt.getText().toString();
                    LogC.d("inputStr:" + inputStr);
                    //Regex rx=new Regex(@"^[^\\/:*?""<>|]+$");
                    if (CUtil.isStringEmpty(inputStr)) {
                      MultiButtonDialog msgDialog = MultiButtonDialog.createMustInputCheckDialog(
                              StratumContentActivity.this,
                              R.string.tx_init_setting_section_print_file_name_rule04, inputEt);
                      msgDialog.show();
                     
                      return;
                  } else if (!CUtil.isStringEmpty(inputStr) && !Util.checkedContent(inputStr, Const.invaidPattern)) {
                        LogC.d("checkedContent is true");
                        MultiButtonDialog msgDialog = MultiButtonDialog.createMsgDialog(
                                StratumContentActivity.this, getString(R.string.dialog_contain_character) + "\n" + Const.FILE_NAME_ILLEGALWORD);
                        msgDialog.show();
                        return;
                    } 

                    type = RuleFileNameType.USER_INPUT.toString();
                    value = inputStr;
                }

                Intent intent = new Intent();
                intent.putExtra(Const.KEY_TYPE, type);
                intent.putExtra(Const.KEY_VALUE, value);
                intent.putExtra("which", whichActivity);
                setResult(1, intent);
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });

        StratumContent.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {

                if (isOpenKSB()) {
                    hideKeySoftboard();
                    setBg();
                }
            }
        });

        StratumContent1.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                if (isOpenKSB()) {
                    hideKeySoftboard();
                    setBg();
                } else {

                    

                    cancelSelect();
                    iv1.setSelected(true);
                    StratumContent1.setSelected(true);
                    type = RuleFileNameType.SETTING_NIL.toString();
                    value = nilTx.getText().toString();
                }
            }
        });

        StratumContent2.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                if (isOpenKSB()) {
                    hideKeySoftboard();
                    setBg();
                } else {

                    

                    cancelSelect();
                    iv2.setSelected(true);
                    StratumContent2.setSelected(true);
                    type = RuleFileNameType.USER_NAME.toString();
                    value = userNTx.getText().toString();
                }
            }
        });

        StratumContent3.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                if (isOpenKSB()) {
                    hideKeySoftboard();
                    setBg();
                } else {

                    

                    cancelSelect();
                    iv3.setSelected(true);
                    StratumContent3.setSelected(true);
                    type = RuleFileNameType.TIME.toString();

                    Intent intent = new Intent(StratumContentActivity.this,
                            TimeSettingActivity.class);
                    intent.putExtra("select", value);
                    startActivityForResult(intent, 2);
                }
            }
        });

        StratumContent4.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {

                
                openKeySoftboard(inputEt);
                cancelSelect();
                cancelBg();

                StratumContent4.setSelected(true);
                iv4.setSelected(true);
                userInput = true;
            }
        });

//        inputEt.setOnClickListener(new BaseOnClickListener() {
//            
//            @Override
//            public void onWork(View arg0) {
//                
//
//                openKeySoftboard(inputEt);
//                cancelSelect();
//                cancelBg();
//
//                StratumContent4.setSelected(true);
//                iv4.setSelected(true);
//                userInput = true;
//            }
//        });
        
    }

    private void cancelBg() {
        StratumContent1.setBackground(getResources().getDrawable(R.drawable.bt_com_01_n));
        StratumContent2.setBackground(getResources().getDrawable(R.drawable.bt_com_01_n));
        StratumContent3.setBackground(getResources().getDrawable(R.drawable.bt_com_01_n));
    }

    private void setBg() {
        StratumContent1.setBackground(getResources().getDrawable(R.drawable.selector_bt_com_01));
        StratumContent2.setBackground(getResources().getDrawable(R.drawable.selector_bt_com_01));
        StratumContent3.setBackground(getResources().getDrawable(R.drawable.selector_bt_com_01));
    }

    private Boolean isOpenKSB() {
        if (userInput) {
            userInput = false;
            return true;
        } else {
            return false;
        }
    }

    private void openKeySoftboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
    }

    private void hideKeySoftboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputEt.getWindowToken(), 0);
    }

    private void cancelSelect() {
        // inputEt.setFocusable(false);
        userInput = false;

        StratumContent1.setSelected(false);
        StratumContent2.setSelected(false);
        StratumContent3.setSelected(false);
        StratumContent4.setSelected(false);
        for (int i = 0; i < ivList.size(); i++) {
            ivList.get(i).setSelected(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        StratumContent.requestFocus();

        if (requestCode == 2) {
            if (resultCode == 2) {
                String str = data.getStringExtra("time");
                value = str;
                timeTx.setText(value);
            }
        }
    }
}
