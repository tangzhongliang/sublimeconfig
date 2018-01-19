
package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import java.util.LinkedHashMap;


import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.widget.ConnecttingDialog;
import jp.co.ricoh.advop.cheetahutil.widget.MultiButtonDialog;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.smb.SMBInfo;
import jp.co.ricoh.advop.idcardscanprint.logic.smb.SMBManager;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry.FolderAuthData;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry.FolderData;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.BaseActivity;
import jp.co.ricoh.advop.idcardscanprint.ui.view.HeaderOkCancel;

public class InputSmbActivity extends BaseActivity {
    
    private HeaderOkCancel header;
    
    private FrameLayout pathLayout;
    private FrameLayout usernameLayout;
    private FrameLayout passwordLayout;
    private FrameLayout connectLayout;
    private EditText pathEdit;
    private EditText usernameEdit;
    private EditText passwordEdit;

    private ConnecttingDialog startTestDlg;
    
    boolean isOkButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_input_smb_info);
        init();
    }

    /**
     * init member variable
     */
    private void init() {
        
        isOkButton = false;
        
        header = (HeaderOkCancel) findViewById(R.id.input_smb_header);
        header.setHeaderTitle(getString(R.string.input_folder_title));
        header.setOkBtnText(getString(R.string.bt_ok));
        header.setCancelBtnText(getString(R.string.bt_cancel));
        
        header.setOkBtnActionListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                isOkButton = true;
                connectSmbServer();
            }
        });
        header.setCancelBtnActionListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                setResult(RESULT_CANCELED);
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });
        
        connectLayout = (FrameLayout) findViewById(R.id.smb_connect_test);
        connectLayout.setOnClickListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                isOkButton = false;
                connectSmbServer();
            }
        });
        
        pathLayout = (FrameLayout) findViewById(R.id.smb_item_path);
        usernameLayout = (FrameLayout) findViewById(R.id.smb_item_username);
        passwordLayout = (FrameLayout) findViewById(R.id.smb_item_password);
        setupKeyboardHide(pathLayout);
        setupKeyboardHide(usernameLayout);
        setupKeyboardHide(passwordLayout);
        pathEdit = (EditText) findViewById(R.id.edit_smb_item_path);
        usernameEdit = (EditText) findViewById(R.id.edit_smb_item_username);
        passwordEdit = (EditText) findViewById(R.id.edit_smb_item_password);
        //hint text type
//        passwordEdit.setTypeface(Typeface.DEFAULT);
//        passwordEdit.setTransformationMethod(new PasswordTransformationMethod());
//        passwordEdit.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD);
        
        FrameLayout inputServer = (FrameLayout)findViewById(R.id.smb_item_path);
        FrameLayout inputUser = (FrameLayout)findViewById(R.id.smb_item_username);
        FrameLayout inputPwd = (FrameLayout)findViewById(R.id.smb_item_password);
        setupKeyboardHide(inputServer);
        setupKeyboardHide(inputUser);
        setupKeyboardHide(inputPwd);
        
        if (CHolder.instance().getLastInputEntry() != null
                && CHolder.instance().getLastInputEntry().getFolderData().getProtocolType() != null
                && "SMB".equalsIgnoreCase(CHolder.instance().getLastInputEntry().getFolderData().getProtocolType())) {
            FolderData folderData = CHolder.instance().getLastInputEntry().getFolderData();
            FolderAuthData folderAuthData = CHolder.instance().getLastInputEntry().getFolderAuthData();
            
            pathEdit.setText(folderData.getPath());
            usernameEdit.setText(folderAuthData.getLoginUserName());
            passwordEdit.setText(folderAuthData.getLoginPassword());
        }
        pathEdit.setText("\\\\172.25.78.108\\share");
        usernameEdit.setText("giraffe");
    }
    
    private void connectSmbServer() {
        if (CUtil.isStringEmpty(pathEdit.getText().toString())) {
            MultiButtonDialog msgDialog = MultiButtonDialog.createMustInputCheckDialog(
                    InputSmbActivity.this,
                    R.string.server_path, pathEdit);
            msgDialog.show();
            return;
        }

        if (startTestDlg == null) {
            startTestDlg = new ConnecttingDialog(InputSmbActivity.this);
            startTestDlg.setText(getString(R.string.connect_dialog_test));
            startTestDlg.setButtonVisable(View.GONE);
            startTestDlg.show();
        }

        new CheckConnectTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onStop() {
        setResult(RESULT_OK);
        super.onStop();
    }
    
    class CheckConnectTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... params) {
            SMBInfo smbInfo = new SMBInfo(pathEdit.getText().toString(), usernameEdit.getText()
                    .toString(), passwordEdit.getText().toString());
            String retString = SMBManager.connect(smbInfo);
            return retString;
        }

        @Override
        protected void onPostExecute(final String result) {
            if (startTestDlg != null && startTestDlg.isShowing()) {
                startTestDlg.dismiss();
                startTestDlg = null;
            }

            final ConnecttingDialog dlg = new ConnecttingDialog(InputSmbActivity.this);
            String message = "";
            BaseOnClickListener dlgOkAction = null;

            if (SMBManager.SMB_CONNECT_SUCCESSS.equals(result)) {
                message = getString(R.string.connect_dialog_success);
                dlgOkAction = new BaseOnClickListener() {

                    @Override
                    public void onWork(View v) {
                        
                        dlg.dismiss();
                        if (isOkButton) {
                            Entry entry = new Entry(new LinkedHashMap<String, Object>());
                            entry.setName(pathEdit.getText().toString());
                            FolderData folderData = entry.getFolderData();
                            folderData.setProtocolType("SMB");
                            folderData.setPath(pathEdit.getText().toString());
                            
                            FolderAuthData folderAuthData = entry.getFolderAuthData();
                            folderAuthData.setLoginUserName(usernameEdit.getText().toString());
                            folderAuthData.setLoginPassword(passwordEdit.getText().toString());
                            
                            Intent intent = getIntent();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("entry", entry);
                            intent.putExtras(bundle);
                            setResult(RESULT_OK, intent);
                            finish();
                            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                        }
                        isOkButton = false;
                    }
                };
            } else if (SMBManager.SMB_CONNECT_FAIL.equals(result)) {
                message = getString(R.string.connect_dialog_failed);
                dlgOkAction = new BaseOnClickListener() {

                    @Override
                    public void onWork(View v) {
                        
                        dlg.dismiss();
                        isOkButton = false;
                    }
                };
            } else if (SMBManager.SMB_AUTH_FAIL.equals(result)) {
                message = getString(R.string.sender_auth_fail);
                dlgOkAction = new BaseOnClickListener() {

                    @Override
                    public void onWork(View v) {
                        
                        dlg.dismiss();
                        isOkButton = false;
                    }
                };
            }

            dlg.setText(message);
            dlg.setButtonText(getString(R.string.bt_ok));
            dlg.SetButtonListener(dlgOkAction);
            dlg.show();
        }
    };
}
