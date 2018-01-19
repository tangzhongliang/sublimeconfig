package jp.co.ricoh.advop.idcardscanprint.logic;

import android.content.Context;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;

import dalvik.system.DexClassLoader;
import jp.co.ricoh.advop.cheetahutil.util.Cancelable;
import jp.co.ricoh.advop.cheetahutil.util.CancelableAsyncTask;
import jp.co.ricoh.advop.cheetahutil.util.HDDUtil;
import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.application.CheetahApplication;
import jp.co.ricoh.advop.idcardscanprint.application.MachineStatus;
import jp.co.ricoh.advop.idcardscanprint.logic.job.JobPrintAndDelete;
import jp.co.ricoh.advop.idcardscanprint.logic.job.JobScanBackAndCombine;
import jp.co.ricoh.advop.idcardscanprint.logic.job.JobScanFront;
import jp.co.ricoh.advop.idcardscanprint.logic.job.JobSend;
import jp.co.ricoh.advop.idcardscanprint.print.PrintManager;
import jp.co.ricoh.advop.idcardscanprint.print.PrintStateMachine.PrintEvent;
import jp.co.ricoh.advop.idcardscanprint.scan.ScanManager;
import jp.co.ricoh.advop.idcardscanprint.scan.ScanStateMachine.ScanEvent;
import jp.co.ricoh.advop.idcardscanprint.ui.fragment.ScanFrontFragment;
import jp.co.ricoh.advop.idcardscanprint.util.Const;
import jp.co.ricoh.advop.idcardscanprint.util.Util;
import jp.co.ricoh.advop.idcardscanprint.model.PreferencesUtil;
import java.io.File;
import java.util.List;

// all public functions should be run in UI thread
public class JobManager {
//    private final static String TAG = JobManager.class.getSimpleName();
    private CheetahApplication application;
    private final static int MIN_INIT_TIME = 2000;
    private ScanManager scanManager;
    private PrintManager printManager;
    private MachineStatus machineStatus;
    
    private CancelableAsyncTask<Void, Void, EXE_RESULT> initAppTask = null;
    
    public enum EXE_RESULT {
        CANCELED,
        SUCCESSED,
        FAILED,
        
        ERR_SCAN,
        ERR_GET_SCANNED_FILE,
        ERR_PRINT,
        ERR_DELETE_TMP_FILE,
        ERR_HDD_NOT_AVAILABLE,
        ERR_LOGIN_USER_FAILED,
        ERR_INIT,
        
        ERR_COPY_FILE,
        ERR_COMBINED_FILE,
        ERR_RESTRICT_DEVICE_INSTALLED,
    }
    public interface ExeCallback {
        public void onComplete(EXE_RESULT ret);
    }

    public JobManager() {
        application = CHolder.instance().getApplication();
    }
    
    public void cancelInitTask() {
        if (initAppTask != null && initAppTask.getStatus() == Status.RUNNING) {
            initAppTask.cheetahCancel(); 
        }
    }
    
    // should be canceled when onDestroy
    public Cancelable initApp(final ExeCallback callback) {
        scanManager = CHolder.instance().getScanManager();
        printManager = CHolder.instance().getPrintManager();
        scanManager.getStateMachine().procScanEvent(ScanEvent.ACTIVITY_CREATED);
        printManager.getStateMachine().procPrintEvent(PrintEvent.CHANGE_APP_ACTIVITY_INITIAL);
        machineStatus = CHolder.instance().getMachineStatusData();
        CHolder.instance().getJobData().setCurrentJob(null);
        CHolder.instance().getLockManager().unlock();
       
        initAppTask = new CancelableAsyncTask<Void, Void, EXE_RESULT>()  {
            @Override
            protected EXE_RESULT doInBackground(Void... params) {
                long stTime = System.currentTimeMillis();
                EXE_RESULT ret = EXE_RESULT.SUCCESSED;
                CHolder.instance().setEntries(null);
                CHolder.instance().setLinkedSelects(null);
                PreferencesUtil.getInstance().clear();
                machineStatus.init();
                
                // get login info
                if (!LogC.runInEMU) {
                    LogC.d("initLoginUserInfo");
                    if (!application.initLoginUserInfo()) {
                        return EXE_RESULT.ERR_LOGIN_USER_FAILED;
                    }
                }
                
                // init scan service
                LogC.d("intiScanService");
                ret = scanManager.intiScanService(this);
                if (ret != EXE_RESULT.SUCCESSED) {
                    return ret;
                }
                
                // init Print service
                LogC.d("intiPrintService");
                ret = printManager.intiPrintService(this);
                if (ret != EXE_RESULT.SUCCESSED) {
                    return ret;
                }

                // wait hdd
                LogC.d("waitHDDStart");
                if (!application.waitHDDStart()) {
                    return EXE_RESULT.ERR_HDD_NOT_AVAILABLE;
                }
                                
                // load assert jar
                LogC.d("load assert jar");
                final String libPath = "/mnt/hdd/" + CHolder.instance().getApplication().getPackageName() + "/jcifs/" + "jcifs.jar";
                String path = HDDUtil.copyAssertJarToFile(CHolder.instance().getApplication(),
                        "jcifs.jar", libPath);
                if (path != null) {
                    final File optimizedDexOutputPath = application.getDir("jcifs", Context.MODE_PRIVATE);
//                    final String optimizedDexOutputPath = "/mnt/hdd/" + CHolder.instance().getApplication().getPackageName() + "/jcifs/";
                    DexClassLoader classLoader = new DexClassLoader(path,
                            optimizedDexOutputPath.getAbsolutePath(), null,
                            application.getClassLoader());
                    CHolder.instance().setClassLoader(classLoader);

                } else {
                    ret = EXE_RESULT.FAILED;
                }

                if (!isCancelled()) {
                    // if time < 2s then wait to 2s
                    long waitTime = MIN_INIT_TIME - (System.currentTimeMillis() - stTime);
                    if (waitTime > 100) {
                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException e) {
                            LogC.w("wait init", e);
                        }
                        
                    }
                }
                
                return ret;
            }

            @Override
            protected void onPostExecute(EXE_RESULT ret) {
//                CHolder.instance().getLockManager().unlock();
                if (isCancelled()) {
                    return;
                }
                callback.onComplete(ret);
            }
            
            @Override
            public void cheetahCancel() {
                this.cancel(false);
            }
        };
        initAppTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return initAppTask;
    }
    
    public void destroyApp() {
        scanManager.destroyScanService();
        printManager.destroyPrintService();
        HDDUtil.deleteFile(Const.SCAN_FILE_PATH);
    }

    public void startScanFront(ExeCallback exeCallback) {
        new JobScanFront(exeCallback).start();
    }
    
    public void startScanBackAndCombine(ExeCallback exeCallback) {
        new JobScanBackAndCombine(exeCallback).start();
    }
    
    public void startSend(ExeCallback exeCallback) {
        // TODO
    }
    
    public void startPrintAndDelete(ExeCallback exeCallback) {
        new JobPrintAndDelete(exeCallback).start();
    }
    
    public void startSendFile(ExeCallback exeCallback) {
        LogC.d("start send file job");
        CHolder.instance().getGlobalDataManager().setCloseDlg(false);
        new JobSend(exeCallback).start();
    }

}
