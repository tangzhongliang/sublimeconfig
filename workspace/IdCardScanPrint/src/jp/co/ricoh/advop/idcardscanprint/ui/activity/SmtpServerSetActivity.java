package jp.co.ricoh.advop.idcardscanprint.ui.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;

import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;


import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.cheetahutil.util.BaseOnClickListener;
import jp.co.ricoh.advop.cheetahutil.widget.ConnecttingDialog;
import jp.co.ricoh.advop.cheetahutil.widget.MultiButtonDialog;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.model.SmtpServerSetting;
import jp.co.ricoh.advop.idcardscanprint.util.Const;

public class SmtpServerSetActivity extends BaseActivity {
    private Integer SMTP_AUTH_REQUEST = 1;
    private Integer SMTP_SSL_REQUEST = 2;
    
    private Integer isOkButton = 0;
    private Integer isTestButton = 1;
    private FrameLayout athuFrameLayout, sslFrameLayout, test, userFrameLayout, pwdFrameLayout;
    private TextView txtTitle, txtAuth, txtSSL;
    private Button btnOK, btnCancel;
    private String message;
    private Map<String, String> smtpSetMap;
    private EditText edtSmtpServer, edtPort, edtUserId, edtPwd;
    //private AsyncTask<Integer, Void, Integer> connnectTest;
    private ConnectTask connnectTest;
    private ConnecttingDialog dlg;
    private String inServer, inPort, inAuth, inSSL, inUser, inPwd;
    private ConnecttingDialog startTestDlg;
    private boolean connectRes = false;

    public class ConnectTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... params) {
            Integer type = params[0];
            //"rstsh02.rst.ricoh.com", "25", "huanglijun", "hlj821034", "false", "true"
            connectRes = confirmSMTP(inServer, inPort, inUser, inPwd, inAuth, inSSL);
            return type;
        }
        @Override
        protected void onPostExecute(final Integer result) {
            String msg = "";
           if(startTestDlg != null && startTestDlg.isShowing()) {
                startTestDlg.dismiss();
                startTestDlg = null;
            }
           if(!connectRes) {
               if(dlg != null && dlg.isShowing()) {
                   
               }
               msg = message;
           } else {                 
               msg = getString(R.string.connect_dialog_success);
               if(result == isOkButton) {
                   saveSmtpSetting();                          
               }                
       
           }
           
           final ConnecttingDialog dlg = new ConnecttingDialog(SmtpServerSetActivity.this);
           dlg.setText(msg);
           dlg.setButtonText(getString(R.string.bt_ok));
           dlg.SetButtonListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                dlg.dismiss();   
                if(connectRes && result == isOkButton) {
                   finish();
                   overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                }
            }
           });
           dlg.show();
        }
        
    }
            
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_smtp_set);
        test = (FrameLayout)findViewById(R.id.smtp_send_test);
        athuFrameLayout = (FrameLayout)findViewById(R.id.smtp_item_certifi);
        userFrameLayout = (FrameLayout)findViewById(R.id.smtp_item_user_code);
        pwdFrameLayout = (FrameLayout)findViewById(R.id.smtp_item_password);
        sslFrameLayout = (FrameLayout)findViewById(R.id.smtp_item_ssl);
        txtTitle = (TextView)findViewById(R.id.tx_title_h_ok_cancel);
        edtSmtpServer = (EditText)findViewById(R.id.smtp_et_server_name);
        edtPort = (EditText)findViewById(R.id.smtp_et_server_port);
        edtUserId = (EditText)findViewById(R.id.smtp_et_user_code);
        edtPwd = (EditText)findViewById(R.id.smtp_et_password);
//        //hint text
//        edtPwd.setTypeface(Typeface.DEFAULT);
//        edtPwd.setTransformationMethod(new PasswordTransformationMethod());
        txtAuth = (TextView)findViewById(R.id.smtp_txt_certifi);
        txtSSL = (TextView)findViewById(R.id.smtp_txt_ssl);
        

        FrameLayout inputServer = (FrameLayout)findViewById(R.id.smtp_item_server);
        FrameLayout inputPort = (FrameLayout)findViewById(R.id.smtp_item_port);
        FrameLayout inputUser = (FrameLayout)findViewById(R.id.smtp_item_user_code);
        FrameLayout inputPwd = (FrameLayout)findViewById(R.id.smtp_item_password);
        setupKeyboardHide(inputServer);
        setupKeyboardHide(inputPort);
        setupKeyboardHide(inputUser);
        setupKeyboardHide(inputPwd);
        
        txtTitle.setText(R.string.smtp_title);
        btnOK = (Button)findViewById(R.id.btn_ok_h_okcancel);
        btnCancel = (Button)findViewById(R.id.btn_cancel_h_okcancel);     
        
        
        initCallBack();
       
        smtpSetMap = PreferencesUtil.getInstance().getSmtpServer();
        if(smtpSetMap != null) {
            LogC.d("------------------get data from preferenceUtil------------");
            edtSmtpServer.setText(smtpSetMap.get(SmtpServerSetting.KEY_MAIL_SERVER));
            edtPort.setText(smtpSetMap.get(SmtpServerSetting.KEY_PORT));
            //LogC.d("set password is" + smtpSetMap.get(SmtpServerSetting.KEY_PASSWORD));
            edtPwd.setText(smtpSetMap.get(SmtpServerSetting.KEY_PASSWORD));
                      
            edtUserId.setText(smtpSetMap.get(SmtpServerSetting.KEY_USER_ID));
            int stringId = Const.SMTP_SERVER_MAP.get(smtpSetMap.get(SmtpServerSetting.KEY_SEND_AUTH));
            txtAuth.setText(stringId);
            stringId = Const.SMTP_SERVER_MAP.get(smtpSetMap.get(SmtpServerSetting.KEY_SSL));
            txtSSL.setText(stringId);
            inAuth = smtpSetMap.get(SmtpServerSetting.KEY_SEND_AUTH);
            inSSL = smtpSetMap.get(SmtpServerSetting.KEY_SSL);
        } else {
            LogC.d("init data");
            inAuth = Const.SMTP_OPTION_FALSE;
            inSSL = Const.SMTP_OPTION_FALSE;
            
            txtSSL.setText(Const.SMTP_SERVER_MAP.get(inAuth));
            txtAuth.setText(Const.SMTP_SERVER_MAP.get(inSSL));
        }
        
        if(inAuth.equals(Const.SMTP_OPTION_FALSE)) {
            edtUserId.setText("");
            userFrameLayout.setEnabled(false);
            edtPwd.setText("");
            pwdFrameLayout.setEnabled(false);
            edtUserId.setEnabled(false);
            edtPwd.setEnabled(false);
        } else {
            edtUserId.setEnabled(true);
            edtPwd.setEnabled(true);
            pwdFrameLayout.setEnabled(true);
            userFrameLayout.setEnabled(true);
            
        }
                
    }
    
    @Override
    protected void onStart() {
        super.onStart();
    }
    
    public boolean confirmSMTP(String host, String port, final String username, final String password, String auth, String enctype) {
        boolean result = false;
        try {
            //LogC.d("host is" + inServer + "port is" + port + "username is" + username + "password is " + password);
            Properties props = new Properties();
            Session session = null;
            
            LogC.d("auth is" + inAuth);
            
            props.put("mail.smtp.host", host); 
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.connectiontimeout", 60000);//1 minute
            props.put("mail.smtp.timeout", 60000);
            if (auth.equalsIgnoreCase(Const.SMTP_OPTION_TRUE)) {
                LogC.d("auth is true");
                props.put("mail.smtp.auth", "true"); 
            } else { 
                LogC.d("auth is false");
                props.put("mail.smtp.auth", "false"); 
            }
//            if (enctype.endsWith("TLS")) {
//                props.setProperty("mail.smtp.starttls.enable", "true");
//            } else if (enctype.endsWith("SSL")) {
//                props.setProperty("mail.smtp.startssl.enable", "true");
//            }
            LogC.d("auth is" + inSSL);
            if (enctype.equalsIgnoreCase(Const.SMTP_OPTION_TRUE)) {
                //Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());  
                LogC.d("inSSL is true");
               
                //props.put("mail.smtp.from", username);
                props.put("mail.smtp.socketFactory.port", port);
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.fallback", "false");
                props.put("mail.smtp.starttls.enable", "true");   
                //props.put("mail.smtp.ssl.enable", "true");
                session = Session.getInstance(props, new Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }

                });
  
            } else {
                LogC.d("inSSL is false");
                props.put("mail.smtp.startssl.enable", "false");
                session = Session.getInstance(props, null);
            }
            
            
            Transport transport = session.getTransport("smtp");
            //int portint = Integer.parseInt(port);
            transport.connect(host, username, password);
            //transport.connect(host, username, password);
            //transport.connect();
            transport.close();
            result = true;

        } catch (AuthenticationFailedException e) {
            message = getString(R.string.sender_auth_fail);
            
            LogC.e("SMTP: Authentication Failed", e);

        } catch(MessagingException e) {            
            message = getString(R.string.connect_dialog_failed);
            LogC.e("SMTP: Messaging Exception Occurred", e);
        } catch (Exception e) {            
            message = getString(R.string.connect_dialog_failed);
            LogC.e("SMTP: Unknown Exception", e);
        }

        return result;
    }

    
   private void updateInputString() {
        inServer = edtSmtpServer.getText().toString();  
        inPort = edtPort.getText().toString();  
//        inAuth = txtAuth.getText().toString();
//        inSSL= txtSSL.getText().toString();  
        inUser = edtUserId.getText().toString();  
        inPwd = edtPwd.getText().toString();  
    }
    
    private void initCallBack(){
        btnOK.setText(R.string.bt_ok);
        btnOK.setOnClickListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
//                if (CUtil.isStringEmpty(edtSmtpServer.getText().toString())
//                        || CUtil.isStringEmpty(edtSmtpServer.getText().toString()) 
//                        || (inAuth.equals(Const.SMTP_OPTION_TRUE) && (CUtil.isStringEmpty(edtUserId.getText().toString()) || CUtil.isStringEmpty(edtPwd.getText().toString())))) {
//                    MultiButtonDialog dlg = MultiButtonDialog.createMsgDialog(SmtpServerSetActivity.this, R.string.input_empty);
//                   
//                    dlg.show();
//                    return;
//                } 
                
             // input check
                if (CUtil.isStringEmpty(edtSmtpServer.getText().toString())) {
                    MultiButtonDialog.createMustInputCheckDialog(SmtpServerSetActivity.this,
                            R.string.smtp_set_server, edtSmtpServer).show();
                    return;
                }
                
                if (CUtil.isStringEmpty(edtPort.getText().toString())) {
                    MultiButtonDialog.createMustInputCheckDialog(SmtpServerSetActivity.this,
                            R.string.smtp_server_port, edtPort).show();
                    return;
                }
                
                if(inAuth.equalsIgnoreCase(Const.SMTP_OPTION_TRUE) && (CUtil.isStringEmpty(edtUserId.getText().toString()))) {
                    MultiButtonDialog.createMustInputCheckDialog(SmtpServerSetActivity.this,
                            R.string.smtp_user_code, edtUserId).show();
                    return;
                }
                
                if(inAuth.equalsIgnoreCase(Const.SMTP_OPTION_TRUE) && (CUtil.isStringEmpty(edtPwd.getText().toString()))) {
                    MultiButtonDialog.createMustInputCheckDialog(SmtpServerSetActivity.this,
                            R.string.smtp_password, edtPwd).show();
                    return;
                }
                if (startTestDlg == null) {
                    connnectTest = new ConnectTask();
                    startTestDlg = new ConnecttingDialog(SmtpServerSetActivity.this);
                    startTestDlg.setText(getString(R.string.connect_dialog_test));
                    startTestDlg.setButtonVisable(View.INVISIBLE);
                    startTestDlg.SetButtonListener(new BaseOnClickListener() {
                        
                        @Override
                        public void onWork(View v) {
                            
                            if(connnectTest != null && connnectTest.getStatus() != AsyncTask.Status.FINISHED) {
                                connnectTest.cancel(true);
                            }
                            startTestDlg.dismiss();
                            startTestDlg = null;
                            
                        }
                    });
                    startTestDlg.show();
                
                    updateInputString();
                    if (connnectTest.getStatus() == AsyncTask.Status.PENDING) {
                        LogC.d("start task!");
                        connnectTest.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, isOkButton);
                    }
                } 
            }
        });
        
      
        btnCancel.setText(R.string.bt_cancel);
        
        btnCancel.setOnClickListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                
            }
        });
        
        test.setOnClickListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
//                if (CUtil.isStringEmpty(edtSmtpServer.getText().toString())
//                        || CUtil.isStringEmpty(edtSmtpServer.getText().toString()) ) {
//                    MultiButtonDialog dlg = MultiButtonDialog.createMsgDialog(SmtpServerSetActivity.this, R.string.input_empty);
//                   
//                    dlg.show();
//                    return;
//                }
                // input check
                if (CUtil.isStringEmpty(edtSmtpServer.getText().toString())) {
                    MultiButtonDialog.createMustInputCheckDialog(SmtpServerSetActivity.this,
                            R.string.smtp_set_server, edtSmtpServer).show();
                    return;
                }
                
                if (CUtil.isStringEmpty(edtPort.getText().toString())) {
                    MultiButtonDialog.createMustInputCheckDialog(SmtpServerSetActivity.this,
                            R.string.smtp_server_port, edtPort).show();
                    return;
                }
                
                if(inAuth.equalsIgnoreCase(Const.SMTP_OPTION_TRUE) && (CUtil.isStringEmpty(edtUserId.getText().toString()))) {
                    MultiButtonDialog.createMustInputCheckDialog(SmtpServerSetActivity.this,
                            R.string.smtp_user_code, edtUserId).show();
                    return;
                }
                
                if(inAuth.equalsIgnoreCase(Const.SMTP_OPTION_TRUE) && (CUtil.isStringEmpty(edtPwd.getText().toString()))) {
                    MultiButtonDialog.createMustInputCheckDialog(SmtpServerSetActivity.this,
                            R.string.smtp_password, edtPwd).show();
                    return;
                }
                connnectTest = new ConnectTask();
                if (startTestDlg == null) {
                    LogC.d("start dialog");
                    startTestDlg = new ConnecttingDialog(SmtpServerSetActivity.this);
                    startTestDlg.setText(getString(R.string.connect_dialog_test));
                    startTestDlg.setButtonVisable(View.INVISIBLE);
                    startTestDlg.SetButtonListener(new BaseOnClickListener() {
                        
                        @Override
                        public void onWork(View v) {
                            
                            if(connnectTest != null && connnectTest.getStatus() != AsyncTask.Status.FINISHED) {
                                connnectTest.cancel(true);
                            }
                            startTestDlg.dismiss();
                            startTestDlg = null;
                            
                        }
                    });
                    startTestDlg.show();
                
                    updateInputString();
                    if (connnectTest.getStatus() == AsyncTask.Status.PENDING) {
                        LogC.d("start task!");
                        connnectTest.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, isTestButton);
                    }
                }
                
            }
        });
        
        athuFrameLayout.setOnClickListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                CHolder.instance().getGlobalDataManager().setSmtpType(SmtpServerSetting.KEY_SEND_AUTH);                   
                Intent intent = new Intent(SmtpServerSetActivity.this, SetOptionListActivity.class);
                intent.putIntegerArrayListExtra("ListData", (ArrayList<Integer>) Const.SMTP_SERVER_LIST);
                intent.putExtra("Inital", Const.SMTP_SERVER_MAP.get(inAuth));
                intent.putExtra("Title", getString(R.string.smtp_send_cer));
                startActivityForResult(intent, SMTP_AUTH_REQUEST);
                SmtpServerSetActivity.this.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);               
            }
        });
        
        sslFrameLayout.setOnClickListener(new BaseOnClickListener() {
            
            @Override
            public void onWork(View v) {
                
                CHolder.instance().getGlobalDataManager().setSmtpType(SmtpServerSetting.KEY_SSL);
                Intent intent = new Intent(SmtpServerSetActivity.this, SetOptionListActivity.class);
                //Bundle bundleSerializable = new Bundle();    

                //bundleSerializable.putSerializable("serializable", (Serializable) Const.SMTP_SERVER_LIST);   
                intent.putExtra("Inital", Const.SMTP_SERVER_MAP.get(inSSL));
                intent.putExtra("Title", getString(R.string.smtp_ssl));
                intent.putIntegerArrayListExtra("ListData", (ArrayList<Integer>) Const.SMTP_SERVER_LIST);
                //intent.putExtras(bundleSerializable);
                //Intent intent = new Intent(SmtpServerSetActivity.this, SetOptionListActivity.this);
                startActivityForResult(intent, SMTP_SSL_REQUEST);
                SmtpServerSetActivity.this.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);                                
            }
        });
        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        super.onActivityResult(requestCode, resultCode, data);
        
        if(requestCode == SMTP_AUTH_REQUEST && resultCode == RESULT_OK) {
            //Integer stringID = data.getIntExtra("selected", Const.SMTP_SERVER_LIST.get(0));
            Bundle bundle = new Bundle();        
            bundle = data.getExtras();
            int stringID = bundle.getInt("selected");
            String seletString = "";
            Iterator it = Const.SMTP_SERVER_MAP.keySet().iterator();    
            while(it.hasNext()){    
               String key;    
                
               key = it.next().toString();    
               
               if(stringID == Const.SMTP_SERVER_MAP.get(key)) {
                   seletString = key;
                   break;
               }
            }
           inAuth = seletString;
           LogC.d("inAuth is " + inAuth);
           txtAuth.setText(stringID);
           
           if(inAuth.equals(Const.SMTP_OPTION_FALSE)) {
               edtUserId.setText("");
               userFrameLayout.setEnabled(false);
               edtPwd.setText("");
               pwdFrameLayout.setEnabled(false);
               edtUserId.setEnabled(false);
               edtPwd.setEnabled(false);
           } else {
               edtUserId.setEnabled(true);
               edtPwd.setEnabled(true);
               userFrameLayout.setEnabled(true);
               pwdFrameLayout.setEnabled(true);
           }
        } else if(requestCode == SMTP_SSL_REQUEST && resultCode == RESULT_OK) {
            //Integer stringID = data.getIntExtra("selected", Const.SMTP_SERVER_LIST.get(0));
            Bundle bundle = new Bundle();        
            bundle = data.getExtras();
            int stringID = bundle.getInt("selected");
            String seletString = "";
            Iterator it = Const.SMTP_SERVER_MAP.keySet().iterator();    
            while(it.hasNext()){    
               String key;    
                
               key = it.next().toString();    
               
               if(stringID == Const.SMTP_SERVER_MAP.get(key)) {
                   seletString = key;
                   break;
               }
            }
            inSSL = seletString;
            LogC.d("inSSL is " + inSSL);
            txtSSL.setText(stringID);
        }
        
    }
    
    
    void saveSmtpSetting() {
        SmtpServerSetting value = new SmtpServerSetting();
        value.setMailServer(inServer);        
        value.setPassword(inPwd);
        value.setPort(inPort);
        value.setSendAuth(inAuth);
        value.setSSL(inSSL);
        value.setUserId(inUser);
        PreferencesUtil.getInstance().setSmtpServer(value.getSmtpMap());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (CHolder.instance().isRestarting()) {
            if(startTestDlg != null && startTestDlg.isShowing()) {
                startTestDlg.dismiss();
            }            
        }
    }
    
    @Override
    protected void onDestroy() {
       
        super.onDestroy();
        if(startTestDlg != null && startTestDlg.isShowing()) {
            startTestDlg.dismiss();
        }
    }
}
