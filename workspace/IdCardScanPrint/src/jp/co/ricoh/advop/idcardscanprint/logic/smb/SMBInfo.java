package jp.co.ricoh.advop.idcardscanprint.logic.smb;

import android.util.Log;
import android.widget.Toast;

import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry;
import jp.co.ricoh.advop.idcardscanprint.ui.activity.InputSmbActivity;

public class SMBInfo {
    
    private String hostNameAndPath;

    private String userName;

    private String password;
    
    public SMBInfo(Entry entry) {
        String path = entry.getFolderData().getPath();
        if (path.startsWith("\\\\")) {
            path = path.replaceFirst("\\\\\\\\", "");
        } else if (path.startsWith("//")) {
            path = path.replaceFirst("//", "");
        } else if (path.startsWith("¥¥")) {
            path = path.replaceFirst("¥¥", "");
        }
        this.hostNameAndPath = path.replaceAll("\\\\", "/").replaceAll("¥", "/");
        this.setUserName(entry.getFolderAuthData().getLoginUserName());
        this.setPassword(entry.getFolderAuthData().getLoginPassword());
    }

    public SMBInfo(String hostNameAndPath, String userName, String password) {
        String path = hostNameAndPath;
        if (path.startsWith("\\\\")) {
            path = path.replaceFirst("\\\\\\\\", "");
        } else if (path.startsWith("//")) {
            path = path.replaceFirst("//", "");
        } else if (path.startsWith("¥¥")) {
            path = path.replaceFirst("¥¥", "");
        }
        this.hostNameAndPath = path.replaceAll("\\\\", "/").replaceAll("¥", "/");
        this.setUserName(userName);
        this.setPassword(password);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        if (CUtil.isStringEmpty(userName)) {
            userName = null;
        }
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (CUtil.isStringEmpty(password)) {
            password = null;
        }
        this.password = password;
    }
    
    public String getPath() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("smb://");
        stringBuilder.append(this.hostNameAndPath);
        String url = stringBuilder.toString();
        return url;
    }
}
