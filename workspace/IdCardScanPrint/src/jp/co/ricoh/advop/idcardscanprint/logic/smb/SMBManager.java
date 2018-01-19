package jp.co.ricoh.advop.idcardscanprint.logic.smb;


import android.util.Log;

import dalvik.system.DexClassLoader;
import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.ftp.FTPManager.UploadProgressListener;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.util.Util;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class SMBManager {
    
    public static final String SMB_AUTH_FAIL = "SMB_AUTH_FAIL";

    public static final String SMB_CONNECT_SUCCESSS = "SMB_CONNECT_SUCCESSS";
    public static final String SMB_CONNECT_FAIL = "SMB_CONNECT_FAIL";
    public static final String SMB_DISCONNECT_SUCCESS = "SMB_DISCONNECT_SUCCESS";

    public static final String SMB_UPLOAD_SUCCESS = "SMB_UPLOAD_SUCCESS";
    public static final String SMB_UPLOAD_FAIL = "SMB_UPLOAD_FAIL";
    
//    public static void uploadFile(File file, SMBInfo smbInfo,
//            UploadProgressListener listener) {
//        BufferedInputStream buffIn = null;
//        SmbFileOutputStream smbOut = null;
//        
//        try {
//            String domain = PreferencesUtil.getInstance().getDomain();
//            String remotePath = smbInfo.getPath() + "/" + Util.getFileName();
//            SmbFile smbFile = null;
//            if (CUtil.isStringEmpty(domain)) {
//                smbFile = new SmbFile(remotePath, new NtlmPasswordAuthentication(null, null, null));
//            } else if (CUtil.isStringEmpty(smbInfo.getUserName())) {
//                smbFile = new SmbFile(remotePath, new NtlmPasswordAuthentication(domain, null, null));
//            } else {
//                smbFile = new SmbFile(remotePath, new NtlmPasswordAuthentication(domain, smbInfo.getUserName(), smbInfo.getPassword()));
//            }
//            LogC.d("TEST","uploadFile:\npath:" + remotePath + "\ndomain:" + domain + "\nuserName:" + smbInfo.getUserName() + "\npassword:" + smbInfo.getPassword());
//            smbOut = new SmbFileOutputStream(smbFile);
//            buffIn = new BufferedInputStream(new FileInputStream(file));
//            
//            byte[] buffer = new byte[8192];
//            int length;
//            while ((length = buffIn.read(buffer)) != -1) {
//                smbOut.write(buffer, 0, length);
//                smbOut.flush();
//            }
//           
//            listener.onUploadProgress(SMBManager.SMB_UPLOAD_SUCCESS, 0, file);
//                   
//        } catch (SmbAuthException e) {
//            LogC.e("TEST","uploadFile  SmbAuthException", e);
//            listener.onUploadProgress(SMBManager.SMB_UPLOAD_FAIL, 0, file);
//                   
//        } catch (SmbException e) {
//            LogC.e("TEST","uploadFile  SmbException", e);
//            listener.onUploadProgress(SMBManager.SMB_UPLOAD_FAIL, 0, file);
//                   
//        } catch (MalformedURLException e) {
//            LogC.e("TEST","uploadFile  MalformedURLException", e);
//            listener.onUploadProgress(SMBManager.SMB_UPLOAD_FAIL, 0, file);
//                   
//        } catch (UnknownHostException e) {
//            LogC.e("TEST","uploadFile  UnknownHostException", e);
//            listener.onUploadProgress(SMBManager.SMB_UPLOAD_FAIL, 0, file);
//                 
//        } catch (IOException e) {
//            LogC.e("TEST","uploadFile  IOException", e);
//            listener.onUploadProgress(SMBManager.SMB_UPLOAD_FAIL, 0,  file);
//                   
//        } finally {
//            try {
//                if (null != smbOut)
//                    smbOut.close();
//                if (null != buffIn)
//                    buffIn.close();
//            } catch (Exception e2) {
//                LogC.e("TEST","uploadFile  IOException", e2);;
//            }
//            listener.onUploadProgress(SMBManager.SMB_DISCONNECT_SUCCESS, 0,
//                    null);
//        }
//    }
//    
//    public static String connect(SMBInfo smbInfo) {
//        try {
//            String domain = PreferencesUtil.getInstance().getDomain();
//            
//            SmbFile smbFile = null;
//            String path = smbInfo.getPath();
//            String username = smbInfo.getUserName();
//            String password = smbInfo.getPassword();
//            
//
//            
//            if (domain == null) {
//                smbFile = new SmbFile(path, new NtlmPasswordAuthentication(null, null, null));
//            } else if (username == null) {
//                smbFile = new SmbFile(path, new NtlmPasswordAuthentication(domain, null, null));
//            } else {
//                smbFile = new SmbFile(path, new NtlmPasswordAuthentication(domain, username, password));
//            }
//            
//            smbFile.connect();
//            return SMBManager.SMB_CONNECT_SUCCESSS;
//        } catch (SmbAuthException e) {
//            LogC.e("TEST","smb connect", e);
//            return SMBManager.SMB_AUTH_FAIL;
//        } catch (MalformedURLException e) {
//            LogC.e("TEST","smb connect", e);
//            return SMBManager.SMB_CONNECT_FAIL;
//        } catch (IOException e) {
//            LogC.e("TEST","smb connect", e);
//            return SMBManager.SMB_CONNECT_FAIL;
//        }
//    }
    
    
    /*****************************************************/
    
    public static void uploadFile(File file, SMBInfo smbInfo,
            UploadProgressListener listener) {
        DexClassLoader classLoader = CHolder.instance().getClassLoader();
        BufferedInputStream buffIn = null;
        
        try {
            String domain = PreferencesUtil.getInstance().getDomain();
            String remotePath = smbInfo.getPath();
            if (!remotePath.endsWith("/")) {
                remotePath = remotePath + "/";
            }
            String username = smbInfo.getUserName();
            String password = smbInfo.getPassword();
           
            Class<?> ntlmPasswordAuthenticationClass = classLoader.loadClass("jcifs.smb.NtlmPasswordAuthentication");
            Class<?>[] ntlmParams = {String.class, String.class, String.class};
            Constructor<?> ntlmConstructor = ntlmPasswordAuthenticationClass.getConstructor(ntlmParams);
            
            Object[] ntlmArgs;
            Object ntlmPasswordAuthentication;
            if (CUtil.isStringEmpty(username)) {
                ntlmPasswordAuthentication = null;
            }else if(CUtil.isStringEmpty(domain)&&CUtil.isStringEmpty(password)){
                ntlmArgs = new Object[] {"", null, null};
                ntlmPasswordAuthentication = ntlmConstructor.newInstance(ntlmArgs);
            } else {
                ntlmArgs = new Object[] {domain, username, password};
                ntlmPasswordAuthentication = ntlmConstructor.newInstance(ntlmArgs);
            }

            Class<?> smbFileClass = classLoader.loadClass("jcifs.smb.SmbFile");
            Object smbFile;
            if (ntlmPasswordAuthentication != null) {
                Class<?>[] smbParams = {String.class, ntlmPasswordAuthenticationClass};
                Constructor<?> smbConstructor = smbFileClass.getConstructor(smbParams);
                Object[] smbArgs= new Object[] {remotePath, ntlmPasswordAuthentication};
                smbFile = smbConstructor.newInstance(smbArgs);
            } else {
                Constructor<?> smbConstructor = smbFileClass.getConstructor(String.class);
                smbFile = smbConstructor.newInstance(remotePath);
            }
            
            Method listMethod = smbFileClass.getMethod("list");
            listMethod.setAccessible(true);
            String[] list = (String[]) listMethod.invoke(smbFile);
            List<String> files = Arrays.asList(list);
            String sendFile = CHolder.instance().getGlobalDataManager().getSendFileName();
            String fileName = Util.getFileNameNoSuffix(sendFile);
            String prefix = Util.getSuffix(sendFile);
            
            
            int index = 1;
            String lastFileName = fileName + "." + prefix;
            while (files.contains(lastFileName)) {
                lastFileName = fileName + "-" + index + "." + prefix;
                index++;
            }
            LogC.e("TEST","file sum="+files.size());
            remotePath = smbInfo.getPath() + "/" + lastFileName;

            if (ntlmPasswordAuthentication != null) {
                Class<?>[] smbParams = {String.class, ntlmPasswordAuthenticationClass};
                Constructor<?> smbConstructor = smbFileClass.getConstructor(smbParams);
                Object[] smbArgs= new Object[] {remotePath, ntlmPasswordAuthentication};
                smbFile = smbConstructor.newInstance(smbArgs);

                LogC.e("TEST","SMB upload:\nremote path:" + remotePath + "\ndomain:" + domain + "\nNot anonymous");
            } else {
                Constructor<?> smbConstructor = smbFileClass.getConstructor(String.class);
                smbFile = smbConstructor.newInstance(remotePath);

                LogC.e("TEST","SMB upload:\nremote path:" + remotePath + "\ndomain:" + domain + "\nAnonymous");
            }
            
            Class<?> smbFileOutputStreamClass = classLoader.loadClass("jcifs.smb.SmbFileOutputStream");
            Constructor<?> smbStreamConstructor = smbFileOutputStreamClass.getConstructor(smbFileClass);
            Object smbOut = smbStreamConstructor.newInstance(smbFile);

            Class<?>[] writeMethodParams = {byte[].class, int.class, int.class};
            Method writeMethod = smbFileOutputStreamClass.getMethod("write", writeMethodParams);
            Method flushMethod = smbFileOutputStreamClass.getMethod("flush");
            Method closeMethod = smbFileOutputStreamClass.getMethod("close");
            writeMethod.setAccessible(true);
            flushMethod.setAccessible(true);
            
            buffIn = new BufferedInputStream(new FileInputStream(file));
            
            byte[] buffer = new byte[8192];
            int length;
            
            while ((length = buffIn.read(buffer)) != -1) {
                writeMethod.invoke(smbOut, buffer, 0, length);
                flushMethod.invoke(smbOut);
            }
            
            listener.onUploadProgress(SMBManager.SMB_UPLOAD_SUCCESS, 0,
                    file);
            try {
                if (null != smbOut)
                    closeMethod.invoke(smbOut);
                if (null != buffIn)
                    buffIn.close();
            } catch (Exception e2) {
                LogC.e("TEST","SMB disconnect failed!\n" + e2.getLocalizedMessage());
            }
        } catch (Exception e) {
            
            if (e.getClass().equals(InvocationTargetException.class)) {
                Throwable realError = ((InvocationTargetException) e).getTargetException();
                LogC.e("TEST","SMB upload failed!\n" + realError.getLocalizedMessage());
            } else {
                LogC.e("TEST","SMB upload failed!\n" + e.getLocalizedMessage());
            }
           e.printStackTrace();
            try {
                if (null != buffIn) {
                    buffIn.close();
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } finally {
                listener.onUploadProgress(SMBManager.SMB_UPLOAD_FAIL, 0,
                        file);
            }
        }
    }
    
    public static String connect(SMBInfo smbInfo) {
        DexClassLoader classLoader = CHolder.instance().getClassLoader();
        
        try {
            String domain = PreferencesUtil.getInstance().getDomain();
            String path = smbInfo.getPath();
            String username = smbInfo.getUserName();
            String password = smbInfo.getPassword();
            if (!path.endsWith("/")) {
                path = path + "/"+System.currentTimeMillis();
            }
            // Improve performance
//            Class<?> configClass = classLoader.loadClass("jcifs.Config");
//            Class<?>[] configParams = {String.class, String.class};
//            Method setPropertyMethod = configClass.getMethod("setProperty", configParams);
//            setPropertyMethod.setAccessible(true);
//            Object[] configArgs= new Object[] {"resolveOrder", "DNS"};
//            setPropertyMethod.invoke(null, configArgs);
//            
//            configArgs= new Object[] {"jcifs.smb.client.dfs.disabled", "true"};
//            setPropertyMethod.invoke(null, configArgs);
//            System.setProperty("jcifs.smb.client.dfs.disabled", "true");
            
            
            
            Class<?> ntlmPasswordAuthenticationClass = classLoader.loadClass("jcifs.smb.NtlmPasswordAuthentication");
            Class<?>[] ntlmParams = {String.class, String.class, String.class};
            Constructor<?> ntlmConstructor = ntlmPasswordAuthenticationClass.getConstructor(ntlmParams);
            
            Object[] ntlmArgs;
            Object ntlmPasswordAuthentication;
            if (CUtil.isStringEmpty(domain)&&CUtil.isStringEmpty(username)) {
                ntlmPasswordAuthentication = null;
            } else if (!CUtil.isStringEmpty(domain) && CUtil.isStringEmpty(username)) {
                ntlmArgs = new Object[] {domain, null, null};
                ntlmPasswordAuthentication = ntlmConstructor.newInstance(ntlmArgs);
            }else if(CUtil.isStringEmpty(domain)&&CUtil.isStringEmpty(password)){
                ntlmArgs = new Object[] {domain, null, null};
                ntlmPasswordAuthentication = ntlmConstructor.newInstance(ntlmArgs);
            }else{
                ntlmArgs = new Object[] {domain, username, password};
                ntlmPasswordAuthentication = ntlmConstructor.newInstance(ntlmArgs);
            }
//            if(CUtil.isStringEmpty(password)){
//                ntlmPasswordAuthentication = null;
//            }else{
//                ntlmArgs = new Object[] {domain, username, password};
//                ntlmPasswordAuthentication = ntlmConstructor.newInstance(ntlmArgs);
//            }
            Log.e("TEST", "username="+username);
            Class<?> smbFileClass = classLoader.loadClass("jcifs.smb.SmbFile");
            Object smbFile;
            if (ntlmPasswordAuthentication != null) {
                Class<?>[] smbParams = {String.class, ntlmPasswordAuthenticationClass};
                Constructor<?> smbConstructor = smbFileClass.getConstructor(smbParams);
                Object[] smbArgs= new Object[] {path, ntlmPasswordAuthentication};
                smbFile = smbConstructor.newInstance(smbArgs);

                LogC.d("TEST","SMB auth test:\npath:" + path + "\ndomain:" + domain + "\nNot anonymous");
            } else {
                Constructor<?> smbConstructor = smbFileClass.getConstructor(String.class);
                smbFile = smbConstructor.newInstance(path);

                LogC.d("TEST","SMB no authtest:\npath:" + path + "\ndomain:" + domain + "\nAnonymous");
            }
            Method connectMethod = smbFileClass.getDeclaredMethod("delete");
            connectMethod.setAccessible(true);
            
            Class<?> smbFileOutputStreamClass = classLoader.loadClass("jcifs.smb.SmbFileOutputStream");
            Constructor<?> smbStreamConstructor = smbFileOutputStreamClass.getConstructor(smbFileClass);
            Object smbOut = smbStreamConstructor.newInstance(smbFile);

            Class<?>[] writeMethodParams = {byte[].class, int.class, int.class};
            Method writeMethod = smbFileOutputStreamClass.getMethod("write", writeMethodParams);
            Method flushMethod = smbFileOutputStreamClass.getMethod("flush");
            Method closeMethod = smbFileOutputStreamClass.getMethod("close");
            writeMethod.setAccessible(true);
            flushMethod.setAccessible(true);
            
            byte[] buffer = "test".getBytes();
                writeMethod.invoke(smbOut,buffer , 0,buffer.length );
                flushMethod.invoke(smbOut);
                try {
                    if (null != smbOut)
                        closeMethod.invoke(smbOut);
                } catch (Exception e2) {
                    LogC.e("TEST","SMB disconnect failed!\n" + e2.getLocalizedMessage());
                }
                connectMethod.invoke(smbFile);
            return SMBManager.SMB_CONNECT_SUCCESSS;
        } catch (Exception e) {
            try {
                if (e.getClass().equals(InvocationTargetException.class)) {
                    Throwable realError = ((InvocationTargetException) e).getTargetException();
                    LogC.e("TEST","SMB connect failed!\n" + realError.getLocalizedMessage());
                    Class<?> smbAuthExceptionClass = classLoader.loadClass("jcifs.smb.SmbAuthException");
                    if (realError.getClass().equals(smbAuthExceptionClass)) {
                        return SMBManager.SMB_AUTH_FAIL;
                    }
                } else {
                    LogC.e("TEST","SMB connect failed!\n" + e.getLocalizedMessage());
                }
                return SMBManager.SMB_CONNECT_FAIL;
            } catch (ClassNotFoundException e1) {
                LogC.e("TEST","SMB connect failed!\n" + e1.getLocalizedMessage());
                return SMBManager.SMB_CONNECT_FAIL;
            }
        }
    }

}
