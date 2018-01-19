package jp.co.ricoh.advop.idcardscanprint.model;

import java.util.ArrayList;

import jp.co.ricoh.advop.idcardscanprint.ui.activity.SplashActivity;
import jp.co.ricoh.advop.idcardscanprint.util.Const;


public class GlobalDataManager {
    public static enum FragmentState {
        SenderSelect, ScanSide1, ScanSide2, ImagePreview
    }
    
    private SplashActivity sActivity;
    private String sendFileName;
    private boolean closeDlg = true;

    public boolean isCloseDlg() {
        return closeDlg;
    }

    public void setCloseDlg(boolean closeDlg) {
        this.closeDlg = closeDlg;
    }

    private String smptType = SmtpServerSetting.KEY_SEND_AUTH;
    
    public SplashActivity getsActivity() {
        return sActivity;
    }

    public void setsActivity(SplashActivity sActivity) {
        this.sActivity = sActivity;
    }

    private FragmentState state = FragmentState.SenderSelect;

    public FragmentState getState() {
        return state;
    }

    public void setState(FragmentState state) {
        this.state = state;
    }
    
    public String getSmtpType() {
        return smptType;
    }
    
    public void setSmtpType(String type) {
        smptType = type;
    }
    
    public String getSendFileName() {
        return sendFileName;
    }

    public void setSendFileName(String sendFileName) {
        this.sendFileName = sendFileName;
    }
    
    public ArrayList<String> sendLogList = new ArrayList<String>();

    public ArrayList<String> getSendLogList() {
        return sendLogList;
    }

    public void setSendLogList(ArrayList<String> sendLogList) {
        this.sendLogList = sendLogList;
    }
    
    
}

