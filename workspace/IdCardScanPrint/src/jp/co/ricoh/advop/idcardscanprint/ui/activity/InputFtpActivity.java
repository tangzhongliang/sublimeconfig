
package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;


import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.widget.ConnecttingDialog;
import jp.co.ricoh.advop.cheetahutil.widget.MultiButtonDialog;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.ftp.FTPInfo;
import jp.co.ricoh.advop.idcardscanprint.logic.ftp.FTPManager;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry.FolderAuthData;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry.FolderData;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.BaseActivity;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.SetOptionListActivity;
import jp.co.ricoh.advop.idcardscanprint.ui.view.HeaderOkCancel;
import jp.co.ricoh.advop.idcardscanprint.util.Const;

public class InputFtpActivity extends BaseActivity {
    
    private Integer ENCODING_REQUEST = 1;
    
    private HeaderOkCancel header;

    private FrameLayout hostNameLayout;
    private FrameLayout serverPortLayout;
    private FrameLayout remotePathLayout;
    private FrameLayout usernameLayout;
    private FrameLayout passwordLayout;
    private FrameLayout connectLayout;

    private EditText hostNameEdit;
    private EditText serverPortEdit;
    private EditText remotePathEdit;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private FrameLayout controlEncodingLayout;
    private TextView controlEncodingText;

    private AsyncTask<Void, Void, String> connnectTest;
    private ConnecttingDialog startTestDlg;
    
    private boolean isOkButton;
    private String encoding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_input_ftp_info);
        init();
    }

    /**
     * init member variable
     */
    private void init() {
        
        isOkButton = false;
        encoding = Const.ENCODING_OPTION_USASCII;
        
        header = (HeaderOkCancel) findViewById(R.id.input_ftp_header);
        header.setHeaderTitle(getString(R.string.input_folder_title));
        header.setOkBtnText(getString(R.string.bt_ok));
        header.setCancelBtnText(getString(R.string.bt_cancel));
        
        header.setOkBtnActionListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                isOkButton = true;
                connectFtpServer();
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
        
        connectLayout = (FrameLayout) findViewById(R.id.ftp_connect_test);
        connectLayout.setOnClickListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                isOkButton = false;
                connectFtpServer();
            }
        });
        
        hostNameLayout = (FrameLayout) findViewById(R.id.ftp_item_hostname);
        serverPortLayout = (FrameLayout) findViewById(R.id.ftp_item_port);
        remotePathLayout = (FrameLayout) findViewById(R.id.ftp_item_path);
        usernameLayout = (FrameLayout) findViewById(R.id.ftp_item_username);
        passwordLayout = (FrameLayout) findViewById(R.id.ftp_item_password);
        setupKeyboardHide(hostNameLayout);
        setupKeyboardHide(serverPortLayout);
        setupKeyboardHide(remotePathLayout);
        setupKeyboardHide(usernameLayout);
        setupKeyboardHide(passwordLayout);

        hostNameEdit = (EditText) findViewById(R.id.edit_ftp_item_hostname);
        serverPortEdit = (EditText) findViewById(R.id.edit_ftp_item_port);
        serverPortEdit.setText("21");
//        serverPortEdit.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        remotePathEdit = (EditText) findViewById(R.id.edit_ftp_item_path);
        controlEncodingLayout = (FrameLayout) findViewById(R.id.ftp_item_encoding);
        controlEncodingText = (TextView)findViewById(R.id.text_ftp_item_encoding);
        controlEncodingText.setText(encoding);
        usernameEdit = (EditText) findViewById(R.id.edit_ftp_item_username);
        passwordEdit = (EditText) findViewById(R.id.edit_ftp_item_password);
        //hint text type
//        passwordEdit.setTypeface(Typeface.DEFAULT);
//        passwordEdit.setTransformationMethod(new PasswordTransformationMethod());
//        passwordEdit.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD);
        
        if (CHolder.instance().getLastInputEntry() != null
                && CHolder.instance().getLastInputEntry().getFolderData().getProtocolType() != null
                && "FTP".equalsIgnoreCase(CHolder.instance().getLastInputEntry().getFolderData().getProtocolType())) {
            FolderData folderData = CHolder.instance().getLastInputEntry().getFolderData();
            FolderAuthData folderAuthData = CHolder.instance().getLastInputEntry().getFolderAuthData();
            
            hostNameEdit.setText(folderData.getServerName());
            serverPortEdit.setText("" + folderData.getPortNumber());
            remotePathEdit.setText(folderData.getPath());
            encoding = folderData.getJapaneseCharCode();
            controlEncodingText.setText(encoding);
            usernameEdit.setText(folderAuthData.getLoginUserName());
            passwordEdit.setText(folderAuthData.getLoginPassword());
        }
        
        controlEncodingLayout.setOnClickListener(new BaseOnClickListener() {

            @Override
            public void onWork(View v) {
                
                Intent intent = new Intent(InputFtpActivity.this, SetOptionListActivity.class);
                //Bundle bundleSerializable = new Bundle();    

                //bundleSerializable.putSerializable("serializable", (Serializable) Const.ENCODING_LIST);   
                intent.putExtra("Inital", Const.ENCODING_MAP.get(encoding));
                intent.putExtra("Title", getString(R.string.ftp_item_encoding));
                //intent.putIntegerArrayListExtra(name, value)
                intent.putIntegerArrayListExtra("ListData", (ArrayList<Integer>) Const.ENCODING_LIST);
                //intent.putExtras(bundleSerializable);
                startActivityForResult(intent, ENCODING_REQUEST);
                InputFtpActivity.this.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

            }
        });
        
    }
    
    private void connectFtpServer() {
        if (CUtil.isStringEmpty(hostNameEdit.getText().toString())) {
            MultiButtonDialog msgDialog = MultiButtonDialog.createMustInputCheckDialog(
                    InputFtpActivity.this,
                    R.string.ftp_item_server, hostNameEdit);
            msgDialog.show();
            return;
        } else if (CUtil.isStringEmpty(serverPortEdit.getText().toString())) {
            MultiButtonDialog msgDialog = MultiButtonDialog.createMustInputCheckDialog(
                    InputFtpActivity.this,
                    R.string.smtp_server_port, serverPortEdit);
            msgDialog.show();
            return;
        }
        
        if (startTestDlg == null) {
            startTestDlg = new ConnecttingDialog(InputFtpActivity.this);
            startTestDlg.setText(getString(R.string.connect_dialog_test));
            startTestDlg.setButtonVisable(View.GONE);
            startTestDlg.show();
        }
        
//        if (connnectTest == null || connnectTest.getStatus() == AsyncTask.Status.PENDING || connnectTest.getStatus() == AsyncTask.Status.FINISHED) {
            connnectTest = new AsyncTask<Void, Void, String>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                }

                @Override
                protected String doInBackground(Void... params) {
                    String remotePath = remotePathEdit.getText().toString();
                    if (CUtil.isStringEmpty(remotePath)) {
                        remotePath = "/";
                    }
                    
                    try {
                        int port = Integer.parseInt(serverPortEdit.getText().toString());
                        
                        if (port < 0 || port > 65535) {
                            return FTPManager.FTP_CONNECT_FAIL;
                        }
                        
                        FTPInfo ftpInfo = new FTPInfo(hostNameEdit.getText().toString(), port, 
                                remotePath, usernameEdit.getText().toString(), passwordEdit.getText().toString(), encoding);
                        String retString =  new FTPManager().connect(ftpInfo);
                        return retString;
                    } catch (Exception e) {
                        LogC.e("Parse port number! " + e.getLocalizedMessage());
                        return FTPManager.FTP_CONNECT_FAIL;
                    }
                }

                @Override
                protected void onPostExecute(final String result) {
                    if (startTestDlg != null && startTestDlg.isShowing()) {
                        startTestDlg.dismiss();
                        startTestDlg = null;
                    }

                    final ConnecttingDialog dlg = new ConnecttingDialog(InputFtpActivity.this);
                    String message = "";
                    BaseOnClickListener dlgOkAction = null;

                    if (FTPManager.FTP_CONNECT_SUCCESSS.equals(result)) {
                        message = getString(R.string.connect_dialog_success);
                        dlgOkAction = new BaseOnClickListener() {

                            @Override
                            public void onWork(View v) {
                                
                                dlg.dismiss();
                                if (isOkButton) {
                                    String remotePath = remotePathEdit.getText().toString();
                                    String name = remotePath;
                                    if (CUtil.isStringEmpty(remotePath)) {
                                        remotePath = "/";
                                        name = "\\";
                                    }
                                    if (!name.startsWith("\\") && !name.startsWith("/") && !name.startsWith("¥")) {
                                        name = "\\" + name;
                                    }
                                    
                                    Entry entry = new Entry(new LinkedHashMap<String, Object>());
                                    entry.setName(hostNameEdit.getText().toString() + name);
                                    FolderData folderData = entry.getFolderData();
                                    folderData.setProtocolType("FTP");
                                    folderData.setServerName(hostNameEdit.getText().toString());
                                    folderData.setPortNumber(Integer.parseInt(serverPortEdit.getText().toString()));
                                    folderData.setPath(remotePath);
                                    folderData.setJapaneseCharCode(encoding);
                                    
                                    FolderAuthData folderAuthData = entry.getFolderAuthData();
                                    folderAuthData.setLoginUserName(usernameEdit.getText().toString());
                                    folderAuthData.setLoginPassword(passwordEdit.getText().toString());
                                    
                                    Intent intent = new Intent(InputFtpActivity.this, InitialSettingReceiverActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
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
                    } else if (FTPManager.FTP_AUTH_FAIL.equals(result)) {
                        message = getString(R.string.sender_auth_fail);
                        dlgOkAction = new BaseOnClickListener() {

                            @Override
                            public void onWork(View v) {
                                
                                dlg.dismiss();
                                isOkButton = false;
                            }
                        };
                    } else {
                        message = getString(R.string.connect_dialog_failed);
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
            connnectTest.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        super.onActivityResult(requestCode, resultCode, data);
        
        if(requestCode == ENCODING_REQUEST && resultCode == RESULT_OK) {
           
            //Integer stringID = data.getIntExtra("selected", Const.ENCODING_MAP.get(0));
            Bundle bundle = new Bundle();        
            bundle = data.getExtras();
            int stringID = bundle.getInt("selected");
            LogC.d("get selected" + stringID);
            String seletString = "";
            Iterator it = Const.ENCODING_MAP.keySet().iterator();    
            while(it.hasNext()) {    
               String key;    
                
               key = it.next().toString();    
               
               if(stringID == Const.ENCODING_MAP.get(key)) {
                   seletString = key;
                   break;
               }
            }
           encoding = seletString;
           if(stringID != 0) {
               controlEncodingText.setText(stringID);
           }
        }
        
    }

    @Override
    protected void onStop() {
        setResult(RESULT_OK);
        super.onStop();
    }
}
