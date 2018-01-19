package jp.co.ricoh.advop.idcardscanprint.model;

import jp.co.ricoh.advop.idcardscanprint.util.Const;

import java.util.HashMap;
import java.util.Map;

public class SmtpServerSetting {
    public final static String KEY_MAIL_SERVER = "mailServer";
    public final static String KEY_PORT = "port";
    public final static String KEY_SEND_AUTH = "sendAuth";
    public final static String KEY_SSL = "ssl";
    public final static String KEY_USER_ID = "userId";
    public final static String KEY_PASSWORD = "password";
    Map<String, String> smtpMap = new HashMap<String, String>();
    
    String mailServer;
    String port;
    String sendAuth = Const.SMTP_OPTION_FALSE;
    String SSL = Const.SMTP_OPTION_FALSE;
    String userId;
    String password;
    public String getMailServer() {
        return mailServer;
    }
    public void setMailServer(String mailServer) {
        this.mailServer = mailServer;
    }
    public String getPort() {
        return port;
    }
    public void setPort(String port) {
        this.port = port;
    }
    public String getSendAuth() {
        return sendAuth;
    }
    public void setSendAuth(String sendAuth) {
        this.sendAuth = sendAuth;
    }
    public String getSSL() {
        return SSL;
    }
    public void setSSL(String sSL) {
        SSL = sSL;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Map<String, String> getSmtpMap() {
        if(smtpMap != null) {
            smtpMap.clear();
            smtpMap.put(KEY_MAIL_SERVER, mailServer);
            smtpMap.put(KEY_PORT, port);
            smtpMap.put(KEY_SEND_AUTH, sendAuth);
            smtpMap.put(KEY_SSL, SSL);
            smtpMap.put(KEY_USER_ID, userId);
            smtpMap.put(KEY_PASSWORD, password);
            
        }
        return smtpMap;
    }
    
    
    public void setSmtpMap(Map<String, String> smtpMap) {
        this.smtpMap = smtpMap;
        
        mailServer = smtpMap.get(KEY_MAIL_SERVER);
        port = smtpMap.get(KEY_PORT);
        sendAuth = smtpMap.get(KEY_SEND_AUTH);
        SSL = smtpMap.get(KEY_SSL);
        userId = smtpMap.get(KEY_USER_ID);
        password = smtpMap.get(KEY_PASSWORD);
    }
    
}
