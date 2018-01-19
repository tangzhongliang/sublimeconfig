package jp.co.ricoh.advop.idcardscanprint.logic.ftp;

import android.util.Log;

import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry;
import jp.co.ricoh.advop.idcardscanprint.util.Const;

public class FTPInfo {

    private String hostName;

    private int serverPort;

    private String remotePath;

    private String userName;

    private String password;

    private String controlEncoding;
    
    public FTPInfo(Entry entry) {
        this.hostName = entry.getFolderData().getServerName().replaceAll("\\\\", "").replaceAll("¥", "").replaceAll("/", "");
        this.serverPort = entry.getFolderData().getPortNumber();
        this.remotePath = entry.getFolderData().getPath().replaceAll("\\\\", "/").replaceAll("¥", "/");
        if (!remotePath.startsWith("/")) {
            remotePath = "/" + remotePath;
        }
        this.userName = entry.getFolderAuthData().getLoginUserName();
        this.password = entry.getFolderAuthData().getLoginPassword();
        this.controlEncoding = CUtil.isStringEmpty(entry.getFolderData().getJapaneseCharCode()) ? Const.ENCODING_OPTION_USASCII : entry.getFolderData().getJapaneseCharCode();
    }

    public FTPInfo(String hostName, int serverPort, String path, String userName, String password, String controlEncoding) {
        this.hostName = hostName.replaceAll("\\\\", "").replaceAll("¥", "").replaceAll("/", "");
        this.serverPort = serverPort;
        this.remotePath = path.replaceAll("\\\\", "/").replaceAll("¥", "/");
        if (!remotePath.startsWith("/")) {
            remotePath = "/" + remotePath;
        }
        this.userName = userName;
        this.password = password;
        this.controlEncoding = controlEncoding;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getControlEncoding() {
        return controlEncoding;
    }

    public void setControlEncoding(String controlEncoding) {
        this.controlEncoding = controlEncoding;
    }
}
