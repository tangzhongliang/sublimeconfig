package jp.co.ricoh.advop.idcardscanprint.logic.job;

import android.os.AsyncTask;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.co.ricoh.advop.cheetahutil.util.CUtil;
import jp.co.ricoh.advop.cheetahutil.util.HDDUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.JpegProcessManager;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.EXE_RESULT;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.ExeCallback;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import jp.co.ricoh.advop.idcardscanprint.scan.ScanStateMachine.ScanEvent;
import jp.co.ricoh.advop.idcardscanprint.ssdk.wrapper.client.RestClient;
import jp.co.ricoh.advop.idcardscanprint.util.Const;
import jp.co.ricoh.advop.idcardscanprint.util.Util;

public class JobScanBackAndCombine extends LongJob {

    public JobScanBackAndCombine(ExeCallback exeCallback) {
        super(exeCallback);
    }

    @Override
    public void onStart() {
        CHolder.instance().getScanManager().getStateMachine().procScanEvent(ScanEvent.REQUEST_JOB_START);
    }
    
    private void delOriginalTmpFiles() {
        CHolder.instance().getScanManager().getStateMachine().delTempScanedFile();
    }
    
    public void GetBackImgFinished() {
        // TODO combine
        new AsyncTask<Void, Void, EXE_RESULT>() {

            @Override
            protected EXE_RESULT doInBackground(Void... params) {
                EXE_RESULT ret = EXE_RESULT.SUCCESSED;
                //EXE_RESULT retTmp= params[0];
               // if(retTmp == EXE_RESULT.SUCCESSED) {
                    String BackPathTmp = CHolder.instance().getJobData().getPathBackImg();
                    String backPath = Const.SCAN_FILE_PATH + "back.jpg";
                    
                    boolean result = HDDUtil.copyFile(BackPathTmp, backPath);
                    delOriginalTmpFiles();
                    if(result) {
                        //String frontPath = CHolder.instance().getJobData().getPathFrontImg();
                        String frontPath = Const.SCAN_FILE_PATH + "front.jpg";
                        
                        String pathCombinedJpg = Const.SCAN_FILE_PATH + Const.COMBILE_FILE_NAME;
                        if(Util.fileIsExists(frontPath) && Util.fileIsExists(backPath)) {
                            try {
                                result = JpegProcessManager.jpeg2in1(frontPath, backPath, pathCombinedJpg);
                            } catch (FileNotFoundException e) {
                                result = false;
                               LogC.w(e);
                               
                            }
                            if(result == false) {
                                return EXE_RESULT.ERR_COMBINED_FILE;
                            }
                            CHolder.instance().getJobData().setPathCombinedJPG(pathCombinedJpg);
                            
                            //file name check and save
                            String newFileName = Util.delIllegalWord(Util.getFileName(), Const.FILE_NAME_ILLEGALWORD, Const.invaidPattern);
                            
                            if (CUtil.isStringEmpty(newFileName)) {
                                newFileName = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());//Util.getDefaultTime();
                            }
                            LogC.d("set print file path:" + pathCombinedJpg);
                            String fileType = (String)PreferencesUtil.getInstance().getSelectedFileFormatValue().getItemValue();
                            if(fileType.equalsIgnoreCase("pdf")) {                                                
                                //String pathPdf = Const.SCAN_FILE_PATH + Util.getFileName() + Const.SEND_FILE_PDF;
                                String pathPdf = Const.SCAN_FILE_PATH + newFileName + Const.SEND_FILE_PDF;
                                CHolder.instance().getGlobalDataManager().setSendFileName(pathPdf);
                                LogC.d("pdf file name is" + pathPdf);
                                JpegProcessManager.JpegToPdf(pathCombinedJpg, pathPdf);
                                
                            } else if(fileType.equalsIgnoreCase("tiff")){
                                //String pathTiff = Const.SCAN_FILE_PATH + Util.getFileName() + Const.SEND_FILE_TIFF;
                                String pathTiff = Const.SCAN_FILE_PATH + newFileName + Const.SEND_FILE_TIFF;
                                CHolder.instance().getGlobalDataManager().setSendFileName(pathTiff);
                                LogC.d("tiff file name is" + pathTiff);
                                JpegProcessManager.JpegToTiff(pathCombinedJpg, pathTiff);
                                
                            } else {                                
                                //String pathJpg = Const.SCAN_FILE_PATH + Util.getFileName() + Const.SEND_FILE_JPG;
                                String pathJpg = Const.SCAN_FILE_PATH + newFileName + Const.SEND_FILE_JPG;
                                CHolder.instance().getGlobalDataManager().setSendFileName(pathJpg);
                                HDDUtil.copyFile(pathCombinedJpg, pathJpg);
                                LogC.d("jpg file name is" + pathJpg);
                               
                            }
                        } else {
                            ret = EXE_RESULT.ERR_COMBINED_FILE;
                            CHolder.instance().getGlobalDataManager().setSendFileName(null);
                            LogC.d("combine file fail!");
                        }
                    } else {
                        ret = EXE_RESULT.ERR_COPY_FILE;
                    }
                    
                    
               // }
                return ret;
                
            }
            
            @Override
            protected void onPostExecute(EXE_RESULT result) {                
               complete(result);                
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//.execute();
        
        
    }
}
