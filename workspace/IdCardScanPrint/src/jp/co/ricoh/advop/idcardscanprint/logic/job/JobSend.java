package jp.co.ricoh.advop.idcardscanprint.logic.job;

import android.os.AsyncTask;

import com.sun.mail.smtp.SMTPSendFailedException;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import javax.mail.MessagingException;


import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.R;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.EXE_RESULT;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.ExeCallback;
import jp.co.ricoh.advop.idcardscanprint.logic.ftp.FTPInfo;
import jp.co.ricoh.advop.idcardscanprint.logic.ftp.FTPManager;
import jp.co.ricoh.advop.idcardscanprint.logic.smb.SMBInfo;
import jp.co.ricoh.advop.idcardscanprint.logic.smb.SMBManager;
import jp.co.ricoh.advop.idcardscanprint.logic.smtp.SMTPManager;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.model.SmtpServerSetting;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.rws.addressbook.Entry.FolderData;

public class JobSend extends LongJob {

    
    public JobSend(ExeCallback exeCallback) {
        super(exeCallback);
    }
    
    @Override
    public void onStart() {
        if (CHolder.instance().getJobData().isJobCancel()) {
            complete(EXE_RESULT.FAILED);
            return;
        }
       // final ArrayList<Entry> list = PreferencesUtil.getInstance().getDestinations();
        final ArrayList<String> logList = CHolder.instance().getGlobalDataManager().getSendLogList();
        logList.clear();
        final Entry[] list = CHolder.instance().getSelects();
        if (list == null || list.length == 0) {
            logList.add(CHolder.instance().getActivity().getString(R.string.mail_send_fail) + ":" + CHolder.instance().getActivity().getString(R.string.recevier_not_select));
            complete(EXE_RESULT.FAILED);
            return;
        }
        
       
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                if (CHolder.instance().getJobData().isJobCancel()) {                    
                    return false;
                }
                String sendFile = CHolder.instance().getGlobalDataManager().getSendFileName();
                for(int i = 0; i < list.length; i++) {
                    if(CHolder.instance().getJobData().isJobCancel()) {
                        return false;
                    }
                    Entry entry = list[i];
                    if(entry != null && entry.getMailData() != null && entry.getMailData().getMailAddress() != null) {
                        LogC.d("mail Send");
                        String to = entry.getMailData().getMailAddress();
                        Map<String, String> smtpSetMap = PreferencesUtil.getInstance().getSmtpServer();
                        
                        String host = smtpSetMap.get(SmtpServerSetting.KEY_MAIL_SERVER);
                        Entry fromEntry = PreferencesUtil.getInstance().getEntrySender();
                        String from = null;
                        if(fromEntry != null) {
                            from = fromEntry.getMailData().getMailAddress();
                        }
                        
                        String password = smtpSetMap.get(SmtpServerSetting.KEY_PASSWORD);
                        String user = smtpSetMap.get(SmtpServerSetting.KEY_USER_ID);
                        String port = smtpSetMap.get(SmtpServerSetting.KEY_PORT);
                        String isAuth = smtpSetMap.get(SmtpServerSetting.KEY_SEND_AUTH);
                        String isSSL = smtpSetMap.get(SmtpServerSetting.KEY_SSL);
                        String subject = PreferencesUtil.getInstance().getSubject();
                        
                        if(host == null) {
                            logList.add(to + ":" + CHolder.instance().getActivity().getString(R.string.mail_send_fail) +" " +  CHolder.instance().getActivity().getString(R.string.smtp_not_set));
                        } else if(from == null) {
                            logList.add(to + ":" + CHolder.instance().getActivity().getString(R.string.mail_send_fail) +" " +  CHolder.instance().getActivity().getString(R.string.sender_not_select));
                        } else if(sendFile == null) {
                            //logList.add(to + ":" + CHolder.instance().getActivity().getString(R.string.mail_send_fail) +  CHolder.instance().getActivity().getString(R.string.sender_not_select)); 
                        } else {
                            try {                 
                                LogC.d("start send mail!!!" + sendFile);
                                SMTPManager.SendEmail(host, from, to, user, password, port, subject, "", isAuth, isSSL, sendFile);
                            } catch (SMTPSendFailedException e) {
                                LogC.e("", e);
                                e.getReturnCode();
                            } catch (MessagingException e) {
                                LogC.e("", e);
                                logList.add(to + ":" + CHolder.instance().getActivity().getString(R.string.mail_send_fail));
                            } catch (Exception e) {                          
                                LogC.w("mail fail, Get exception", e);
                                logList.add(to + ":" + CHolder.instance().getActivity().getString(R.string.mail_send_fail));
                            }
                        }
                    } else if (entry.getFolderData().getProtocolType() != null) {
                        FolderData folderData = entry.getFolderData();
                        final String to = entry.getName();
                        File file = new File(CHolder.instance().getGlobalDataManager().getSendFileName());
                        if ("FTP".equalsIgnoreCase(folderData.getProtocolType())) {
                            new FTPManager().uploadFile(file, new FTPInfo(entry), new FTPManager.UploadProgressListener() {

                                @Override
                                public void onUploadProgress(String currentStep, long uploadSize, File file) {
                                    if (currentStep.equals(FTPManager.FTP_UPLOAD_SUCCESS)) {
                                        LogC.d("FTP Upload successful");
                                    } else if (currentStep.equals(FTPManager.FTP_UPLOAD_FAIL)) {
                                        LogC.d("FTP Upload failed!");
                                        logList.add(to + ":" + CHolder.instance().getActivity().getString(R.string.sender_fail));
                                    } else if (currentStep.equals(FTPManager.FTP_AUTH_FAIL)) {
                                        LogC.d("FTP Upload auth failed!");
                                        logList.add(to + ":" + CHolder.instance().getActivity().getString(R.string.sender_auth_fail));
                                    }
                                }
                            });
                        } else if ("SMB".equalsIgnoreCase(folderData.getProtocolType())) {
                            SMBManager.uploadFile(file, new SMBInfo(entry), new FTPManager.UploadProgressListener() {

                                @Override
                                public void onUploadProgress(String currentStep, long uploadSize, File file) {
                                    if (currentStep.equals(SMBManager.SMB_UPLOAD_SUCCESS)) {
                                        LogC.d("SMB Upload successful");
                                    } else if (currentStep.equals(SMBManager.SMB_UPLOAD_FAIL)) {
                                        LogC.d("SMB Upload failed!");
                                        logList.add(to + ":" + CHolder.instance().getActivity().getString(R.string.sender_fail));
                                    } else if (currentStep.equals(SMBManager.SMB_AUTH_FAIL)) {
                                        LogC.d("SMB Upload auth failed!");
                                        logList.add(to + ":" + CHolder.instance().getActivity().getString(R.string.sender_auth_fail));
                                    }
                                }
                            });
                        }
                    }
                }
                return true;
            
            }
            
            @Override
            protected void onPostExecute(Boolean result) {
                if(result) {
                    complete(EXE_RESULT.SUCCESSED);
                } else {
                    complete(EXE_RESULT.FAILED);
                }
            }
        }.execute();
        
       
    }
    
    

}
