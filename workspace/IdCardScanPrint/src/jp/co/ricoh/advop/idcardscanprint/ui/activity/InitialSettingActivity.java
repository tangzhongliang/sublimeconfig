
package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;

import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;


public class InitialSettingActivity extends BaseActivity {

    /**
     * attain the class name
     */
    private String TAG = InitialSettingActivity.class.getSimpleName();

    /**
     * go back to preview activity(送信先選択画面) button
     */
    private RelativeLayout goBackBtn;

    private Button okBtn;
    /**
     * touching the layout, you can to set addressee
     */
    private LinearLayout addresseeLayout;

    /**
     * touching the layout, you can to set parameter about the scanning
     */
    private LinearLayout scanSettingsLayout;

    /**
     * touching the layout, you can to set parameter about the printting
     */
    private LinearLayout printSettingsLayout;

    /**
     * touching the layout, you can to set the rule about the file name
     */
    private LinearLayout ruleSetNameLayout;

    /**
     * touching the layout, you can to set smtp
     */
    private LinearLayout smtpLayout;

    /**
     * touching the layout, you can to set messenger
     */
    private LinearLayout messengerLayout;

    private RelativeLayout fileNameLayout;

    private RelativeLayout domainLayout;
    private LinearLayout aboutLayout;
    /**
     * throughing the EditText to gain the String of file name
     */
    private EditText fileNameEdit;
    private EditText domainEdit;

    private final String vaidPattern = "[0-9a-zA-Z.]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.initial_setting);
        init();
        setActionListener();
    }

    /**
     * init member variable
     */
    private void init() {

        setEnableBack();
        goBackBtn = (RelativeLayout) findViewById(R.id.init_setting_goback_btn);
        okBtn = (Button) findViewById(R.id.btn_ok_h_okcancel);
        addresseeLayout = (LinearLayout) findViewById(R.id.init_setting_item_addressee);
        scanSettingsLayout = (LinearLayout) findViewById(R.id.init_setting_item_scan_settings);
        printSettingsLayout = (LinearLayout) findViewById(R.id.init_setting_item_print_setting);
        ruleSetNameLayout = (LinearLayout) findViewById(R.id.init_setting_item_rule_set_name);
        messengerLayout = (LinearLayout) findViewById(R.id.init_setting_item_messenger);
        smtpLayout = (LinearLayout) findViewById(R.id.init_setting_item_smtp);
        fileNameLayout = (RelativeLayout) findViewById(R.id.init_setting_item_set_file_name);
        fileNameEdit = (EditText) findViewById(R.id.et_init_setting_item_set_file_name);
        domainLayout = (RelativeLayout) findViewById(R.id.init_setting_item_set_domain);
        domainEdit = (EditText) findViewById(R.id.et_init_setting_item_set_domain);
        aboutLayout = (LinearLayout) findViewById(R.id.init_setting_item_about);

        initSendFileNameDefaultValue();

        setupKeyboardHide(fileNameLayout);
        setupKeyboardHide(domainLayout);
    }

    private void initSendFileNameDefaultValue() {
        String subject = PreferencesUtil.getInstance().getSubject();
        if (subject != null && subject.length() > 0) {
            fileNameEdit.setText(subject);
        }

        String doamin = PreferencesUtil.getInstance().getDomain();
        if (doamin != null && doamin.length() > 0) {
            domainEdit.setText(doamin);
        }
    }

    /**
     * add actionListener to view
     */
    private void setActionListener() {

        goBackBtn.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {
                
                finish();
                InitialSettingActivity.this.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });

        
        okBtn.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View arg0) {

                
                String fileName = fileNameEdit.getEditableText().toString();
                if (!CUtil.isStringEmpty(fileName)) {
                    PreferencesUtil.getInstance().setSubject(fileName);
                }
                saveAndCheck();
//                if(!saveAndCheck()) {
//                    return;
//                }
                
                finish();
                InitialSettingActivity.this.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });

        addresseeLayout.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                
                saveAndCheck();
//                if(!saveAndCheck()) {
//                    return;
//                }
                startActivityForResult((new Intent(InitialSettingActivity.this,
                        InitialSettingReceiverActivity.class)), 6);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        scanSettingsLayout.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                
                saveAndCheck();
//                if(!saveAndCheck()) {
//                    return;
//                }
                Intent intent = new Intent(InitialSettingActivity.this,
                        InitialSettingScanActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        printSettingsLayout.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                
                saveAndCheck();
//                if(!saveAndCheck()) {
//                    return;
//                }
                Intent intent = new Intent(InitialSettingActivity.this,
                        InitialSettingPrintActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        ruleSetNameLayout.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                
                saveAndCheck();
//                if(!saveAndCheck()) {
//                    return;
//                }
                Intent intent = new Intent(InitialSettingActivity.this,
                        InitialSettingRuleFileNameActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        messengerLayout.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                
                saveAndCheck();
//                if(!saveAndCheck()) {
//                    return;
//                }
                Intent intent = new Intent(InitialSettingActivity.this, SenderSettingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        smtpLayout.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                
                saveAndCheck();
//                if(!saveAndCheck()) {
//                    return;
//                }
                Intent intent = new Intent(InitialSettingActivity.this, SmtpServerSetActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        
        aboutLayout.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                
                saveAndCheck();
                Intent intent = new Intent(InitialSettingActivity.this, AppInfoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); // 開発ガイドのとおり。

                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

    }

    @Override
    public void onStart() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.init_setting_item);
        layout.requestFocus();
        super.onStart();
    }
    
    private void saveAndCheck() {
        String fileName = fileNameEdit.getEditableText().toString();
        //if (!CUtil.isStringEmpty(fileName)) {
            PreferencesUtil.getInstance().setSubject(fileName);
       // }

        String domain = domainEdit.getEditableText().toString();
        //if (!CUtil.isStringEmpty(domain) && Util.checkedContent(domain, vaidPattern)) {
            PreferencesUtil.getInstance().setDomain(domain);
//        } else if(CUtil.isStringEmpty(domain)){
//            PreferencesUtil.getInstance().setDomain(domain);
//        } else if(!CUtil.isStringEmpty(domain) && !Util.checkedContent(domain, vaidPattern)) {
//            MultiButtonDialog msgDialog = MultiButtonDialog.createMsgDialog(InitialSettingActivity.this, getString(R.string.dialog_domain_wrong));
//            msgDialog.show();
//           return false; 
//        }
        //return true;
    }

    @Override
    protected void onStop() {
        setResult(RESULT_OK);
        super.onStop();
    }
}
