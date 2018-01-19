package jp.co.ricoh.advop.idcardscanprint.logic.ftp;

import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.smb.SMBManager;
import jp.co.ricoh.advop.idcardscanprint.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

public class FTPManager {
    
    public static final String FTP_AUTH_FAIL = "FTP_AUTH_FAIL";
    
    public static final String FTP_CONNECT_SUCCESSS = "FTP_CONNECT_SUCCESSS";
    public static final String FTP_CONNECT_FAIL = "FTP_CONNECT_FAIL";
    public static final String FTP_DISCONNECT_SUCCESS = "FTP_DISCONNECT_SUCCESS";
    public static final String FTP_DISCONNECT_FAIL = "FTP_DISCONNECT_FAIL";

    public static final String FTP_UPLOAD_SUCCESS = "FTP_UPLOAD_SUCCESS";
    public static final String FTP_UPLOAD_FAIL = "FTP_UPLOAD_FAIL";
    public static final String FTP_UPLOAD_LOADING = "FTP_UPLOAD_LOADING";

    private FTPClient ftpClient;

    public FTPManager() {
        this.ftpClient = new FTPClient();
    }

    public void uploadFile(File file, FTPInfo ftpInfo,
                                 UploadProgressListener listener) {

        String result = this.connect(ftpInfo);
        if (!FTP_CONNECT_SUCCESSS.equals(result)) {
            listener.onUploadProgress(result, 0,
                    file);
        }
        
        InputStream inputStream = null;
        
        try {
            ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
            ftpClient.makeDirectory(ftpInfo.getRemotePath());
            
            ftpClient.changeWorkingDirectory(ftpInfo.getRemotePath());
            
            boolean flag = true;
            inputStream = new FileInputStream(file);
            String sendFile = CHolder.instance().getGlobalDataManager().getSendFileName();
            String fileName = Util.getFileNameNoSuffix(sendFile);
            String prefix = Util.getSuffix(sendFile);
            List<String> files = Arrays.asList(ftpClient.listNames());
            
            int index = 1;
            String lastFileName = fileName + "." + prefix;
            while (files.contains(lastFileName)) {
                lastFileName = fileName + "-" + index + "." + prefix;
                index++;
            }
            
            flag = ftpClient.storeFile(lastFileName, inputStream);
            inputStream.close();
            
            if (flag) {
                listener.onUploadProgress(FTPManager.FTP_UPLOAD_SUCCESS, 0,
                        file);
            } else {
                int reply = ftpClient.getReplyCode();
                LogC.e("FTP upload failed! code=" + reply);
                listener.onUploadProgress(FTPManager.FTP_UPLOAD_FAIL, 0,
                        file);
            }

            this.closeConnection();
        } catch (Exception e) {
            LogC.e("FTP upload failed!\n" + e.getLocalizedMessage());
            this.closeConnection();
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e1) {
                // do nothing
            } finally {
                listener.onUploadProgress(FTPManager.FTP_UPLOAD_FAIL, 0,
                        file);
            }
        }
        
        
    }

    public String connect(FTPInfo ftpInfo) {
        if (ftpClient.isConnected()) {
            this.closeConnection();
        }
        
        int reply;
        try {

            ftpClient.setControlEncoding(ftpInfo.getControlEncoding());
            ftpClient.setConnectTimeout(30000);
            ftpClient.setDefaultTimeout(30000);
            
            ftpClient.setControlKeepAliveTimeout(60); // set timeout to 1 minutes
            
            ftpClient.connect(ftpInfo.getHostName(), ftpInfo.getServerPort());

            reply = ftpClient.getReplyCode();
            LogC.d("FTP connect reply:" + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                this.closeConnection();
                return FTPManager.FTP_CONNECT_FAIL;
            }
            
            ftpClient.setSoTimeout(30000);
            
            boolean loginflag = false;
            if (CUtil.isStringEmpty(ftpInfo.getUserName())) {
                loginflag = ftpClient.login("anonymous", "anonymous");

                LogC.d("FTP connect:\nserver:" + ftpInfo.getHostName() + "\npath:" + ftpInfo.getRemotePath() + "\nAnonymous");
            } else {
                loginflag = ftpClient.login(ftpInfo.getUserName(), ftpInfo.getPassword());

                LogC.d("FTP connect:\nserver:" + ftpInfo.getHostName() + "\npath:" + ftpInfo.getRemotePath() + "\nNot anonymous");
            }
            
            reply = ftpClient.getReplyCode();
            if (!loginflag || !FTPReply.isPositiveCompletion(reply)) {
                LogC.e("FTP auth failed!");
                this.closeConnection();
                return FTPManager.FTP_AUTH_FAIL;
            }

            ftpClient.enterLocalPassiveMode();
            ftpClient.setDataTimeout(30000);
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            return FTPManager.FTP_CONNECT_SUCCESSS;
            
        } catch (Exception e) {
            this.closeConnection();
            LogC.e("FTP connect failed!\n" + e.getLocalizedMessage());
            return FTPManager.FTP_CONNECT_FAIL;
        }
    }
    
    public String closeConnection() {
        if (ftpClient != null) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (Exception e) {
                LogC.e("FTP disconnect failed!\n" + e.getLocalizedMessage());
                return FTPManager.FTP_DISCONNECT_FAIL;
            }
        }
        return FTPManager.FTP_DISCONNECT_SUCCESS;
    }
    
    public interface UploadProgressListener {
        public void onUploadProgress(String currentStep, long uploadSize, File file);
    }

}
