
package jp.co.ricoh.advop.idcardscanprint.logic.job;

import android.os.AsyncTask;

import jp.co.ricoh.advop.cheetahutil.util.HDDUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.logic.CHolder;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.EXE_RESULT;
import jp.co.ricoh.advop.idcardscanprint.logic.JobManager.ExeCallback;
import jp.co.ricoh.advop.idcardscanprint.scan.ScanStateMachine.ScanEvent;
import jp.co.ricoh.advop.idcardscanprint.util.Const;

public class JobScanFront extends LongJob {

    public JobScanFront(ExeCallback exeCallback) {
        super(exeCallback);
    }

    @Override
    public void onStart() {
        CHolder.instance().getScanManager().getStateMachine().procScanEvent(ScanEvent.REQUEST_JOB_START);
    }

    public void GetFrontImgFinished() {
     // TODO asyc copy file
        new AsyncTask<Void, Void, EXE_RESULT>() {

            @Override
            protected EXE_RESULT doInBackground(Void... params) {
                EXE_RESULT ret = EXE_RESULT.SUCCESSED;
                //EXE_RESULT retTmp= params[0];
                //if(retTmp == EXE_RESULT.SUCCESSED) {
                 String frontPathTmp = CHolder.instance().getJobData().getPathFrontImg();
                 String frontPath = Const.SCAN_FILE_PATH + "front.jpg";
                 LogC.d("src path:" + frontPathTmp + "dst path:" + frontPath);
                 boolean result = HDDUtil.copyFile(frontPathTmp, frontPath);
                 if(!result) {
                     ret = EXE_RESULT.ERR_COPY_FILE;
                     LogC.e("copy file fail!");
                 }
                 
               //}
                 CHolder.instance().getScanManager().getStateMachine().delTempScanedFile();
                 return ret;
                
            }
            
            @Override
            protected void onPostExecute(EXE_RESULT result) {
                complete(result);               
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//.execute();
        
        
    }
 

}
